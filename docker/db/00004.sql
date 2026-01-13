/*
 * Cloud Resource & Information Management System (CRIMSy)
 * Initial data 
 * Copyright 2022 Leibniz-Institut f. Pflanzenbiochemie
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
\set LBAC_SCHEMA_VERSION '\'00004\''



/*
create user_session class
*/

CREATE TABLE user_sessions (
    id          SERIAL NOT NULL PRIMARY KEY,
    user_id     INTEGER NOT NULL REFERENCES usersGroups (id) ON DELETE CASCADE,
    token       VARCHAR(255) NOT NULL UNIQUE,
    last_seen   TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_user_sessions_user_id ON user_sessions(user_id);
CREATE INDEX idx_user_sessions_last_seen ON user_sessions(last_seen);
