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

import ballerina/test;

@test:Config {
    groups: ["ibmmqQueue"]
}
function basicQueueProducerConsumerTest() returns error? {
    QueueManager queueManager = check new(name = "QM1", host = "localhost", channel = "DEV.APP.SVRCONN");
    Queue producer = check queueManager.accessQueue("DEV.QUEUE.1", MQOO_OUTPUT);
    Queue consumer = check queueManager.accessQueue("DEV.QUEUE.1", MQOO_INPUT_AS_Q_DEF);
    check producer->put({
        payload: "Hello World".toBytes()
    });
    Message? message = check consumer->get();
    if message !is () {
        test:assertEquals(string:fromBytes(message.payload), "Hello World");
    } else {
        test:assertFail("Expected a value for message");
    }
    check producer->close();
    check consumer->close();
    check queueManager.disconnect();
}

@test:Config {
    groups: ["ibmmqQueue"]
}
function pubSubMultipleMessagesQueueProducerConsumerTest() returns error? {
    QueueManager queueManager = check new(name = "QM1", host = "localhost", channel = "DEV.APP.SVRCONN");
    Queue producer = check queueManager.accessQueue("DEV.QUEUE.1", MQOO_OUTPUT);
    Queue consumer = check queueManager.accessQueue("DEV.QUEUE.1", MQOO_INPUT_AS_Q_DEF);
    foreach int i in 0 ... 4 {
        check producer->put({
            payload: i.toString().toBytes()
        });
    }
    foreach int i in 0 ... 4 {
        Message? message = check consumer->get(options = MQGMO_WAIT, waitInterval = 2);
        if message !is () {
            test:assertEquals(string:fromBytes(message.payload), i.toString());
        } else {
            test:assertFail("Expected a value for message");
        }
    }
    check producer->close();
    check consumer->close();
    check queueManager.disconnect();
}

@test:Config {
    groups: ["ibmmqQueue"]
}
function consumerWithoutProducingMessageTest() returns error? {
    QueueManager queueManager = check new(name = "QM1", host = "localhost", channel = "DEV.APP.SVRCONN");
    Queue consumer = check queueManager.accessQueue("DEV.QUEUE.1", MQOO_INPUT_AS_Q_DEF);
    Message|Error? result = consumer->get(options = MQGMO_WAIT, waitInterval = 5);
    test:assertTrue(result is ());
    check consumer->close();
    check queueManager.disconnect();
}

@test:Config {
    groups: ["ibmmqQueue"]
}
function produceToNonExistingQueueTest() returns error? {
    QueueManager queueManager = check new(name = "QM1", host = "localhost", channel = "DEV.APP.SVRCONN");
    Queue|Error result = queueManager.accessQueue("NON.EXISTING.QUEUE", MQOO_OUTPUT);
    if result is Error {
        test:assertEquals(result.message(), "Error occurred while accessing queue: MQJE001: Completion Code '2', Reason '2085'.");
        test:assertEquals(result.detail().reasonCode, 2085);
        test:assertEquals(result.detail().completionCode, 2);
    } else {
        test:assertFail("Expected an error");
    }
    check queueManager.disconnect();
}

@test:Config {
    groups: ["ibmmqQueue"]
}
function consumerConnectToNonExistingQueueTest() returns error? {
    QueueManager queueManager = check new(name = "QM1", host = "localhost", channel = "DEV.APP.SVRCONN");
    Queue|Error result = queueManager.accessQueue("NON.EXISTING.QUEUE", MQOO_INPUT_AS_Q_DEF);
    if result is Error {
        test:assertEquals(result.message(), "Error occurred while accessing queue: MQJE001: Completion Code '2', Reason '2085'.");
        test:assertEquals(result.detail().reasonCode, 2085);
        test:assertEquals(result.detail().completionCode, 2);
    } else {
        test:assertFail("Expected an error");
    }
    check queueManager.disconnect();
}

@test:Config {
    groups: ["ibmmqQueue"]
}
function produceWithInvalidQueueNameTest() returns error? {
    QueueManager queueManager = check new(name = "QM1", host = "localhost", channel = "DEV.APP.SVRCONN");
    Queue|Error result = queueManager.accessQueue("INVALID QUEUE", MQOO_INPUT_AS_Q_DEF);
    if result is Error {
        test:assertEquals(result.message(), "Error occurred while accessing queue: MQJE001: Completion Code '2', Reason '2085'.");
        test:assertEquals(result.detail().reasonCode, 2085);
        test:assertEquals(result.detail().completionCode, 2);
    } else {
        test:assertFail("Expected an error");
    }
    check queueManager.disconnect();
}

@test:Config {
    groups: ["ibmmqQueue"]
}
function consumeFromAnInvalidQueueNameTest() returns error? {
    QueueManager queueManager = check new(name = "QM1", host = "localhost", channel = "DEV.APP.SVRCONN");
    Queue|Error result = queueManager.accessQueue("INVALID QUEUE", MQOO_INPUT_AS_Q_DEF);
    if result is Error {
        test:assertEquals(result.message(), "Error occurred while accessing queue: MQJE001: Completion Code '2', Reason '2085'.");
        test:assertEquals(result.detail().reasonCode, 2085);
        test:assertEquals(result.detail().completionCode, 2);
    } else {
        test:assertFail("Expected an error");
    }
    check queueManager.disconnect();
}
