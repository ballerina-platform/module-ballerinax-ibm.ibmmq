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
import com.ibm.mq.MQGetMessageOptions;
import com.ibm.mq.MQMessage;
import com.ibm.mq.MQPutMessageOptions;
import com.ibm.mq.MQTopic;
import com.ibm.mq.constants.CMQC;
import io.ballerina.lib.ibm.ibmmq.config.GetMessageOptions;
import io.ballerina.runtime.api.Environment;
import io.ballerina.runtime.api.values.BMap;
import io.ballerina.runtime.api.values.BObject;
import io.ballerina.runtime.api.values.BString;

import static io.ballerina.lib.ibm.ibmmq.CommonUtils.createError;
import static io.ballerina.lib.ibm.ibmmq.Constants.IBMMQ_ERROR;

/**
 * Representation of {@link com.ibm.mq.MQTopic} with utility methods to invoke as inter-op functions.
 */
public class Topic {
    public static Object put(Environment environment, BObject topicObject, BMap message, long options) {
        MQTopic topic = (MQTopic) topicObject.getNativeData(Constants.NATIVE_TOPIC);
        MQMessage mqMessage = CommonUtils.getMqMessageFromBMessage(message);
        return environment.yieldAndRun(() -> {
            try {
                MQPutMessageOptions pmo = new MQPutMessageOptions();
                pmo.options = (int) options;
                topic.put(mqMessage, pmo);
                return null;
            } catch (Exception e) {
                return createError(IBMMQ_ERROR,
                        String.format("Error occurred while putting a message to the topic: %s", e.getMessage()), e);
            }
        });
    }

    public static Object get(Environment environment, BObject topicObject, BMap<BString, Object> bGetMsgOptions) {
        MQTopic topic = (MQTopic) topicObject.getNativeData(Constants.NATIVE_TOPIC);
        GetMessageOptions getMsgOptions = new GetMessageOptions(bGetMsgOptions);
        MQMessage mqMessage = CommonUtils.getMqMessage(getMsgOptions.matchOptions());
        MQGetMessageOptions mqGetMsgOptions = CommonUtils.getMqGetMsgOptions(getMsgOptions);
        return environment.yieldAndRun(() -> {
            try {
                topic.get(mqMessage, mqGetMsgOptions);
                return CommonUtils.getBMessageFromMQMessage(environment.getRuntime(), mqMessage);
            } catch (MQException e) {
                if (e.reasonCode == CMQC.MQRC_NO_MSG_AVAILABLE) {
                    return null;
                } else {
                    return createError(IBMMQ_ERROR,
                            String.format("Error occurred while getting a message from the topic: %s", e.getMessage()),
                            e);
                }
            }
        });
    }

    public static Object close(Environment env, BObject topicObject) {
        MQTopic topic = (MQTopic) topicObject.getNativeData(Constants.NATIVE_TOPIC);
        return env.yieldAndRun(() -> {
            try {
                topic.close();
                return null;
            } catch (MQException e) {
                return createError(IBMMQ_ERROR,
                        String.format("Error occurred while closing the topic: %s", e.getMessage()), e);
            }
        });
    }
}
