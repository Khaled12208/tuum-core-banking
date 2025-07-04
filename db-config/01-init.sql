-- Database initialization script for Tuum Core Banking
-- This script creates the complete schema for the banking system

-- Enable UUID extension
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Processed messages tracking table
CREATE TABLE IF NOT EXISTS processed_messages (
    message_id VARCHAR(100) PRIMARY KEY,
    message_type VARCHAR(20) NOT NULL CHECK (message_type IN ('CREATE_ACCOUNT', 'CREATE_TRANSACTION')),
    processed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    result_data JSONB,
    CONSTRAINT idx_processed_at UNIQUE (processed_at)
);

-- Create index for processed_at
CREATE INDEX IF NOT EXISTS idx_processed_messages_processed_at ON processed_messages(processed_at);

-- Accounts table
CREATE TABLE IF NOT EXISTS accounts (
    account_id VARCHAR(50) PRIMARY KEY,
    customer_id VARCHAR(50) NOT NULL,
    country VARCHAR(3) NOT NULL, 
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create index for customer_id
CREATE INDEX IF NOT EXISTS idx_accounts_customer_id ON accounts(customer_id);

-- Balances table (one record per currency per account)
CREATE TABLE IF NOT EXISTS balances (
    balance_id VARCHAR(50) PRIMARY KEY,
    account_id VARCHAR(50) NOT NULL,
    currency VARCHAR(4) NOT NULL CHECK (currency IN ('EUR', 'SEK', 'GBP', 'USD')),
    available_amount DECIMAL(15, 2) NOT NULL DEFAULT 0.00,
    version_number INT NOT NULL DEFAULT 1, -- Optimistic locking
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_balances_account FOREIGN KEY (account_id) REFERENCES accounts(account_id) ON DELETE CASCADE,
    CONSTRAINT unique_account_currency UNIQUE (account_id, currency),
    CONSTRAINT check_positive_balance CHECK (available_amount >= 0) -- Ensure non-negative balances
);

-- Create indexes for balances
CREATE INDEX IF NOT EXISTS idx_balances_account_id ON balances(account_id);
CREATE INDEX IF NOT EXISTS idx_balances_currency ON balances(currency);

-- Transactions table
CREATE TABLE IF NOT EXISTS transactions (
    transaction_id VARCHAR(50) PRIMARY KEY, -- Use idempotency key from message
    account_id VARCHAR(50) NOT NULL,
    amount DECIMAL(15, 2) NOT NULL,
    currency VARCHAR(4) NOT NULL CHECK (currency IN ('EUR', 'SEK', 'GBP', 'USD')),
    direction VARCHAR(3) NOT NULL CHECK (direction IN ('IN', 'OUT')),
    description VARCHAR(255) NOT NULL,
    balance_after DECIMAL(15, 2) NOT NULL,
    status VARCHAR(10) DEFAULT 'PENDING' CHECK (status IN ('PENDING', 'COMPLETED', 'FAILED')),
    idempotency_key VARCHAR(100) NOT NULL, -- From queue message
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_transactions_account FOREIGN KEY (account_id) REFERENCES accounts(account_id) ON DELETE CASCADE,
    CONSTRAINT unique_idempotency UNIQUE (idempotency_key),
    CONSTRAINT check_positive_amount CHECK (amount > 0) -- Ensure positive transaction amounts
);

-- Create indexes for transactions
CREATE INDEX IF NOT EXISTS idx_transactions_account_id ON transactions(account_id);
CREATE INDEX IF NOT EXISTS idx_transactions_created_at ON transactions(created_at);
CREATE INDEX IF NOT EXISTS idx_transactions_status ON transactions(status);
CREATE INDEX IF NOT EXISTS idx_transactions_account_currency ON transactions(account_id, currency);

-- Update triggers for updated_at columns
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Create triggers for updated_at columns
DROP TRIGGER IF EXISTS trg_update_updated_at_accounts ON accounts;
CREATE TRIGGER trg_update_updated_at_accounts
    BEFORE UPDATE ON accounts
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

DROP TRIGGER IF EXISTS trg_update_updated_at_balances ON balances;
CREATE TRIGGER trg_update_updated_at_balances
    BEFORE UPDATE ON balances
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

-- Sample data for testing
-- Insert sample accounts
INSERT INTO accounts (account_id, customer_id, country) VALUES 
    ('ACC001', 'CUST001', 'EST'),
    ('ACC002', 'CUST001', 'SWE'),
    ('ACC003', 'CUST002', 'GBR'),
    ('ACC004', 'CUST003', 'USA')
ON CONFLICT (account_id) DO NOTHING;

-- Insert sample balances
INSERT INTO balances (balance_id, account_id, currency, available_amount) VALUES 
    ('BAL001', 'ACC001', 'EUR', 1000.00),
    ('BAL002', 'ACC002', 'SEK', 5000.00),
    ('BAL003', 'ACC003', 'GBP', 750.00),
    ('BAL004', 'ACC004', 'USD', 1200.00)
ON CONFLICT (balance_id) DO NOTHING;
