// Copyright (c) 2023, WSO2 LLC. (http://www.wso2.org) All Rights Reserved.
//
// WSO2 LLC. licenses this file to you under the Apache License,
// Version 2.0 (the "License"); you may not use this file except
// in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied.  See the License for the
// specific language governing permissions and limitations
// under the License.

import ballerinax/ibm.ibmmq;

public function main() returns error? {
    ibmmq:QueueManager queueManager = check new (
        name = "QM1", host = "localhost", channel = "DEV.APP.SVRCONN"
    );
    ibmmq:Queue producer = check queueManager.accessQueue("DEV.QUEUE.1", ibmmq:MQOO_OUTPUT);

    ibmmq:MQRFH2 mqrfh2Header = {
        flags: 12,
        fieldValues: table [
            {folder: "mcd", 'field: "Msd", value: "TestMcdValue"},
            {folder: "jms", 'field: "Dlv", value: 134},
            {folder: "mqps", 'field: "Ret", value: true}
        ]
    };

    check producer->put({
        headers: [mqrfh2Header],
        payload: "This is a sample message to IBM MQ queue".toBytes()
    });
    check producer->close();
    check queueManager.disconnect();
}
