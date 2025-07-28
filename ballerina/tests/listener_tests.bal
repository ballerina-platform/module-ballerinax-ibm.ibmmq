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
final Queue queueProducer = check queueManager.accessQueue("test-queue", MQOO_OUTPUT);
final Topic topicProducer = check queueManager.accessTopic("test-topic", "test-topic", OPEN_AS_PUBLICATION, MQOO_OUTPUT);

@test:BeforeGroups {
    value: ["messageListener", "listenerValidations"]
}
isolated function beforeMessageListenerTests() returns error? {
    Service queue3Service = @ServiceConfig {
        queueName: "test-queue"
    } service object {
        remote function onMessage(Message message) returns error? {
            lock {
                queueServiceReceivedMessageCount += 1;
            }
        }
    };

    Service topic3Service = @ServiceConfig {
        topicName: "test-topic"
    } service object {
        remote function onMessage(Message message) returns error? {
            lock {
                topicServiceReceivedMessageCount += 1;
            }
        }
    };
    check ibmmqListener.attach(queue3Service, "test-queue-service");
    check ibmmqListener.attach(topic3Service, "test-topic-service");
    check ibmmqListener.'start();
}

@test:Config {
    groups: ["messageListener"]
}
isolated function testQueueService() returns error? {
    check queueProducer->put({
        payload: "Hello World from queue".toBytes()
    });
    runtime:sleep(2);
    lock {
        test:assertEquals(queueServiceReceivedMessageCount, 1, "'test-queue' did not received the expected number of messages");
    }
}

@test:Config {
    groups: ["messageListener"]
}
isolated function testTopicService() returns error? {
    check topicProducer->send({
        payload: "Hello World from queue".toBytes()
    });
    runtime:sleep(2);
    lock {
        test:assertEquals(topicServiceReceivedMessageCount, 1, "'test-queue' did not received the expected number of messages");
    }
}
