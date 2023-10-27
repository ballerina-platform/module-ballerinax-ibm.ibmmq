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

public type Destination distinct client object {
    remote function put(Message message) returns Error?;

    remote function get(GetMessageOptions options = {}) returns Message|Error?;
};

public type Queue distinct client object {
    *Destination;
};

public client class Topic {
    *Destination;

    remote function put(Message message) returns Error? =
    @java:Method {
        name: "externPut",
        'class: "io.ballerina.lib.ibm.ibmmq.Topic"
    } external;

    remote function get(GetMessageOptions options = {}) returns Message|Error  =
    @java:Method {
        name: "externGet",
        'class: "io.ballerina.lib.ibm.ibmmq.Topic"
    } external;
};

