#!/bin/bash
#
# - refresh base images by pulling from docker hub
# - build and tag images locally
# - push built images  to docker hub
#
p=`dirname $0`
DIR=`realpath "$p/../.."`

TEST_REPOSITORY=$1

function cleanup() {
    echo "build process has failed"
}

#
# push image to registry; apply additional tags (e.g. "LATEST";
# if supplied) 
#
# note: in test setup, the TEST_REPOSITORY must be registered in 
# "insecure-registries" in daemon.json 
function pushImage() {
    IMAGE_TAG=$1
    echo "pushImage $IMAGE_BASE --> $IMAGE_TAG"
    if [ -z $TEST_REPOSITORY ] ; then
        IMAGE_DST=$IMAGE_BASE:$IMAGE_TAG
        if [ $IMAGE_DST != $IMAGE ] ; then
            docker image tag $IMAGE $IMAGE_DST
        fi
        docker push $IMAGE_DST
    else
        IMAGE_DST=$TEST_REPOSITORY/$IMAGE_BASE:$IMAGE_TAG
        if [ $IMAGE_DST != $IMAGE ] ; then
            docker image tag $IMAGE $IMAGE_DST
        fi
        docker push $IMAGE_DST
    fi
}

trap "cleanup; exit 1" ERR

cd $DIR
rm -rf target/docker
cp -r docker target/
cp -r config/extralib target/docker/ui/
cp ui/target/ui.war target/docker/ui/

BRANCH=`git status | grep "On branch" | cut -d' ' -f3`
pushd ui
REVISION=`mvn org.apache.maven.plugins:maven-help-plugin:evaluate -Dexpression=project.version -q -DforceStdout`
MAJOR=`echo $REVISION | cut -d. -f1`
MINOR=`echo $REVISION | cut -d. -f2`
popd

touch config/releases
RELEASE=$MAJOR.$MINOR
RELEASE_FLAGS=`grep "release_$RELEASE;" config/releases | cut -d';' -f3`
(grep -v -s "release_$RELEASE;" config/releases || exit 0) > config/releases.tmp
echo "release_$RELEASE;$BRANCH;$RELEASE_FLAGS" >> config/releases.tmp
mv config/releases.tmp config/releases


# force pull (refresh) of images from Docker Hub
#grep --include Dockerfile -rhE "^FROM " . | \
#   cut -d' ' -f2- | tr -d ' ' | sort | uniq | \
#   xargs -l1 docker pull 

for i in db proxy ui fasta ; do
    pushd target/docker/$i 
    IMAGE_BASE=crimsy$i
    IMAGE_TAG=$RELEASE
    IMAGE=$IMAGE_BASE:$IMAGE_TAG
    echo "Building image '$IMAGE'"
    docker build -t $IMAGE .
    pushImage  $IMAGE_TAG
    if echo $RELEASE_FLAGS | grep -q LATEST  ; then
        pushImage LATEST
    fi
    if echo $RELEASE_FLAGS | grep -q MAJOR ; then
        pushImage $MAJOR
    fi
    popd
done

