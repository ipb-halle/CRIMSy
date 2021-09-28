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
\set LBAC_SCHEMA_VERSION '\'00002\''

BEGIN TRANSACTION;

UPDATE lbac.info SET value=:LBAC_SCHEMA_VERSION WHERE key='DBSchema Version';

CREATE TABLE compositions(
    materialid  INTEGER NOT NULL PRIMARY KEY REFERENCES materials (materialid) ON UPDATE CASCADE ON DELETE CASCADE,
    type VARCHAR NOT NULL
);

CREATE TABLE material_compositions(
    materialid  INTEGER NOT NULL REFERENCES materials (materialid),
    componentid INTEGER NOT NULL REFERENCES materials (materialid),
	concentration FLOAT,
    PRIMARY KEY (materialid, componentid)
);

INSERT INTO materialdetailtypes(id,name) VALUES(7,'COMPOSITION');

CREATE TABLE components_history(
    id SERIAL NOT NULL PRIMARY KEY,
    materialid INTEGER NOT NULL REFERENCES materials(materialid),
    mdate TIMESTAMP NOT NULL,
    actorId INTEGER NOT NULL REFERENCES usersgroups(id),
    digest VARCHAR,
    action VARCHAR NOT NULL,
    materialid_old INTEGER REFERENCES materials (materialid) ON UPDATE CASCADE ON DELETE CASCADE, 
    materialid_new INTEGER REFERENCES materials (materialid) ON UPDATE CASCADE ON DELETE CASCADE,
    concentration_old FLOAT,
    concentration_new FLOAT
);

ALTER TABLE structures_hist RENAME COLUMN mtime TO mdate;
ALTER TABLE materials_hist RENAME COLUMN materialid TO id;
ALTER TABLE storages_hist RENAME COLUMN materialid TO id;
ALTER TABLE items_history RENAME COLUMN itemid TO id;
ALTER TABLE taxonomy_history RENAME COLUMN taxonomyid TO id;
ALTER TABLE biomaterial_history RENAME COLUMN mtime TO mdate;

ALTER TABLE structures_hist DROP CONSTRAINT structures_hist_pkey;
ALTER TABLE structures_hist ADD PRIMARY KEY (id,actorid,mdate);
ALTER TABLE materials_hist DROP CONSTRAINT materials_hist_pkey;
ALTER TABLE materials_hist ADD PRIMARY KEY (id,actorid,mdate);
ALTER TABLE storages_hist DROP CONSTRAINT storages_hist_pkey;
ALTER TABLE storages_hist ADD PRIMARY KEY (id,actorid,mdate);

COMMIT TRANSACTION;
