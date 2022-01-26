#!/bin/bash
#
# - refresh base images by pulling from docker hub
# - build and tag images locally
# - push built images  to docker hub
#
p=`dirname $0`
DIR=`realpath "$p/../.."`

cleanup() {
    echo "build process has failed"
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

RELEASE=$MAJOR.$MINOR

#
# need some better mechanism to determine revisions for
# MAJOR and LATEST tags
#

#grep --include Dockerfile -rhE "^FROM " . | \
#   cut -d' ' -f2- | tr -d ' ' | sort | uniq | \
#   xargs -l1 docker pull 

for i in db proxy ui fasta ; do
    pushd target/docker/$i 
    IMAGE=ipbhalle/crimsy$i:$RELEASE
    echo "Building image '$IMAGE'"
    docker build -t $IMAGE .
    if [ $BRANCH = 'master' ] ; then
        docker image tag $IMAGE ipbhalle/crimsy$i:LATEST
    fi
#   docker push ipbhalle/crimsyplugins
    popd
done

