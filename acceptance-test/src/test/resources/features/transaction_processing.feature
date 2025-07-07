Feature: Transaction Processing
  As a banking system user
  I want to process financial transactions
  So that I can manage account balances

  Background:
    Given the banking system is running
    And I have an existing account with sufficient balance

  @smoke @transaction
  Scenario Outline: Create transactions successfully
    Given I want to create a transaction with the following details:
      | accountId | amount | currency | direction | description |
      | <accountId> | <amount> | <currency> | <direction> | <description> |
    When I send a request to create the transaction
    Then the transaction should be created successfully
    And the response should contain the transaction details
    And the account balance should be updated correctly

    Examples:
      | accountId | amount | currency | direction | description |
      | <accountId> | 100.50 | EUR | IN | Salary payment |
      | <accountId> | 50.25 | EUR | OUT | Grocery shopping |
      | <accountId> | 200.00 | EUR | IN | Bonus payment |
      | <accountId> | 75.10 | EUR | OUT | Restaurant bill |

  @smoke @transaction
  Scenario: Retrieve transaction details successfully
    Given I have an existing transaction
    When I request the transaction details
    Then the transaction details should be returned successfully
    And the response should contain the correct transaction information

  @smoke @transaction
  Scenario: Retrieve account transactions successfully
    Given I have an account with multiple transactions
    When I request the account transactions
    Then the account transactions should be returned successfully
    And the response should contain a list of transactions
