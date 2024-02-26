# Consume messages

This example demonstrates how to consume messages from an IBM MQ queue.

## Prerequisites

### 1. Setup IBM MQ server

Refer to the [Setup Guide](https://dev-central.ballerina.io/ballerinax/ibm.ibmmq/latest#setup-guide) to set up the IBM MQ server locally.

### 2. Configuration

Update IBM MQ related configurations in `Config.toml` in the example directory:

```toml
queueManagerName = "<queue-manager-name>"
host = "<host>"
port = <port>
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
