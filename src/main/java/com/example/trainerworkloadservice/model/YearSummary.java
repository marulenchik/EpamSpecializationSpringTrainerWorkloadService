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
public class YearSummary {
    private int year;

    @Builder.Default
    private Map<Integer, MonthSummary> months = new ConcurrentHashMap<>();
}

