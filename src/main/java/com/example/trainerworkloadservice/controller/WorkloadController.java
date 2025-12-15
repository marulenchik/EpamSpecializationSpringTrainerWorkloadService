package com.example.trainerworkloadservice.controller;

import com.example.trainerworkloadservice.dto.WorkloadRequest;
import com.example.trainerworkloadservice.service.WorkloadService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/workload")
@RequiredArgsConstructor
@Slf4j
public class WorkloadController {

    private final WorkloadService workloadService;

    @PostMapping
    public ResponseEntity<Map<String, String>> handleWorkload(@Valid @RequestBody WorkloadRequest request) {
        log.info("Dispatching workload request for trainer {}", request.getTrainerUsername());
        workloadService.processWorkload(request);
        return ResponseEntity.ok(Map.of("message", "Workload processed"));
    }
}

