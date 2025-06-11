/*
 * Copyright (c) 2025, WSO2 LLC. (http://www.wso2.org).
 *
 *  WSO2 LLC. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied. See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package io.ballerina.lib.ibm.ibmmq;

import io.ballerina.runtime.api.PredefinedTypes;
import io.ballerina.runtime.api.creators.ValueCreator;
import io.ballerina.runtime.api.types.ObjectType;
import io.ballerina.runtime.api.types.Parameter;
import io.ballerina.runtime.api.types.RemoteMethodType;
import io.ballerina.runtime.api.types.ServiceType;
import io.ballerina.runtime.api.types.Type;
import io.ballerina.runtime.api.utils.TypeUtils;
import io.ballerina.runtime.api.values.BObject;

import static io.ballerina.lib.ibm.ibmmq.CommonUtils.createError;
import static io.ballerina.lib.ibm.ibmmq.Constants.BMESSAGE_NAME;
import static io.ballerina.lib.ibm.ibmmq.Constants.IBMMQ_ERROR;
import static io.ballerina.lib.ibm.ibmmq.ModuleUtils.getModule;

/**
 * Validates a Ballerina IBMMQ service.
 * This is a runtime validator, which will eventually be replaced by a compile-time validator.
 *
 * @since 1.3.0
 */
public class ServiceValidator {
    private final ServiceType serviceType;
    private RemoteMethodType onMessageMethod;
    private RemoteMethodType onErrorMethod;
    private boolean isServiceIsolated;
    private boolean isOnMessageIsolated;
    private boolean isOnErrorIsolated;

    ServiceValidator(BObject service) {
        this.serviceType = (ServiceType) TypeUtils.getType(service);
        this.isServiceIsolated = ((ObjectType) TypeUtils.getReferredType(serviceType)).isIsolated();
    }

    public void validate() {
        if (this.serviceType.getResourceMethods().length > 0) {
            throw createError(IBMMQ_ERROR, "IBMMQ service cannot have resource methods");
        }
        RemoteMethodType[] remoteMethods = this.serviceType.getRemoteMethods();
        if (remoteMethods.length < 1 || remoteMethods.length > 2) {
            throw createError(IBMMQ_ERROR, "IBMMQ service must have exactly one or two remote methods");
        }
        validateRemoteMethods(remoteMethods);
    }

    private void validateRemoteMethods(RemoteMethodType[] remoteMethods) {
        for (RemoteMethodType remoteMethod : remoteMethods) {
            if (remoteMethod.getName().equals("onMessage")) {
                validateOnMessageMethod(remoteMethod);
            } else if (remoteMethod.getName().equals("onError")) {
                validateOnErrorMethod(remoteMethod);
            } else {
                throw createError(IBMMQ_ERROR, "Invalid remote method name: " + remoteMethod.getName());
            }
        }
        if (this.onMessageMethod == null) {
            throw createError(IBMMQ_ERROR, "IBMMQ service must have an 'onMessage' remote method");
        }
    }

    private void validateOnMessageMethod(RemoteMethodType remoteMethod) {
        if (remoteMethod.getParameters().length != 1) {
            throw createError(IBMMQ_ERROR, "onMessage method must have exactly one parameter");
        }
        Parameter parameter = remoteMethod.getParameters()[0];
        Type messageType = ValueCreator.createRecordValue(getModule(), BMESSAGE_NAME).getType();
        if (!TypeUtils.isSameType(messageType, TypeUtils.getReferredType(parameter.type))) {
            throw createError(IBMMQ_ERROR, "onMessage method parameter must be of type 'ibmmq:Message'");
        }
        this.onMessageMethod = remoteMethod;
        this.isOnMessageIsolated = remoteMethod.isIsolated();
    }

    private void validateOnErrorMethod(RemoteMethodType remoteMethod) {
        if (remoteMethod.getParameters().length != 1) {
            throw createError(IBMMQ_ERROR, "onError method must have exactly one parameter");
        }
        Parameter parameter = remoteMethod.getParameters()[0];
        if (!TypeUtils.isSameType(TypeUtils.getReferredType(parameter.type), PredefinedTypes.TYPE_ERROR)) {
            throw createError(IBMMQ_ERROR, "onError method parameter must be of type 'error'");
        }
        this.onErrorMethod = remoteMethod;
        this.isOnErrorIsolated = remoteMethod.isIsolated();
    }

    public RemoteMethodType getOnMessageMethod() {
        return this.onMessageMethod;
    }

    public RemoteMethodType getOnErrorMethod() {
        return this.onErrorMethod;
    }

    public boolean isOnMessageIsolated() {
        return this.isServiceIsolated && this.isOnMessageIsolated;
    }

    public boolean isOnErrorIsolated() {
        return this.isServiceIsolated && this.isOnErrorIsolated;
    }
}
