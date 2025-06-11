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

package io.ballerina.lib.ibm.ibmmq;

import com.ibm.mq.jms.MQConnectionFactory;
import com.ibm.msg.client.wmq.WMQConstants;
import io.ballerina.runtime.api.Environment;
import io.ballerina.runtime.api.types.AnnotatableType;
import io.ballerina.runtime.api.utils.StringUtils;
import io.ballerina.runtime.api.utils.TypeUtils;
import io.ballerina.runtime.api.values.BMap;
import io.ballerina.runtime.api.values.BObject;
import io.ballerina.runtime.api.values.BString;

import javax.jms.Connection;
import javax.jms.Destination;
import javax.jms.MessageConsumer;
import javax.jms.Session;
import javax.jms.Topic;
import javax.net.ssl.SSLSocketFactory;

import static io.ballerina.lib.ibm.ibmmq.CommonUtils.createError;
import static io.ballerina.lib.ibm.ibmmq.CommonUtils.getServiceConfigAnnotationName;
import static io.ballerina.lib.ibm.ibmmq.Constants.HOST;
import static io.ballerina.lib.ibm.ibmmq.Constants.IBMMQ_ERROR;
import static io.ballerina.lib.ibm.ibmmq.Constants.PORT;
import static io.ballerina.lib.ibm.ibmmq.Constants.SECURE_SOCKET;
import static io.ballerina.lib.ibm.ibmmq.Constants.SSL_CIPHER_SUITE;
import static io.ballerina.lib.ibm.ibmmq.Constants.USER_ID;
import static io.ballerina.lib.ibm.ibmmq.QueueManager.getSecureSocketFactory;
import static io.ballerina.lib.ibm.ibmmq.QueueManager.getSslProtocol;

/**
 * Native class for the Ballerina IBM MQ Listener.
 *
 * @since 1.3.0
 */
public final class Listener {
    static final String NATIVE_JMS_CONNECTION = "JMSConnection";
    static final String NATIVE_JMS_CONSUMER = "JMSConsumer";

    static final BString CONFIG = StringUtils.fromString("config");
    static final BString QUEUE_NAME = StringUtils.fromString("queueName");
    static final BString TOPIC_NAME = StringUtils.fromString("topicName");
    static final BString DURABLE = StringUtils.fromString("durable");
    static final BString SUBSCRIPTION_NAME = StringUtils.fromString("subscriptionName");
    static final String QUEUE_PREFIX = "queue:///";

    private Listener() {}

    public static Object initListener(BObject listener, BMap<BString, Object> configs) {
        try {
            MQConnectionFactory connectionFactory = new MQConnectionFactory();
            connectionFactory.setHostName(configs.getStringValue(HOST).getValue());
            connectionFactory.setPort(configs.getIntValue(PORT).intValue());
            connectionFactory.setQueueManager(configs.getStringValue(Constants.QUEUE_MANAGER_NAME).getValue());
            connectionFactory.setChannel(configs.getStringValue(Constants.CHANNEL).getValue());
            connectionFactory.setTransportType(WMQConstants.WMQ_CM_CLIENT);

            // Optional SSL
            if (configs.containsKey(SSL_CIPHER_SUITE)) {
                connectionFactory.setSSLCipherSuite(configs.getStringValue(SSL_CIPHER_SUITE).getValue());
            }

            BMap<BString, Object> secureSocket = (BMap<BString, Object>) configs.getMapValue(SECURE_SOCKET);
            if (secureSocket != null) {
                String sslProtocol = getSslProtocol(configs);
                SSLSocketFactory sslSocketFactory = getSecureSocketFactory(sslProtocol, secureSocket);
                connectionFactory.setSSLSocketFactory(sslSocketFactory);
            }

            String user = configs.getStringValue(USER_ID).getValue();
            String pass = configs.getStringValue(Constants.PASSWORD).getValue();
            Connection connection = connectionFactory.createConnection(user, pass);
            connection.setClientID(configs.getStringValue(Constants.QUEUE_MANAGER_NAME).getValue());
            listener.addNativeData(NATIVE_JMS_CONNECTION, connection);
        } catch (Exception e) {
            return createError(IBMMQ_ERROR, "Failed to initialize listener", e);
        }
        return null;
    }

    public static Object attach(Environment environment, BObject listener, BObject service, Object name) {
        AnnotatableType serviceType = (AnnotatableType) TypeUtils.getType(service);
        BMap<BString, Object> serviceConfig =
                (BMap<BString, Object>) serviceType.getAnnotation(getServiceConfigAnnotationName());
        BMap<BString, Object> configs = (BMap<BString, Object>) serviceConfig.getMapValue(CONFIG);
        Connection connection = (Connection) listener.getNativeData(NATIVE_JMS_CONNECTION);
        try {
            Session session = connection.createSession();
            MessageConsumer consumer;
            if (configs.containsKey(QUEUE_NAME)) {
                consumer = getQueueConsumer(session, configs);
                consumer.setMessageListener(new BallerinaMessageListener(environment.getRuntime(), service));
                service.addNativeData(NATIVE_JMS_CONSUMER, consumer);
            } else if (configs.containsKey(TOPIC_NAME)) {
                consumer = getTopicConsumer(session, configs);
                consumer.setMessageListener(new BallerinaMessageListener(environment.getRuntime(), service));
                service.addNativeData(NATIVE_JMS_CONSUMER, consumer);
            }
            // else is not handled as the serviceConfig is validated by the compiler
        } catch (Exception e) {
            throw createError(IBMMQ_ERROR, "Failed to attach service to listener", e);
        }
        return null;
    }

    public static Object detach(BObject service) {
        MessageConsumer consumer = (MessageConsumer) service.getNativeData(NATIVE_JMS_CONSUMER);
        if (consumer != null) {
            try {
                consumer.close();
            } catch (Exception e) {
                return createError(IBMMQ_ERROR, "Failed to close JMS consumer", e);
            }
        }
        return null;
    }

    public static Object start(BObject listener) {
        Connection connection = (Connection) listener.getNativeData(NATIVE_JMS_CONNECTION);
        try {
            connection.start();
        } catch (Exception e) {
            return createError(IBMMQ_ERROR, "Failed to start the listener", e);
        }
        return null;
    }

    public static Object immediateStop(BObject listener) {
        Connection connection = (Connection) listener.getNativeData(NATIVE_JMS_CONNECTION);
        try {
            if (connection != null) {
                connection.close();
            }
        } catch (Exception e) {
            return createError(IBMMQ_ERROR, "Failed to stop the listener", e);
        }
        return null;
    }

    public static Object gracefulStop(BObject listener) {
        Connection connection = (Connection) listener.getNativeData(NATIVE_JMS_CONNECTION);
        try {
            if (connection != null) {
                connection.stop();
            }
        } catch (Exception e) {
            return createError(IBMMQ_ERROR, "Failed to stop the listener", e);
        }
        return null;
    }

    private static MessageConsumer getQueueConsumer(Session session, BMap<BString, Object> queueConfig) {
        String queueName = queueConfig.getStringValue(QUEUE_NAME).getValue();
        try {
            Destination queue = session.createQueue(queueName);
            return session.createConsumer(queue);
        } catch (Exception e) {
            throw createError(IBMMQ_ERROR, "Failed to create a consumer for the queue: " + queueName, e);
        }
    }

    private static MessageConsumer getTopicConsumer(Session session, BMap<BString, Object> topicConfig) {
        String topicName = topicConfig.getStringValue(TOPIC_NAME).getValue();
        boolean durable = topicConfig.getBooleanValue(DURABLE);
        String subscriptionName = null;
        if (!topicConfig.containsKey(SUBSCRIPTION_NAME)) {
            if (durable) {
                throw createError(IBMMQ_ERROR, "Subscription name is required for durable topics");
            }
        } else {
            subscriptionName = topicConfig.getStringValue(SUBSCRIPTION_NAME).getValue();
        }
        try {
            Topic topic = session.createTopic(topicName);
            return durable ? session.createDurableSubscriber(topic, subscriptionName) : session.createConsumer(topic);
        } catch (Exception e) {
            throw createError(IBMMQ_ERROR, "Failed to create a consumer for the topic: " + topicName, e);
        }
    }
}
