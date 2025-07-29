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

listener Listener ibmmqListener = check new Listener({
    channel: "DEV.APP.SVRCONN",
    host: "localhost",
    name: "QM1",
    userID: "app",
    password: "password"
});

isolated int queueServiceReceivedMessageCount = 0;
isolated int topicServiceReceivedMessageCount = 0;

final QueueManager queueManager = check new (
    name = "QM1", host = "localhost", channel = "DEV.APP.SVRCONN",
    userID = "app", password = "password"
);
final Queue queueProducer = check queueManager.accessQueue("DEV.QUEUE.3", MQOO_OUTPUT);
final Topic topicProducer = check queueManager.accessTopic("DEV.TOPIC.1", "DEV.TOPIC.1", OPEN_AS_PUBLICATION, MQOO_OUTPUT);

@test:Config {
    groups: ["service"]
}
isolated function testQueueService() returns error? {
    Service consumerSvc = @ServiceConfig {
        queueName: "DEV.QUEUE.3",
        pollingInterval: 2,
        receiveTimeout: 1
    } service object {
        remote function onMessage(Message message) returns error? {
            lock {
                queueServiceReceivedMessageCount += 1;
            }
        }
    };
    check ibmmqListener.attach(consumerSvc, "dev-queue-3-service");
    check queueProducer->put({
        payload: "Hello World from queue".toBytes()
    });
    runtime:sleep(2);
    lock {
        test:assertEquals(queueServiceReceivedMessageCount, 1, "'DEV.QUEUE.3' did not received the expected number of messages");
    }
}

@test:Config {
    groups: ["service", "notworking"]
}
isolated function testTopicService() returns error? {
    Service consumerSvc = @ServiceConfig {
        topicName: "DEV.TOPIC.1",
        subscriberName: "DEV.SUB.1",
        pollingInterval: 1,
        receiveTimeout: 1
    } service object {
        remote function onMessage(Message message) returns error? {
            lock {
                topicServiceReceivedMessageCount += 1;
            }
        }
    };
    check ibmmqListener.attach(consumerSvc, "dev-topic-1-service");
    check topicProducer->send({
        payload: "Hello World from topic".toBytes()
    });
    runtime:sleep(2);
    lock {
        test:assertEquals(topicServiceReceivedMessageCount, 1, "'DEV.TOPIC.1' did not received the expected number of messages");
    }
}

isolated int serviceWithCallerReceivedMsgCount = 0;

@test:Config {
    groups: ["service"]
}
isolated function testServiceWithCaller() returns error? {
    Service consumerSvc = @ServiceConfig {
        sessionAckMode: CLIENT_ACKNOWLEDGE,
        topicName: "DEV.TOPIC.1",
        subscriberName: "test.subscription",
        pollingInterval: 1,
        receiveTimeout: 1
    } service object {
        remote function onMessage(Message message, Caller caller) returns error? {
            lock {
                serviceWithCallerReceivedMsgCount += 1;
            }
            check caller->acknowledge(message);
        }
    };
    check ibmmqListener.attach(consumerSvc, "test-caller-svc");
    check topicProducer->send({
        payload: "Hello World from topic".toBytes()
    });
    runtime:sleep(2);
    lock {
        test:assertEquals(serviceWithCallerReceivedMsgCount, 1, "'DEV.TOPIC.1' did not received the expected number of messages");
    }
}

isolated int ServiceWithTransactionsMsgCount = 0;

@test:Config {
    groups: ["service", "transactions"]
}
isolated function testServiceWithTransactions() returns error? {
    Service consumerSvc = @ServiceConfig {
        sessionAckMode: SESSION_TRANSACTED,
        topicName: "DEV.TOPIC.1",
        subscriberName: "test.transated.sub",
        pollingInterval: 1,
        receiveTimeout: 1
    } service object {
        isolated remote function onMessage(Message message, Caller caller) returns error? {
            lock {
                ServiceWithTransactionsMsgCount += 1;
            }
            string content = check string:fromBytes(message.payload);
            if content == "End of messages" {
                check caller->'commit();
            }
        }
    };
    check ibmmqListener.attach(consumerSvc, "test-transacted-service");

    check topicProducer->send({
        payload: "This is the first message".toBytes()
    });
    check topicProducer->send({
        payload: "This is the second message".toBytes()
    });
    check topicProducer->send({
        payload: "This is the third message".toBytes()
    });
    check topicProducer->send({
        payload: "End of messages".toBytes()
    });
    runtime:sleep(5);
    lock {
        test:assertEquals(ServiceWithTransactionsMsgCount, 4, "Invalid number of received messages");
    }
}

@test:Config {
    groups: ["service"]
}
isolated function testServiceWithOnError() returns error? {
    Service consumerSvc = @ServiceConfig {
        sessionAckMode: CLIENT_ACKNOWLEDGE,
        topicName: "DEV.TOPIC.1"
    } service object {
        remote function onMessage(Message message) returns error? {
        }

        remote function onError(Error err) returns error? {
        }
    };
    check ibmmqListener.attach(consumerSvc, "test-onerror-service");
}

@test:Config {
    groups: ["service"]
}
isolated function testServiceReturningError() returns error? {
    Service consumerSvc = @ServiceConfig {
        sessionAckMode: CLIENT_ACKNOWLEDGE,
        topicName: "DEV.TOPIC.1"
    } service object {
        remote function onMessage(Message message) returns error? {
            return error("Error occurred while processing the message");
        }
    };
    check ibmmqListener.attach(consumerSvc, "test-onMessage-error-service");

    check topicProducer->send({
        payload: "This is a sample message".toBytes()
    });
    runtime:sleep(2);
}

@test:Config {
    groups: ["service"]
}
isolated function testListenerImmediateStop() returns error? {
    Listener msgListener = check new Listener({
        channel: "DEV.APP.SVRCONN",
        host: "localhost",
        name: "QM1",
        userID: "app",
        password: "password"
    });
    Service consumerSvc = @ServiceConfig {
        sessionAckMode: CLIENT_ACKNOWLEDGE,
        topicName: "DEV.TOPIC.1"
    } service object {
        remote function onMessage(Message message, Caller caller) returns error? {
        }
    };
    check msgListener.attach(consumerSvc, "consumer-svc");
    check msgListener.'start();
    runtime:sleep(2);
    check msgListener.immediateStop();
}

@test:Config {
    groups: ["service"]
}
isolated function testServiceAttachWithoutSvcPath() returns error? {
    Service consumerSvc = @ServiceConfig {
        sessionAckMode: CLIENT_ACKNOWLEDGE,
        topicName: "DEV.TOPIC.1"
    } service object {
        remote function onMessage(Message message, Caller caller) returns error? {
        }
    };
    check ibmmqListener.attach(consumerSvc);
}

@test:Config {
    groups: ["service"]
}
isolated function testServiceDetach() returns error? {
    Service consumerSvc = @ServiceConfig {
        sessionAckMode: CLIENT_ACKNOWLEDGE,
        topicName: "DEV.TOPIC.1"
    } service object {
        remote function onMessage(Message message, Caller caller) returns error? {
        }
    };
    check ibmmqListener.attach(consumerSvc, "consumer-svc");
    runtime:sleep(2);
    check ibmmqListener.detach(consumerSvc);
}

@test:AfterGroups {
    value: ["service", "validations"]
}
isolated function afterMessageListenerTests() returns error? {
    check queueProducer->close();
    check topicProducer->close();
}

