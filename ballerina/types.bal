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

# IBM MQ queue manager configurations.
#
# + name - Name of the queue manager
# + host - IBM MQ server host
# + port - IBM MQ server port 
# + channel - IBM MQ channel  
# + userID - IBM MQ userId  
# + password - IBM MQ user password
# + secureSocket - Configurations related to SSL/TLS encryption
public type QueueManagerConfiguration record {|
    string name;
    string host;
    int port = 1414;
    string channel;
    string userID?;
    string password?;
    SecureSocket secureSocket?;
|};

# Configurations for secure communication with the IBM MQ server.
#
# + cert - Configurations associated with crypto:TrustStore or single certificate file that the client trusts
# + key - Configurations associated with crypto:KeyStore or combination of certificate and private key of the client
# + protocol - SSL/TLS protocol related options
# + ciphers - List of ciphers to be used. By default, all the available cipher suites are supported
# + provider - Name of the security provider used for SSL connections. The default value is the default security provider
# of the JVM
public type SecureSocket record {|
    crypto:TrustStore|string cert;
    record {|
        crypto:KeyStore keyStore;
        string keyPassword?;
    |}|CertKey key?;
    record {|
        Protocol name;
        string[] versions?;
    |} protocol?;
    string[] ciphers?;
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

# Represents protocol options.
public enum Protocol {
    SSL,
    TLS
}

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
