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
import com.ibm.mq.MQMessage;
import io.ballerina.runtime.api.creators.ErrorCreator;
import io.ballerina.runtime.api.creators.ValueCreator;
import io.ballerina.runtime.api.utils.StringUtils;
import io.ballerina.runtime.api.values.BError;
import io.ballerina.runtime.api.values.BMap;
import io.ballerina.runtime.api.values.BString;

import java.io.IOException;
import java.util.Optional;

import static io.ballerina.lib.ibm.ibmmq.Constants.IBMMQ_ERROR;
import static io.ballerina.lib.ibm.ibmmq.ModuleUtils.getModule;

/**
 * {@code CommonUtils} contains the common utility functions for the Ballerina IBM MQ connector.
 */
public class CommonUtils {

    public static final String BTOPIC = "TOPIC";
    public static final String TOPIC_OBJECT = "TOPIC_OBJECT";

    private static final String ERROR_DETAILS = "ErrorDetails";
    private static final BString ERROR_REASON_CODE = StringUtils.fromString("ErrorDetails");
    private static final BString ERROR_ERROR_CODE = StringUtils.fromString("ErrorDetails");
    private static final BString ERROR_COMPLETION_CODE = StringUtils.fromString("ErrorDetails");
    private static final BString MESSAGE_PAYLOAD = StringUtils.fromString("payload");
    private static final BString MESSAGE_PROPERTIES = StringUtils.fromString("properties");

    public static MQMessage getMqMessage(BMap<BString, Object> bMessage) {
        byte[] payload = bMessage.getArrayValue(MESSAGE_PAYLOAD).getBytes();
        BMap<BString, Object> properties = (BMap<BString, Object>) bMessage.getMapValue(MESSAGE_PROPERTIES);

        MQMessage mqMessage = new MQMessage();
        try {
            mqMessage.write(payload);
        } catch (IOException e) {
            throw createError(IBMMQ_ERROR,
                    String.format("Error occurred while populating payload: %s", e.getMessage()), e);
        }
        for (BString key : properties.getKeys()) {
            try {
                mqMessage.setObjectProperty(key.getValue(), properties.get(key));
            } catch (MQException e) {
                throw createError(IBMMQ_ERROR,
                        String.format("Error occurred while setting message properties: %s", e.getMessage()), e);
            }
        }
        return new MQMessage();
    }

    public static BError createError(String errorType, String message, Throwable throwable) {
        BError cause = ErrorCreator.createError(throwable);
        BMap<BString, Object> errorDetails = ValueCreator.createRecordValue(getModule(), ERROR_DETAILS);
        if (throwable instanceof MQException) {
            errorDetails.put(ERROR_REASON_CODE, ((MQException) throwable).getReason());
            errorDetails.put(ERROR_ERROR_CODE, ((MQException) throwable).getErrorCode());
            errorDetails.put(ERROR_COMPLETION_CODE, ((MQException) throwable).getCompCode());
        }
        return ErrorCreator.createError(
                ModuleUtils.getModule(), errorType, StringUtils.fromString(message), cause, errorDetails);
    }

    public static Optional<String> getOptionalStringProperty(BMap<BString, Object> config, BString fieldName) {
        if (config.containsKey(fieldName)) {
            return Optional.of(config.getStringValue(fieldName).getValue());
        }
        return Optional.empty();
    }
}
