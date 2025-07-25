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

import io.ballerina.lib.ibm.ibmmq.CommonUtils;
import io.ballerina.lib.ibm.ibmmq.Constants;
import io.ballerina.lib.ibm.ibmmq.config.QueueManagerConfiguration;
import io.ballerina.runtime.api.Environment;
import io.ballerina.runtime.api.values.BError;
import io.ballerina.runtime.api.values.BMap;
import io.ballerina.runtime.api.values.BObject;
import io.ballerina.runtime.api.values.BString;

import java.util.Objects;
import java.util.UUID;

import javax.jms.Connection;
import javax.jms.JMSException;
import javax.jms.MessageConsumer;
import javax.jms.Queue;
import javax.jms.Session;
import javax.jms.Topic;

import static io.ballerina.lib.ibm.ibmmq.CommonUtils.createError;
import static io.ballerina.lib.ibm.ibmmq.Constants.IBMMQ_ERROR;

/**
 * Native class for the Ballerina IBM MQ Listener.
 *
 * @since 1.3.0
 */
public final class ListenerV2 {
    static final String NATIVE_CONNECTION = "native.connection";
    static final String NATIVE_SERVICE = "native.service";
    static final String NATIVE_SESSION = "native.session";
    static final String NATIVE_CONSUMER = "native.consumer";
    static final String NATIVE_MESSAGE = "native.message";

    private ListenerV2() {
    }

    public static Object init(BObject bListener, BMap<BString, Object> configurations) {
        try {
            QueueManagerConfiguration config = new QueueManagerConfiguration(configurations);
            Connection jmsConnection = CommonUtils.getJmsConnection(config);
            if (Objects.isNull(jmsConnection.getClientID())) {
                jmsConnection.setClientID(UUID.randomUUID().toString());
            }
            jmsConnection.setExceptionListener(new LoggingExceptionListener());
            bListener.addNativeData(NATIVE_CONNECTION, jmsConnection);
        } catch (Exception e) {
            return createError(IBMMQ_ERROR, "Failed to initialize listener", e);
        }
        return null;
    }

    public static Object attach(Environment env, BObject bListener, BObject bService, Object name) {
        Connection connection = (Connection) bListener.getNativeData(NATIVE_CONNECTION);
        try {
            Service.validateService(bService);
            Service nativeService = new Service(bService);
            ServiceConfig svcConfig = nativeService.getServiceConfig();
            int sessionAckMode = getSessionAckMode(svcConfig.ackMode());
            boolean transacted = Session.SESSION_TRANSACTED == sessionAckMode;
            Session session = connection.createSession(transacted, sessionAckMode);
            MessageConsumer consumer = getConsumer(session, svcConfig);
            MessageDispatcher messageDispatcher = new MessageDispatcher(env.getRuntime(), nativeService, session);
            consumer.setMessageListener(messageDispatcher);
            bService.addNativeData(NATIVE_SERVICE, nativeService);
            bService.addNativeData(NATIVE_SESSION, session);
            bService.addNativeData(NATIVE_CONSUMER, consumer);
        } catch (BError | JMSException e) {
            String errorMsg = Objects.isNull(e.getMessage()) ? "Unknown error" : e.getMessage();
            return createError(IBMMQ_ERROR, String.format("Failed to attach service to listener: %s", errorMsg), e);
        }
        return null;
    }

    private static int getSessionAckMode(String ackMode) {
        return switch (ackMode) {
            case Constants.SESSION_TRANSACTED_MODE -> Session.SESSION_TRANSACTED;
            case Constants.AUTO_ACKNOWLEDGE_MODE -> Session.AUTO_ACKNOWLEDGE;
            case Constants.CLIENT_ACKNOWLEDGE_MODE -> Session.CLIENT_ACKNOWLEDGE;
            case null, default -> Session.DUPS_OK_ACKNOWLEDGE;
        };
    }

    private static MessageConsumer getConsumer(Session session, ServiceConfig svcConfig)
            throws JMSException {
        if (svcConfig instanceof QueueConfig queueConfig) {
            Queue queue = session.createQueue(queueConfig.queueName());
            return session.createConsumer(queue, queueConfig.messageSelector());
        }
        TopicConfig topicConfig = (TopicConfig) svcConfig;
        Topic topic = session.createTopic(topicConfig.topicName());
        switch (topicConfig.consumerType()) {
            case "DURABLE" -> {
                return session.createDurableConsumer(
                        topic, topicConfig.subscriberName(), topicConfig.messageSelector(), topicConfig.noLocal());
            }
            case "SHARED" -> {
                return session.createSharedConsumer(topic, topicConfig.subscriberName(), topicConfig.messageSelector());
            }
            case "SHARED_DURABLE" -> {
                return session.createSharedDurableConsumer(
                        topic, topicConfig.subscriberName(), topicConfig.messageSelector());
            }
            default -> {
                return session.createConsumer(topic, topicConfig.messageSelector(), topicConfig.noLocal());
            }
        }
    }

    public static Object detach(BObject bService) {
        Session session = (Session) bService.getNativeData(NATIVE_SESSION);
        MessageConsumer consumer = (MessageConsumer) bService.getNativeData(NATIVE_CONSUMER);
        try {
            if (Objects.isNull(session)) {
                return createError(IBMMQ_ERROR, "Could not find the native JMS session");
            }
            if (Objects.isNull(consumer)) {
                return createError(IBMMQ_ERROR,"Could not find the native JMS consumer");
            }

            consumer.close();
            session.close();
        } catch (Exception e) {
            String errorMsg = Objects.isNull(e.getMessage()) ? "Unknown error" : e.getMessage();
            return createError(IBMMQ_ERROR,
                    String.format("Failed to detach a service from the listener: %s", errorMsg), e);
        }
        return null;
    }

    public static Object start(BObject bListener) {
        Connection connection = (Connection) bListener.getNativeData(NATIVE_CONNECTION);
        try {
            connection.start();
        } catch (JMSException e) {
            String errorMsg = Objects.isNull(e.getMessage()) ? "Unknown error" : e.getMessage();
            return createError(IBMMQ_ERROR,
                    String.format("Error occurred while starting the Ballerina IBM MQ listener: %s", errorMsg), e);
        }
        return null;
    }

    public static Object gracefulStop(BObject bListener) {
        Connection nativeConnection = (Connection) bListener.getNativeData(NATIVE_CONNECTION);
        try {
            nativeConnection.stop();
            nativeConnection.close();
        } catch (JMSException e) {
            String errorMsg = Objects.isNull(e.getMessage()) ? "Unknown error" : e.getMessage();
            return createError(IBMMQ_ERROR,
                    String.format(
                            "Error occurred while gracefully stopping the Ballerina IBM MQ listener: %s", errorMsg), e);
        }
        return null;
    }

    public static Object immediateStop(BObject bListener) {
        Connection nativeConnection = (Connection) bListener.getNativeData(NATIVE_CONNECTION);
        try {
            nativeConnection.stop();
            nativeConnection.close();
        } catch (JMSException e) {
            String errorMsg = Objects.isNull(e.getMessage()) ? "Unknown error" : e.getMessage();
            return createError(IBMMQ_ERROR,
                    String.format("Error occurred while gracefully stopping the Ballerina IBM MQ listener: %s",
                            errorMsg), e);
        }
        return null;
    }
}
