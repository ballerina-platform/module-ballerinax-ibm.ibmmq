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
import ballerina/time;

@test:Config {
    groups: ["ibmmqQueue"]
}
function basicQueueProducerConsumerTest() returns error? {
    QueueManager queueManager = check new (name = "QM1", host = "localhost", channel = "DEV.APP.SVRCONN");
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
function basicQueueProducerConsumerWithJsonTest() returns error? {
    QueueManager queueManager = check new (name = "QM1", host = "localhost", channel = "DEV.APP.SVRCONN");
    Queue producer = check queueManager.accessQueue("DEV.QUEUE.1", MQOO_OUTPUT);
    Queue consumer = check queueManager.accessQueue("DEV.QUEUE.1", MQOO_INPUT_AS_Q_DEF);
    json messageBody = {
        "data":{
            "EmployeeRecord":{
                "EmployeeId":"0001",
                "EmployeeName":{
                    "FirstName":"Mahroof",
                    "LastName":"Sabthar"
                    },
                "EmployeeFullName":"Mahroof   Sabthar",
                "EmployeeSalary":1500.0,
                "EmployeeGrade":"A",
                "EmployeeRating":99.8,
                "EmployeeDepartments":[
                    {
                        "DeptCode":20901,
                        "DeptName":"R&D"
                    },{
                        "DeptCode":29041,
                        "DeptName":"Ballerina"
                    }
                ],
                "EmployeeAddress":"Vijya RoadKolonnawa",
                "EmployeeAddressRed":{
                    "Street":"Vijya Road",
                    "City":"Kolonnawa"
                },
                "FineAmount":100.0,
                "PenaltyRating":9.2
            }
        }
    };
    byte[] payload = messageBody.toJsonString().toBytes();
    check producer->put({
        payload: payload
    });
    Message? message = check consumer->get();
    if message !is () {
        string rawMessageBody = check string:fromBytes(message.payload);
        json receivedMessage = check rawMessageBody.fromJsonString();
        test:assertEquals(receivedMessage, messageBody);
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
    QueueManager queueManager = check new (name = "QM1", host = "localhost", channel = "DEV.APP.SVRCONN");
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
    QueueManager queueManager = check new (name = "QM1", host = "localhost", channel = "DEV.APP.SVRCONN");
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
    QueueManager queueManager = check new (name = "QM1", host = "localhost", channel = "DEV.APP.SVRCONN");
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
    QueueManager queueManager = check new (name = "QM1", host = "localhost", channel = "DEV.APP.SVRCONN");
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
    QueueManager queueManager = check new (name = "QM1", host = "localhost", channel = "DEV.APP.SVRCONN");
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
    QueueManager queueManager = check new (name = "QM1", host = "localhost", channel = "DEV.APP.SVRCONN");
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
function produceAndConsumerMessageWithAdditionalPropertiesTest() returns error? {
    QueueManager queueManager = check new (name = "QM1", host = "localhost", channel = "DEV.APP.SVRCONN");
    Queue producer = check queueManager.accessQueue("DEV.QUEUE.1", MQOO_OUTPUT);
    Queue consumer = check queueManager.accessQueue("DEV.QUEUE.1", MQOO_INPUT_AS_Q_DEF);
    time:Utc timeNow = time:utcNow();
    check producer->put({
        payload: "Hello World".toBytes(),
        correlationId: "1234".toBytes(),
        expiry: timeNow[0],
        format: "mqformat",
        messageId: "test-id".toBytes(),
        messageType: 2,
        persistence: 0,
        priority: 4,
        putApplicationType: 28,
        replyToQueueManagerName: "QM1",
        replyToQueueName: "DEV.QUEUE.1"
    });
    Message? message = check consumer->get();
    if message !is () {
        test:assertEquals(string:fromBytes(message.payload), "Hello World");
        test:assertEquals(message.expiry, timeNow[0]);
        test:assertEquals(message.format, "mqformat");
        test:assertEquals(message.messageType, 2);
        test:assertEquals(message.persistence, 0);
        test:assertEquals(message.priority, 4);
        test:assertEquals(message.putApplicationType, 28);
        test:assertEquals(message.replyToQueueManagerName, "QM1");
        test:assertEquals(message.replyToQueueName, "DEV.QUEUE.1");
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
function produceAndConsumerMessageWithMultipleHeaderTypesTest() returns error? {
    QueueManager queueManager = check new (name = "QM1", host = "localhost", channel = "DEV.APP.SVRCONN");
    Queue producer = check queueManager.accessQueue("DEV.QUEUE.1", MQOO_OUTPUT);
    Queue consumer = check queueManager.accessQueue("DEV.QUEUE.1", MQOO_INPUT_AS_Q_DEF);
    check producer->put({
        payload: "Hello World".toBytes(),
        headers: [
            {
                facility: "facility".toBytes(),
                'function: "test",
                abendCode: "code",
                authenticator: "authenti"
            },
            {
                flags: 12,
                fieldValues: table [
                    {folder: "mcd", 'field: "Msd", value: "TestMcdValue"},
                    {folder: "jms", 'field: "Dlv", value: 134},
                    {folder: "mqps", 'field: "Ret", value: true}
                ]
            },
            {
                flags: 15,
                nameValuePairs: {"pair1": "value1", "pair2": "value2"}
            }
        ]
    });
    Message? message = check consumer->get();
    if message !is () {
        test:assertEquals(string:fromBytes(message.payload), "Hello World");
        Header[]? headers = message.headers;
        if headers is () {
            test:assertFail("Expected MQCIH headers");
        }
        test:assertTrue(headers.length() == 3);
        Header header = headers[0];
        if header is MQCIH {
            test:assertEquals(header.facility, "facility".toBytes());
            test:assertEquals(header.'function, "test");
            test:assertEquals(header.abendCode, "code");
            test:assertEquals(header.authenticator, "authenti");
        }
        header = headers[1];
        if header is MQRFH2 {
            test:assertEquals(header.flags, 12);
            test:assertEquals(header.fieldValues.get(["mcd", "Msd"]), {folder: "mcd", 'field: "Msd", value: "TestMcdValue"});
            test:assertEquals(header.fieldValues.get(["jms", "Dlv"]), {folder: "jms", 'field: "Dlv", value: 134});
            test:assertEquals(header.fieldValues.get(["mqps", "Ret"]), {folder: "mqps", 'field: "Ret", value: "1"});
        }
        header = headers[2];
        if header is MQRFH {
            test:assertEquals(header.flags, 15);
            test:assertEquals(header.nameValuePairs, {"pair1": "value1", "pair2": "value2"});
        }
    } else {
        test:assertFail("Expected a value for message");
    }
    check producer->close();
    check consumer->close();
    check queueManager.disconnect();
}

