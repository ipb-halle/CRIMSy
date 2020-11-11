#!/bin/bash
#
# Cloud Resource & Information Management System (CRIMSy)
# Integration Test Setup Script
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
#==========================================================
#
function buildDistServer {
    pushd docker/crimsyci > /dev/null
    docker inspect crimsyci >/dev/null 2>/dev/null || docker build -f Dockerfile -t crimsyci .
    popd > /dev/null
}

# DANGER: remove installation without (almost) any trace
function cleanup {
    dst=`echo $0 | cut -d' ' -f2`
    ssh -o "StrictHostKeyChecking no" $dst "./configBatch.sh CLEANUP"
}
export -f cleanup

function createNodeConfig {
    key=`echo $0 | cut -d' ' -f1`
    dst=`echo $0 | cut -d' ' -f2`
    cloud=`grep $key "$LBAC_REPO/util/test/etc/nodeconfig.txt" | \
        cut -c9-18 | \
        sed -e 's/^[[:blank:]]*//;s/[[:blank:]]*$//'`
    url="http://`hostname -f`:8000/$cloud"

    echo "copying configBatch.sh script ..."
    scp -q -o "StrictHostKeyChecking no" "$LBAC_REPO/util/bin/configBatch.sh" $dst:

    echo "executing configBatch.sh ..."
    ssh -o "StrictHostKeyChecking no" $dst "chmod +x configBatch.sh && ./configBatch.sh CONFIG $url $key"

    echo "fetching node configuration ..."
    scp -q -o "StrictHostKeyChecking no" $dst:etc/$cloud/config.sh.asc "$LBAC_REPO/config/nodes/$key.sh.asc"
}
export -f createNodeConfig

function error {
    echo $1
    exit 1
}

function installNode {
    dst=`echo $0 | cut -d' ' -f2`
    ssh -o "StrictHostKeyChecking no" $dst "./bin/install.sh"
}
export -f installNode

function runDistServer {
    cp docker/crimsyci/index.html target/integration/htdocs
    (docker inspect crimsyci_service | grep Status | grep -q running ) && docker stop crimsyci_service
    docker inspect crimsyci_service >/dev/null 2>&1 && docker rm crimsyci_service 
    docker run -p 8000:80 \
        --mount type=bind,src=`realpath target/integration/htdocs`,dst=/usr/local/apache2/htdocs \
        --hostname `hostname -f` \
        --detach --name crimsyci_service \
        crimsyci
}

function safetyCheck {
    if [ -d config ] ; then
        if [ ! -f config/INTEGRATION_TEST ] ; then
            cat <<EOF
*****************************************************************
*                                                               *
*                         ERROR!                                *
*                                                               *
* This tree already contains a config directory, which is not   *
* specially marked for integration testing. We refuse to        *
* continue as this would damage valuable data (certificates,    *
* keys, ...).                                                   *
*                                                               *
*****************************************************************
EOF
            exit 1

        fi
    fi
    mkdir -p config/logs
    cat > config/INTEGRATION_TEST << EOF
*****************************************************************
*                                                               *
*                       WARNING!                                *
*                                                               *
* This file enables batch mode for the camgr.sh script, which   * 
* can result destruction of configuration data without further  *
* questions asked. Do not use configuration data stored in this *
* directory for production purposes.                            *
*                                                               *
*****************************************************************
EOF
}

function setupTestRootCA {
    $LBAC_REPO/util/bin/camgr.sh --batch --mode ca
    chmod -R go+rX $LBAC_REPO/target/integration/htdocs
}

function setupTestSubCA {
    cloud=`echo $0 | cut -d: -f1`

    $LBAC_REPO/util/bin/camgr.sh --batch --mode ca --cloud $cloud

    $LBAC_REPO/util/bin/camgr.sh --batch --mode sign --extension v3_subCA \
        --input $LBAC_REPO/config/$cloud/CA/cacert.req \
        --output $LBAC_REPO/config/$cloud/CA/cacert.pem

    $LBAC_REPO/util/bin/camgr.sh --batch --mode importSubCA --cloud $cloud

    $LBAC_REPO/util/bin/camgr.sh --batch --mode devcert --cloud $cloud

    LBAC_CA_DIR=$LBAC_REPO/config/$cloud/CA
    . $LBAC_CA_DIR/cloud.cfg

    sed -e "s,CLOUDCONFIG_DOWNLOAD_URL,$DOWNLOAD_URL," $LBAC_REPO/util/bin/configure.sh | \
    sed -e "s,CLOUDCONFIG_CLOUD_NAME,$CLOUD," |\
    openssl smime -sign -signer $LBAC_CA_DIR/$DEV_CERT.pem \
      -md sha256 -binary -out $LBAC_REPO/target/integration/htdocs/$cloud/configure.sh.sig \
      -stream -nodetach \
      -inkey $LBAC_CA_DIR/$DEV_CERT.key \
      -passin file:$LBAC_CA_DIR/$DEV_CERT.passwd 

    cp $LBAC_CA_DIR/$DEV_CERT.pem $LBAC_REPO/target/integration/htdocs/$cloud/devcert.pem

    chmod -R go+rX $LBAC_REPO/target/integration/htdocs
}
export -f setupTestSubCA

function setupTestCAconf {
    dist=`echo $0 | cut -d: -f1`
    superior=`echo $0 | cut -d: -f2`
    cloud=`echo $0 | cut -d: -f3`
    name=`echo $0 | cut -d: -f4`

    mkdir -p "$LBAC_REPO/config/$cloud/CA"
    mkdir -p "$LBAC_REPO/target/integration/htdocs/$dist"
    cp "$LBAC_REPO/util/test/etc/nodeconfig.txt" "$LBAC_REPO/target/integration/htdocs/$dist"

    cat > "$LBAC_REPO/config/$cloud/CA/cloud.cfg" <<EOF
#
# Integration Test CA config
# Tue Dec 10 17:05:31 CET 2019
#
CA_COUNTRY="XX"
CA_STATE="Some State"
CA_PLACE="Somecity"
CA_ORG="SomeOrg"
CA_OU="Integration Test Dept."
CA_EMAIL="admin@somewhere.invalid"
CA_CN="$name CA"
CA_CRL="http://`hostname -f`:8000/$dist"
DEV_COUNTRY="DE"
DEV_STATE="State"
DEV_PLACE="Location"
DEV_ORG="Organization"
DEV_OU="OU"
DEV_EMAIL="Email"
DEV_CN="Developer Name"
DEV_CERT=""
DOWNLOAD_URL="http://`hostname -f`:8000/$dist"
SUPERIOR_URL="http://`hostname -f`:8000/$superior"
SCP_ADDR="$LBAC_REPO/target/integration/htdocs/$dist"
CLOUD="$cloud"
CLOUD_NAME="$name"
EOF

}
export -f setupTestCAconf

#
#==========================================================
#
function setup {
    cat $LBAC_REPO/util/test/etc/cloudconfig.txt | \
        xargs -l1 -i /bin/bash -c setupTestCAconf "{}"

    echo "=== Distribution Server ==="
    buildDistServer
    runDistServer

    echo "=== Setup ROOT CA ==="
    setupTestRootCA

    echo "=== Setup Sub CAs ==="
    tail -n +2 $LBAC_REPO/util/test/etc/cloudconfig.txt | \
        xargs -l1 -i /bin/bash -c setupTestSubCA "{}"

    echo "=== create node configurations ==="
    cat $HOSTLIST | xargs -l1 -i /bin/bash -c createNodeConfig "{}"

    # package master nodes
    echo "=== determine master nodes ==="
    tail -n +2 $LBAC_REPO/util/test/etc/cloudconfig.txt | \
        cut -d: -f1 | \
        xargs -l1 -i $LBAC_REPO/util/bin/package.sh "{}" MASTERBATCH

    # package all other nodes
    echo "=== package all nodes ==="
    tail -n +2 $LBAC_REPO/util/test/etc/cloudconfig.txt | \
        cut -d: -f1 | \
        xargs -l1 -i $LBAC_REPO/util/bin/package.sh "{}" AUTOBATCH

    # install node
    echo "=== install nodes ==="
    cat $HOSTLIST | xargs -l1 -i /bin/bash -c installNode "{}"

    #
    # ToDo: multiple cloud memberships 
    #

    #
    echo "sleep 15 seconds to settle everything ..."
    sleep 15

}
#
#==========================================================
#
function preprocess {
        SRC=$0
        DESTDIR=`dirname $SRC`
        DESTFILE=`basename $SRC ".m4"`
        pushd $LBAC_REPO >/dev/null
        m4 $SRC > $DESTDIR/$DESTFILE
        rm $SRC
        popd > /dev/null

}
export -f preprocess
#
#==========================================================
#
function runTests {
    # check prerequisites
    echo "checking prerequsites"
    (docker inspect dist_proxy_1 2>/dev/null | grep -q running ) \
        || error "Service seems unavailable ..."

    # build test containers and set up environment
    echo "checking / building test environment"
    pushd $LBAC_REPO/util/test >/dev/null
    docker inspect --type image cypress >/dev/null 2>/dev/null || docker build -f Dockerfile -t cypress .

    sudo rm -r $LBAC_REPO/target/cypress
    mkdir -p $LBAC_REPO/target/cypress
    cp -r cypress $LBAC_REPO/target/cypress/
    cat <<EOF >$LBAC_REPO/target/cypress/config_m4.inc
dnl
dnl Cypress test fixtures configuration
dnl
define(\`TESTBASE_HOSTNAME',\``head -1 $HOSTLIST | cut -d' ' -f2`')dnl
EOF
    find $LBAC_REPO/target/cypress -type f -name "*.m4" -exec /bin/bash -c preprocess {} \;

    # run tests ...
    echo "running tests"
    docker run -v $LBAC_REPO/target/cypress/cypress:/app/cypress --name cy1 cypress --browser firefox --headless

    # clean up
    echo "removing test container"
    docker rm cy1
    popd >/dev/null
}
#
#==========================================================
#
function tearDown {

    # tear down nodes
    cat $HOSTLIST | xargs -l1 -i /bin/bash -c cleanup "{}"

    # remove target directory (needs root privilege)
    sudo rm -r config/ target/

}
#
#==========================================================
#
function mainFunc {

    for action  in $* ; do
        case $action in
            setup)
                mvn --batch-mode -DskipTests clean install
                setup
                ;;

            test)
                runTests
                ;;

            teardown)
                tearDown
                ;;

            *)
                echo "WARNING: Ignoring unrecognized target."
                ;;
        esac
    done
}
#
#==========================================================
#
p=`dirname $0`
export LBAC_REPO=`realpath "$p/../.."`
umask 0022

if [ $# -lt 2 ] ; then
    echo "usage: `basename $0` HOSTLIST TARGET [TARGET ...]"
    echo
    echo "HOSTLIST is a file containing one nodekey hostname pair per line"
    echo
    echo "TARGET must be one of 'setup', 'test' and 'teardown'"
    exit 1
fi
HOSTLIST=`realpath $1`
shift

TEST_DATE=`date +%Y%m%d%H%M`

cd $LBAC_REPO
safetyCheck

mainFunc $* 2>&1 | tee $LBAC_REPO/config/logs/test.$TEST_DATE.log

echo
echo "******************************************************************"
echo "*                                                                *"
echo "* FINISHED!                                                      *"
echo "* Please find your log file in config/logs/test.$TEST_DATE.log *"
echo "* and test outcomes in directory target/cypress/                 *"
echo "*                                                                *"
echo "******************************************************************"

