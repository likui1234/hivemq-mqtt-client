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
 *
 */

package org.mqttbee.mqtt.mqtt5;

import org.jetbrains.annotations.NotNull;
import org.mqttbee.internal.mqtt.MqttRxClientBuilder;
import org.mqttbee.mqtt.MqttClient;

/**
 * MQTT client according to the MQTT 5.0 specification.
 *
 * @author Silvio Giebl
 */
public interface Mqtt5Client extends MqttClient {

    static @NotNull Mqtt5ClientBuilder builder() {
        return new MqttRxClientBuilder();
    }

    @Override
    @NotNull Mqtt5ClientConfig getConfig();

    /**
     * Turns the API of this client into a reactive API.
     * <p>
     * The reactive API can be used simultaneously with the other APIs.
     *
     * @return a reactive API for this client.
     */
    @NotNull Mqtt5RxClient toRx();

    /**
     * Turns the API of this client into a asynchronous API based on futures and callbacks.
     * <p>
     * The asynchronous API can be used simultaneously with the other APIs.
     *
     * @return a asynchronous API for this client.
     */
    @NotNull Mqtt5AsyncClient toAsync();

    /**
     * Turns the API of this client into a blocking API.
     * <p>
     * The blocking API can be used simultaneously with the other APIs.
     *
     * @return a blocking API for this client.
     */
    @NotNull Mqtt5BlockingClient toBlocking();
}