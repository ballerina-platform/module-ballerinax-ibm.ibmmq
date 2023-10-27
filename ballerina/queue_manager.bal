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

public isolated class QueueManager {

    public isolated function init(*QueueManagerConfiguration configurations) returns Error? {
        check self.externInit(configurations);
    }

    isolated function externInit(QueueManagerConfiguration configurations) returns Error? = @java:Method {
        name: "init",
        'class: "io.ballerina.lib.ibm.ibmmq.QueueManager"
    } external;

    public isolated function accessQueue(string queueName, ConnectionOpenOptions options) returns Queue|Error {
        return error Error("Not implemented");
    }

    public isolated function accessTopic(string topicName, string topicString, ConnectionOpenOptions options) returns Topic|Error {
        return error Error("Not implemented");
    }
}
