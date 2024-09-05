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
    QueueManager queueManager = check new (
        name = "QM1", host = "localhost", channel = "DEV.APP.SVRCONN", 
        userID = "app", password = "password");
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
function basicQueueProducerConsumerWithOneQueueObjectTest() returns error? {
    QueueManager queueManager = check new (
        name = "QM1", host = "localhost", channel = "DEV.APP.SVRCONN", 
        userID = "app", password = "password");
    Queue queue = check queueManager.accessQueue("DEV.QUEUE.1", MQOO_OUTPUT | MQOO_INPUT_AS_Q_DEF);
    check queue->put({
        payload: "Hello World with one queue".toBytes()
    });
    Message? message = check queue->get();
    if message !is () {
        test:assertEquals(string:fromBytes(message.payload), "Hello World with one queue");
    } else {
        test:assertFail("Expected a value for message");
    }
    check queue->close();
    check queueManager.disconnect();
}

@test:Config {
    groups: ["ibmmqQueue"]
}
function basicQueueProducerConsumerWithJsonPayloadTest() returns error? {
    QueueManager queueManager = check new (
        name = "QM1", host = "localhost", channel = "DEV.APP.SVRCONN", 
        userID = "app", password = "password");
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
                "EmployeeSalary":1500.00d,
                "EmployeeGrade":"A",
                "EmployeeRating":99.8d,
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
                "FineAmount":100.00d,
                "PenaltyRating":9.2d
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
    QueueManager queueManager = check new (
        name = "QM1", host = "localhost", channel = "DEV.APP.SVRCONN", 
        userID = "app", password = "password");
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
    QueueManager queueManager = check new (
        name = "QM1", host = "localhost", channel = "DEV.APP.SVRCONN", 
        userID = "app", password = "password");
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
    QueueManager queueManager = check new (
        name = "QM1", host = "localhost", channel = "DEV.APP.SVRCONN", 
        userID = "app", password = "password");
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
    QueueManager queueManager = check new (
        name = "QM1", host = "localhost", channel = "DEV.APP.SVRCONN", 
        userID = "app", password = "password");
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
    QueueManager queueManager = check new (
        name = "QM1", host = "localhost", channel = "DEV.APP.SVRCONN", 
        userID = "app", password = "password");
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
    QueueManager queueManager = check new (
        name = "QM1", host = "localhost", channel = "DEV.APP.SVRCONN", 
        userID = "app", password = "password");
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
    QueueManager queueManager = check new (
        name = "QM1", host = "localhost", channel = "DEV.APP.SVRCONN", 
        userID = "app", password = "password");
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
function produceAndConsumerMessageWithAdditionalPropertiesWithJsonPayloadTest() returns error? {
    QueueManager queueManager = check new (
        name = "QM1", host = "localhost", channel = "DEV.APP.SVRCONN", 
        userID = "app", password = "password");
    Queue producer = check queueManager.accessQueue("DEV.QUEUE.1", MQOO_OUTPUT);
    Queue consumer = check queueManager.accessQueue("DEV.QUEUE.1", MQOO_INPUT_AS_Q_DEF);
    time:Utc timeNow = time:utcNow();
    json messageBody = {
        "data":{
            "EmployeeRecord":{
                "EmployeeId":"0001",
                "EmployeeName":{
                    "FirstName":"Mahroof",
                    "LastName":"Sabthar"
                    },
                "EmployeeFullName":"Mahroof   Sabthar",
                "EmployeeSalary":1500.00d,
                "EmployeeGrade":"A",
                "EmployeeRating":99.8d,
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
                "FineAmount":100.00d,
                "PenaltyRating":9.2d
            }
        }
    };
    byte[] payload = messageBody.toJsonString().toBytes();
    check producer->put({
        payload: payload,
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
        string rawMessageBody = check string:fromBytes(message.payload);
        json receivedMessage = check rawMessageBody.fromJsonString();
        test:assertEquals(receivedMessage, messageBody);
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
    QueueManager queueManager = check new (
        name = "QM1", host = "localhost", channel = "DEV.APP.SVRCONN", 
        userID = "app", password = "password");
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

@test:Config {
    groups: ["ibmmqQueue"]
}
function produceAndConsumerMessageWithMultipleHeaderTypesWithJsonPayloadTest() returns error? {
    QueueManager queueManager = check new (
        name = "QM1", host = "localhost", channel = "DEV.APP.SVRCONN", 
        userID = "app", password = "password");
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
                "EmployeeSalary":1500.00d,
                "EmployeeGrade":"A",
                "EmployeeRating":99.8d,
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
                "FineAmount":100.00d,
                "PenaltyRating":9.2d
            }
        }
    };
    byte[] payload = messageBody.toJsonString().toBytes();
    check producer->put({
        payload: payload,
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
        string rawMessageBody = check string:fromBytes(message.payload);
        json receivedMessage = check rawMessageBody.fromJsonString();
        test:assertEquals(receivedMessage, messageBody);
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

@test:Config {
    groups: ["ibmmqQueue", "messageIdentification"]
}
function produceMessagesWithIdentification() returns error? {
    QueueManager queueManager = check new (
        name = "QM1", host = "localhost", channel = "DEV.ADMIN.SVRCONN", 
        userID = "admin", password = "password");
    Queue queue = check queueManager.accessQueue("DEV.QUEUE.1", MQOO_OUTPUT | MQOO_INPUT_AS_Q_DEF | MQOO_SET_IDENTITY_CONTEXT);

    string messageContent = "This is a sample message with a identification.";
    string accountingToken = "accountint-token-1";
    string userId = "user-1";
    check queue->put({
        accountingToken: accountingToken.toBytes(),
        persistence: 1,
        userId: userId,
        payload: messageContent.toBytes()
    }, MQPMO_SET_IDENTITY_CONTEXT);

    Message? message = check queue->get();
    test:assertTrue(message is Message, "Could not retrieve a message");

    byte[]? payload = message?.payload;
    test:assertEquals(message?.userId, userId, "Invalid userId");
    byte[] retrievedAccountingToken = trimTrailingZeros(check message?.accountingToken.ensureType());
    test:assertEquals(retrievedAccountingToken, accountingToken.toBytes(), "Invalid accounting token");
    test:assertEquals(string:fromBytes(check payload.ensureType()), messageContent, "Invalid message content");

    check queue->close();
    check queueManager.disconnect();
}

function trimTrailingZeros(byte[] bytes) returns byte[] {
    int i = bytes.length() - 1;
    while (i >= 0 && bytes[i] == 0) {
        i -= 1;
    }
    return bytes.slice(0, i + 1);
}

@test:Config {
    groups: ["ibmmqQueue", "charset"]
}
function produceMessagesWithCharacterSet() returns error? {
    QueueManager queueManager = check new (
        name = "QM1", host = "localhost", channel = "DEV.APP.SVRCONN", 
        userID = "app", password = "password");
    Queue queue = check queueManager.accessQueue("DEV.QUEUE.1", MQOO_OUTPUT | MQOO_INPUT_AS_Q_DEF);

    MessageCharset characterSet = CCSI_UTF8;
    string messageContent = "This is a sample UTF-8 charset based message";
    check queue->put({
        characterSet,
        payload: messageContent.toBytes()
    });

    Message? message = check queue->get();
    test:assertTrue(message is Message, "Could not retrieve a message");

    test:assertEquals(message?.characterSet, characterSet, "Invalid character-set found");
    byte[]? payload = message?.payload;
    test:assertEquals(string:fromBytes(check payload.ensureType()), messageContent, "Invalid message content");

    check queue->close();
    check queueManager.disconnect();
}

@test:Config {
    groups: ["ibmmqQueue", "encoding"]
}
function produceMessageWithEncoding() returns error? {
    QueueManager queueManager = check new (
        name = "QM1", host = "localhost", channel = "DEV.APP.SVRCONN", 
        userID = "app", password = "password");
    Queue queue = check queueManager.accessQueue("DEV.QUEUE.1", MQOO_OUTPUT | MQOO_INPUT_AS_Q_DEF);

    int encoding = MQENC_INTEGER_NORMAL;
    json messageBody = {
        user: "Doe, John",
        id: 1234,
        description: "Regular application developer"
    };
    byte[] payload = messageBody.toJsonString().toBytes();
    check queue->put({
        encoding,
        payload
    });

    Message? message = check queue->get();
    test:assertTrue(message is Message, "Could not retrieve a message");

    test:assertEquals(message?.encoding, encoding, "Invalid encoding found");

    byte[]? retrievedPayload = message?.payload;
    if retrievedPayload is () {
        test:assertFail("Could not find the message payload");
    }
    string rawMessageBody = check string:fromBytes(retrievedPayload);
    json receivedMessage = check rawMessageBody.fromJsonString();
    test:assertEquals(receivedMessage, messageBody, "Invalid message content");

    check queue->close();
    check queueManager.disconnect();
}

@test:Config {
    groups: ["ibmmqQueue", "matchOptions"]
}
function produceConsumeWithMsgId() returns error? {
    QueueManager queueManager = check new (
        name = "QM1", host = "localhost", channel = "DEV.APP.SVRCONN", 
        userID = "app", password = "password");
    Queue queue = check queueManager.accessQueue("DEV.QUEUE.2", MQOO_OUTPUT | MQOO_INPUT_AS_Q_DEF);
    
    byte[] providedMsgId = "msg-id-1".toBytes();
    string messageContent = "This is a sample message with a message-id.";
    check queue->put({
        messageId: providedMsgId,
        payload: messageContent.toBytes()
    });

    Message? message = check queue->get(matchOptions = { messageId: providedMsgId });
    test:assertTrue(message is Message, "Could not retrieve a message for a valid message identifier");

    byte[]? payload = message?.payload;
    test:assertEquals(string:fromBytes(check payload.ensureType()), messageContent);

    check queue->close();
    check queueManager.disconnect();
}

@test:Config {
    groups: ["ibmmqQueue", "matchOptions"]
}
function produceConsumeWithInvalidMsgId() returns error? {
    QueueManager queueManager = check new (
        name = "QM1", host = "localhost", channel = "DEV.APP.SVRCONN", 
        userID = "app", password = "password");
    Queue queue = check queueManager.accessQueue("DEV.QUEUE.2", MQOO_OUTPUT | MQOO_INPUT_AS_Q_DEF);
    
    string messageContent = "This is a sample message with a message-id.";
    check queue->put({
        payload: messageContent.toBytes()
    });

    Message? message = check queue->get(matchOptions = { messageId: "test-msg-id-1".toBytes() });
    test:assertTrue(message is (), "Retrieved a message for an invalid message identifier");

    check queue->close();
    check queueManager.disconnect();
}

@test:Config {
    groups: ["ibmmqQueue", "matchOptions"]
}
function produceConsumeWithCorrId() returns error? {
    QueueManager queueManager = check new (
        name = "QM1", host = "localhost", channel = "DEV.APP.SVRCONN", 
        userID = "app", password = "password");
    Queue queue = check queueManager.accessQueue("DEV.QUEUE.2", MQOO_OUTPUT | MQOO_INPUT_AS_Q_DEF);
    
    byte[] providedCorrId = "corr-id-1".toBytes();
    string messageContent = "This is a sample message with a correlation-id.";
    check queue->put({
        correlationId: providedCorrId,
        payload: messageContent.toBytes()
    });

    Message? message = check queue->get(matchOptions = { correlationId: providedCorrId });
    test:assertTrue(message is Message, "Could not retrieve a message for a valid correlation identifier");
    
    byte[]? correlationId = message?.correlationId;
    test:assertTrue(correlationId is byte[], "Could not find the correlation identifier for the message");

    byte[]? payload = message?.payload;
    test:assertEquals(string:fromBytes(check payload.ensureType()), messageContent);

    check queue->close();
    check queueManager.disconnect();
}

@test:Config {
    groups: ["ibmmqQueue", "matchOptions"]
}
function produceConsumeWithInvalidCorrId() returns error? {
    QueueManager queueManager = check new (
        name = "QM1", host = "localhost", channel = "DEV.APP.SVRCONN", 
        userID = "app", password = "password");
    Queue queue = check queueManager.accessQueue("DEV.QUEUE.2", MQOO_OUTPUT | MQOO_INPUT_AS_Q_DEF);
    
    string messageContent = "This is a sample message with a message-id.";
    check queue->put({
        payload: messageContent.toBytes()
    });

    Message? message = check queue->get(matchOptions = { correlationId: "test-corr-id-1".toBytes() });
    test:assertTrue(message is (), "Retrieved a message for an invalid correlation identifier");

    check queue->close();
    check queueManager.disconnect();
}

@test:Config {
    groups: ["ibmmqQueue", "matchOptions"]
}
function produceConsumeWithMsgIdAndCorrId() returns error? {
    QueueManager queueManager = check new (
        name = "QM1", host = "localhost", channel = "DEV.APP.SVRCONN", 
        userID = "app", password = "password");
    Queue queue = check queueManager.accessQueue("DEV.QUEUE.2", MQOO_OUTPUT | MQOO_INPUT_AS_Q_DEF);
    
    byte[] providedMsgId = "msg-id-1".toBytes();
    byte[] providedCorrId = "corr-id-1".toBytes();
    string messageContent = "This is a sample message with a message-id and a correlation-id.";
    check queue->put({
        messageId: providedMsgId,
        correlationId: providedCorrId,
        payload: messageContent.toBytes()
    });

    Message? message = check queue->get(matchOptions = { messageId: providedMsgId, correlationId: providedCorrId });
    test:assertTrue(message is Message, "Could not retrieve a message for a valid message identifier and correlation identifier");

    byte[]? payload = message?.payload;
    test:assertEquals(string:fromBytes(check payload.ensureType()), messageContent);

    check queue->close();
    check queueManager.disconnect();
}

@test:Config {
    groups: ["ibmmqQueue", "matchOptions"],
    dependsOn: [produceConsumeWithMsgIdAndCorrId]
}
function produceConsumeWithInvalidMsgIdAndCorrId() returns error? {
    QueueManager queueManager = check new (
        name = "QM1", host = "localhost", channel = "DEV.APP.SVRCONN", 
        userID = "app", password = "password");
    Queue queue = check queueManager.accessQueue("DEV.QUEUE.2", MQOO_OUTPUT | MQOO_INPUT_AS_Q_DEF);
    
    byte[] providedMsgId = "msg-id-1".toBytes();
    byte[] providedCorrId = "corr-id-1".toBytes();
    string messageContent = "This is a sample message with a message-id and a correlation-id.";
    check queue->put({
        correlationId: providedCorrId,
        payload: messageContent.toBytes()
    });

    Message? message = check queue->get(matchOptions = { messageId: providedMsgId, correlationId: providedCorrId });
    test:assertTrue(message is (), "Retrieved a message for an invalid message-id and a correct correlation identifier");

    check queue->close();
    check queueManager.disconnect();
}

@test:Config {
    groups: ["ibmmqQueue", "matchOptions"],
    dependsOn: [produceConsumeWithInvalidMsgIdAndCorrId]
}
function produceConsumeWithMsgIdAndInvalidCorrId() returns error? {
    QueueManager queueManager = check new (
        name = "QM1", host = "localhost", channel = "DEV.APP.SVRCONN", 
        userID = "app", password = "password");
    Queue queue = check queueManager.accessQueue("DEV.QUEUE.2", MQOO_OUTPUT | MQOO_INPUT_AS_Q_DEF);
    
    byte[] providedMsgId = "msg-id-1".toBytes();
    byte[] providedCorrId = "corr-id-1".toBytes();
    string messageContent = "This is a sample message with a message-id and a correlation-id.";
    check queue->put({
        messageId: providedMsgId,
        payload: messageContent.toBytes()
    });

    Message? message = check queue->get(matchOptions = { messageId: providedMsgId, correlationId: providedCorrId });
    test:assertTrue(message is (), "Retrieved a message for a correct message-id and an invalid correlation identifier");

    check queue->close();
    check queueManager.disconnect();
}
