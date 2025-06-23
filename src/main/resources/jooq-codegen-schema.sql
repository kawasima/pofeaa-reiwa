-- Schema creation script for jOOQ code generation
-- Based on DbSetup.java

-- Accounts table
CREATE TABLE IF NOT EXISTS accounts (
    id BIGINT NOT NULL,
    baseline_balance DECIMAL(10, 2) NOT NULL,
    currency VARCHAR(3) NOT NULL DEFAULT 'USD',
    account_type VARCHAR(20) NOT NULL DEFAULT 'SAVING',
    annual_interest_rate DECIMAL(8, 6),
    overdraft_limit DECIMAL(10, 2),
    overdraft_interest_rate DECIMAL(8, 6),
    CONSTRAINT pk_accounts PRIMARY KEY (id),
    CONSTRAINT chk_account_type CHECK (account_type IN ('SAVING', 'CHECKING'))
);

-- Activities table
CREATE TABLE IF NOT EXISTS activities (
    id BIGINT NOT NULL,
    owner_account_id BIGINT NOT NULL,
    source_account_id BIGINT NOT NULL,
    target_account_id BIGINT NOT NULL,
    timestamp TIMESTAMP NOT NULL,
    amount DECIMAL(10, 2) NOT NULL,
    currency VARCHAR(3) NOT NULL DEFAULT 'USD',
    CONSTRAINT pk_activities PRIMARY KEY (id),
    CONSTRAINT fk_activities_owner FOREIGN KEY (owner_account_id) REFERENCES accounts(id) ON DELETE CASCADE,
    CONSTRAINT chk_positive_amount CHECK (amount > 0)
);

-- Indexes
CREATE INDEX IF NOT EXISTS idx_activities_owner_timestamp ON activities(owner_account_id, timestamp);
CREATE INDEX IF NOT EXISTS idx_activities_source ON activities(source_account_id);
CREATE INDEX IF NOT EXISTS idx_activities_target ON activities(target_account_id);