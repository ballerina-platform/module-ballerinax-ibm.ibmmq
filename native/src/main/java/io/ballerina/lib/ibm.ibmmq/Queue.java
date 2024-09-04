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
import com.ibm.mq.MQQueue;
import com.ibm.mq.constants.CMQC;
import io.ballerina.lib.ibm.ibmmq.config.GetMessageOptions;
import io.ballerina.runtime.api.Environment;
import io.ballerina.runtime.api.Future;
import io.ballerina.runtime.api.values.BError;
import io.ballerina.runtime.api.values.BMap;
import io.ballerina.runtime.api.values.BObject;
import io.ballerina.runtime.api.values.BString;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static io.ballerina.lib.ibm.ibmmq.CommonUtils.createError;
import static io.ballerina.lib.ibm.ibmmq.Constants.IBMMQ_ERROR;

/**
 * Representation of {@link com.ibm.mq.MQQueue} with utility methods to invoke as inter-op functions.
 */
public class Queue {
    private static final ExecutorService QUEUE_EXECUTOR_SERVICE = Executors.newCachedThreadPool(
            new MQThreadFactory("balx-ibmmq-queue-client-network-thread"));

    public static Object put(Environment environment, BObject queueObject, BMap<BString, Object> message,
                             long options) {
        MQQueue queue = (MQQueue) queueObject.getNativeData(Constants.NATIVE_QUEUE);
        MQMessage mqMessage = CommonUtils.getMqMessageFromBMessage(message);
        Future future = environment.markAsync();
        QUEUE_EXECUTOR_SERVICE.execute(() -> {
            try {
                MQPutMessageOptions pmo = new MQPutMessageOptions();
                pmo.options = (int) options;
                queue.put(mqMessage, pmo);
                future.complete(null);
            } catch (MQException e) {
                BError bError = createError(IBMMQ_ERROR,
                        String.format("Error occurred while putting a message to the queue: %s", e.getMessage()), e);
                future.complete(bError);
            }
        });
        return null;
    }

    public static Object get(Environment environment, BObject queueObject, BMap<BString, Object> bGetMsgOptions) {
        MQQueue queue = (MQQueue) queueObject.getNativeData(Constants.NATIVE_QUEUE);
        GetMessageOptions getMsgOptions = new GetMessageOptions(bGetMsgOptions);
        MQMessage mqMessage = CommonUtils.getMqMessage(getMsgOptions.matchOptions());
        MQGetMessageOptions mqGetMsgOptions = CommonUtils.getMqGetMsgOptions(getMsgOptions);
        Future future = environment.markAsync();
        QUEUE_EXECUTOR_SERVICE.execute(() -> {
            try {
                queue.get(mqMessage, mqGetMsgOptions);
                future.complete(CommonUtils.getBMessageFromMQMessage(environment.getRuntime(), mqMessage));
            } catch (MQException e) {
                if (e.reasonCode == CMQC.MQRC_NO_MSG_AVAILABLE) {
                    future.complete(null);
                } else {
                    BError bError = createError(IBMMQ_ERROR,
                            String.format("Error occurred while getting a message from the queue: %s",
                                    e.getMessage()), e);
                    future.complete(bError);
                }
            }
        });
        return null;
    }

    public static Object close(Environment env, BObject queueObject) {
        MQQueue queue = (MQQueue) queueObject.getNativeData(Constants.NATIVE_QUEUE);
        Future future = env.markAsync();
        QUEUE_EXECUTOR_SERVICE.execute(() -> {
            try {
                queue.close();
                future.complete(null);
            } catch (MQException e) {
                BError bError = createError(IBMMQ_ERROR,
                        String.format("Error occurred while closing the queue: %s", e.getMessage()), e);
                future.complete(bError);
            }
        });
        return null;
    }
}
