/*
 * Leibniz Bioactives Cloud
 * Init script for database postgres 12.6
 * 
 * Copyright 2023 Leibniz-Institut f. Pflanzenbiochemie
 *  
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at 
 *  
 *     http://www.apache.org/licenses/LICENSE-2.0
 *  
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *  
 *=========================================================
 *
 * NOTE: TABLE CONSTRAINTS IN THIS FILE ARE RELAXED FOR TESTING PURPOSES:
 *
 *   - collection_id may be NULL in table 'files'
 *   - user_id may be NULL in table 'files'
 *
 *=========================================================
 *
 * define global vars 
 */
\set LBAC_SCHEMA_VERSION '\'00001\''

\set LBAC_SCHEMA lbac
\set LBAC_DATABASE lbac
\set LBAC_USER lbac
\set LBAC_PW lbac
--  quoted stuff --
\set LBAC_SCHEMA_QUOTED '\'' :LBAC_SCHEMA '\''
\set LBAC_DATABASE_QUOTED '\'' :LBAC_DATABASE '\''
\set LBAC_USER_QUOTED '\'' :LBAC_USER '\''
\set LBAC_PW_QUOTED '\'' :LBAC_PW '\''

/*
 *=========================================================
 *
 * terminate all session from database lbac --
 * getting db exclusive --
 */
SELECT pg_terminate_backend(pg_stat_activity.pid)
FROM pg_stat_activity
WHERE pg_stat_activity.datname = :LBAC_DATABASE_QUOTED
  AND pid <> pg_backend_pid();

/*
 * clean up 
 */
-- the following statement fails if LBAC_USER is not known!
-- REASSIGN OWNED BY :LBAC_USER TO postgres;
DROP SCHEMA IF EXISTS :LBAC_SCHEMA CASCADE;
DROP DATABASE IF EXISTS :LBAC_DATABASE;
DROP USER IF EXISTS :LBAC_USER;

/*
 * (re-)create database objects
 */
-- roles --
CREATE USER :LBAC_USER PASSWORD :LBAC_PW_QUOTED;
-- db --
CREATE DATABASE :LBAC_DATABASE WITH ENCODING 'UTF8' OWNER :LBAC_USER;

\connect :LBAC_DATABASE

-- schema --
CREATE SCHEMA AUTHORIZATION :LBAC_USER;

-- adjust schema search path --
ALTER USER :LBAC_USER SET search_path to :LBAC_SCHEMA,public;

-- privileges --
GRANT USAGE ON SCHEMA :LBAC_SCHEMA, public TO :LBAC_USER;
GRANT CONNECT, TEMPORARY, TEMP  ON  DATABASE :LBAC_DATABASE to :LBAC_USER;
GRANT SELECT, UPDATE, INSERT, DELETE ON ALL TABLES IN SCHEMA :LBAC_SCHEMA to :LBAC_USER;
GRANT EXECUTE ON ALL FUNCTIONS IN SCHEMA public to :LBAC_USER;
REVOKE ALL ON ALL TABLES IN SCHEMA :LBAC_SCHEMA FROM public;

\connect - :LBAC_USER

BEGIN TRANSACTION;

-- tables --

CREATE TABLE info ( 
    key VARCHAR NOT NULL PRIMARY KEY,
    value VARCHAR,
    owner_id INTEGER,
    aclist_id INTEGER
);
INSERT INTO info VALUES ('SETTING_JOB_SECRET', 'test_secret', null, null);

/**
 * - CRIMSy Jobs
 */
CREATE TABLE jobs (
    jobid       SERIAL NOT NULL PRIMARY KEY,
    input       BYTEA,
    jobdate     TIMESTAMP NOT NULL DEFAULT now(),
    jobtype     INTEGER NOT NULL,
    output      BYTEA,
    ownerid     INTEGER,
    queue       VARCHAR NOT NULL,
    status      INTEGER NOT NULL
);

COMMIT;
