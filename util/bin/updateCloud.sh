#!/bin/bash
#
# Maintenance Script
# Cloud Resource & Information Management System (CRIMSy)
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
# Update the CRL in the proxy container
# This command should be executed by cron on a regular basis.
# 
#==========================================================
#
function download {
    NAME=`echo "$0" | cut -f1`
    HASH=`echo "$0" | cut -f2`
    FP=`echo "$0" | cut -f3`
    CACERT=`echo "$0" | cut -f4`
    CRL=`echo "$0" | cut -f5`

    curl --silent --output $LBAC_DATASTORE/dist/proxy/conf/crl/$NAME.$FP.crl $CRL
    curl --silent --output $LBAC_DATASTORE/dist/proxy/conf/crt/$NAME.$FP.pem $CACERT
}
export -f download

function error {
        echo $1
        exit 1
}

#
# Download CA certificates and CRLs, provide symlinks
#
function install {
    rm -f $LBAC_DATASTORE/dist/proxy/conf/crt/*
    rm -f $LBAC_DATASTORE/dist/proxy/conf/crl/*
    cat $LBAC_DATASTORE/dist/etc/*/addresses.txt | sort | uniq -f2 -w41 | xargs -l1 -i /bin/bash -c download "{}"

    c_rehash $LBAC_DATASTORE/dist/proxy/conf/crt/
    c_rehash $LBAC_DATASTORE/dist/proxy/conf/crl/
}

#
# Get new CACerts and CRLs and install them into proxy Container
#
function cacrl {
    . $LBAC_DATASTORE/dist/etc/config.sh
    install

    tar -C $LBAC_DATASTORE/dist/proxy/conf -cf /tmp/ca_update.tar crt/ crl/
    docker cp /tmp/ca_update.tar dist_proxy_1:/install/
    docker exec dist_proxy_1 /usr/local/bin/ca_update.sh
    rm "/tmp/ca_update.tar"
    chown -R --reference=$LBAC_DATASTORE/dist/proxy/conf/httpd.conf \
        $LBAC_DATASTORE/dist/proxy/conf/crt \
        $LBAC_DATASTORE/dist/proxy/conf/crl
}

#
# Refresh the docker containers by forcing a pull from remote 
# and rebuilding. Usually called once per week.
#
function refresh {
    . $LBAC_DATASTORE/dist/etc/config.sh
    cd $LBAC_DATASTORE/dist
    docker-compose down --rmi all --volumes --remove-orphans
    docker-compose build --pull
    docker-compose up -d
    if [ $LBAC_DOCKER_EXCLUSIVE = "ON" ] ; then
        docker image prune -f
    fi
}

#
# Do the maintenance stuff for this node.
# Usually called once hourly.
#
function update {
    cacrl
    docker exec dist_ui_1 /usr/local/bin/logpurge.sh
}
# 
#==========================================================
#
. $HOME/.lbac || error "Local cloud node is configured properly"
export LBAC_DATASTORE

case "$1" in
    install)
            install
            ;;
    cacrl)
            test `id -u` -eq 0 || error "The cacrl function must be called as root"
            cacrl
            ;;
    update)
            test `id -u` -eq 0 || error "The update function must be called as root"
            update
            ;;
    refresh)
            test `id -u` -eq 0 || error "The refresh function must be called as root"
            refresh
            ;;
    *)
            error "Usage: updateCloud.sh install | cacrl | refresh | update"
esac

