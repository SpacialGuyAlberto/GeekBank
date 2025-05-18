// src/main/java/com/geekbank/bank/models/SynchronizedResponse.java
package com.geekbank.bank.support.sync;

public class SynchronizedResponse {
    private int synchronizedCount;

    public SynchronizedResponse(int synchronizedCount) {
        this.synchronizedCount = synchronizedCount;
    }

    public int getSynchronized() {
        return synchronizedCount;
    }

    public void setSynchronized(int synchronizedCount) {
        this.synchronizedCount = synchronizedCount;
    }
}
