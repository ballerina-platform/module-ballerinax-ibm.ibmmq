# Ballerina `ibm.ibmmq` Library

[![Build](https://github.com/ballerina-platform/module-ballerinax-ibm.ibmmq/actions/workflows/build-timestamped-master.yml/badge.svg)](https://github.com/ballerina-platform/module-ballerinax-ibm.ibmmq/actions/workflows/build-timestamped-master.yml)
[![codecov](https://codecov.io/gh/ballerina-platform/module-ballerinax-ibm.ibmmq/branch/main/graph/badge.svg)](https://codecov.io/gh/ballerina-platform/module-ballerinax-ibm.ibmmq)
[![Trivy](https://github.com/ballerina-platform/module-ballerinax-ibm.ibmmq/actions/workflows/trivy-scan.yml/badge.svg)](https://github.com/ballerina-platform/module-ballerinax-ibm.ibmmq/actions/workflows/trivy-scan.yml)
[![GitHub Last Commit](https://img.shields.io/github/last-commit/ballerina-platform/module-ballerinax-ibm.ibmmq.svg)](https://github.com/ballerina-platform/module-ballerinax-ibm.ibmmq/commits/main)

[IBM MQ](https://www.ibm.com/products/mq) is a powerful messaging middleware platform designed for facilitating reliable
communication between disparate systems and applications. IBM MQ ensures the secure and orderly exchange of messages
asynchronously, decoupling senders and receivers for efficient and scalable communication. It supports both
point-to-point and publish/subscribe messaging models via queues and topics.

The `ballerinax/ibm.ibmmq` package provides an API to connect to an IBM MQ server using Ballerina. The current connector is compatible with IBM MQ server versions up to 9.3.

## Setup guide

To use the Ballerina IBM MQ connector, you need to have an IBM MQ instance running or possess an IBM MQ cloud account.
For setting up IBM MQ locally, you can refer to the [IBM MQ official documentation](https://www.ibm.com/docs/en/ibm-mq/9.3?topic=migrating-installing-uninstalling).
Alternatively, to use IBM MQ on the cloud, [sign up](https://cloud.ibm.com/registration) for an IBM MQ cloud account.

### Create a queue

1. Log into IBM MQ console. If you are running an IBM MQ server locally you can navigate to `https://<host>:<port>/ibmmq/console` URL in your browser to access the IBM MQ console.

2. Click on the `Create a queue` link.

    <img src=https://raw.githubusercontent.com/ballerina-platform/module-ballerinax-ibm.ibmmq/main/docs/setup/resources/select-create-queue.png alt="Create a queue" style="width: 70%;">

3. Select the queue type.

    <img src=https://raw.githubusercontent.com/ballerina-platform/module-ballerinax-ibm.ibmmq/main/docs/setup/resources/select-queue-type.png alt="Select the queue type" style="width: 70%;">

### Create a topic

1. Go back to the home page and click on the `Manage` link on the sidebar.

    <img src=https://raw.githubusercontent.com/ballerina-platform/module-ballerinax-ibm.ibmmq/main/docs/setup/resources/click-manage-link.png alt="Click on manage" style="width: 70%;">

2. Navigate to `Events` tab.

    <img src=https://raw.githubusercontent.com/ballerina-platform/module-ballerinax-ibm.ibmmq/main/docs/setup/resources/navigate-to-events-tab.png alt="Navigate to events tab" style="width: 70%;">

3. Click on `Create`.

    <img src=https://raw.githubusercontent.com/ballerina-platform/module-ballerinax-ibm.ibmmq/main/docs/setup/resources/click-on-create.png alt="Click on create" style="width: 70%;">

## Quickstart

To use the IBM MQ connector in your Ballerina application, modify the `.bal` file as follows:

### Step 1: Import the connector

Import `ballerinax/ibm.ibmmq` module into your Ballerina project.

```ballerina
import ballerinax/ibm.ibmmq;
```

### Step 2: Add IBM MQ driver

Add `com.ibm.mq.allclient` as a platform dependency to the `Ballerina.toml`.

```toml
[[platform.java21.dependency]]
groupId = "com.ibm.mq"
artifactId = "com.ibm.mq.allclient"
version = "9.3.4.0"
```

### Step 3: Instantiate a new connector

Create an `ibmmq:QueueManager` instance by giving IBM MQ configuration.

```ballerina
configurable string queueManagerName = ?;
configurable string host = ?;
configurable int port = ?;
configurable string channel = ?;
configurable string userID = ?;
configurable string password = ?;

ibmmq:QueueManager queueManager = check new (
    name = queueManagerName, host = host, port = port, channel = channel, userID = userID, password = password
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

### Step 4: Invoke the connector operations

Now, utilize the available connector operations.

#### Produce messages to an IBM MQ queue

```ballerina
check queue->put({
    payload: "This is a sample message to IBM MQ queue".toBytes()
});
```

#### Produce messages to an IBM MQ topic

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

### Step 5: Run the Ballerina application

```Shell
bal run
```

### Examples

The following example shows how to use the `ibm.ibmmq` connector to produce and consume messages using an IBM MQ server.

1. [Produce messages](https://github.com/ballerina-platform/module-ballerinax-ibm.ibmmq/tree/main/examples/produce-messages) - Produce messages to an IBM MQ queue.

2. [Consume messages](https://github.com/ballerina-platform/module-ballerinax-ibm.ibmmq/tree/main/examples/consume-messages) - Consume messages from an IBM MQ queue.

3. [Securing IBM MQ client](https://github.com/ballerina-platform/module-ballerinax-ibm.ibmmq/tree/main/examples/ibmmq-client-security) - Initiate secure communication between an IBM MQ client and an IBM MQ server.

4. [Produce MQIIH headers](https://github.com/ballerina-platform/module-ballerinax-ibm.ibmmq/tree/main/examples/produce-mqiih-headers) - Produce IBM MQ messages to an IBM MQ queue with the MQIIH headers.

5. [Consume MQIIH headers](https://github.com/ballerina-platform/module-ballerinax-ibm.ibmmq/tree/main/examples/consume-mqiih-headers) - Consume messages with the MQIIH header from an IBM MQ queue.

6. [Produce MQRFH2 headers](https://github.com/ballerina-platform/module-ballerinax-ibm.ibmmq/tree/main/examples/produce-mqrfh2-headers) - Produce IBM MQ messages to an IBM MQ queue with the MQRFH2 headers.

7. [Consume MQIIH headers](https://github.com/ballerina-platform/module-ballerinax-ibm.ibmmq/tree/main/examples/consume-mqrfh2-headers) - Consume messages with the MQRFH2 header from an IBM MQ queue.

8. [Consume messages using a service](https://github.com/ballerina-platform/module-ballerinax-ibm.ibmmq/tree/main/examples/consume-messages-service) - Consume messages from an IBM MQ queue using a Ballerina service.

## Issues and projects

Issues and Projects tabs are disabled for this repository as this is part of the Ballerina Standard Library. To report bugs, request new features, start new discussions, view project boards, etc., go to the [Ballerina Standard Library parent repository](https://github.com/ballerina-platform/ballerina-standard-library).

This repository only contains the source code for the library.

## Build from the source

### Set up the prerequisites

1. Download and install Java SE Development Kit (JDK) version 21 (from one of the following locations).

   * [Oracle](https://www.oracle.com/java/technologies/downloads/)
   * [OpenJDK](https://adoptium.net/)

        > **Note:** Set the JAVA_HOME environment variable to the path name of the directory into which you installed JDK.

2. Generate a Github access token with read package permissions, then set the following `env` variables:
    ```sh
   export packageUser=<Your GitHub Username>
   export packagePAT=<GitHub Personal Access Token>
    ```

3. Download and install [Docker](https://www.docker.com/). This is required to run the tests.

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
5. To run a group of tests:
    ```
    ./gradlew clean test -Pgroups=<test_group_names>
    ```
6. To debug the library with Ballerina language:
   ```
   ./gradlew clean build -PbalJavaDebug=<port>
   ```
7. Publish ZIP artifact to the local `.m2` repository:
   ```
   ./gradlew clean build publishToMavenLocal
   ```
8. Publish the generated artifacts to the local Ballerina central repository:
   ```
   ./gradlew clean build -PpublishToLocalCentral=true
   ```
9. Publish the generated artifacts to the Ballerina central repository:
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
