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

public interface Constants {
    // Error names for IBM MQ package
    public static final String IBMMQ_ERROR = "Error";

    // Native properties in respective ballerina objects
    String NATIVE_QUEUE_MANAGER = "queueManager";
    String NATIVE_TOPIC = "topic";
    String NATIVE_QUEUE = "queue";

    String MQCIH_RECORD_NAME = "MQCIH";
    String MQRFH2_RECORD_NAME = "MQRFH2";
    String MQRFH_RECORD_NAME = "MQRFH";

    String MQRFH2FIELD_RECORD_NAME = "MQRFH2Field";

    BString STRUC_ID_FIELD = StringUtils.fromString("strucId");
    BString STRUC_LENGTH_FIELD = StringUtils.fromString("strucLength");
    BString FLAGS_FIELD = StringUtils.fromString("flags");
    BString VERSION_FIELD = StringUtils.fromString("version");
    BString ENCODING_FIELD = StringUtils.fromString("encoding");
    BString CODED_CHARSET_ID_FIELD = StringUtils.fromString("codedCharSetId");
    BString FORMAT_FIELD = StringUtils.fromString("format");
}
