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

@test:Config {}
function basicPublisherSubscriberTest() returns error? {
    QueueManager queueManager = check new QueueManager(name = "QM1", host = "localhost", channel = "DEV.APP.SVRCONN");
    Topic subTopic = check queueManager.accessTopic("dev", "DEV.BASE.TOPIC", OPEN_AS_SUBSCRIPTION, MQSO_CREATE);
    Topic pubTopic = check queueManager.accessTopic("dev", "DEV.BASE.TOPIC", OPEN_AS_PUBLICATION, MQOO_OUTPUT);
    check pubTopic->put({
        payload: "Hello World".toBytes()
    });
    Message message = check subTopic->get();
    test:assertEquals(string:fromBytes(message.payload), "Hello World");
    check subTopic->close();
    check queueManager.disconnect();
}

@test:Config {}
function pubSubMultipleMessagesInOrderTest() returns error? {
    QueueManager queueManager = check new QueueManager(name = "QM1", host = "localhost", channel = "DEV.APP.SVRCONN");
    Topic subTopic = check queueManager.accessTopic("dev", "DEV.BASE.TOPIC", OPEN_AS_SUBSCRIPTION, MQSO_CREATE);
    Topic pubTopic = check queueManager.accessTopic("dev", "DEV.BASE.TOPIC", OPEN_AS_PUBLICATION, MQOO_OUTPUT);
    foreach int i in 0 ... 4 {
        check pubTopic->put({
            payload: i.toString().toBytes()
        });
    }
    foreach int i in 0 ... 4 {
        Message message = check subTopic->get(waitInterval = 2);
        test:assertEquals(string:fromBytes(message.payload), i.toString());
    }
    check subTopic->close();
    check queueManager.disconnect();
}

@test:Config {}
function subscribeWithFiniteTimeoutTest() returns error? {
    QueueManager queueManager = check new QueueManager(name = "QM1", host = "localhost", channel = "DEV.APP.SVRCONN");
    Topic subTopic = check queueManager.accessTopic("dev", "DEV.BASE.TOPIC", OPEN_AS_SUBSCRIPTION, MQSO_CREATE);
    Topic pubTopic = check queueManager.accessTopic("dev", "DEV.BASE.TOPIC", OPEN_AS_PUBLICATION, MQOO_OUTPUT);
    check pubTopic->put({
        payload: "Hello World".toBytes()
    });
    Message message = check subTopic->get(waitInterval = 5);
    test:assertEquals(string:fromBytes(message.payload), "Hello World");
    check subTopic->close();
    check queueManager.disconnect();
}

@test:Config {}
function subscribeWithoutPublishTest() returns error? {
    QueueManager queueManager = check new QueueManager(name = "QM1", host = "localhost", channel = "DEV.APP.SVRCONN");
    Topic subTopic = check queueManager.accessTopic("dev", "DEV.BASE.TOPIC", OPEN_AS_SUBSCRIPTION, MQSO_CREATE);
    Message|Error result = subTopic->get(waitInterval = 5, gmOptions = MQGMO_NO_WAIT);
    if result is Error {
        test:assertEquals(result.message(), "Error occurred while getting a message from the topic: MQJE001: Completion Code '2', Reason '2033'.");
        test:assertEquals(result.detail().reasonCode, 2033);
        test:assertEquals(result.detail().completionCode, 2);
    } else {
        test:assertFail("Expected an error");
    }
    check subTopic->close();
    check queueManager.disconnect();
}

@test:Config {}
function publishToNonExistingTopicTest() returns error? {
    QueueManager queueManager = check new QueueManager(name = "QM1", host = "localhost", channel = "DEV.APP.SVRCONN");
    Topic|Error result = queueManager.accessTopic("dev", "NON.EXISTING.TOPIC", OPEN_AS_PUBLICATION, MQOO_OUTPUT);
    if result is Error {
        test:assertEquals(result.message(), "Error occurred while accessing topic: MQJE001: Completion Code '2', Reason '2085'.");
        test:assertEquals(result.detail().reasonCode, 2085);
        test:assertEquals(result.detail().completionCode, 2);
    } else {
        test:assertFail("Expected an error");
    }
    check queueManager.disconnect();
}

@test:Config {}
function subscribeToNonExistingTopicTest() returns error? {
    QueueManager queueManager = check new QueueManager(name = "QM1", host = "localhost", channel = "DEV.APP.SVRCONN");
    Topic|Error result = queueManager.accessTopic("dev", "NON.EXISTING.TOPIC", OPEN_AS_SUBSCRIPTION, MQSO_CREATE);
    if result is Error {
        test:assertEquals(result.message(), "Error occurred while accessing topic: MQJE001: Completion Code '2', Reason '2085'.");
        test:assertEquals(result.detail().reasonCode, 2085);
        test:assertEquals(result.detail().completionCode, 2);
    } else {
        test:assertFail("Expected an error");
    }
    check queueManager.disconnect();
}

@test:Config {}
function subscribeWithInvalidTopicNameTest() returns error? {
    QueueManager queueManager = check new QueueManager(name = "QM1", host = "localhost", channel = "DEV.APP.SVRCONN");
    Topic|Error result = queueManager.accessTopic("dev", "INVALID TOPIC", OPEN_AS_SUBSCRIPTION, MQSO_CREATE);
    if result is Error {
        test:assertEquals(result.message(), "Error occurred while accessing topic: MQJE001: Completion Code '2', Reason '2152'.");
        test:assertEquals(result.detail().reasonCode, 2152);
        test:assertEquals(result.detail().completionCode, 2);
    } else {
        test:assertFail("Expected an error");
    }
    check queueManager.disconnect();
}

@test:Config {}
function publishWithInvalidTopicNameTest() returns error? {
    QueueManager queueManager = check new QueueManager(name = "QM1", host = "localhost", channel = "DEV.APP.SVRCONN");
    Topic|Error result = queueManager.accessTopic("dev", "INVALID TOPIC", OPEN_AS_PUBLICATION, MQOO_OUTPUT);
    if result is Error {
        test:assertEquals(result.message(), "Error occurred while accessing topic: MQJE001: Completion Code '2', Reason '2085'.");
        test:assertEquals(result.detail().reasonCode, 2085);
        test:assertEquals(result.detail().completionCode, 2);
    } else {
        test:assertFail("Expected an error");
    }
    check queueManager.disconnect();
}

@test:Config {}
function accessTopicAfterQMDisconnectTest() returns error? {
    QueueManager queueManager = check new QueueManager(name = "QM1", host = "localhost", channel = "DEV.APP.SVRCONN");
    check queueManager.disconnect();
    Topic|Error result = queueManager.accessTopic("dev", "DEV.BASE.TOPIC", OPEN_AS_PUBLICATION, MQOO_OUTPUT);
    if result is Error {
        test:assertEquals(result.message(), "Error occurred while accessing topic: MQJE001: An MQException " +
        "occurred: Completion Code '2', Reason '2018'\n'MQJI002: Not connected to a queue manager.'.");
        test:assertEquals(result.detail().reasonCode, 2018);
        test:assertEquals(result.detail().completionCode, 2);
    } else {
        test:assertFail("Expected an error");
    }
}

@test:Config {}
function putToTopicAfterTopicCloseTest() returns error? {
    QueueManager queueManager = check new QueueManager(name = "QM1", host = "localhost", channel = "DEV.APP.SVRCONN");
    Topic pubTopic = check queueManager.accessTopic("dev", "DEV.BASE.TOPIC", OPEN_AS_PUBLICATION, MQOO_OUTPUT);
    check pubTopic->close();
    Error? result = pubTopic->put({
        payload: "Hello World".toBytes()
    });
    if result is Error {
        test:assertEquals(result.message(), "Error occurred while putting a message to the topic: MQJE001: " +
        "An MQException occurred: Completion Code '2', Reason '2019'\n'MQJI027: The queue has been closed.'.");
        test:assertEquals(result.detail().reasonCode, 2019);
        test:assertEquals(result.detail().completionCode, 2);
    } else {
        test:assertFail("Expected an error");
    }
    check queueManager.disconnect();
}

@test:Config {}
function putToTopicAfterQMDisconnectTest() returns error? {
    QueueManager queueManager = check new QueueManager(name = "QM1", host = "localhost", channel = "DEV.APP.SVRCONN");
    Topic pubTopic = check queueManager.accessTopic("dev", "DEV.BASE.TOPIC", OPEN_AS_PUBLICATION, MQOO_OUTPUT);
    check queueManager.disconnect();
    Error? result = pubTopic->put({
        payload: "Hello World".toBytes()
    });
    if result is Error {
        test:assertEquals(result.message(), "Error occurred while putting a message to the topic: MQJE001: An MQException " +
        "occurred: Completion Code '2', Reason '2018'\n'MQJI002: Not connected to a queue manager.'.");
        test:assertEquals(result.detail().reasonCode, 2018);
        test:assertEquals(result.detail().completionCode, 2);
    } else {
        test:assertFail("Expected an error");
    }
    check queueManager.disconnect();
}
