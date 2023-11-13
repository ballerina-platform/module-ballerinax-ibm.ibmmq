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

# The SSL Cipher Suite to be used for secure communication with the IBM MQ server.
public type SSL_CIPHER_SUITE SSL_ECDHE_ECDSA_WITH_3DES_EDE_CBC_SHA|SSL_ECDHE_ECDSA_WITH_AES_128_CBC_SHA256
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
    SSL_CIPHER_SUITE sslCipherSuite?;
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
