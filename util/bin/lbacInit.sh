#!/bin/bash
#
# Cloud Resource & Information Management System (CRIMSy)
# Init Script
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
max_repeat=8
compose="dist_"

#
#==========================================================
#
function check() {
    unit=$compose$1
    echo "Checking unit: $unit" 
    image_id=`docker images -q ${unit}:latest`
    if [ -z "$image_id" ] ; then
        status="missing"
    else
        #  created, restarting, running, removing, paused, exited, or dead
        status=`docker inspect ${unit}_1 | grep "Status" | cut -d: -f2 | cut -d\" -f2`
    fi
    echo $status
}

#
#==========================================================
#
# specifically remove dead containers
#
function cleanup() {
    unit=$1
    docker rm -v -f $compose${unit}_1
}

# 
#==========================================================
#
function error {
        echo $1
        exit 1
}

#
#==========================================================
#
function remove() {

    docker-compose down --rmi local --remove-orphans 2>/dev/null

    if [ "$LBAC_DOCKER_EXCLUSIVE" = "ON" ] ;then

        # clean up containers
        for i in `docker ps -a -q | tr -d $'\n'` ; do
            docker stop $i
            docker rm -v -f $i
        done

        # clean up dangling images
        for i in `docker images -q -f dangling=true | tr -d $'\n'` ; do
            docker rmi $i
        done

        # clean up dangling volumes
        for i in `docker volume ls -q -f dangling=true | tr -d $'\n'` ; do
            docker volume rm $i
        done

    fi
}
#
#==========================================================
#
function restartService() {
    service=$1
    repeat=0
    restarted=0
    while [ $repeat -lt $max_repeat ] ; do
        check $service
        echo "CONTAINER STATUS: $status"
        case $status in
            restarting)
                sleep 10
                ;;
            running)
                if [ $restarted -lt 1 ] ; then
                    restarted=1
                    docker restart $compose${service}_1
                else
                    return 0
                fi
                ;;
            removing)
                sleep 10
                ;;
            *)
                ;;
        esac
        repeat=$(($repeat + 1))
    done
    return 1
}

#
#==========================================================
#
function  startService() {
    service=$1
    repeat=0 
    while [ $repeat -lt $max_repeat ] ; do
        check $service
        echo "CONTAINER STATUS: $status"
        case $status in
            missing)
                docker-compose up -d --build --remove-orphans $service
                ;;
            restarting)
                sleep 30
                ;;
            created)
                docker-compose up -d $service
                ;;
            exited)
                docker-compose start $service
                ;;
            paused)
                docker-compose unpause $service
                ;;
            running)
                return 0
                ;;
            removing) 
                sleep 30
                ;;
            dead)
                cleanup $service
                ;;
            *)
                ;;
        esac 
        repeat=$(($repeat + 1))
    done
    return 1
}

function start() {
    startService db || error "Starting database container failed"
    startService ui || error "Starting ui container failed"
    startService proxy || error "Starting proxy container failes"
}
#
#==========================================================
#
function stopService() {
    service=$1
    docker-compose stop $service
}

function stop() {
    stopService proxy
    stopService ui
    stopService db
}
#
#==========================================================
#
test `id -u` -eq 0 || error "This script must be called as root"

. $HOME/.lbac || error "Local cloud node not configured"
. "$LBAC_DATASTORE/dist/etc/config.sh" || error "Could not load config file"

pushd $LBAC_DATASTORE/dist > /dev/null

case $1 in
    start)
        start
        ;;
    startService)
        startService $2
        ;;
    stop)
        stop
        ;;
    stopService)
        stopService $2
        ;;
    check)
        check $2
        ;;
    remove)
        remove
        ;;
    restartService)
        restartService $2
        ;;
    *)
        echo "Usage: lbacInit.sh check SERVICE|start|startService SERVICE|stop|stopService SERVICE|remove"
esac

popd > /dev/null

