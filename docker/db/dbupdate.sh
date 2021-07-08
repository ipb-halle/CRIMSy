#!/bin/bash
#
# Leibniz Bioactives Cloud 
# Copyright 2018 Leibniz-Institut f. Pflanzenbiochemie 
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
# - developer must update the value of CURRENT_SCHEMA_VERSION
#   and the list of update commands in the "case" section
#
# - the database update scripts (SQL) must include update 
#   statements for the schema version
#
#
CURRENT_SCHEMA_VERSION=00001
CURRENT_PG_VERSION=12
 cd /docker-entrypoint-initdb.d/

function getSchemaVersion {
    if [ -e /data/db/pgsql/PG_VERSION ] ; then
        PG_VERSION=`cat /data/db/pgsql/PG_VERSION`
    else
        PG_VERSION=$CURRENT_PG_VERSION
        ln -s /data/db/pgsql_$CURRENT_PG_VERSION /data/db/pgsql
    fi

    if [ $PG_VERSION != $CURRENT_PG_VERSION ] ; then
        LBAC_SCHEMA_VERSION='BACKUP'

        rm /data/db/pgsql
        ln -s /data/db/pgsql_$CURRENT_PG_VERSION /data/db/pgsql
        return
    fi

    LBAC_SCHEMA_VERSION=`echo "\\pset tuples_only on
        SELECT value FROM lbac.info WHERE key='DBSchema Version';" \
        | psql lbac | head -1 | tr -d ' '`
}

function updatePre11 {
    if [ ! -h /data/db/pgsql ] ; then
        if [ -e /data/db/pgsql ] ; then
            mv /data/db/pgsql /data/db/pgsql_96
            ln -s /data/db/pgsql_96 /data/db/pgsql
        fi
    fi
}

    updatePre11
    getSchemaVersion

    echo "Found schema of database             : $LBAC_SCHEMA_VERSION"
    echo "Found target schema in filedefinition: $CURRENT_SCHEMA_VERSION "

while [ "$LBAC_SCHEMA_VERSION" != "$CURRENT_SCHEMA_VERSION" ] ; do
    OLD_SCHEMA_VERSION=$LBAC_SCHEMA_VERSION

    case $LBAC_SCHEMA_VERSION in
        BACKUP)
            echo "Scheduling version update ..."
            touch /data/db/VERSION_UPDATE
            exit
            ;;
        00000)
            echo "Applying 00001.sql ..."
            cat 00001.sql | psql lbac
            ;;
        00001)
            echo "reached head of development"
            ;;
    esac

    # detect if schema update has succeeded
    getSchemaVersion
    if test $OLD_SCHEMA_VERSION = $LBAC_SCHEMA_VERSION ; then
        echo "ERROR: Database update failed!"
        echo "Found old schema: $OLD_SCHEMA_VERSION"
        echo "Found new schema: $LBAC_SCHEMA_VERSION"
        exit 1
    fi

done

