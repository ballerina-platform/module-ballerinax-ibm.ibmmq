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

// Option to indicate whether the topic is being opened for either publication or subscription.
public const OPEN_AS_SUBSCRIPTION = 1;
public const OPEN_AS_PUBLICATION = 2;

// Options that control the opening of the queue for a consumer.
public const MQOO_BROWSE = 8;
public const MQOO_INPUT_AS_Q_DEF = 1;
public const MQOO_INPUT_EXCLUSIVE = 4;
public const MQOO_INPUT_SHARED = 2;

// Options that control the opening of the topic for either publication or subscription.
public const MQOO_ALTERNATE_USER_AUTHORITY = 4096;
public const MQOO_BIND_AS_Q_DEF = 0;
public const MQOO_FAIL_IF_QUIESCING = 8192;
public const MQOO_OUTPUT = 16;
public const MQOO_PASS_ALL_CONTEXT = 512;
public const MQOO_PASS_IDENTITY_CONTEXT = 256;
public const MQOO_SET_ALL_CONTEXT = 2048;
public const MQOO_SET_IDENTITY_CONTEXT = 1024;
public const MQSO_CREATE = 2;

// Options related to the the get message in a topic.
public const MQGMO_WAIT = 1;
public const MQGMO_NO_WAIT = 0;
public const MQGMO_SYNCPOINT = 2;
public const MQGMO_NO_SYNCPOINT = 4;
public const MQGMO_BROWSE_FIRST = 16;
public const MQGMO_BROWSE_NEXT = 32;
public const MQGMO_BROWSE_MSG_UNDER_CURSOR = 2048;
public const MQGMO_MSG_UNDER_CURSOR = 256;
public const MQGMO_LOCK = 512;
public const MQGMO_UNLOCK = 1024;
public const MQGMO_ACCEPT_TRUNCATED_MSG = 64;
public const MQGMO_FAIL_IF_QUIESCING = 8192;
public const MQGMO_CONVERT = 16384;
