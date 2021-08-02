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

CREATE TABLE material_compositions(
    materialid  INTEGER NOT NULL REFERENCES materials (materialid),
    componentid INTEGER NOT NULL REFERENCES materials (materialid),
	concentration FLOAT NOT NULL DEFAULT 0,
    PRIMARY KEY (materialid, componentid)
);

CREATE TABLE compositions(
    materialid  INTEGER NOT NULL PRIMARY KEY REFERENCES materials (materialid) ON UPDATE CASCADE ON DELETE CASCADE,
    type VARCHAR NOT NULL
);

INSERT INTO materialdetailtypes(id,name) VALUES(7,'COMPOSITION');

