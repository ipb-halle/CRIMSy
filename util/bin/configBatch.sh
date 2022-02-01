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

CRIMSYCI_URL=$2:8000/$3 
CRIMSYREG_URL=$2:5000
CLOUD=$3
TEST_ID=$4
p=`dirname $0`
LBAC_DATASTORE=`realpath $p`

#
#==========================================================
#
# Provide test data (from nodeconfig.txt file) 
#
function getTestData {

    data=`grep $TEST_ID nodeconfig.txt`
    PRIMARY_CLOUD=`echo "$data" | cut -c9-18 | sed -e 's/^[[:blank:]]*//;s/[[:blank:]]*$//'`
    LBAC_CITY=`echo "$data" | cut -c20-35 | sed -e 's/^[[:blank:]]*//;s/[[:blank:]]*$//'`
    LBAC_INSTITUTION=`echo "$data" | cut -c36-59 | sed -e 's/^[[:blank:]]*//;s/[[:blank:]]*$//'`
    LBAC_INSTITUTION_SHORT=`echo "$data" | cut -c50-68 | sed -e 's/^[[:blank:]]*//;s/[[:blank:]]*$//'`
    LBAC_INSTITUTION_MD5=`echo -n $LBAC_INSTITUTION | md5sum | cut -c1-32`
    LBAC_MANAGER_EMAIL=`echo "$data" | cut -c70-105 | sed -e 's/^[[:blank:]]*//;s/[[:blank:]]*$//'`

}
#
#==========================================================
#
function createConfiguration {
    URL=$HOST:8000/$CLOUD
    curl --silent --output configure.sh.sig $URL/configure.sh.sig
    curl --silent --output chain.pem $URL/chain.pem
    curl --silent --output devcert.pem $URL/devcert.pem
    curl --silent --output nodeconfig.txt $URL/nodeconfig.txt

    openssl verify -CAfile chain.pem devcert.pem || exit 1
    openssl smime -verify -in configure.sh.sig -certfile devcert.pem -CAfile chain.pem -out configure.sh || exit 1
    chmod +x configure.sh

    . configure.sh BATCH
    LBAC_IMAGE_REGISTRY=CRIMSYREG_URL
    makeTempConfig

    # dialog_DATASTORE
    echo "LBAC_DATASTORE=\"$LBAC_DATASTORE\"" > $HOME/.lbac 
    echo "LBAC_DATASTORE=\"$LBAC_DATASTORE\"" >> $TMP_CONFIG 
    makeDirectories

    # always a fresh object id!
    LBAC_NODE_ID=`uuidgen -r | tr -d $'\n'`
    echo "LBAC_NODE_ID=\"$LBAC_NODE_ID\"" >> $TMP_CONFIG

    echo "LBAC_PROXY_HSTS=\"OFF\"" >> $TMP_CONFIG
    echo "LBAC_UPDATE_LEVEL=\"PATCH\"" >> $TMP_CONFIG
    echo "LBAC_INIT_TYPE=\"SYSTEMD\"" >> $TMP_CONFIG
    echo "LBAC_DOCKER_EXCLUSIVE=\"OFF\"" >> $TMP_CONFIG

    # set default admin password (not for production use!)
    echo -n "admin" > "$LBAC_DATASTORE/etc/$LBAC_ADMIN_PWFILE"

    getTestData
    LBAC_INTRANET_FQHN=`hostname -f`
    LBAC_INTERNET_FQHN=$LBAC_INTRANET_FQHN

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

case $1 in 
    CONFIG)
        createConfiguration
        exit 0
        ;;
    CLEANUP)
        echo "cleaning up everything ..."
        sudo ./dist/bin/setupROOT.sh totalClean
    ;;
esac
