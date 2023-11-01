## Overview

The `ballerinax/ibm.ibmmq` module provides an API to connect to an IBM MQ server using Ballerina.

This module is created with minimal deviation from the IBM MQ java client API to make it easy for the developers who are used to working with the IBM MQ java client.
 
Currently, the following IBM MQ API Classes are supported through this package.

- QueueManager
- Queue
- Destination (Queue, Topic)
- Message

### IBM MQ queue

IBM MQ queues are used for point-to-point messaging. In point-to-point messaging, a producer application sends a message to a queue, and a single consumer application retrieves the message from the queue.

#### Producer

```ballerina
import ballerinax/ibm.ibmmq;

public function main() returns error? {
    ibmmq:QueueManager queueManager = check new (
        name = "QM1", host = "localhost", channel = "DEV.APP.SVRCONN"
    );
    ibmmq:Queue producer = check queueManager.accessQueue("DEV.QUEUE.1", ibmmq:MQOO_OUTPUT);
    check producer->put({
        payload: "This is a sample message to IBM MQ queue".toBytes()
    });
    check producer->close();
    check queueManager.disconnect();
}
```

#### Consumer

```ballerina
import ballerinax/ibm.ibmmq;
import ballerina/io;

public function main() returns error? {
    ibmmq:QueueManager queueManager = check new (
        name = "QM1", host = "localhost", channel = "DEV.APP.SVRCONN"
    );
    ibmmq:Queue consumer = check queueManager.accessQueue(
            "DEV.QUEUE.1", ibmmq:MQOO_INPUT_AS_Q_DEF);
    while true {
        ibmmq:Message? message = check consumer->get(options = ibmmq:MQGMO_WAIT);
        if message is () {
            continue;
        }
        io:println(string:fromBytes(message.payload));
    }
}
```

### IBM MQ topic

IBM MQ topics are used in the publish/subscribe (pub/sub) messaging model. In pub/sub, publishers send messages to topics, and subscribers subscribe to topics. When a publisher sends a message to a topic, the queue manager delivers the message to all subscribers that are subscribed to that topic.

#### Publisher

```ballerina
import ballerinax/ibm.ibmmq;

public function main() returns error? {
    ibmmq:QueueManager queueManager = check new (
        name = "QM1", host = "localhost", channel = "DEV.APP.SVRCONN"
    );
    ibmmq:Topic publisher = check queueManager.accessTopic(
        "dev", "DEV.BASE.TOPIC", ibmmq:OPEN_AS_PUBLICATION, ibmmq:MQOO_OUTPUT);
    check publisher->put({
        payload: "This is a sample message to IBM MQ topic".toBytes()
    });
    check publisher->close();
    check queueManager.disconnect();
}
```

#### Subscriber

```ballerina
import ballerinax/ibm.ibmmq;
import ballerina/io;

public function main() returns error? {
    ibmmq:QueueManager queueManager = check new(
        name = "QM1", host = "localhost", channel = "DEV.APP.SVRCONN");
    ibmmq:Topic subscriber = check queueManager.accessTopic(
        "dev", "DEV.BASE.TOPIC", ibmmq:OPEN_AS_SUBSCRIPTION, ibmmq:MQSO_CREATE);
    while true {
        ibmmq:Message? message = check subscriber->get(options = ibmmq:MQGMO_WAIT);
        if message is () {
            continue;
        }
        io:println(string:fromBytes(message.payload));
    }
}
```
