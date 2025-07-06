CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;

CREATE TYPE currency_enum AS ENUM ('EUR', 'SEK', 'GBP', 'USD');
CREATE TYPE direction_enum AS ENUM ('IN', 'OUT');
CREATE TYPE status_enum AS ENUM ('PENDING', 'COMPLETED', 'FAILED');

-- Accounts table
CREATE TABLE IF NOT EXISTS accounts (
    account_id VARCHAR(50) PRIMARY KEY,
    customer_id VARCHAR(50) NOT NULL,
    country VARCHAR(3) NOT NULL,
    account_name VARCHAR(200),
    account_type VARCHAR(50),
    idempotency_key VARCHAR(100) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(idempotency_key)
);

-- Balances table
CREATE TABLE IF NOT EXISTS balances (
    balance_id VARCHAR(50) PRIMARY KEY,
    account_id VARCHAR(50) NOT NULL,
    currency currency_enum NOT NULL,
    available_amount DECIMAL(15,2) NOT NULL DEFAULT 0.00,
    version_number INTEGER NOT NULL DEFAULT 1,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (account_id) REFERENCES accounts(account_id) ON DELETE CASCADE,
    UNIQUE(account_id, currency)
);

-- Transactions table
CREATE TABLE IF NOT EXISTS transactions (
    transaction_id VARCHAR(50) PRIMARY KEY,
    account_id VARCHAR(50) NOT NULL,
    balance_id VARCHAR(50) NOT NULL,
    amount DECIMAL(15,2) NOT NULL,
    currency currency_enum NOT NULL,
    direction direction_enum NOT NULL,
    description TEXT,
    balance_after_transaction DECIMAL(15,2) NOT NULL,
    status status_enum NOT NULL DEFAULT 'COMPLETED',
    idempotency_key VARCHAR(100) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (account_id) REFERENCES accounts(account_id) ON DELETE CASCADE,
    FOREIGN KEY (balance_id) REFERENCES balances(balance_id) ON DELETE CASCADE,
    UNIQUE(idempotency_key)
);

-- Processed messages table for idempotency
CREATE TABLE IF NOT EXISTS processed_messages (
    id SERIAL PRIMARY KEY,
    message_id VARCHAR(100) NOT NULL,
    message_type VARCHAR(50) NOT NULL,
    idempotency_key VARCHAR(100) NOT NULL,
    processed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    result_data TEXT,
    UNIQUE(idempotency_key)
);

-- Indexes for better performance
CREATE INDEX IF NOT EXISTS idx_accounts_customer_id ON accounts(customer_id);
CREATE INDEX IF NOT EXISTS idx_balances_account_id ON balances(account_id);
CREATE INDEX IF NOT EXISTS idx_transactions_account_id ON transactions(account_id);
CREATE INDEX IF NOT EXISTS idx_transactions_created_at ON transactions(created_at);
CREATE INDEX IF NOT EXISTS idx_processed_messages_message_id ON processed_messages(message_id);

-- Insert sample data for testing
INSERT INTO accounts (account_id, customer_id, country, account_name, account_type, idempotency_key) VALUES
('ACC123456', 'CUST001', 'EE', 'Test Account 1', 'SAVINGS', 'test-key-1'),
('ACC789012', 'CUST002', 'SE', 'Test Account 2', 'CHECKING', 'test-key-2')
ON CONFLICT (idempotency_key) DO NOTHING;

INSERT INTO balances (balance_id, account_id, currency, available_amount, version_number) VALUES
('BAL001', 'ACC123456', 'EUR', 1000.00, 1),
('BAL002', 'ACC123456', 'USD', 500.00, 1),
('BAL003', 'ACC789012', 'SEK', 2500.00, 1)
ON CONFLICT (account_id, currency) DO NOTHING;

-- Test data with international characters
INSERT INTO accounts (account_id, customer_id, country, account_name, account_type, idempotency_key) VALUES
('ACC345678', 'CUST003', 'EE', 'Eesti konto', 'SAVINGS', 'test-key-3'),
('ACC901234', 'CUST004', 'SA', 'حساب تجريبي', 'CHECKING', 'test-key-4')
ON CONFLICT (idempotency_key) DO NOTHING;
