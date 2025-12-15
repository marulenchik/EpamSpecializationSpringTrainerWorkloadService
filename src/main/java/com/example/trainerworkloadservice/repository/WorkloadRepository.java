package com.example.trainerworkloadservice.repository;

import com.example.trainerworkloadservice.model.TrainerWorkload;

import java.util.function.Supplier;

public interface WorkloadRepository {

    TrainerWorkload findByUsername(String trainerUsername);

    TrainerWorkload save(TrainerWorkload workload);

    TrainerWorkload getOrCreate(String trainerUsername, Supplier<TrainerWorkload> creator);
}

