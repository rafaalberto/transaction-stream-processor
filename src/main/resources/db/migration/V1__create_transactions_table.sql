CREATE TABLE transactions
(
    id                 UUID PRIMARY KEY,
    amount             NUMERIC(19, 2)           NOT NULL,
    currency           CHAR(3)                  NOT NULL,
    status             VARCHAR(32)              NOT NULL,
    type               VARCHAR(32)              NOT NULL,
    occurred_at        TIMESTAMP WITH TIME ZONE NOT NULL,
    created_at         TIMESTAMP WITH TIME ZONE NOT NULL,
    external_reference VARCHAR(100)             NOT NULL
);
