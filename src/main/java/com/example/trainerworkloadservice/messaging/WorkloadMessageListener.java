package com.example.trainerworkloadservice.messaging;

import com.example.trainerworkloadservice.dto.WorkloadRequest;
import com.example.trainerworkloadservice.logging.LoggingUtils;
import com.example.trainerworkloadservice.service.WorkloadService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.messaging.handler.annotation.Headers;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class WorkloadMessageListener {

    private static final String QUEUE_NAME = "trainer.workload.queue";
    private static final String TX_HEADER = "transactionId";

    private final WorkloadService workloadService;

    @JmsListener(destination = QUEUE_NAME, containerFactory = "jmsListenerContainerFactory")
    public void onMessage(WorkloadRequest request, @Headers Map<String, Object> headers) {
        String txId = Optional.ofNullable(headers.get(TX_HEADER))
                .map(Object::toString)
                .orElseGet(LoggingUtils::ensureTransactionId);

        MDC.put(LoggingUtils.TRANSACTION_ID_KEY, txId);
        try {
            log.info("tx={} Received workload message for trainer {}", txId, request.getTrainerUsername());
            workloadService.processWorkload(request);
        } finally {
            MDC.remove(LoggingUtils.TRANSACTION_ID_KEY);
        }
    }
}


