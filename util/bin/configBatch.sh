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
# which is necessary for integration testing (Selenium).
# The script operates the original configure.sh script for 
# the current (PRIMARY) cloud.
#

URL=$2
TEST_ID=$3
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
    LBAC_MANAGER_EMAIL=`echo "$data" | cut -c70-105 | sed -e 's/^[[:blank:]]*//;s/[[:blank:]]*$//'`

}
#
#==========================================================
#
function createConfiguration {
    curl --output configure.sh.sig $URL/configure.sh.sig
    curl --output chain.txt $URL/chain.txt
    curl --output devcert.pem $URL/devcert.pem
    curl --output nodeconfig.txt $URL/nodeconfig.txt

    openssl verify -CAfile chain.txt devcert.pem || exit 1
    openssl smime -verify -in configure.sh.sig -certfile devcert.pem -CAfile chain.txt -out configure.sh || exit 1
    chmod +x configure.sh

    . configure.sh BATCH
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

    # NOTE: docker exclusive might collide with Selenium Driver?
    echo "LBAC_DOCKER_EXCLUSIVE=\"ON\"" >> $TMP_CONFIG

    getTestData
    LBAC_INTRANET_FQHN=`hostname -f`
    LBAC_INTERNET_FQHN=$LBAC_INTRANET_FQHN

    echo "LBAC_INSTITUTION=\"$LBAC_INSTITUTION\"" >> $TMP_CONFIG
    echo "LBAC_INSTITUTION_SHORT=\"$LBAC_INSTITUTION_SHORT\"" >> $TMP_CONFIG
    echo "LBAC_MANAGER_EMAIL=\"$LBAC_MANAGER_EMAIL\"" >> $TMP_CONFIG
    echo "LBAC_INTRANET_FQHN=\"$LBAC_INTRANET_FQHN\"" >> $TMP_CONFIG
    echo "LBAC_INTERNET_FQHN=\"$LBAC_INTRANET_FQHN\"" >> $TMP_CONFIG

    LBAC_COUNTRY="XX"
    LBAC_STATE="Some State"
    LBAC_SSL_ORGANIZATION="$LBAC_INSTITUTION"
    LBAC_SSL_OU="Integration Testing Dept."
    LBAC_SSL_EMAIL="$LBAC_MANAGER_EMAIL"

    makeCertReq

    # dialog_SAVE
    mv $TMP_CONFIG $LBAC_DATASTORE/$LBAC_CONFIG
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
    INSTALL)
        echo "work in progress ..."
        exit 0
        ;;
esac
