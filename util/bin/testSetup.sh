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

if [ $# -eq 0 ] ; then
    echo "usage: `basename $0` HOSTLIST"
    echo 
    echo "HOSTLIST is a file containing one nodekey hostname pair per line"
fi
HOSTLIST=$1

#
#==========================================================
#
function buildDistServer {
    pushd docker/crimsyci > /dev/null
    docker inspect crimsyci >/dev/null 2>/dev/null || docker build -f Dockerfile -t crimsyci .
    popd > /dev/null
}

function createNodeConfig {
    key=`echo $0 | cut -d' ' -f1`
    dst=`echo $0 | cut -d' ' -f2`
    cloud=`grep $key "$LBAC_REPO/util/test/etc/nodeconfig.txt" | cut -c9-18`
    url="http://`hostname -f`:8000/$cloud"

    scp "$LBAC_REPO/util/bin/configBatch.sh" $dst:
    ssh $dst "chmod +x configBatch.sh && ./configBatch.sh $url $key"
    scp $dst:etc/config.sh.asc "$LBAC_REPO/config/nodes/$key.sh.asc"
}
export -f createNodeConfig

function runDistServer {
    cp docker/crimsyci/index.html target/integration/htdocs
    (docker inspect crimsyci_service | grep Status | grep -q running )\
        || docker start crimsyci_service \
        || docker run -p 8000:80 \
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
    mkdir -p config
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

function setupTestCA {
    $LBAC_REPO/util/bin/camgr.sh --batch --mode ca

    for cloud in cloudONE cloudTWO ; do

        $LBAC_REPO/util/bin/camgr.sh --batch --mode ca --cloud $cloud

        $LBAC_REPO/util/bin/camgr.sh --batch --mode sign --extension v3_subCA \
            --input $LBAC_REPO/config/$cloud/CA/cacert.req \
            --output $LBAC_REPO/config/$cloud/CA/cacert.pem

        $LBAC_REPO/util/bin/camgr.sh --batch --mode importSubCA --cloud $cloud
    done
}


function setupTestCAconf {
    for i in "rootCA:rootCA::CI root" \
             "cloudONE:rootCA:cloudONE:CRIMSy CI Cloud ONE" \
             "cloudTWO:rootCA:cloudTWO:CRIMSy CI Cloud TWO" ; do

        dist=`echo $i | cut -d: -f1`
        superior=`echo $i | cut -d: -f2`
        cloud=`echo $i | cut -d: -f3`
        name=`echo $i | cut -d: -f4`

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

    done

}

cd $LBAC_REPO
safetyCheck
setupTestCAconf
buildDistServer
runDistServer
setupTestCA

cat $HOSTLIST | xargs -l1 -i /bin/bash  -c createNodeConfig "{}"

