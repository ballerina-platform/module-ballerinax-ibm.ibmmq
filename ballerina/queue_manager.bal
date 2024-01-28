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

# Represents an IBM MQ queue manager.
public isolated class QueueManager {

    # Initialize an IBM MQ queue manager.
    # ```ballerina
    # ibmmq:QueueManager queueManager = check new(name = "QM1", host = "localhost", channel = "DEV.APP.SVRCONN");
    # ```
    #
    # + configurations - The configurations to be used when initializing the IBM MQ queue manager
    # + return - The `ibmmq:QueueManager` or an `ibmmq:Error` if the initialization failed
    public isolated function init(*QueueManagerConfiguration configurations) returns Error? {
        check self.externInit(configurations);
    }

    isolated function externInit(QueueManagerConfiguration configurations) returns Error? = @java:Method {
        name: "init",
        'class: "io.ballerina.lib.ibm.ibmmq.QueueManager"
    } external;

    # Establishes access to an IBM MQ queue on this queue manager.
    # ```ballerina
    # ibmmq:Queue queue = check queueManager.accessQueue("queue1", ibmmq:MQOO_OUTPUT);
    # ```
    # 
    # + queueName - Name of the queue
    # + options - The options which control the opening of the queue
    # + return - The `ibmmq:Queue` object or an `ibmmq:Error` if the operation failed
    public isolated function accessQueue(string queueName, int options) returns Queue|Error =
    @java:Method {
        'class: "io.ballerina.lib.ibm.ibmmq.QueueManager"
    } external;

    # Establishes access to an IBM MQ topic on this queue manager.
    # ```ballerina
    # ibmmq:Topic topic = check queueManager.accessTopic(
    #   "dev", "DEV.BASE.TOPIC", ibmmq:OPEN_AS_PUBLICATION, ibmmq:MQOO_OUTPUT
    # );
    # ```
    # 
    # + topicName - The topic string to publish or subscribe against
    # + topicString - The name of the topic object as defined on the local queue manager
    # + openTopicOption - Indicates whether the topic is being opened for either publication or subscription
    # + options - Options that control the opening of the topic for either publication or subscription
    # + return - The `ibmmq:Topic` object or an `ibmmq:Error` if the operation failed
    public isolated function accessTopic(string topicName, string topicString, OPEN_TOPIC_OPTION openTopicOption,
            int options) returns Topic|Error =
    @java:Method {
        'class: "io.ballerina.lib.ibm.ibmmq.QueueManager"
    } external;

    # Ends the connection to the IBM MQ queue manager.
    #
    # + return - An `ibmmq:Error` if the operation failed
    public isolated function disconnect() returns Error? =
    @java:Method {
        'class: "io.ballerina.lib.ibm.ibmmq.QueueManager"
    } external;
}
