/*
 * Executed by the Postgres Docker entrypoint during first initialization.
 * NOTE: the database already exists at this step (POSTGRES_DB), so do not
 * run CREATE DATABASE statements here.
 */

-- Keep app objects in the default schema used by JPA.
CREATE SCHEMA IF NOT EXISTS public;

-- Optional DB-level defaults for new sessions.
ALTER DATABASE tinyfarm SET search_path TO public;
