package org.mqttbee.mqtt5.handler.auth;

import io.netty.channel.ChannelHandlerContext;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.api.mqtt5.Mqtt5ClientData;
import org.mqttbee.api.mqtt5.auth.Mqtt5EnhancedAuthProvider;
import org.mqttbee.api.mqtt5.exception.Mqtt5MessageException;
import org.mqttbee.api.mqtt5.message.auth.Mqtt5Auth;
import org.mqttbee.api.mqtt5.message.auth.Mqtt5AuthBuilder;
import org.mqttbee.api.mqtt5.message.disconnect.Mqtt5Disconnect;
import org.mqttbee.api.mqtt5.message.disconnect.Mqtt5DisconnectReasonCode;
import org.mqttbee.mqtt.message.auth.MqttAuthBuilderImpl;
import org.mqttbee.mqtt.message.auth.MqttAuthImpl;
import org.mqttbee.mqtt.message.disconnect.MqttDisconnectImpl;
import org.mqttbee.mqtt5.Mqtt5ClientDataImpl;
import org.mqttbee.mqtt5.handler.disconnect.Mqtt5DisconnectUtil;

import static org.mqttbee.api.mqtt5.message.auth.Mqtt5AuthReasonCode.CONTINUE_AUTHENTICATION;
import static org.mqttbee.api.mqtt5.message.auth.Mqtt5AuthReasonCode.REAUTHENTICATE;

/**
 * Enhanced reauth handling according during connection according to the MQTT 5 specification.
 *
 * @author Silvio Giebl
 */
public class Mqtt5ReAuthHandler extends AbstractMqtt5AuthHandler {

    public static final String NAME = "reauth";

    Mqtt5ReAuthHandler() {
    }

    @Override
    public void userEventTriggered(final ChannelHandlerContext ctx, final Object evt) {
        if (evt instanceof Mqtt5ReAuthEvent) {
            writeReAuth(ctx);
        } else {
            ctx.fireUserEventTriggered(evt);
        }
    }

    /**
     * Sends a AUTH message with the Reason Code REAUTHENTICATE. Calls
     * {@link Mqtt5EnhancedAuthProvider#onReAuth(Mqtt5ClientData, Mqtt5AuthBuilder)}.
     *
     * @param ctx the channel handler context.
     */
    private void writeReAuth(@NotNull final ChannelHandlerContext ctx) {
        final Mqtt5ClientDataImpl clientData = Mqtt5ClientDataImpl.from(ctx.channel());
        final Mqtt5EnhancedAuthProvider enhancedAuthProvider = getEnhancedAuthProvider(clientData);
        final MqttAuthBuilderImpl authBuilder = getAuthBuilder(REAUTHENTICATE, enhancedAuthProvider);

        enhancedAuthProvider.onReAuth(clientData, authBuilder)
                .thenRunAsync(() -> ctx.writeAndFlush(authBuilder.build()), ctx.executor());
    }

    @Override
    public void channelRead(final ChannelHandlerContext ctx, final Object msg) {
        if (msg instanceof MqttAuthImpl) {
            readAuth(ctx, (MqttAuthImpl) msg);
        } else if (msg instanceof MqttDisconnectImpl) {
            readDisconnect(ctx, (MqttDisconnectImpl) msg);
        } else {
            ctx.fireChannelRead(msg);
        }
    }

    /**
     * Handles an incoming AUTH message with the Reason Code SUCCESS.
     * <ul>
     * <li>Calls {@link Mqtt5EnhancedAuthProvider#onReAuthSuccess(Mqtt5ClientData, Mqtt5Auth)}.</li>
     * <li>Sends a DISCONNECT message if the enhanced auth provider did not accept the AUTH message.</li>
     * </ul>
     *
     * @param ctx                  the channel handler context.
     * @param auth                 the incoming AUTH message.
     * @param clientData           the data of the client.
     * @param enhancedAuthProvider the enhanced auth provider.
     */
    void readAuthSuccess(
            @NotNull final ChannelHandlerContext ctx, @NotNull final MqttAuthImpl auth,
            @NotNull final Mqtt5ClientDataImpl clientData,
            @NotNull final Mqtt5EnhancedAuthProvider enhancedAuthProvider) {

        enhancedAuthProvider.onReAuthSuccess(clientData, auth).thenAcceptAsync(accepted -> {
            if (!accepted) {
                Mqtt5DisconnectUtil.disconnect(
                        ctx.channel(), Mqtt5DisconnectReasonCode.NOT_AUTHORIZED, "Server auth success not accepted");
            }
        }, ctx.executor());
    }

    /**
     * Handles an incoming AUTH message with the Reason Code REAUTHENTICATE.
     * <ul>
     * <li>Sends a DISCONNECT message if server reauth is not allowed.</li>
     * <li>Otherwise calls
     * {@link Mqtt5EnhancedAuthProvider#onServerReAuth(Mqtt5ClientData, Mqtt5Auth, Mqtt5AuthBuilder)}.</li>
     * <li>Sends a new AUTH message if the enhanced auth provider accepted the incoming AUTH message.</li>
     * <li>Otherwise sends a DISCONNECT message.</li>
     * </ul>
     *
     * @param ctx                  the channel handler context.
     * @param auth                 the incoming AUTH message.
     * @param clientData           the data of the client.
     * @param enhancedAuthProvider the enhanced auth provider.
     */
    void readReAuth(
            @NotNull final ChannelHandlerContext ctx, @NotNull final MqttAuthImpl auth,
            @NotNull final Mqtt5ClientDataImpl clientData,
            @NotNull final Mqtt5EnhancedAuthProvider enhancedAuthProvider) {

        if (clientData.allowsServerReAuth()) {
            final MqttAuthBuilderImpl authBuilder = getAuthBuilder(CONTINUE_AUTHENTICATION, enhancedAuthProvider);

            enhancedAuthProvider.onServerReAuth(clientData, auth, authBuilder).thenAcceptAsync(accepted -> {
                if (accepted) {
                    ctx.writeAndFlush(authBuilder.build());
                } else {
                    Mqtt5DisconnectUtil.disconnect(ctx.channel(), Mqtt5DisconnectReasonCode.NOT_AUTHORIZED,
                            new Mqtt5MessageException(auth, "Server reauth not accepted"));
                }
            }, ctx.executor());
        } else {
            Mqtt5DisconnectUtil.disconnect(ctx.channel(), Mqtt5DisconnectReasonCode.PROTOCOL_ERROR,
                    new Mqtt5MessageException(auth, "Server must not send AUTH with the Reason Code REAUTHENTICATE"));
        }
    }

    /**
     * Handles an incoming DISCONNECT message. Calls
     * {@link Mqtt5EnhancedAuthProvider#onReAuthError(Mqtt5ClientData, Mqtt5Disconnect)}.
     *
     * @param ctx        the channel handler context.
     * @param disconnect the incoming DISCONNECT message.
     */
    private void readDisconnect(
            @NotNull final ChannelHandlerContext ctx, @NotNull final MqttDisconnectImpl disconnect) {

        cancelTimeout();

        final Mqtt5ClientDataImpl clientData = Mqtt5ClientDataImpl.from(ctx.channel());
        final Mqtt5EnhancedAuthProvider enhancedAuthProvider = getEnhancedAuthProvider(clientData);

        enhancedAuthProvider.onReAuthError(clientData, disconnect);

        ctx.fireChannelRead(disconnect);
    }

    @NotNull
    @Override
    protected String getTimeoutReasonString() {
        return "Timeout while waiting for AUTH or DISCONNECT";
    }

}