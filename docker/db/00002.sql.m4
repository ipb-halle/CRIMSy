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
\set LBAC_SCHEMA_VERSION '\'00002\''

BEGIN TRANSACTION;

UPDATE lbac.info SET value=:LBAC_SCHEMA_VERSION WHERE key='DBSchema Version';

/* 
 * definition of master node
 * will be commented out (i.e. skipped) for master node 
 */
LBAC_MASTER_SKIP INSERT INTO nodes (id, baseUrl, institution, local, rank) VALUES
LBAC_MASTER_SKIP ('LBAC_MASTER_NODE_ID', 'LBAC_MASTER_URL', 'LBAC_MASTER_INSTITUTION', False, 10);

/* 
 * definition of local node 
 */
INSERT INTO nodes (id, baseUrl, institution, local, rank) VALUES
  ( 'LBAC_NODE_ID',
    'https://LBAC_INTERNET_FQHN:8443/ui',
    'LBAC_INSTITUTION_SHORT', True, LBAC_NODE_RANK);

COMMIT TRANSACTION;

