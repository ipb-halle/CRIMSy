include(dist/etc/config_m4.inc)dnl
/*
 * Leibniz Bioactives Cloud
 * Init script for database postgres 12.6
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

/* define global vars */
\set LBAC_DATABASE lbac
\set LBAC_USER lbac

\connect :LBAC_DATABASE :LBAC_USER

INSERT INTO clouds (name) VALUES ('LBAC_PRIMARY_CLOUD') ON CONFLICT DO NOTHING;

/* definition of master node (will be skipped on master node) */
LBAC_MASTER_SKIP INSERT INTO nodes (id, baseUrl, institution, local) VALUES
LBAC_MASTER_SKIP ('LBAC_MASTER_NODE_ID', 'LBAC_MASTER_URL', 'LBAC_MASTER_INSTITUTION', False)
LBAC_MASTER_SKIP ON CONFLICT DO NOTHING;

/* definition of local node */
INSERT INTO nodes (id, baseUrl, institution, local) VALUES
  ( 'LBAC_NODE_ID',
    'https://LBAC_INTERNET_FQHN:8443/ui',
    'LBAC_INSTITUTION_SHORT', True)
  ON CONFLICT DO NOTHING;

INSERT INTO cloud_nodes (node_id, cloud_id, rank) 
  SELECT 'LBAC_NODE_ID'::UUID AS node_id, id AS cloud_id, LBAC_NODE_RANK AS rank 
  FROM clouds WHERE name='LBAC_PRIMARY_CLOUD'
  ON CONFLICT DO NOTHING;
LBAC_MASTER_SKIP INSERT INTO cloud_nodes (node_id, cloud_id, rank) 
LBAC_MASTER_SKIP SELECT 'LBAC_MASTER_NODE_ID'::UUID AS node_id, id AS cloud_id, 10 AS rank
LBAC_MASTER_SKIP FROM clouds WHERE name='LBAC_PRIMARY_CLOUD'
LBAC_MASTER_SKIP ON CONFLICT DO NOTHING;

