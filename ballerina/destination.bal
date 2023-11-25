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

import ballerina/jballerina.java;

# IBM MQ destination client type.
public type Destination distinct client object {
    remote function put(Message message) returns Error?;

    remote function get(*GetMessageOptions getMessageOptions) returns Message|Error?;

    remote function close() returns Error?;
};

# IBM MQ Queue client.
public isolated client class Queue {
    *Destination;

    # Puts a message to an IBM MQ queue.
    # 
    # + message - IBM MQ message
    # + return - An `ibmmq:Error` if the operation fails or else `()`
    isolated remote function put(Message message) returns Error? =
    @java:Method {
        'class: "io.ballerina.lib.ibm.ibmmq.Queue"
    } external;

    # Retrieves a message from an IBM MQ queue.
    # 
    # + getMessageOptions - Options to control message retrieval
    # + return - An `ibmmq:Message` if there is a message in the queue, `()` if there 
    #           is no message or else `ibmmq:Error` if the operation fails
    isolated remote function get(*GetMessageOptions getMessageOptions) returns Message|Error? =
    @java:Method {
        'class: "io.ballerina.lib.ibm.ibmmq.Queue"
    } external;

    # Closes the IBM MQ queue object. No further operations on this object are permitted after it is closed.
    # 
    # + return - An `ibmmq:Error` if the operation fails or else `()`
    isolated remote function close() returns Error? =
    @java:Method {
        'class: "io.ballerina.lib.ibm.ibmmq.Queue"
    } external;
}

# IBM MQ Topic client.
public isolated client class Topic {
    *Destination;

    # Puts a message to an IBM MQ topic.
    # 
    # + message - IBM MQ message
    # + return - An `ibmmq:Error` if the operation fails or else `()`
    isolated remote function put(Message message) returns Error? =
    @java:Method {
        'class: "io.ballerina.lib.ibm.ibmmq.Topic"
    } external;

    # Retrieves a message from an IBM MQ topic.
    # 
    # + getMessageOptions - Options to control message retrieval
    # + return - An `ibmmq:Message` if there is a message in the topic, `()` if there 
    #           is no message or else `ibmmq:Error` if the operation fails
    isolated remote function get(*GetMessageOptions getMessageOptions) returns Message|Error?  =
    @java:Method {
        'class: "io.ballerina.lib.ibm.ibmmq.Topic"
    } external;

    # Closes the IBM MQ topic object. No further operations on this object are permitted after it is closed.
    # 
    # + return - An `ibmmq:Error` if the operation fails or else `()`
    isolated remote function close() returns Error? =
    @java:Method {
        'class: "io.ballerina.lib.ibm.ibmmq.Topic"
    } external;
};
