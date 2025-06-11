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
import io.ballerina.runtime.api.PredefinedTypes;
import io.ballerina.runtime.api.Runtime;
import io.ballerina.runtime.api.async.StrandMetadata;
import io.ballerina.runtime.api.creators.TypeCreator;
import io.ballerina.runtime.api.types.AnnotatableType;
import io.ballerina.runtime.api.utils.StringUtils;
import io.ballerina.runtime.api.utils.TypeUtils;
import io.ballerina.runtime.api.values.BError;
import io.ballerina.runtime.api.values.BMap;
import io.ballerina.runtime.api.values.BObject;
import io.ballerina.runtime.api.values.BString;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.jms.Connection;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
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
import static io.ballerina.lib.ibm.ibmmq.ModuleUtils.getModule;
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
    static final String NATIVE_JMS_SESSION = "JMSSession";
    static final String NATIVE_SERVICE_LIST = "ServiceList";

    static final BString CONFIG = StringUtils.fromString("config");
    static final BString QUEUE_NAME = StringUtils.fromString("queueName");
    static final BString TOPIC_NAME = StringUtils.fromString("topicName");
    static final BString DURABLE = StringUtils.fromString("durable");
    static final BString SUBSCRIPTION_NAME = StringUtils.fromString("subscriptionName");

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

            // Initialize service list for tracking attached services
            listener.addNativeData(NATIVE_SERVICE_LIST, new ArrayList<BObject>());
        } catch (Exception e) {
            return createError(IBMMQ_ERROR, "Failed to initialize listener", e);
        }
        return null;
    }

    public static Object attach(Environment environment, BObject listener, BObject service, Object name) {
        AnnotatableType serviceType = (AnnotatableType) TypeUtils.getType(service);
        BMap<BString, Object> serviceConfig =
                (BMap<BString, Object>) serviceType.getAnnotation(getServiceConfigAnnotationName());

        // Validate service configuration exists
        if (serviceConfig == null) {
            return createError(IBMMQ_ERROR, "Service configuration annotation is required");
        }

        BMap<BString, Object> configs = (BMap<BString, Object>) serviceConfig.getMapValue(CONFIG);
        if (configs == null) {
            return createError(IBMMQ_ERROR, "Service configuration 'config' field is required");
        }

        Connection connection = (Connection) listener.getNativeData(NATIVE_JMS_CONNECTION);
        if (connection == null) {
            return createError(IBMMQ_ERROR, "JMS connection is not initialized");
        }

        try {
            // Check if this is the first service being attached
            @SuppressWarnings("unchecked")
            List<BObject> serviceList = (List<BObject>) listener.getNativeData(NATIVE_SERVICE_LIST);
            boolean isFirstService = (serviceList == null || serviceList.isEmpty());

            Session session = connection.createSession();
            MessageConsumer consumer;
            if (configs.containsKey(QUEUE_NAME)) {
                consumer = getQueueConsumer(session, configs);
            } else if (configs.containsKey(TOPIC_NAME)) {
                consumer = getTopicConsumer(session, configs);
            } else {
                return createError(IBMMQ_ERROR,
                        "Either queueName or topicName must be specified in service configuration");
            }

            // Start the connection if this is the first service
            if (isFirstService) {
                connection.start();
            }

            // Instead of setMessageListener, schedule periodic polling task
            schedulePollingTask(environment.getRuntime(), consumer, service);

            service.addNativeData(NATIVE_JMS_CONSUMER, consumer);
            service.addNativeData(NATIVE_JMS_SESSION, session);

            // Add service to the service list for tracking
            if (serviceList != null) {
                serviceList.add(service);
            }
        } catch (Exception e) {
            return createError(IBMMQ_ERROR, "Failed to attach service to listener", e);
        }
        return null;
    }

    private static void schedulePollingTask(Runtime runtime, MessageConsumer consumer, BObject service) {
        ServiceValidator serviceValidator = new ServiceValidator(service);
        serviceValidator.validate();

        // Create a message poller similar to Kafka's approach
        IBMMQMessagePoller poller = new IBMMQMessagePoller(runtime, consumer, service, serviceValidator);

        // Store the poller so we can stop it later
        service.addNativeData("MESSAGE_POLLER", poller);

        // Start the polling
        poller.startPolling();
    }

    // IBM MQ Message Poller following Kafka's pattern
    private static class IBMMQMessagePoller {
        private final Runtime runtime;
        private final MessageConsumer consumer;
        private final BObject service;
        private final ServiceValidator serviceValidator;
        private final AtomicBoolean closed = new AtomicBoolean(false);
        private final ScheduledExecutorService executorService = Executors.newScheduledThreadPool(1);
        private ScheduledFuture<?> pollTaskFuture;
        private static final int POLLING_INTERVAL_MS = 100; // 100ms polling interval
        private static final long STOP_TIMEOUT_MS = 5000; // 5 second timeout for shutdown

        public IBMMQMessagePoller(Runtime runtime, MessageConsumer consumer, BObject service,
                                   ServiceValidator serviceValidator) {
            this.runtime = runtime;
            this.consumer = consumer;
            this.service = service;
            this.serviceValidator = serviceValidator;
        }

        public void startPolling() {
            final Runnable pollingFunction = this::poll;
            this.pollTaskFuture = this.executorService.scheduleAtFixedRate(
                pollingFunction, 0, POLLING_INTERVAL_MS, TimeUnit.MILLISECONDS);
        }

        private void poll() {
            if (closed.get()) {
                return;
            }

            try {
                // Poll for messages with no wait (non-blocking)
                Message message = consumer.receiveNoWait();

                if (message != null) {
                    processReceivedMessage(message);
                }
            } catch (JMSException e) {
                if (!closed.get()) {
                    handlePollingError(e);
                }
            } catch (Exception e) {
                if (!closed.get()) {
                    createError(IBMMQ_ERROR, "Unexpected error during polling", e).printStackTrace();
                }
            }
        }

        private void processReceivedMessage(Message message) {
            if (closed.get()) {
                return;
            }

            try {
                BMap<BString, Object> bMessage = MessageMapper.toBallerinaMessage(message);
                StrandMetadata strandMetadata = new StrandMetadata(getModule().getOrg(), getModule().getName(),
                        getModule().getVersion(), "onMessage");

                // Create a semaphore-based callback similar to Kafka's approach
                Semaphore semaphore = new Semaphore(0);
                IBMMQPollCycleCallback callback = new IBMMQPollCycleCallback(semaphore);

                // Java 17 pattern: Parameters must be doubled with boolean flags
                Object[] params = new Object[] { bMessage, true };

                if (serviceValidator.isOnMessageIsolated()) {
                    runtime.invokeMethodAsyncConcurrently(
                            service,
                            serviceValidator.getOnMessageMethod().getName(),
                            null,
                            strandMetadata,
                            callback,
                            null,
                            TypeCreator.createUnionType(PredefinedTypes.TYPE_ERROR, PredefinedTypes.TYPE_NULL),
                            params);
                } else {
                    runtime.invokeMethodAsyncSequentially(
                            service,
                            serviceValidator.getOnMessageMethod().getName(),
                            null,
                            strandMetadata,
                            callback,
                            null,
                            TypeCreator.createUnionType(PredefinedTypes.TYPE_ERROR, PredefinedTypes.TYPE_NULL),
                            params);
                }

                // Wait for Ballerina processing to complete before continuing polling
                // This ensures we don't overwhelm the system with messages
                try {
                    semaphore.acquire();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    stop();
                }

            } catch (JMSException e) {
                handleMessageProcessingError(e);
            }
        }

        private void handlePollingError(JMSException e) {
            if (serviceValidator.getOnErrorMethod() != null) {
                BError ballerinaError = createError(IBMMQ_ERROR, "Failed to poll messages", e);
                invokeOnError(ballerinaError);
            } else {
                createError(IBMMQ_ERROR, "Failed to poll messages", e).printStackTrace();
            }
        }

        private void handleMessageProcessingError(JMSException e) {
            if (serviceValidator.getOnErrorMethod() != null) {
                BError ballerinaError = createError(IBMMQ_ERROR, "Failed to process message", e);
                invokeOnError(ballerinaError);
            } else {
                createError(IBMMQ_ERROR, "Failed to process message", e).printStackTrace();
            }
        }

        private void invokeOnError(BError ballerinaError) {
            StrandMetadata errorStrandMetadata = new StrandMetadata(getModule().getOrg(), getModule().getName(),
                    getModule().getVersion(), "onError");
            MessageCallback errorCallback = new MessageCallback();

            // Java 17 pattern: Parameters must be doubled with boolean flags
            Object[] errorParams = new Object[] { ballerinaError, true };

            if (serviceValidator.isOnErrorIsolated()) {
                runtime.invokeMethodAsyncConcurrently(
                        service,
                        serviceValidator.getOnErrorMethod().getName(),
                        null,
                        errorStrandMetadata,
                        errorCallback,
                        null,
                        TypeCreator.createUnionType(PredefinedTypes.TYPE_ERROR, PredefinedTypes.TYPE_NULL),
                        errorParams);
            } else {
                runtime.invokeMethodAsyncSequentially(
                        service,
                        serviceValidator.getOnErrorMethod().getName(),
                        null,
                        errorStrandMetadata,
                        errorCallback,
                        null,
                        TypeCreator.createUnionType(PredefinedTypes.TYPE_ERROR, PredefinedTypes.TYPE_NULL),
                        errorParams);
            }
        }

        public void stop() {
            closed.set(true);
            if (pollTaskFuture != null) {
                pollTaskFuture.cancel(true);
            }
            executorService.shutdown();
            try {
                if (!executorService.awaitTermination(STOP_TIMEOUT_MS,
                                                       TimeUnit.MILLISECONDS)) {
                    executorService.shutdownNow();
                }
            } catch (InterruptedException e) {
                executorService.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }

        public boolean isClosed() {
            return closed.get();
        }
    }

    // Callback similar to Kafka's KafkaPollCycleFutureListener
    private static class IBMMQPollCycleCallback implements io.ballerina.runtime.api.async.Callback {
        private final Semaphore semaphore;

        public IBMMQPollCycleCallback(Semaphore semaphore) {
            this.semaphore = semaphore;
        }

        @Override
        public void notifySuccess(Object result) {
            semaphore.release();
            if (result instanceof BError) {
                ((BError) result).printStackTrace();
            }
        }

        @Override
        public void notifyFailure(BError error) {
            semaphore.release();
            error.printStackTrace();
        }
    }

        public static Object detach(BObject listener, BObject service) {
        MessageConsumer consumer = (MessageConsumer) service.getNativeData(NATIVE_JMS_CONSUMER);
        Session session = (Session) service.getNativeData(NATIVE_JMS_SESSION);
                        IBMMQMessagePoller poller = (IBMMQMessagePoller) service.getNativeData("MESSAGE_POLLER");

        try {
            // Stop the polling first
            if (poller != null) {
                poller.stop();
            }

            if (consumer != null) {
                consumer.close();
            }
            if (session != null) {
                session.close();
            }

            // Remove service from the service list
            @SuppressWarnings("unchecked")
            List<BObject> serviceList = (List<BObject>) listener.getNativeData(NATIVE_SERVICE_LIST);
            if (serviceList != null) {
                serviceList.remove(service);
            }
        } catch (Exception e) {
            return createError(IBMMQ_ERROR, "Failed to close JMS consumer and session", e);
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
        // First, clean up all attached services
        @SuppressWarnings("unchecked")
        List<BObject> serviceList = (List<BObject>) listener.getNativeData(NATIVE_SERVICE_LIST);
        if (serviceList != null) {
            // Create a copy to avoid concurrent modification
            List<BObject> servicesToDetach = new ArrayList<>(serviceList);
            for (BObject service : servicesToDetach) {
                detach(listener, service);
            }
        }
        // Then immediately close the connection
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
        // First, clean up all attached services
        @SuppressWarnings("unchecked")
        List<BObject> serviceList = (List<BObject>) listener.getNativeData(NATIVE_SERVICE_LIST);
        if (serviceList != null) {
            // Create a copy to avoid concurrent modification
            List<BObject> servicesToDetach = new ArrayList<>(serviceList);
            for (BObject service : servicesToDetach) {
                detach(listener, service);
            }
        }

        // Then stop and close the connection
        Connection connection = (Connection) listener.getNativeData(NATIVE_JMS_CONNECTION);
        try {
            if (connection != null) {
                connection.stop();
                connection.close();
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

        // Validate subscription name for durable topics
        if (durable) {
            if (!topicConfig.containsKey(SUBSCRIPTION_NAME) ||
                topicConfig.getStringValue(SUBSCRIPTION_NAME) == null ||
                topicConfig.getStringValue(SUBSCRIPTION_NAME).getValue().trim().isEmpty()) {
                throw createError(IBMMQ_ERROR, "Subscription name is required for durable topics");
            }
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
