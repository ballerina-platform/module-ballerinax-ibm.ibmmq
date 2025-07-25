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

package io.ballerina.lib.ibm.ibmmq.listener;

import io.ballerina.runtime.api.creators.ValueCreator;
import io.ballerina.runtime.api.types.ObjectType;
import io.ballerina.runtime.api.types.Parameter;
import io.ballerina.runtime.api.types.PredefinedTypes;
import io.ballerina.runtime.api.types.RemoteMethodType;
import io.ballerina.runtime.api.types.ServiceType;
import io.ballerina.runtime.api.types.Type;
import io.ballerina.runtime.api.utils.StringUtils;
import io.ballerina.runtime.api.utils.TypeUtils;
import io.ballerina.runtime.api.values.BMap;
import io.ballerina.runtime.api.values.BObject;
import io.ballerina.runtime.api.values.BString;

import static io.ballerina.lib.ibm.ibmmq.CommonUtils.createError;
import static io.ballerina.lib.ibm.ibmmq.Constants.BMESSAGE_NAME;
import static io.ballerina.lib.ibm.ibmmq.Constants.IBMMQ_ERROR;
import static io.ballerina.lib.ibm.ibmmq.ModuleUtils.getModule;
import static io.ballerina.runtime.api.constants.RuntimeConstants.ORG_NAME_SEPARATOR;
import static io.ballerina.runtime.api.constants.RuntimeConstants.VERSION_SEPARATOR;

/**
 * Validates a Ballerina IBMMQ service.
 * This is a runtime validator, which will eventually be replaced by a compile-time validator.
 *
 * @since 1.3.0
 */
public class IbmmqService {
    private static final String SERVICE_CONFIG_ANNOTATION_NAME = "ServiceConfig";
    private static final Type messageType = ValueCreator.createRecordValue(getModule(), BMESSAGE_NAME).getType();

    private static final BString CONFIG = StringUtils.fromString("config");
    static final BString SUBSCRIPTION_NAME = StringUtils.fromString("subscriptionName");
    static final BString QUEUE_NAME = StringUtils.fromString("queueName");
    static final BString TOPIC_NAME = StringUtils.fromString("topicName");
    static final BString DURABLE = StringUtils.fromString("durable");

    private final ServiceType serviceType;
    private RemoteMethodType onMessageMethod;
    private RemoteMethodType onErrorMethod;
    private boolean isServiceIsolated;
    private boolean isOnMessageIsolated;
    private boolean isOnErrorIsolated;
    private BMap<BString, Object> config;
    private String subscriptionName;

    private ServiceContext context;

    IbmmqService(BObject service) {
        this.serviceType = (ServiceType) TypeUtils.getType(service);
        this.isServiceIsolated = ((ObjectType) TypeUtils.getReferredType(serviceType)).isIsolated();
    }

    public void initialize() {
        this.validate();
        this.extractConfigs();
    }

    private void validate() {
        if (this.serviceType.getResourceMethods().length > 0) {
            throw createError(IBMMQ_ERROR, "IBMMQ service cannot have resource methods");
        }
        RemoteMethodType[] remoteMethods = this.serviceType.getRemoteMethods();
        if (remoteMethods.length < 1 || remoteMethods.length > 2) {
            throw createError(IBMMQ_ERROR, "IBMMQ service must have exactly one or two remote methods");
        }
        validateRemoteMethods(remoteMethods);
    }

    private void extractConfigs() {
        this.setConfigs();
    }

    private void validateRemoteMethods(RemoteMethodType[] remoteMethods) {
        for (RemoteMethodType remoteMethod : remoteMethods) {
            if (remoteMethod.getName().equals("onMessage")) {
                validateOnMessageMethod(remoteMethod);
            } else if (remoteMethod.getName().equals("onError")) {
                validateOnErrorMethod(remoteMethod);
            } else {
                throw createError(IBMMQ_ERROR, "Invalid remote method queueManagerName: " + remoteMethod.getName());
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

    private void setConfigs() {
        BMap<BString, Object> serviceConfig =
                (BMap<BString, Object>) this.serviceType.getAnnotation(getServiceConfigAnnotationName());
        if (serviceConfig == null) {
            throw createError(IBMMQ_ERROR, "Service configuration annotation is required");
        }
        BMap<BString, Object> config = (BMap<BString, Object>) serviceConfig.getMapValue(CONFIG);
        if (config.containsKey(SUBSCRIPTION_NAME)) {
            this.subscriptionName = config.getStringValue(SUBSCRIPTION_NAME).getValue();
        }
        this.config = config;
    }

    private static BString getServiceConfigAnnotationName() {
        return StringUtils.fromString(getModule().getOrg() + ORG_NAME_SEPARATOR +
                getModule().getName() + VERSION_SEPARATOR + getModule().getMajorVersion() + VERSION_SEPARATOR +
                SERVICE_CONFIG_ANNOTATION_NAME);
    }

    public boolean isTopicConsumer() {
        return this.config.containsKey(TOPIC_NAME);
    }

    public String getTopicName() {
        if (this.isTopicConsumer()) {
            return this.config.getStringValue(TOPIC_NAME).getValue();
        }
        throw new IllegalStateException("Topic queueManagerName is not available for non-topic consumers");
    }

    public boolean isDurableConsumer() {
        if (this.isTopicConsumer()) {
            return this.config.getBooleanValue(DURABLE);
        }
        return false;
    }

    public String getSubscriptionName() {
        if (this.isDurableConsumer()) {
            return this.subscriptionName;
        }
        return null;
    }

    public String getQueueName() {
        if (!this.isTopicConsumer()) {
            return this.config.getStringValue(QUEUE_NAME).getValue();
        }
        throw new IllegalStateException("Queue queueManagerName is not available for topic consumers");
    }

    @Override
    public String toString() {
        return "IbmmqService{type=" + serviceType.getName() +
                ", topic=" + (isTopicConsumer() ? getTopicName() : "null") +
                ", queue=" + (!isTopicConsumer() ? getQueueName() : "null") +
                ", durable=" + isDurableConsumer() +
                '}';
    }

    public ServiceContext getContext() {
        if (this.context == null) {
            throw new IllegalStateException("Service context is not initialized.");
        }
        return this.context;
    }

    public void setContext(ServiceContext context) {
        this.context = context;
    }

    public void close() {
        if (this.context == null) {
            throw new IllegalStateException("Service context is not initialized.");
        }
        this.context.close();
    }

    public boolean hasOnError() {
        return this.onErrorMethod != null;
    }

    public boolean isOnMessageIsolated() {
        return this.isServiceIsolated && this.isOnMessageIsolated;
    }

    public boolean isOnErrorIsolated() {
        return this.isServiceIsolated && this.isOnErrorIsolated;
    }
}
