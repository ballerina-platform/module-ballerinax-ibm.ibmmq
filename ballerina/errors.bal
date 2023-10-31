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

# Represents a IBM MQ distinct error.
public type Error distinct error<ErrorDetails>;
  
# The error details type for the IBM MQ module.
#
# + reasonCode - The reason code for the error
# + errorCode - The error code for the error
# + completionCode - The completion code for the error
public type ErrorDetails record {|
  int reasonCode?;
  string errorCode?;
  int completionCode?;
|};
