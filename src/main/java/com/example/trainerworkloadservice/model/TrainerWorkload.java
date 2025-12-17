package com.example.trainerworkloadservice.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TrainerWorkload {
    private String trainerUsername;
    private String trainerFirstName;
    private String trainerLastName;
    private TrainerStatus trainerStatus;

    @Builder.Default
    private Map<Integer, YearSummary> years = new ConcurrentHashMap<>();
}



