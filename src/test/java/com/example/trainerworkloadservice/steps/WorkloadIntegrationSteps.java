package com.example.trainerworkloadservice.steps;

import com.example.trainerworkloadservice.dto.ActionType;
import com.example.trainerworkloadservice.dto.WorkloadRequest;
import com.example.trainerworkloadservice.model.MonthSummary;
import com.example.trainerworkloadservice.model.TrainerWorkload;
import com.example.trainerworkloadservice.model.YearSummary;
import com.example.trainerworkloadservice.repository.InMemoryWorkloadRepository;
import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.cucumber.datatable.DataTable;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.Assertions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.config.JmsListenerEndpointRegistry;
import org.springframework.jms.listener.MessageListenerContainer;

import java.time.Duration;
import java.time.LocalDate;
import java.util.Map;
import java.util.Objects;

public class WorkloadIntegrationSteps {

    private static final String QUEUE_NAME = "trainer.workload.queue";

    @Autowired
    private JmsTemplate jmsTemplate;

    @Autowired
    private JmsListenerEndpointRegistry registry;

    @Autowired
    private InMemoryWorkloadRepository repository;

    private WorkloadRequest request;
    private String trainerUsername;

    @Before
    public void setUp() {
        repository.clear();
        registry.getListenerContainers().forEach(MessageListenerContainer::stop);
    }

    @Given("JMS listeners are running")
    public void jms_listeners_are_running() {
        registry.getListenerContainers().forEach(MessageListenerContainer::start);
    }

    @When("a workload message is published:")
    public void a_workload_message_is_published(DataTable table) {
        Map<String, String> row = table.asMaps(String.class, String.class).get(0);
        this.request = toRequest(row);
        this.trainerUsername = request.getTrainerUsername();

        WorkloadRequest payload = Objects.requireNonNull(request, "Workload request must be initialized");
        jmsTemplate.convertAndSend(QUEUE_NAME, payload, message -> {
            message.setStringProperty("transactionId", "it-test-tx");
            return message;
        });
    }

    @Then("the workload is stored with {long} minutes for {int}-{int}")
    public void the_workload_is_stored(long expectedMinutes, int year, int month) {
        Awaitility.await()
                .atMost(Duration.ofSeconds(5))
                .untilAsserted(() -> assertWorkload(expectedMinutes, year, month));
    }

    private void assertWorkload(long expectedMinutes, int year, int month) {
        TrainerWorkload workload = repository.findByUsername(trainerUsername);
        Assertions.assertNotNull(workload, "Trainer workload should be saved");

        YearSummary yearSummary = workload.getYears().get(year);
        Assertions.assertNotNull(yearSummary, "Year summary should exist");

        MonthSummary monthSummary = yearSummary.getMonths().get(month);
        Assertions.assertNotNull(monthSummary, "Month summary should exist");
        Assertions.assertEquals(expectedMinutes, monthSummary.getTotalTrainingDuration(), "Monthly duration should match");
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

