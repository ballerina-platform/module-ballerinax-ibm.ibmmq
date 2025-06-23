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
import com.ibm.mq.headers.MQIIH;
import io.ballerina.runtime.api.Runtime;
import io.ballerina.runtime.api.creators.ValueCreator;
import io.ballerina.runtime.api.utils.StringUtils;
import io.ballerina.runtime.api.values.BMap;
import io.ballerina.runtime.api.values.BString;

import java.io.IOException;
import java.util.ArrayList;

import static io.ballerina.lib.ibm.ibmmq.Constants.CODED_CHARSET_ID_FIELD;
import static io.ballerina.lib.ibm.ibmmq.Constants.ENCODING_FIELD;
import static io.ballerina.lib.ibm.ibmmq.Constants.FLAGS_FIELD;
import static io.ballerina.lib.ibm.ibmmq.Constants.FORMAT_FIELD;
import static io.ballerina.lib.ibm.ibmmq.Constants.MQIIH_RECORD_NAME;
import static io.ballerina.lib.ibm.ibmmq.Constants.STRUC_ID_FIELD;
import static io.ballerina.lib.ibm.ibmmq.Constants.STRUC_LENGTH_FIELD;
import static io.ballerina.lib.ibm.ibmmq.Constants.VERSION_FIELD;
import static io.ballerina.lib.ibm.ibmmq.ModuleUtils.getModule;

/**
 * Header class with util methods for handling MQIIH headers.
 */
public class MQIIHHeader {

    private static final BString L_TERM_OVERRIDE_FIELD = StringUtils.fromString("lTermOverride");
    private static final BString MFS_MAP_NAME_FIELD = StringUtils.fromString("mfsMapName");
    private static final BString REPLY_TO_FORMAT_FIELD = StringUtils.fromString("replyToFormat");
    private static final BString AUTHENTICATOR_FIELD = StringUtils.fromString("authenticator");
    private static final BString TRAN_INSTANCE_ID_FIELD = StringUtils.fromString("tranInstanceId");
    private static final BString TRANS_STATE_FIELD = StringUtils.fromString("tranState");
    private static final BString COMMIT_MODE_FIELD = StringUtils.fromString("commitMode");
    private static final BString SECURITY_SCOPE_FIELD = StringUtils.fromString("securityScope");


    private MQIIHHeader() {}

    public static void decodeHeader(Runtime runtime, MQMessage msg, ArrayList<BMap<BString, Object>> headers)
            throws IOException {
        MQIIH mqiih = new MQIIH();
        int dataOffset = msg.getDataOffset();
        try {
            mqiih.read(msg);
            headers.add(getBHeaderFromMQIIH(mqiih));
            MQRFH2Header.decodeHeader(runtime, msg, headers);
        } catch (MQDataException e) {
            msg.seek(dataOffset);
            HeaderUtils.decodeUnSupportedHeaders(runtime, msg, headers);
        }
    }

    private static BMap<BString, Object> getBHeaderFromMQIIH(MQIIH mqiih) {
        BMap<BString, Object> header = ValueCreator.createRecordValue(getModule(), MQIIH_RECORD_NAME);
        header.put(FLAGS_FIELD, mqiih.getFlags());
        header.put(ENCODING_FIELD, mqiih.getEncoding());
        header.put(CODED_CHARSET_ID_FIELD, mqiih.getCodedCharSetId());
        header.put(FORMAT_FIELD, StringUtils.fromString(mqiih.getFormat()));
        header.put(STRUC_ID_FIELD, StringUtils.fromString(mqiih.getStrucId()));
        header.put(STRUC_LENGTH_FIELD, mqiih.getStrucLength());
        header.put(VERSION_FIELD, mqiih.getVersion());
        header.put(L_TERM_OVERRIDE_FIELD, StringUtils.fromString(mqiih.getLTermOverride()));
        header.put(MFS_MAP_NAME_FIELD, StringUtils.fromString(mqiih.getMFSMapName()));
        header.put(REPLY_TO_FORMAT_FIELD, StringUtils.fromString(mqiih.getReplyToFormat()));
        header.put(AUTHENTICATOR_FIELD, StringUtils.fromString(mqiih.getAuthenticator()));
        header.put(TRAN_INSTANCE_ID_FIELD, ValueCreator.createArrayValue(mqiih.getTranInstanceId()));
        header.put(TRANS_STATE_FIELD, StringUtils.fromString(String.valueOf(mqiih.getTranState())));
        header.put(COMMIT_MODE_FIELD, StringUtils.fromString(String.valueOf(mqiih.getCommitMode())));
        header.put(SECURITY_SCOPE_FIELD, StringUtils.fromString(String.valueOf(mqiih.getSecurityScope())));
        return header;
    }

    public static Object createMQIIHHeaderFromBHeader(BMap<BString, Object> bHeader) {
        MQIIH header = new MQIIH();
        header.setFlags(bHeader.getIntValue(FLAGS_FIELD).intValue());
        header.setFormat(bHeader.getStringValue(FORMAT_FIELD).getValue());
        header.setCodedCharSetId(bHeader.getIntValue(CODED_CHARSET_ID_FIELD).intValue());
        header.setEncoding(bHeader.getIntValue(ENCODING_FIELD).intValue());
        header.setLTermOverride(bHeader.getStringValue(L_TERM_OVERRIDE_FIELD).getValue());
        header.setMFSMapName(bHeader.getStringValue(MFS_MAP_NAME_FIELD).getValue());
        header.setReplyToFormat(bHeader.getStringValue(REPLY_TO_FORMAT_FIELD).getValue());
        header.setAuthenticator(bHeader.getStringValue(AUTHENTICATOR_FIELD).getValue());
        header.setTranInstanceId(bHeader.getArrayValue(TRAN_INSTANCE_ID_FIELD).getByteArray());
        header.setTranState(bHeader.getStringValue(TRANS_STATE_FIELD).getValue().charAt(0));
        header.setCommitMode(bHeader.getStringValue(COMMIT_MODE_FIELD).getValue().charAt(0));
        header.setSecurityScope(bHeader.getStringValue(SECURITY_SCOPE_FIELD).getValue().charAt(0));
        return header;
    }
}
