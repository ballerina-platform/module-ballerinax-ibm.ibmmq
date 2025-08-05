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

import ballerina/log;
import ballerinax/ibm.ibmmq;

configurable string name = ?;
configurable string host = ?;
configurable int port = ?;
configurable string channel = ?;
configurable string userID = ?;
configurable string password = ?;
configurable string queueName = ?;
configurable decimal pollingInterval = ?;

type MessagePayload record {|
    string messageId;
    string content;
|};

isolated map<MessagePayload> processedMessages = {};

listener ibmmq:Listener ibmmqListener = new ({
    name,
    host,
    port,
    channel,
    userID,
    password
});

@ibmmq:ServiceConfig {
    sessionAckMode: ibmmq:SESSION_TRANSACTED,
    queueName,
    pollingInterval
}
service on ibmmqListener {

    remote function onMessage(ibmmq:Message message, ibmmq:Caller caller) returns error? {
        string stringPayload = check string:fromBytes(message.payload);
        log:printInfo("Message received", message = stringPayload);

        MessagePayload payload = check stringPayload.fromJsonStringWithType();
        error? result = processMessage(payload);
        if result is error {
            log:printError("Message processing failed, rolling back", 'error = result);
            check caller->'rollback();
        } else {
            check caller->'commit();
        }
    }
}

isolated function processMessage(MessagePayload payload) returns error? {
    lock {
        if processedMessages.hasKey(payload.messageId) {
            return error("Duplicate message ID received: " + payload.messageId);
        }
        processedMessages[payload.messageId] = payload.cloneReadOnly();
    }
}

