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
import io.ballerina.runtime.api.Environment;
import io.ballerina.runtime.api.values.BMap;
import io.ballerina.runtime.api.values.BObject;
import io.ballerina.runtime.api.values.BString;

import java.util.ArrayList;
import java.util.List;

import javax.jms.JMSException;
import javax.jms.MessageConsumer;
import javax.jms.Queue;
import javax.jms.Topic;

import static io.ballerina.lib.ibm.ibmmq.CommonUtils.createError;
import static io.ballerina.lib.ibm.ibmmq.Constants.IBMMQ_ERROR;
import static javax.jms.Session.AUTO_ACKNOWLEDGE;

/**
 * Native class for the Ballerina IBM MQ Listener.
 *
 * @since 1.3.0
 */
public final class Listener {
    static final String NATIVE_CONNECTION_MAP = "native.connection.map";
    static final String NATIVE_SERVICE_LIST = "native.service.list";
    static final String NATIVE_SERVICE = "native.service";

    private Listener() {
    }

    public static Object initListener(BObject listener, BMap<BString, Object> configs) {
        try {
            ConnectionMap connectionMap = new ConnectionMap(configs);
            connectionMap.setupConnectionFactory();
            listener.addNativeData(NATIVE_CONNECTION_MAP, connectionMap);
            listener.addNativeData(NATIVE_SERVICE_LIST, new ArrayList<BObject>());
        } catch (Exception e) {
            return createError(IBMMQ_ERROR, "Failed to initialize listener", e);
        }
        return null;
    }

    public static Object attach(Environment environment, BObject listener, BObject service, Object name) {
        try {
            IbmmqService nativeService = new IbmmqService(service);
            nativeService.initialize();
            ConnectionMap connectionMap = (ConnectionMap) listener.getNativeData(NATIVE_CONNECTION_MAP);
            MessageConsumer consumer = getConsumer(connectionMap, nativeService);
            consumer.setMessageListener(new BallerinaIbmmqListener(environment.getRuntime(), service, nativeService));
            service.addNativeData(NATIVE_SERVICE, nativeService);
            List<BObject> serviceList = (List<BObject>) listener.getNativeData(NATIVE_SERVICE_LIST);
            serviceList.add(service);
        } catch (Exception e) {
            return createError(IBMMQ_ERROR, "Failed to attach service to listener", e);
        }
        return null;
    }

    public static void start(BObject listener) {
        ConnectionMap connectionMap = (ConnectionMap) listener.getNativeData(NATIVE_CONNECTION_MAP);
        connectionMap.startAll();
    }

    public static void detach(BObject service) {
        IbmmqService nativeService = (IbmmqService) service.getNativeData(NATIVE_SERVICE);
        nativeService.close();
    }

    public static void gracefulStop(BObject listener) {
        List<BObject> serviceList = (List<BObject>) listener.getNativeData(NATIVE_SERVICE_LIST);
        closeServiceList(serviceList);
    }

    public static void immediateStop(BObject listener) {
        List<BObject> serviceList = (List<BObject>) listener.getNativeData(NATIVE_SERVICE_LIST);
        closeServiceList(serviceList);
    }

    private static MessageConsumer getQueueConsumer(MQSession session, String queueName)
            throws JMSException {
        Queue queue = session.createQueue(queueName);
        return session.createConsumer(queue);
    }

    private static MessageConsumer getTopicConsumer(MQSession session, String topicName, boolean durable,
                                                    String subscriptionName) {
        try {
            Topic topic = session.createTopic(topicName);
            MessageConsumer consumer;
            if (durable) {
                consumer = session.createDurableConsumer(topic, subscriptionName, null, false);
            } else {
                consumer = session.createConsumer(topic, null, false);
            }
            return consumer;
        } catch (JMSException e) {
            throw createError(IBMMQ_ERROR, "Failed to create topic consumer", e);
        }
    }

    private static MessageConsumer getConsumer(ConnectionMap connectionMap, IbmmqService nativeService) {
        try {
            boolean isDurable = nativeService.isDurableConsumer();
            String subscriptionName = nativeService.getSubscriptionName();
            MQConnection connection = connectionMap.getConnection(isDurable, subscriptionName);
            MQSession session = (MQSession) connection.createSession(false, AUTO_ACKNOWLEDGE);
            MessageConsumer consumer;
            if (nativeService.isTopicConsumer()) {
                consumer = getTopicConsumer(session, nativeService.getTopicName(), isDurable, subscriptionName);
            } else {
                consumer = getQueueConsumer(session, nativeService.getQueueName());
            }
            ServiceContext context = new ServiceContext(connection, session, consumer, isDurable);
            nativeService.setContext(context);
            return consumer;
        } catch (Exception e) {
            throw createError(IBMMQ_ERROR, "Failed to create consumer", e);
        }
    }

    private static void closeServiceList(List<BObject> serviceList) {
        if (serviceList != null) {
            for (BObject service : serviceList) {
                IbmmqService nativeService = (IbmmqService) service.getNativeData(NATIVE_SERVICE);
                if (nativeService != null) {
                    nativeService.close();
                }
            }
        }
    }
}
