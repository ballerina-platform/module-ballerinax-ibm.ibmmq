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
import static io.ballerina.lib.ibm.ibmmq.Constants.CODED_CHARSET_ID_FIELD;
import static io.ballerina.lib.ibm.ibmmq.Constants.ENCODING_FIELD;
import static io.ballerina.lib.ibm.ibmmq.Constants.FLAGS_FIELD;
import static io.ballerina.lib.ibm.ibmmq.Constants.FORMAT_FIELD;
import static io.ballerina.lib.ibm.ibmmq.Constants.IBMMQ_ERROR;
import static io.ballerina.lib.ibm.ibmmq.Constants.MQRFH_RECORD_NAME;
import static io.ballerina.lib.ibm.ibmmq.Constants.STRUC_ID_FIELD;
import static io.ballerina.lib.ibm.ibmmq.Constants.STRUC_LENGTH_FIELD;
import static io.ballerina.lib.ibm.ibmmq.Constants.VERSION_FIELD;
import static io.ballerina.lib.ibm.ibmmq.ModuleUtils.getModule;

/**
 * Header class with util methods for handling MQRFH headers.
 */
public class MQRFHHeader {

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
            MQCIHHeader.decodeHeader(runtime, msg, headers);
        }
    }

    public static MQRFH createMQRFHHeaderFromBHeader(BMap<BString, Object> bHeader) {
        MQRFH header = new MQRFH();
        header.setFlags(bHeader.getIntValue(FLAGS_FIELD).intValue());
        if (bHeader.getIntValue(ENCODING_FIELD).intValue() != 0) {
            header.setEncoding(bHeader.getIntValue(ENCODING_FIELD).intValue());
        }
        header.setCodedCharSetId(bHeader.getIntValue(CODED_CHARSET_ID_FIELD).intValue());
        header.setFormat(bHeader.getStringValue(FORMAT_FIELD).getValue());
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
        header.put(ENCODING_FIELD, mqrfh.getEncoding());
        header.put(STRUC_ID_FIELD, StringUtils.fromString(mqrfh.getStrucId()));
        header.put(STRUC_LENGTH_FIELD, mqrfh.getStrucLength());
        header.put(VERSION_FIELD, mqrfh.getVersion());
        header.put(CODED_CHARSET_ID_FIELD, mqrfh.getCodedCharSetId());
        header.put(FORMAT_FIELD, StringUtils.fromString(mqrfh.getFormat()));
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
