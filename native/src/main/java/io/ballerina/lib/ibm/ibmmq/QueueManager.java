/*
 * Copyright (c) 2023, WSO2 LLC. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.ballerina.lib.ibm.ibmmq;

import com.ibm.mq.MQException;
import com.ibm.mq.MQQueue;
import com.ibm.mq.MQQueueManager;
import com.ibm.mq.MQTopic;
import com.ibm.mq.constants.MQConstants;
import io.ballerina.lib.ibm.ibmmq.config.QueueManagerConfiguration;
import io.ballerina.runtime.api.creators.ValueCreator;
import io.ballerina.runtime.api.values.BMap;
import io.ballerina.runtime.api.values.BObject;
import io.ballerina.runtime.api.values.BString;

import java.util.Hashtable;
import java.util.Objects;

import javax.net.ssl.SSLSocketFactory;

import static io.ballerina.lib.ibm.ibmmq.CommonUtils.createError;
import static io.ballerina.lib.ibm.ibmmq.Constants.BQUEUE;
import static io.ballerina.lib.ibm.ibmmq.Constants.BTOPIC;
import static io.ballerina.lib.ibm.ibmmq.Constants.IBMMQ_ERROR;
import static io.ballerina.lib.ibm.ibmmq.Constants.NATIVE_QUEUE_MANAGER;
import static io.ballerina.lib.ibm.ibmmq.Constants.QUEUE_MANAGER_NAME;
import static io.ballerina.lib.ibm.ibmmq.SslUtils.getSecureSocketFactory;
import static io.ballerina.lib.ibm.ibmmq.SslUtils.getSslProtocol;

/**
 * Representation of {@link com.ibm.mq.MQQueueManager} with utility methods to invoke as inter-op functions.
 */
public class QueueManager {
    static final String QUEUE_MNG_CONFIG = "QUEUE_MNG_CONFIG";

    /**
     * Creates an IBM MQ queue manager with the provided configurations.
     *
     * @param queueManager   Ballerina queue-manager object
     * @param configurations IBM MQ connection configurations
     * @return A Ballerina `ibmmq:Error` if there are connection problems
     */
    public static Object init(BObject queueManager, BMap<BString, Object> configurations) {
        try {
            QueueManagerConfiguration queueManagerConfig = new QueueManagerConfiguration(configurations);
            queueManager.addNativeData(QUEUE_MNG_CONFIG, queueManagerConfig);
            Hashtable<String, Object> connectionProperties = getConnectionProperties(queueManagerConfig);
            String queueManagerName = configurations.getStringValue(QUEUE_MANAGER_NAME).getValue();
            MQQueueManager mqQueueManager = new MQQueueManager(queueManagerName, connectionProperties);
            queueManager.addNativeData(NATIVE_QUEUE_MANAGER, mqQueueManager);
        } catch (MQException e) {
            return createError(IBMMQ_ERROR,
                    String.format("Error occurred while initializing the connection manager: %s",
                            e.getMessage()), e);
        } catch (Exception e) {
            return createError(IBMMQ_ERROR,
                    String.format("Unexpected error occurred while initializing the connection manager: %s",
                            e.getMessage()), e);
        }
        return null;
    }

    static Hashtable<String, Object> getConnectionProperties(QueueManagerConfiguration configurations)
            throws Exception {
        Hashtable<String, Object> properties = new Hashtable<>();
        properties.put(MQConstants.HOST_NAME_PROPERTY, configurations.host());
        properties.put(MQConstants.PORT_PROPERTY, configurations.port());
        properties.put(MQConstants.CHANNEL_PROPERTY, configurations.channel());
        if (Objects.nonNull(configurations.userID())) {
            properties.put(MQConstants.USER_ID_PROPERTY, configurations.userID());
        }
        if (Objects.nonNull(configurations.password())) {
            properties.put(MQConstants.PASSWORD_PROPERTY, configurations.password());
        }
        updateSSlConfig(properties, configurations);
        return properties;
    }

    @SuppressWarnings("unchecked")
    private static void updateSSlConfig(Hashtable<String, Object> properties,
                                        QueueManagerConfiguration configurations) throws Exception {
        if (Objects.nonNull(configurations.sslCipherSuite())) {
            properties.put(MQConstants.SSL_CIPHER_SUITE_PROPERTY, configurations.sslCipherSuite());;
        }
        if (Objects.nonNull(configurations.secureSocket())) {
            String sslProtocol = getSslProtocol(configurations.sslCipherSuite());
            SSLSocketFactory sslSocketFactory = getSecureSocketFactory(sslProtocol, configurations.secureSocket());
            properties.put(MQConstants.SSL_SOCKET_FACTORY_PROPERTY, sslSocketFactory);
        }
    }

    public static Object accessQueue(BObject queueManagerObject, BString queueName, Long options) {
        MQQueueManager queueManager = (MQQueueManager) queueManagerObject.getNativeData(NATIVE_QUEUE_MANAGER);
        try {
            MQQueue mqQueue = queueManager.accessQueue(queueName.getValue(), options.intValue());
            BObject bQueue = ValueCreator.createObjectValue(ModuleUtils.getModule(), BQUEUE);
            bQueue.addNativeData(Constants.NATIVE_QUEUE, mqQueue);
            return bQueue;
        } catch (MQException e) {
            return createError(IBMMQ_ERROR,
                    java.lang.String.format("Error occurred while accessing queue: %s", e.getMessage()), e);
        }
    }

    public static Object accessTopic(BObject queueManagerObject, BString topicName,
                                     BString topicString, Long openTopicOption, Long options) {
        MQQueueManager queueManager = (MQQueueManager) queueManagerObject.getNativeData(NATIVE_QUEUE_MANAGER);
        QueueManagerConfiguration queueMngConfig = (QueueManagerConfiguration) queueManagerObject
                .getNativeData(QUEUE_MNG_CONFIG);
        try {
            MQTopic mqTopic = queueManager.accessTopic(topicName.getValue(), topicString.getValue(),
                    openTopicOption.intValue(), options.intValue());
            BObject bTopic = ValueCreator.createObjectValue(ModuleUtils.getModule(), BTOPIC);
            bTopic.addNativeData(QUEUE_MNG_CONFIG, queueMngConfig);
            bTopic.addNativeData(Constants.NATIVE_TOPIC, mqTopic);

            return bTopic;
        } catch (MQException e) {
            return createError(IBMMQ_ERROR,
                    java.lang.String.format("Error occurred while accessing topic: %s", e.getMessage()), e);
        }
    }

    public static Object disconnect(BObject queueManagerObject) {
        MQQueueManager queueManager = (MQQueueManager) queueManagerObject.getNativeData(NATIVE_QUEUE_MANAGER);
        try {
            queueManager.disconnect();
        } catch (MQException e) {
            return createError(IBMMQ_ERROR,
                    java.lang.String.format("Error occurred while disconnecting queue manager: %s", e.getMessage()), e);
        }
        return null;
    }
}
