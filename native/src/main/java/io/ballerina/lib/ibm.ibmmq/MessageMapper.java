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

import io.ballerina.runtime.api.creators.ValueCreator;
import io.ballerina.runtime.api.utils.StringUtils;
import io.ballerina.runtime.api.values.BMap;
import io.ballerina.runtime.api.values.BString;

import java.io.UnsupportedEncodingException;
import java.util.Enumeration;

import javax.jms.BytesMessage;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.TextMessage;

import static io.ballerina.lib.ibm.ibmmq.CommonUtils.createError;
import static io.ballerina.lib.ibm.ibmmq.Constants.CORRELATION_ID_FIELD;
import static io.ballerina.lib.ibm.ibmmq.Constants.EXPIRY_FIELD;
import static io.ballerina.lib.ibm.ibmmq.Constants.FORMAT_FIELD;
import static io.ballerina.lib.ibm.ibmmq.Constants.IBMMQ_ERROR;
import static io.ballerina.lib.ibm.ibmmq.Constants.MESSAGE_ID_FIELD;
import static io.ballerina.lib.ibm.ibmmq.Constants.MESSAGE_PAYLOAD;
import static io.ballerina.lib.ibm.ibmmq.Constants.MESSAGE_PROPERTIES;
import static io.ballerina.lib.ibm.ibmmq.Constants.MESSAGE_USERID;
import static io.ballerina.lib.ibm.ibmmq.Constants.PERSISTENCE_FIELD;
import static io.ballerina.lib.ibm.ibmmq.Constants.PRIORITY_FIELD;
import static io.ballerina.lib.ibm.ibmmq.Constants.REPLY_TO_QUEUE_NAME_FIELD;
import static io.ballerina.lib.ibm.ibmmq.ModuleUtils.getModule;

public class MessageMapper {
    static final BString TEXT = StringUtils.fromString("text");
    static final BString BINARY = StringUtils.fromString("binary");
    static final BString UNKNOWN = StringUtils.fromString("unknown");

    public static BMap<BString, Object> toBallerinaMessage(Message message) throws JMSException {
        BMap<BString, Object> result = ValueCreator.createRecordValue(getModule(), Constants.BMESSAGE_NAME);

        // Common properties - convert byte arrays to Ballerina arrays
        result.put(MESSAGE_ID_FIELD, ValueCreator.createArrayValue(safeBytes(message.getJMSMessageID())));
        result.put(CORRELATION_ID_FIELD, ValueCreator.createArrayValue(safeBytes(message.getJMSCorrelationID())));
        result.put(PRIORITY_FIELD, message.getJMSPriority());
        result.put(EXPIRY_FIELD, (int) message.getJMSExpiration()); // or TTL
        result.put(PERSISTENCE_FIELD, message.getJMSDeliveryMode());

        result.put(REPLY_TO_QUEUE_NAME_FIELD, (message.getJMSReplyTo() != null)
                ? StringUtils.fromString(message.getJMSReplyTo().toString()) : null);
        String userIdProperty = message.getStringProperty("JMSXUserID");
        result.put(MESSAGE_USERID, userIdProperty != null ? StringUtils.fromString(userIdProperty) : null);

        // JMS doesn't expose: format, putApplicationType, encoding, characterSet, accountingToken
        // You may need to pass them via custom properties or switch to MQMessage for that

        // Custom Properties
        BMap<BString, Object> props = ValueCreator.createMapValue();
        Enumeration<?> propNames = message.getPropertyNames();
        while (propNames.hasMoreElements()) {
            String name = (String) propNames.nextElement();
            Object value = message.getObjectProperty(name);
            props.put(StringUtils.fromString(name), value);
        }
        result.put(MESSAGE_PROPERTIES, props);

        // Payload - convert byte arrays to Ballerina arrays
        if (message instanceof TextMessage) {
            try {
                byte[] payload = ((TextMessage) message).getText().getBytes("UTF-8");
                result.put(MESSAGE_PAYLOAD, ValueCreator.createArrayValue(payload));
            } catch (UnsupportedEncodingException e) {
                throw createError(IBMMQ_ERROR, "Unsupported encoding for TextMessage payload: UTF-8", e);
            }
            result.put(FORMAT_FIELD, TEXT);

        } else if (message instanceof BytesMessage) {
            BytesMessage bytesMessage = (BytesMessage) message;
            byte[] payload = new byte[(int) bytesMessage.getBodyLength()];
            bytesMessage.readBytes(payload);
            result.put(MESSAGE_PAYLOAD, ValueCreator.createArrayValue(payload));
            result.put(FORMAT_FIELD, BINARY);

        } else {
            // fallback: try getBody
            byte[] fallback = null;
            try {
                fallback = message.getBody(String.class).getBytes("UTF-8");
            } catch (UnsupportedEncodingException e) {
                throw createError(IBMMQ_ERROR, "Unsupported encoding for message body: UTF-8", e);
            }
            result.put(MESSAGE_PAYLOAD, ValueCreator.createArrayValue(fallback));
            result.put(FORMAT_FIELD, UNKNOWN);
        }
        return result;
    }

    private static byte[] safeBytes(String value) {
        return value != null ? value.getBytes(java.nio.charset.StandardCharsets.UTF_8) : new byte[0];
    }
}

