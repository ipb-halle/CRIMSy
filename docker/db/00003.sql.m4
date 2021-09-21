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
        sequenceType VARCHAR,
        circular BOOLEAN,
        annotations VARCHAR
);

COMMIT TRANSACTION;
