package com.example.trainerworkloadservice.steps;

import com.example.trainerworkloadservice.dto.ActionType;
import com.example.trainerworkloadservice.dto.WorkloadRequest;
import com.example.trainerworkloadservice.model.MonthSummary;
import com.example.trainerworkloadservice.model.TrainerStatus;
import com.example.trainerworkloadservice.model.TrainerWorkload;
import com.example.trainerworkloadservice.model.YearSummary;
import com.example.trainerworkloadservice.repository.InMemoryWorkloadRepository;
import com.example.trainerworkloadservice.service.WorkloadService;
import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.cucumber.datatable.DataTable;
import org.junit.jupiter.api.Assertions;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;
import java.util.Map;

public class WorkloadComponentSteps {

    @Autowired
    private WorkloadService workloadService;

    @Autowired
    private InMemoryWorkloadRepository repository;

    private WorkloadRequest request;

    @Before
    public void resetRepository() {
        repository.clear();
    }

    @Given("a workload request:")
    public void a_workload_request(DataTable table) {
        Map<String, String> row = table.asMaps(String.class, String.class).get(0);
        this.request = toRequest(row);
    }

    @Given("trainer {string} already has {long} minutes recorded for {int}-{int}")
    public void trainer_already_has_minutes(String username, long minutes, int year, int month) {
        TrainerWorkload workload = TrainerWorkload.builder()
                .trainerUsername(username)
                .trainerFirstName("Pre")
                .trainerLastName("Loaded")
                .trainerStatus(TrainerStatus.ACTIVE)
                .build();
        YearSummary yearSummary = YearSummary.builder().year(year).build();
        MonthSummary monthSummary = MonthSummary.builder().month(month).totalTrainingDuration(minutes).build();
        yearSummary.getMonths().put(month, monthSummary);
        workload.getYears().put(year, yearSummary);
        repository.save(workload);
    }

    @When("the workload is processed")
    public void the_workload_is_processed() {
        workloadService.processWorkload(request);
    }

    @Then("trainer {string} has total monthly duration {long} minutes for {int}-{int}")
    public void trainer_has_total_monthly_duration(String username, long expectedMinutes, int year, int month) {
        TrainerWorkload workload = repository.findByUsername(username);
        Assertions.assertNotNull(workload, "Trainer workload should exist");

        YearSummary yearSummary = workload.getYears().get(year);
        Assertions.assertNotNull(yearSummary, "Year summary should exist");

        MonthSummary monthSummary = yearSummary.getMonths().get(month);
        Assertions.assertNotNull(monthSummary, "Month summary should exist");
        Assertions.assertEquals(expectedMinutes, monthSummary.getTotalTrainingDuration(), "Monthly duration should match");
    }

    @Then("trainer {string} has status {string}")
    public void trainer_has_status(String username, String expectedStatus) {
        TrainerWorkload workload = repository.findByUsername(username);
        Assertions.assertNotNull(workload, "Trainer workload should exist");
        Assertions.assertEquals(TrainerStatus.valueOf(expectedStatus), workload.getTrainerStatus());
    }

    private WorkloadRequest toRequest(Map<String, String> row) {
        WorkloadRequest req = new WorkloadRequest();
        req.setTrainerUsername(row.get("username"));
        req.setTrainerFirstName(row.get("firstName"));
        req.setTrainerLastName(row.get("lastName"));
        req.setIsActive(Boolean.parseBoolean(row.get("active")));
        req.setTrainingDate(LocalDate.parse(row.get("date")));
        req.setTrainingDuration(Integer.parseInt(row.get("duration")));
        req.setActionType(ActionType.valueOf(row.get("action")));
        return req;
    }
}

