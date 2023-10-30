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

public type GM_OPTIONS MQGMO_WAIT|MQGMO_NO_WAIT|MQGMO_SYNCPOINT|MQGMO_NO_SYNCPOINT|MQGMO_BROWSE_FIRST|MQGMO_BROWSE_MSG_UNDER_CURSOR|MQGMO_MSG_UNDER_CURSOR|MQGMO_LOCK|MQGMO_UNLOCK|MQGMO_ACCEPT_TRUNCATED_MSG|MQGMO_BROWSE_NEXT|MQGMO_ACCEPT_TRUNCATED_MSG|MQGMO_FAIL_IF_QUIESCING|MQGMO_CONVERT;

public type OPEN_TOPIC_OPTION OPEN_AS_SUBSCRIPTION|OPEN_AS_PUBLICATION;

public type QueueManagerConfiguration record {|
    string name;
    string host;
    int port = 1414;
    string channel;
    string userID?;
    string password?;
|};

public type AccessQueueOptions MQOO_OUTPUT|MQOO_BROWSE|MQOO_INPUT_AS_Q_DEF|MQOO_INPUT_EXCLUSIVE|MQOO_INPUT_SHARED;
public type AccessTopicOptions MQOO_ALTERNATE_USER_AUTHORITY|MQOO_BIND_AS_Q_DEF|MQOO_FAIL_IF_QUIESCING|MQOO_OUTPUT|MQOO_PASS_ALL_CONTEXT|MQOO_PASS_IDENTITY_CONTEXT|MQOO_SET_ALL_CONTEXT|MQOO_SET_IDENTITY_CONTEXT|MQSO_CREATE;

public type GetMessageOptions record {|
    GM_OPTIONS gmOptions = MQGMO_NO_WAIT;
    int waitInterval = 10;    
|};

public type Property record {|
    map<int> descriptor?;
    boolean|byte|byte[]|decimal|float|int|string value;
|};

public type Message record {|
    map<Property> properties?;
    byte[] payload;
|};
