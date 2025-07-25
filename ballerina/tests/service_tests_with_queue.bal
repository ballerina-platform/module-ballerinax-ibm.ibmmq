// Copyright (c) 2025, WSO2 LLC. (http://www.wso2.org).
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

import ballerina/lang.runtime;
import ballerina/test;

const decimal SLEEP_TIME = 2;
byte[] queuePayload = [];

listener Listener ibmmqListener1 = new Listener({
    channel: "DEV.APP.SVRCONN",
    host: "localhost",
    name: "QM1",
    userID: "app",
    password: "password"
});

@ServiceConfig {
    queueName: "DEV.QUEUE.3"
}
service Service on ibmmqListener1 {
    remote function onMessage(Message message) returns Error? {
        queuePayload = message.payload;
        return;
    }
}

@test:Config {
    groups: ["service", "queue"]
}
function testConsumeMessageFromServiceWithQueue() returns error? {
    QueueManager queueManager = check new (
        name = "QM1", host = "localhost", channel = "DEV.APP.SVRCONN",
        userID = "app", password = "password"
    );
    Queue producer = check queueManager.accessQueue("DEV.QUEUE.3", MQOO_OUTPUT);
    check producer->put({
        payload: "Hello World from queue".toBytes()
    });
    runtime:sleep(SLEEP_TIME);
    check producer->close();
    check queueManager.disconnect();
    test:assertEquals(string:fromBytes(queuePayload), "Hello World from queue");
}
