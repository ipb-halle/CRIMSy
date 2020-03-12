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
