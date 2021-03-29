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
UPDATE lbac.info SET value=:LBAC_SCHEMA_VERSION WHERE key='DBSchema Version';


BEGIN TRANSACTION;

CREATE TABLE projecttypes (
    id INTEGER NOT NULL PRIMARY KEY,
    type VARCHAR
);

CREATE TABLE budgetreservationtypes (
    id INTEGER NOT NULL PRIMARY KEY
);

CREATE TABLE materialtypes (
    id INTEGER NOT NULL PRIMARY KEY,
    name VARCHAR NOT NULL
);

CREATE TABLE materialdetailtypes (
    id INTEGER NOT NULL PRIMARY KEY,
    name VARCHAR NOT NULL
);

CREATE TABLE materialinformations (
    id INTEGER NOT NULL PRIMARY KEY,
    materialtypeid INTEGER NOT NULL REFERENCES materialtypes(id),
    materialdetailtypeid INTEGER NOT NULL REFERENCES materialdetailtypes(id),
    mandatory BOOLEAN NOT NULL
);

CREATE TABLE projects (
    id SERIAL NOT NULL PRIMARY KEY,
    name VARCHAR NOT NULL,
    budget NUMERIC,
    budgetBlocked BOOLEAN default false,
    projecttypeid INTEGER NOT NULL REFERENCES projecttypes(id),
    owner_id INTEGER NOT NULL REFERENCES usersgroups(id),
    aclist_id INTEGER NOT NULL REFERENCES aclists(id),
    description VARCHAR,
    ctime TIMESTAMP  NOT NULL DEFAULT now(),
    mtime TIMESTAMP  NOT NULL DEFAULT now(),
    deactivated BOOLEAN NOT NULL DEFAULT false
);
CREATE UNIQUE index project_index_name_unique ON projects (LOWER(name));

CREATE TABLE projecttemplates (
    id SERIAL NOT NULL PRIMARY KEY,
    materialdetailtypeid INTEGER NOT NULL REFERENCES materialdetailtypes(id),
    aclistid INTEGER NOT NULL REFERENCES aclists(id),
    projectid INTEGER NOT NULL REFERENCES projects(id)
);

CREATE TABLE budgetreservations (
    id SERIAL NOT NULL PRIMARY KEY,
    startDate DATE,
    endDate DATE ,
    type INTEGER  NOT NULL REFERENCES budgetreservationtypes(id),
    budget NUMERIC NOT NULL
);


INSERT INTO materialtypes VALUES(1,'STRUCTURE');
INSERT INTO materialtypes VALUES(2,'MATERIAL_COMPOSITION');
INSERT INTO materialtypes VALUES(3,'BIOMATERIAL');
INSERT INTO materialtypes VALUES(4,'CONSUMABLE');
INSERT INTO materialtypes VALUES(5,'SEQUENCE');
INSERT INTO materialtypes VALUES(6,'TISSUE');
INSERT INTO materialtypes VALUES(7,'TAXONOMY');


INSERT INTO projecttypes VALUES(1,'CHEMICAL_PROJECT');
INSERT INTO projecttypes VALUES(2,'IT_PROJECT');
INSERT INTO projecttypes VALUES(3,'FINANCE_PROJECT');
INSERT INTO projecttypes VALUES(4,'BIOLOGICAL_PROJECT');
INSERT INTO projecttypes VALUES(5,'BIOCHEMICAL_PROJECT');



INSERT INTO materialdetailtypes VALUES(1,'COMMON_INFORMATION');
INSERT INTO materialdetailtypes VALUES(2,'STRUCTURE_INFORMATION');
INSERT INTO materialdetailtypes VALUES(3,'INDEX');
INSERT INTO materialdetailtypes VALUES(4,'HAZARD_INFORMATION');
INSERT INTO materialdetailtypes VALUES(5,'STORAGE_CLASSES');
INSERT INTO materialdetailtypes VALUES(6,'TAXONOMY');

INSERT INTO materialInformations VALUES(1,1,1,false);
INSERT INTO materialInformations VALUES(2,1,2,false);
INSERT INTO materialInformations VALUES(3,1,3,false);
INSERT INTO materialInformations VALUES(4,1,4,false);
INSERT INTO materialInformations VALUES(5,1,5,false);

INSERT INTO materialInformations VALUES(6,2,1,false);

INSERT INTO materialInformations VALUES(7,3,1,false);
INSERT INTO materialInformations VALUES(8,3,6,false);

INSERT INTO materialInformations VALUES(9,4,1,false);
INSERT INTO materialInformations VALUES(10,4,3,false);



CREATE TABLE indextypes (
        id SERIAL NOT NULL PRIMARY KEY,
        name VARCHAR NOT NULL,
        javaclass VARCHAR,
        UNIQUE(name));

CREATE TABLE materials (
        materialid SERIAL NOT NULL PRIMARY KEY,
        materialTypeId INTEGER NOT NULL REFERENCES materialtypes(id),
        ctime TIMESTAMP  NOT NULL DEFAULT now(),
        aclist_id INTEGER NOT NULL REFERENCES aclists(id),
        owner_id INTEGER NOT NULL REFERENCES usersgroups(id),
        deactivated BOOLEAN NOT NULL DEFAULT false,
        projectId INTEGER REFERENCES projects(id));

CREATE TABLE material_indices (
        id SERIAL NOT NULL PRIMARY KEY,
        materialid INTEGER NOT NULL REFERENCES materials(materialid),
        typeid INTEGER NOT NULL REFERENCES indextypes(id),
        value VARCHAR NOT NULL,
        language VARCHAR,
        rank INTEGER);

CREATE TABLE materialdetailrights (
        id SERIAL NOT NULL PRIMARY KEY,
        materialid INTEGER NOT NULL REFERENCES materials(materialid),
        aclistid INTEGER NOT NULL REFERENCES aclists(id),
        materialtypeid INTEGER NOT NULL REFERENCES materialdetailtypes(id));

CREATE TABLE molecules (
        id SERIAL NOT NULL PRIMARY KEY,
        format VARCHAR NOT NULL,
        molecule molecule);

CREATE TABLE structures (
        id INTEGER PRIMARY KEY REFERENCES materials(materialid),
        sumformula VARCHAR,
        molarMass FLOAT,
        exactMolarMass FLOAT,
        moleculeID INTEGER REFERENCES molecules(id));

CREATE TABLE structures_hist (
        id INTEGER NOT NULL REFERENCES structures(id),
        actorId INTEGER NOT NULL REFERENCES usersgroups(id),
        mtime TIMESTAMP  NOT NULL,
        digest VARCHAR,
        sumformula_old VARCHAR,
        sumformula_new VARCHAR,
        molarMass_old FLOAT,
        molarMass_new FLOAT,
        exactMolarMass_old FLOAT,
        exactMolarMass_new FLOAT,
        moleculeId_old INTEGER REFERENCES molecules(id),
        moleculeId_new INTEGER REFERENCES molecules(id),
        PRIMARY KEY (id,mtime));

CREATE TABLE  hazards (
        id INTEGER PRIMARY KEY,
        name VARCHAR NOT NULL);

CREATE TABLE  hazards_materials (
        typeid INTEGER NOT NULL REFERENCES hazards(id),
        materialid INTEGER NOT NULL REFERENCES materials(materialid),
        remarks VARCHAR,
        PRIMARY KEY(materialid,typeid));

CREATE TABLE  storageclasses (
        id INTEGER PRIMARY KEY,
        name VARCHAR NOT NULL);

CREATE TABLE  storageconditions (
        id INTEGER PRIMARY KEY,
        name VARCHAR NOT NULL);

CREATE TABLE  storages (
        materialid INTEGER PRIMARY KEY REFERENCES materials(materialid),
        storageclass INTEGER NOT NULL REFERENCES storageClasses(id),
        description VARCHAR);


CREATE TABLE  storageconditions_storages (
        conditionId INTEGER NOT NULL REFERENCES storageconditions(id),
        materialid INTEGER NOT NULL REFERENCES storages(materialid),
        PRIMARY KEY (conditionId,materialid)
);

insert into storageconditions(id,name)values(1,'moistureSensitive');
insert into storageconditions(id,name)values(2,'keepMoisture');
insert into storageconditions(id,name)values(3,'lightSensitive');
insert into storageconditions(id,name)values(4,'underInertGas');
insert into storageconditions(id,name)values(5,'acidSensitive');
insert into storageconditions(id,name)values(6,'alkaliSensitive');
insert into storageconditions(id,name)values(7,'KeepAwayFromOxidants');
insert into storageconditions(id,name)values(8,'frostSensitive');
insert into storageconditions(id,name)values(9,'keepCool');
insert into storageconditions(id,name)values(10,'keepFrozen');
insert into storageconditions(id,name)values(11,'storeUnderMinus40Degrees');
insert into storageconditions(id,name)values(12,'storeUnderMinus80Degrees');

insert into indextypes(id,name,javaclass)values(1,'name',null);
insert into indextypes(id,name,javaclass)values(2,'GESTIS/ZVG',null);
insert into indextypes(id,name,javaclass)values(3,'CAS/RN',null);
insert into indextypes(id,name,javaclass)values(4,'Carl Roth Sicherheitsdatenblatt',null);

insert into storageclasses(id,name)values(1,'1');
insert into storageclasses(id,name)values(2,'2A');
insert into storageclasses(id,name)values(3,'2B');
insert into storageclasses(id,name)values(4,'3');
insert into storageclasses(id,name)values(5,'4.1A');
insert into storageclasses(id,name)values(6,'4.1B');
insert into storageclasses(id,name)values(7,'4.2');
insert into storageclasses(id,name)values(8,'4.3');
insert into storageclasses(id,name)values(9,'5.1A');
insert into storageclasses(id,name)values(10,'5.1B');
insert into storageclasses(id,name)values(11,'5.1C');
insert into storageclasses(id,name)values(12,'6.1A');
insert into storageclasses(id,name)values(13,'6.1B');
insert into storageclasses(id,name)values(14,'6.1C');
insert into storageclasses(id,name)values(15,'6.1D');
insert into storageclasses(id,name)values(16,'6.2');
insert into storageclasses(id,name)values(17,'7');
insert into storageclasses(id,name)values(18,'8A');
insert into storageclasses(id,name)values(19,'8B');
insert into storageclasses(id,name)values(20,'10');
insert into storageclasses(id,name)values(21,'11');
insert into storageclasses(id,name)values(22,'12');
insert into storageclasses(id,name)values(23,'13');

insert into hazards(id,name)values(1,'explosive');
insert into hazards(id,name)values(2,'highlyFlammable');
insert into hazards(id,name)values(3,'oxidizing');
insert into hazards(id,name)values(4,'compressedGas');
insert into hazards(id,name)values(5,'corrosive');
insert into hazards(id,name)values(6,'poisonous');
insert into hazards(id,name)values(7,'irritant');
insert into hazards(id,name)values(8,'unhealthy');
insert into hazards(id,name)values(9,'environmentallyHazardous');
insert into hazards(id,name)values(10,'danger');
insert into hazards(id,name)values(11,'attention');
insert into hazards(id,name)values(12,'hazardStatements');
insert into hazards(id,name)values(13,'precautionaryStatements');

CREATE TABLE  materials_hist (
        materialid INTEGER NOT NULL REFERENCES materials(materialid),
        actorId INTEGER NOT NULL REFERENCES usersgroups(id),
        mDate TIMESTAMP,
        digest VARCHAR,
        action VARCHAR,
        aclistid_old INTEGER REFERENCES aclists(id),
        aclistid_new INTEGER REFERENCES aclists(id),
        projectid_old INTEGER REFERENCES projects(id),
        projectid_new INTEGER REFERENCES projects(id),
        ownerid_old INTEGER REFERENCES usersgroups(id),
        ownerid_new INTEGER REFERENCES usersgroups(id),
        PRIMARY KEY(materialid,mDate));

CREATE TABLE  material_indices_hist (
    id SERIAL NOT NULL PRIMARY KEY,
    materialid INTEGER NOT NULL REFERENCES materials(materialid),
    typeid INTEGER NOT NULL REFERENCES indextypes(id),
    mDate TIMESTAMP NOT NULL,
    actorId INTEGER NOT NULL REFERENCES usersgroups(id),
    digest VARCHAR,
    value_old VARCHAR,
    value_new VARCHAR,
    rank_old INTEGER,
    rank_new INTEGER,
    language_old VARCHAR,
    language_new VARCHAR);

CREATE TABLE  hazards_materials_hist (
    id SERIAL NOT NULL PRIMARY KEY,
    materialid INTEGER NOT NULL REFERENCES materials(materialid),
    mdate TIMESTAMP NOT NULL,
    actorId INTEGER NOT NULL REFERENCES usersgroups(id),
    digest VARCHAR,
    typeid_old INTEGER REFERENCES hazards(id),
    typeid_new INTEGER REFERENCES hazards(id),
    remarks_old VARCHAR,
    remarks_new VARCHAR);

CREATE TABLE  storages_hist (
    materialid INTEGER NOT NULL REFERENCES materials(materialid),
    mdate TIMESTAMP NOT NULL,
    actorId INTEGER NOT NULL REFERENCES usersgroups(id),
    digest VARCHAR,
    description_old VARCHAR,
    description_new VARCHAR,
    storageclass_old INTEGER REFERENCES storageclasses(id),
    storageclass_new INTEGER REFERENCES storageclasses(id),
    PRIMARY KEY(materialid,mdate));


CREATE TABLE  storagesconditions_storages_hist (
    id SERIAL NOT NULL PRIMARY KEY,
    materialid INTEGER NOT NULL REFERENCES materials(materialid),
    mdate TIMESTAMP NOT NULL,
    actorId INTEGER NOT NULL REFERENCES usersgroups(id),
    digest VARCHAR,
    conditionId_old INTEGER,
    conditionId_new INTEGER);

CREATE TABLE containertypes(
    name varchar NOT NULL PRIMARY KEY,
    description varchar,
    transportable BOOLEAN NOT NULL DEFAULT true,
    unique_name BOOLEAN NOT NULL DEFAULT true,
    rank integer not null);

CREATE TABLE containers(
    id SERIAL NOT NULL PRIMARY KEY,
    parentcontainer INTEGER REFERENCES containers(id),
    label VARCHAR NOT NULL,
    projectid INTEGER REFERENCES projects(id),
    dimension VARCHAR,
    type VARCHAR NOT NULL REFERENCES containertypes(name),
    firesection VARCHAR,
    gmosafety VARCHAR,
    barcode VARCHAR,
    deactivated BOOLEAN NOT NULL DEFAULT false);

CREATE TABLE nested_containers(
    sourceid INTEGER NOT NULL REFERENCES containers(id),
    targetid INTEGER NOT NULL REFERENCES containers(id),
    nested BOOLEAN NOT NULL,
    PRIMARY KEY(sourceid,targetid));

CREATE TABLE solvents(
    id SERIAL NOT NULL PRIMARY KEY,
    name VARCHAR NOT NULL);


CREATE TABLE items(
    id SERIAL NOT NULL PRIMARY KEY,
    materialid INTEGER NOT NULL REFERENCES materials(materialid),
    amount FLOAT NOT NULL,
    articleid INTEGER,
    projectid INTEGER REFERENCES projects(id),
    concentration FLOAT,
    unit VARCHAR,
    purity VARCHAR,
    solventid INTEGER REFERENCES solvents(id),
    description VARCHAR,
    owner_id INTEGER  NOT NULL REFERENCES usersgroups(id),
    containersize FLOAT,
    containertype VARCHAR REFERENCES containertypes(name),
    containerid INTEGER REFERENCES containers(id),
    ctime TIMESTAMP  NOT NULL DEFAULT now(),
    expiry_date TIMESTAMP,
    aclist_id INTEGER NOT NULL,
    label VARCHAR
);

CREATE TABLE item_positions(
    id SERIAL NOT NULL PRIMARY KEY,
    itemid INTEGER REFERENCES items(id),
    containerid INTEGER NOT NULL REFERENCES containers(id),
    itemrow INTEGER ,
    itemcol INTEGER );

CREATE TABLE items_history(
    itemid INTEGER NOT NULL REFERENCES items(id),
    mdate TIMESTAMP NOT NULL,
    actorid INTEGER NOT NULL REFERENCES usersgroups(id),
    action VARCHAR NOT NULL,
    projectid_old INTEGER REFERENCES projects(id),
    projectid_new INTEGER REFERENCES projects(id),
    concentration_old FLOAT,
    concentration_new FLOAT,
    purity_old VARCHAR,
    purity_new VARCHAR,
    description_new VARCHAR,
    description_old VARCHAR,
    amount_old FLOAT,
    amount_new FLOAT,
    owner_old INTEGER REFERENCES usersgroups(id),
    owner_new INTEGER REFERENCES usersgroups(id),
    parent_containerid_new INTEGER REFERENCES containers(id),
    parent_containerid_old INTEGER REFERENCES containers(id),
    aclistid_old INTEGER REFERENCES aclists(id),
    aclistid_new INTEGER REFERENCES aclists(id),
    PRIMARY KEY(itemid,actorid,mdate));

CREATE TABLE item_positions_history(
    id SERIAL PRIMARY KEY,
    itemid INTEGER NOT NULL REFERENCES items(id),
    containerid INTEGER NOT NULL REFERENCES containers(id),
    mdate TIMESTAMP NOT NULL,
    actorid INTEGER NOT NULL REFERENCES usersgroups(id),
    row_old INTEGER,
    row_new INTEGER,
    col_old INTEGER,
    col_new INTEGER
);

insert into containertypes(name,description,rank,transportable,unique_name)values('ROOM',null,100,false,true);
insert into containertypes(name,description,rank,transportable,unique_name)values('CUPBOARD',null,90,false,false);
insert into containertypes(name,description,rank,transportable,unique_name)values('FREEZER',null,90,false,true);
insert into containertypes(name,description,rank,transportable,unique_name)values('WELLPLATE',null,50,true,true);
insert into containertypes(name,description,rank,transportable,unique_name)values('GLAS_FLASK',null,0,true,true);
insert into containertypes(name,description,rank,transportable,unique_name)values('PLASTIC_FLASK',null,0,true,true);
insert into containertypes(name,description,rank,transportable,unique_name)values('GLAS_VIAL',null,0,true,true);
insert into containertypes(name,description,rank,transportable,unique_name)values('PLASTIC_VIAL',null,0,true,true);
insert into containertypes(name,description,rank,transportable,unique_name)values('GLAS_AMPOULE',null,0,true,true);
insert into containertypes(name,description,rank,transportable,unique_name)values('PLASTIC_AMPOULE',null,0,true,true);
insert into containertypes(name,description,rank,transportable,unique_name)values('STEEL_BARREL',null,0,true,true);
insert into containertypes(name,description,rank,transportable,unique_name)values('PLASTIC_BARREL',null,0,true,true);
insert into containertypes(name,description,rank,transportable,unique_name)values('STEEL_CONTAINER',null,0,true,true);
insert into containertypes(name,description,rank,transportable,unique_name)values('PLASTIC_CONTAINER',null,0,true,true);
insert into containertypes(name,description,rank,transportable,unique_name)values('CARTON',null,0,true,true);
insert into containertypes(name,description,rank,transportable,unique_name)values('PLASTIC_BAG',null,0,true,true);
insert into containertypes(name,description,rank,transportable,unique_name)values('PLASTIC_SACK',null,0,true,true);
insert into containertypes(name,description,rank,transportable,unique_name)values('PAPER_BAG',null,0,true,true);
insert into containertypes(name,description,rank,transportable,unique_name)values('COMPRESSED_GAS_CYLINDER',null,0,true,true);

CREATE TABLE taxonomy_level(
    id SERIAL NOT NULL PRIMARY KEY,
    name VARCHAR NOT NULL,
    rank INTEGER NOT NULL);

CREATE TABLE taxonomy(
    id INTEGER NOT NULL PRIMARY KEY REFERENCES materials(materialid),
    level INTEGER NOT NULL REFERENCES taxonomy_level(id));

CREATE TABLE effective_taxonomy(
    id SERIAL NOT NULL PRIMARY KEY,
    taxoid INTEGER NOT NULL REFERENCES taxonomy(id),
    parentid INTEGER NOT NULL REFERENCES taxonomy(id));

CREATE TABLE taxonomy_history(
    taxonomyid INTEGER NOT NULL REFERENCES materials(materialid),
    actorid INTEGER NOT NULL REFERENCES usersgroups(id),
    mdate TIMESTAMP NOT NULL,
    action VARCHAR NOT NULL,
    digest VARCHAR,
    level_old INTEGER,
    level_new INTEGER,
    parentid_old INTEGER,
    parentid_new INTEGER,
    PRIMARY KEY(taxonomyid,actorid,mdate));

CREATE TABLE tissues(
  id INTEGER NOT NULL PRIMARY KEY REFERENCES materials(materialid),
  taxoid INTEGER NOT NULL REFERENCES taxonomy(id)
);

CREATE TABLE biomaterial(
    id INTEGER NOT NULL PRIMARY KEY REFERENCES materials(materialid),
    taxoid INTEGER NOT NULL REFERENCES taxonomy(id),
    tissueid INTEGER REFERENCES tissues(id)
);

CREATE TABLE biomaterial_history(
    id INTEGER NOT NULL REFERENCES biomaterial(id),
    actorid INTEGER NOT NULL REFERENCES usersGroups(id),
    mtime TIMESTAMP NOT NULL,
    digest VARCHAR,
    action VARCHAR NOT NULL,
    tissueid_old INTEGER REFERENCES tissues(id),
    tissueid_new INTEGER REFERENCES tissues(id),
    taxoid_old INTEGER REFERENCES taxonomy(id),
    taxoid_new INTEGER REFERENCES taxonomy(id),
    PRIMARY KEY(id,actorid,mtime)
);

INSERT INTO taxonomy_level VALUES(1,'domain',100);
INSERT INTO taxonomy_level VALUES(2,'kingdom',200);
INSERT INTO taxonomy_level VALUES(3,'subkingdom',300);
INSERT INTO taxonomy_level VALUES(4,'division',400);
INSERT INTO taxonomy_level VALUES(5,'subdivision',500);
INSERT INTO taxonomy_level VALUES(6,'class',600);
INSERT INTO taxonomy_level VALUES(7,'subclass',700);
INSERT INTO taxonomy_level VALUES(8,'superorder',800);
INSERT INTO taxonomy_level VALUES(9,'order',900);
INSERT INTO taxonomy_level VALUES(10,'suborder',1000);
INSERT INTO taxonomy_level VALUES(11,'family',1100);
INSERT INTO taxonomy_level VALUES(12,'subfamily',1200);
INSERT INTO taxonomy_level VALUES(13,'tribe',1300);
INSERT INTO taxonomy_level VALUES(14,'genus',1400);
INSERT INTO taxonomy_level VALUES(15,'section',1500);
INSERT INTO taxonomy_level VALUES(16,'series',1600);
INSERT INTO taxonomy_level VALUES(17,'aggregate',1700);
INSERT INTO taxonomy_level VALUES(18,'species',1800);
INSERT INTO taxonomy_level VALUES(19,'subspecies',1900);
INSERT INTO taxonomy_level VALUES(20,'variety',2000);
INSERT INTO taxonomy_level VALUES(21,'form',2100);



/*
 * ToDo: xxxxx add ON UPDATE CASCADE/ ON DELETE CASCADE ?
 */

CREATE TABLE folders (
    folderid        SERIAL NOT NULL PRIMARY KEY,
    name            VARCHAR,
    parentid        INTEGER REFERENCES folders(folderid) ON UPDATE RESTRICT ON DELETE RESTRICT,
    aclist_id       INTEGER REFERENCES aclists(id) ON UPDATE RESTRICT ON DELETE RESTRICT,
    owner_id        INTEGER REFERENCES usersGroups(id) ON UPDATE RESTRICT ON DELETE RESTRICT,
    ctime           TIMESTAMP NOT NULL DEFAULT now(),
    projectid       INTEGER REFERENCES projects(id)  ON UPDATE RESTRICT ON DELETE RESTRICT
);
INSERT INTO folders (name) VALUES ('default');

CREATE TABLE experiments (
    experimentid    SERIAL NOT NULL PRIMARY KEY,
    code            VARCHAR,
    description     VARCHAR,
    template        BOOLEAN NOT NULL DEFAULT FALSE,
    folderid        INTEGER NOT NULL REFERENCES folders(folderid)  ON UPDATE RESTRICT ON DELETE RESTRICT,
    aclist_id       INTEGER REFERENCES aclists(id) ON UPDATE CASCADE ON DELETE CASCADE,
    owner_id        INTEGER REFERENCES usersGroups(id) ON UPDATE CASCADE ON DELETE CASCADE,
    ctime           TIMESTAMP NOT NULL DEFAULT now(),
    projectid       INTEGER REFERENCES projects(id)  ON UPDATE CASCADE ON DELETE CASCADE
);

CREATE TABLE exp_records (
    exprecordid     BIGSERIAL NOT NULL PRIMARY KEY,
    experimentid    INTEGER NOT NULL REFERENCES experiments (experimentid) ON UPDATE CASCADE ON DELETE CASCADE,
    changetime      TIMESTAMP,
    creationtime    TIMESTAMP,
    type            INTEGER,
    revision        INTEGER NOT NULL DEFAULT 1,
    next            BIGINT DEFAULT NULL REFERENCES exp_records(exprecordid) ON UPDATE CASCADE ON DELETE SET NULL
);

/*
 * Note: currently either materialid or itemid must be set
 *
 * ToDo: 
 * - add references to documents, users, etc.
 * - additional indexing for payload column (works 
 *   only if payload is of JSON type; see example below)
 */
CREATE TABLE linked_data (
    recordid        BIGSERIAL NOT NULL PRIMARY KEY,
    exprecordid     BIGINT NOT NULL 
                    REFERENCES exp_records(exprecordid) ON UPDATE CASCADE ON DELETE CASCADE,
    materialid      INTEGER 
                    REFERENCES materials(materialid) ON UPDATE CASCADE ON DELETE CASCADE,
    itemid          INTEGER CHECK (COALESCE(materialid, itemid,fileid) IS NOT NULL)
                    REFERENCES items(id) ON UPDATE CASCADE ON DELETE CASCADE,
    fileid          INTEGER REFERENCES files(id) ON UPDATE CASCADE ON DELETE CASCADE,
    rank            INTEGER DEFAULT 0,
    type            INTEGER NOT NULL,
    payload         VARCHAR
);

/*
 * B-tree index example:
 * CREATE INDEX i_exp_linked_data_val ON exp_linked_data (((payload->>'val')::DOUBLE PRECISION))
 *      WHERE (payload->>'val') IS NOT NULL;
 */

CREATE TABLE exp_assays (
    exprecordid     BIGINT NOT NULL PRIMARY KEY REFERENCES exp_records(exprecordid) ON UPDATE CASCADE ON DELETE CASCADE,
    outcometype     INTEGER NOT NULL,
    remarks         VARCHAR,
    units           VARCHAR
);

/* ToDo: xxxxx create fulltext index on exp_texts! */
CREATE TABLE exp_texts (
    exprecordid     BIGINT NOT NULL PRIMARY KEY REFERENCES exp_records(exprecordid) ON UPDATE CASCADE ON DELETE CASCADE,
    text            VARCHAR 
);

/**
 * - Agency: job scheduling
 * - barcode printing
 */
CREATE TABLE jobs (
    jobid       SERIAL NOT NULL PRIMARY KEY,
    input       BYTEA,
    jobdate     TIMESTAMP NOT NULL DEFAULT now(),
    jobtype     INTEGER NOT NULL,
    output      BYTEA,
    ownerid     INTEGER REFERENCES usersgroups(id) ON UPDATE CASCADE ON DELETE CASCADE,
    queue       VARCHAR NOT NULL,
    status      INTEGER NOT NULL
);

CREATE TABLE printers (
    queue       VARCHAR NOT NULL PRIMARY KEY,
    name        VARCHAR NOT NULL,
    aclistid    INTEGER NOT NULL REFERENCES aclists(id) ON UPDATE CASCADE ON DELETE CASCADE,
    config      VARCHAR NOT NULL DEFAULT '',
    contact     VARCHAR NOT NULL DEFAULT '',
    driver      VARCHAR NOT NULL,
    model       VARCHAR NOT NULL DEFAULT '',
    ownerid     INTEGER NOT NULL REFERENCES usersgroups(id) ON UPDATE CASCADE ON DELETE CASCADE,
    place       VARCHAR NOT NULL DEFAULT '',
    status      INTEGER NOT NULL DEFAULT 0
);

CREATE TABLE labels (
    id          SERIAL NOT NULL PRIMARY KEY,
    name        VARCHAR NOT NULL DEFAULT '',
    description VARCHAR NOT NULL DEFAULT '',
    labeltype   VARCHAR NOT NULL DEFAULT '',
    printermodel VARCHAR NOT NULL DEFAULT '',
    config      VARCHAR NOT NULL DEFAULT ''
);

CREATE TABLE preferences (
    id          SERIAL NOT NULL PRIMARY KEY,
    user_id     INTEGER NOT NULL REFERENCES usersgroups(id) ON UPDATE CASCADE ON DELETE CASCADE,
    key         VARCHAR NOT NULL,
    value       VARCHAR,
    UNIQUE (user_id, key)
);

CREATE TABLE images (
    id          BIGINT NOT NULL PRIMARY KEY REFERENCES exp_records(exprecordid) ON UPDATE CASCADE ON DELETE CASCADE,
    preview     VARCHAR,
    image       VARCHAR,
    aclist_id   INTEGER REFERENCES aclists(id) ON UPDATE CASCADE ON DELETE CASCADE,
    owner_id    INTEGER REFERENCES usersGroups(id) ON UPDATE CASCADE ON DELETE CASCADE
);

COMMIT TRANSACTION;
