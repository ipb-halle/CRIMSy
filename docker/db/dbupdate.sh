#!/bin/bash
#
# Cloud Resource & Information Management System (CRIMSy)
# Copyright 2022 Leibniz-Institut f. Pflanzenbiochemie 
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#
#==========================================================
#
# DEVELOPERS PLEASE NOTE:
# -----------------------
# - script to be installed in container at /usr/local/bin
#   and executed as user postgres
#
# - developer must update the value of EXPECTED_SCHEMA_VERSION
#   and the list of update commands in the "case" section
#
# - the database update scripts (SQL) must include update 
#   statements for the schema version
#
#
EXPECTED_SCHEMA_VERSION=00003
cd /docker-entrypoint-initdb.d/

function getSchemaVersion {
    LBAC_SCHEMA_VERSION=`echo "\\pset tuples_only on
        SELECT value FROM lbac.info WHERE key='DBSchema Version';" \
        | psql lbac | head -1 | tr -d ' '`
}

getSchemaVersion
echo "Found schema of database             : $LBAC_SCHEMA_VERSION"
echo "Found target schema in filedefinition: $EXPECTED_SCHEMA_VERSION "

while [ "$LBAC_SCHEMA_VERSION" != "$EXPECTED_SCHEMA_VERSION" ] ; do
    OLD_SCHEMA_VERSION=$LBAC_SCHEMA_VERSION
    NEXT_SCHEMA_VERSION=`printf '%05i' $(($LBAC_SCHEMA_VERSION + 1))`

    echo "Applying $NEXT_SCHEMA_VERSION.sql ..."
    cat $NEXT_SCHEMA_VERSION.sql | psql lbac

    # detect if schema update has succeeded
    getSchemaVersion
    if [ $OLD_SCHEMA_VERSION = $LBAC_SCHEMA_VERSION ] ; then
        echo "ERROR: Database update failed!"
        echo "Found schema: $OLD_SCHEMA_VERSION"
        echo "Expected schema: $NEXT_SCHEMA_VERSION"
        exit 1
    fi

done
echo "SUCCESS: Database update succeeded!"
