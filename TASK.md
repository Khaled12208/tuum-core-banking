# Software Engineer Test Assignment

## Description

Goal of this assignment is to familiarize candidates for working in Tuum and using the technologies we use daily.

## Requirements

The tasks consist of implementing a small core banking solution:

- **Account** to keep track of current accounts, balances, and transaction history
- **Capability** to publish messages into RabbitMQ for other consumers

Account service must contain integration tests and test coverage must be at least **80%**.

Please ensure that code is shared with us via Source code management platforms (Github, Gitlab etc).

## Technologies to Use

- Java 11+
- SpringBoot
- MyBatis
- Gradle
- Postgres
- RabbitMQ
- JUnit

## Applications

### Account Service

Account service keeps track of accounts, their balances, and transactions. Account service must publish all insert and update operations to RabbitMQ.

Application provides the following REST APIs:

#### 1. Create Account

Creates a bank account for the customer and returns an account object together with balance objects.

**Input:**

- Customer ID
- Country
- List of currencies (allowed values are EUR, SEK, GBP, USD)

**Output:**

- Account ID
- Customer ID
- List of balances:
  - Available amount
  - Currency

**Errors:**

- Invalid currency

> **Note:** API must create balances for the account in given currencies.

#### 2. Get Account

Return the account object.

**Input:**

- Account ID

**Output:**

- Account ID
- Customer ID
- List of balances:
  - Available amount
  - Currency

**Errors:**

- Account not found

#### 3. Create Transaction

Create a transaction on the account and return the transaction object.

**Input:**

- Account ID
- Amount
- Currency
- Direction of transaction (IN, OUT)
- Description

**Output:**

- Account ID
- Transaction ID
- Amount
- Currency
- Direction of transaction
- Description
- Balance after transaction

**Errors:**

- Invalid currency
- Invalid direction
- Invalid amount (if negative amount for example)
- Insufficient funds
- Account missing
- Description missing

> **Note:** Transactions with direction IN must increase account balance with the transaction amount and transactions with direction OUT must decrease the balance of account in respective currency.

#### 4. Get Transaction

Return a list of transactions.

**Input:**

- Account ID

**Output:**

- Account ID
- Transaction ID
- Amount
- Currency
- Direction of transaction
- Description

**Errors:**

- Invalid account

## Deliverables

- [ ] **Source code**
- [ ] **Instructions** on how to build and run applications
- [ ] **Dockerfile** and **docker-compose.yml** which includes database and RabbitMQ and initializes necessary database structure
- [ ] **Explanation** of important choices in your solution
- [ ] **Estimate** on how many transactions can your account application can handle per second on your development machine
- [ ] **Describe** what do you have to consider to be able to scale applications horizontally
