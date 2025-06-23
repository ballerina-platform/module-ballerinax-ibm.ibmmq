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

import ballerina/test;

byte[] durableTopicPayload = [];
byte[] nonDurableTopicPayload = [];

listener Listener ibmmqListener2 = new Listener({
    name: "QM1",
    host: "localhost",
    port: 1414,
    channel: "DEV.APP.SVRCONN",
    userID: "app",
    password: "password"
});

@ServiceConfig {
    config: {
        topicName: "DEV.TOPIC.1",
        subscriptionName: "DEV.SUB.1",
        durable: true
    }
}
service Service on ibmmqListener2 {
    remote function onMessage(Message message) returns Error? {
        durableTopicPayload = message.payload;
        return;
    }
}

@ServiceConfig {
    config: {
        topicName: "DEV.TOPIC.2"
    }
}
service Service on ibmmqListener2 {
    remote function onMessage(Message message) returns Error? {
        nonDurableTopicPayload = message.payload;
        return;
    }
}

@test:Config {
    groups: ["service", "topic"]
}
function testConsumeMessageFromServiceWithTopic() returns error? {
    string payload = "Hello World from durable topic";
    QueueManager queueManager = check new (
        name = "QM1", host = "localhost", channel = "DEV.APP.SVRCONN",
        userID = "app", password = "password"
    );
    Topic producer = check queueManager.accessTopic("DEV.TOPIC.1", "DEV.TOPIC.1", OPEN_AS_PUBLICATION, MQOO_OUTPUT);
    check producer->send({
        payload: payload.toBytes()
    });
    check producer->close();
    check queueManager.disconnect();
    test:assertEquals(string:fromBytes(durableTopicPayload), payload);
}

@test:Config {
    groups: ["service", "topic"]
}
function testConsumeMessageFromServiceWithNonDurableTopic() returns error? {
    string payload = "Hello World from non-durable topic";
    QueueManager queueManager = check new (
        name = "QM1", host = "localhost", channel = "DEV.APP.SVRCONN",
        userID = "app", password = "password"
    );
    Topic producer = check queueManager.accessTopic("DEV.TOPIC.2", "DEV.TOPIC.2", OPEN_AS_PUBLICATION, MQOO_OUTPUT);
    check producer->send({
        payload: payload.toBytes()
    });
    check producer->close();
    check queueManager.disconnect();
    test:assertEquals(string:fromBytes(nonDurableTopicPayload), payload);
}
