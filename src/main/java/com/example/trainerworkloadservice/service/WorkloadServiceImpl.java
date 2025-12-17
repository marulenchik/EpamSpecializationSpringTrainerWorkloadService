package com.example.trainerworkloadservice.service;

import com.example.trainerworkloadservice.dto.ActionType;
import com.example.trainerworkloadservice.dto.WorkloadRequest;
import com.example.trainerworkloadservice.logging.LoggingUtils;
import com.example.trainerworkloadservice.model.MonthSummary;
import com.example.trainerworkloadservice.model.TrainerStatus;
import com.example.trainerworkloadservice.model.TrainerWorkload;
import com.example.trainerworkloadservice.model.YearSummary;
import com.example.trainerworkloadservice.repository.WorkloadRepository;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.YearMonth;

@Service
@RequiredArgsConstructor
@Slf4j
public class WorkloadServiceImpl implements WorkloadService {

    private final WorkloadRepository repository;

    @Override
    @CircuitBreaker(name = "workloadService", fallbackMethod = "processWorkloadFallback")
    public void processWorkload(WorkloadRequest request) {
        String txId = LoggingUtils.ensureTransactionId();
        log.info("tx={} Processing workload action={} for trainer={}", txId, request.getActionType(), request.getTrainerUsername());

        TrainerWorkload workload = repository.getOrCreate(request.getTrainerUsername(), () -> buildFromRequest(request));
        updateTrainerProfile(workload, request);
        applyTrainingDuration(workload, request);

        repository.save(workload);
        log.info("tx={} Completed workload update for trainer={}", txId, workload.getTrainerUsername());
    }

    // Circuit breaker fallback to keep service resilient and log failure context.
    @SuppressWarnings("unused")
    private void processWorkloadFallback(WorkloadRequest request, Throwable throwable) {
        String txId = LoggingUtils.ensureTransactionId();
        log.error("tx={} Circuit breaker fallback executed for trainer={} due to {}", txId, request.getTrainerUsername(), throwable.getMessage(), throwable);
        throw new IllegalStateException("Workload service currently unavailable", throwable);
    }

    private TrainerWorkload buildFromRequest(WorkloadRequest request) {
        return TrainerWorkload.builder()
                .trainerUsername(request.getTrainerUsername())
                .trainerFirstName(request.getTrainerFirstName())
                .trainerLastName(request.getTrainerLastName())
                .trainerStatus(request.getIsActive() ? TrainerStatus.ACTIVE : TrainerStatus.INACTIVE)
                .build();
    }

    private void updateTrainerProfile(TrainerWorkload workload, WorkloadRequest request) {
        workload.setTrainerFirstName(request.getTrainerFirstName());
        workload.setTrainerLastName(request.getTrainerLastName());
        workload.setTrainerStatus(request.getIsActive() ? TrainerStatus.ACTIVE : TrainerStatus.INACTIVE);
    }

    private void applyTrainingDuration(TrainerWorkload workload, WorkloadRequest request) {
        YearMonth yearMonth = YearMonth.from(request.getTrainingDate());
        YearSummary yearSummary = workload.getYears()
                .computeIfAbsent(yearMonth.getYear(), year -> YearSummary.builder().year(year).build());

        MonthSummary monthSummary = yearSummary.getMonths()
                .computeIfAbsent(yearMonth.getMonthValue(), month -> MonthSummary.builder()
                        .month(month)
                        .totalTrainingDuration(0)
                        .build());

        long updatedDuration = switch (request.getActionType()) {
            case ADD -> monthSummary.getTotalTrainingDuration() + request.getTrainingDuration();
            case DELETE -> Math.max(0, monthSummary.getTotalTrainingDuration() - request.getTrainingDuration());
        };

        // Prevent negative totals and keep operation-level logs tied to the transaction.
        log.info("tx={} Updating month={} year={} action={} previousDuration={} newDuration={}",
                LoggingUtils.ensureTransactionId(),
                yearMonth.getMonthValue(),
                yearMonth.getYear(),
                request.getActionType(),
                monthSummary.getTotalTrainingDuration(),
                updatedDuration);

        monthSummary.setTotalTrainingDuration(updatedDuration);
    }
}



