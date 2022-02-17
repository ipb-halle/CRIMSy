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

#
#
# DANGER: remove installation without (almost) any trace
function cleanup {
    node=`echo $1 | cut -d' ' -f1`
    remote=`echo $1 | cut -d' ' -f2`
    login=`echo $1 | cut -d' ' -f3`
    echo "performing teardown at $login@remote ($node) ..."
    ssh -o "StrictHostKeyChecking no" $login@$remote "./configBatch.sh CLEANUP"
}

#
#
#
function compile {

    mvn --batch-mode -DskipTests clean install

    pushd $LBAC_REPO/ui >/dev/null
    REVISION=`mvn org.apache.maven.plugins:maven-help-plugin:evaluate -Dexpression=project.version -q -DforceStdout`
    MAJOR=`echo $REVISION | cut -d. -f1`
    MINOR=`echo $REVISION | cut -d. -f2`
    RELEASE="$MAJOR.$MINOR"
    popd >/dev/null
}

#
#
#
function copyNodeConfig {
    cloud=`echo $1 | cut -d' ' -f1`
    node=`echo $1 | cut -d' ' -f2`
    cp $LBAC_REPO/config/nodes/${node}_${cloud}.sh.asc $LBAC_REPO/config/$cloud/
}

#
#
#
function createNodeConfig {
    node=`echo $1 | cut -d' ' -f1`
    remote=`echo $1 | cut -d' ' -f2`
    login=`echo $1 | cut -d' ' -f3`
    cloud=`grep $node "$LBAC_REPO/util/test/etc/nodeconfig.txt" | \
        cut -c9-18 | \
        sed -e 's/^[[:blank:]]*//;s/[[:blank:]]*$//'`
    crimsyhost=`hostname -f`

    echo "executing createNodeConfig for host $login@$remote ($node) ..."

    echo "copying configBatch.sh script ..."
    scp -q -o "StrictHostKeyChecking no" "$LBAC_REPO/util/bin/configBatch.sh" $login@$remote:

    echo "executing configBatch.sh ..."
    ssh -o "StrictHostKeyChecking no" $login@$remote "chmod +x configBatch.sh && ./configBatch.sh CONFIG $crimsyhost $cloud $node"

    echo "fetching node configuration ..."
    scp -q -o "StrictHostKeyChecking no" $login@$remote:etc/$cloud/config.sh.asc "$LBAC_REPO/config/nodes/${node}_${cloud}.sh.asc"
}

#
# preprocess Cypress test cases
#
function cypressPreprocess {
        SRC=$0
        DESTDIR=`dirname $SRC`
        DESTFILE=`basename $SRC ".m4"`
        pushd $LBAC_REPO >/dev/null
        m4 $SRC > $DESTDIR/$DESTFILE
        rm $SRC
        popd > /dev/null

}
export -f cypressPreprocess

#
#
#
function error {
    echo $1
    exit 1
}

#
#
#
function installNode {
    remote=`echo $1 | cut -d' ' -f2`
    login=`echo $1 | cut -d' ' -f3`
    echo "installNode called for host: $login@$remote"
    ssh -o "StrictHostKeyChecking no" $login@$remote "./bin/install.sh"
}

#
#
#
function restore {
    if [ $NODE = "all" ] ; then
        echo "Restoring snapshot '$RESTORE' on all nodes"
        cat $HOSTLIST | \
            while read record ; do
                remote=`echo $record | cut -d' ' -f2`
                login=`echo $record | cut -d' ' -f3`
                ssh -o "StrictHostKeyChecking no" "$login@$remote" ./dist/bin/setupROOT.sh restore $RESTORE
            done
    else 
        echo "Restoring snapshot '$RESTORE' on node '$RESTORE'"
        grep $NODE $HOSTLIST | \
            while read record ; do
                remote=`echo $record | cut -d' ' -f2`
                login=`echo $record | cut -d' ' -f3`
                ssh -o "StrictHostKeyChecking no" "$login@$remote" ./dist/bin/setupROOT.sh restore $RESTORE
            done
    fi
}

#
#
#
function runDistServer {
    echo "Setting up distribution servers"
    cp docker/crimsyci/index.html target/integration/htdocs
    (docker inspect crimsyci_service | grep Status | grep -q running ) && docker stop crimsyci_service
    docker inspect crimsyci_service >/dev/null 2>&1 && docker rm crimsyci_service 
    docker run -p 8000:80 \
        --mount type=bind,src=`realpath target/integration/htdocs`,dst=/usr/local/apache2/htdocs \
        --hostname `hostname -f` \
        --detach --name crimsyci_service \
        crimsyci

    (docker inspect crimsyreg_service | grep Status | grep -q running ) && docker stop crimsyreg_service
    docker inspect crimsyreg_service >/dev/null 2>&1 && docker rm crimsyreg_service
    docker run -p 5000:5000 \
        --hostname `hostname -f` \
        --detach --name crimsyreg_service \
        registry
}

#
# run Tests. 
# NOTE: currently only runs on first node in HOSTLIST
#
function runTests {
    # check prerequisites
    echo "checking prerequsites"
    (docker inspect dist_proxy_1 2>/dev/null | grep -q running ) \
        || error "Service seems unavailable ..."

    # initialize database with test data
    HOST=`head -1 $HOSTLIST | cut -d' ' -f2`
    docker cp $LBAC_REPO/util/test/etc/initial_data.sql dist_db_1:/tmp/
    wget -o /dev/null -O /dev/null --no-check-certificate https://$HOST/ui/index.xhtml
    echo "waiting 3 sec. for webapp to initialize database ..."
    sleep 3
    docker exec -i -u postgres dist_db_1 psql -Ulbac lbac -f /tmp/initial_data.sql

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
define(\`TESTBASE_HOSTNAME',\`$HOST')dnl
EOF
    find $LBAC_REPO/target/cypress -type f -name "*.m4" -exec /bin/bash -c cypressPreprocess {} \;

    # run tests ...
    echo "running tests"
    docker run -v $LBAC_REPO/target/cypress/cypress:/app/cypress --name cy1 cypress --browser firefox --headless

    # clean up
    echo "removing test container"
    docker rm cy1
    popd >/dev/null

    echo "*****************************************************************"
    echo "*                                                               *"
    echo "* TESTS FINISHED                                                *"
    echo "* Please find the log file in config/logs/test.$TEST_DATE.log *"
    echo "* and test outcomes in directory target/cypress/                *"
    echo "*                                                               *"
    echo "*****************************************************************"
}

#
#
#
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

#
#
#
function setupFunc {
    cat $LBAC_REPO/util/test/etc/cloudconfig.txt | \
        while read record ; do
            setupTestCAconf "$record"
        done

    echo "=== Distribution Server ==="
    buildDistServer
    runDistServer

    echo "=== Build Docker Images ==="
    $LBAC_REPO/util/bin/buildDocker.sh `hostname -f`:5000

    echo "=== Setup ROOT CA ==="
    setupTestRootCA

    echo "=== Setup Sub CAs ==="
    tail -n +2 $LBAC_REPO/util/test/etc/cloudconfig.txt | \
        while read record ; do
            setupTestSubCA "$record"
            setupConfigure "$record"
        done

    echo "=== create node configurations ==="
    cat $HOSTLIST | while read record ; do
        echo | createNodeConfig "$record"
    done

    # package master nodes
    echo "=== determine master nodes ==="
    tail -n +2 $LBAC_REPO/util/test/etc/cloudconfig.txt | \
        cut -d: -f1 | \
        while read record ; do
            $LBAC_REPO/util/bin/package.sh "$record" MASTERBATCH
        done

    # package all other nodes
    echo "=== package all nodes ==="
    cat $LBAC_REPO/util/test/etc/cloudnodes.txt |\
        while read record ; do
            copyNodeConfig "$record"
        done
    tail -n +2 $LBAC_REPO/util/test/etc/cloudconfig.txt | \
        cut -d: -f1 | \
        while read record ; do
            $LBAC_REPO/util/bin/package.sh "$record" AUTOBATCH
        done

    # install node
    echo "=== install nodes ==="
    cat $HOSTLIST | while read record ; do
        echo | installNode "$record"
    done

    #
    # ToDo: multiple cloud memberships 
    #

    #
    echo "sleep 15 seconds to settle everything ..."
    sleep 15

    echo "*****************************************************************"
    echo "*                                                               *"
    echo "* SETUP FINISHED                                                *"
    echo "* Please find the log file in config/logs/test.$TEST_DATE.log *"
    echo "*                                                               *"
    echo "*****************************************************************"
}

#
#
#
function setupTestRootCA {
    $LBAC_REPO/util/bin/camgr.sh --batch --mode ca
    chmod -R go+rX $LBAC_REPO/target/integration/htdocs
}

#
#
#
function setupTestSubCA {
    cloud=`echo $1 | cut -d: -f1`

    $LBAC_REPO/util/bin/camgr.sh --batch --mode ca --cloud $cloud

    $LBAC_REPO/util/bin/camgr.sh --batch --mode sign --extension v3_subCA \
        --input $LBAC_REPO/config/$cloud/CA/cacert.req \
        --output $LBAC_REPO/config/$cloud/CA/cacert.pem

    $LBAC_REPO/util/bin/camgr.sh --batch --mode importSubCA --cloud $cloud

    $LBAC_REPO/util/bin/camgr.sh --batch --mode devcert --cloud $cloud

    LBAC_CA_DIR=$LBAC_REPO/config/$cloud/CA
    . $LBAC_CA_DIR/cloud.cfg

    cp $LBAC_CA_DIR/$DEV_CERT.pem $LBAC_REPO/target/integration/htdocs/$cloud/devcert.pem
}

function setupConfigure {
    cloud=`echo $1 | cut -d: -f1`
    LBAC_CA_DIR=$LBAC_REPO/config/$cloud/CA
    . $LBAC_CA_DIR/cloud.cfg

    sed -e "s,CLOUDCONFIG_DOWNLOAD_URL,$DOWNLOAD_URL," $LBAC_REPO/util/bin/configure.sh | \
    sed -e "s,CLOUDCONFIG_CURRENT_RELEASE,$RELEASE," |\
    sed -e "s,CLOUDCONFIG_CLOUD_NAME,$CLOUD," |\
    openssl smime -sign -signer $LBAC_CA_DIR/$DEV_CERT.pem \
      -md sha256 -binary -out $LBAC_REPO/target/integration/htdocs/$cloud/configure.sh.sig \
      -stream -nodetach \
      -inkey $LBAC_CA_DIR/$DEV_CERT.key \
      -passin file:$LBAC_CA_DIR/$DEV_CERT.passwd 

    chmod -R go+rX $LBAC_REPO/target/integration/htdocs
}

#
#
#
function setupTestCAconf {
    dist=`echo $1 | cut -d: -f1`
    superior=`echo $1 | cut -d: -f2`
    cloud=`echo $1 | cut -d: -f3`
    name=`echo $1 | cut -d: -f4`

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

#
# 
#
function snapshot {
    if [ $NODE = "all" ] ; then
        echo "Taking snapshot '$SNAPSHOT' of all nodes"
        cat $HOSTLIST | \
            while read record ; do
                remote=`echo $record | cut -d' ' -f2`
                login=`echo $record | cut -d' ' -f3`
                ssh -o "StrictHostKeyChecking no" "$login@$remote" ./dist/bin/setupROOT.sh snapshot $SNAPSHOT
            done
    else
        echo "Taking snapshot '$SNAPSHOT' for node '$NODE'"
        grep $NODE $HOSTLIST | \
            while read record ; do
                remote=`echo $record | cut -d' ' -f2`
                login=`echo $record | cut -d' ' -f3`
                ssh -o "StrictHostKeyChecking no" "$login@$remote" ./dist/bin/setupROOT.sh snapshot $SNAPSHOT
            done
    fi
}

#
#
#
function teardown {

    # tear down nodes
    cat $HOSTLIST | while read record ; do
        echo | cleanup "$record"
    done

    # remove target directory (may need root privilege?)
    rm -rf config/ target/

    docker stop crimsyci_service
    docker stop crimsyreg_service
    docker container prune -f

    echo "*****************************************************************"
    echo "*                                                               *"
    echo "* TEARDOWN FINISHED                                             *"
    echo "* Removed everything including logs.                            *"
    echo "*                                                               *"
    echo "*****************************************************************"
}
#
#==========================================================
#
function printHelp {
BOLD=$'\x1b[1m'
REGULAR=$'\x1b[0m'
cat <<EOF
${BOLD}NAME${REGULAR}
    testSetup.sh

${BOLD}SYNOPSIS${REGULAR}
    testSetup.sh [-H|hostlist HOSTLIST] [-h|--help] [-n|--node NODE] 
        [-p|--pause] [-R|--restore LABEL] [-r|--runTests] [-S|--snapshot LABEL] 
        [-s|--setup] [-t|--teardown] [-w|--wake]

${BOLD}DESCRIPTION${REGULAR}
    Set up, operate and clean up a test environment of CRIMSy. Setup includes 
    compilation, setup of a PKI and installation one or more nodes. 
    This script requires passwordless sudo and passwordless login on all 
    involved hosts.

${BOLD}OPTIONS${REGULAR}
-H|--hostlist HOSTLIST
    mapping of node names (node1, node2, ...) to real host names. Each line contains
    the node name followed by the host name and the user account separated by a single 
    space character.

-h|--help
    print this help text

-n|--node NODE
    operate on node NODE only (default all)

-p|--pause
    pause execution of node NODE

-R|--restore LABEL
    restore the snapshot with label LABEL

-r|--runTests
    run the Cypress integration tests

-S|--snapshot LABEL
    create a labelled snapshot of the database and the file storage 

-s|--setup
    do the full setup starting with compilation

-t|--teardown
    remove all nodes and clean up everything

-w|--wake
    resume execution of node NODE
EOF

}

function mainFunc {
    if [ -z "$HOSTLIST" ] ; then
        error "Must provide HOSTLIST. Call testSetup.sh -h for help."
    fi

    if [ -n "$PAUSE" ] ; then
        echo "Pause not implemented"
        exit 0
    fi

    if [ -n "$WAKE" ] ; then
        echo "Wake not implemented"
        exit 0
    fi

    if [ -n "$SNAPSHOT" ] ; then
        snapshot
        exit 0
    fi

    if [ -n "$RESTORE" ] ; then
        restore
        exit 0
    fi

    if [ -n "$SETUP" ] ; then
        echo "setup"
        compile
        setupFunc
    fi

    if [ -n "$RUNTESTS" ] ; then
        echo "run tests"
        runTests
    fi

    if [ -n "$TEARDOWN" ] ; then
        echo "tear down"
        teardown
    fi
}
#
#==========================================================
#
p=`dirname $0`
export LBAC_REPO=`realpath "$p/../.."`
umask 0022
cd $LBAC_REPO
safetyCheck
TEST_DATE=`date +%Y%m%d%H%M`

HOSTLIST=''
NODE=all
PAUSE=''
RESTORE=''
RUNTESTS=''
SNAPSHOT=''
SETUP=''
TEARDOWN=''
WAKE=''

GETOPT=$(getopt -o 'H:hn:pR:rS:stw' --longoptions 'hostlist:,help,node:,pause,restore:,runTests:,snapshot:,setup,teardown:,wake' -n 'testSetup.sh' -- "$@")

if [ $? -ne 0 ]; then
        echo 'Error in commandline evaluation. Terminating...' >&2
        exit 1
fi

eval set -- "$GETOPT"
unset GETOPT

while true ; do
    case "$1" in
    '-H'|'--hostlist')
        HOSTLIST=`realpath "$2"`
        shift 2
        continue
        ;;
    '-h'|'--help')
        printHelp
        exit 0
        ;;
    '-n'|'--node')
        NODE=$2
        shift 2
        continue
        ;;
    '-p'|'--pause')
        PAUSE='pause'
        shift
        continue
        ;;
    '-R'|'--restore')
        RESTORE=$2
        shift 2
        continue
        ;;
    '-r'|'--runTests')
        RUNTESTS='runTests'
        shift
        continue
        ;;
    '-S'|'--snapshot')
        SNAPSHOT=$2
        shift 2
        continue
        ;;
    '-s'|'--setup')
        SETUP='setup'
        shift
        continue
        ;;
    '-t'|'--teardown')
        TEARDOWN='teardown'
        shift
        continue
        ;;
    '-w'|'--wake')
        WAKE='wake'
        shift
        continue
        ;;
    '--')
        shift
        break
        ;;
    *)
        echo 'Internal error!' >&2
        exit 1
        ;;
    esac
done


mainFunc 2>&1 | tee $LBAC_REPO/config/logs/test.$TEST_DATE.log

