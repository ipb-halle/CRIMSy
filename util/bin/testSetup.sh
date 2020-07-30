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
p=`dirname $0`
LBAC_REPO=`realpath "$p/../.."`
#
#==========================================================
#
function buildDistServer {
    pushd docker/crimsyci > /dev/null
    docker inspect crimsyci >/dev/null 2>/dev/null || docker build -f Dockerfile -t crimsyci .
    popd > /dev/null
}

function runDistServer {
    mkdir -p target/integration/htdocs
    cp docker/crimsyci/index.html target/integration/htdocs
    (docker inspect crimsyci_service | grep Status | grep -q running )\
        || docker start crimsyci_service \
        || docker run -p 8000:80 \
        --mount type=bind,src=`realpath target/integration/htdocs`,dst=/usr/local/apache2/htdocs \
        --hostname `hostname -f` \
        --detach --name crimsyci_service \
        crimsyci
}


cd $LBAC_REPO
buildDistServer
runDistServer

