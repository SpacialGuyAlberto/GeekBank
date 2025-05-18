// src/main/java/com/geekbank/bank/models/ProgressResponse.java
package com.geekbank.bank.support.sync.model;

public class ProgressResponse {
    private int progress;

    public ProgressResponse(int progress) {
        this.progress = progress;
    }

    public int getProgress() {
        return progress;
    }

    public void setProgress(int progress) {
        this.progress = progress;
    }
}
