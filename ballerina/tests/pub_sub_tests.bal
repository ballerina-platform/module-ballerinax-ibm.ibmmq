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
function subscribeWithFiniteTimeout() returns error? {
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
function subscribeWithoutPublish() returns error? {
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
function publishToNonExistingTopic() returns error? {
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
function subscribeToNonExistingTopic() returns error? {
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
function subscribeWithInvalidTopicName() returns error? {
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
function publishWithInvalidTopicName() returns error? {
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
function accessTopicAfterQMDisconnect() returns error? {
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
function putToTopicAfterTopicClose() returns error? {
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
function putToTopicAfterQMDisconnect() returns error? {
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
