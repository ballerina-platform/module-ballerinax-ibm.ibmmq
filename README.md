# Ballerina `ibm.ibmmq` Library

[![Build](https://github.com/ballerina-platform/module-ballerinax-ibm.ibmmq/actions/workflows/build-timestamped-master.yml/badge.svg)](https://github.com/ballerina-platform/module-ballerinax-ibm.ibmmq/actions/workflows/build-timestamped-master.yml)
[![codecov](https://codecov.io/gh/ballerina-platform/module-ballerinax-ibm.ibmmq/branch/main/graph/badge.svg)](https://codecov.io/gh/ballerina-platform/module-ballerinax-ibm.ibmmq)
[![Trivy](https://github.com/ballerina-platform/module-ballerinax-ibm.ibmmq/actions/workflows/trivy-scan.yml/badge.svg)](https://github.com/ballerina-platform/module-ballerinax-ibm.ibmmq/actions/workflows/trivy-scan.yml)
[![GitHub Last Commit](https://img.shields.io/github/last-commit/ballerina-platform/module-ballerinax-ibm.ibmmq.svg)](https://github.com/ballerina-platform/module-ballerinax-ibm.ibmmq/commits/main)

[IBM MQ](https://www.ibm.com/products/mq) is a powerful messaging middleware platform designed for facilitating reliable
communication between disparate systems and applications. IBM MQ ensures the secure and orderly exchange of messages
asynchronously, decoupling senders and receivers for efficient and scalable communication. It supports both
point-to-point and publish/subscribe messaging models via queues and topics.

The `ballerinax/ibm.ibmmq` module provides an API to connect to an IBM MQ server using Ballerina.

## Setup guide

To use the Ballerina IBM MQ connector, you need to have an IBM MQ instance running or possess an IBM MQ cloud account.
For setting up IBM MQ locally, you can refer to the [IBM MQ official documentation](https://www.ibm.com/docs/en/ibm-mq/9.3?topic=migrating-installing-uninstalling).
Alternatively, to use IBM MQ on the cloud, [sign up](https://cloud.ibm.com/registration) for an IBM MQ cloud account.

### Create a queue

1. Log into IBM MQ console. If you are running an IBM MQ server locally you can navigate to
   [https://localhost:9443/ibmmq/console](https://localhost:9443/ibmmq/console) URL in your browser to access the IBM MQ console.
2. Click on the `Create a queue` link.

![Create a queue](https://raw.githubusercontent.com/ballerina-platform/module-ballerinax-ibm.ibmmq/main/docs/setup/resources/select-create-queue.png)

3. Select the queue type.

![Select the queue type](https://raw.githubusercontent.com/ballerina-platform/module-ballerinax-ibm.ibmmq/main/docs/setup/resources/select-queue-type.png)

4. Go back to the home page and click on the `Manage` link on the sidebar.

![Click on Manage](https://raw.githubusercontent.com/ballerina-platform/module-ballerinax-ibm.ibmmq/main/docs/setup/resources/click-manage-link.png)

5. Navigate to `Events` tab.

![Navigate to Events tab](https://raw.githubusercontent.com/ballerina-platform/module-ballerinax-ibm.ibmmq/main/docs/setup/resources/navigate-to-events-tab.png)

6. Click on `Create`.

![Click on create](https://raw.githubusercontent.com/ballerina-platform/module-ballerinax-ibm.ibmmq/main/docs/setup/resources/click-on-create.png)

## Quickstart

To use the IBM MQ connector in your Ballerina application, modify the .bal file as follows:

### Step 1: Import the connector

Import `ballerinax/ibm.ibmmq` package into your Ballerina project.

```ballerina
import ballerinax/ibm.ibmmq;
```

### Step 2: Instantiate a new connector

Create an `ibmmq:QueueManager` instance by giving IBM MQ configuration.

```ballerina
configurable string queueManagerName = ?;
configurable string host = ?;
configurable int port = ?;
configurable string channel = ?;
configurable string userID = ?;
configurable string password = ?;

ibmmq:QueueManager queueManager = check new (
    name = queueManagerName, host = host, channel = channel, userID = userID, password = password
);
```

Create an `ibmmq:Queue` or `ibmmq:Topic` using the `ibmmq:QueueManager` instance with relevant configurations.

```ballerina
configurable string queueName = ?;

ibmmq:Queue queue = check queueManager.accessQueue(queueName, ibmmq:MQOO_OUTPUT | ibmmq:MQOO_INPUT_AS_Q_DEF);
```

Create an `ibmmq:Topic` using the `ibmmq:QueueManager` instance with relevant configurations.

```ballerina
configurable string topicName = ?;
configurable string topicString = ?;

ibmmq:Topic topic = check queueManager.accessTopic(
    topicName, topicString, ibmmq:MQOO_OUTPUT | ibmmq:MQOO_INPUT_AS_Q_DEF
);
```

### Step 3: Invoke the connector operations

Now, utilize the available connector operations.

#### Produce message to an IBM MQ queue

```ballerina
check queue->put({
    payload: "This is a sample message to IBM MQ queue".toBytes()
});
```

#### Produce message to an IBM MQ queue

```ballerina
check topic->put({
    payload: "This is a sample message to IBM MQ topic".toBytes()
});
```

#### Retrieve messages from an IBM MQ queue

```ballerina
ibmmq:Message? message = check queue->get();
```

#### Retrieve messages from an IBM MQ topic

```ballerina
ibmmq:Message? message = check topic->get();
```

### Examples

The following example shows how to use the `ibm.ibmmq` connector to produce and consume messages using an IBM MQ server.

## Issues and projects 

Issues and Projects tabs are disabled for this repository as this is part of the Ballerina Standard Library. To report bugs, request new features, start new discussions, view project boards, etc., go to the [Ballerina Standard Library parent repository](https://github.com/ballerina-platform/ballerina-standard-library). 

This repository only contains the source code for the library.

## Build from the source

### Set up the prerequisites

* Download and install Java SE Development Kit (JDK) version 17 (from one of the following locations).

   * [Oracle](https://www.oracle.com/java/technologies/downloads/)

   * [OpenJDK](https://adoptium.net/)

        > **Note:** Set the JAVA_HOME environment variable to the path name of the directory into which you installed JDK.

2. Download and install [Docker](https://www.docker.com/). This is required to run the tests.

### Build the source

Execute the commands below to build from the source.

1. To build the library:
   ```    
   ./gradlew clean build
   ```

2. To run the tests:
   ```
   ./gradlew clean test
   ```
3. To build the library without the tests:
   ```
   ./gradlew clean build -x test
   ```
4. To debug library implementation:
   ```
   ./gradlew clean build -Pdebug=<port>
   ```
5. To debug the library with Ballerina language:
   ```
   ./gradlew clean build -PbalJavaDebug=<port>
   ```
6. Publish ZIP artifact to the local `.m2` repository:
   ```
   ./gradlew clean build publishToMavenLocal
   ```
7. Publish the generated artifacts to the local Ballerina central repository:
   ```
   ./gradlew clean build -PpublishToLocalCentral=true
   ```
8. Publish the generated artifacts to the Ballerina central repository:
   ```
   ./gradlew clean build -PpublishToCentral=true
   ```

## Contribute to Ballerina

As an open source project, Ballerina welcomes contributions from the community. 

For more information, go to the [contribution guidelines](https://github.com/ballerina-platform/ballerina-lang/blob/master/CONTRIBUTING.md).

## Code of conduct

All the contributors are encouraged to read the [Ballerina Code of Conduct](https://ballerina.io/code-of-conduct).

## Useful links

* For more information go to the [`ibm.ibmmq` library](https://lib.ballerina.io/ballerinax/ibm.ibmmq/latest).
* For example demonstrations of the usage, go to [Ballerina By Examples](https://ballerina.io/learn/by-example/).
* Chat live with us via our [Discord server](https://discord.gg/ballerinalang).
* Post all technical questions on Stack Overflow with the [#ballerina](https://stackoverflow.com/questions/tagged/ballerina) tag.
