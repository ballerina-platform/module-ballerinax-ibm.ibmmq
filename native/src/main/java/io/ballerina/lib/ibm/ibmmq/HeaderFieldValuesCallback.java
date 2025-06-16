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

import io.ballerina.runtime.api.async.Callback;
import io.ballerina.runtime.api.creators.TypeCreator;
import io.ballerina.runtime.api.creators.ValueCreator;
import io.ballerina.runtime.api.values.BError;
import io.ballerina.runtime.api.values.BTable;

import java.util.concurrent.CountDownLatch;

import static io.ballerina.lib.ibm.ibmmq.Constants.MQRFH2FIELD_RECORD_NAME;
import static io.ballerina.lib.ibm.ibmmq.ModuleUtils.getModule;

/**
 * {@link Callback} implementation for the header value table.
 */
public class HeaderFieldValuesCallback implements Callback {

    private final CountDownLatch latch;
    private BTable headerValueTable;

    public HeaderFieldValuesCallback(CountDownLatch latch) {
        this.latch = latch;
    }

    @Override
    public void notifySuccess(Object result) {
        if (result instanceof BTable bTable) {
            this.headerValueTable = bTable;
        } else if (result instanceof BError bError) {
            bError.printStackTrace();
            headerValueTable = ValueCreator.createTableValue(TypeCreator.createTableType(TypeCreator
                    .createRecordType(MQRFH2FIELD_RECORD_NAME, getModule(), 0, false, 0), false));
        }
        latch.countDown();
    }

    @Override
    public void notifyFailure(BError bError) {
        bError.printStackTrace();
        latch.countDown();
        System.exit(1);
    }

    public BTable getHeaderValueTable() {
        return headerValueTable;
    }
}
