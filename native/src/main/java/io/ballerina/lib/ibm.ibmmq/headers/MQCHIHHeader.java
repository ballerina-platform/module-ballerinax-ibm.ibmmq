package io.ballerina.lib.ibm.ibmmq.headers;

import com.ibm.mq.MQMessage;
import com.ibm.mq.headers.MQCIH;
import com.ibm.mq.headers.MQDataException;
import io.ballerina.runtime.api.Runtime;
import io.ballerina.runtime.api.creators.ValueCreator;
import io.ballerina.runtime.api.utils.StringUtils;
import io.ballerina.runtime.api.values.BMap;
import io.ballerina.runtime.api.values.BString;

import java.io.IOException;
import java.util.ArrayList;

import static io.ballerina.lib.ibm.ibmmq.Constants.FLAGS_FIELD;
import static io.ballerina.lib.ibm.ibmmq.Constants.STRUC_ID_FIELD;
import static io.ballerina.lib.ibm.ibmmq.Constants.STRUC_LENGTH_FIELD;
import static io.ballerina.lib.ibm.ibmmq.Constants.VERSION_FIELD;
import static io.ballerina.lib.ibm.ibmmq.ModuleUtils.getModule;

/**
 * Header class with util methods for handling MQCHIH headers.
 */
public class MQCHIHHeader {

    private static final String MQCIH_RECORD_NAME = "MQCIH";
    private static final BString RETURN_CODE_FIELD = StringUtils.fromString("returnCode");
    private static final BString COMP_CODE_FIELD = StringUtils.fromString("compCode");
    private static final BString REASON_FIELD = StringUtils.fromString("reason");
    private static final BString UOW_CONTROL_FIELD = StringUtils.fromString("UOWControl");
    private static final BString WAIT_INTERVAL_FIELD = StringUtils.fromString("waitInterval");
    private static final BString LINK_TYPE_FIELD = StringUtils.fromString("linkType");
    private static final BString FACILITY_KEEP_TIME_FIELD = StringUtils.fromString("facilityKeepTime");
    private static final BString ADS_DECSRIPTOR_FIELD = StringUtils.fromString("ADSDescriptor");
    private static final BString CONVERSATIONAL_TASK_FIELD = StringUtils.fromString("conversationalTask");
    private static final BString TASK_END_STATUS_FIELD = StringUtils.fromString("taskEndStatus");
    private static final BString FACITLIY_FIELD = StringUtils.fromString("facility");
    private static final BString FUNCTION_FIELD = StringUtils.fromString("function");
    private static final BString ABEND_CODE_FIELD = StringUtils.fromString("abendCode");
    private static final BString AUTHENTICATOR_FIELD = StringUtils.fromString("authenticator");
    private static final BString RESERVED1_FIELD = StringUtils.fromString("reserved1");
    private static final BString RESERVED2_FIELD = StringUtils.fromString("reserved2");
    private static final BString RESERVED3_FIELD = StringUtils.fromString("reserved3");
    private static final BString REPLY_TO_FORMAT_FIELD = StringUtils.fromString("replyToFormat");
    private static final BString REMOTE_SYS_ID_FIELD = StringUtils.fromString("remoteSysId");
    private static final BString REMOTE_TRANS_ID_FIELD = StringUtils.fromString("remoteTransId");
    private static final BString TRANSACTION_ID_FIELD = StringUtils.fromString("transactionId");
    private static final BString FACILITY_LIKE_FIELD = StringUtils.fromString("facilityLike");
    private static final BString ATTENTION_ID_FIELD = StringUtils.fromString("attentionId");
    private static final BString START_CODE_FIELD = StringUtils.fromString("startCode");
    private static final BString CANCEL_CODE_FIELD = StringUtils.fromString("cancelCode");
    private static final BString NEXT_TRANSATION_ID_FIELD = StringUtils.fromString("nextTransactionId");
    private static final BString INPUT_ITEM_FIELD = StringUtils.fromString("inputItem");


    private MQCHIHHeader() {}

    public static void decodeHeader(Runtime runtime, MQMessage msg, ArrayList<BMap<BString, Object>> headers)
            throws IOException {
        MQCIH mqcih = new MQCIH();
        int dataOffset = msg.getDataOffset();
        try {
            mqcih.read(msg);
            headers.add(getBHeaderFromMQCIH(mqcih));
            MQRFH2Header.decodeHeader(runtime, msg, headers);
        } catch (MQDataException e) {
            msg.seek(dataOffset);
        }
    }

    private static BMap<BString, Object> getBHeaderFromMQCIH(MQCIH mqcih) {
        BMap<BString, Object> header = ValueCreator.createRecordValue(getModule(), MQCIH_RECORD_NAME);
        header.put(FLAGS_FIELD, mqcih.getFlags());
        header.put(STRUC_ID_FIELD, StringUtils.fromString(mqcih.getStrucId()));
        header.put(STRUC_LENGTH_FIELD, mqcih.getStrucLength());
        header.put(VERSION_FIELD, mqcih.getVersion());
        header.put(RETURN_CODE_FIELD, mqcih.getReturnCode());
        header.put(COMP_CODE_FIELD, mqcih.getCompCode());
        header.put(REASON_FIELD, mqcih.getReason());
        header.put(UOW_CONTROL_FIELD, mqcih.getUOWControl());
        header.put(WAIT_INTERVAL_FIELD, mqcih.getGetWaitInterval());
        header.put(LINK_TYPE_FIELD, mqcih.getLinkType());
        header.put(FACILITY_KEEP_TIME_FIELD, mqcih.getFacilityKeepTime());
        header.put(ADS_DECSRIPTOR_FIELD, mqcih.getADSDescriptor());
        header.put(CONVERSATIONAL_TASK_FIELD, mqcih.getConversationalTask());
        header.put(TASK_END_STATUS_FIELD, mqcih.getTaskEndStatus());
        header.put(FACITLIY_FIELD, ValueCreator.createArrayValue(mqcih.getFacility()));
        header.put(FUNCTION_FIELD, StringUtils.fromString(mqcih.getFunction()));
        header.put(ABEND_CODE_FIELD, StringUtils.fromString(mqcih.getAbendCode()));
        header.put(AUTHENTICATOR_FIELD, StringUtils.fromString(mqcih.getAuthenticator()));
        header.put(RESERVED1_FIELD, StringUtils.fromString(mqcih.getReserved1()));
        header.put(RESERVED2_FIELD, StringUtils.fromString(mqcih.getReserved2()));
        header.put(RESERVED3_FIELD, StringUtils.fromString(mqcih.getReserved3()));
        header.put(REPLY_TO_FORMAT_FIELD, StringUtils.fromString(mqcih.getReplyToFormat()));
        header.put(REMOTE_SYS_ID_FIELD, StringUtils.fromString(mqcih.getRemoteSysId()));
        header.put(REMOTE_TRANS_ID_FIELD, StringUtils.fromString(mqcih.getRemoteTransId()));
        header.put(TRANSACTION_ID_FIELD, StringUtils.fromString(mqcih.getTransactionId()));
        header.put(FACILITY_LIKE_FIELD, StringUtils.fromString(mqcih.getFacilityLike()));
        header.put(ATTENTION_ID_FIELD, StringUtils.fromString(mqcih.getAttentionId()));
        header.put(START_CODE_FIELD, StringUtils.fromString(mqcih.getStartCode()));
        header.put(CANCEL_CODE_FIELD, StringUtils.fromString(mqcih.getCancelCode()));
        header.put(NEXT_TRANSATION_ID_FIELD, StringUtils.fromString(mqcih.getNextTransactionId()));
        header.put(INPUT_ITEM_FIELD, mqcih.getInputItem());
        return header;
    }

    public static Object createMQCIHHeaderFromBHeader(BMap<BString, Object> bHeader) {
        MQCIH header = new MQCIH();
        header.setFlags(bHeader.getIntValue(FLAGS_FIELD).intValue());
        header.setReturnCode(bHeader.getIntValue(RETURN_CODE_FIELD).intValue());
        header.setCompCode(bHeader.getIntValue(COMP_CODE_FIELD).intValue());
        header.setReason(bHeader.getIntValue(REASON_FIELD).intValue());
        header.setUOWControl(bHeader.getIntValue(UOW_CONTROL_FIELD).intValue());
        header.setGetWaitInterval(bHeader.getIntValue(WAIT_INTERVAL_FIELD).intValue());
        header.setLinkType(bHeader.getIntValue(LINK_TYPE_FIELD).intValue());
        header.setFacilityKeepTime(bHeader.getIntValue(FACILITY_KEEP_TIME_FIELD).intValue());
        header.setADSDescriptor(bHeader.getIntValue(ADS_DECSRIPTOR_FIELD).intValue());
        header.setConversationalTask(bHeader.getIntValue(CONVERSATIONAL_TASK_FIELD).intValue());
        header.setTaskEndStatus(bHeader.getIntValue(TASK_END_STATUS_FIELD).intValue());
        header.setFacility(bHeader.getArrayValue(FACITLIY_FIELD).getBytes());
        header.setFunction(bHeader.getStringValue(FUNCTION_FIELD).getValue());
        header.setAbendCode(bHeader.getStringValue(ABEND_CODE_FIELD).getValue());
        header.setAuthenticator(bHeader.getStringValue(AUTHENTICATOR_FIELD).getValue());
        header.setReserved1(bHeader.getStringValue(RESERVED1_FIELD).getValue());
        header.setReserved2(bHeader.getStringValue(RESERVED2_FIELD).getValue());
        header.setReserved3(bHeader.getStringValue(RESERVED3_FIELD).getValue());
        header.setReplyToFormat(bHeader.getStringValue(REPLY_TO_FORMAT_FIELD).getValue());
        header.setRemoteSysId(bHeader.getStringValue(REMOTE_SYS_ID_FIELD).getValue());
        header.setRemoteTransId(bHeader.getStringValue(REMOTE_TRANS_ID_FIELD).getValue());
        header.setTransactionId(bHeader.getStringValue(TRANSACTION_ID_FIELD).getValue());
        header.setFacilityLike(bHeader.getStringValue(FACILITY_LIKE_FIELD).getValue());
        header.setAttentionId(bHeader.getStringValue(ATTENTION_ID_FIELD).getValue());
        header.setStartCode(bHeader.getStringValue(START_CODE_FIELD).getValue());
        header.setCancelCode(bHeader.getStringValue(CANCEL_CODE_FIELD).getValue());
        header.setNextTransactionId(bHeader.getStringValue(NEXT_TRANSATION_ID_FIELD).getValue());
        header.setInputItem(bHeader.getIntValue(INPUT_ITEM_FIELD).intValue());
        return header;
    }
}
