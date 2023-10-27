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

public type QueueManagerConfiguration record {|
    string host;
    int port = 1414;
    string channel;
    string userID?;
    string password?;
|};

public enum ConnectionOpenOptions {
    MQOO_OUTPUT = "MQOO_OUTPUT",
    MQOO_INPUT_AS_Q_DEF = "MQOO_INPUT_AS_Q_DEF",
    MQOO_INPUT_EXCLUSIVE = "MQOO_INPUT_EXCLUSIVE",
    MQOO_INPUT_SHARED = "MQOO_INPUT_SHARED"
}

public type Property record {|
    map<anydata> descriptor;
    boolean|byte|byte[]|decimal|float|int|string property;
|};

public type Message record {|
    map<Property> properties;
    byte[] payload;
|};
