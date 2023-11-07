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
import com.ibm.mq.headers.MQCIH;
import com.ibm.mq.headers.MQDH;
import com.ibm.mq.headers.MQDLH;
import com.ibm.mq.headers.MQDataException;
import com.ibm.mq.headers.MQHeaderList;
import com.ibm.mq.headers.MQIIH;
import com.ibm.mq.headers.MQRFH;
import com.ibm.mq.headers.MQRFH2;
import com.ibm.mq.headers.MQRMH;
import com.ibm.mq.headers.MQSAPH;
import com.ibm.mq.headers.MQTM;
import com.ibm.mq.headers.MQWIH;
import com.ibm.mq.headers.MQXQH;
import com.ibm.mq.headers.pcf.MQEPH;
import io.ballerina.runtime.api.PredefinedTypes;
import io.ballerina.runtime.api.Runtime;
import io.ballerina.runtime.api.creators.ErrorCreator;
import io.ballerina.runtime.api.creators.TypeCreator;
import io.ballerina.runtime.api.creators.ValueCreator;
import io.ballerina.runtime.api.utils.StringUtils;
import io.ballerina.runtime.api.values.BArray;
import io.ballerina.runtime.api.values.BError;
import io.ballerina.runtime.api.values.BIterator;
import io.ballerina.runtime.api.values.BMap;
import io.ballerina.runtime.api.values.BObject;
import io.ballerina.runtime.api.values.BString;
import io.ballerina.runtime.api.values.BTable;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;

import static io.ballerina.lib.ibm.ibmmq.Constants.IBMMQ_ERROR;
import static io.ballerina.lib.ibm.ibmmq.Constants.MQRFH2FIELD_RECORD_NAME;
import static io.ballerina.lib.ibm.ibmmq.ModuleUtils.getModule;

/**
 * {@code CommonUtils} contains the common utility functions for the Ballerina IBM MQ connector.
 */
public class CommonUtils {

    private static final String ERROR_DETAILS = "ErrorDetails";
    private static final BString ERROR_REASON_CODE = StringUtils.fromString("reasonCode");
    private static final BString ERROR_ERROR_CODE = StringUtils.fromString("errorCode");
    private static final BString ERROR_COMPLETION_CODE = StringUtils.fromString("completionCode");
    private static final BString MESSAGE_PAYLOAD = StringUtils.fromString("payload");
    private static final BString MESSAGE_PROPERTIES = StringUtils.fromString("properties");
    private static final BString MESSAGE_HEADERS = StringUtils.fromString("headers");
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
    private static final BString WAIT_INTERVAL = StringUtils.fromString("waitInterval");
    private static final BString OPTIONS = StringUtils.fromString("options");
    private static final BString FIELD_VALUES_FIELD = StringUtils.fromString("fieldValues");
    private static final BString FOLDER_STRINGS_FIELD = StringUtils.fromString("folderStrings");
    private static final BString FLAGS_FIELD = StringUtils.fromString("flags");
    private static final BString VERSION_FIELD = StringUtils.fromString("version");
    private static final BString NAME_VALUE_CCSID_FIELD = StringUtils.fromString("nameValueCCSID");
    private static final BString NAME_VALUE_DATA_FIELD = StringUtils.fromString("nameValueData");
    private static final BString STRUC_ID_FIELD = StringUtils.fromString("strucId");
    private static final BString STRUC_LENGTH_FIELD = StringUtils.fromString("strucLength");
    private static final BString FOLDER_FIELD = StringUtils.fromString("folder");
    private static final BString FIELD_FIELD = StringUtils.fromString("field");
    private static final BString VALUE_FIELD = StringUtils.fromString("value");
    private static final String MQRFH2_RECORD_NAME = "MQRFH2";
    private static final String NATIVE_UTILS_OBJECT_NAME = "NativeUtils";
    private static final String ADD_FIELDS_TO_TABLE_FUNCTION_NAME = "addMQRFH2FieldsToTable";

    private static final MQPropertyDescriptor defaultPropertyDescriptor = new MQPropertyDescriptor();

    public static MQMessage getMqMessageFromBMessage(BMap<BString, Object> bMessage) {
        MQMessage mqMessage = new MQMessage();
        BMap<BString, Object> properties = (BMap<BString, Object>) bMessage.getMapValue(MESSAGE_PROPERTIES);
        if (Objects.nonNull(properties)) {
            populateMQProperties(properties, mqMessage);
        }
        BArray headers = bMessage.getArrayValue(MESSAGE_HEADERS);
        if (Objects.nonNull(headers)) {
            try {
                populateMQHeaders(headers, mqMessage);
            } catch (IOException e) {
                throw createError(IBMMQ_ERROR,
                        String.format("Error occurred while writing headers: %s", e.getMessage()), e);
            }
        }
        byte[] payload = bMessage.getArrayValue(MESSAGE_PAYLOAD).getBytes();
        try {
            mqMessage.write(payload);
        } catch (IOException e) {
            throw createError(IBMMQ_ERROR,
                    String.format("Error occurred while populating payload: %s", e.getMessage()), e);
        }
        return mqMessage;
    }

    public static BMap<BString, Object> getBMessageFromMQMessage(Runtime runtime, MQMessage mqMessage) {
        BMap<BString, Object> bMessage = ValueCreator.createRecordValue(getModule(), BMESSAGE_NAME);
        try {
            bMessage.put(MESSAGE_HEADERS, getBHeaders(runtime, mqMessage));
            bMessage.put(MESSAGE_PROPERTY, getBProperties(mqMessage));
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

    private static void handlePropertyValue(BMap<BString, Object> properties, MQMessage mqMessage, BString key)
            throws MQException {
        BMap<BString, Object> property = (BMap<BString, Object>) properties.getMapValue(key);
        MQPropertyDescriptor propertyDescriptor = defaultPropertyDescriptor;
        if (property.containsKey(PROPERTY_DESCRIPTOR)) {
            propertyDescriptor = getMQPropertyDescriptor(properties.getMapValue(PROPERTY_DESCRIPTOR));
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

    private static void populateMQHeaders(BArray bHeaders, MQMessage mqMessage) throws IOException {
        MQHeaderList headerList = new MQHeaderList();
        for (int i = 0; i < bHeaders.size(); i++) {
            BMap<BString, Object> bHeader = (BMap) bHeaders.get(i);
            headerList.add(createMQRFH2HeaderFromBHeader(bHeader));
        }
        try {
            headerList.write(mqMessage);
        } catch (IOException e) {
            throw createError(IBMMQ_ERROR,
                    String.format("Error occurred while putting a message to the topic: %s", e.getMessage()), e);
        }
    }

    private static MQRFH2 createMQRFH2HeaderFromBHeader(BMap<BString, Object> bHeader) throws IOException {
        MQRFH2 header = new MQRFH2();
        header.setFlags(bHeader.getIntValue(FLAGS_FIELD).intValue());
        BArray folderStringsArray = bHeader.getArrayValue(FOLDER_STRINGS_FIELD);
        header.setFolderStrings(folderStringsArray.getStringArray());
        header.setNameValueCCSID(bHeader.getIntValue(NAME_VALUE_CCSID_FIELD).intValue());
        header.setNameValueData(bHeader.getArrayValue(NAME_VALUE_DATA_FIELD).getBytes());
        BTable fieldTable = (BTable) bHeader.get(FIELD_VALUES_FIELD);
        BIterator fieldTableIterator = fieldTable.getIterator();
        while (fieldTableIterator.hasNext()) {
            setFieldValueToMQRFH2Header(fieldTableIterator, header);
        }
        return header;
    }

    private static void setFieldValueToMQRFH2Header(BIterator fieldTableIterator, MQRFH2 header) throws IOException {
        BMap<BString, Object> bField = (BMap<BString, Object>) ((BArray) fieldTableIterator.next()).get(1);
        String folder = bField.getStringValue(FOLDER_FIELD).getValue();
        String field = bField.getStringValue(FIELD_FIELD).getValue();
        Object value = bField.get(VALUE_FIELD);
        if (value instanceof Long longValue) {
            header.setLongFieldValue(folder, field, longValue.intValue());
        } else if (value instanceof Integer intValue) {
            header.setIntFieldValue(folder, field, intValue);
        } else if (value instanceof Boolean booleanValue) {
            header.setFieldValue(folder, field, booleanValue);
        } else if (value instanceof Byte byteValue) {
            header.setByteFieldValue(folder, field, byteValue);
        } else if (value instanceof byte[] bytesValue) {
            header.setFieldValue(folder, field, bytesValue);
        } else if (value instanceof Float floatValue) {
            header.setFloatFieldValue(folder, field, floatValue);
        } else if (value instanceof Double doubleValue) {
            header.setFloatFieldValue(folder, field, doubleValue.floatValue());
        } else if (value instanceof BString stringValue) {
            header.setFieldValue(folder, field, stringValue.getValue());
        } else {
            header.setFieldValue(folder, field, value);
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

    public static MQGetMessageOptions getGetMessageOptions(BMap<BString, Object> bOptions) {
        int waitInterval = bOptions.getIntValue(WAIT_INTERVAL).intValue();
        int options = bOptions.getIntValue(OPTIONS).intValue();
        MQGetMessageOptions getMessageOptions = new MQGetMessageOptions();
        getMessageOptions.waitInterval = waitInterval * 1000;
        getMessageOptions.options = options;
        return getMessageOptions;
    }

    private static Object getBHeaders(Runtime runtime, MQMessage mqMessage) {
        ArrayList<BMap<BString, Object>> bHeaders = new ArrayList<>();
        try {
            readHeadersFromMQMessage(runtime, bHeaders, HeaderType.MQRFH2, mqMessage);
        } catch (IOException e) {
            throw createError(IBMMQ_ERROR,
                    String.format("Error occurred while reading headers: %s", e.getMessage()), e);
        }
        if (bHeaders.isEmpty()) {
            return null;
        }
        BArray headerArray = ValueCreator.createArrayValue(TypeCreator.createArrayType(bHeaders.get(0).getType()));
        for (BMap<BString, Object> header : bHeaders) {
            headerArray.append(header);
        }
        return headerArray;
    }

    private static void readHeadersFromMQMessage(Runtime runtime, ArrayList<BMap<BString, Object>> headers,
                                                 HeaderType type, MQMessage msg) throws IOException {
        int dataOffset = msg.getDataOffset();
        switch (type) {
            case MQRFH2: {
                MQRFH2 mqrfh2 = new MQRFH2();
                try {
                    mqrfh2.read(msg);
                    headers.add(getBHeaderFromMQRFH2(runtime, mqrfh2));
                    break;
                } catch (MQDataException e) {
                    msg.seek(dataOffset);
                }
            }
            /*-fallthrough*/
            case MQDLH: {
                MQDLH dlh = new MQDLH();
                try {
                    // Only MQRFH2 headers is supported at the moment. Other headers are read here
                    // to move the cursor to the payload value.
                    dlh.read(msg);
                    break;
                } catch (MQDataException e) {
                    msg.seek(dataOffset);
                }
            }
            /*-fallthrough*/
            case MQRFH: {
                MQRFH mqrfh = new MQRFH();
                try {
                    mqrfh.read(msg);
                    break;
                } catch (MQDataException e) {
                    msg.seek(dataOffset);
                }
            }
            /*-fallthrough*/
            case MQCIH: {
                MQCIH mqcih = new MQCIH();
                try {
                    mqcih.read(msg);
                    break;
                } catch (MQDataException e) {
                    msg.seek(dataOffset);
                }
            }
            /*-fallthrough*/
            case MQIIH: {
                MQIIH mqiih = new MQIIH();
                try {
                    mqiih.read(msg);
                    break;
                } catch (MQDataException e) {
                    msg.seek(dataOffset);
                }
            }
            /*-fallthrough*/
            case MQTM: {
                MQTM mqtm = new MQTM();
                try {
                    mqtm.read(msg);
                    break;
                } catch (MQDataException e) {
                    msg.seek(dataOffset);
                }
            }
            /*-fallthrough*/
            case MQRMH: {
                MQRMH mqrmh = new MQRMH();
                try {
                    mqrmh.read(msg);
                    break;
                } catch (MQDataException e) {
                    msg.seek(dataOffset);
                }
            }
            /*-fallthrough*/
            case MQSAPH: {
                MQSAPH mqsaph = new MQSAPH();
                try {
                    mqsaph.read(msg);
                    break;
                } catch (MQDataException e) {
                    msg.seek(dataOffset);
                }
            }
            /*-fallthrough*/
            case MQWIH: {
                MQWIH mqwih = new MQWIH();
                try {
                    mqwih.read(msg);
                    break;
                } catch (MQDataException e) {
                    msg.seek(dataOffset);
                }
            }
            /*-fallthrough*/
            case MQXQH: {
                MQXQH mqxqh = new MQXQH();
                try {
                    mqxqh.read(msg);
                    break;
                } catch (MQDataException e) {
                    msg.seek(dataOffset);
                }
            }
            /*-fallthrough*/
            case MQDH: {
                MQDH mqdh = new MQDH();
                try {
                    mqdh.read(msg);
                    break;
                } catch (MQDataException e) {
                    msg.seek(dataOffset);
                }
            }
            /*-fallthrough*/
            case MQEPH: {
                MQEPH mqeph = new MQEPH();
                try {
                    mqeph.read(msg);
                    break;
                } catch (MQDataException e) {
                    msg.seek(dataOffset);
                    return;
                }
            }
        }
        readHeadersFromMQMessage(runtime, headers, HeaderType.MQRFH2, msg);
    }

    private static BMap<BString, Object> getBHeaderFromMQRFH2(Runtime runtime, MQRFH2 mqrfh2) throws IOException {
        BMap<BString, Object> header = ValueCreator.createRecordValue(getModule(), MQRFH2_RECORD_NAME);
        header.put(FLAGS_FIELD, mqrfh2.getFlags());
        BArray folderStringArray = ValueCreator.createArrayValue(TypeCreator
                .createArrayType(PredefinedTypes.TYPE_STRING));
        String[] folderStrings = mqrfh2.getFolderStrings();
        for (String folderString : folderStrings) {
            folderStringArray.append(StringUtils.fromString(folderString));
        }
        header.put(FOLDER_STRINGS_FIELD, folderStringArray);
        header.put(NAME_VALUE_CCSID_FIELD, mqrfh2.getNameValueCCSID());
        header.put(NAME_VALUE_DATA_FIELD, ValueCreator.createArrayValue(mqrfh2.getNameValueData()));
        header.put(STRUC_ID_FIELD, StringUtils.fromString(mqrfh2.getStrucId()));
        header.put(STRUC_LENGTH_FIELD, mqrfh2.getStrucLength());
        header.put(VERSION_FIELD, mqrfh2.getVersion());
        BTable fieldValuesTable = getBHeaderFieldValuesFromMQMessage(runtime, mqrfh2);
        header.put(FIELD_VALUES_FIELD, fieldValuesTable);
        return header;
    }

    private static BTable getBHeaderFieldValuesFromMQMessage(Runtime runtime, MQRFH2 mqrfh2) throws IOException {
        BArray fieldArray = ValueCreator.createArrayValue(TypeCreator.createArrayType(TypeCreator
                .createRecordType(MQRFH2FIELD_RECORD_NAME, getModule(), 0, false, 0)));
        MQRFH2.Element[] folders = mqrfh2.getFolders();
        int i = 0;
        for (MQRFH2.Element folder : folders) {
            MQRFH2.Element[] children = folder.getChildren();
            for (MQRFH2.Element child : children) {
                BMap<BString, Object> field = ValueCreator.createRecordValue(getModule(), MQRFH2FIELD_RECORD_NAME);
                field.put(FOLDER_FIELD, StringUtils.fromString(folder.getName()));
                field.put(FIELD_FIELD, StringUtils.fromString(child.getName()));
                field.put(VALUE_FIELD, getBValueForMQObjectValue(child.getValue()));
                fieldArray.add(i, field);
                i = i + 1;
            }
        }
        BObject nativeUtilsObject = ValueCreator.createObjectValue(getModule(), NATIVE_UTILS_OBJECT_NAME);
        CountDownLatch latch = new CountDownLatch(1);
        HeaderFieldValuesCallback headerFieldValuesCallback = new HeaderFieldValuesCallback(latch);
        runtime.invokeMethodAsyncConcurrently(nativeUtilsObject, ADD_FIELDS_TO_TABLE_FUNCTION_NAME, null,
                null, headerFieldValuesCallback, null, PredefinedTypes.TYPE_ANY, fieldArray, true);
        try {
            latch.await();
        } catch (InterruptedException e) {
            throw createError(IBMMQ_ERROR,
                    String.format("Error occurred while adding MQRFH2 fields: %s", e.getMessage()), e);
        }
        return headerFieldValuesCallback.getHeaderValueTable();
    }

    private static Object getBValueForMQObjectValue(Object value) {
        if (value instanceof Integer intValue) {
            return intValue.longValue();
        } else if (value instanceof String stringValue) {
            return StringUtils.fromString(stringValue);
        } else if (value instanceof byte[] bytesValue) {
            return ValueCreator.createArrayValue(bytesValue);
        } else {
            return value;
        }
    }

    public static BError createError(String errorType, String message, Throwable throwable) {
        BError cause = ErrorCreator.createError(throwable);
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
