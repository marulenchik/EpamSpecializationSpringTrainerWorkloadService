Feature: Consume workload messages from the queue

  Scenario: Incoming JMS message is persisted through the listener
    Given JMS listeners are running
    When a workload message is published:
      | username | firstName | lastName | active | date       | duration | action |
      | carol    | Carol     | Wayne    | true   | 2025-03-05 | 45       | ADD    |
    Then the workload is stored with 45 minutes for 2025-3

  Scenario: Delete messages do not reduce totals below zero
    Given trainer "dave" already has 20 minutes recorded for 2025-4
    And JMS listeners are running
    When a workload message is published:
      | username | firstName | lastName | active | date       | duration | action |
      | dave     | Dave      | Miles    | true   | 2025-04-01 | 25       | DELETE |
    Then the workload is stored with 0 minutes for 2025-4

