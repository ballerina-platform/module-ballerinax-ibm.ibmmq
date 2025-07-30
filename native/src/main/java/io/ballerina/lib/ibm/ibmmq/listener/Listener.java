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

import java.util.ArrayList;
import java.util.List;
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
public final class Listener {
    static final String NATIVE_CONNECTION = "native.connection";
    static final String NATIVE_SERVICE_LIST = "native.service.list";
    static final String NATIVE_SERVICE = "native.service";
    static final String NATIVE_RECEIVER = "native.receiver";
    static final String LISTENER_STARTED = "listener.started";
    static final String DURABLE = "DURABLE";
    static final String SHARED = "SHARED";
    static final String SHARED_DURABLE = "SHARED_DURABLE";

    private Listener() {
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
            bListener.addNativeData(NATIVE_SERVICE_LIST, new ArrayList<BObject>());
        } catch (Exception e) {
            return createError(IBMMQ_ERROR, "Failed to initialize listener", e);
        }
        return null;
    }

    public static Object attach(Environment env, BObject bListener, BObject bService, Object name) {
        Connection connection = (Connection) bListener.getNativeData(NATIVE_CONNECTION);
        Object started = bListener.getNativeData(LISTENER_STARTED);
        try {
            Service.validateService(bService);
            Service nativeService = new Service(bService);
            ServiceConfig svcConfig = nativeService.getServiceConfig();
            int sessionAckMode = getSessionAckMode(svcConfig.ackMode());
            boolean transacted = Session.SESSION_TRANSACTED == sessionAckMode;
            Session session = connection.createSession(transacted, sessionAckMode);
            MessageConsumer consumer = getConsumer(session, svcConfig);
            MessageDispatcher messageDispatcher = new MessageDispatcher(env.getRuntime(), nativeService, session);
            MessageReceiver receiver = new MessageReceiver(
                    session, consumer, messageDispatcher, svcConfig.pollingInterval(), svcConfig.receiveTimeout());
            bService.addNativeData(NATIVE_SERVICE, nativeService);
            bService.addNativeData(NATIVE_RECEIVER, receiver);
            List<BObject> serviceList = (List<BObject>) bListener.getNativeData(NATIVE_SERVICE_LIST);
            serviceList.add(bService);
            if (Objects.nonNull(started) && ((Boolean) started)) {
                receiver.consume();
            }
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
            case DURABLE -> {
                return session.createDurableConsumer(
                        topic, topicConfig.subscriberName(), topicConfig.messageSelector(), topicConfig.noLocal());
            }
            case SHARED -> {
                return session.createSharedConsumer(topic, topicConfig.subscriberName(), topicConfig.messageSelector());
            }
            case SHARED_DURABLE -> {
                return session.createSharedDurableConsumer(
                        topic, topicConfig.subscriberName(), topicConfig.messageSelector());
            }
            default -> {
                return session.createConsumer(topic, topicConfig.messageSelector(), topicConfig.noLocal());
            }
        }
    }

    public static Object detach(BObject bService) {
        Object receiver = bService.getNativeData(NATIVE_RECEIVER);
        try {
            if (Objects.isNull(receiver)) {
                return createError(IBMMQ_ERROR, "Could not find the native IBM MQ message receiver");
            }
            ((MessageReceiver) receiver).stop();
        } catch (Exception e) {
            String errorMsg = Objects.isNull(e.getMessage()) ? "Unknown error" : e.getMessage();
            return createError(IBMMQ_ERROR,
                    String.format("Failed to detach a service from the listener: %s", errorMsg), e);
        }
        return null;
    }

    public static Object start(BObject bListener) {
        Connection connection = (Connection) bListener.getNativeData(NATIVE_CONNECTION);
        List<BObject> bServices = (List<BObject>) bListener.getNativeData(NATIVE_SERVICE_LIST);
        try {
            connection.start();
            for (BObject bService: bServices) {
                MessageReceiver receiver = (MessageReceiver) bService.getNativeData(NATIVE_RECEIVER);
                receiver.consume();
            }
            bListener.addNativeData(LISTENER_STARTED, Boolean.valueOf(true));
        } catch (JMSException e) {
            String errorMsg = Objects.isNull(e.getMessage()) ? "Unknown error" : e.getMessage();
            return createError(IBMMQ_ERROR,
                    String.format("Error occurred while starting the Ballerina IBM MQ listener: %s", errorMsg), e);
        }
        return null;
    }

    public static Object gracefulStop(BObject bListener) {
        Connection nativeConnection = (Connection) bListener.getNativeData(NATIVE_CONNECTION);
        List<BObject> bServices = (List<BObject>) bListener.getNativeData(NATIVE_SERVICE_LIST);
        try {
            for (BObject bService: bServices) {
                MessageReceiver receiver = (MessageReceiver) bService.getNativeData(NATIVE_RECEIVER);
                receiver.stop();
            }
            nativeConnection.stop();
            nativeConnection.close();
        } catch (Exception e) {
            String errorMsg = Objects.isNull(e.getMessage()) ? "Unknown error" : e.getMessage();
            return createError(IBMMQ_ERROR,
                    String.format(
                            "Error occurred while gracefully stopping the Ballerina IBM MQ listener: %s", errorMsg), e);
        }
        return null;
    }

    public static Object immediateStop(BObject bListener) {
        Connection nativeConnection = (Connection) bListener.getNativeData(NATIVE_CONNECTION);
        List<BObject> bServices = (List<BObject>) bListener.getNativeData(NATIVE_SERVICE_LIST);
        try {
            for (BObject bService: bServices) {
                MessageReceiver receiver = (MessageReceiver) bService.getNativeData(NATIVE_RECEIVER);
                receiver.stop();
            }
            nativeConnection.stop();
            nativeConnection.close();
        } catch (Exception e) {
            String errorMsg = Objects.isNull(e.getMessage()) ? "Unknown error" : e.getMessage();
            return createError(IBMMQ_ERROR,
                    String.format("Error occurred while gracefully stopping the Ballerina IBM MQ listener: %s",
                            errorMsg), e);
        }
        return null;
    }
}
