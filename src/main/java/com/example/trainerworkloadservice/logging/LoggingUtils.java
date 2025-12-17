package com.example.trainerworkloadservice.logging;

import org.slf4j.MDC;

import java.util.Optional;
import java.util.UUID;

public final class LoggingUtils {

    public static final String TRANSACTION_ID_KEY = "transactionId";

    private LoggingUtils() {
    }

    public static String ensureTransactionId() {
        String txId = MDC.get(TRANSACTION_ID_KEY);
        if (txId == null || txId.isBlank()) {
            txId = UUID.randomUUID().toString();
            MDC.put(TRANSACTION_ID_KEY, txId);
        }
        return txId;
    }

    public static Optional<String> getTransactionId() {
        return Optional.ofNullable(MDC.get(TRANSACTION_ID_KEY));
    }
}



