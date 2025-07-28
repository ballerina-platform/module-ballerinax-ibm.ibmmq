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

final Listener ibmmqListener = check new Listener({
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

@test:BeforeGroups {
    value: ["service", "validations"]
}
isolated function beforeMessageListenerTests() returns error? {
    check ibmmqListener.'start();
}

@test:Config {
    groups: ["service", "queue"]
}
isolated function testQueueService() returns error? {
    Service consumerSvc = @ServiceConfig {
        queueName: "DEV.QUEUE.3"
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
    groups: ["service", "topic"]
}
isolated function testTopicService() returns error? {
    Service consumerSvc = @ServiceConfig {
        topicName: "DEV.TOPIC.1",
        subscriberName: "DEV.SUB.1"
    } service object {
        remote function onMessage(Message message) returns error? {
            lock {
                topicServiceReceivedMessageCount += 1;
            }
        }
    };
    check ibmmqListener.attach(consumerSvc, "dev-topic-1-service");
    check topicProducer->send({
        payload: "Hello World from queue".toBytes()
    });
    runtime:sleep(2);
    lock {
        test:assertEquals(topicServiceReceivedMessageCount, 1, "'DEV.TOPIC.1' did not received the expected number of messages");
    }
}

isolated int serviceWithCallerReceivedMsgCount = 0;

@test:Config {
    groups: ["service", "topic"]
}
isolated function testServiceWithCaller() returns error? {
    Service consumerSvc = @ServiceConfig {
        sessionAckMode: CLIENT_ACKNOWLEDGE,
        topicName: "DEV.TOPIC.1",
        subscriberName: "test.subscription"
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
