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
\set LBAC_SCHEMA_VERSION '\'00003\''

BEGIN TRANSACTION;
UPDATE lbac.info SET value=:LBAC_SCHEMA_VERSION WHERE key='DBSchema Version';

/*
 * Termvector table
 */
CREATE TABLE termvectors (
  wordroot    VARCHAR    NOT NULL,
  file_id     UUID NOT NULL REFERENCES files (id) ON UPDATE CASCADE ON DELETE CASCADE,
  termfrequency INTEGER NOT NULL,
  PRIMARY KEY(wordroot, file_id)
);

CREATE INDEX i_termvectors_file_id ON termvectors (file_id);

ALTER TABLE files ADD COLUMN document_language VARCHAR NOT NULL DEFAULT 'en';

CREATE TABLE unstemmed_words(
  wordroot VARCHAR NOT NULL,
  file_id UUID NOT NULL REFERENCES files (id) ON UPDATE CASCADE ON DELETE CASCADE,
  unstemmed_word VARCHAR NOT NULL,
  PRIMARY KEY(file_id, unstemmed_word),
  FOREIGN KEY (wordroot, file_id) REFERENCES termvectors (wordroot, file_id)
      ON UPDATE CASCADE ON DELETE CASCADE
);
  
CREATE INDEX i_unstemmed_words_wordroot ON  unstemmed_words (wordroot);

COMMIT TRANSACTION;

/*
 * Actions not under transaction control
 */
ALTER TABLE files DROP COLUMN termvectors;
