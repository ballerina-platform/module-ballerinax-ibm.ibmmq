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
import ballerina/io;

configurable string queueManagerName = ?;
configurable string host = ?;
configurable int port = ?;
configurable string channel = ?;
configurable string userID = ?;
configurable string password = ?;
configurable string queueName = ?;

listener ibmmq:Listener consumer = new({
    name: queueManagerName,
    host,
    port,
    channel,
    userID,
    password
});

@ibmmq:ServiceConfig {
    config: {
        topicName,
        subscriptionName: "DEV.BASE.TOPIC",
        durable: false
    },
    pollingInterval: 1
}
service ibmmq:Service on consumer {
    remote function onMessage(ibmmq:Message message) returns error? {
        io:println(string:fromBytes(message.payload));
    }
}
