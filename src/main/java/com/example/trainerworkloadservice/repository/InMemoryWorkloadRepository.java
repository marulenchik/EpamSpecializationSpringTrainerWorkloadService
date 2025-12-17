package com.example.trainerworkloadservice.repository;

import com.example.trainerworkloadservice.model.TrainerWorkload;
import org.springframework.stereotype.Repository;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Supplier;

@Repository
public class InMemoryWorkloadRepository implements WorkloadRepository {

    private final ConcurrentMap<String, TrainerWorkload> store = new ConcurrentHashMap<>();

    @Override
    public TrainerWorkload findByUsername(String trainerUsername) {
        return store.get(trainerUsername);
    }

    @Override
    public TrainerWorkload save(TrainerWorkload workload) {
        store.put(workload.getTrainerUsername(), workload);
        return workload;
    }

    @Override
    public TrainerWorkload getOrCreate(String trainerUsername, Supplier<TrainerWorkload> creator) {
        return store.computeIfAbsent(trainerUsername, key -> creator.get());
    }
}



