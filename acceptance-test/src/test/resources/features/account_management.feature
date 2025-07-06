Feature: Account Management
  As a banking system user
  I want to manage customer accounts
  So that I can create and retrieve account information

  Background:
    Given the banking system is running
    And I have a unique customer ID

  @smoke @account
  Scenario Outline: Create a new account successfully
    Given I want to create a new account with the following details:
      | customerId | country | currencies |
      | <customerId> | <country> | <currencies> |
    When I send a request to create the account
    Then the account should be created successfully
    And the response should contain the account details
    And the account should have the following balances initialized:
      | currency | expectedAmount |
      | <currencies> | 0.0 |

    Examples:
      | customerId | country | currencies |
      | CUST_001   | EE      | EUR        |
      | CUST_002   | EE      | USD        |
      | CUST_003   | EE      | SEK        |
      | CUST_004   | EE      | EUR,USD    |
      | CUST_005   | EE      | EUR,USD,SEK|

  @smoke @account
  Scenario: Retrieve account details successfully
    Given I have an existing account
    When I request the account details
    Then the account details should be returned successfully
    And the response should contain the correct account information

  @smoke @account
  Scenario: Retrieve account balances successfully
    Given I have an existing account with multiple currencies
    When I request the account balances
    Then the account balances should be returned successfully
    And each currency should have a balance amount

  @exact-match
  Scenario: Create a new account with exact curl payload
    Given I want to create a new account with exact payload:
      | customerId | country | currencies |
      | 12345      | EE      | EUR        |
    When I send a request to create the account
    Then the account should be created successfully
    And the response should contain the account details 