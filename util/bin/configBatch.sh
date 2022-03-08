#!/bin/bash
#
# Batch configuration
# Cloud Resource & Information Management System
#
# Copyright 2020 Leibniz-Institut f. Pflanzenbiochemie 
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
#
#==========================================================
#
# This script generates a node configuration in batch mode 
# which is necessary for integration testing (Selenium / Cypress).
# The script operates the original configure.sh script for 
# the current (PRIMARY) cloud.
#

p=`dirname $0`
LBAC_DATASTORE=`realpath $p`
CRIMSY_HOST=$2

#
#==========================================================
#
# Provide test data (from nodeconfig.cfg file) 
#
function getTestData {

    . nodeconfig.cfg
    LBAC_INSTITUTION_MD5=`echo -n $LBAC_INSTITUTION | md5sum | cut -c1-32`
    CRIMSYCI_URL=http://$CRIMSY_HOST:8000/$PRIMARY_CLOUD
    CRIMSYREG_URL=$CRIMSY_HOST:5000

}
#
#==========================================================
#
function createConfiguration {
    getTestData

    curl --silent --output configure.sh.sig $CRIMSYCI_URL/configure.sh.sig
    curl --silent --output chain.pem $CRIMSYCI_URL/chain.pem
    curl --silent --output devcert.pem $CRIMSYCI_URL/devcert.pem

    openssl verify -CAfile chain.pem devcert.pem || exit 1
    openssl smime -verify -in configure.sh.sig -certfile devcert.pem -CAfile chain.pem -out configure.sh || exit 1
    chmod +x configure.sh

    . configure.sh BATCH

    # call getTestData 2nd time
    getTestData
    LBAC_IMAGE_REGISTRY=$CRIMSYREG_URL
    makeTempConfig

    # dialog_DATASTORE
    echo "LBAC_DATASTORE=\"$LBAC_DATASTORE\"" > $HOME/.lbac 
    echo "LBAC_DATASTORE=\"$LBAC_DATASTORE\"" >> $TMP_CONFIG 
    makeDirectories

    # always a fresh object id!
    LBAC_NODE_ID=`uuidgen -r | tr -d $'\n'`
    echo "LBAC_NODE_ID=\"$LBAC_NODE_ID\"" >> $TMP_CONFIG

    echo "LBAC_PROXY_HSTS=\"OFF\"" >> $TMP_CONFIG
    echo "LBAC_INIT_TYPE=\"SYSTEMD\"" >> $TMP_CONFIG
    echo "LBAC_DOCKER_EXCLUSIVE=\"OFF\"" >> $TMP_CONFIG

    # set default admin password (not for production use!)
    echo -n "admin" > "$LBAC_DATASTORE/etc/$LBAC_ADMIN_PWFILE"

    LBAC_INTRANET_FQHN=`hostname -f`
    LBAC_INTERNET_FQHN=$LBAC_INTRANET_FQHN

    echo "LBAC_UPDATE_LEVEL=\"$LBAC_UPDATE_LEVEL\"" >> $TMP_CONFIG
    echo "LBAC_INSTITUTION=\"$LBAC_INSTITUTION\"" >> $TMP_CONFIG
    echo "LBAC_INSTITUTION_SHORT=\"$LBAC_INSTITUTION_SHORT\"" >> $TMP_CONFIG
    echo "LBAC_INSTITUTION_MD5=\"$LBAC_INSTITUTION_MD5\"" >> $TMP_CONFIG
    echo "LBAC_MANAGER_EMAIL=\"$LBAC_MANAGER_EMAIL\"" >> $TMP_CONFIG
    echo "LBAC_INTRANET_FQHN=\"$LBAC_INTRANET_FQHN\"" >> $TMP_CONFIG
    echo "LBAC_INTERNET_FQHN=\"$LBAC_INTRANET_FQHN\"" >> $TMP_CONFIG

    LBAC_COUNTRY="XX"
    LBAC_STATE="Some State"
    LBAC_SSL_ORGANIZATION="$LBAC_INSTITUTION"
    LBAC_SSL_OU="Integration Testing Dept."
    LBAC_SSL_EMAIL="$LBAC_MANAGER_EMAIL"

    makeCertReq
    appendCertRequest

    # dialog_SAVE
    mv $TMP_CONFIG $LBAC_DATASTORE/etc/$LBAC_CONFIG
    copyInstaller 
    upgradeOldConfig
    encrypt
    cleanUp
    rm configure.sh.sig
    mv configure.sh $LBAC_DATASTORE/bin
}

#
#==========================================================
#
function loadData {
    # initialize database with test data
    wget -o /dev/null -O /dev/null --no-check-certificate https://`hostname -f`/ui/index.xhtml
    echo "waiting 5 sec. for webapp to initialize database ..."
    sleep 5
    docker cp tmp/initial_data.sql dist_db_1:/tmp
    docker exec -i -u postgres dist_db_1 psql -Ulbac lbac -f /tmp/initial_data.sql
}
#
#==========================================================
#
case $1 in 
    CONFIG)
        createConfiguration
        exit 0
        ;;
    LOAD_DATA)
        loadData
        exit 0
        ;;
    CLEANUP)
        echo "cleaning up everything ..."
        sudo ./dist/bin/setupROOT.sh totalClean
    ;;
esac
