include(dist/etc/config_m4.inc)dnl
/*
 * Leibniz Bioactives Cloud
 * Initial data 
 * Copyright 2021 Leibniz-Institut f. Pflanzenbiochemie
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
 
 
/* Only postgres dbuser can grant priviliges /*
\connect lbac 
\connect - postgres
BEGIN TRANSACTION;
CREATE USER fasta WITH PASSWORD 'fasta'; 
ALTER USER fasta SET search_path to lbac;
COMMIT TRANSACTION;
 

\connect lbac 
\connect - lbac
\set LBAC_SCHEMA_VERSION '\'00003\''

BEGIN TRANSACTION;

UPDATE lbac.info SET value=:LBAC_SCHEMA_VERSION WHERE key='DBSchema Version';

INSERT INTO materialdetailtypes VALUES(8,'SEQUENCE_INFORMATION');

INSERT INTO materialInformations VALUES(11,5,1,false);
INSERT INTO materialInformations VALUES(12,5,3,false);
INSERT INTO materialInformations VALUES(13,5,8,false);

CREATE TABLE sequences (
        id INTEGER PRIMARY KEY NOT NULL REFERENCES materials(materialid),
        sequenceString VARCHAR,
        sequenceType VARCHAR NOT NULL,
        circular BOOLEAN,
        annotations VARCHAR
);

CREATE TABLE sequences_history(
    id INTEGER NOT NULL REFERENCES sequences(id),
    actorid INTEGER NOT NULL REFERENCES usersgroups(id),
    mdate TIMESTAMP NOT NULL,
    digest VARCHAR,
    action VARCHAR NOT NULL,
    sequenceString_old VARCHAR,
    sequenceString_new VARCHAR,
    circular_old BOOLEAN,
    circular_new BOOLEAN,
    annotations_old VARCHAR,
    annotations_new VARCHAR,
    PRIMARY KEY(id,actorid,mdate)
);

CREATE TABLE temp_search_parameter (
  id         SERIAL    NOT NULL PRIMARY KEY,
  cdate      TIMESTAMP NOT NULL DEFAULT now(),
  processid  UUID NOT NULL,
  parameter  JSONB NOT NULL
);


GRANT SELECT ON sequences TO fasta;
GRANT SELECT ON materials TO fasta;
GRANT SELECT ON material_compositions TO fasta;
GRANT SELECT ON ACENTRIES TO fasta;
GRANT SELECT ON MEMBERSHIPS TO fasta;
GRANT SELECT ON temp_search_parameter TO fasta;
GRANT SELECT ON material_indices TO fasta;
GRANT SELECT ON USERSGROUPS TO fasta;
GRANT SELECT ON projects TO fasta;

COMMIT TRANSACTION;
