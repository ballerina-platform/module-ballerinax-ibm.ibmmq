# Consume messages

This example demonstrates how to consume messages from an IBM MQ queue using a Ballerina service.

## Prerequisites

### 1. Setup IBM MQ server

Refer to the [Setup Guide](https://dev-central.ballerina.io/ballerinax/ibm.ibmmq/latest#setup-guide) to set up the IBM MQ server locally.

### 2. IBM MQ dependencies

Add the following dependency to your `Ballerina.toml` to include the IBM MQ driver to the package.

```toml
[[platform.java21.dependency]]
groupId = "com.ibm.mq"
artifactId = "com.ibm.mq.allclient"
version = "9.4.1.0"
```

### 3. Configuration

Update IBM MQ related configurations in `Config.toml` in the example directory:

```toml
queueManagerName = "<queue-manager-name>"
host = "<host>"
port = "<port>"
channel = "<ibm-mq-channel>"
userID = "<user-ID>"
password = "<password>"
queueName = "<queue-name>"
```

## Run the Example

Execute the following command to run the example:

```bash
bal run
```
