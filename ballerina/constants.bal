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
