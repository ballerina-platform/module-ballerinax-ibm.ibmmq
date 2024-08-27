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

import static io.ballerina.lib.ibm.ibmmq.Constants.MATCH_OPTIONS;
import static io.ballerina.lib.ibm.ibmmq.Constants.OPTIONS;
import static io.ballerina.lib.ibm.ibmmq.Constants.WAIT_INTERVAL;

/**
 * Represents the IBM MQ GET message options.
 *
 * @param options Get message option
 * @param waitInterval The maximum time (in seconds) that a `get` call waits for a suitable message to arrive.
 *                     It is used in conjunction with `MQGMO_WAIT`.
 * @param matchOptions Message selection criteria
 */
public record GetMessageOptions(int options, int waitInterval, MatchOptions matchOptions) {

    public GetMessageOptions(BMap<BString, Object> getMsgOptions) {
        this (
                getMsgOptions.getIntValue(OPTIONS).intValue(),
                getMsgOptions.getIntValue(WAIT_INTERVAL).intValue() * 1000,
                getMatchOptions(getMsgOptions)
        );
    }

    @SuppressWarnings("unchecked")
    private static MatchOptions getMatchOptions(BMap<BString, Object> getMsgOptions) {
        if (!getMsgOptions.containsKey(MATCH_OPTIONS)) {
            return null;
        }
        return new MatchOptions((BMap<BString, Object>) getMsgOptions.getMapValue(MATCH_OPTIONS));
    }
}
