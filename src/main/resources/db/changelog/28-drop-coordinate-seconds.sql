-- liquibase formatted sql

-- changeset jlandau2:36
ALTER TABLE coordinate DROP COLUMN seconds;
-- rollback alter table coordinate add secondsINT(11) NOT NULL;