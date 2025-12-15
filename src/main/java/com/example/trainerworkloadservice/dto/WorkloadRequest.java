package com.example.trainerworkloadservice.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.time.LocalDate;

@Data
public class WorkloadRequest {

    @NotBlank
    private String trainerUsername;

    @NotBlank
    private String trainerFirstName;

    @NotBlank
    private String trainerLastName;

    @NotNull
    private Boolean isActive;

    @NotNull
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate trainingDate;

    @Positive
    private int trainingDuration;

    @NotNull
    private ActionType actionType;
}

