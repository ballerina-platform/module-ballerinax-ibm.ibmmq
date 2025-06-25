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
import com.ibm.mq.MQPropertyDescriptor;
import com.ibm.mq.constants.MQConstants;
import com.ibm.mq.headers.MQHeaderList;
import io.ballerina.lib.ibm.ibmmq.config.GetMessageOptions;
import io.ballerina.lib.ibm.ibmmq.config.MatchOptions;
import io.ballerina.lib.ibm.ibmmq.headers.MQRFH2Header;
import io.ballerina.runtime.api.Runtime;
import io.ballerina.runtime.api.creators.ErrorCreator;
import io.ballerina.runtime.api.creators.TypeCreator;
import io.ballerina.runtime.api.creators.ValueCreator;
import io.ballerina.runtime.api.flags.SymbolFlags;
import io.ballerina.runtime.api.types.ArrayType;
import io.ballerina.runtime.api.types.PredefinedTypes;
import io.ballerina.runtime.api.utils.StringUtils;
import io.ballerina.runtime.api.values.BArray;
import io.ballerina.runtime.api.values.BError;
import io.ballerina.runtime.api.values.BMap;
import io.ballerina.runtime.api.values.BString;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import javax.jms.BytesMessage;
import javax.jms.DeliveryMode;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;

import static io.ballerina.lib.ibm.ibmmq.Constants.BMESSAGE_NAME;
import static io.ballerina.lib.ibm.ibmmq.Constants.BPROPERTY;
import static io.ballerina.lib.ibm.ibmmq.Constants.CORRELATION_ID_FIELD;
import static io.ballerina.lib.ibm.ibmmq.Constants.ERROR_COMPLETION_CODE;
import static io.ballerina.lib.ibm.ibmmq.Constants.ERROR_DETAILS;
import static io.ballerina.lib.ibm.ibmmq.Constants.ERROR_ERROR_CODE;
import static io.ballerina.lib.ibm.ibmmq.Constants.ERROR_REASON_CODE;
import static io.ballerina.lib.ibm.ibmmq.Constants.EXPIRY_FIELD;
import static io.ballerina.lib.ibm.ibmmq.Constants.FORMAT_FIELD;
import static io.ballerina.lib.ibm.ibmmq.Constants.IBMMQ_ERROR;
import static io.ballerina.lib.ibm.ibmmq.Constants.MESSAGE_ACCOUNTING_TOKEN;
import static io.ballerina.lib.ibm.ibmmq.Constants.MESSAGE_CHARSET;
import static io.ballerina.lib.ibm.ibmmq.Constants.MESSAGE_ENCODING;
import static io.ballerina.lib.ibm.ibmmq.Constants.MESSAGE_HEADERS;
import static io.ballerina.lib.ibm.ibmmq.Constants.MESSAGE_ID_FIELD;
import static io.ballerina.lib.ibm.ibmmq.Constants.MESSAGE_PAYLOAD;
import static io.ballerina.lib.ibm.ibmmq.Constants.MESSAGE_PROPERTIES;
import static io.ballerina.lib.ibm.ibmmq.Constants.MESSAGE_PROPERTY;
import static io.ballerina.lib.ibm.ibmmq.Constants.MESSAGE_TYPE_FIELD;
import static io.ballerina.lib.ibm.ibmmq.Constants.MESSAGE_USERID;
import static io.ballerina.lib.ibm.ibmmq.Constants.MQCIH_RECORD_NAME;
import static io.ballerina.lib.ibm.ibmmq.Constants.MQIIH_RECORD_NAME;
import static io.ballerina.lib.ibm.ibmmq.Constants.MQRFH2_RECORD_NAME;
import static io.ballerina.lib.ibm.ibmmq.Constants.MQRFH_RECORD_NAME;
import static io.ballerina.lib.ibm.ibmmq.Constants.PD_CONTEXT;
import static io.ballerina.lib.ibm.ibmmq.Constants.PD_COPY_OPTIONS;
import static io.ballerina.lib.ibm.ibmmq.Constants.PD_OPTIONS;
import static io.ballerina.lib.ibm.ibmmq.Constants.PD_SUPPORT;
import static io.ballerina.lib.ibm.ibmmq.Constants.PD_VERSION;
import static io.ballerina.lib.ibm.ibmmq.Constants.PERSISTENCE_FIELD;
import static io.ballerina.lib.ibm.ibmmq.Constants.PRIORITY_FIELD;
import static io.ballerina.lib.ibm.ibmmq.Constants.PROPERTY_DESCRIPTOR;
import static io.ballerina.lib.ibm.ibmmq.Constants.PROPERTY_VALUE;
import static io.ballerina.lib.ibm.ibmmq.Constants.PUT_APPLICATION_TYPE_FIELD;
import static io.ballerina.lib.ibm.ibmmq.Constants.REPLY_TO_QM_NAME_FIELD;
import static io.ballerina.lib.ibm.ibmmq.Constants.REPLY_TO_QUEUE_NAME_FIELD;
import static io.ballerina.lib.ibm.ibmmq.Constants.USER_ID;
import static io.ballerina.lib.ibm.ibmmq.ModuleUtils.getModule;
import static io.ballerina.lib.ibm.ibmmq.headers.MQCIHHeader.createMQCIHHeaderFromBHeader;
import static io.ballerina.lib.ibm.ibmmq.headers.MQIIHHeader.createMQIIHHeaderFromBHeader;
import static io.ballerina.lib.ibm.ibmmq.headers.MQRFH2Header.createMQRFH2HeaderFromBHeader;
import static io.ballerina.lib.ibm.ibmmq.headers.MQRFHHeader.createMQRFHHeaderFromBHeader;

/**
 * {@code CommonUtils} contains the common utility functions for the Ballerina IBM MQ connector.
 */
public class CommonUtils {
    private static final MQPropertyDescriptor defaultPropertyDescriptor = new MQPropertyDescriptor();
    private static final ArrayType BHeaderUnionType = TypeCreator.createArrayType(
            TypeCreator.createUnionType(List.of(
                    TypeCreator.createRecordType(MQRFH2_RECORD_NAME, getModule(), SymbolFlags.PUBLIC, true, 0),
                    TypeCreator.createRecordType(MQRFH_RECORD_NAME, getModule(), SymbolFlags.PUBLIC, true, 0),
                    TypeCreator.createRecordType(MQCIH_RECORD_NAME, getModule(), SymbolFlags.PUBLIC, true, 0),
                    TypeCreator.createRecordType(MQIIH_RECORD_NAME, getModule(), SymbolFlags.PUBLIC, true, 0))));

    public static MQMessage getMqMessageFromBMessage(BMap<BString, Object> bMessage) {
        MQMessage mqMessage = new MQMessage();
        BMap<BString, Object> properties = (BMap<BString, Object>) bMessage.getMapValue(MESSAGE_PROPERTIES);
        if (Objects.nonNull(properties)) {
            populateMQProperties(properties, mqMessage);
        }
        BArray headers = bMessage.getArrayValue(MESSAGE_HEADERS);
        if (Objects.nonNull(headers)) {
            populateMQHeaders(headers, mqMessage);
        }
        byte[] payload = bMessage.getArrayValue(MESSAGE_PAYLOAD).getBytes();
        assignOptionalFieldsToMqMessage(bMessage, mqMessage);
        try {
            mqMessage.write(payload);
        } catch (IOException e) {
            throw createError(IBMMQ_ERROR,
                    String.format("Error occurred while populating payload: %s", e.getMessage()), e);
        }
        return mqMessage;
    }

    public static Message getJmsMessageFromBMessage(Session session, BMap bMessage) {
        try {
            byte[] payload = bMessage.getArrayValue(MESSAGE_PAYLOAD).getBytes();
            BytesMessage message = session.createBytesMessage();
            message.writeBytes(payload);
            if (bMessage.containsKey(MESSAGE_PROPERTIES)) {
                populateMessageProperties(bMessage.getMapValue(MESSAGE_PROPERTIES), message);
            }

            // Optional fields
            if (bMessage.containsKey(CORRELATION_ID_FIELD)) {
                byte[] correlationId = bMessage.getArrayValue(CORRELATION_ID_FIELD).getBytes();
                message.setJMSCorrelationIDAsBytes(correlationId);
            }

            if (bMessage.containsKey(MESSAGE_ID_FIELD)) {
                byte[] messageId = bMessage.getArrayValue(MESSAGE_ID_FIELD).getBytes();
                message.setJMSMessageID(new String(messageId, StandardCharsets.UTF_8));
            }

            if (bMessage.containsKey(PRIORITY_FIELD)) {
                int priority = ((Long) bMessage.get(PRIORITY_FIELD)).intValue();
                message.setJMSPriority(priority);
            }

            if (bMessage.containsKey(EXPIRY_FIELD)) {
                long ttl = ((Long) bMessage.get(EXPIRY_FIELD)).longValue();
                message.setJMSExpiration(ttl);
            }

            if (bMessage.containsKey(PERSISTENCE_FIELD)) {
                int persistence = ((Long) bMessage.get(PERSISTENCE_FIELD)).intValue();
                int deliveryMode = (persistence == 1) ? DeliveryMode.PERSISTENT : DeliveryMode.NON_PERSISTENT;
                message.setJMSDeliveryMode(deliveryMode); // Note: normally set via producer.send(...)
            }

            if (bMessage.containsKey(USER_ID)) {
                String userId = bMessage.getStringValue(USER_ID).getValue();
                message.setStringProperty("JMSXUserID", userId);
            }

            if (bMessage.containsKey(REPLY_TO_QUEUE_NAME_FIELD)) {
                String replyToQueue = bMessage.getStringValue(REPLY_TO_QUEUE_NAME_FIELD).getValue();
                Destination replyTo = session.createQueue(replyToQueue);
                message.setJMSReplyTo(replyTo);
            }
            return message;
        } catch (JMSException e) {
            throw createError(IBMMQ_ERROR,
                    String.format("Error occurred while creating JMS message from Ballerina message: %s",
                            e.getMessage()), e);
        } catch (Exception e) {
            throw createError(IBMMQ_ERROR,
                    String.format("Unexpected error occurred while creating JMS message from Ballerina message: %s",
                            e.getMessage()), e);
        }
    }

    public static BMap<BString, Object> getBMessageFromMQMessage(Runtime runtime, MQMessage mqMessage) {
        BMap<BString, Object> bMessage = ValueCreator.createRecordValue(getModule(), BMESSAGE_NAME);
        try {
            bMessage.put(MESSAGE_HEADERS, getBHeaders(runtime, mqMessage));
            bMessage.put(MESSAGE_PROPERTY, getBProperties(mqMessage));
            bMessage.put(FORMAT_FIELD, StringUtils.fromString(mqMessage.format));
            bMessage.put(MESSAGE_ID_FIELD, ValueCreator.createArrayValue(mqMessage.messageId));
            bMessage.put(CORRELATION_ID_FIELD, ValueCreator.createArrayValue((mqMessage.correlationId)));
            bMessage.put(EXPIRY_FIELD, mqMessage.expiry);
            bMessage.put(PRIORITY_FIELD, mqMessage.priority);
            bMessage.put(PERSISTENCE_FIELD, mqMessage.persistence);
            bMessage.put(MESSAGE_TYPE_FIELD, mqMessage.messageType);
            bMessage.put(PUT_APPLICATION_TYPE_FIELD, mqMessage.putApplicationType);
            bMessage.put(REPLY_TO_QUEUE_NAME_FIELD, StringUtils.fromString(mqMessage.replyToQueueName.strip()));
            bMessage.put(REPLY_TO_QM_NAME_FIELD, StringUtils.fromString(mqMessage.replyToQueueManagerName.strip()));
            bMessage.put(MESSAGE_ENCODING, mqMessage.encoding);
            bMessage.put(MESSAGE_CHARSET, mqMessage.characterSet);
            if (Objects.nonNull(mqMessage.accountingToken)) {
                bMessage.put(MESSAGE_ACCOUNTING_TOKEN, ValueCreator.createArrayValue(mqMessage.accountingToken));
            }
            if (Objects.nonNull(mqMessage.userId)) {
                bMessage.put(MESSAGE_USERID, StringUtils.fromString(mqMessage.userId.strip()));
            }
            byte[] payload = mqMessage.readStringOfByteLength(mqMessage.getDataLength())
                    .getBytes(StandardCharsets.UTF_8);
            bMessage.put(MESSAGE_PAYLOAD, ValueCreator.createArrayValue(payload));
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
            if (propertyObject instanceof Integer intProperty) {
                property.put(PROPERTY_VALUE, intProperty.longValue());
            } else if (propertyObject instanceof String stringProperty) {
                property.put(PROPERTY_VALUE, StringUtils.fromString(stringProperty));
            } else if (propertyObject instanceof byte[] bytesProperty) {
                property.put(PROPERTY_VALUE, ValueCreator.createArrayValue(bytesProperty));
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

    private static void populateMessageProperties(BMap<BString, Object> properties, Message jmsMessage) {
        for (BString key : properties.getKeys()) {
            try {
                handleMessagePropertyValues(properties, jmsMessage, key);
            } catch (Exception e) {
                throw createError(IBMMQ_ERROR,
                        String.format("Error occurred while setting message properties: %s", e.getMessage()), e);
            }
        }
    }

    @SuppressWarnings("unchecked")
    private static void handlePropertyValue(BMap<BString, Object> properties, MQMessage mqMessage, BString key)
            throws MQException {
        BMap<BString, Object> property = (BMap<BString, Object>) properties.getMapValue(key);
        MQPropertyDescriptor propertyDescriptor = defaultPropertyDescriptor;
        if (property.containsKey(PROPERTY_DESCRIPTOR)) {
            propertyDescriptor = getMQPropertyDescriptor(
                    (BMap<BString, Object>) properties.getMapValue(PROPERTY_DESCRIPTOR));
        }
        Object value = property.get(PROPERTY_VALUE);
        if (value instanceof Long longValue) {
            mqMessage.setIntProperty(key.getValue(), propertyDescriptor, longValue.intValue());
        } else if (value instanceof Boolean booleanValue) {
            mqMessage.setBooleanProperty(key.getValue(), propertyDescriptor, booleanValue);
        } else if (value instanceof Byte byteValue) {
            mqMessage.setByteProperty(key.getValue(), propertyDescriptor, byteValue);
        } else if (value instanceof byte[] bytesValue) {
            mqMessage.setBytesProperty(key.getValue(), propertyDescriptor, bytesValue);
        } else if (value instanceof Float floatValue) {
            mqMessage.setFloatProperty(key.getValue(), propertyDescriptor, floatValue);
        } else if (value instanceof Double doubleValue) {
            mqMessage.setDoubleProperty(key.getValue(), propertyDescriptor, doubleValue);
        } else if (value instanceof BString stringValue) {
            mqMessage.setStringProperty(key.getValue(), propertyDescriptor, stringValue.getValue());
        }
    }

    private static void handleMessagePropertyValues(BMap<BString, Object> properties, Message jmsMessage, BString key)
            throws Exception {
        BMap<BString, Object> property = (BMap<BString, Object>) properties.getMapValue(key);
        Object value = property.get(PROPERTY_VALUE);
        if (value instanceof Long longValue) {
            jmsMessage.setLongProperty(key.getValue(), longValue);
        } else if (value instanceof Boolean booleanValue) {
            jmsMessage.setBooleanProperty(key.getValue(), booleanValue);
        } else if (value instanceof Byte byteValue) {
            jmsMessage.setByteProperty(key.getValue(), byteValue);
        } else if (value instanceof byte[] bytesValue) {
            jmsMessage.setObjectProperty(key.getValue(), bytesValue);
        } else if (value instanceof Float floatValue) {
            jmsMessage.setFloatProperty(key.getValue(), floatValue);
        } else if (value instanceof Double doubleValue) {
            jmsMessage.setDoubleProperty(key.getValue(), doubleValue);
        } else if (value instanceof BString stringValue) {
            jmsMessage.setStringProperty(key.getValue(), stringValue.getValue());
        }
    }

    private static void assignOptionalFieldsToMqMessage(BMap<BString, Object> bMessage, MQMessage mqMessage) {
        if (bMessage.containsKey(FORMAT_FIELD)) {
            mqMessage.format = bMessage.getStringValue(FORMAT_FIELD).getValue();
        }
        if (bMessage.containsKey(MESSAGE_ID_FIELD)) {
            mqMessage.messageId = bMessage.getArrayValue(MESSAGE_ID_FIELD).getByteArray();
        }
        if (bMessage.containsKey(CORRELATION_ID_FIELD)) {
            mqMessage.correlationId = bMessage.getArrayValue(CORRELATION_ID_FIELD).getByteArray();
        }
        if (bMessage.containsKey(EXPIRY_FIELD)) {
            mqMessage.expiry = bMessage.getIntValue(EXPIRY_FIELD).intValue();
        }
        if (bMessage.containsKey(PRIORITY_FIELD)) {
            mqMessage.priority = bMessage.getIntValue(PRIORITY_FIELD).intValue();
        }
        if (bMessage.containsKey(PERSISTENCE_FIELD)) {
            mqMessage.persistence = bMessage.getIntValue(PERSISTENCE_FIELD).intValue();
        }
        if (bMessage.containsKey(MESSAGE_TYPE_FIELD)) {
            mqMessage.messageType = bMessage.getIntValue(MESSAGE_TYPE_FIELD).intValue();
        }
        if (bMessage.containsKey(PUT_APPLICATION_TYPE_FIELD)) {
            mqMessage.putApplicationType = bMessage.getIntValue(PUT_APPLICATION_TYPE_FIELD).intValue();
        }
        if (bMessage.containsKey(REPLY_TO_QUEUE_NAME_FIELD)) {
            mqMessage.replyToQueueName = bMessage.getStringValue(REPLY_TO_QUEUE_NAME_FIELD).getValue();
        }
        if (bMessage.containsKey(REPLY_TO_QM_NAME_FIELD)) {
            mqMessage.replyToQueueManagerName = bMessage.getStringValue(REPLY_TO_QM_NAME_FIELD).getValue();
        }
        if (bMessage.containsKey(MESSAGE_ENCODING)) {
            mqMessage.encoding = bMessage.getIntValue(MESSAGE_ENCODING).intValue();
        }
        if (bMessage.containsKey(MESSAGE_CHARSET)) {
            mqMessage.characterSet = bMessage.getIntValue(MESSAGE_CHARSET).intValue();
        }
        if (bMessage.containsKey(MESSAGE_ACCOUNTING_TOKEN)) {
            mqMessage.accountingToken = bMessage.getArrayValue(MESSAGE_ACCOUNTING_TOKEN).getByteArray();
        }
        if (bMessage.containsKey(MESSAGE_USERID)) {
            mqMessage.userId = bMessage.getStringValue(MESSAGE_USERID).getValue();
        }
    }

    private static MQPropertyDescriptor getMQPropertyDescriptor(BMap<BString, Object> descriptor) {
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

    private static void populateMQHeaders(BArray bHeaders, MQMessage mqMessage) {
        MQHeaderList headerList = new MQHeaderList();
        for (int i = 0; i < bHeaders.size(); i++) {
            BMap<BString, Object> bHeader = (BMap) bHeaders.get(i);
            HeaderType headerType = HeaderType.valueOf(bHeader.getType().getName());
            switch (headerType) {
                case MQRFH2 -> headerList.add(createMQRFH2HeaderFromBHeader(bHeader));
                case MQRFH -> headerList.add(createMQRFHHeaderFromBHeader(bHeader));
                case MQCIH -> headerList.add(createMQCIHHeaderFromBHeader(bHeader));
                case MQIIH -> headerList.add(createMQIIHHeaderFromBHeader(bHeader));
                default -> throw createError(IBMMQ_ERROR, String.format("Error occurred while populating headers: " +
                        "Unsupported header type %s", headerType), null);
            }
        }
        try {
            headerList.write(mqMessage);
        } catch (IOException e) {
            throw createError(IBMMQ_ERROR,
                    String.format("Error occurred while putting a message to the topic: %s", e.getMessage()), e);
        }
    }

    private static BMap populateDescriptorFromMQPropertyDescriptor(MQPropertyDescriptor propertyDescriptor) {
        BMap<BString, Object> descriptor = ValueCreator.createMapValue(TypeCreator
                .createMapType(PredefinedTypes.TYPE_INT));
        descriptor.put(PD_VERSION, propertyDescriptor.version);
        descriptor.put(PD_COPY_OPTIONS, propertyDescriptor.copyOptions);
        descriptor.put(PD_OPTIONS, propertyDescriptor.options);
        descriptor.put(PD_SUPPORT, propertyDescriptor.support);
        descriptor.put(PD_CONTEXT, propertyDescriptor.context);
        return descriptor;
    }

    public static MQMessage getMqMessage(MatchOptions matchOptions) {
        MQMessage message = new MQMessage();
        if (Objects.isNull(matchOptions)) {
            return message;
        }

        if (Objects.nonNull(matchOptions.messageId())) {
            message.messageId = matchOptions.messageId();
        }
        if (Objects.nonNull(matchOptions.correlationId())) {
            message.correlationId = matchOptions.correlationId();
        }
        return message;
    }

    public static MQGetMessageOptions getMqGetMsgOptions(GetMessageOptions getMsgOptions) {
        MQGetMessageOptions mqGetMsgOptions = new MQGetMessageOptions();
        mqGetMsgOptions.waitInterval = getMsgOptions.waitInterval();
        mqGetMsgOptions.options = getMsgOptions.options();

        MatchOptions matchOptions = getMsgOptions.matchOptions();
        if (Objects.isNull(matchOptions)) {
            return mqGetMsgOptions;
        }

        int matchOpt = MQConstants.MQMO_NONE;
        if (Objects.nonNull(matchOptions.messageId())) {
            matchOpt |= MQConstants.MQMO_MATCH_MSG_ID;
        }
        if (Objects.nonNull(matchOptions.correlationId())) {
            matchOpt |= MQConstants.MQMO_MATCH_CORREL_ID;
        }
        mqGetMsgOptions.matchOptions = matchOpt;
        return mqGetMsgOptions;
    }

    private static Object getBHeaders(Runtime runtime, MQMessage mqMessage) {
        ArrayList<BMap<BString, Object>> bHeaders = new ArrayList<>();
        try {
            MQRFH2Header.decodeHeader(runtime, mqMessage, bHeaders);
        } catch (IOException e) {
            throw createError(IBMMQ_ERROR,
                    String.format("Error occurred while reading headers: %s", e.getMessage()), e);
        }
        if (bHeaders.isEmpty()) {
            return null;
        }
        BArray headerArray = ValueCreator.createArrayValue(BHeaderUnionType);
        for (BMap<BString, Object> header : bHeaders) {
            headerArray.append(header);
        }
        return headerArray;
    }

    public static BError createError(String errorType, String message) {
        return createError(errorType, message, null);
    }

    public static BError createError(String errorType, String message, Throwable throwable) {
        BError cause = null;
        if (throwable != null) {
            cause = ErrorCreator.createError(throwable);
        }
        BMap<BString, Object> errorDetails = ValueCreator.createRecordValue(getModule(), ERROR_DETAILS);
        if (throwable instanceof MQException exception) {
            errorDetails.put(ERROR_REASON_CODE, exception.getReason());
            errorDetails.put(ERROR_ERROR_CODE, StringUtils.fromString(exception.getErrorCode()));
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
