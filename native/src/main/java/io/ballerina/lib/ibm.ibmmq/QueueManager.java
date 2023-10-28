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
import io.ballerina.runtime.api.creators.ValueCreator;
import io.ballerina.runtime.api.utils.StringUtils;
import io.ballerina.runtime.api.values.BMap;
import io.ballerina.runtime.api.values.BObject;
import io.ballerina.runtime.api.values.BString;

import java.util.Hashtable;

import static io.ballerina.lib.ibm.ibmmq.CommonUtils.createError;
import static io.ballerina.lib.ibm.ibmmq.CommonUtils.getOptionalStringProperty;
import static io.ballerina.lib.ibm.ibmmq.Constants.IBMMQ_ERROR;
import static io.ballerina.lib.ibm.ibmmq.Constants.NATIVE_QUEUE_MANAGER;

/**
 * Representation of {@link com.ibm.mq.MQQueueManager} with utility methods to invoke as inter-op functions.
 */
public class QueueManager {
    private static final BString QUEUE_MANAGER_NAME = StringUtils.fromString("name");
    private static final BString HOST = StringUtils.fromString("host");
    private static final BString PORT = StringUtils.fromString("port");
    private static final BString CHANNEL = StringUtils.fromString("channel");
    private static final BString USER_ID = StringUtils.fromString("userID");
    private static final BString PASSWORD = StringUtils.fromString("password");
    private static final String BTOPIC = "Topic";
    private static final String BQUEUE = "Queue";

    /**
     * Creates a IBM MQ queue manager with the provided configurations.
     *
     * @param queueManager Ballerina queue-manager object
     * @param configurations IBM MQ connection configurations
     * @return A Ballerina `ibmmq:Error` if there are connection problems
     */
    public static Object init(BObject queueManager, BMap<BString, Object> configurations) {
        Hashtable<String, Object> connectionProperties = getConnectionProperties(configurations);
        try {
            String queueManagerName = configurations.getStringValue(QUEUE_MANAGER_NAME).getValue();
            MQQueueManager mqQueueManager = new MQQueueManager(queueManagerName, connectionProperties);
            queueManager.addNativeData(NATIVE_QUEUE_MANAGER, mqQueueManager);
        } catch (MQException e) {
            return createError(IBMMQ_ERROR,
                    String.format("Error occurred while initializing the connection manager: %s", e.getMessage()), e);
        }
        return null;
    }

    private static Hashtable<String, Object> getConnectionProperties(BMap<BString, Object> configurations) {
        Hashtable<String, Object> properties = new Hashtable<>();
        String host = configurations.getStringValue(HOST).getValue();
        properties.put(MQConstants.HOST_NAME_PROPERTY, host);
        Long port = configurations.getIntValue(PORT);
        properties.put(MQConstants.PORT_PROPERTY, port);
        String channel = configurations.getStringValue(CHANNEL).getValue();
        properties.put(MQConstants.CHANNEL_PROPERTY, channel);
        getOptionalStringProperty(configurations, USER_ID)
                .ifPresent(userId -> properties.put(MQConstants.USER_ID_PROPERTY, userId));
        getOptionalStringProperty(configurations, PASSWORD)
                .ifPresent(password -> properties.put(MQConstants.PASSWORD_PROPERTY, password));
        return properties;
    }

    public static Object accessQueue(BObject queueManagerObject, BString queueName, Long options) {
        MQQueueManager queueManager = (MQQueueManager) queueManagerObject.getNativeData(NATIVE_QUEUE_MANAGER);
        try {
            MQQueue mqQueue = queueManager.accessQueue(queueName.getValue(), options.intValue());
            BObject bQueue = ValueCreator.createObjectValue(ModuleUtils.getModule(), BQUEUE);
            bQueue.addNativeData(Constants.NATIVE_TOPIC, mqQueue);
            return bQueue;
        } catch (MQException e) {
            return createError(IBMMQ_ERROR,
                    String.format("Error occurred while accessing queue: %s", e.getMessage()), e);
        }
    }

    public static Object accessTopic(BObject queueManagerObject, BString topicName,
                                     BString topicString, Long openTopicOption, Long options) {
        MQQueueManager queueManager = (MQQueueManager) queueManagerObject.getNativeData(NATIVE_QUEUE_MANAGER);
        try {
            MQTopic mqTopic = queueManager.accessTopic(topicName.getValue(), topicString.getValue(),
                    openTopicOption.intValue(), options.intValue());
            BObject bTopic = ValueCreator.createObjectValue(ModuleUtils.getModule(), BTOPIC);
            bTopic.addNativeData(Constants.NATIVE_TOPIC, mqTopic);
            return bTopic;
        } catch (MQException e) {
            return createError(IBMMQ_ERROR,
                    String.format("Error occurred while accessing topic: %s", e.getMessage()), e);
        }
    }
}
