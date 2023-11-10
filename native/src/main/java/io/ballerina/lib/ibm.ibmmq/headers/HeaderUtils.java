package io.ballerina.lib.ibm.ibmmq.headers;

import com.ibm.mq.MQMessage;
import com.ibm.mq.headers.MQDataException;
import com.ibm.mq.headers.MQIIH;
import com.ibm.mq.headers.MQMD;
import com.ibm.mq.headers.MQMD1;
import com.ibm.mq.headers.MQMDE;
import com.ibm.mq.headers.MQRMH;
import com.ibm.mq.headers.MQSAPH;
import com.ibm.mq.headers.MQTM;
import com.ibm.mq.headers.MQTM2;
import com.ibm.mq.headers.MQTMC2;
import com.ibm.mq.headers.MQWIH;
import com.ibm.mq.headers.MQXQH;
import io.ballerina.runtime.api.Runtime;
import io.ballerina.runtime.api.values.BMap;
import io.ballerina.runtime.api.values.BString;

import java.io.IOException;
import java.util.ArrayList;

/**
 * This class contains utility methods needed for manipulating headers.
 */
public class HeaderUtils {

    // Following header types are read and ignored as the decode and encode to ballerina is not yet added.
    public static void decodeUnSupportedHeaders(Runtime runtime, MQMessage msg,
                                                ArrayList<BMap<BString, Object>> headers) throws IOException {
        int dataOffset = msg.getDataOffset();
        try {
            MQIIH mqiih = new MQIIH();
            mqiih.read(msg);
            MQRFH2Header.decodeHeader(runtime, msg, headers);
            return;
        } catch (MQDataException e) {
            msg.seek(dataOffset);
        }
        try {
            MQMD mqmd = new MQMD();
            mqmd.read(msg);
            MQRFH2Header.decodeHeader(runtime, msg, headers);
            return;
        } catch (MQDataException e) {
            msg.seek(dataOffset);
        }
        try {
            MQMD1 mqmd1 = new MQMD1();
            mqmd1.read(msg);
            MQRFH2Header.decodeHeader(runtime, msg, headers);
            return;
        } catch (MQDataException e) {
            msg.seek(dataOffset);
        }
        try {
            MQMDE mqmde = new MQMDE();
            mqmde.read(msg);
            MQRFH2Header.decodeHeader(runtime, msg, headers);
            return;
        } catch (MQDataException e) {
            msg.seek(dataOffset);
        }
        try {
            MQRMH mqrmh = new MQRMH();
            mqrmh.read(msg);
            MQRFH2Header.decodeHeader(runtime, msg, headers);
            return;
        } catch (MQDataException e) {
            msg.seek(dataOffset);
        }
        try {
            MQSAPH mqsaph = new MQSAPH();
            mqsaph.read(msg);
            MQRFH2Header.decodeHeader(runtime, msg, headers);
            return;
        } catch (MQDataException e) {
            msg.seek(dataOffset);
        }
        try {
            MQTM mqtm = new MQTM();
            mqtm.read(msg);
            MQRFH2Header.decodeHeader(runtime, msg, headers);
            return;
        } catch (MQDataException e) {
            msg.seek(dataOffset);
        }
        try {
            MQTM2 mqtm2 = new MQTM2();
            mqtm2.read(msg);
            MQRFH2Header.decodeHeader(runtime, msg, headers);
            return;
        } catch (MQDataException e) {
            msg.seek(dataOffset);
        }
        try {
            MQTMC2 mqtmc2 = new MQTMC2();
            mqtmc2.read(msg);
            MQRFH2Header.decodeHeader(runtime, msg, headers);
            return;
        } catch (MQDataException e) {
            msg.seek(dataOffset);
        }
        try {
            MQWIH mqwih = new MQWIH();
            mqwih.read(msg);
            MQRFH2Header.decodeHeader(runtime, msg, headers);
            return;
        } catch (MQDataException e) {
            msg.seek(dataOffset);
        }
        try {
            MQXQH mqxqh = new MQXQH();
            mqxqh.read(msg);
            MQRFH2Header.decodeHeader(runtime, msg, headers);
            return;
        } catch (MQDataException e) {
            msg.seek(dataOffset);
        }
    }
}
