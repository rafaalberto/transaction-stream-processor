ALTER TABLE transactions
    ADD CONSTRAINT uk_transactions_external_reference
        UNIQUE (external_reference);
