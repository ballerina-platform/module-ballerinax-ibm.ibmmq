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

package io.ballerina.lib.ibm.ibmmq.listener;

import com.ibm.mq.jms.MQConnection;
import com.ibm.mq.jms.MQConnectionFactory;
import com.ibm.msg.client.wmq.WMQConstants;
import io.ballerina.lib.ibm.ibmmq.Constants;
import io.ballerina.runtime.api.values.BMap;
import io.ballerina.runtime.api.values.BString;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.net.ssl.SSLSocketFactory;

import static io.ballerina.lib.ibm.ibmmq.CommonUtils.createError;
import static io.ballerina.lib.ibm.ibmmq.Constants.CHANNEL;
import static io.ballerina.lib.ibm.ibmmq.Constants.HOST;
import static io.ballerina.lib.ibm.ibmmq.Constants.PORT;
import static io.ballerina.lib.ibm.ibmmq.Constants.QUEUE_MANAGER_NAME;
import static io.ballerina.lib.ibm.ibmmq.Constants.SECURE_SOCKET;
import static io.ballerina.lib.ibm.ibmmq.Constants.SSL_CIPHER_SUITE;
import static io.ballerina.lib.ibm.ibmmq.Constants.USER_ID;
import static io.ballerina.lib.ibm.ibmmq.SslUtils.getSecureSocketFactory;
import static io.ballerina.lib.ibm.ibmmq.SslUtils.getSslProtocol;

/**
 * Represents a connection map for the Ballerina IBM MQ connector.
 *
 * @since 1.4.0
 */
public final class ConnectionMap {
    private static final String BALLERINA_CLIENT = "Ballerina.IBM.MQ.Client";
    private final Map<String, MQConnection> durableConnectionMap;
    private MQConnection sharedConnection;
    private MQConnectionFactory connectionFactory;
    private final BMap<BString, Object> configs;
    private final String username;
    private final String password;

    public ConnectionMap(BMap<BString, Object> configs) {
        this.connectionFactory = new MQConnectionFactory();
        this.durableConnectionMap = new ConcurrentHashMap<>();
        this.sharedConnection = null;
        this.configs = configs;
        this.username = configs.getStringValue(USER_ID).getValue();
        this.password = configs.getStringValue(Constants.PASSWORD).getValue();
    }

    public void setupConnectionFactory() {
        try {
            this.connectionFactory.setHostName(this.configs.getStringValue(HOST).getValue());
            this.connectionFactory.setPort(this.configs.getIntValue(PORT).intValue());
            this.connectionFactory.setQueueManager(this.configs.getStringValue(QUEUE_MANAGER_NAME).getValue());
            this.connectionFactory.setChannel(configs.getStringValue(CHANNEL).getValue());
            this.connectionFactory.setTransportType(WMQConstants.WMQ_CM_CLIENT);
            if (configs.containsKey(SSL_CIPHER_SUITE)) {
                this.connectionFactory.setSSLCipherSuite(this.configs.getStringValue(SSL_CIPHER_SUITE).getValue());
            }
            BMap<BString, Object> secureSocket = (BMap<BString, Object>) this.configs.getMapValue(SECURE_SOCKET);
            if (secureSocket != null) {
                String sslProtocol = getSslProtocol(this.configs);
                SSLSocketFactory sslSocketFactory = getSecureSocketFactory(sslProtocol, secureSocket);
                this.connectionFactory.setSSLSocketFactory(sslSocketFactory);
            }
        } catch (Exception e) {
            throw createError(Constants.IBMMQ_ERROR, "Failed to create the connection factory", e);
        }
    }

    /**
     * Returns a durable connection for the given client ID. If a connection already exists for the client ID,
     * it returns that connection, otherwise creates a new one.
     *
     * @param isDurable boolean indicating if the connection is durable
     * @param clientId  the client ID for the connection
     * @return MQConnection
     */
    public MQConnection getConnection(boolean isDurable, String clientId) {
        if (!isDurable) {
            if (this.sharedConnection == null) {
                try {
                    this.sharedConnection = (MQConnection) this.connectionFactory.createConnection(this.username,
                            this.password);
                } catch (Exception e) {
                    throw createError(Constants.IBMMQ_ERROR, "Failed to create a shared connection", e);
                }
            }
            return this.sharedConnection;
        }
        try {
            MQConnection connection = (MQConnection) this.connectionFactory.createConnection(this.username,
                    this.password);
            connection.setClientID(clientId);
            connection.setIntProperty(WMQConstants.WMQ_MSG_BATCH_SIZE, 1);
            connection.setIntProperty(WMQConstants.WMQ_POLLING_INTERVAL, 1000);
            this.durableConnectionMap.put(clientId, connection);
            return connection;
        } catch (Exception e) {
            throw createError(Constants.IBMMQ_ERROR, "Failed to create a new connection for client ID: " + clientId,
                    e);
        }
    }

    public void startAll() {
        if (this.sharedConnection != null) {
            try {
                this.sharedConnection.start();
            } catch (Exception e) {
                throw createError(Constants.IBMMQ_ERROR, "Failed to start the shared connection", e);
            }
        }
        try {
            this.durableConnectionMap.values().forEach(connection -> {
                try {
                    connection.start();
                } catch (Exception e) {
                    throw createError(Constants.IBMMQ_ERROR, "Failed to start the durable connection", e);
                }
            });
        } catch (Exception e) {
            throw createError(Constants.IBMMQ_ERROR, "Failed to start the durable connection", e);
        }
    }

    public void close() {
        if (this.sharedConnection != null) {
            try {
                this.sharedConnection.close();
            } catch (Exception e) {
                throw createError(Constants.IBMMQ_ERROR, "Failed to close the shared connection", e);
            }
        }
        this.durableConnectionMap.forEach((clientId, connection) -> {
            try {
                connection.close();
            } catch (Exception e) {
                throw createError(Constants.IBMMQ_ERROR,
                        "Failed to close the durable connection for client ID: " + clientId, e);
            }
        });
        this.durableConnectionMap.clear();
        this.sharedConnection = null;
    }
}
