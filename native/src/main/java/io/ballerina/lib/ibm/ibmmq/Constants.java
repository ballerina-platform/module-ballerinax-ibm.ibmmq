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

import io.ballerina.runtime.api.utils.StringUtils;
import io.ballerina.runtime.api.values.BString;

import java.util.List;

public interface Constants {
    // Error names for IBM MQ package
    String IBMMQ_ERROR = "Error";

    // IBM MQ Error details related field names
    String ERROR_DETAILS = "ErrorDetails";
    BString ERROR_REASON_CODE = StringUtils.fromString("reasonCode");
    BString ERROR_ERROR_CODE = StringUtils.fromString("errorCode");
    BString ERROR_COMPLETION_CODE = StringUtils.fromString("completionCode");

    // Native properties in respective ballerina objects
    String NATIVE_QUEUE_MANAGER = "queueManager";
    String NATIVE_TOPIC = "topic";
    String NATIVE_QUEUE = "queue";

    String MQCIH_RECORD_NAME = "MQCIH";
    String MQRFH2_RECORD_NAME = "MQRFH2";
    String MQRFH_RECORD_NAME = "MQRFH";
    String MQIIH_RECORD_NAME = "MQIIH";

    String MQRFH2FIELD_RECORD_NAME = "MQRFH2Field";

    BString STRUC_ID_FIELD = StringUtils.fromString("strucId");
    BString STRUC_LENGTH_FIELD = StringUtils.fromString("strucLength");
    BString FLAGS_FIELD = StringUtils.fromString("flags");
    BString VERSION_FIELD = StringUtils.fromString("version");
    BString ENCODING_FIELD = StringUtils.fromString("encoding");
    BString CODED_CHARSET_ID_FIELD = StringUtils.fromString("codedCharSetId");
    BString MESSAGE_HEADERS = StringUtils.fromString("headers");

    // Ballerina record names
    String BTOPIC = "Topic";
    String BQUEUE = "Queue";
    String BPROPERTY = "Property";
    String BMESSAGE_NAME = "Message";
    String BCALLER_NAME = "Caller";

    // IBM MQ queue manager related configuration names
    BString QUEUE_MANAGER_NAME = StringUtils.fromString("name");
    BString HOST = StringUtils.fromString("host");
    BString PORT = StringUtils.fromString("port");
    BString CHANNEL = StringUtils.fromString("channel");
    BString USER_ID = StringUtils.fromString("userID");
    BString PASSWORD = StringUtils.fromString("password");
    BString SSL_CIPHER_SUITE = StringUtils.fromString("sslCipherSuite");
    BString SECURE_SOCKET = StringUtils.fromString("secureSocket");
    BString CERT = StringUtils.fromString("cert");
    BString KEY = StringUtils.fromString("key");
    BString CERT_FILE = StringUtils.fromString("certFile");
    BString KEY_FILE = StringUtils.fromString("keyFile");
    BString KEY_PASSWORD = StringUtils.fromString("keyPassword");
    BString KEY_STORE_PASSWORD = StringUtils.fromString("password");
    BString KEY_STORE_PATH = StringUtils.fromString("path");
    BString CRYPTO_TRUSTSTORE_PATH = StringUtils.fromString("path");
    BString CRYPTO_TRUSTSTORE_PASSWORD = StringUtils.fromString("password");
    String NATIVE_DATA_PRIVATE_KEY = "NATIVE_DATA_PRIVATE_KEY";
    String NATIVE_DATA_PUBLIC_KEY_CERTIFICATE = "NATIVE_DATA_PUBLIC_KEY_CERTIFICATE";

    // Supported TLS versions
    String TLS_V_1_0 = "TLSv1.0";
    String TLS_V_1_2 = "TLSv1.2";
    String TLS_V_1_3 = "TLSv1.3";

    // Cipher suite mapping for TLS version
    List<String> TLS_V_1_0_CIPHER_SPEC = List.of(
            "SSL_RSA_WITH_3DES_EDE_CBC_SHA", "SSL_RSA_WITH_AES_128_CBC_SHA",
            "SSL_RSA_WITH_AES_256_CBC_SHA", "SSL_RSA_WITH_DES_CBC_SHA"
    );
    List<String> TLS_V_1_3_CIPHER_SPEC = List.of(
            "TLS_AES_128_GCM_SHA256", "TLS_AES_256_GCM_SHA384", "TLS_CHACHA20_POLY1305_SHA256",
            "TLS_AES_128_CCM_SHA256", "TLS_AES_128_CCM_8_SHA256", "*TLS13", "*TLS13ORHIGHER"
    );

    // IBM MQ message property names
    BString MESSAGE_PAYLOAD = StringUtils.fromString("payload");
    BString MESSAGE_PROPERTIES = StringUtils.fromString("properties");
    BString MESSAGE_PROPERTY = StringUtils.fromString("property");
    BString PD_VERSION = StringUtils.fromString("version");
    BString PD_COPY_OPTIONS = StringUtils.fromString("copyOptions");
    BString PD_OPTIONS = StringUtils.fromString("options");
    BString PD_SUPPORT = StringUtils.fromString("support");
    BString PD_CONTEXT = StringUtils.fromString("context");
    BString PROPERTY_VALUE = StringUtils.fromString("value");
    BString PROPERTY_DESCRIPTOR = StringUtils.fromString("descriptor");
    BString WAIT_INTERVAL = StringUtils.fromString("waitInterval");
    BString OPTIONS = StringUtils.fromString("options");
    BString MATCH_OPTIONS = StringUtils.fromString("matchOptions");
    BString FORMAT_FIELD = StringUtils.fromString("format");
    BString MESSAGE_ID_FIELD = StringUtils.fromString("messageId");
    BString CORRELATION_ID_FIELD = StringUtils.fromString("correlationId");
    BString EXPIRY_FIELD = StringUtils.fromString("expiry");
    BString PRIORITY_FIELD = StringUtils.fromString("priority");
    BString PERSISTENCE_FIELD = StringUtils.fromString("persistence");
    BString MESSAGE_TYPE_FIELD = StringUtils.fromString("messageType");
    BString PUT_APPLICATION_TYPE_FIELD = StringUtils.fromString("putApplicationType");
    BString REPLY_TO_QUEUE_NAME_FIELD = StringUtils.fromString("replyToQueueName");
    BString REPLY_TO_QM_NAME_FIELD = StringUtils.fromString("replyToQueueManagerName");
    BString MESSAGE_ENCODING = StringUtils.fromString("encoding");
    BString MESSAGE_CHARSET = StringUtils.fromString("characterSet");
    BString MESSAGE_ACCOUNTING_TOKEN = StringUtils.fromString("accountingToken");
    BString MESSAGE_USERID = StringUtils.fromString("userId");

    /**
     * JMS Acknowledge Modes.
     */
    static final String AUTO_ACKNOWLEDGE_MODE = "AUTO_ACKNOWLEDGE";
    static final String CLIENT_ACKNOWLEDGE_MODE = "CLIENT_ACKNOWLEDGE";
    static final String SESSION_TRANSACTED_MODE = "SESSION_TRANSACTED";
}
