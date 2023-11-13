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
    groups: ["secureSocket"]
}
function basicPublisherSubscriberSecureSocketTest() returns error? {
    QueueManager queueManager = check new (
        name = "QM2", 
        host = "localhost",
        port = 1415, 
        channel = "DEV.APP.SVRCONN",
        sslCipherSuite = TLS12ORHIGHER,
        secureSocket = {
            cert: "./tests/resources/secrets/server.crt",
            'key: {
                certFile: "./tests/resources/secrets/client.crt",
                keyFile: "./tests/resources/secrets/client.key"
            }
        }
    );
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
    groups: ["secureSocket"]
}
function basicQueueProducerConsumerSecureSocketTest() returns error? {
    QueueManager queueManager = check new (
        name = "QM2",
        host = "localhost",
        port = 1415,
        channel = "DEV.APP.SVRCONN",
        sslCipherSuite = TLS12ORHIGHER,
        secureSocket = {
            cert: "./tests/resources/secrets/server.crt",
            'key: {
                certFile: "./tests/resources/secrets/client.crt",
                keyFile: "./tests/resources/secrets/client.key"
            }
        }
    );
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
