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
import ballerina/io;

public function main() returns error? {
    ibmmq:QueueManager queueManager = check new (
        name = "QM1", host = "localhost", channel = "DEV.APP.SVRCONN"
    );
    ibmmq:Queue consumer = check queueManager.accessQueue(
            "DEV.QUEUE.1", ibmmq:MQOO_INPUT_AS_Q_DEF);
    while true {
        ibmmq:Message? message = check consumer->get(options = ibmmq:MQGMO_WAIT);
        if message is () {
            continue;
        }
        io:println(string:fromBytes(message.payload));
        ibmmq:Header[]? headers = message.headers;
        if headers is () {
            continue;
        }
        ibmmq:Header header = headers[0];
        if header is ibmmq:MQRFH2 {
            table<ibmmq:MQRFH2Field> key(folder, 'field) fieldTable = header.fieldValues;
            if fieldTable.hasKey(["mcd", "Msd"]) {
                io:println(fieldTable.get(["mcd", "Msd"]));
            }
        }
    }
}
