// Copyright (c) 2024, WSO2 LLC. (http://www.wso2.org).
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

configurable string queueManagerName = ?;
configurable string host = ?;
configurable int port = ?;
configurable string channel = ?;
configurable string userID = ?;
configurable string password = ?;
configurable string queueName = ?;

public function main() returns error? {
    ibmmq:QueueManager queueManager = check new (
        name = queueManagerName, 
        host = host, 
        channel = channel, 
        userID = userID, 
        password = password
    );
    ibmmq:Queue queue = check queueManager.accessQueue(queueName, ibmmq:MQOO_OUTPUT);

    ibmmq:MQRFH2 mqrfh2Header = {
        flags: 12,
        fieldValues: table [
            {folder: "mcd", 'field: "Msd", value: "TestMcdValue"},
            {folder: "jms", 'field: "Dlv", value: 134},
            {folder: "mqps", 'field: "Ret", value: true}
        ]
    };

    check queue->put({
        headers: [mqrfh2Header],
        payload: "This is a sample message to IBM MQ queue".toBytes()
    });
    check queue->close();
    check queueManager.disconnect();
}
