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

import ballerina/test;

@test:Config {
    groups: ["listenerValidations"]
}
isolated function testAnnotationNotFound() returns error? {
    Service svc = service object {
        remote function onMessage(Message message, Caller caller) returns error? {
        }
    };
    Error? result = ibmmqListener.attach(svc);
    test:assertTrue(result is Error);
    if result is Error {
        test:assertEquals(
                result.message(),
                "Failed to attach service to listener: Service configuration annotation is required.",
                "Invalid error message received");
    }
}

@test:Config {
    groups: ["listenerValidations"]
}
isolated function testSvcWithResourceMethods() returns error? {
    Service svc = @ServiceConfig {
        sessionAckMode: CLIENT_ACKNOWLEDGE,
        queueName: "test-svc-attach"
    } service object {

        resource function get .() returns error? {
        }

        remote function onMessage(Message message, Caller caller) returns error? {
        }
    };
    Error? result = ibmmqListener.attach(svc);
    test:assertTrue(result is Error);
    if result is Error {
        test:assertEquals(
                result.message(),
                "Failed to attach service to listener: IBM MQ service cannot have resource methods.",
                "Invalid error message received");
    }
}

@test:Config {
    groups: ["listenerValidations"]
}
isolated function testSvcWithNoRemoteMethods() returns error? {
    Service svc = @ServiceConfig {
        sessionAckMode: CLIENT_ACKNOWLEDGE,
        queueName: "test-svc-attach"
    } service object {};
    Error? result = ibmmqListener.attach(svc);
    test:assertTrue(result is Error);
    if result is Error {
        test:assertEquals(
                result.message(),
                "Failed to attach service to listener: IBM MQ service must have exactly one or two remote methods.",
                "Invalid error message received");
    }
}

@test:Config {
    groups: ["listenerValidations"]
}
isolated function testSvcWithInvalidRemoteMethod() returns error? {
    Service svc = @ServiceConfig {
        sessionAckMode: CLIENT_ACKNOWLEDGE,
        queueName: "test-svc-attach"
    } service object {

        remote function onRequest(Message message, Caller caller) returns error? {
        }
    };
    Error? result = ibmmqListener.attach(svc);
    test:assertTrue(result is Error);
    if result is Error {
        test:assertEquals(
                result.message(),
                "Failed to attach service to listener: Invalid remote method name: onRequest.",
                "Invalid error message received");
    }
}

@test:Config {
    groups: ["listenerValidations"]
}
isolated function testSvcMethodWithAdditionalParameters() returns error? {
    Service svc = @ServiceConfig {
        sessionAckMode: CLIENT_ACKNOWLEDGE,
        queueName: "test-svc-attach"
    } service object {

        remote function onMessage(Message message, Caller caller, string requestType) returns error? {
        }
    };
    Error? result = ibmmqListener.attach(svc);
    test:assertTrue(result is Error);
    if result is Error {
        test:assertEquals(
                result.message(),
                "Failed to attach service to listener: onMessage method can have only have either one or two parameters.",
                "Invalid error message received");
    }
}

@test:Config {
    groups: ["listenerValidations"]
}
isolated function testSvcMethodWithInvalidParams() returns error? {
    Service svc = @ServiceConfig {
        sessionAckMode: CLIENT_ACKNOWLEDGE,
        queueName: "test-svc-attach"
    } service object {

        remote function onMessage(Message message, string requestType) returns error? {
        }
    };
    Error? result = ibmmqListener.attach(svc);
    test:assertTrue(result is Error);
    if result is Error {
        test:assertEquals(
                result.message(),
                "Failed to attach service to listener: onMessage method parameters must be of type 'ibmmq:Message' or 'ibmmq:Caller'.",
                "Invalid error message received");
    }
}

@test:Config {
    groups: ["listenerValidations"]
}
isolated function testSvcMethodMandatoryParamMissing() returns error? {
    Service svc = @ServiceConfig {
        sessionAckMode: CLIENT_ACKNOWLEDGE,
        queueName: "test-svc-attach"
    } service object {

        remote function onMessage(Caller caller) returns error? {
        }
    };
    Error? result = ibmmqListener.attach(svc);
    test:assertTrue(result is Error);
    if result is Error {
        test:assertEquals(
                result.message(),
                "Failed to attach service to listener: Required parameter 'ibmmq:Message' can not be found.",
                "Invalid error message received");
    }
}

@test:Config {
    groups: ["listenerValidations"]
}
isolated function testSvcOnErrorWithoutParameters() returns error? {
    Service svc = @ServiceConfig {
        sessionAckMode: CLIENT_ACKNOWLEDGE,
        queueName: "test-svc-attach"
    } service object {

        remote function onMessage(Message message, Caller caller) returns error? {}

        remote function onError() returns error? {}
    };
    Error? result = ibmmqListener.attach(svc);
    test:assertTrue(result is Error);
    if result is Error {
        test:assertEquals(
                result.message(),
                "Failed to attach service to listener: onError method must have exactly one parameter of type 'ibmmq:Error'.",
                "Invalid error message received");
    }
}

@test:Config {
    groups: ["listenerValidations"]
}
isolated function testSvcOnErrorWithInvalidParameter() returns error? {
    Service svc = @ServiceConfig {
        sessionAckMode: CLIENT_ACKNOWLEDGE,
        queueName: "test-svc-attach"
    } service object {

        remote function onMessage(Message message, Caller caller) returns error? {}

        remote function onError(Message message) returns error? {}
    };
    Error? result = ibmmqListener.attach(svc);
    test:assertTrue(result is Error);
    if result is Error {
        test:assertEquals(
                result.message(),
                "Failed to attach service to listener: onError method parameter must be of type 'ibmmq:Error'.",
                "Invalid error message received");
    }    
}

@test:Config {
    groups: ["listenerValidations"]
}
isolated function testSvcOnErrorWithAdditionalParameters() returns error? {
    Service svc = @ServiceConfig {
        sessionAckMode: CLIENT_ACKNOWLEDGE,
        queueName: "test-svc-attach"
    } service object {

        remote function onMessage(Message message, Caller caller) returns error? {}

        remote function onError(Error err, Message message) returns error? {}
    };
    Error? result = ibmmqListener.attach(svc);
    test:assertTrue(result is Error);
    if result is Error {
        test:assertEquals(
                result.message(),
                "Failed to attach service to listener: onError method must have exactly one parameter of type 'ibmmq:Error'.",
                "Invalid error message received");
    }    
}
