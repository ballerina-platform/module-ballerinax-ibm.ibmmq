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
import com.ibm.mq.jms.MQSession;

import javax.jms.MessageConsumer;

import static io.ballerina.lib.ibm.ibmmq.CommonUtils.createError;
import static io.ballerina.lib.ibm.ibmmq.Constants.IBMMQ_ERROR;

/**
 * Represents the service context for the Ballerina IBM MQ connector.
 * This class is used to hold the MQ connection related context information for the listener.
 *
 * @since 1.3.0
 */
public class ServiceContext {
    private final MQConnection connection;
    private final MQSession session;
    private final MessageConsumer consumer;
    private final boolean isDurable;

    public ServiceContext(MQConnection connection, MQSession session, MessageConsumer consumer, boolean isDurable) {
        this.connection = connection;
        this.session = session;
        this.consumer = consumer;
        this.isDurable = isDurable;
    }

    public MQConnection getConnection() {
        return connection;
    }

    public void close() {
        try {
            if (consumer != null) {
                consumer.close();
            }
            if (session != null) {
                session.close();
            }
            if (this.isDurable && connection != null) {
                connection.close();
            }
        } catch (Exception e) {
            throw createError(IBMMQ_ERROR, "Failed to close MQ resources", e);
        }
    }
}
