-- # swap transaction
CREATE SCHEMA main;

CREATE TABLE IF NOT EXISTS main.address_holder
(
    holder_id   BIGSERIAL NOT NULL,
    address     character varying(66) COLLATE pg_catalog."default" NOT NULL,
    token       character varying(255) COLLATE pg_catalog."default" NOT NULL,
    amount      numeric  NOT NULL,
    update_time DATE NOT NULL DEFAULT now(),
    CONSTRAINT address_holder_pkey PRIMARY KEY (holder_id),
    CONSTRAINT address_holder_unq UNIQUE (address, token)
);

CREATE TABLE IF NOT EXISTS main.transfer_journal
(
    transfer_id   BIGSERIAL NOT NULL,
    address     character varying(66) COLLATE pg_catalog."default" NOT NULL,
    token       character varying(255) COLLATE pg_catalog."default" NOT NULL,
    amount      numeric  NOT NULL,
    create_time DATE NOT NULL,
    CONSTRAINT transfer_journal_pkey PRIMARY KEY (transfer_id)
);

CREATE TABLE IF NOT EXISTS main.swap_fee_event
(
    event_id         BIGSERIAL NOT NULL,
    token_first      character varying(255) COLLATE pg_catalog."default" NOT NULL,
    token_second     character varying(255) COLLATE pg_catalog."default" NOT NULL,
    swap_fee         bigint  NOT NULL,
    fee_out          bigint  NOT NULL,
    fee_addree       character varying(66) COLLATE pg_catalog."default" NOT NULL,
    signer           character varying(66) COLLATE pg_catalog."default" NOT NULL,
    ts               DATE    NOT NULL DEFAULT now(),
    CONSTRAINT swap_fee_event_pkey PRIMARY KEY (event_id)
);

CREATE TABLE IF NOT EXISTS main.transaction_payload
(
    transaction_hash character varying(66) COLLATE pg_catalog."default" NOT NULL,
    json_val            json                                            NOT NULL,
    CONSTRAINT transaction_payload_pkey PRIMARY KEY (transaction_hash)
);

--# swap transaction
CREATE TABLE IF NOT EXISTS main.swap_transaction
(
    swap_seq         BIGSERIAL NOT NULL,
    transaction_hash character varying(66) COLLATE pg_catalog."default"  NOT NULL,
    total_value      numeric,
    token_a          character varying(512) COLLATE pg_catalog."default" NOT NULL,
    amount_a         numeric                                             NOT NULL,
    token_b          character varying(512) COLLATE pg_catalog."default" NOT NULL,
    amount_b         numeric                                             NOT NULL,
    account          character varying(34) COLLATE pg_catalog."default"  NOT NULL,
    ts               bigint                                              NOT NULL,
    swap_type        smallint                                            NOT NULL,
    CONSTRAINT swap_transaction_pkey PRIMARY KEY (swap_seq),
    CONSTRAINT txn_hash_unq UNIQUE (transaction_hash)
);

--// swap天维度汇总统计
CREATE TABLE IF NOT EXISTS main.swap_day_stat
(
    stat_date DATE NOT NULL,
    volume    numeric,
    tvl       numeric,
    CONSTRAINT swap_day_stat_pkey PRIMARY KEY (stat_date)
);

CREATE TABLE IF NOT EXISTS main.token_swap_day_stat
(
    token_name    character varying(255) COLLATE pg_catalog."default" NOT NULL,
    ts            DATE                                                NOT NULL DEFAULT now(),
    volume_amount numeric                                             NOT NULL,
    volume        numeric,
    tvl_amount    numeric                                             NOT NULL,
    tvl           numeric,
    CONSTRAINT token_swap_day_stat_pkey PRIMARY KEY (token_name, ts)
);

CREATE TABLE IF NOT EXISTS main.token_price_day
(
    token_name    character varying(255) COLLATE pg_catalog."default" NOT NULL,
    ts            bigint                                              NOT NULL,
    price         numeric                                             NOT NULL,
    CONSTRAINT token_price_day_pkey PRIMARY KEY (token_name, ts)
);

CREATE TABLE IF NOT EXISTS main.token_price_stat
(
    token_name    character varying(255) COLLATE pg_catalog."default" NOT NULL,
    ts            DATE                                                NOT NULL DEFAULT now(),
    price         numeric                                             NOT NULL,
    max_price     numeric                                             NOT NULL,
    min_price     numeric                                             NOT NULL,
    rate          numeric,
    CONSTRAINT token_price_stat_pkey PRIMARY KEY (token_name, ts)
);


CREATE TABLE IF NOT EXISTS main.pool_swap_day_stat
(
    first_token_name  character varying(255) COLLATE pg_catalog."default" NOT NULL,
    second_token_name character varying(255) COLLATE pg_catalog."default" NOT NULL,
    ts                DATE                                                NOT NULL DEFAULT now(),
    volume_amount     numeric                                             NOT NULL,
    volume            numeric,
    tvl_a_amount      numeric                                             NOT NULL,
    tvl_a             numeric,
    tvl_b_amount      numeric                                             NOT NULL,
    tvl_b             numeric,
    CONSTRAINT pool_swap_day_stat_pkey PRIMARY KEY (first_token_name, second_token_name, ts)
);

CREATE TABLE IF NOT EXISTS main.pool_fee_day_stat
(
    first_token_name  character varying(255) COLLATE pg_catalog."default" NOT NULL,
    second_token_name character varying(255) COLLATE pg_catalog."default" NOT NULL,
    ts                DATE                                                NOT NULL DEFAULT now(),
    fees_amount     numeric                                             NOT NULL,
    fees            numeric,
    CONSTRAINT pool_fees_day_stat_pkey PRIMARY KEY (first_token_name, second_token_name, ts)
    );

CREATE TABLE IF NOT EXISTS main.handle_offset
(
    offset_id    character varying(50) COLLATE pg_catalog."default" NOT NULL,
    ts           bigint                                             NOT NULL,
    offset_value bigint                                             NOT NULL,
    CONSTRAINT handle_offset_pkey PRIMARY KEY (offset_id)
);