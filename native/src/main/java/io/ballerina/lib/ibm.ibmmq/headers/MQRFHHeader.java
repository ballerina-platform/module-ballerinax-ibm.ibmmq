package io.ballerina.lib.ibm.ibmmq.headers;

import com.ibm.mq.MQMessage;
import com.ibm.mq.headers.MQDataException;
import com.ibm.mq.headers.MQRFH;
import io.ballerina.runtime.api.Runtime;
import io.ballerina.runtime.api.creators.ValueCreator;
import io.ballerina.runtime.api.utils.StringUtils;
import io.ballerina.runtime.api.values.BMap;
import io.ballerina.runtime.api.values.BString;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static io.ballerina.lib.ibm.ibmmq.CommonUtils.createError;
import static io.ballerina.lib.ibm.ibmmq.Constants.FLAGS_FIELD;
import static io.ballerina.lib.ibm.ibmmq.Constants.IBMMQ_ERROR;
import static io.ballerina.lib.ibm.ibmmq.Constants.STRUC_ID_FIELD;
import static io.ballerina.lib.ibm.ibmmq.Constants.STRUC_LENGTH_FIELD;
import static io.ballerina.lib.ibm.ibmmq.Constants.VERSION_FIELD;
import static io.ballerina.lib.ibm.ibmmq.ModuleUtils.getModule;

/**
 * Header class with util methods for handling MQRFH headers.
 */
public class MQRFHHeader {

    private static final String MQRFH_RECORD_NAME = "MQRFH";
    private static final BString NAME_VALUE_PAIRS_FIELD = StringUtils.fromString("nameValuePairs");

    private MQRFHHeader() {}

    public static void decodeHeader(Runtime runtime, MQMessage msg, ArrayList<BMap<BString, Object>> headers)
            throws IOException {
        MQRFH mqrfh = new MQRFH();
        int dataOffset = msg.getDataOffset();
        try {
            mqrfh.read(msg);
            headers.add(getBHeaderFromMQRFH(mqrfh));
            MQRFH2Header.decodeHeader(runtime, msg, headers);
        } catch (MQDataException e) {
            msg.seek(dataOffset);
            MQCHIHHeader.decodeHeader(runtime, msg, headers);
        }
    }

    public static MQRFH createMQRFHHeaderFromBHeader(BMap<BString, Object> bHeader) {
        MQRFH header = new MQRFH();
        header.setFlags(bHeader.getIntValue(FLAGS_FIELD).intValue());
        BMap<BString, Object> nameValuePairsMap = (BMap<BString, Object>) bHeader.getMapValue(NAME_VALUE_PAIRS_FIELD);
        for (BString key : nameValuePairsMap.getKeys()) {
            try {
                header.addNameValuePair(key.getValue(), nameValuePairsMap.getStringValue(key).getValue());
            } catch (IOException e) {
                throw createError(IBMMQ_ERROR, String
                        .format("Error occurred while adding key pair values to MQRFH header: %s", e.getMessage()), e);
            }
        }
        return header;
    }

    private static BMap<BString, Object> getBHeaderFromMQRFH(MQRFH mqrfh) {
        BMap<BString, Object> header = ValueCreator.createRecordValue(getModule(), MQRFH_RECORD_NAME);
        header.put(FLAGS_FIELD, mqrfh.getFlags());
        header.put(STRUC_ID_FIELD, StringUtils.fromString(mqrfh.getStrucId()));
        header.put(STRUC_LENGTH_FIELD, mqrfh.getStrucLength());
        header.put(VERSION_FIELD, mqrfh.getVersion());
        header.put(NAME_VALUE_PAIRS_FIELD, getBNameValuePairsFromMQRFH(mqrfh));
        return header;
    }

    private static BMap<BString, Object> getBNameValuePairsFromMQRFH(MQRFH mqrfh) {
        BMap<BString, Object> nameValuePairs = ValueCreator.createMapValue();
        try {
            for (MQRFH.NameValuePair nameValuePair : (List<MQRFH.NameValuePair>) mqrfh.getNameValuePairs()) {
                nameValuePairs.put(StringUtils.fromString(nameValuePair.getName()),
                        StringUtils.fromString(nameValuePair.getValue()));
            }
        } catch (IOException e) {
            throw createError(IBMMQ_ERROR, String
                    .format("Error occurred while adding name value pairs to MQRFH header: %s", e.getMessage()), e);
        }
        return nameValuePairs;
    }
}
