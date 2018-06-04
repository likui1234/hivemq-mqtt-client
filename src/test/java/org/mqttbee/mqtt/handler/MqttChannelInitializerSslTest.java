/*
 * Copyright 2018 The MQTT Bee project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.mqttbee.mqtt.handler;

import io.netty.channel.Channel;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.handler.ssl.SslHandler;
import io.reactivex.SingleEmitter;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mqttbee.api.mqtt.MqttClientSslConfig;

import org.mqttbee.api.mqtt.mqtt5.message.connect.connack.Mqtt5ConnAck;
import org.mqttbee.mqtt.MqttClientData;
import org.mqttbee.mqtt.message.connect.MqttConnect;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.TrustManagerFactory;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author Christoph Schäbel
 */
public class MqttChannelInitializerSslTest {

    @Mock
    private MqttConnect mqtt5Connect;

    @Mock
    private SingleEmitter<Mqtt5ConnAck> connAckEmitter;

    @Mock
    private MqttClientData clientData;

    private Channel channel;

    @Before
    public void before() {
        MockitoAnnotations.initMocks(this);
        channel = new EmbeddedChannel();
        when(clientData.getSslConfig()).thenReturn(Optional.empty());
    }

    @Test(expected = IllegalStateException.class)
    public void test_initialize_no_ssldata_present() {
        when(clientData.usesSsl()).thenReturn(true);

        final MqttChannelInitializer mqttChannelInitializer =
                new MqttChannelInitializer(mqtt5Connect, connAckEmitter, clientData);

        mqttChannelInitializer.initChannel(channel);
    }

    @Test
    public void test_initialize_default_ssldata() {

        when(clientData.usesSsl()).thenReturn(true);
        when(clientData.getSslConfig()).thenReturn(Optional.of(mock(TestSslConfig.class)));

        final MqttChannelInitializer mqttChannelInitializer =
                new MqttChannelInitializer(mqtt5Connect, connAckEmitter, clientData);

        mqttChannelInitializer.initChannel(channel);

        assertNotNull(channel.pipeline().get(SslHandler.class));
    }

    private static class TestSslConfig implements MqttClientSslConfig {

        private final KeyManagerFactory keyManagerFactory;
        private final TrustManagerFactory trustManagerFactory;
        private final List<String> cipherSuites;
        private final List<String> protocols;
        private final int handshakeTimeout;

        private TestSslConfig(
                final KeyManagerFactory keyManagerFactory, final TrustManagerFactory trustManagerFactory,
                final List<String> cipherSuites, final List<String> protocols, final int handshakeTimeout) {
            this.keyManagerFactory = keyManagerFactory;
            this.trustManagerFactory = trustManagerFactory;
            this.cipherSuites = cipherSuites;
            this.protocols = protocols;
            this.handshakeTimeout = handshakeTimeout;
        }

        @Override
        public KeyManagerFactory getKeyManagerFactory() {
            return keyManagerFactory;
        }

        @Override
        public TrustManagerFactory getTrustManagerFactory() {
            return trustManagerFactory;
        }

        @Override
        public List<String> getCipherSuites() {
            return cipherSuites;
        }

        @Override
        public List<String> getProtocols() {
            return protocols;
        }

        @Override
        public int getHandshakeTimeoutMs() {
            return handshakeTimeout;
        }
    }


}