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
    groups: ["ibmmqTopic"]
}
function basicPublisherSubscriberTest() returns error? {
    QueueManager queueManager = check new (name = "QM1", host = "localhost", channel = "DEV.APP.SVRCONN");
    Topic subscriber = check queueManager.accessTopic("dev", "DEV.BASE.TOPIC", OPEN_AS_SUBSCRIPTION, MQSO_CREATE);
    Topic publisher = check queueManager.accessTopic("dev", "DEV.BASE.TOPIC", OPEN_AS_PUBLICATION, MQOO_OUTPUT);
    check publisher->put({
        payload: "Hello World".toBytes()
    });
    Message? message = check subscriber->get();
    if message !is () {
        test:assertEquals(string:fromBytes(message.payload), "Hello World");
    } else {
        test:assertFail("Expected a value for message");
    }
    check subscriber->close();
    check publisher->close();
    check queueManager.disconnect();
}

@test:Config {
    groups: ["ibmmqTopic"]
}
function pubSubMultipleMessagesInOrderTest() returns error? {
    QueueManager queueManager = check new (name = "QM1", host = "localhost", channel = "DEV.APP.SVRCONN");
    Topic subscriber = check queueManager.accessTopic("dev", "DEV.BASE.TOPIC", OPEN_AS_SUBSCRIPTION, MQSO_CREATE);
    Topic publisher = check queueManager.accessTopic("dev", "DEV.BASE.TOPIC", OPEN_AS_PUBLICATION, MQOO_OUTPUT);
    foreach int i in 0 ... 4 {
        check publisher->put({
            payload: i.toString().toBytes()
        });
    }
    foreach int i in 0 ... 4 {
        Message? message = check subscriber->get(options = MQGMO_WAIT, waitInterval = 5);
        if message !is () {
            test:assertEquals(string:fromBytes(message.payload), i.toString());
        } else {
            test:assertFail("Expected a value for message");
        }
    }
    check subscriber->close();
    check publisher->close();
    check queueManager.disconnect();
}

@test:Config {
    groups: ["ibmmqTopic"]
}
function subscribeWithFiniteTimeoutTest() returns error? {
    QueueManager queueManager = check new (name = "QM1", host = "localhost", channel = "DEV.APP.SVRCONN");
    Topic subscriber = check queueManager.accessTopic("dev", "DEV.BASE.TOPIC", OPEN_AS_SUBSCRIPTION, MQSO_CREATE);
    Topic publisher = check queueManager.accessTopic("dev", "DEV.BASE.TOPIC", OPEN_AS_PUBLICATION, MQOO_OUTPUT);
    check publisher->put({
        payload: "Hello World".toBytes()
    });
    Message? message = check subscriber->get(options = MQGMO_WAIT, waitInterval = 5);
    if message !is () {
        test:assertEquals(string:fromBytes(message.payload), "Hello World");
    } else {
        test:assertFail("Expected a value for message");
    }
    check subscriber->close();
    check publisher->close();
    check queueManager.disconnect();
}

@test:Config {
    groups: ["ibmmqTopic"]
}
function subscribeWithoutPublishTest() returns error? {
    QueueManager queueManager = check new (name = "QM1", host = "localhost", channel = "DEV.APP.SVRCONN");
    Topic subscriber = check queueManager.accessTopic("dev", "DEV.BASE.TOPIC", OPEN_AS_SUBSCRIPTION, MQSO_CREATE);
    Message|Error? result = subscriber->get(waitInterval = 5);
    test:assertTrue(result is ());
    check subscriber->close();
    check queueManager.disconnect();
}

@test:Config {
    groups: ["ibmmqTopic"]
}
function publishToNonExistingTopicTest() returns error? {
    QueueManager queueManager = check new (name = "QM1", host = "localhost", channel = "DEV.APP.SVRCONN");
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

@test:Config {
    groups: ["ibmmqTopic"]
}
function subscribeToNonExistingTopicTest() returns error? {
    QueueManager queueManager = check new (name = "QM1", host = "localhost", channel = "DEV.APP.SVRCONN");
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

@test:Config {
    groups: ["ibmmqTopic"]
}
function subscribeWithInvalidTopicNameTest() returns error? {
    QueueManager queueManager = check new (name = "QM1", host = "localhost", channel = "DEV.APP.SVRCONN");
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

@test:Config {
    groups: ["ibmmqTopic"]
}
function publishWithInvalidTopicNameTest() returns error? {
    QueueManager queueManager = check new (name = "QM1", host = "localhost", channel = "DEV.APP.SVRCONN");
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

@test:Config {
    groups: ["ibmmqTopic"]
}
function accessTopicAfterQMDisconnectTest() returns error? {
    QueueManager queueManager = check new (name = "QM1", host = "localhost", channel = "DEV.APP.SVRCONN");
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

@test:Config {
    groups: ["ibmmqTopic"]
}
function putToTopicAfterTopicCloseTest() returns error? {
    QueueManager queueManager = check new (name = "QM1", host = "localhost", channel = "DEV.APP.SVRCONN");
    Topic publisher = check queueManager.accessTopic("dev", "DEV.BASE.TOPIC", OPEN_AS_PUBLICATION, MQOO_OUTPUT);
    check publisher->close();
    Error? result = publisher->put({
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

@test:Config {
    groups: ["ibmmqTopic"]
}
function putToTopicAfterQMDisconnectTest() returns error? {
    QueueManager queueManager = check new (name = "QM1", host = "localhost", channel = "DEV.APP.SVRCONN");
    Topic publisher = check queueManager.accessTopic("dev", "DEV.BASE.TOPIC", OPEN_AS_PUBLICATION, MQOO_OUTPUT);
    check queueManager.disconnect();
    Error? result = publisher->put({
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

@test:Config {
    groups: ["ibmmqTopic"]
}
function publishSubscribeWithHeadersTest() returns error? {
    QueueManager queueManager = check new (name = "QM1", host = "localhost", channel = "DEV.APP.SVRCONN");
    Topic subscriber = check queueManager.accessTopic("dev", "DEV.BASE.TOPIC", OPEN_AS_SUBSCRIPTION, MQSO_CREATE);
    Topic publisher = check queueManager.accessTopic("dev", "DEV.BASE.TOPIC", OPEN_AS_PUBLICATION, MQOO_OUTPUT);
    check publisher->put({
        payload: "Hello World".toBytes(),
        headers: [
            {
                flags: 12,
                fieldValues: table [
                    {folder: "mcd", 'field: "Msd", value: "TestMcdValue"},
                    {folder: "jms", 'field: "Dlv", value: 134},
                    {folder: "mqps", 'field: "Ret", value: true}
                ]
            },
            {
                flags: 13,
                fieldValues: table [
                    {folder: "mqps", 'field: "Sud", value: "TestUserData"},
                    {folder: "mqpse", 'field: "Sid", value: "PubData"},
                    {folder: "mqps", 'field: "Ret", value: true}
                ]
            },
            {
                flags: 14,
                fieldValues: table [
                    {folder: "mcd", 'field: "Msd", value: "TestMcdValue23"},
                    {folder: "jms", 'field: "Dlv", value: 1341},
                    {folder: "mqps", 'field: "Ret", value: false}
                ]
            }
        ]
    });
    Message? message = check subscriber->get();
    if message !is () {
        test:assertEquals(string:fromBytes(message.payload), "Hello World");
        Header[]? headers = message.headers;
        if headers is () {
            test:assertFail("Expected MQRFH2 headers");
        }
        test:assertEquals(headers[0].flags, 12);
        Header header = headers[0];
        if header is MQRFH2 {
            table<MQRFH2Field> key(folder, 'field) fieldTable = header.fieldValues;
            test:assertEquals(fieldTable.get(["mcd", "Msd"]), {folder: "mcd", 'field: "Msd", value: "TestMcdValue"});
            test:assertEquals(fieldTable.get(["jms", "Dlv"]), {folder: "jms", 'field: "Dlv", value: 134});
            test:assertEquals(fieldTable.get(["mqps", "Ret"]), {folder: "mqps", 'field: "Ret", value: "1"});
        }

        test:assertEquals(headers[1].flags, 13);
        header = headers[1];
        if header is MQRFH2 {
            table<MQRFH2Field> key(folder, 'field) fieldTable = header.fieldValues;
            test:assertEquals(fieldTable.get(["mqps", "Sud"]), {folder: "mqps", 'field: "Sud", value: "TestUserData"});
            test:assertEquals(fieldTable.get(["mqpse", "Sid"]), {folder: "mqpse", 'field: "Sid", value: "PubData"});
            test:assertEquals(fieldTable.get(["mqps", "Ret"]), {folder: "mqps", 'field: "Ret", value: "1"});
        }

        test:assertEquals(headers[2].flags, 14);
        header = headers[2];
        if header is MQRFH2 {
            table<MQRFH2Field> key(folder, 'field) fieldTable = header.fieldValues;
            test:assertEquals(fieldTable.get(["mcd", "Msd"]), {folder: "mcd", 'field: "Msd", value: "TestMcdValue23"});
            test:assertEquals(fieldTable.get(["jms", "Dlv"]), {folder: "jms", 'field: "Dlv", value: 1341});
            test:assertEquals(fieldTable.get(["mqps", "Ret"]), {folder: "mqps", 'field: "Ret", value: "0"});
        }
    } else {
        test:assertFail("Expected a value for message");
    }
    check subscriber->close();
    check publisher->close();
    check queueManager.disconnect();
}

@test:Config {
    groups: ["ibmmqTopic"]
}
function publishSubscribeWithMQRFHHeadersTest() returns error? {
    QueueManager queueManager = check new (name = "QM1", host = "localhost", channel = "DEV.APP.SVRCONN");
    Topic subscriber = check queueManager.accessTopic("dev", "DEV.BASE.TOPIC", OPEN_AS_SUBSCRIPTION, MQSO_CREATE);
    Topic publisher = check queueManager.accessTopic("dev", "DEV.BASE.TOPIC", OPEN_AS_PUBLICATION, MQOO_OUTPUT);
    check publisher->put({
        payload: "Hello World".toBytes(),
        headers: [
            {
                flags: 12,
                nameValuePairs: {"pair1": "value1", "pair2": "value2"}
            }
        ]
    });
    Message? message = check subscriber->get();
    if message !is () {
        test:assertEquals(string:fromBytes(message.payload), "Hello World");
        Header[]? headers = message.headers;
        if headers is () {
            test:assertFail("Expected MQRFH headers");
        }
        test:assertEquals(headers[0].flags, 12);
        Header header = headers[0];
        if header is MQRFH {
            map<string> nameValuePairs = header.nameValuePairs;
            test:assertEquals(nameValuePairs, {"pair1": "value1", "pair2": "value2"});
        }
    } else {
        test:assertFail("Expected a value for message");
    }
    check subscriber->close();
    check publisher->close();
    check queueManager.disconnect();
}

@test:Config {
    groups: ["ibmmqTopic"]
}
function publishSubscribeWithMQCIHHeadersTest() returns error? {
    QueueManager queueManager = check new (name = "QM1", host = "localhost", channel = "DEV.APP.SVRCONN");
    Topic subscriber = check queueManager.accessTopic("dev", "DEV.BASE.TOPIC", OPEN_AS_SUBSCRIPTION, MQSO_CREATE);
    Topic publisher = check queueManager.accessTopic("dev", "DEV.BASE.TOPIC", OPEN_AS_PUBLICATION, MQOO_OUTPUT);
    check publisher->put({
        payload: "Hello World".toBytes(),
        headers: [
            {
                flags: 12,
                returnCode: 12,
                compCode: 0,
                reason: 5,
                UOWControl: 2,
                waitInterval: 12,
                linkType: 1,
                facilityKeepTime: 10,
                ADSDescriptor: 20,
                conversationalTask: 12,
                taskEndStatus: 4,
                facility: "facility".toBytes(),
                'function: "test",
                abendCode: "code",
                authenticator: "authenti",
                reserved1: "reserve1",
                reserved2: "reserve2",
                reserved3: "reserve3",
                replyToFormat: "reformat",
                remoteSysId: "rSId",
                remoteTransId: "rTId",
                transactionId: "trId",
                facilityLike: "fcLk",
                attentionId: "atId",
                startCode: "stCd",
                cancelCode: "ccCd",
                nextTransactionId: "ntid",
                inputItem: 23
            }
        ]
    });
    Message? message = check subscriber->get();
    if message !is () {
        test:assertEquals(string:fromBytes(message.payload), "Hello World");
        Header[]? headers = message.headers;
        if headers is () {
            test:assertFail("Expected MQCIH headers");
        }
        Header header = headers[0];
        if header is MQCIH {
            test:assertEquals(header, {
                strucLength: 180,
                version: 2,
                strucId: "CIH ",
                flags: 12,
                returnCode: 12,
                compCode: 0,
                reason: 5,
                UOWControl: 2,
                waitInterval: 12,
                linkType: 1,
                facilityKeepTime: 10,
                ADSDescriptor: 20,
                conversationalTask: 12,
                taskEndStatus: 4,
                facility: "facility".toBytes(),
                'function: "test",
                abendCode: "code",
                authenticator: "authenti",
                reserved1: "reserve1",
                reserved2: "reserve2",
                reserved3: "reserve3",
                replyToFormat: "reformat",
                remoteSysId: "rSId",
                remoteTransId: "rTId",
                transactionId: "trId",
                facilityLike: "fcLk",
                attentionId: "atId",
                startCode: "stCd",
                cancelCode: "ccCd",
                nextTransactionId: "ntid",
                inputItem: 23
            });
        }
    } else {
        test:assertFail("Expected a value for message");
    }
    check subscriber->close();
    check publisher->close();
    check queueManager.disconnect();
}
