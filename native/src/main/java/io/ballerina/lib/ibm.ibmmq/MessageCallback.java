/*
 * Copyright (c) 2025, WSO2 LLC. (http://www.wso2.org).
 *
 *  WSO2 LLC. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied. See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package io.ballerina.lib.ibm.ibmmq;

import io.ballerina.runtime.api.async.Callback;
import io.ballerina.runtime.api.values.BError;

/**
 * Callback for the Ballerina runtime.
 *
 * @since 1.3.0
 */
public class MessageCallback implements Callback {

    MessageCallback() {
        // No-argument constructor for simple callback pattern
    }

    @Override
    public void notifySuccess(Object result) {
        // Handle successful execution - could add logging or metrics here
        if (result instanceof BError) {
            ((BError) result).printStackTrace();
        }
    }

    @Override
    public void notifyFailure(BError error) {
        // Handle failure - log the error
        error.printStackTrace();
    }
}
