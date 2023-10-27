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
import com.ibm.mq.MQQueueManager;
import com.ibm.mq.MQTopic;
import com.ibm.mq.constants.MQConstants;
import io.ballerina.lib.ibm.ibmmq.utils.CommonUtils;
import io.ballerina.lib.ibm.ibmmq.utils.ModuleUtils;
import io.ballerina.runtime.api.Environment;
import io.ballerina.runtime.api.creators.ValueCreator;
import io.ballerina.runtime.api.utils.StringUtils;
import io.ballerina.runtime.api.values.BMap;
import io.ballerina.runtime.api.values.BObject;
import io.ballerina.runtime.api.values.BString;

import java.util.Hashtable;

import static io.ballerina.lib.ibm.ibmmq.utils.CommonUtils.createError;
import static io.ballerina.lib.ibm.ibmmq.utils.CommonUtils.getOptionalStringProperty;
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

    /**
     * Creates a JMS connection with the provided configurations.
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

    public static Object externAccessTopic(Environment env, BObject queueManagerObject, BString topicName,
                                           BString topicString, Long openAs, Long options) {
        MQQueueManager queueManager = (MQQueueManager) queueManagerObject.getNativeData(NATIVE_QUEUE_MANAGER);
        try {
            MQTopic mqTopic = queueManager.accessTopic(topicName.getValue(), topicString.getValue(),
                    openAs.intValue(), options.intValue());
            BObject bTopic = ValueCreator.createObjectValue(ModuleUtils.getModule(), CommonUtils.BTOPIC);
            bTopic.addNativeData(CommonUtils.TOPIC_OBJECT, mqTopic);
            return bTopic;
        } catch (MQException e) {
            return createError(IBMMQ_ERROR,
                    String.format("Error occurred while accessing topic: %s", e.getMessage()), e);
        }
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
}
