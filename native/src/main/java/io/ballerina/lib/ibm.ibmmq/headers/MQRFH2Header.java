package io.ballerina.lib.ibm.ibmmq.headers;

import com.ibm.mq.MQMessage;
import com.ibm.mq.headers.MQDataException;
import com.ibm.mq.headers.MQRFH2;
import io.ballerina.lib.ibm.ibmmq.HeaderFieldValuesCallback;
import io.ballerina.runtime.api.PredefinedTypes;
import io.ballerina.runtime.api.Runtime;
import io.ballerina.runtime.api.creators.TypeCreator;
import io.ballerina.runtime.api.creators.ValueCreator;
import io.ballerina.runtime.api.utils.StringUtils;
import io.ballerina.runtime.api.values.BArray;
import io.ballerina.runtime.api.values.BIterator;
import io.ballerina.runtime.api.values.BMap;
import io.ballerina.runtime.api.values.BObject;
import io.ballerina.runtime.api.values.BString;
import io.ballerina.runtime.api.values.BTable;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;

import static io.ballerina.lib.ibm.ibmmq.CommonUtils.createError;
import static io.ballerina.lib.ibm.ibmmq.Constants.FLAGS_FIELD;
import static io.ballerina.lib.ibm.ibmmq.Constants.IBMMQ_ERROR;
import static io.ballerina.lib.ibm.ibmmq.Constants.MQRFH2FIELD_RECORD_NAME;
import static io.ballerina.lib.ibm.ibmmq.Constants.STRUC_ID_FIELD;
import static io.ballerina.lib.ibm.ibmmq.Constants.STRUC_LENGTH_FIELD;
import static io.ballerina.lib.ibm.ibmmq.Constants.VERSION_FIELD;
import static io.ballerina.lib.ibm.ibmmq.ModuleUtils.getModule;

public class MQRFH2Header {

    private static final BString FIELD_VALUES_FIELD = StringUtils.fromString("fieldValues");
    private static final BString FOLDER_STRINGS_FIELD = StringUtils.fromString("folderStrings");
    private static final BString NAME_VALUE_CCSID_FIELD = StringUtils.fromString("nameValueCCSID");
    private static final BString NAME_VALUE_DATA_FIELD = StringUtils.fromString("nameValueData");
    private static final BString FOLDER_FIELD = StringUtils.fromString("folder");
    private static final BString FIELD_FIELD = StringUtils.fromString("field");
    private static final BString VALUE_FIELD = StringUtils.fromString("value");
    private static final String MQRFH2_RECORD_NAME = "MQRFH2";
    private static final String NATIVE_UTILS_OBJECT_NAME = "NativeUtils";
    private static final String ADD_FIELDS_TO_TABLE_FUNCTION_NAME = "addMQRFH2FieldsToTable";

    private MQRFH2Header() {}

    public static void decodeHeader(Runtime runtime, MQMessage msg, ArrayList<BMap<BString, Object>> headers)
            throws IOException {
        MQRFH2 mqrfh2 = new MQRFH2();
        int dataOffset = msg.getDataOffset();
        try {
            mqrfh2.read(msg);
            headers.add(getBHeaderFromMQRFH2(runtime, mqrfh2));
            MQRFH2Header.decodeHeader(runtime, msg, headers);
        } catch (MQDataException e) {
            msg.seek(dataOffset);
            MQRFHHeader.decodeHeader(runtime, msg, headers);
        }
    }

    public static MQRFH2 createMQRFH2HeaderFromBHeader(BMap<BString, Object> bHeader) {
        MQRFH2 header = new MQRFH2();
        header.setFlags(bHeader.getIntValue(FLAGS_FIELD).intValue());
        BArray folderStringsArray = bHeader.getArrayValue(FOLDER_STRINGS_FIELD);
        try {
            header.setFolderStrings(folderStringsArray.getStringArray());
        } catch (IOException e) {
            throw createError(IBMMQ_ERROR, String
                    .format("Error occurred while setting folder string to MQRFH2 header: %s", e.getMessage()), e);
        }
        header.setNameValueCCSID(bHeader.getIntValue(NAME_VALUE_CCSID_FIELD).intValue());
        header.setNameValueData(bHeader.getArrayValue(NAME_VALUE_DATA_FIELD).getBytes());
        BTable fieldTable = (BTable) bHeader.get(FIELD_VALUES_FIELD);
        BIterator fieldTableIterator = fieldTable.getIterator();
        while (fieldTableIterator.hasNext()) {
            setFieldValueToMQRFH2Header(fieldTableIterator, header);
        }
        return header;
    }

    private static void setFieldValueToMQRFH2Header(BIterator fieldTableIterator, MQRFH2 header) {
        BMap<BString, Object> bField = (BMap<BString, Object>) ((BArray) fieldTableIterator.next()).get(1);
        String folder = bField.getStringValue(FOLDER_FIELD).getValue();
        String field = bField.getStringValue(FIELD_FIELD).getValue();
        Object value = bField.get(VALUE_FIELD);
        try {
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
        } catch (IOException e) {
            throw createError(IBMMQ_ERROR, String
                    .format("Error occurred while setting field values to MQRFH2 header: %s", e.getMessage()), e);
        }
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
}
