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

# Open topic as a subscription.
public const int OPEN_AS_SUBSCRIPTION = 1;

# Open topic as a publication.
public const int OPEN_AS_PUBLICATION = 2;

# Open the queue to browse messages.
public const int MQOO_BROWSE = 8;

# Open the queue to get messages using the queue-defined default.
public const int MQOO_INPUT_AS_Q_DEF = 1;

# Open the queue to get messages with exclusive access.
public const int MQOO_INPUT_EXCLUSIVE = 4;

# Open the queue to get messages with shared access.
public const int MQOO_INPUT_SHARED = 2;

# Enables the AlternateUserId field in the ObjDesc parameter contains a user identifier to use to validate this MQOPEN call.
public const int MQOO_ALTERNATE_USER_AUTHORITY = 4096;

# The local queue manager binds the queue handle in the way defined by the DefBind queue attribute.
public const int MQOO_BIND_AS_Q_DEF = 0;

# The MQOPEN call fails if the queue manager is in quiescing state. This option is valid for all types of object.
public const int MQOO_FAIL_IF_QUIESCING = 8192;

# This allows the MQPMO_PASS_ALL_CONTEXT option to be specified in the PutMsgOpts parameter when a message is put on a queue.
public const int MQOO_PASS_ALL_CONTEXT = 512;

# This allows the MQPMO_PASS_IDENTITY_CONTEXT option to be specified in the PutMsgOpts parameter when a message is put on a queue.
public const int MQOO_PASS_IDENTITY_CONTEXT = 256;

# This allows the MQPMO_SET_ALL_CONTEXT option to be specified in the PutMsgOpts parameter when a message is put on a queue.
public const int MQOO_SET_ALL_CONTEXT = 2048;

# This allows the MQPMO_SET_IDENTITY_CONTEXT option to be specified in the PutMsgOpts parameter when a message is put on a queue.
public const int MQOO_SET_IDENTITY_CONTEXT = 1024;

# Open the queue to put messages.
public const int MQOO_OUTPUT = 16;

# The application waits until a suitable message arrives.
public const int MQGMO_WAIT = 1;

# The application does not wait if no suitable message is available.
public const int MQGMO_NO_WAIT = 0;

# The request is to operate within the normal unit-of-work protocols.
public const int MQGMO_SYNCPOINT = 2;

# The request is to operate outside the normal unit-of-work protocols.
public const int MQGMO_NO_SYNCPOINT = 4;

# When a queue is opened with the MQOO_BROWSE option, a browse cursor is established, positioned logically 
# before the first message on the queue.
public const int MQGMO_BROWSE_FIRST = 16;

# Advance the browse cursor to the next message on the queue that satisfies the selection criteria specified 
# on the MQGET call.
public const int MQGMO_BROWSE_NEXT = 32;

# Retrieve the message pointed to by the browse cursor nondestructively, regardless of the MQMO_* options 
# specified in the MatchOptions field in MQGMO.
public const int MQGMO_BROWSE_MSG_UNDER_CURSOR = 2048;

# Retrieve the message pointed to by the browse cursor, regardless of the MQMO_* options specified in the 
# MatchOptions field in MQGMO.
public const int MQGMO_MSG_UNDER_CURSOR = 256;

# Lock the message that is browsed, so that the message becomes invisible to any other handle open for the queue.
public const int MQGMO_LOCK = 512;

# Unlock a message. The message to be unlocked must have been previously locked by an MQGET call with the 
# MQGMO_LOCK option.
public const int MQGMO_UNLOCK = 1024;

# If the message buffer is too small to hold the complete message, allow the MQGET call to fill the 
# buffer with as much of the message as the buffer can hold.
public const int MQGMO_ACCEPT_TRUNCATED_MSG = 64;

# Force the MQGET call to fail if the queue manager is in the quiescing state.
public const int MQGMO_FAIL_IF_QUIESCING = 8192;

# Requests the application data to be converted.
public const int MQGMO_CONVERT = 16384;

# Subscribe Option create
public const int MQSO_CREATE = 2;

# Encoding for normal integer representation (most significant byte first) or the big-endian format.
public const int MQENC_INTEGER_NORMAL = 1;

# Encoding for reversed integer representation (least significant byte first) or the little-endian format.
public const int MQENC_INTEGER_REVERSED = 2;

# Packed-decimal integers are represented in the conventional way: Each decimal digit in the printable form of the number is represented in packed decimal by a single hexadecimal digit in the range X'0' through X'9'.
public const int MQENC_DECIMAL_NORMAL = 16;

# Packed-decimal integers are represented in the same way as `ENC_DECIMAL_NORMAL`, but with the bytes arranged in reverse order.
public const int MQENC_DECIMAL_REVERSED = 32;

# Floating-point numbers are represented using the standard IEEE3 floating-point format
public const int MQENC_FLOAT_IEEE_NORMAL = 256;

# Floating-point numbers are represented in the same way as `ENC_FLOAT_IEEE_NORMAL`, but with the bytes arranged in reverse order.
public const int MQENC_FLOAT_IEEE_REVERSED = 512;

# Floating-point numbers are represented using the standard zSeries (System/390) floating-point format.
public const int MQENC_FLOAT_S390 = 768;

# Coded Character Set Identifiers - Appl.
public const int  MQCCSI_APPL = -3;

# ASCII codeset
public const int MQCCSI_ASCII = 850;

# ISO standard ASCII codeset 
public const int MQCCSI_ASCII_ISO = 819;

# Coded Character Set Identifiers - As Published.
public const int MQCCSI_AS_PUBLISHED = -4;

# The CodedCharSetId of the data in the String field is defined by the CodedCharSetId field in the header structure 
# that precedes the MQCFH structure, or by the CodedCharSetId field in the MQMD if the MQCFH is at the start of the message.
public const int MQCCSI_DEFAULT = 0;

# The American EBCDIC codeset  
public const int MQCCSI_EBCDIC = 37;

# Coded Character Set Identifiers - Embedded.
public const int MQCCSI_EMBEDDED = -1;

# Character data in the message is in the same character set as this structure.
public const int MQCCSI_INHERIT = -2;

# Character data in the message is in the queue manager's character set.
public const int MQCCSI_Q_MGR = 0;

# Coded Character Set Identifiers - Undefined.
public const int MQCCSI_UNDEFINED = 0;

# Unicode codeset
public const int MQCCSI_UNICODE = 1200;

# UTF-8 codeset
public const int MQCCSI_UTF8 = 1208;

const string DEFAULT_BLANK_VALUE = "        ";

# The request is to operate within the normal unit-of-work protocols. The message is not visible outside the 
# unit of work until the unit of work is committed. If the unit of work is backed out, the message is deleted.
public const int MQPMO_SYNCPOINT = 2;

# The request is to operate outside the normal unit-of-work protocols. The message is available immediately, 
# and it cannot be deleted by backing out a unit of work.
public const int MQPMO_NO_SYNCPOINT = 4;

# Both identity and origin context are set to indicate no context.
public const int MQPMO_NO_CONTEXT = 16384;

# The message is to have default context information associated with it, for both identity and origin.
public const int MQPMO_DEFAULT_CONTEXT = 32;

# The message is to have context information associated with it.
public const int MQPMO_SET_IDENTITY_CONTEXT = 1024;

# The message is to have context information associated with it.
public const int MQPMO_SET_ALL_CONTEXT = 2048;

# This option forces the MQPUT or MQPUT1 call to fail if the queue manager is in the quiescing state.
public const int MQPMO_FAIL_IF_QUIESCING = 8192;

# The queue manager replaces the contents of the MsgId field in MQMD with a new message identifier.
public const int MQPMO_NEW_MSG_ID = 64;

# The queue manager replaces the contents of the CorrelId field in MQMD with a new correlation identifier.
public const int MQPMO_NEW_CORREL_ID = 128;

# This option tells the queue manager how the application puts messages in groups and segments of logical messages.
public const int MQPMO_LOGICAL_ORDER = 32768;

# This indicates that the AlternateUserId field in the ObjDesc parameter of the MQPUT1 call contains a user identifier 
# that is to be used to validate authority to put messages on the queue.
public const int MQPMO_ALTERNATE_USER_AUTHORITY = 4096;

# Use this option to fill ResolvedQName in the MQPMO structure with the name of the local queue to which 
# the message is put, and ResolvedQMgrName with the name of the local queue manager that hosts the local queue.
public const int MQPMO_RESOLVE_LOCAL_Q = 262144;

# The MQPMO_ASYNC_RESPONSE option requests that an MQPUT or MQPUT1 operation is completed without the 
# application waiting for the queue manager to complete the call.
public const int MQPMO_ASYNC_RESPONSE = 65536;

// SSL cipher suite related constants

# SSL cipher suite using ECDHE-ECDSA for key exchange with 3DES encryption and SHA integrity.
public const string SSL_ECDHE_ECDSA_WITH_3DES_EDE_CBC_SHA = "SSL_ECDHE_ECDSA_WITH_3DES_EDE_CBC_SHA";

# SSL cipher suite using ECDHE-ECDSA for key exchange with AES 128-bit encryption and SHA-256 integrity.
public const string SSL_ECDHE_ECDSA_WITH_AES_128_CBC_SHA256 = "SSL_ECDHE_ECDSA_WITH_AES_128_CBC_SHA256";

# SSL cipher suite using ECDHE-ECDSA for key exchange with AES 128-bit GCM encryption and SHA-256 integrity.
public const string SSL_ECDHE_ECDSA_WITH_AES_128_GCM_SHA256 = "SSL_ECDHE_ECDSA_WITH_AES_128_GCM_SHA256";

# SSL cipher suite using ECDHE-ECDSA for key exchange with AES 256-bit CBC encryption and SHA-384 integrity.
public const string SSL_ECDHE_ECDSA_WITH_AES_256_CBC_SHA384 = "SSL_ECDHE_ECDSA_WITH_AES_256_CBC_SHA384";

# SSL cipher suite using ECDHE-ECDSA for key exchange with AES 256-bit GCM encryption and SHA-384 integrity.
public const string SSL_ECDHE_ECDSA_WITH_AES_256_GCM_SHA384 = "SSL_ECDHE_ECDSA_WITH_AES_256_GCM_SHA384";

# SSL cipher suite using ECDHE-ECDSA for key exchange with NULL encryption and SHA integrity (not recommended for production use).
public const string SSL_ECDHE_ECDSA_WITH_NULL_SHA = "SSL_ECDHE_ECDSA_WITH_NULL_SHA";

# SSL cipher suite using ECDHE-ECDSA for key exchange with RC4 128-bit encryption and SHA integrity (not recommended for production use).
public const string SSL_ECDHE_ECDSA_WITH_RC4_128_SHA = "SSL_ECDHE_ECDSA_WITH_RC4_128_SHA";

# SSL cipher suite using ECDHE-RSA for key exchange with 3DES encryption and SHA integrity.
public const string SSL_ECDHE_RSA_WITH_3DES_EDE_CBC_SHA = "SSL_ECDHE_RSA_WITH_3DES_EDE_CBC_SHA";

# SSL cipher suite using ECDHE-RSA for key exchange with AES 128-bit encryption and SHA-256 integrity.
public const string SSL_ECDHE_RSA_WITH_AES_128_CBC_SHA256 = "SSL_ECDHE_RSA_WITH_AES_128_CBC_SHA256";

# SSL cipher suite using ECDHE-RSA for key exchange with AES 128-bit GCM encryption and SHA-256 integrity.
public const string SSL_ECDHE_RSA_WITH_AES_128_GCM_SHA256 = "SSL_ECDHE_RSA_WITH_AES_128_GCM_SHA256";

# SSL cipher suite using ECDHE-RSA for key exchange with AES 256-bit CBC encryption and SHA-384 integrity.
public const string SSL_ECDHE_RSA_WITH_AES_256_CBC_SHA384 = "SSL_ECDHE_RSA_WITH_AES_256_CBC_SHA384";

# SSL cipher suite using ECDHE-RSA for key exchange with AES 256-bit GCM encryption and SHA-384 integrity.
public const string SSL_ECDHE_RSA_WITH_AES_256_GCM_SHA384 = "SSL_ECDHE_RSA_WITH_AES_256_GCM_SHA384";

# SSL cipher suite using ECDHE-RSA for key exchange with NULL encryption and SHA integrity (not recommended for production use).
public const string SSL_ECDHE_RSA_WITH_NULL_SHA = "SSL_ECDHE_RSA_WITH_NULL_SHA";

# SSL cipher suite using ECDHE-RSA for key exchange with RC4 128-bit encryption and SHA integrity (not recommended for production use).
public const string SSL_ECDHE_RSA_WITH_RC4_128_SHA = "SSL_ECDHE_RSA_WITH_RC4_128_SHA";

# SSL cipher suite using RSA for key exchange with 3DES encryption and SHA integrity.
public const string SSL_RSA_WITH_3DES_EDE_CBC_SHA = "SSL_RSA_WITH_3DES_EDE_CBC_SHA";

# SSL cipher suite using RSA for key exchange with AES 128-bit encryption and SHA integrity.
public const string SSL_RSA_WITH_AES_128_CBC_SHA = "SSL_RSA_WITH_AES_128_CBC_SHA";

# SSL cipher suite using RSA for key exchange with AES 128-bit encryption, SHA-256 integrity.
public const string SSL_RSA_WITH_AES_128_CBC_SHA256 = "SSL_RSA_WITH_AES_128_CBC_SHA256";

# SSL cipher suite using RSA for key exchange with AES 128-bit GCM encryption, SHA-256 integrity.
public const string SSL_RSA_WITH_AES_128_GCM_SHA256 = "SSL_RSA_WITH_AES_128_GCM_SHA256";

# SSL cipher suite using RSA for key exchange with AES 256-bit CBC encryption and SHA integrity.
public const string SSL_RSA_WITH_AES_256_CBC_SHA = "SSL_RSA_WITH_AES_256_CBC_SHA";

# SSL cipher suite using RSA for key exchange with AES 256-bit CBC encryption, SHA-256 integrity.
public const string SSL_RSA_WITH_AES_256_CBC_SHA256 = "SSL_RSA_WITH_AES_256_CBC_SHA256";

# SSL cipher suite using RSA for key exchange with AES 256-bit GCM encryption, SHA-384 integrity.
public const string SSL_RSA_WITH_AES_256_GCM_SHA384 = "SSL_RSA_WITH_AES_256_GCM_SHA384";

# SSL cipher suite using RSA for key exchange with DES encryption and SHA integrity (not recommended for production use).
public const string SSL_RSA_WITH_DES_CBC_SHA = "SSL_RSA_WITH_DES_CBC_SHA";

# SSL cipher suite using RSA for key exchange with NULL encryption and SHA-256 integrity (not recommended for production use).
public const string SSL_RSA_WITH_NULL_SHA256 = "SSL_RSA_WITH_NULL_SHA256";

# SSL cipher suite using RSA for key exchange with RC4 128-bit encryption and SHA integrity (not recommended for production use).
public const string SSL_RSA_WITH_RC4_128_SHA = "SSL_RSA_WITH_RC4_128_SHA";

# TLS 1.2 protocol version.
public const string TLS12 = "*TLS12";

# SSL cipher suite using AES 128-bit GCM encryption, SHA-256 integrity.
public const string TLS_AES_128_GCM_SHA256 = "TLS_AES_128_GCM_SHA256";

# SSL cipher suite using AES 256-bit GCM encryption, SHA-384 integrity.
public const string TLS_AES_256_GCM_SHA384 = "TLS_AES_256_GCM_SHA384";

# SSL cipher suite using ChaCha20-Poly1305 encryption, SHA-256 integrity.
public const string TLS_CHACHA20_POLY1305_SHA256 = "TLS_CHACHA20_POLY1305_SHA256";

# SSL cipher suite using AES 128-bit CCM encryption, SHA-256 integrity.
public const string TLS_AES_128_CCM_SHA256 = "TLS_AES_128_CCM_SHA256";

# SSL cipher suite using AES 128-bit CCM 8 encryption, SHA-256 integrity.
public const string TLS_AES_128_CCM_8_SHA256 = "TLS_AES_128_CCM_8_SHA256";

# Any supported SSL/TLS cipher suite.
public const string ANY = "*ANY";

# TLS 1.3 protocol version.
public const string TLS13 = "*TLS13";

# TLS 1.2 or higher protocol version.
public const string TLS12ORHIGHER = "*TLS12ORHIGHER";

# TLS 1.3 or higher protocol version.
public const string TLS13ORHIGHER = "*TLS13ORHIGHER";

