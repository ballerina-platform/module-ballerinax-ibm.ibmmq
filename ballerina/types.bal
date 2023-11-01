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

# Options which can be provided when opening an IBM MQ topic.
public type OPEN_TOPIC_OPTION OPEN_AS_SUBSCRIPTION|OPEN_AS_PUBLICATION;

# IBM MQ queue manager configurations.
#
# + name - Name of the queue manager
# + host - IBM MQ server host
# + port - IBM MQ server port 
# + channel - IBM MQ channel  
# + userID - IBM MQ userId  
# + password - IBM MQ user password
public type QueueManagerConfiguration record {|
    string name;
    string host;
    int port = 1414;
    string channel;
    string userID?;
    string password?;
|};

# IBM MQ get message options.
#
# + options - Get message option 
# + waitInterval - The maximum time (in seconds) that a `get` call waits for a suitable message to 
#                   arrive. It is used in conjunction with `ibmmq.MQGMO_WAIT`.
public type GetMessageOptions record {|
    int options = MQGMO_NO_WAIT;
    int waitInterval = 10;
|};

# Represents an IBM MQ message property.
#
# + descriptor - Property descriptor  
# + value - Property value
public type Property record {|
    map<int> descriptor?;
    boolean|byte|byte[]|decimal|float|int|string value;
|};

# Represents an IBM MQ message.
#
# + properties - Message properties
# + payload - Message payload
public type Message record {|
    map<Property> properties?;
    byte[] payload;
|};
