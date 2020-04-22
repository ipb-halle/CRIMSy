/*
 *     Leibniz Bioactives Cloud
 *     Copyright 2017 Leibniz-Institut f. Pflanzenbiochemie
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 *
 */

SET @TESTCLOUD = 'TESTCLOUD';
SET @TESTNODE = '986ad1be-9a3b-4a70-8600-c489c2a00da4';
SET @TESTCOLLECTION = '34bffdef-f488-4931-bc70-49e60894c1be';

SET @SCHEMA_VERSION = '00001';
SET @OWNER = '0a662938-e11e-4825-bd45-fa117963d12f';
SET @ADMIN_ACCOUNT = '3d8ce3de-18c0-452c-b135-47e886a8349e';
SET @ADMIN_GROUP = '2d30543a-1d09-451e-829b-f4a4de8a326a';
SET @PUBLIC_ACCOUNT = '088e3bc0-7fb2-422e-b29a-71ca3ec907d2';
SET @PUBLIC_GROUP = 'be851b5b-64f0-4298-b8c0-b5b3d4629d6e';


/*
 * Cloud
 */
CREATE TABLE clouds (
    id          IDENTITY NOT NULL PRIMARY KEY,
    name        VARCHAR,
    UNIQUE (name)
);

INSERT INTO clouds (name) VALUES (@TESTCLOUD);
    

/*
 * Nodes
 */
CREATE TABLE nodes (
  id                    UUID    NOT NULL PRIMARY KEY,
  baseUrl               VARCHAR NOT NULL,
  institution           VARCHAR NOT NULL,
  local                 BOOLEAN NOT NULL DEFAULT FALSE,
  publicNode            BOOLEAN NOT NULL DEFAULT FALSE,
  version               VARCHAR NOT NULL DEFAULT '00005',
  publicKey             VARCHAR NOT NULL DEFAULT '',
);

INSERT INTO nodes (id, baseUrl, institution, local, version) VALUES 
  (CAST(@TESTNODE AS UUID), 'http://localhost/', 'TEST', true, @SCHEMA_VERSION);

/*
 * CloudNodes
 */
CREATE TABLE cloud_nodes (
    id          IDENTITY NOT NULL PRIMARY KEY,
    cloud_id    BIGINT NOT NULL REFERENCES clouds(id) ON DELETE CASCADE,
    node_id     UUID NOT NULL REFERENCES nodes (id) ON DELETE CASCADE,
    rank        INTEGER NOT NULL DEFAULT 1,
    publickey   VARCHAR NOT NULL DEFAULT '',
    failures    INTEGER NOT NULL DEFAULT 0,
    retryTime   BIGINT NOT NULL DEFAULT 0,
    UNIQUE (cloud_id, node_id)
);

/* we suppose the cloud_id to be 1 */
INSERT INTO cloud_nodes (cloud_id, node_id) VALUES (1, CAST(@TESTNODE AS UUID));
    
/*
 * Admission (users, groups, memberships, ACLs, ...)
 */
CREATE TABLE usersGroups (
    id                  UUID NOT NULL PRIMARY KEY,
    memberType          VARCHAR(1) NOT NULL,
    subSystemType       INTEGER,
    subSystemData       VARCHAR,
    modified            TIMESTAMP DEFAULT now(),
    node_id             UUID REFERENCES nodes(id) ON DELETE CASCADE,
    login               VARCHAR,
    name                VARCHAR,
    email               VARCHAR,
    password            VARCHAR,
    phone               VARCHAR
);


CREATE TABLE memberships (
    id          UUID NOT NULL PRIMARY KEY,
    group_id    UUID NOT NULL REFERENCES usersGroups (id) ON DELETE CASCADE,
    member_id   UUID NOT NULL REFERENCES usersGroups (id) ON DELETE CASCADE,
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
        membership_id           UUID NOT NULL REFERENCES memberships(id) ON DELETE CASCADE,
        nestingpathset_id       UUID NOT NULL REFERENCES nestingpathsets(id) ON DELETE CASCADE,
        UNIQUE(nestingpathset_id, membership_id)
);

CREATE TABLE nestingpathset_memberships (
        nestingpathsets_id      UUID NOT NULL REFERENCES nestingpathsets(id) ON DELETE CASCADE,
        memberships_id          UUID NOT NULL REFERENCES memberships(id) ON DELETE CASCADE,
        UNIQUE(nestingpathsets_id, memberships_id)
);

/*
 * ACLs
 */

CREATE TABLE aclists (
    id          UUID PRIMARY KEY,
    name        VARCHAR,
    node_id     UUID REFERENCES nodes (id) ON DELETE CASCADE,
    modified    TIMESTAMP DEFAULT now(),
    permCode    INTEGER
);

CREATE TABLE acentries (
    aclist_id   UUID NOT NULL REFERENCES aclists(id) ON DELETE CASCADE,
    member_id   UUID NOT NULL REFERENCES usersGroups(id) ON DELETE CASCADE,
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
 * Collections and other distributed resources
 */
CREATE TABLE collections (
  id          UUID NOT NULL PRIMARY KEY,
  description VARCHAR,
  name        VARCHAR,
  indexPath   VARCHAR,
  storagePath VARCHAR,
  node_id     UUID NOT NULL REFERENCES nodes (id) ON DELETE CASCADE,
  owner_id    UUID NOT NULL REFERENCES usersGroups(id) ON DELETE CASCADE,
  aclist_id   UUID NOT NULL REFERENCES aclists(id) ON DELETE CASCADE
);

CREATE TABLE files (
  id            UUID    NOT NULL PRIMARY KEY,
  name          VARCHAR NOT NULL,
  filename      VARCHAR NOT NULL,
  hash          VARCHAR,
  created       TIMESTAMP DEFAULT now(),
  user_id       UUID REFERENCES usersGroups (id) ON DELETE SET NULL,
  document_language VARCHAR NOT NULL DEFAULT 'en',
  collection_id UUID NOT NULL REFERENCES collections (id)
);

CREATE TABLE info (
  key           VARCHAR NOT NULL PRIMARY KEY,
  value         VARCHAR,
  owner_id      UUID REFERENCES usersGroups(id) ON DELETE CASCADE,
  aclist_id     UUID REFERENCES aclists(id) ON DELETE CASCADE
);

CREATE TABLE topics (
  id            UUID NOT NULL PRIMARY KEY,
  name          VARCHAR,
  category      VARCHAR,
  owner_id      UUID REFERENCES usersGroups(id) ON DELETE CASCADE,
  aclist_id     UUID REFERENCES aclists(id) ON DELETE CASCADE,
  node_id       UUID NOT NULL REFERENCES nodes (id) ON DELETE CASCADE,
  cloud_name    VARCHAR NOT NULL
);

CREATE TABLE postings (
  id            UUID NOT NULL PRIMARY KEY,
  text          VARCHAR,
  owner_id      UUID REFERENCES usersGroups(id) ON DELETE CASCADE,
  topic_id      UUID REFERENCES topics(id) ON DELETE CASCADE,
  created       TIMESTAMP DEFAULT now()
);

CREATE TABLE termvectors (
  wordroot    VARCHAR    NOT NULL,
  file_id     UUID     NOT NULL,
  termfrequency INTEGER NOT NULL,
 PRIMARY KEY(wordroot, file_id)
);

CREATE TABLE unstemmed_words(
  wordroot VARCHAR NOT NULL,
  file_id UUID NOT NULL REFERENCES files (id),
  unstemmed_word VARCHAR NOT NULL,
  PRIMARY KEY(file_id, unstemmed_word)  
);

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
	budget DOUBLE,
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
	budget DOUBLE NOT NULL);


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
        molecule VARCHAR);

CREATE TABLE structures (
        id INTEGER PRIMARY KEY REFERENCES materials(materialid),
        sumformula VARCHAR,
        molarMass DOUBLE,
        exactMolarMass DOUBLE,
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
        mDate TIMESTAMP NOT NULL,
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
    amount DOUBLE NOT NULL,
    articleid INTEGER,
    projectid INTEGER REFERENCES projects(id),
    concentration DOUBLE,
    unit VARCHAR,
    purity VARCHAR,
    solventid INTEGER REFERENCES solvents(id),
    description VARCHAR,
    owner UUID  NOT NULL REFERENCES usersgroups(id),
    containersize DOUBLE ,
    containertype VARCHAR REFERENCES containertypes(name),
    containerid INTEGER REFERENCES containers(id),
    ctime TIMESTAMP  NOT NULL DEFAULT now());

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

INSERT INTO taxonomy_level VALUES(1,'domain');
INSERT INTO taxonomy_level VALUES(2,'kingdom');
INSERT INTO taxonomy_level VALUES(3,'phylum');
INSERT INTO taxonomy_level VALUES(4,'class');
INSERT INTO taxonomy_level VALUES(5,'order');
INSERT INTO taxonomy_level VALUES(6,'family');
INSERT INTO taxonomy_level VALUES(7,'genus');
INSERT INTO taxonomy_level VALUES(8,'species');