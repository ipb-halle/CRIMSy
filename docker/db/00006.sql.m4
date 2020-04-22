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
	type VARCHAR);
	
CREATE TABLE budgetreservationtypes (id INTEGER NOT NULL PRIMARY KEY);

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
        mandatory BOOLEAN NOT NULL);
	
	
CREATE TABLE projects (
	id SERIAL NOT NULL PRIMARY KEY,
        name VARCHAR NOT NULL,
	budget NUMERIC,
        budgetBlocked BOOLEAN default false,
	projecttypeid INTEGER NOT NULL REFERENCES projecttypes(id),
	ownerid UUID NOT NULL REFERENCES usersgroups(id),
        usergroups UUID NOT NULL REFERENCES aclists(id),
	description VARCHAR,
	ctime TIMESTAMP  NOT NULL DEFAULT now(),
	mtime TIMESTAMP  NOT NULL DEFAULT now());

CREATE TABLE projecttemplates (
	id SERIAL NOT NULL PRIMARY KEY,
	materialdetailtypeid INTEGER NOT NULL REFERENCES materialdetailtypes(id),
	aclistid UUID NOT NULL REFERENCES aclists(id),
        projectid INTEGER NOT NULL REFERENCES projects(id));
	
CREATE TABLE budgetreservations (
	id SERIAL NOT NULL PRIMARY KEY,
	startDate DATE,
	endDate DATE ,
	type INTEGER  NOT NULL REFERENCES budgetreservationtypes(id),
	budget NUMERIC NOT NULL);


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
        javaclass VARCHAR);

CREATE TABLE materials (
        materialid SERIAL NOT NULL PRIMARY KEY,
        materialTypeId INTEGER NOT NULL REFERENCES materialtypes(id),
        ctime TIMESTAMP  NOT NULL DEFAULT now(),
        userGroups UUID NOT NULL REFERENCES aclists(id),
        ownerId UUID NOT NULL REFERENCES usersgroups(id),
        deactivated BOOLEAN NOT NULL,
        projectId INTEGER NOT NULL REFERENCES projects(id));

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
        aclistid UUID NOT NULL REFERENCES aclists(id),
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
        actorId UUID NOT NULL REFERENCES usersgroups(id),
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
        PRIMARY KEY (conditionId,materialid));





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
insert into indextypes(id,name,javaclass)values(3,'CAS/RM',null);
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
        actorId UUID NOT NULL REFERENCES usersgroups(id),
        mDate TIMESTAMP,
        digest VARCHAR,
        action VARCHAR,
        aclistid_old UUID REFERENCES aclists(id),
        aclistid_new UUID REFERENCES aclists(id),
        projectid_old INTEGER REFERENCES projects(id),
        projectid_new INTEGER REFERENCES projects(id),
        ownerid_old UUID REFERENCES usersgroups(id),
        ownerid_new UUID REFERENCES usersgroups(id),
        PRIMARY KEY(materialid,mDate));

CREATE TABLE  material_indices_hist (
    id SERIAL NOT NULL PRIMARY KEY,
    materialid INTEGER NOT NULL REFERENCES materials(materialid),
    typeid INTEGER NOT NULL REFERENCES indextypes(id),
    mDate TIMESTAMP NOT NULL,
    actorId UUID NOT NULL REFERENCES usersgroups(id),
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
    actorId UUID NOT NULL REFERENCES usersgroups(id),
    digest VARCHAR,
    typeid_old INTEGER REFERENCES hazards(id),
    typeid_new INTEGER REFERENCES hazards(id),
    remarks_old VARCHAR,
    remarks_new VARCHAR);

CREATE TABLE  storages_hist (
    materialid INTEGER NOT NULL REFERENCES materials(materialid),
    mdate TIMESTAMP NOT NULL,
    actorId UUID NOT NULL REFERENCES usersgroups(id),
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
    actorId UUID NOT NULL REFERENCES usersgroups(id),
    digest VARCHAR,
    conditionId_old INTEGER,
    conditionId_new INTEGER);

CREATE TABLE containertypes(
    name varchar NOT NULL PRIMARY KEY,
    description varchar,
    rank integer not null);

CREATE TABLE containers(
    id SERIAL NOT NULL PRIMARY KEY,
    parentcontainer INTEGER REFERENCES containers(id),
    label VARCHAR NOT NULL,
    projectid INTEGER REFERENCES projects(id),
    dimension VARCHAR,
    type VARCHAR NOT NULL REFERENCES containertypes(name),
    firesection VARCHAR,
    gvo_class VARCHAR,
    barcode VARCHAR);

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
    owner UUID  NOT NULL REFERENCES usersgroups(id),
    containersize FLOAT,
    containertype VARCHAR REFERENCES containertypes(name),
    containerid INTEGER REFERENCES containers(id),
    ctime TIMESTAMP  NOT NULL DEFAULT now()
);

CREATE TABLE item_positions(
    id SERIAL NOT NULL PRIMARY KEY,
    itemid INTEGER REFERENCES items(id),
    containerid INTEGER NOT NULL REFERENCES containers(id),
    row INTEGER ,
    col INTEGER );

CREATE TABLE itemtransfers(
    itemid INTEGER NOT NULL REFERENCES items(id),
    projectid INTEGER NOT NULL REFERENCES projects(id),
    actorid UUID NOT NULL REFERENCES usersgroups(id),
    transferdate TIMESTAMP NOT NULL,
    item_container_old INTEGER REFERENCES item_positions(id),
    item_container_new INTEGER REFERENCES item_positions(id),
    PRIMARY KEY(itemid,projectid,actorid,transferdate));

CREATE TABLE items_history(
    itemid INTEGER NOT NULL REFERENCES items(id),
    mdate TIMESTAMP NOT NULL,
    actorid UUID NOT NULL REFERENCES usersgroups(id),
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
    owner_old UUID REFERENCES usersgroups(id),
    owner_new UUID REFERENCES usersgroups(id),
    PRIMARY KEY(itemid,actorid,mdate));

insert into containertypes(name,description,rank)values('ROOM',null,100);
insert into containertypes(name,description,rank)values('CUPBOARD',null,90);
insert into containertypes(name,description,rank)values('FREEZER',null,90);
insert into containertypes(name,description,rank)values('WELLPLATE',null,50);
insert into containertypes(name,description,rank)values('GLAS_FLASK',null,0);
insert into containertypes(name,description,rank)values('PLASTIC_FLASK',null,0);
insert into containertypes(name,description,rank)values('GLAS_VIAL',null,0);
insert into containertypes(name,description,rank)values('PLASTIC_VIAL',null,0);
insert into containertypes(name,description,rank)values('GLAS_AMPOULE',null,0);
insert into containertypes(name,description,rank)values('PLASTIC_AMPOULE',null,0);
insert into containertypes(name,description,rank)values('STEEL_BARREL',null,0);
insert into containertypes(name,description,rank)values('PLASTIC_BARREL',null,0);
insert into containertypes(name,description,rank)values('STEEL_CONTAINER',null,0);
insert into containertypes(name,description,rank)values('PLASTIC_CONTAINER',null,0);
insert into containertypes(name,description,rank)values('CARTON',null,0);
insert into containertypes(name,description,rank)values('PLASTIC_BAG',null,0);
insert into containertypes(name,description,rank)values('PLASTIC_SACK',null,0);
insert into containertypes(name,description,rank)values('PAPER_BAG',null,0);
insert into containertypes(name,description,rank)values('COMPRESSED_GAS_CYLINDER',null,0);

CREATE TABLE taxonomy_level(
    id SERIAL NOT NULL PRIMARY KEY,
    name VARCHAR NOT NULL);

CREATE TABLE taxonomy(
    id INTEGER NOT NULL PRIMARY KEY REFERENCES materials(materialid),
    level INTEGER NOT NULL REFERENCES taxonomy_level(id));

CREATE TABLE effective_taxonomy(
    id SERIAL NOT NULL PRIMARY KEY,
    taxoid INTEGER NOT NULL REFERENCES taxonomy(id),
    parentid INTEGER NOT NULL REFERENCES taxonomy(id));

CREATE TABLE taxonomy_history(
    actorid UUID NOT NULL REFERENCES usersgroups(id),
    mdate TIMESTAMP NOT NULL,
    action VARCHAR NOT NULL,
    digest VARCHAR NOT NULL,
    level_old INTEGER,
    level_new INTEGER,
    PRIMARY KEY(actorid,mdate));

INSERT INTO taxonomy_level VALUES(1,'domain');
INSERT INTO taxonomy_level VALUES(2,'kingdom');
INSERT INTO taxonomy_level VALUES(3,'phylum');
INSERT INTO taxonomy_level VALUES(4,'class');
INSERT INTO taxonomy_level VALUES(5,'order');
INSERT INTO taxonomy_level VALUES(6,'family');
INSERT INTO taxonomy_level VALUES(7,'genus');
INSERT INTO taxonomy_level VALUES(8,'species');

COMMIT TRANSACTION;








