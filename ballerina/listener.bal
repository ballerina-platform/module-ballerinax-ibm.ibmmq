// Copyright (c) 2025, WSO2 LLC. (http://www.wso2.org).
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

import ballerina/jballerina.java;

# Represents an IBMMQ Listener endpoint that can be used to receive messages from an IBMMQ queue.
public isolated class Listener {

    # Initializes the IBMMQ listener.
    # + configurations - The configurations to be used when initializing the IBMMQ listener
    # + return - An error if the initialization failed, nil otherwise
    public isolated function init(*QueueManagerConfiguration configurations) returns Error? {
        return self.initListener(configurations);
    }

    # Attaches an IBMMQ service to the IBMMQ listener.
    # + s - The IBMMQ Service to attach
    # + name - The name of the queue/topic to attach to
    # + return - An error if the attaching failed, nil otherwise
    public isolated function attach(Service s, string[]|string? name = ()) returns Error? = @java:Method {
        'class: "io.ballerina.lib.ibm.ibmmq.listener.ListenerV2"
    } external;

    # Detaches an IBMMQ service from the IBMMQ listener.
    # + s - The IBMMQ Service to detach
    # + return - An error if the detaching failed, nil otherwise
    public isolated function detach(Service s) returns Error? = @java:Method {
        'class: "io.ballerina.lib.ibm.ibmmq.listener.ListenerV2"
    } external;

    # Starts the IBMMQ listener.
    # + return - An error if the starting failed, nil otherwise
    public isolated function 'start() returns Error? = @java:Method {
        'class: "io.ballerina.lib.ibm.ibmmq.listener.ListenerV2"
    } external;

    # Gracefully stops the IBMMQ listener.
    # + return - An error if the stopping failed, nil otherwise
    public isolated function gracefulStop() returns Error? = @java:Method {
        'class: "io.ballerina.lib.ibm.ibmmq.listener.ListenerV2"
    } external;

    # Immediately stops the IBMMQ listener.
    # + return - An error if the stopping failed, nil otherwise
    public isolated function immediateStop() returns Error? = @java:Method {
        'class: "io.ballerina.lib.ibm.ibmmq.listener.ListenerV2"
    } external;

    isolated function initListener(QueueManagerConfiguration configurations) returns Error? = @java:Method {
        'class: "io.ballerina.lib.ibm.ibmmq.listener.ListenerV2"
    } external;
}
