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

import ballerina/crypto;

# Options which can be provided when opening an IBM MQ topic.
public type OPEN_TOPIC_OPTION OPEN_AS_SUBSCRIPTION|OPEN_AS_PUBLICATION;

# Header types that are provided in the IBM MQ message.
public type Header MQRFH2|MQRFH|MQCIH|MQIIH;

# The coded character set used in application message data.
public type MessageCharset MQCCSI_APPL|MQCCSI_ASCII|MQCCSI_ASCII_ISO|MQCCSI_AS_PUBLISHED|MQCCSI_DEFAULT|
    MQCCSI_EBCDIC|MQCCSI_EMBEDDED|MQCCSI_INHERIT|MQCCSI_Q_MGR|MQCCSI_UNDEFINED|MQCCSI_UNICODE|MQCCSI_UTF8;

# The SSL Cipher Suite to be used for secure communication with the IBM MQ server.
public type SslCipherSuite SSL_ECDHE_ECDSA_WITH_3DES_EDE_CBC_SHA|SSL_ECDHE_ECDSA_WITH_AES_128_CBC_SHA256
    |SSL_ECDHE_ECDSA_WITH_AES_128_GCM_SHA256|SSL_ECDHE_ECDSA_WITH_AES_256_CBC_SHA384|SSL_ECDHE_ECDSA_WITH_AES_256_GCM_SHA384
    |SSL_ECDHE_ECDSA_WITH_NULL_SHA|SSL_ECDHE_ECDSA_WITH_RC4_128_SHA|SSL_ECDHE_RSA_WITH_3DES_EDE_CBC_SHA|SSL_ECDHE_RSA_WITH_AES_128_CBC_SHA256
    |SSL_ECDHE_RSA_WITH_AES_128_GCM_SHA256|SSL_ECDHE_RSA_WITH_AES_256_CBC_SHA384|SSL_ECDHE_RSA_WITH_AES_256_GCM_SHA384
    |SSL_ECDHE_RSA_WITH_NULL_SHA|SSL_ECDHE_RSA_WITH_RC4_128_SHA|SSL_RSA_WITH_3DES_EDE_CBC_SHA|SSL_RSA_WITH_AES_128_CBC_SHA
    |SSL_RSA_WITH_AES_128_CBC_SHA256|SSL_RSA_WITH_AES_128_GCM_SHA256|SSL_RSA_WITH_AES_256_CBC_SHA|SSL_RSA_WITH_AES_256_CBC_SHA256
    |SSL_RSA_WITH_AES_256_GCM_SHA384|SSL_RSA_WITH_DES_CBC_SHA|SSL_RSA_WITH_NULL_SHA256|SSL_RSA_WITH_RC4_128_SHA
    |TLS12|TLS_AES_128_GCM_SHA256|TLS_AES_256_GCM_SHA384|TLS_CHACHA20_POLY1305_SHA256|TLS_AES_128_CCM_SHA256
    |TLS_AES_128_CCM_8_SHA256|ANY|TLS13|TLS12ORHIGHER|TLS13ORHIGHER;

# IBM MQ queue manager configurations.
#
# + name - Name of the queue manager  
# + host - IBM MQ server host  
# + port - IBM MQ server port  
# + channel - IBM MQ channel  
# + userID - IBM MQ userId  
# + password - IBM MQ user password  
# + secureSocket - Configurations related to SSL/TLS encryption
# + sslCipherSuite - Defines the combination of key exchange, encryption, 
#   and integrity algorithms used for establishing a secure SSL/TLS connection
public type QueueManagerConfiguration record {|
    string name;
    string host;
    int port = 1414;
    string channel;
    string userID?;
    string password?;
    SecureSocket secureSocket?;
    SslCipherSuite sslCipherSuite?;
|};

# Configurations for secure communication with the IBM MQ server.
#
# + cert - Configurations associated with `crypto:TrustStore` or single certificate file that the client trusts
# + key - Configurations associated with `crypto:KeyStore` or combination of certificate and private key of the client
# + provider - Name of the security provider used for SSL connections. The default value is the default security provider
# of the JVM
public type SecureSocket record {|
    crypto:TrustStore|string cert;
    crypto:KeyStore|CertKey key?;
    string provider?;
|};

# Represents a combination of certificate, private key, and private key password if encrypted.
#
# + certFile - A file containing the certificate
# + keyFile - A file containing the private key in PKCS8 format
# + keyPassword - Password of the private key if it is encrypted
public type CertKey record {|
    string certFile;
    string keyFile;
    string keyPassword?;
|};

# IBM MQ get message options.
#
# + options - Get message option 
# + waitInterval - The maximum time (in seconds) that a `get` call waits for a suitable message to 
#                  arrive. It is used in conjunction with `ibmmq:MQGMO_WAIT`.
# + matchOptions - Message selection criteria
public type GetMessageOptions record {|
    int options = MQGMO_NO_WAIT;
    int waitInterval = 10;
    MatchOptions matchOptions?;
|};

# Represents the selection criteria that determine which message is retrieved.
#
# + messageId - The message identifier of the message which needs to be retrieved
# + correlationId - The Correlation identifier of the message which needs to be retrieved
public type MatchOptions record {|
    byte[] messageId?;
    byte[] correlationId?;
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
# + encoding - Specifies the representation used for numeric values in the application message data. 
#              This can be represented using as a combination of `ibmmq:MQENC_*` options 
# + characterSet - The coded character set identifier of character data in the application message data
# + accountingToken - The accounting token, which is part of the message's identity and allows the work performed as a result of the message to be properly charged 
# + userId - Id of the user who originated the message
# + headers - Headers to be sent in the message
# + payload - Message payload
public type Message record {|
    map<Property> properties?;
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
    int encoding = MQENC_INTEGER_NORMAL|MQENC_DECIMAL_NORMAL|MQENC_FLOAT_IEEE_NORMAL;
    MessageCharset characterSet = MQCCSI_Q_MGR;
    byte[] accountingToken?;
    string userId?;
    Header[] headers?;
    byte[] payload;
|};

# Header record representing the MQRFH2 structure.
#
# + flags - Flag of the header  
# + encoding - Numeric encoding of data that follows NameValueData  
# + codedCharSetId - Character set identifier of data that follows NameValueData
# + folderStrings - Contents of the variable part of the structure  
# + nameValueCCSID - Coded character set for the NameValue data  
# + nameValueData - NameValueData variable-length field  
# + nameValueLength - Length of NameValueData  
# + format - Format name of data that follows NameValueData.The name should be padded with  
# blanks to the length of the field.  
# + strucId - Structure identifier  
# + strucLength - Length of the structure  
# + version - Structure version number  
# + fieldValues - Table containing all occurrences of field values matching  
# the specified field name in the folder
public type MQRFH2 record {|
    int flags = 0;
    int encoding = 273;
    int codedCharSetId = -2;
    string[] folderStrings = [];
    int nameValueCCSID = 1208;
    byte[] nameValueData = [];
    int nameValueLength = 0;
    string format = DEFAULT_BLANK_VALUE;
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
    boolean|byte|byte[]|float|int|string value;
|};

# Header record representing the MQRFH structure.
#
# + flags - Flag of the header  
# + encoding - Numeric encoding of data that follows NameValueString  
# + strucId - Structure identifier  
# + strucLength - Length of the structure  
# + version - Structure version number  
# + codedCharSetId - Character set identifier of data that follows NameValueString
# + format - Format name of data that follows NameValueString
# + nameValuePairs - Related name-value pairs
public type MQRFH record {|
    int flags = 0;
    int encoding = 0;
    string strucId = "RFH ";
    int strucLength = 32;
    int version = 1;
    int codedCharSetId = 0;
    string format = DEFAULT_BLANK_VALUE;
    map<string> nameValuePairs = {};
|};

# Header record representing the MQCIH structure.
#
# + flags - Flag of the header  
# + encoding - Numeric encoding of data that follows NameValueData 
# + codedCharSetId - Character set identifier of data that follows NameValueString
# + format - MQ format name of data that follows MQCIH
# + strucId - Structure identifier  
# + strucLength - Length of the structure  
# + version - Structure version number  
# + returnCode - Return code from bridge  
# + compCode - MQ completion code or CICS EIBRESP  
# + reason - MQ reason or feedback code, or CICS EIBRESP2  
# + UOWControl - Unit-of-work control  
# + waitInterval - Wait interval for MQGET call issued by bridge task  
# + linkType - Link type  
# + facilityKeepTime - Bridge facility release time  
# + ADSDescriptor - Send/receive ADS descriptor  
# + conversationalTask - Whether task can be conversational  
# + taskEndStatus - Status at end of task  
# + facility - Bridge facility token  
# + 'function - MQ call name or CICS EIBFN function  
# + abendCode - Abend code  
# + authenticator - Password or passticket  
# + replyToFormat - MQ format name of reply message  
# + remoteSysId - Remote CICS system Id to use  
# + remoteTransId - CICS RTRANSID to use  
# + transactionId - Transaction to attach  
# + facilityLike - Terminal emulated attributes  
# + attentionId - AID key  
# + startCode - Transaction start code  
# + cancelCode - Abend transaction code  
# + nextTransactionId - Next transaction to attach  
# + inputItem - Reserved
public type MQCIH record {|
    int flags = 0;
    int encoding = 0;
    int codedCharSetId = 0;
    string format = DEFAULT_BLANK_VALUE;
    string strucId = "CIH ";
    int strucLength = 180;
    int version = 2;
    int returnCode = 0;
    int compCode = 0;
    int reason = 0;
    int UOWControl = 273;
    int waitInterval = -2;
    int linkType = 1;
    int facilityKeepTime = 0;
    int ADSDescriptor = 0;
    int conversationalTask = 0;
    int taskEndStatus = 0;
    byte[] facility = [];
    string 'function = "";
    string abendCode = "";
    string authenticator = "";
    string replyToFormat = "";
    string remoteSysId = "";
    string remoteTransId = "";
    string transactionId = "";
    string facilityLike = "";
    string attentionId = "";
    string startCode = "";
    string cancelCode = "";
    string nextTransactionId = "";
    int inputItem = 0;
|};

# Header record representing the MQIIH structure.
#
# + flags - Flag of the header  
# + encoding - Numeric encoding of data that follows NameValueString  
# + strucId - Structure identifier  
# + strucLength - Length of the structure  
# + version - Structure version number  
# + codedCharSetId - Character set identifier of data that follows NameValueString  
# + format - Format name of data that follows NameValueString  
# + lTermOverride - The logical terminal override, placed in the IO PCB field
# + mfsMapName - The message format services map name, placed in the IO PCB field
# + replyToFormat - This is the MQ format name of the reply message that is sent 
#                   in response to the current message
# + authenticator - RACF password or passticket
# + tranInstanceId - This is the transaction instance identifier
# + tranState - This indicates the IMS conversation state
# + commitMode - IMS commit mode
# + securityScope - This indicates the IMS security processing required
public type MQIIH record {|
    int flags = 0;
    int encoding = 0;
    string strucId = "IIH ";
    int strucLength = 84;
    int version = 1;
    int codedCharSetId = 0;
    string format = DEFAULT_BLANK_VALUE;
    string lTermOverride = DEFAULT_BLANK_VALUE;
    string mfsMapName = DEFAULT_BLANK_VALUE;
    string replyToFormat = DEFAULT_BLANK_VALUE;
    string authenticator = DEFAULT_BLANK_VALUE;
    byte[] tranInstanceId = [0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0];
    string:Char tranState = " ";
    string:Char commitMode = "0";
    string:Char securityScope = "C";
|};
