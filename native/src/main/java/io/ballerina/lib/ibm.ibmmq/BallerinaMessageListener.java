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

import io.ballerina.runtime.api.PredefinedTypes;
import io.ballerina.runtime.api.Runtime;
import io.ballerina.runtime.api.creators.TypeCreator;
import io.ballerina.runtime.api.types.Type;
import io.ballerina.runtime.api.values.BMap;
import io.ballerina.runtime.api.values.BObject;
import io.ballerina.runtime.api.values.BString;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;

import static io.ballerina.lib.ibm.ibmmq.CommonUtils.createError;
import static io.ballerina.lib.ibm.ibmmq.Constants.IBMMQ_ERROR;

/**
 * This class is an implementation of the MessageListener interface for Ballerina IBM MQ.
 */
public class BallerinaMessageListener implements MessageListener {
    private final Runtime runtime;
    private final BObject service;
    private final ServiceValidator serviceValidator;
    private final Type returnType = TypeCreator.createUnionType(PredefinedTypes.TYPE_ERROR, PredefinedTypes.TYPE_NULL);

    BallerinaMessageListener(Runtime runtime, BObject service) {
        this.runtime = runtime;
        this.service = service;
        this.serviceValidator = new ServiceValidator(service);
        serviceValidator.validate();
    }

    @Override
    public void onMessage(Message message) {
        try {
            BMap<BString, Object> bMessage = MessageMapper.toBallerinaMessage(message);
            if (this.serviceValidator.isOnMessageIsolated()) {
                this.runtime.invokeMethodAsyncConcurrently(
                        this.service,
                        this.serviceValidator.getOnMessageMethod().getName(),
                        null,
                        null,
                        null,
                        null,
                        this.returnType,
                        bMessage, false);
            } else {
                this.runtime.invokeMethodAsyncSequentially(
                        this.service,
                        this.serviceValidator.getOnMessageMethod().getName(),
                        null,
                        null,
                        null,
                        null,
                        this.returnType,
                        bMessage, false);
            }
        } catch (JMSException e) {
            throw createError(IBMMQ_ERROR, "Failed to map the IBMMQ message to a Ballerina message", e);
        }
    }
}
