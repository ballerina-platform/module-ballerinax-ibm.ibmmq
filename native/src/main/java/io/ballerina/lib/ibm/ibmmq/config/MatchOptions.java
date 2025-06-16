/*
 * Copyright (c) 2024, WSO2 LLC. (http://www.wso2.org).
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

package io.ballerina.lib.ibm.ibmmq.config;

import io.ballerina.runtime.api.values.BMap;
import io.ballerina.runtime.api.values.BString;

import static io.ballerina.lib.ibm.ibmmq.Constants.CORRELATION_ID_FIELD;
import static io.ballerina.lib.ibm.ibmmq.Constants.MESSAGE_ID_FIELD;

/**
 * Represents the selection criteria that determine which message is retrieved.
 *
 * @param messageId     The message identifier of the message which needs to be retrieved
 * @param correlationId The Correlation identifier of the message which needs to be retrieved
 */
public record MatchOptions(byte[] messageId, byte[] correlationId) {

    public MatchOptions(BMap<BString, Object> matchOptions) {
        this(
                getByteArrIfPresent(matchOptions, MESSAGE_ID_FIELD),
                getByteArrIfPresent(matchOptions, CORRELATION_ID_FIELD)
        );
    }

    private static byte[] getByteArrIfPresent(BMap<BString, Object> matchOptions, BString key) {
        if (!matchOptions.containsKey(key)) {
            return null;
        }
        return matchOptions.getArrayValue(key).getByteArray();
    }
}
