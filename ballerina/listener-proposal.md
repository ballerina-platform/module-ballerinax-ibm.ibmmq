# Proposal: Listener Support for IBMMQ

- Authors
    - Thisaru Guruge
- Reviewed By
    - Danesh Kuruppu
- Created Date
    - 2025-06-04
- Issue
    - []()
- State
    - Submitted

## Summary

The Current IBMMQ connector uses the pull model where the user has to poll the messages from the queue. Although this can be used in some cases, it does not align with the existing Ballerina listener-service model. Therefore, this proposal aims to introduce a listener to the IBMMQ connector where the user can consume messages seemingly like a push model.

## Goals

- Introduce a `listener` to the IBMMQ connector
- Support listener-service model in the IBMMQ connector

## Motivation

The IBMMQ connector currently supports the pull model, where the user has to poll the messages from the queue with a desired intervals. This requires writing a main function including a loop and a sleep. Although this works, it does not align with the existing Ballerina listener-service model, where the developers can write a service and _listen_ to the messages. There are queries on this in the Ballerina community, and this proposal aims to address that.

## Description

This section describes the proposed changes to the IBMMQ connector to introduce a listener.

### Listener

The `ibmmq:Listener` type will be introduced to the IBMMQ connector, which can be used to connect with the IBMMQ server. The `ibmmq:Listener` is bound to a specific queue manager and can be used to attach multiple services to it. Therefore, the existing `ibmmq:QueueManagerConfiguration` is used to configure the listener. Then the listener will handle the connection to the queue manager and will invoke the attached services when messages are received from a queue or a topic.

Following is the definition of the `ibmmq:Listener` type.

```ballerina
# Represents an IBMMQ Listener endpoint that can be used to receive messages from an IBMMQ queue.
public isolated class Listener {
    # Initializes the IBMMQ listener.
    # + configurations - The configurations to be used when initializing the IBMMQ listener
    # + return - An error if the initialization failed, nil otherwise
    public isolated function init(*QueueManagerConfiguration configurations) returns Error? {

    }

    # Attaches an IBMMQ service to the IBMMQ listener.
    # + s - The IBMMQ Service to attach
    # + name - The name of the queue to attach to
    # + return - An error if the attaching failed, nil otherwise
    public isolated function attach(Service s, string[]|string? name = ()) returns Error? {

    }

    # Detaches an IBMMQ service from the IBMMQ listener.
    # + s - The IBMMQ Service to detach
    # + return - An error if the detaching failed, nil otherwise
    public isolated function detach(Service s) returns Error? {

    }

    # Starts the IBMMQ listener.
    # + return - An error if the starting failed, nil otherwise
    public isolated function 'start() returns Error? {

    }

    # Gracefully stops the IBMMQ listener.
    # + return - An error if the stopping failed, nil otherwise
    public isolated function gracefulStop() returns Error? {

    }

    # Immediately stops the IBMMQ listener.
    # + return - An error if the stopping failed, nil otherwise
    public isolated function immediateStop() returns Error? {

    }
}
```

### Service

The `ibmmq:Service` type will be introduced to the IBMMQ connector, which can be used to define a service that can be attached to the IBMMQ listener. The service is bound to a specific topic or a queue and will be invoked when messages are received from that topic or queue. The service can define remote methods to handle the messages and errors.

Following is the definition of the `ibmmq:Service` type.

```ballerina
# Represents an IBMMQ service object that can be attached to an `ibmmq:Listener`.
public type Service distinct service object {};
```

The `ibmmq:Service` type will include two main remote methods:

* `onMessage` - This method will be invoked when a message is received from the IBMMQ queue.
* `onError` - This method will be invoked when an error occurs while receiving messages from the IBMMQ queue.

To validate the service type, a compiler plugin should be introduced, but at the first iteration, this validation can be done at runtime.

#### Service Configuration

A service configuration annotation will be introduced to configure the service to configure the queue or the topic to which the service is bound. The service configuration annotation will be used to annotate the service type.

```ballerina
# Configuration for an IBM MQ queue.
#
# + queueName - The name of the queue to consume messages from.
# + options - The options which control the opening of the queue.
public type QueueConfig record {|
    string queueName;
    int options = MQOO_INPUT_AS_Q_DEF;
|};

# Configuration for an IBM MQ topic subscription.
#
# + topicName - The name of the topic to subscribe to.
# + subscriptionName - The name of the subscription. This is required only if the durable flag is set to `true`.
# + durable - Indicates whether the subscription is durable. Set this to `false` to stop receiving messages sent to the
# topic/queue while the listener is offline.
# + options - Options to control message retrieval.
# + matchOptions - Message selection criteria
public type TopicConfig record {|
    string topicName;
    string? subscriptionName = ();
    boolean durable = true;
    int options;
    MatchOptions matchOptions?;
|};

# The service configuration type for the `ibmmq:Service`.
#
# + config - The topic or queue configuration to subscribe to.
public type ServiceConfigType record {|
    QueueConfig|TopicConfig config;
|};

# Annotation to configure the `ibmmq:Service`.
public annotation ServiceConfigType ServiceConfig on service;
```

Since a service can be bound to either a queue or a topic, the `ServiceConfigType` will include a single field `config` that can be either a `QueueConfig` or a `TopicConfig`. The user can use the `ServiceConfigType` to annotate the service type.


### Example Usage

Following are some example usages of the `ibmmq:Listener` and `ibmmq:Service` types.

#### Subscribe to a Queue

```ballerina
import ballerinax/ibm.ibmmq;

configurable string queueManagerName = ?;
configurable string host = ?;
configurable int port = ?;
configurable string channel = ?;
configurable string userID = ?;
configurable string password = ?;
configurable string queueName = ?;

listener ibmmq:Listener queueManager = new({
    name: queueManagerName,
    host,
    port,
    channel,
    userID,
    password
});

@ibmmq:ServiceConfig {
    config: {
        name: queueName
    }
}
service ibmmq:Service on queueManager {
    remote function onMessage(ibmmq:Message message) returns error? {
        // Handle the received message
    }
}
 ```

 #### Subscribe to a Topic

 ```ballerina
 import ballerinax/ibm.ibmmq;

 configurable string queueManagerName = ?;
configurable string host = ?;
configurable int port = ?;
configurable string channel = ?;
configurable string userID = ?;
configurable string password = ?;
configurable string topicName = ?;
configurable string subscriptionName = ?;
configurable boolean durable = ?;

 listener ibmmq:Listener queueManager = new({
    name: queueManagerName,
    host,
    port,
    channel,
    userID,
    password
 });

 @ibmmq:ServiceConfig {
    config: {
        topicName,
        subscriptionName,
        durable
    }
}
service ibmmq:Service on queueManager {
    remote function onMessage(ibmmq:Message message) returns error? {
        // Handle the received message
    }
}
```

#### Handling Errors

```ballerina
import ballerina/log;
import ballerinax/ibm.ibmmq;

configurable string queueManagerName = ?;
configurable string host = ?;
configurable int port = ?;
configurable string channel = ?;
configurable string userID = ?;
configurable string password = ?;

listener ibmmq:Listener queueManager = new({
    name: queueManagerName,
    host,
    port,
    channel,
    userID,
    password
});

@ibmmq:ServiceConfig {
    config: {
        queueName
    }
}
service ibmmq:Service on queueManager {
    remote function onMessage(ibmmq:Message message) returns error? {
        // Handle the received message
    }

    remote function onError(error err) returns error? {
        log:printError("Error occurred while receiving messages from the queue", err);
    }
}
```
