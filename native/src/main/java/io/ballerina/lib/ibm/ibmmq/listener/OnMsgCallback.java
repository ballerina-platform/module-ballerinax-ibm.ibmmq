package io.ballerina.lib.ibm.ibmmq.listener;

import io.ballerina.runtime.api.values.BError;

import java.util.concurrent.Semaphore;

/**
 * {@code OnMsgCallback} provides ability control poll cycle flow by notifications received from Ballerina
 * IBM MQ service.
 *
 * @since 1.3.0.
 */
public class OnMsgCallback {
    private final Semaphore semaphore;

    OnMsgCallback(Semaphore semaphore) {
        this.semaphore = semaphore;
    }

    public void notifySuccess(Object obj) {
        semaphore.release();
        if (obj instanceof BError bError) {
            bError.printStackTrace();
        }
    }

    public void notifyFailure(BError bError) {
        semaphore.release();
        bError.printStackTrace();
    }
}
