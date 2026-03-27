ALTER TABLE transactions
    ADD COLUMN account_id UUID NOT NULL;
CREATE INDEX idx_transactions_account_id ON transactions (account_id);