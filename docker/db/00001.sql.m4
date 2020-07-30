include(dist/etc/config_m4.inc)dnl
/*
 * Leibniz Bioactives Cloud
 * Init script for database postgres 9.6
 * 
 * Copyright 2017 Leibniz-Institut f. Pflanzenbiochemie
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
 */

/*
 * define global vars 
 */
\set LBAC_SCHEMA_VERSION '\'00005\''

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

-- check for usefull extensions and install it --
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS "pgchem_tigress";

\connect - :LBAC_USER

BEGIN TRANSACTION;

-- tables --

/*
 * Nodes, Clouds, etc.
 */
CREATE TABLE nodes (
  id          UUID    NOT NULL PRIMARY KEY,
  baseUrl     VARCHAR NOT NULL,
  institution VARCHAR NOT NULL,
  local       BOOLEAN NOT NULL DEFAULT FALSE,
  publicNode  BOOLEAN NOT NULL DEFAULT FALSE,
  version     VARCHAR NOT NULL DEFAULT '00001',
  publickey   VARCHAR NOT NULL DEFAULT 'dummy'
);

CREATE TABLE clouds (
    id          BIGSERIAL NOT NULL PRIMARY KEY,
    name        VARCHAR,
    UNIQUE (name)
);
INSERT INTO clouds (name) VALUES ('LBAC_PRIMARY_CLOUD');

CREATE TABLE cloud_nodes (
    id          BIGSERIAL NOT NULL PRIMARY KEY,
    node_id     UUID NOT NULL REFERENCES nodes(id) ON UPDATE CASCADE ON DELETE CASCADE,
    cloud_id    BIGINT NOT NULL REFERENCES clouds(id) ON UPDATE CASCADE ON DELETE CASCADE,
    rank        INTEGER NOT NULL DEFAULT 1,
    publickey   VARCHAR NOT NULL DEFAULT 'dummy',
    failures    INTEGER NOT NULL DEFAULT 0,
    retrytime   BIGINT NOT NULL DEFAULT 0,
    UNIQUE (cloud_id, node_id)
);


/* definition of master node (will be skipped on master node) */
LBAC_MASTER_SKIP INSERT INTO nodes (id, baseUrl, institution, local) VALUES
LBAC_MASTER_SKIP ('LBAC_MASTER_NODE_ID', 'LBAC_MASTER_URL', 'LBAC_MASTER_INSTITUTION', False);

/* definition of local node */
INSERT INTO nodes (id, baseUrl, institution, local) VALUES
  ( 'LBAC_NODE_ID',
    'https://LBAC_INTERNET_FQHN:8443/ui',
    'LBAC_INSTITUTION_SHORT', True);

INSERT INTO cloud_nodes (node_id, cloud_id, rank) 
  SELECT 'LBAC_NODE_ID'::UUID AS node_id, id AS cloud_id, LBAC_NODE_RANK AS rank 
  FROM clouds WHERE name='LBAC_PRIMARY_CLOUD';
LBAC_MASTER_SKIP INSERT INTO cloud_nodes (node_id, cloud_id, rank) 
LBAC_MASTER_SKIP  SELECT 'LBAC_MASTER_NODE_ID'::UUID AS node_id, id AS cloud_id, 10 AS rank
LBAC_MASTER_SKIP  FROM clouds WHERE name='LBAC_PRIMARY_CLOUD';

/*
 * Admission 
 * users, groups, memberships 
 */
CREATE TABLE usersGroups (
    id                  UUID NOT NULL PRIMARY KEY,
    memberType          VARCHAR(1) NOT NULL,
    subSystemType       INTEGER,
    subSystemData       VARCHAR,
    modified            TIMESTAMP DEFAULT now(),
    node_id             UUID REFERENCES nodes(id) ON UPDATE CASCADE ON DELETE CASCADE,
    login               VARCHAR,
    name                VARCHAR,
    email               VARCHAR,
    password            VARCHAR,
    phone               VARCHAR
);

CREATE TABLE memberships (
    id          UUID NOT NULL PRIMARY KEY,
    group_id    UUID NOT NULL REFERENCES usersGroups (id) ON UPDATE CASCADE ON DELETE CASCADE,
    member_id   UUID NOT NULL REFERENCES usersGroups (id) ON UPDATE CASCADE ON DELETE CASCADE,
    nested      BOOLEAN NOT NULL DEFAULT FALSE,
    UNIQUE(group_id, member_id)
);

CREATE INDEX i_memberships_group ON memberships (group_id);
CREATE INDEX i_memberships_member ON memberships (member_id);


CREATE TABLE nestingpathsets (
    id  UUID NOT NULL PRIMARY KEY,
    membership_id UUID NOT NULL REFERENCES memberships(id) ON UPDATE CASCADE ON DELETE CASCADE 
);

CREATE TABLE membership_nestingpathsets (
        membership_id           UUID NOT NULL REFERENCES memberships(id) ON UPDATE CASCADE ON DELETE CASCADE,
        nestingpathset_id       UUID NOT NULL REFERENCES nestingpathsets(id) ON UPDATE CASCADE ON DELETE CASCADE,
        UNIQUE(nestingpathset_id, membership_id)
);
/* 
 * ToDo: Do some database sanitation for memberships.  
 * This cleanup / sanitation is necessary, when the membership table 
 * is manipulated out of band, because the table 'nestingpathsets' is not 
 * covered by referential integrity, i.e. deleting memberships via 
 * 'DELETE FROM memberships ...' will cover all other tables but not 
 * 'removtingpathsets'.
 * 
 *   CREATE OR REPLACE FUNCTION cleanNestingPathSets ( id UUID ) RETURNS INTEGER
 *   ...
 *   CREATE TRIGGER ... AFTER DELETE ON membership_nestingpathsets EXECUTE PROCEDURE cleanNestingPathSets(OLD.nestingpathset_id); 
 *
 * Alternatively on could run the following SQL command manually:
 * 
 *   DELETE FROM nestingpathsets AS np USING (SELECT id FROM nestingpathsets 
 *   EXCEPT SELECT nestingpathset_id FROM membership_nestingpathsets) AS e WHERE np.id=e.id;
 *
 */

CREATE TABLE nestingpathset_memberships (
        nestingpathsets_id      UUID NOT NULL REFERENCES nestingpathsets(id) ON UPDATE CASCADE ON DELETE CASCADE,
        memberships_id          UUID NOT NULL REFERENCES memberships(id) ON UPDATE CASCADE ON DELETE CASCADE,
        UNIQUE(nestingpathsets_id, memberships_id)
);

/*
 * Admission
 * ACLs
 */
CREATE TABLE aclists (
    id          UUID PRIMARY KEY,
    name        VARCHAR,
    permCode    INTEGER
);

CREATE TABLE acentries (
    aclist_id   UUID NOT NULL REFERENCES aclists(id) ON UPDATE CASCADE ON DELETE CASCADE,
    member_id   UUID NOT NULL REFERENCES usersGroups(id) ON UPDATE CASCADE ON DELETE CASCADE,
    permRead    BOOLEAN NOT NULL DEFAULT FALSE,
    permEdit    BOOLEAN NOT NULL DEFAULT FALSE,
    permCreate  BOOLEAN NOT NULL DEFAULT FALSE,
    permDelete  BOOLEAN NOT NULL DEFAULT FALSE,
    permChown   BOOLEAN NOT NULL DEFAULT FALSE,
    permGrant   BOOLEAN NOT NULL DEFAULT FALSE,
    permSuper   BOOLEAN NOT NULL DEFAULT FALSE,
    PRIMARY KEY(aclist_id, member_id)
);

/*
 * Info
 */
CREATE TABLE info (
  key           VARCHAR NOT NULL PRIMARY KEY,
  value         VARCHAR,
  owner_id      UUID REFERENCES usersGroups(id) ON UPDATE CASCADE ON DELETE CASCADE,
  aclist_id     UUID REFERENCES aclists(id) ON UPDATE CASCADE ON DELETE CASCADE
);

INSERT INTO info (key, value, owner_id, aclist_id) VALUES ('DBSchema Version', '00000', null, null);

/*
 * Collections and other distributed resources
 */
CREATE TABLE collections (
  id          UUID NOT NULL PRIMARY KEY,
  description VARCHAR,
  name        VARCHAR,
  indexPath   VARCHAR,
  storagePath VARCHAR,
  node_id     UUID NOT NULL REFERENCES nodes (id) ON UPDATE CASCADE ON DELETE CASCADE,
  owner_id    UUID NOT NULL REFERENCES usersGroups(id) ON UPDATE CASCADE ON DELETE CASCADE,
  aclist_id   UUID NOT NULL REFERENCES aclists(id) ON UPDATE CASCADE ON DELETE CASCADE
);

CREATE TABLE files (
  id            UUID    NOT NULL PRIMARY KEY,
  name          VARCHAR NOT NULL,
  filename      VARCHAR NOT NULL,
  hash          VARCHAR,
  created       TIMESTAMP DEFAULT now(),
  user_id       UUID REFERENCES usersGroups (id) ON DELETE SET NULL,
  collection_id UUID NOT NULL REFERENCES collections (id) ON UPDATE CASCADE ON DELETE CASCADE,
  document_language VARCHAR NOT NULL DEFAULT 'en'
);

CREATE TABLE topics (
  id            UUID NOT NULL PRIMARY KEY,
  name          VARCHAR,
  category      VARCHAR,
  owner_id      UUID REFERENCES usersGroups(id) ON UPDATE CASCADE ON DELETE CASCADE,
  aclist_id     UUID REFERENCES aclists(id) ON UPDATE CASCADE ON DELETE CASCADE,
  node_id       UUID NOT NULL REFERENCES nodes (id) ON UPDATE CASCADE ON DELETE CASCADE,
  cloud_name    VARCHAR NOT NULL DEFAULT 'LBAC_PRIMARY_CLOUD' REFERENCES clouds(name) ON UPDATE CASCADE ON DELETE CASCADE
);

CREATE TABLE postings (
  id            UUID NOT NULL PRIMARY KEY,
  text          VARCHAR,
  owner_id      UUID REFERENCES usersGroups(id) ON UPDATE CASCADE ON DELETE CASCADE,
  topic_id      UUID REFERENCES topics(id) ON UPDATE CASCADE ON DELETE CASCADE,
  created       TIMESTAMP DEFAULT now()
);


/*
 * Termvector table
 */
CREATE TABLE termvectors (
  wordroot    VARCHAR    NOT NULL,
  file_id     UUID NOT NULL REFERENCES files (id) ON UPDATE CASCADE ON DELETE CASCADE,
  termfrequency INTEGER NOT NULL,
  PRIMARY KEY(wordroot, file_id)
);
CREATE INDEX i_termvectors_file_id ON termvectors (file_id);


CREATE TABLE unstemmed_words(
  wordroot VARCHAR NOT NULL,
  file_id UUID NOT NULL REFERENCES files (id) ON UPDATE CASCADE ON DELETE CASCADE,
  unstemmed_word VARCHAR NOT NULL,
  PRIMARY KEY(file_id, unstemmed_word),
  FOREIGN KEY (wordroot, file_id) REFERENCES termvectors (wordroot, file_id)
      ON UPDATE CASCADE ON DELETE CASCADE
);
CREATE INDEX i_unstemmed_words_wordroot ON  unstemmed_words (wordroot);

/*
 * finally set DBSchema Version
 */
UPDATE lbac.info SET value=:LBAC_SCHEMA_VERSION WHERE key='DBSchema Version';
COMMIT;

