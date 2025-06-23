#!/bin/bash

echo "=== Applying IBM MQ Topic Configuration ==="
echo "Script started at $(date)"

# Get the queue manager name from environment variable
QMGR_NAME=${MQ_QMGR_NAME:-QM1}
echo "Detected queue manager: $QMGR_NAME"

# Wait for IBM MQ to be ready by polling the queue manager
echo "Waiting for IBM MQ to be ready..."
until echo "DISPLAY QMGR" | runmqsc $QMGR_NAME > /dev/null 2>&1; do
    echo "IBM MQ not ready yet, waiting..."
    sleep 5
done

echo "IBM MQ is ready!"

# Check if MQSC config file exists and apply it
if [ -f "/etc/configs/mqconfig.mqsc" ]; then
    echo "Applying MQSC configuration file..."
    runmqsc $QMGR_NAME < /etc/configs/mqconfig.mqsc
    echo "MQSC configuration applied."
else
    echo "No MQSC config file found, applying basic topic configuration..."
    echo "DEFINE TOPIC(DEV.TOPIC.1) TOPICSTR('DEV.TOPIC.1') TYPE(LOCAL)" | runmqsc $QMGR_NAME
    echo "DEFINE TOPIC(DEV.TOPIC.2) TOPICSTR('DEV.TOPIC.2') TYPE(LOCAL)" | runmqsc $QMGR_NAME
    echo "Setting authority record for app user..."
    echo "SET AUTHREC OBJTYPE(TOPIC) PROFILE('DEV.TOPIC.1') PRINCIPAL('app') AUTHADD(ALL)" | runmqsc $QMGR_NAME
    echo "SET AUTHREC OBJTYPE(TOPIC) PROFILE('DEV.TOPIC.2') PRINCIPAL('app') AUTHADD(ALL)" | runmqsc $QMGR_NAME
fi

echo "Configuration applied successfully to $QMGR_NAME at $(date)!"
