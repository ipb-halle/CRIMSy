include(dist/etc/config_m4.inc)dnl
/*
 * Leibniz Bioactives Cloud
 * Initial data 
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
 * Common header to all scripts; just increment schema version
 */
\connect lbac 
\connect - lbac
\set LBAC_SCHEMA_VERSION '\'00005\''

BEGIN TRANSACTION;
UPDATE lbac.info SET value=:LBAC_SCHEMA_VERSION WHERE key='DBSchema Version';

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

INSERT INTO cloud_nodes (node_id, cloud_id, rank) 
  SELECT 'LBAC_NODE_ID'::UUID AS node_id, id AS cloud_id, LBAC_NODE_RANK AS rank 
  FROM clouds WHERE name='LBAC_PRIMARY_CLOUD';
LBAC_MASTER_SKIP INSERT INTO cloud_nodes (node_id, cloud_id, rank) 
LBAC_MASTER_SKIP  SELECT 'LBAC_MASTER_NODE_ID'::UUID AS node_id, id AS cloud_id, 10 AS rank
LBAC_MASTER_SKIP  FROM clouds WHERE name='LBAC_PRIMARY_CLOUD';

COMMIT TRANSACTION;

/**
 * commands without explicit transaction (may fail)
 */
ALTER TABLE nodes drop column rank;
ALTER TABLE nodes drop column status;
ALTER TABLE nodes drop column last_seen_at_master;
ALTER TABLE nodes drop column last_seen_local;

ALTER TABLE aclists DROP COLUMN IF EXISTS node_id;
ALTER TABLE aclists DROP COLUMN IF EXISTS modified;

/* temporarily, we lose all nested memberships! */
DELETE FROM memberships WHERE nested=true;
DELETE FROM nestingpathsets;
DELETE FROM nestingpathset_memberships;
ALTER TABLE nestingpathsets ADD COLUMN membership_id UUID NOT NULL REFERENCES memberships(id) ON DELETE CASCADE ON UPDATE CASCADE;


ALTER TABLE nestingpathsets ADD COLUMN membership_id UUID NOT NULL REFERENCES memberships(id) ON DELETE CASCADE ON UPDATE CASCADE;

UPDATE files set user_id = u.id FROM (SELECT id FROM usersgroups WHERE 
  membertype = 'U' AND subsystemtype = 1 AND login = 'admin') AS u 
  WHERE user_id IS NULL;

SELECT FORMAT('ALTER TABLE topics ADD COLUMN cloud_name VARCHAR NOT NULL DEFAULT 
  ''%s'' REFERENCES clouds(name) ON UPDATE CASCADE ON DELETE CASCADE;', name) 
  FROM (SELECT name FROM clouds LIMIT 1) AS cn \gexec


