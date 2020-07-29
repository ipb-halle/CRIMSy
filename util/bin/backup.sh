#!/bin/bash
#
# LBAC Backup Script
# Copyright 2017 Leibniz-Institut f. Pflanzenbiochemie 
#
#  Licensed under the Apache License, Version 2.0 (the "License");
#  you may not use this file except in compliance with the License.
#  You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
#  Unless required by applicable law or agreed to in writing, software
#  distributed under the License is distributed on an "AS IS" BASIS,
#  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
#  See the License for the specific language governing permissions and
#  limitations under the License.
#
#==========================================================
#
BACKUP_AGE=4


function snapshotSOLR {
    collection=$0
    docker exec dist_lbacsolr_1 wget -O - \
      "http://localhost:8983/solr/$collection/replication?command=backup&numberToKeep=2"
}
export -f snapshotSOLR

#
#==========================================================
#
function backupDB {
	mkdir -p "$LBAC_DATASTORE/backup/db"
	pushd "$LBAC_DATASTORE/backup/db" > /dev/null
	rm -f dump.latest.sql
	docker exec dist_db_1 pg_dump --create --clean --if-exists \
          -U lbac > "$LBAC_DATASTORE/backup/db/dump.$DATE.sql" || \
          error "Error during database dump"
	ln -s dump.$DATE.sql dump.latest.sql
	popd > /dev/null
}

function backupSOLR {
        mkdir -p "$LBAC_DATASTORE/backup/solr"
        pushd "$LBAC_DATASTORE/backup/solr" > /dev/null

        #
        # snapshots will be made:
        # - for all Solr core directories (i.e. LBAC collections)
        # - by Solr, but
        # - only if Solr is currently running
        #
        "$LBAC_DATASTORE/dist/bin/lbacInit.sh" check lbacsolr | \
          grep -q running && \
          find ../../data/solr -mindepth 1 -maxdepth 1 -type d | \
          cut -c17- | xargs -l1 /bin/bash -c snapshotSOLR 

        rm -f solr.latest.tar.gz
        tar -C "$LBAC_DATASTORE/data" -czf solr.$DATE.tar.gz solr 
        ln -s solr.$DATE.tar.gz solr.latest.tar.gz
        popd > /dev/null
}

function backupUI {
        mkdir -p "$LBAC_DATASTORE/backup/ui"
        pushd "$LBAC_DATASTORE/backup/ui" > /dev/null
        rm -f ui.latest.tar.gz
        tar -C "$LBAC_DATASTORE/data" -czf ui.$DATE.tar.gz ui
        ln -s ui.$DATE.tar.gz ui.latest.tar.gz
        popd > /dev/null
}

function cleanUp {
        pushd "$LBAC_DATASTORE/backup" > /dev/null
        find db/ solr/ ui/ -type f -mtime +$BACKUP_AGE -exec rm {} \; 
        popd > /dev/null
}

function error {
	echo $1
	exit 1
}
#
#==========================================================
#
test `id -u` -eq 0 || error "This script must be called as root"

test -r "$HOME/.lbac" && . $HOME/.lbac 

test -n "$LBAC_DATASTORE" || error "LBAC_DATASTORE not defined" 

test -r "$LBAC_DATASTORE/dist/etc/config.sh" || error "Could not obtain LBAC configuration"
. "$LBAC_DATASTORE/dist/etc/config.sh"

DATE=`date "+%Y%m%d%H%M"`

"$LBAC_DATASTORE/dist/bin/lbacInit.sh" check db | grep -q running || error "Database container not running"

backupDB
backupSOLR
backupUI
cleanUp

