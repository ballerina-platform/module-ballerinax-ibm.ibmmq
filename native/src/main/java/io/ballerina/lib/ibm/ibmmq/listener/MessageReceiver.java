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

import io.ballerina.runtime.api.values.BError;

import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.Session;

/**
 * A {MessageReceiver} periodically polls messages from the IBM MQ and dispatches the messages to the IBM MQ service
 * using the message dispatcher.
 *
 * @since 1.3.0.
 */
public class MessageReceiver {

    private final ScheduledExecutorService executorService = Executors.newScheduledThreadPool(1);
    private final AtomicBoolean closed = new AtomicBoolean(false);

    private final Session session;
    private final MessageConsumer consumer;
    private final MessageDispatcher messageDispatcher;
    private final long receiveInterval;
    private final long receiveTimeout;
    private long stopTimeout = 30000;

    private ScheduledFuture<?> pollingTaskFuture;

    public MessageReceiver(Session session, MessageConsumer consumer, MessageDispatcher messageDispatcher,
                           long pollingInterval, long receiveTimeout) {
        this.session = session;
        this.consumer = consumer;
        this.messageDispatcher = messageDispatcher;
        this.receiveInterval = pollingInterval;
        this.receiveTimeout = receiveTimeout;
    }

    private void poll() {
        try {
            Message message = null;
            if (!closed.get()) {
                message = this.consumer.receive(this.receiveTimeout);
            }
            if (Objects.isNull(message)) {
                return;
            }
            Semaphore semaphore = new Semaphore(0);
            OnMsgCallback callback = new OnMsgCallback(semaphore);
            this.messageDispatcher.onMessage(message, callback);
            // We suspend execution of poll cycle here before moving to the next cycle.
            // Once we receive signal from BVM via KafkaPollCycleFutureListener this suspension is removed
            // We will move to the next polling cycle.
            try {
                semaphore.acquire();
            } catch (InterruptedException e) {
                this.messageDispatcher.onError(e);
                this.pollingTaskFuture.cancel(false);
            }
        } catch (BError bError) {
            this.messageDispatcher.onError(bError);
        } catch (Exception e) {
            this.messageDispatcher.onError(e);
            // When un-recoverable exception is thrown we stop scheduling task to the executor.
            // Later at stopConsume() on KafkaRecordConsumer we close the consumer.
            this.pollingTaskFuture.cancel(false);
        }
    }

    public void consume() {
        this.pollingTaskFuture = this.executorService.scheduleAtFixedRate(
                this::poll, 0, this.receiveInterval, TimeUnit.MILLISECONDS);
    }

    public void stop() throws Exception {
        closed.set(true);
        this.consumer.close();
        this.session.close();
        this.pollingTaskFuture.cancel(true);
        try {
            this.executorService.awaitTermination(stopTimeout, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            // (Re-)Cancel if current thread also interrupted
            this.executorService.shutdownNow();
            // Preserve interrupt status
            Thread.currentThread().interrupt();
        }
    }
}
