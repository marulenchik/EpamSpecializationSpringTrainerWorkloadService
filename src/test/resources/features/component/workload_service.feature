Feature: Manage trainer workload via the domain service

  Scenario: Adding training time updates totals and keeps trainer active
    Given a workload request:
      | username | firstName | lastName | active | date       | duration | action |
      | alice    | Alice     | Smith    | true   | 2025-01-15 | 60       | ADD    |
    When the workload is processed
    Then trainer "alice" has total monthly duration 60 minutes for 2025-1
    And trainer "alice" has status "ACTIVE"

  Scenario: Deleting training time cannot reduce the total below zero
    Given trainer "bob" already has 30 minutes recorded for 2025-2
    And a workload request:
      | username | firstName | lastName | active | date       | duration | action |
      | bob      | Bob       | Stone    | true   | 2025-02-10 | 45       | DELETE |
    When the workload is processed
    Then trainer "bob" has total monthly duration 0 minutes for 2025-2
    And trainer "bob" has status "ACTIVE"

