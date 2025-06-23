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

import io.ballerina.runtime.api.Runtime;
import io.ballerina.runtime.api.concurrent.StrandMetadata;
import io.ballerina.runtime.api.values.BError;
import io.ballerina.runtime.api.values.BMap;
import io.ballerina.runtime.api.values.BObject;
import io.ballerina.runtime.api.values.BString;

import java.io.PrintStream;

import javax.jms.Message;
import javax.jms.MessageListener;

import static io.ballerina.lib.ibm.ibmmq.CommonUtils.createError;
import static io.ballerina.lib.ibm.ibmmq.Constants.IBMMQ_ERROR;

/**
 * Implements the IBMMQ {@code MessageListener} class for the Ballerina IBM MQ connector.
 */
public class BallerinaIbmmqListener implements MessageListener {
    private static final PrintStream OUT = System.out;
    private static final String ON_MESSAGE = "onMessage";
    private static final String ON_ERROR = "onError";

    private final Runtime runtime;
    private final BObject service;
    private final IbmmqService nativeService;

    BallerinaIbmmqListener(Runtime runtime, BObject service, IbmmqService nativeService) {
        this.runtime = runtime;
        this.service = service;
        this.nativeService = nativeService;
    }

    @Override
    public void onMessage(Message message) {
        Thread.startVirtualThread(() -> {
            try {
                boolean isIsolated = this.nativeService.isOnMessageIsolated();
                BMap<BString, Object> ballerinaMessage = MessageMapper.toBallerinaMessage(message);
                StrandMetadata strandMetadata = new StrandMetadata(isIsolated, null);
                this.runtime.callMethod(this.service, ON_MESSAGE, strandMetadata, ballerinaMessage);
            } catch (Throwable e) {
                OUT.println("Error occurred while processing the message: " + e.getMessage());
                BError error = createError(IBMMQ_ERROR, "Failed to fetch the message", e);
                if (this.nativeService.hasOnError()) {
                    boolean isIsolated = this.nativeService.isOnErrorIsolated();
                    this.runtime.callMethod(this.service, ON_ERROR, new StrandMetadata(isIsolated, null), error);
                } else {
                    throw error;
                }
            }
        });
    }
}
