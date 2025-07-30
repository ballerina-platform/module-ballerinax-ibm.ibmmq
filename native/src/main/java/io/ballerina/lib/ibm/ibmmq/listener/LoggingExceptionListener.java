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

import java.io.PrintStream;

import javax.jms.ExceptionListener;
import javax.jms.JMSException;

/**
 * Logging exception listener class for JMS {@link javax.jms.Connection}.
 */
public class LoggingExceptionListener implements ExceptionListener {
    private static final PrintStream ERR_OUT = System.err;

    @Override
    public void onException(JMSException connectionException) {
        ERR_OUT.println("Connection exception received from the IBM MQ: " + connectionException.getMessage());
        connectionException.printStackTrace();
    }
}

