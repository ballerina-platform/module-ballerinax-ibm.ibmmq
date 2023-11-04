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

# Header types that are provided in the IBM MQ message.
public type Header MQRFH2;

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
# arrive. It is used in conjunction with `ibmmq.MQGMO_WAIT`.
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
    boolean|byte|byte[]|float|int|string value;
|};

# Represents an IBM MQ message.
#
# + properties - Message properties  
# + headers - Headers to be sent in the message  
# + format - Format associated with the header
# + messageId - Message identifier
# + correlationId - Correlation identifier
# + expiry - Message lifetime
# + priority - Message priority
# + persistence - Message persistence
# + messageType - Message type
# + putApplicationType - Type of application that put the message
# + replyToQueueName - Name of reply queue
# + replyToQueueManagerName - Name of reply queue manager
# + payload - Message payload
public type Message record {|
    map<Property> properties?;
    Header[] headers?;
    string format?;
    byte[] messageId?;
    byte[] correlationId?;
    int expiry?;
    int priority?;
    int persistence?;
    int messageType?;
    int putApplicationType?;
    string replyToQueueName?;
    string replyToQueueManagerName?;
    byte[] payload;
|};

# Header record representing the MQRFH2 structure.
#
# + flags - Flag of the header
# + folderStrings - Contents of the variable part of the structure
# + nameValueCCSID - Coded character set for the NameValue data
# + nameValueData - NameValueData variable-length field
# + strucId - Structure identifier
# + strucLength - Length of the structure
# + version - Structure version number
# + fieldValues - Table containing all occurrences of field values matching 
#                 the specified field name in the folder
public type MQRFH2 record {|
    int flags = 0;
    string[] folderStrings = [];
    int nameValueCCSID = 0;
    byte[] nameValueData = [];
    string strucId = "RFH ";
    int strucLength = 36;
    int version = 2;
    table<MQRFH2Field> key(folder, 'field) fieldValues = table [];
|};

# Record defining a field in the MQRFH2 record.
#
# + folder - The name of the folder containing the field
# + 'field - The field name
# + value - The field value
public type MQRFH2Field record {|
    readonly string folder;
    readonly string 'field;
    int|float|byte|byte[]|string value;
|};
