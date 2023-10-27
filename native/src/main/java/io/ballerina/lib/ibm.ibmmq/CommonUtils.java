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
import com.ibm.mq.MQPropertyDescriptor;
import io.ballerina.runtime.api.creators.ErrorCreator;
import io.ballerina.runtime.api.creators.TypeCreator;
import io.ballerina.runtime.api.creators.ValueCreator;
import io.ballerina.runtime.api.utils.StringUtils;
import io.ballerina.runtime.api.values.BError;
import io.ballerina.runtime.api.values.BMap;
import io.ballerina.runtime.api.values.BString;

import java.io.IOException;
import java.util.Collections;
import java.util.Enumeration;
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
    private static final BString ERROR_REASON_CODE = StringUtils.fromString("reasonCode");
    private static final BString ERROR_ERROR_CODE = StringUtils.fromString("errorCode");
    private static final BString ERROR_COMPLETION_CODE = StringUtils.fromString("completionCode");
    private static final BString MESSAGE_PAYLOAD = StringUtils.fromString("payload");
    private static final BString MESSAGE_PROPERTIES = StringUtils.fromString("properties");
    private static final BString MESSAGE_PROPERTY = StringUtils.fromString("property");
    private static final String BPROPERTY = "Property";
    private static final String BMESSAGE_NAME = "Message";
    private static final BString PD_VERSION = StringUtils.fromString("version");
    private static final BString PD_COPY_OPTIONS = StringUtils.fromString("copyOptions");
    private static final BString PD_OPTIONS = StringUtils.fromString("options");
    private static final BString PD_SUPPORT = StringUtils.fromString("support");
    private static final BString PD_CONTEXT = StringUtils.fromString("context");
    private static final BString PROPERTY_VALUE = StringUtils.fromString("value");
    private static final BString PROPERTY_DESCRIPTOR = StringUtils.fromString("descriptor");


    private static final MQPropertyDescriptor defaultPropertyDescriptor = new MQPropertyDescriptor();

    public static MQMessage getMqMessageFromBMessage(BMap<BString, Object> bMessage) {
        byte[] payload = bMessage.getArrayValue(MESSAGE_PAYLOAD).getBytes();
        MQMessage mqMessage = new MQMessage();
        try {
            mqMessage.write(payload);
        } catch (IOException e) {
            throw createError(IBMMQ_ERROR,
                    String.format("Error occurred while populating payload: %s", e.getMessage()), e);
        }
        BMap<BString, Object> properties = (BMap<BString, Object>) bMessage.getMapValue(MESSAGE_PROPERTIES);
        populateMQProperties(properties, mqMessage);
        return mqMessage;
    }

    public static BMap<BString, Object> getBMessageFromMQMessage(MQMessage mqMessage) {
        BMap<BString, Object> bMessage = ValueCreator.createRecordValue(getModule(), BMESSAGE_NAME);
        try {
            byte[] payload = new byte[mqMessage.getDataLength()];
            mqMessage.readFully(payload);
            bMessage.put(MESSAGE_PAYLOAD, payload);
            bMessage.put(MESSAGE_PROPERTY, getBProperties(mqMessage));
            return bMessage;
        } catch (MQException | IOException e) {
            throw createError(IBMMQ_ERROR,
                    String.format("Error occurred while reading the message: %s", e.getMessage()), e);
        }
    }

    private static BMap<BString, Object> getBProperties(MQMessage mqMessage) throws MQException {
        BMap<BString, Object> properties = ValueCreator.createMapValue(TypeCreator
                .createMapType(TypeCreator.createRecordType(BPROPERTY, getModule(), 0, false, 0)));
        Enumeration<String> propertyNames = mqMessage.getPropertyNames("%");
        for (String propertyName : Collections.list(propertyNames)) {
            BMap<BString, Object> property = ValueCreator.createRecordValue(getModule(), BPROPERTY);
            MQPropertyDescriptor propertyDescriptor = new MQPropertyDescriptor();
            Object propertyObject = mqMessage.getObjectProperty(propertyName, propertyDescriptor);
            if (propertyObject instanceof Integer) {
                property.put(PROPERTY_VALUE, ((Integer) propertyObject).longValue());
            } else if (propertyObject instanceof String) {
                property.put(PROPERTY_VALUE, StringUtils.fromString((String) propertyObject));
            } else {
                property.put(PROPERTY_VALUE, propertyObject);
            }
            property.put(PROPERTY_DESCRIPTOR,
                    populateDescriptorFromMQPropertyDescriptor(propertyDescriptor));
            properties.put(StringUtils.fromString(propertyName), property);
        }
        return properties;
    }

    private static void populateMQProperties(BMap<BString, Object> properties, MQMessage mqMessage) {
        for (BString key : properties.getKeys()) {
            try {
                handlePropertyValue(properties, mqMessage, key);
            } catch (MQException e) {
                throw createError(IBMMQ_ERROR,
                        String.format("Error occurred while setting message properties: %s", e.getMessage()), e);
            }
        }
    }

    private static void handlePropertyValue(BMap<BString, Object> properties, MQMessage mqMessage, BString key)
            throws MQException {
        BMap<BString, Object> property = (BMap<BString, Object>) properties.getMapValue(key);
        MQPropertyDescriptor propertyDescriptor = defaultPropertyDescriptor;
        if (property.containsKey(PROPERTY_DESCRIPTOR)) {
            propertyDescriptor = getMQPropertyDescriptor(properties.getMapValue(PROPERTY_DESCRIPTOR));
        }
        Object value = property.get(PROPERTY_VALUE);
        if (value instanceof Long) {
            mqMessage.setIntProperty(key.getValue(), propertyDescriptor, ((Long) properties.get(key)).intValue());
        } else if (value instanceof Boolean) {
            mqMessage.setBooleanProperty(key.getValue(), propertyDescriptor, ((Boolean) properties.get(key)));
        } else if (value instanceof Byte) {
            mqMessage.setByteProperty(key.getValue(), propertyDescriptor, (Byte) properties.get(key));
        } else if (value instanceof byte[]) {
            mqMessage.setBytesProperty(key.getValue(), propertyDescriptor, ((byte[]) properties.get(key)));
        } else if (value instanceof Float) {
            mqMessage.setFloatProperty(key.getValue(), propertyDescriptor, (Float) properties.get(key));
        } else if (value instanceof Double) {
            mqMessage.setDoubleProperty(key.getValue(), propertyDescriptor, (Double) properties.get(key));
        } else if (value instanceof BString) {
            mqMessage.setStringProperty(key.getValue(), propertyDescriptor, ((BString) properties.get(key)).getValue());
        }
    }

    private static MQPropertyDescriptor getMQPropertyDescriptor(BMap descriptor) {
        MQPropertyDescriptor propertyDescriptor = new MQPropertyDescriptor();
        if (descriptor.containsKey(PD_VERSION)) {
            propertyDescriptor.version = ((Long) descriptor.get(PD_VERSION)).intValue();
        }
        if (descriptor.containsKey(PD_COPY_OPTIONS)) {
            propertyDescriptor.copyOptions = ((Long) descriptor.get(PD_COPY_OPTIONS)).intValue();
        }
        if (descriptor.containsKey(PD_OPTIONS)) {
            propertyDescriptor.options = ((Long) descriptor.get(PD_OPTIONS)).intValue();
        }
        if (descriptor.containsKey(PD_SUPPORT)) {
            propertyDescriptor.support = ((Long) descriptor.get(PD_SUPPORT)).intValue();
        }
        if (descriptor.containsKey(PD_CONTEXT)) {
            propertyDescriptor.context = ((Long) descriptor.get(PD_CONTEXT)).intValue();
        }
        return propertyDescriptor;
    }

    private static BMap populateDescriptorFromMQPropertyDescriptor(MQPropertyDescriptor propertyDescriptor) {
        BMap<BString, Object> descriptor = ValueCreator.createMapValue();
        descriptor.put(PD_VERSION, propertyDescriptor.version);
        descriptor.put(PD_COPY_OPTIONS, propertyDescriptor.copyOptions);
        descriptor.put(PD_OPTIONS, propertyDescriptor.options);
        descriptor.put(PD_SUPPORT, propertyDescriptor.support);
        descriptor.put(PD_CONTEXT, propertyDescriptor.context);
        return descriptor;
    }

    public static BError createError(String errorType, String message, Throwable throwable) {
        BError cause = ErrorCreator.createError(throwable);
        BMap<BString, Object> errorDetails = ValueCreator.createRecordValue(getModule(), ERROR_DETAILS);
        if (throwable instanceof MQException exception) {
            errorDetails.put(ERROR_REASON_CODE, exception.getReason());
            errorDetails.put(ERROR_ERROR_CODE, exception.getErrorCode());
            errorDetails.put(ERROR_COMPLETION_CODE, exception.getCompCode());
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
