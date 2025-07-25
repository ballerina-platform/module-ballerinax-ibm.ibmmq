/*
 * Copyright (c) 2025, WSO2 LLC. (http://www.wso2.org).
 *
 *  WSO2 LLC. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied. See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package io.ballerina.lib.ibm.ibmmq.config;

import io.ballerina.runtime.api.values.BMap;
import io.ballerina.runtime.api.values.BString;

import static io.ballerina.lib.ibm.ibmmq.CommonUtils.getOptionalStringProperty;
import static io.ballerina.lib.ibm.ibmmq.Constants.CHANNEL;
import static io.ballerina.lib.ibm.ibmmq.Constants.HOST;
import static io.ballerina.lib.ibm.ibmmq.Constants.PASSWORD;
import static io.ballerina.lib.ibm.ibmmq.Constants.PORT;
import static io.ballerina.lib.ibm.ibmmq.Constants.QUEUE_MANAGER_NAME;
import static io.ballerina.lib.ibm.ibmmq.Constants.SECURE_SOCKET;
import static io.ballerina.lib.ibm.ibmmq.Constants.SSL_CIPHER_SUITE;
import static io.ballerina.lib.ibm.ibmmq.Constants.USER_ID;

/**
 * IBM MQ queue manager configurations.
 *
 * @param queueManagerName  Name of the queue manager.
 * @param host              IBM MQ server host.
 * @param port              IBM MQ server port (default is 1414).
 * @param channel           IBM MQ channel.
 * @param userID            (Optional) IBM MQ user ID.
 * @param password          (Optional) IBM MQ user password.
 * @param sslCipherSuite    (Optional) Defines the combination of key exchange, encryption,
 *                          and integrity algorithms used for establishing a secure SSL/TLS connection.
 * @param secureSocket      (Optional) Configurations related to SSL/TLS encryption.
 *
 * @since 1.3.0
 */
public record QueueManagerConfiguration(String queueManagerName, String host, int port, String channel, String userID,
                                        String password, String sslCipherSuite, BMap<BString, Object> secureSocket) {
    @SuppressWarnings("unchecked")
    public QueueManagerConfiguration(BMap<BString, Object> configurations) {
        this(
                configurations.getStringValue(QUEUE_MANAGER_NAME).getValue(),
                configurations.getStringValue(HOST).getValue(),
                configurations.getIntValue(PORT).intValue(),
                configurations.getStringValue(CHANNEL).getValue(),
                getOptionalStringProperty(configurations, USER_ID).orElse(null),
                getOptionalStringProperty(configurations, PASSWORD).orElse(null),
                getOptionalStringProperty(configurations, SSL_CIPHER_SUITE).orElse(null),
                (BMap<BString, Object>) configurations.getMapValue(SECURE_SOCKET)
        );
    }
}
