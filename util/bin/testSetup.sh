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
    node=`echo $1 | cut -d';' -f1`
    remote=`echo $1 | cut -d';' -f2`
    login=`echo $1 | cut -d';' -f3`
    echo "performing teardown at $login@remote ($node) ..."
    ssh -o "StrictHostKeyChecking no" $login@$remote "./configBatch.sh CLEANUP"
}

#
#
#
function compile {
    stage=$1
    echo "=== Build Docker Images ==="
    if [ -n "$stage" ] ; then
        $LBAC_REPO/util/bin/buildDocker.sh \
            --branch-file "$BRANCH_FILE" \
            --stage-label $stage \
            --registry `hostname -f`:5000
    else 
        echo "No stage selected, calling buildDocker.sh directly"
        $LBAC_REPO/util/bin/buildDocker.sh --registry `hostname -f`:5000
    fi
}

#
#
#
function copyNodeConfig {
    cloud=`echo $1 | cut -d';' -f1`
    node=`echo $1 | cut -d';' -f2`
    cp $LBAC_REPO/config/nodes/${node}_${cloud}.sh.asc $LBAC_REPO/config/$cloud/
}

#
#
#
function createNodeConfig {
    node=`echo $1 | cut -d';' -f1`
    remote=`echo $1 | cut -d';' -f2`
    login=`echo $1 | cut -d';' -f3`
    cloud=`grep -E "^$node;" "$LBAC_REPO/util/test/etc/nodeconfig.cfg" |\
        grep PRIMARY_CLOUD |\
        cut -d= -f2`
    crimsyhost=`hostname -f`

    echo "executing createNodeConfig for host $login@$remote ($node) ..."

    echo "copying node configuration ..."
    grep -E "^$node;" "$LBAC_REPO/util/test/etc/nodeconfig.cfg" |\
    cut -d';' -f2- |\
    ssh -o "StrictHostKeyChecking no" $login@$remote bash -c "cat - > nodeconfig.cfg"

    echo "copying configBatch.sh script ..."
    scp -q -o "StrictHostKeyChecking no" "$LBAC_REPO/util/bin/configBatch.sh" $login@$remote:

    echo "executing configBatch.sh ..."
    ssh -o "StrictHostKeyChecking no" $login@$remote "chmod +x configBatch.sh && ./configBatch.sh CONFIG $crimsyhost"

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
function infoLog {
    msg=`echo "$1 FINISHED                                                        -" | cut -c-61`
    echo "*****************************************************************"
    echo "*                                                               *"
    echo "* $msg *"
    echo "* Please find the log file in config/logs/test.$TEST_DATE.log *"
    echo "*                                                               *"
    echo "*****************************************************************"
}

#
#
#
function installFunc {
    # install node
    echo "=== install nodes ==="
    grep -vE "^#" $HOSTLIST |\
    if [ $NODE = "all" ] ; then cat ; else grep $NODE ; fi |\
    while read record ; do
        echo | installNode "$record"
    done

}

#
#
#
function installNode {
    remote=`echo $1 | cut -d';' -f2`
    login=`echo $1 | cut -d';' -f3`
    echo "installNode called for host: $login@$remote"
    ssh -o "StrictHostKeyChecking no" $login@$remote "./bin/install.sh"
}

#
#
#
function restore {
    if [ $NODE = "all" ] ; then
        echo "Restoring snapshot '$RESTORE' on all nodes"
    else
        echo "Restoring snapshot '$RESTORE' on node '$NODE'"
    fi

    grep -vE "^#" $HOSTLIST |\
    if [ $NODE = "all" ] ; then cat ; else grep $NODE ; fi |\
    while read record ; do
        remote=`echo $record | cut -d';' -f2`
        login=`echo $record | cut -d';' -f3`
        ssh -o "StrictHostKeyChecking no" "$login@$remote" sudo ./dist/bin/update.sh restore $RESTORE
    done
}

#
# run Cypress tests. 
# NOTE: tests are currently available for node1 only 
#
function runCypress {
    # check prerequisites
    echo "checking test prerequsites ..."
    grep -vE "^#" $HOSTLIST |\
    if [ $NODE = "all" ] ; then cat ; else grep $NODE ; fi |\
    while read record ; do
        node=`echo $record | cut -d';' -f1`
        remote=`echo $record | cut -d';' -f2`
        login=`echo $record | cut -d';' -f3`
        echo | ssh -o "StrictHostKeyChecking no" "$login@$remote" \
            bash -c "docker inspect dist_proxy_1 2>/dev/null | grep -q running" \
            || error "Service seems unavailable at node $node"

    done

    # initialize database with test data
    echo "loading initial data ..."
    grep -vE "^#" $HOSTLIST |\
    if [ $NODE = "all" ] ; then cat ; else grep $NODE ; fi |\
    while read record ; do
        node=`echo $record | cut -d';' -f1`
        remote=`echo $record | cut -d';' -f2`
        login=`echo $record | cut -d';' -f3`
        if [ -s $LBAC_REPO/util/test/etc/$node.initial_data.sql ] ; then
            echo "processing node $node"
            echo | scp -o "StrictHostKeyChecking no" $LBAC_REPO/util/test/etc/$node.initial_data.sql "$login@$remote:tmp/initial_data.sql"
            echo | ssh -o "StrictHostKeyChecking no" "$login@$remote" ./configBatch LOAD_DATA
        fi
    done

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
define(\`TESTBASE_HOSTNAME',\`$remote')dnl
EOF
    find $LBAC_REPO/target/cypress -type f -name "*.m4" -exec /bin/bash -c cypressPreprocess {} \;

    # run tests ...
    echo "*"
    echo "* Running Tests"
    echo "* Test outcomes will be saved in target/cypress/"
    echo "*"
    docker run -v $LBAC_REPO/target/cypress/cypress:/app/cypress --name cy1 cypress --browser firefox --headless

    # clean up
    echo "removing test container"
    docker rm cy1
    popd >/dev/null
}

#
#
#
function runDistServer {
    echo "=== Distribution Servers ===" 
    buildDistServer

    mkdir -p "$LBAC_REPO/config/integration/htdocs"
    cp docker/crimsyci/index.html config/integration/htdocs
    (docker inspect crimsyci_service | grep Status | grep -q running ) && docker stop crimsyci_service
    docker inspect crimsyci_service >/dev/null 2>&1 && docker rm crimsyci_service 
    docker run -p 8000:80 \
        --mount type=bind,src=`realpath config/integration/htdocs`,dst=/usr/local/apache2/htdocs \
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
#
#
function runJobs {
    runDistServer
    grep -vE "^#" "$JOB_FILE" |\
    while read job; do
        echo "executing: $job"
        j=`echo "$job" | cut -d';' -f1`
        case $j in
            compile)
                stage=`echo "$job" | cut -d';' -f2`
                compile $stage
                ;;
            cypress)
                NODE=`echo "$job" | cut -d';' -f2`
                runCypress
                ;;
            install)
                NODE=`echo "$job" | cut -d';' -f2`
                installFunc
                echo "sleep 15 seconds to settle everything ..."
                sleep 15
                ;;
            join)
                NODE=`echo "$job" | cut -d';' -f2`
                CLOUD=`echo "$job" | cut -d';' -f3`
                runJoin
                ;;
            leave)
                NODE=`echo "$job" | cut -d';' -f2`
                CLOUD=`echo "$job" | cut -d';' -f3`
                runLeave
                ;;
            pause)
                PAUSE=`echo "$job" | cut -d';' -f2`
                runPause
                ;;
            restore)
                NODE=`echo "$job" | cut -d';' -f2`
                RESTORE=`echo "$job" | cut -d';' -f3`
                restore
                ;;
            sleep)
                sleep `echo "$job" | cut -d';' -f2`
                ;;
            setup)
                NODE="all"
                setupFunc
                ;;
            snapshot)
                NODE=`echo "$job" | cut -d';' -f2`
                SNAPSHOT=`echo "$job" | cut -d';' -f3`
                snapshot
                ;;
            teardown)
                NODE="all"
                teardown
                ;;
            test)
                SCRIPT=`echo "$job" | cut -d';' -f2`
                ARGS=`echo "$job" | cut -d';' -f3-`
                echo | util/test/bin/$SCRIPT $HOSTLIST "$ARGS"
                ;;
            update)
                NODE=`echo "$job" | cut -d';' -f2`
                UPDATE_CMD=`echo "$job" | cut -d';' -f3-`
                runUpdate
                echo "sleep 15 seconds to settle everything ..."
                sleep 15
                ;;
            wake)
                WAKE=`echo "$job" | cut -d';' -f2`
                runWake
                ;;
            *)
                echo "ignored: $j"
                ;;
        esac
    done
}

#
#
#
function runJoin {
    url="http://`hostname -f`:8000/$CLOUD"
    remote=`grep -vE "^#" $HOSTLIST | grep $NODE | cut -d';' -f2`
    login=`grep -vE "^#" $HOSTLIST | grep $NODE | cut -d';' -f3`

    echo | ssh -o "StrictHostKeyChecking no" "$login@$remote" \
        dist/bin/join.sh --request $CLOUD --url $url 

    scp -q -o "StrictHostKeyChecking no" $login@$remote:etc/$CLOUD/config.sh.asc "$LBAC_REPO/config/nodes/${NODE}_${CLOUD}.sh.asc"

    copyNodeConfig "$CLOUD;$NODE"
    $LBAC_REPO/util/bin/package.sh "$CLOUD" AUTOBATCH

    echo | ssh -o "StrictHostKeyChecking no" "$login@$remote" \
        dist/bin/join.sh --join $CLOUD
}

#
#
#
function runLeave {
    remote=`grep -vE "^#" $HOSTLIST | grep $NODE | cut -d';' -f2`
    login=`grep -vE "^#" $HOSTLIST | grep $NODE | cut -d';' -f3`

    echo | ssh -o "StrictHostKeyChecking no" "$login@$remote" \
        dist/bin/join.sh --leave $CLOUD 
}

#
#
#
function runPause {

    if [ $PAUSE = "all" ] ; then
        echo "Pausing ALL nodes" 
    else 
        echo "Pausing node '$PAUSE'"
    fi

    grep -vE "^#" $HOSTLIST |\
    if [ $PAUSE = all ] ; then cat ; else grep $PAUSE; fi |\
    while read record ; do
        remote=`echo $record | cut -d';' -f2`
        login=`echo $record | cut -d';' -f3`
        ssh -o "StrictHostKeyChecking no" "$login@$remote" sudo ./dist/bin/setupROOT.sh shutdown
    done
}

#
#
#
function runSetup {
    runDistServer
    rm -f config/revision_info.cfg

    if [ -n "$BRANCH_FILE" ] ; then
        initial_stage=`grep -vE "^#" "$BRANCH_FILE" | cut -d';' -f1 | sort | uniq | head -1`
        echo "Executing initial setup stage $initial_stage"
    else 
        echo "Executing direct setup"
        initial_stage=''
    fi
    compile $initial_stage
    setupFunc 
    installFunc

    # ToDo: multiple cloud memberships 

    echo "sleep 15 seconds to settle everything ..."
    sleep 15

    if [ -n "$BRANCH_FILE" ] ; then
        grep -vE "^#" "$BRANCH_FILE" | cut -d';' -f1 |\
        grep -v $initial_stage | sort | uniq |\
        while read record ; do
            echo "compiling for stage $record"
            compile $record
	    UPDATE_CMD=container
            runUpdate 
            echo "sleep 15 seconds to settle everything ..."
            sleep 15
        done
    fi
}


#
#
#
function runUpdate {
    echo "=== run update on nodes ==="
    grep -vE "^#" $HOSTLIST |\
    if [ $NODE = "all" ] ; then cat ; else grep $NODE ; fi |\
    while read record ; do
        node=`echo $record | cut -d';' -f1`
        remote=`echo $record | cut -d';' -f2`
        login=`echo $record | cut -d';' -f3`

	for cmd in `echo $UPDATE_CMD | tr ';' ' '` ; done
	        echo "updating node $node with $cmd"
	        echo | ssh -o "StrictHostKeyChecking no" "$login@$remote" sudo ./dist/bin/update.sh $cmd
	done
    done
}

#
#
#
function runWake {
    if [ $WAKE = "all" ] ; then
        echo "Waking ALL nodes" 
    else
        echo "Waking node '$WAKE'"
    fi

    grep -vE "^#" $HOSTLIST |\
    if [ $WAKE = all ] ; then cat ; else grep $WAKE ; fi |\
    while read record ; do
        remote=`echo $record | cut -d';' -f2`
        login=`echo $record | cut -d';' -f3`
        ssh -o "StrictHostKeyChecking no" "$login@$remote" sudo ./dist/bin/setupROOT.sh start
    done
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
    grep -vE "^#" $LBAC_REPO/util/test/etc/cloudconfig.cfg |\
    while read record ; do
        setupTestCAconf "$record"
    done

    echo "=== Setup ROOT CA ==="
    setupTestRootCA

    echo "=== Setup Sub CAs ==="
    grep -vE "^#" $LBAC_REPO/util/test/etc/cloudconfig.cfg |\
    cut -d';' -f3 |\
    while read cloud ; do
        if [ -n "$cloud" ] ; then
            setupTestSubCA
            setupConfigure
        fi
    done

    echo "=== create node configurations ==="
    grep -vE "^#" $HOSTLIST |\
    if [ $NODE = "all" ] ; then cat ; else grep $NODE ; fi |\
    while read record ; do
        echo | createNodeConfig "$record"
    done

    # package master nodes
    echo "=== determine master nodes ==="
    grep -vE "^#" $LBAC_REPO/util/test/etc/cloudconfig.cfg |\
    cut -d';' -f3 |\
    while read cloud ; do
        if [ -n "$cloud" ] ; then
            $LBAC_REPO/util/bin/package.sh "$cloud" MASTERBATCH
        fi
    done

    # package all other nodes
    echo "=== package all nodes ==="
    grep -vE "^#" $LBAC_REPO/util/test/etc/cloudnodes.cfg |\
    while read record ; do
        copyNodeConfig "$record"
    done

    grep -vE "^#" $LBAC_REPO/util/test/etc/cloudconfig.cfg |\
    cut -d';' -f3 |\
    while read cloud; do
        if [ -n "$cloud" ] ; then
            $LBAC_REPO/util/bin/package.sh "$cloud" AUTOBATCH
        fi
    done
}

#
#
#
function setupTestRootCA {
    $LBAC_REPO/util/bin/camgr.sh --batch --mode ca
    chmod -R go+rX $LBAC_REPO/config/integration/htdocs
}

#
#
#
function setupTestSubCA {
    $LBAC_REPO/util/bin/camgr.sh --batch --mode ca --cloud $cloud

    $LBAC_REPO/util/bin/camgr.sh --batch --mode sign --extension v3_subCA \
        --input $LBAC_REPO/config/$cloud/CA/cacert.req \
        --output $LBAC_REPO/config/$cloud/CA/cacert.pem

    $LBAC_REPO/util/bin/camgr.sh --batch --mode importSubCA --cloud $cloud

    $LBAC_REPO/util/bin/camgr.sh --batch --mode devcert --cloud $cloud

    LBAC_CA_DIR=$LBAC_REPO/config/$cloud/CA
    . $LBAC_CA_DIR/cloud.cfg

    cp $LBAC_CA_DIR/$DEV_CERT.pem $LBAC_REPO/config/integration/htdocs/$cloud/devcert.pem
}

function setupConfigure {
    LBAC_CA_DIR=$LBAC_REPO/config/$cloud/CA
    . $LBAC_CA_DIR/cloud.cfg

    # initial revision defined in runSetup
    sed -e "s,CLOUDCONFIG_DOWNLOAD_URL,$DOWNLOAD_URL," $LBAC_REPO/util/bin/configure.sh |\
    sed -e "s,CLOUDCONFIG_CLOUD_NAME,$CLOUD," |\
    openssl smime -sign -signer $LBAC_CA_DIR/$DEV_CERT.pem \
      -md sha256 -binary -out $LBAC_REPO/config/integration/htdocs/$cloud/configure.sh.sig \
      -stream -nodetach \
      -inkey $LBAC_CA_DIR/$DEV_CERT.key \
      -passin file:$LBAC_CA_DIR/$DEV_CERT.passwd 

    chmod -R go+rX $LBAC_REPO/config/integration/htdocs
}

#
#
#
function setupTestCAconf {
    dist=`echo $1 | cut -d';' -f1`
    superior=`echo $1 | cut -d';' -f2`
    cloud=`echo $1 | cut -d';' -f3`
    name=`echo $1 | cut -d';' -f4`

    mkdir -p "$LBAC_REPO/config/$cloud/CA"
    mkdir -p "$LBAC_REPO/config/integration/htdocs/$dist"

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
SCP_ADDR="$LBAC_REPO/config/integration/htdocs/$dist"
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
    else
        echo "Taking snapshot '$SNAPSHOT' for node '$NODE'"
    fi

    grep -vE "^#" $HOSTLIST |\
    if [ $NODE = all ] ; then cat ; else grep $NODE ; fi |\
    while read record ; do
        remote=`echo $record | cut -d';' -f2`
        login=`echo $record | cut -d';' -f3`
        ssh -o "StrictHostKeyChecking no" "$login@$remote" sudo ./dist/bin/update.sh snapshot $SNAPSHOT
    done
}

#
#
#
function teardown {

    # tear down nodes
    grep -vE "^#" $HOSTLIST |\
    if [ $NODE = all ] ; then cat ; else grep $NODE ; fi |\
    while read record ; do
        echo | cleanup "$record"
    done

    if [ $NODE = all ] ; then
        # remove target directory (may need root privilege?)
        rm -rf config/ target/

        docker stop crimsyci_service
        docker stop crimsyreg_service
        reg_vol=`docker inspect crimsyreg_service |  $LBAC_REPO/util/bin/jason.py "[0]['Mounts'][0]['Name']"`

        docker container prune -f
        docker image prune -f
        docker volume rm -f $reg_vol
        msg="Removed everything including logs.                           "
    else
        echo 
        echo "NOTE: teardown limited to node $NODE"
        echo
        msg="Keeping configuration and logs.                              "
    fi

    echo "*****************************************************************"
    echo "*                                                               *"
    echo "* TEARDOWN FINISHED                                             *"
    echo "* $msg *"
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
    testSetup.sh [-b|--branch-file FILE] [-H|hostlist HOSTLIST] [-h|--help] 
        [-j|--jobs JOBFILE] [-b|--branch-file FILE] [-n|--node NODE] 
        [-p|--pause NODE] [-R|--restore LABEL] [-r|--runTests] 
        [-S|--snapshot LABEL] [-s|--setup] [-t|--teardown] [-u|--update CMD] 
        [-w|--wake NODE]

${BOLD}DESCRIPTION${REGULAR}
    Set up, operate and clean up a test environment of CRIMSy. Setup includes 
    compilation, setup of a PKI and installation one or more nodes. 
    This script requires passwordless sudo and passwordless login on all 
    involved hosts.

${BOLD}OPTIONS${REGULAR}
-b|--branch-file FILE
    a list of branches to compile and build on each stage. Contains a stage 
    label, the the branch name and respective flags for container image 
    tagging separated by semicolon

-H|--hostlist HOSTLIST
    mapping of node names (node1, node2, ...) to real host names. Each line 
    contains the node name followed by the host name and the user account 
    separated by semicolon.

-h|--help
    print this help text

-j|--jobs JOBFILE
    run a set of jobs (compile, setup, install, join, test, teardown, update).
    Requires a file with job parameters (e.g. update command, node or cloud 
    name, etc.).

-n|--node NODE
    operate on node NODE only (default all)

-p|--pause NODE
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

-u|--update CMD
    run update.sh on remote node with argument. If used with --branch-file, CMD defaults 
    to 'container'

-w|--wake NODE
    resume execution of node NODE
EOF

}

function mainFunc {
    if [ -z "$HOSTLIST" ] ; then
        error "Must provide HOSTLIST. Call testSetup.sh -h for help."
    fi

    if [ -n "$JOB_FILE" ] ; then
        runJobs
        infoLog "JOB EXECUTION"
        exit 0
    fi

    if [ -n "$PAUSE" ] ; then
        runPause
        exit 0
    fi

    if [ -n "$WAKE" ] ; then
        runWake
        exit 0
    fi

    if [ -n "$SNAPSHOT" ] ; then
        snapshot
        infoLog SNAPSHOT
        exit 0
    fi

    if [ -n "$RESTORE" ] ; then
        restore
        infoLog RESTORE
        exit 0
    fi

    if [ -n "$SETUP" ] ; then
        echo "Starting setup ..."
        runSetup
        infoLog SETUP
    fi

    if [ -n "$UPDATE" ] ; then
        if [ -n "$SETUP" ] ; then
            error "Cannot combine setup and update in a single run"
        fi
        echo "Run update ..."
	UPDATE_CMD=container
        runUpdate 
        infoLog UPDATE
    fi

    if [ -n "$RUNTESTS" ] ; then
        echo "run tests"
        runTests
        infoLog TESTS
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

BRANCH_FILE=''
HOSTLIST=''
JOB_FILE=''
NODE=all
PAUSE=''
RESTORE=''
RUNTESTS=''
SNAPSHOT=''
SETUP=''
TEARDOWN=''
UPDATE=''
WAKE=''

GETOPT=$(getopt -o 'b:H:hj:n:p:R:rS:stu:w:' --longoptions 'branch-file:,hostlist:,help,jobs:,node:,pause:,restore:,runTests,snapshot:,setup,teardown,update:wake:' -n 'testSetup.sh' -- "$@")

if [ $? -ne 0 ]; then
        echo 'Error in commandline evaluation. Terminating...' >&2
        exit 1
fi

eval set -- "$GETOPT"
unset GETOPT

while true ; do
    case "$1" in
    '-b'|'--branch-file')
        BRANCH_FILE=`realpath "$2"`
        shift 2
        continue
        ;;
    '-H'|'--hostlist')
        HOSTLIST=`realpath "$2"`
        shift 2
        continue
        ;;
    '-h'|'--help')
        printHelp
        exit 0
        ;;
    '-j'|'--jobs')
        JOB_FILE=`realpath "$2"`
        shift 2
        continue
        ;;
    '-n'|'--node')
        NODE=$2
        shift 2
        continue
        ;;
    '-p'|'--pause')
        PAUSE=$2
        shift 2
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
    '-u'|'--update')
        UPDATE='update'
        UPDATE_CMD=$2
        shift 2
        continue
        ;;
    '-w'|'--wake')
        WAKE=$2 
        shift 2
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

