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

package org.mqttbee.mqtt.mqtt5.message.publish.pubrec;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mqttbee.annotations.DoNotImplement;
import org.mqttbee.mqtt.datatypes.MqttUtf8String;
import org.mqttbee.mqtt.mqtt5.datatypes.Mqtt5UserProperties;
import org.mqttbee.mqtt.mqtt5.datatypes.Mqtt5UserPropertiesBuilder;

/**
 * @author Silvio Giebl
 */
@DoNotImplement
public interface Mqtt5PubRecBuilder {

    @NotNull Mqtt5PubRecBuilder reasonCode(@NotNull Mqtt5PubRecReasonCode reasonCode);

    @NotNull Mqtt5PubRecBuilder reasonString(@Nullable String reasonString);

    @NotNull Mqtt5PubRecBuilder reasonString(@Nullable MqttUtf8String reasonString);

    @NotNull Mqtt5PubRecBuilder userProperties(@NotNull Mqtt5UserProperties userProperties);

    @NotNull Mqtt5UserPropertiesBuilder.Nested<? extends Mqtt5PubRecBuilder> userProperties();
}