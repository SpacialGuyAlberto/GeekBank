package com.geekbank.bank.models;

public enum TransactionStatus {
    PENDING,
    COMPLETED,
    FAILED,
    CANCELLED,
    EXPIRED,
    AWAITING_MANUAL_PROCESSING // Nuevo estado
}
