#!/bin/bash
#
#
function buildFunc {
    for i in db proxy ui fasta ; do
        pushd target/docker/$i 
        IMAGE_BASE=crimsy$i
        IMAGE_TAG=$REVISION
        IMAGE=$IMAGE_BASE:$IMAGE_TAG
        echo "Building image '$IMAGE'"
        docker build $PULL_FLAG -t $IMAGE .
        pushImage  $IMAGE_TAG
        if echo $RELEASE_FLAGS | grep -q LATEST  ; then
            pushImage LATEST
        fi
        if echo $RELEASE_FLAGS | grep -q MINOR ; then
            pushImage $MAJOR.$MINOR
        fi
        if echo $RELEASE_FLAGS | grep -q MAJOR ; then
            pushImage $MAJOR
        fi
        popd
    done
}

function cleanup {
    echo "build process has failed"
}

function compile {
    flags="$1"

    mvn --batch-mode -DskipTests clean install
    pushd ui
    REVISION=`mvn org.apache.maven.plugins:maven-help-plugin:evaluate -Dexpression=project.version -q -DforceStdout`
    MAJOR=`echo $REVISION | cut -d. -f1`
    MINOR=`echo $REVISION | cut -d. -f2`
    popd

    rm -rf target/docker
    cp -r docker target/
    cp -r target/extralib target/docker/ui/
    cp ui/target/ui.war target/docker/ui/

    if [ -n $STAGE_LABEL ] ; then
        flags="$flags,$STAGE_LABEL"
    fi
    grep -v ";$BRANCH;" config/revision_info.cfg > config/revision_info.tmp
    echo "$REVISION;$BRANCH;$flags" >> config/revision_info.tmp
    mv config/revision_info.tmp config/revision_info.cfg

}


function printHelp {
BOLD=$'\x1b[1m'
REGULAR=$'\x1b[0m'
cat <<EOF
${BOLD}NAME${REGULAR}
    buildDocker.sh

${BOLD}SYNOPSIS${REGULAR}
    buildDocker.sh [-b|branch-file FILE] [-h|--help] [-p|--pull] 
        [-s|--stage-label LABEL] [-t|--test_registry REGISTRY] 

${BOLD}DESCRIPTION${REGULAR}
    Compile CRIMSy source code, build Docker containers and push them
    to a registry. The action can be performed on multiple branches
    upon request. 

-b|--branch-file FILE
    A file containing the stages label, branches and respective flags 
    for tagging the images of a respective branch. Stages can be 
    filtered (used by testSetup.sh). Flags can be "MINOR", "MAJOR" 
    and "LATEST" to to tag containers accordingly. 

      Format: 
        STAGE:BRANCH;FLAG[,FLAG] 

      Example file: 
        stage1:production_3;MINOR
        stage1:production_31;MAJOR,LATEST
        stage1:testing;
        stage2:production_3;MINOR
        stage2:production_31;MINOR
        stage2:testing;MAJOR,LATEST

-h|--help
    Print this help text.

-p|--pull
    Always attempt to pull a newer version of the image during build 

-s|--stage-label LABEL
    Limit build to a given stage label. Note: testSetup.sh executes stages in 
    alphabetic order. The stage label is reproduced in the flag section of 
    the file config/revision_info.cfg.

-t|--test-registry REGISTRY
    Instead of the official docker registry (hub.docker.com), the resulting 
    Docker images will be pushed to the specified registry. 
EOF
}

#
# push image to registry; apply additional tags (e.g. "LATEST" 
# if supplied) 
#
# note: in test setup, the TEST_REGISTRY must be registered in 
# "insecure-registries" in daemon.json 
function pushImage {
    IMAGE_TAG=$1
    echo "pushImage $IMAGE_BASE --> $IMAGE_TAG"
    if [ -z $TEST_REGISTRY ] ; then
        IMAGE_DST=$IMAGE_BASE:$IMAGE_TAG
        if [ $IMAGE_DST != $IMAGE ] ; then
            docker image tag $IMAGE $IMAGE_DST
        fi
        docker push $IMAGE_DST
    else
        IMAGE_DST=$TEST_REGISTRY/$IMAGE_BASE:$IMAGE_TAG
        if [ $IMAGE_DST != $IMAGE ] ; then
            docker image tag $IMAGE $IMAGE_DST
        fi
        docker push $IMAGE_DST
    fi
}

function buildDocker {

    CURRENT_BRANCH=`git status --branch --porcelain=2 | grep "branch.head" | cut -d' ' -f3`
    if [ -r $BRANCH_FILE ] ; then
        git stash push -u
        cat $BRANCH_FILE |\
        if [ -z $STAGE_LABEL ] ; then cat ; else grep $STAGE_LABEL ; fi |\
        while read record ; do
            BRANCH=`echo $record | cut -d';' -f2`
            RELEASE_FLAGS=`echo $record | cut -d';' -f3`
            echo "processing branch file entry: $record"
            git checkout $BRANCH
            compile "$RELEASE_FLAGS" 
            buildFunc
        done
        git checkout "$CURRENT_BRANCH"
        git stash pop || echo "git stash returned with an error: ignored"
    else 
        # just compile the current branch and tag it latest
        RELEASE_FLAGS='LATEST,MINOR,MAJOR'
        BRANCH=$CURRENT_BRANCH
        compile "$RELEASE_FLAGS"
        buildFunc
    fi
}

trap "cleanup; exit 1" ERR
p=`dirname $0`
export LBAC_REPO=`realpath "$p/../.."`
umask 0022
cd $LBAC_REPO

BRANCH_FILE=''
PULL_FLAG=''
STAGE_LABEL=''
TEST_REGISTRY=''

GETOPT=$(getopt -o 'b:hps:t:' --longoptions 'branch-file:,help,pull,stage-label:,test-registry:' -n 'buildDocker.sh' -- "$@")

if [ $? -ne 0 ]; then
        echo 'Error in commandline evaluation. Terminating...' >&2
        exit 1
fi

eval set -- "$GETOPT"
unset GETOPT

while true ; do
    case "$1" in
    '-b'|'--branch-file')
        BRANCH_FILE="$2"
        shift 2
        continue
        ;;
    '-h'|'--help')
        printHelp
        exit 0
        ;;
    '-p'|'--pull')
        PULL_FLAG="--pull"
        shift
        continue
        ;;
    '-s'|'--stage-label')
        STAGE_LABEL="$2"
        shift 2
        continue
        ;;
    '-t'|'--test-registry')
        TEST_REGISTRY="$2"
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

buildDocker
