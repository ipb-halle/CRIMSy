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
SET @SCHEMA_VERSION = '00001';


CREATE DOMAIN IF NOT EXISTS jsonb AS other;
CREATE DOMAIN IF NOT EXISTS RawJsonb AS other;

CREATE ALIAS SUBSTRUCTURE FOR "de.ipb_halle.h2.MockSubstructureMatch.substructure";

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
  publicKey             VARCHAR NOT NULL DEFAULT ''
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
    id                  SERIAL NOT NULL PRIMARY KEY,
    memberType          VARCHAR(1) NOT NULL,
    subSystemType       INTEGER,
    subSystemData       VARCHAR,
    modified            TIMESTAMP DEFAULT now(),
    node_id             UUID REFERENCES nodes(id) ON DELETE CASCADE,
    login               VARCHAR,
    name                VARCHAR,
    email               VARCHAR,
    password            VARCHAR,
    phone               VARCHAR,
    shortcut            VARCHAR UNIQUE CHECK (upper(shortcut) = shortcut)
);


CREATE TABLE memberships (
    id          SERIAL NOT NULL PRIMARY KEY,
    group_id    INTEGER NOT NULL REFERENCES usersGroups (id) ON DELETE CASCADE,
    member_id   INTEGER NOT NULL REFERENCES usersGroups (id) ON DELETE CASCADE,
    nested      BOOLEAN NOT NULL DEFAULT FALSE,
    UNIQUE(group_id, member_id)
);

CREATE INDEX i_memberships_group ON memberships (group_id);
CREATE INDEX i_memberships_member ON memberships (member_id);


CREATE TABLE nestingpathsets (
    id  SERIAL NOT NULL PRIMARY KEY,
    membership_id INTEGER NOT NULL REFERENCES memberships(id) ON UPDATE CASCADE ON DELETE CASCADE
);

CREATE TABLE nestingpathset_memberships (
        nestingpathsets_id      INTEGER NOT NULL REFERENCES nestingpathsets(id) ON DELETE CASCADE,
        memberships_id          INTEGER NOT NULL REFERENCES memberships(id) ON DELETE CASCADE,
        UNIQUE(nestingpathsets_id, memberships_id)
);

/*
 * ACLs
 */

CREATE TABLE aclists (
    id          SERIAL NOT NULL PRIMARY KEY,
    name        VARCHAR,
    modified    TIMESTAMP DEFAULT now(),
    permCode    INTEGER
);

CREATE TABLE acentries (
    aclist_id   INTEGER NOT NULL REFERENCES aclists(id) ON DELETE CASCADE,
    member_id   INTEGER NOT NULL REFERENCES usersGroups(id) ON DELETE CASCADE,
    permRead    BOOLEAN NOT NULL DEFAULT FALSE,
    permEdit    BOOLEAN NOT NULL DEFAULT FALSE,
    permCreate  BOOLEAN NOT NULL DEFAULT FALSE,
    permDelete  BOOLEAN NOT NULL DEFAULT FALSE,
    permChown   BOOLEAN NOT NULL DEFAULT FALSE,
    permGrant   BOOLEAN NOT NULL DEFAULT FALSE,
    permSuper   BOOLEAN NOT NULL DEFAULT FALSE,
    PRIMARY KEY(aclist_id, member_id)
);

CREATE TABLE info (
  key           VARCHAR NOT NULL PRIMARY KEY,
  value         VARCHAR,
  owner_id      INTEGER REFERENCES usersGroups(id) ON DELETE CASCADE,
  aclist_id     INTEGER REFERENCES aclists(id) ON DELETE CASCADE
);

/*
 * Collections and other distributed resources
 */
CREATE TABLE collections (
  id          SERIAL NOT NULL PRIMARY KEY,
  description VARCHAR,
  name        VARCHAR,
  indexPath   VARCHAR,
  storagePath VARCHAR,
  owner_id    INTEGER NOT NULL REFERENCES usersGroups(id) ON DELETE CASCADE,
  aclist_id   INTEGER NOT NULL REFERENCES aclists(id) ON DELETE CASCADE
);

CREATE TABLE files (
  id            SERIAL NOT NULL PRIMARY KEY,
  name          VARCHAR NOT NULL,
  filename      VARCHAR NOT NULL,
  hash          VARCHAR,
  created       TIMESTAMP DEFAULT now(),
  user_id       INTEGER REFERENCES usersGroups (id) ON DELETE SET NULL,
  document_language VARCHAR NOT NULL DEFAULT 'en',
  collection_id INTEGER NOT NULL REFERENCES collections (id) ON UPDATE CASCADE ON DELETE CASCADE
);



CREATE TABLE topics (
  id            SERIAL NOT NULL PRIMARY KEY,
  name          VARCHAR,
  category      VARCHAR,
  owner_id      INTEGER REFERENCES usersGroups(id) ON DELETE CASCADE,
  aclist_id     INTEGER REFERENCES aclists(id) ON DELETE CASCADE,
  cloud_name    VARCHAR NOT NULL
);

CREATE TABLE postings (
  id            SERIAL NOT NULL PRIMARY KEY,
  text          VARCHAR,
  owner_id      INTEGER REFERENCES usersGroups(id) ON DELETE CASCADE,
  topic_id      INTEGER REFERENCES topics(id) ON DELETE CASCADE,
  created       TIMESTAMP DEFAULT now()
);

CREATE TABLE termvectors (
  wordroot    VARCHAR    NOT NULL ,
  file_id     INTEGER     NOT NULL,
  termfrequency INTEGER NOT NULL,
 PRIMARY KEY(wordroot, file_id)
);

CREATE TABLE unstemmed_words(
  stemmed_word VARCHAR NOT NULL,
  file_id INTEGER NOT NULL REFERENCES files (id) ON UPDATE CASCADE ON DELETE CASCADE,
  unstemmed_word VARCHAR NOT NULL,
  PRIMARY KEY(stemmed_word,file_id, unstemmed_word),
  FOREIGN KEY (stemmed_word, file_id) REFERENCES termvectors (wordroot, file_id)
      ON UPDATE CASCADE ON DELETE CASCADE
);

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
  mandatory BOOLEAN NOT NULL);

CREATE TABLE projects (
  id SERIAL NOT NULL PRIMARY KEY,
  name VARCHAR NOT NULL,
  budget DOUBLE,
  budgetBlocked BOOLEAN default false,
  projecttypeid INTEGER NOT NULL REFERENCES projecttypes(id),
  owner_id INTEGER NOT NULL REFERENCES usersgroups(id),
  aclist_id INTEGER NOT NULL REFERENCES aclists(id),
  description VARCHAR,
  ctime TIMESTAMP  NOT NULL DEFAULT now(),
  mtime TIMESTAMP  NOT NULL DEFAULT now(),
  deactivated BOOLEAN NOT NULL DEFAULT false
);


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
    budget DOUBLE NOT NULL
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
        javaclass VARCHAR);

CREATE TABLE materials (
        materialid SERIAL NOT NULL PRIMARY KEY,
        materialTypeId INTEGER NOT NULL REFERENCES materialtypes(id),
        ctime TIMESTAMP  NOT NULL DEFAULT now(),
        aclist_id INTEGER NOT NULL REFERENCES aclists(id),
        owner_id INTEGER NOT NULL REFERENCES usersgroups(id),
        deactivated BOOLEAN NOT NULL,
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
        molecule VARCHAR);

CREATE TABLE structures (
        id INTEGER PRIMARY KEY REFERENCES materials(materialid),
        sumformula VARCHAR,
        molarMass DOUBLE,
        exactMolarMass DOUBLE,
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
        name VARCHAR NOT NULL,
        category INTEGER NOT NULL,
        has_remarks BOOLEAN NOT NULL DEFAULT false);

CREATE TABLE  material_hazards (
        typeid INTEGER NOT NULL REFERENCES hazards(id),
        materialid INTEGER NOT NULL REFERENCES materials(materialid),
        remarks VARCHAR,
        PRIMARY KEY(materialid,typeid)
);

CREATE TABLE  storageclasses (
        id INTEGER PRIMARY KEY,
        name VARCHAR NOT NULL
);

CREATE TABLE  storageconditions (
        id INTEGER PRIMARY KEY,
        name VARCHAR NOT NULL
);

CREATE TABLE  storages (
        materialid INTEGER PRIMARY KEY REFERENCES materials(materialid),
        storageclass INTEGER NOT NULL REFERENCES storageClasses(id),
        description VARCHAR
);


CREATE TABLE  storageconditions_material (
        conditionId INTEGER NOT NULL REFERENCES storageconditions(id),
        materialid INTEGER NOT NULL REFERENCES materials(materialid),
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

insert into indextypes(name,javaclass)values('name',null);
insert into indextypes(name,javaclass)values('GESTIS/ZVG',null);
insert into indextypes(name,javaclass)values('CAS/RM',null);
insert into indextypes(name,javaclass)values('Carl Roth Sicherheitsdatenblatt',null);

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

insert into hazards(id,name,category,has_remarks)values(1,'GHS01',1,false);
insert into hazards(id,name,category,has_remarks)values(2,'GHS02',1,false);
insert into hazards(id,name,category,has_remarks)values(3,'GHS03',1,false);
insert into hazards(id,name,category,has_remarks)values(4,'GHS04',1,false);
insert into hazards(id,name,category,has_remarks)values(5,'GHS05',1,false);
insert into hazards(id,name,category,has_remarks)values(6,'GHS06',1,false);
insert into hazards(id,name,category,has_remarks)values(7,'GHS07',1,false);
insert into hazards(id,name,category,has_remarks)values(8,'GHS08',1,false);
insert into hazards(id,name,category,has_remarks)values(9,'GHS09',1,false);
insert into hazards(id,name,category,has_remarks)values(10,'HS',2,true);
insert into hazards(id,name,category,has_remarks)values(11,'PS',2,true);
insert into hazards(id,name,category,has_remarks)values(12,'S1',3,false);
insert into hazards(id,name,category,has_remarks)values(13,'S2',3,false);
insert into hazards(id,name,category,has_remarks)values(14,'S3',3,false);
insert into hazards(id,name,category,has_remarks)values(15,'S4',3,false);
insert into hazards(id,name,category,has_remarks)values(16,'R1',4,false);
insert into hazards(id,name,category,has_remarks)values(17,'C1',5,true);
insert into hazards(id,name,category,has_remarks)values(18,'GHS10',1,false);
insert into hazards(id,name,category,has_remarks)values(19,'GHS11',1,false);
insert into hazards(id,name,category,has_remarks)values(20,'GMO',6,false);

CREATE TABLE  materials_hist (
        materialid INTEGER NOT NULL REFERENCES materials(materialid),
        actorId INTEGER NOT NULL REFERENCES usersgroups(id),
        mDate TIMESTAMP NOT NULL,
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

CREATE TABLE  material_hazards_hist (
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
    parentcontainer INTEGER REFERENCES containers(id) ON UPDATE CASCADE ON DELETE SET NULL,
    label VARCHAR NOT NULL,
    projectid INTEGER REFERENCES projects(id),
    rows INTEGER,
    columns INTEGER,
    type VARCHAR NOT NULL REFERENCES containertypes(name),
    firearea VARCHAR,
    gmosafetylevel VARCHAR,
    barcode VARCHAR,
    swapdimensions BOOLEAN NOT NULL DEFAULT false,
    zerobased BOOLEAN NOT NULL DEFAULT false,
    deactivated BOOLEAN NOT NULL DEFAULT false);

CREATE TABLE nested_containers(
    sourceid INTEGER NOT NULL REFERENCES containers(id) ON UPDATE CASCADE ON DELETE CASCADE,
    targetid INTEGER NOT NULL REFERENCES containers(id) ON UPDATE CASCADE ON DELETE CASCADE,
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
    concentrationunit VARCHAR,
    unit VARCHAR,
    purity VARCHAR,
    solventid INTEGER REFERENCES solvents(id),
    description VARCHAR,
    owner_id INTEGER  NOT NULL REFERENCES usersgroups(id),
    containersize DOUBLE ,
    containertype VARCHAR REFERENCES containertypes(name),
    containerid INTEGER REFERENCES containers(id) ON UPDATE CASCADE ON DELETE SET NULL,
    aclist_id INTEGER NOT NULL,
    expiry_date TIMESTAMP,
    ctime TIMESTAMP  NOT NULL DEFAULT now(),
    label VARCHAR,
    parent_id INTEGER REFERENCES items(id)
 );

CREATE TABLE item_positions(
    id SERIAL NOT NULL PRIMARY KEY,
    itemid INTEGER NOT NULL REFERENCES items(id) ON UPDATE CASCADE ON DELETE CASCADE,
    containerid INTEGER NOT NULL REFERENCES containers(id) ON UPDATE CASCADE ON DELETE CASCADE,
    itemrow INTEGER,
    itemcol INTEGER,
    UNIQUE(itemid),
    UNIQUE(containerid, itemrow, itemcol)
);

CREATE TABLE items_history(
    itemid INTEGER NOT NULL REFERENCES items(id) ON UPDATE CASCADE ON DELETE CASCADE,
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
    parent_containerid_new INTEGER REFERENCES containers(id) ON UPDATE CASCADE ON DELETE CASCADE,
    parent_containerid_old INTEGER REFERENCES containers(id) ON UPDATE CASCADE ON DELETE CASCADE,
    aclistid_old INTEGER REFERENCES aclists(id) ON UPDATE CASCADE ON DELETE CASCADE,
    aclistid_new INTEGER REFERENCES aclists(id) ON UPDATE CASCADE ON DELETE CASCADE,
    PRIMARY KEY(itemid,actorid,mdate));

CREATE TABLE item_positions_history(
    id SERIAL PRIMARY KEY,
    itemid INTEGER NOT NULL REFERENCES items(id) ON UPDATE CASCADE ON DELETE CASCADE,
    containerid INTEGER NOT NULL REFERENCES containers(id) ON UPDATE CASCADE ON DELETE CASCADE,
    mdate TIMESTAMP NOT NULL,
    actorid INTEGER NOT NULL REFERENCES usersgroups(id),
    row_old INTEGER,
    row_new INTEGER,
    col_old INTEGER,
    col_new INTEGER
);

insert into containertypes(name,description,rank,transportable,unique_name)values('ROOM',null,100,false,true);
insert into containertypes(name,description,rank,transportable,unique_name)values('CUPBOARD',null,90,false,false);
insert into containertypes(name,description,rank,transportable,unique_name)values('FREEZER',null,90,false,false);
insert into containertypes(name,description,rank,transportable,unique_name)values('TRAY',null,60,true,true);
insert into containertypes(name,description,rank,transportable,unique_name)values('WELLPLATE',null,50,true,true);
insert into containertypes(name,description,rank,transportable,unique_name)values('GLASS_BOTTLE',null,0,true,true);
insert into containertypes(name,description,rank,transportable,unique_name)values('PLASTIC_BOTTLE',null,0,true,true);
insert into containertypes(name,description,rank,transportable,unique_name)values('GLASS_VIAL',null,0,true,true);
insert into containertypes(name,description,rank,transportable,unique_name)values('PLASTIC_VIAL',null,0,true,true);
insert into containertypes(name,description,rank,transportable,unique_name)values('GLASS_AMPOULE',null,0,true,true);
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
    level INTEGER NOT NULL REFERENCES taxonomy_level(id)
    );

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
    id INTEGER NOT NULL REFERENCES biomaterial(id) ON UPDATE CASCADE ON DELETE CASCADE,
    actorid INTEGER NOT NULL REFERENCES usersGroups(id) ON UPDATE CASCADE ON DELETE CASCADE,
    mtime TIMESTAMP NOT NULL,
    digest VARCHAR,
    action VARCHAR NOT NULL,
    tissueid_old INTEGER REFERENCES tissues(id)  ON UPDATE CASCADE ON DELETE CASCADE,
    tissueid_new INTEGER REFERENCES tissues(id)  ON UPDATE CASCADE ON DELETE CASCADE,
    taxoid_old INTEGER REFERENCES taxonomy(id)  ON UPDATE CASCADE ON DELETE CASCADE,
    taxoid_new INTEGER REFERENCES taxonomy(id)  ON UPDATE CASCADE ON DELETE CASCADE,
    PRIMARY KEY(id,actorid,mtime)
);

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
    folderid        INTEGER NOT NULL REFERENCES folders(folderid)  ON UPDATE CASCADE ON DELETE CASCADE,
    aclist_id       INTEGER REFERENCES aclists(id) ON UPDATE CASCADE ON DELETE CASCADE,
    owner_id        INTEGER REFERENCES usersGroups(id) ON UPDATE CASCADE ON DELETE CASCADE,
    ctime           TIMESTAMP NOT NULL,
    projectid       INTEGER REFERENCES projects(id) ON UPDATE CASCADE ON DELETE CASCADE
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
 * Note: either materialid or itemid must be set
 *
 * ToDo: see below
 *   - additional indexing for payload column (example see below)
 *   - additional references (documents, users, ...)
 */
CREATE TABLE linked_data (
    recordid        BIGSERIAL NOT NULL PRIMARY KEY,
    exprecordid     BIGINT NOT NULL REFERENCES exp_records(exprecordid) ON UPDATE CASCADE ON DELETE CASCADE,
    materialid      INTEGER REFERENCES materials(materialid) ON UPDATE CASCADE ON DELETE CASCADE,
    fileid          INTEGER REFERENCES files(id) ON UPDATE CASCADE ON DELETE CASCADE,
    itemid          INTEGER REFERENCES items(id) ON UPDATE CASCADE ON DELETE CASCADE,
    rank            INTEGER DEFAULT 0,
    type            INTEGER NOT NULL,
    payload         VARCHAR
);

/*
 * B-tree index example:
 * CREATE INDEX i_exp_assay_outcome_val ON exp_assay_outcomes (((outcome->>'val')::DOUBLE PRECISION))
 *      WHERE (outcome->>'val') IS NOT NULL;
 */

CREATE TABLE exp_assays (
    exprecordid     BIGINT NOT NULL REFERENCES exp_records(exprecordid) ON UPDATE CASCADE ON DELETE CASCADE,
    outcometype     INTEGER NOT NULL,
    remarks         VARCHAR,
    targetid        INTEGER REFERENCES materials (materialid) ON UPDATE CASCADE ON DELETE SET NULL,
    units           VARCHAR
);

/* ToDo: xxxxx create fulltext index on exp_texts! */
CREATE TABLE exp_texts (
    exprecordid     BIGINT NOT NULL REFERENCES exp_records(exprecordid) ON UPDATE CASCADE ON DELETE CASCADE,
    text            VARCHAR 
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
    title       VARCHAR,
    preview     VARCHAR,
    image       VARCHAR,
    aclist_id   INTEGER REFERENCES aclists(id) ON UPDATE CASCADE ON DELETE CASCADE,
    owner_id    INTEGER REFERENCES usersGroups(id) ON UPDATE CASCADE ON DELETE CASCADE
);


