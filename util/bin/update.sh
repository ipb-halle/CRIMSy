#!/bin/bash
#
# Setup & Maintenance Script for Proxy & UI
# Cloud Resource & Information Management System (CRIMSy)
# Copyright 2022 Leibniz-Institut f. Pflanzenbiochemie 
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
# 
#==========================================================
#
# temporarily enforce compatibility mode of docker-compose
# as long as we are in transition from docker-compose v1 
# (Python) to v2 (Go)
#
export COMPOSE_COMPATIBILITY=true

#
LBAC_ADMIN_PWFILE=admin.passwd
BACKUP_AGE=4
# 
#==========================================================
#
function createDbConfig {
    # create database script
    rm -f $LBAC_DATASTORE/tmp/clouds.sql
    CLOUDS=`cat $LBAC_DATASTORE/etc/clouds.cfg | cut -d';' -f1`

    for CLOUD in $CLOUDS ; do

        . $LBAC_DATASTORE/dist/etc/$CLOUD/master.sh
        cat <<EOF | m4 >> $LBAC_DATASTORE/tmp/clouds.sql 
dnl
dnl Automatically generated by update.sh 
dnl
define(\`LBAC_CLOUD',\`$CLOUD')dnl
define(\`LBAC_MASTER_SKIP',\`$LBAC_MASTER_SKIP')dnl
define(\`LBAC_MASTER_NODE_ID', \`$LBAC_MASTER_NODE_ID')dnl
define(\`LBAC_MASTER_URL', \`$LBAC_MASTER_URL')dnl
define(\`LBAC_MASTER_INSTITUTION', \`$LBAC_MASTER_INSTITUTION')dnl
define(\`LBAC_NODE_ID',\`$LBAC_NODE_ID')dnl
define(\`LBAC_NODE_RANK',\`$LBAC_NODE_RANK')dnl
define(\`LBAC_INTERNET_FQHN',\`$LBAC_INTERNET_FQHN')dnl
define(\`LBAC_INSTITUTION_SHORT',\`$LBAC_INSTITUTION_SHORT')dnl
dnl
INSERT INTO clouds (name) VALUES ('LBAC_CLOUD') ON CONFLICT DO NOTHING;
LBAC_MASTER_SKIP INSERT INTO nodes (id, baseurl, institution, local) VALUES 
LBAC_MASTER_SKIP ('LBAC_MASTER_NODE_ID', 'LBAC_MASTER_URL', 'LBAC_MASTER_INSTITUTION', False) ON CONFLICT DO NOTHING;
INSERT INTO nodes (id, baseUrl, institution, local) VALUES
  ( 'LBAC_NODE_ID', 'https://LBAC_INTERNET_FQHN:8443/ui', 'LBAC_INSTITUTION_SHORT', True) ON CONFLICT DO NOTHING;
LBAC_MASTER_SKIP INSERT INTO cloud_nodes (node_id, rank, cloud_id) SELECT 'LBAC_MASTER_NODE_ID'::UUID AS node_id, 10 AS rank, id AS cloud_id 
LBAC_MASTER_SKIP FROM clouds WHERE name='LBAC_CLOUD' ON CONFLICT DO NOTHING;
INSERT INTO cloud_nodes (node_id, rank, cloud_id) SELECT 'LBAC_NODE_ID'::UUID, LBAC_NODE_RANK AS rank, id AS cloud_id 
FROM clouds WHERE name='LBAC_CLOUD' ON CONFLICT DO NOTHING;
EOF

    done
}


function createProxyConfig {
    sudo rm -rf $LBAC_DATASTORE/tmp/proxy_conf
    CLOUD=`cat $LBAC_DATASTORE/etc/primary.cfg`
    cp -r  $LBAC_DATASTORE/dist/etc/proxy_conf $LBAC_DATASTORE/tmp
    cp $LBAC_DATASTORE/dist/etc/official_cert.pem $LBAC_DATASTORE/tmp/proxy_conf
    cp $LBAC_DATASTORE/dist/etc/official_cert.key $LBAC_DATASTORE/tmp/proxy_conf
    cp $LBAC_DATASTORE/dist/etc/$CLOUD/$CLOUD.cert $LBAC_DATASTORE/tmp/proxy_conf/lbac_cert.pem
    openssl pkey -in "$LBAC_DATASTORE/etc/lbac_cert.key" \
      -passin "file:$LBAC_DATASTORE/etc/lbac_cert.passwd" \
      -out "$LBAC_DATASTORE/tmp/proxy_conf/lbac_cert.key"


    find $LBAC_DATASTORE/tmp/proxy_conf -type f -name "*.m4" -exec /bin/bash -c preprocess {} \;

    CLOUDS=`cat $LBAC_DATASTORE/etc/clouds.cfg | cut -d';' -f1`
    for c in $CLOUDS ; do
        proxyProcessCloud $c
    done

    c_rehash $LBAC_DATASTORE/tmp/proxy_conf/crl/
}

function createUiConfig {
    sudo rm -rf $LBAC_DATASTORE/tmp/ui_conf
    mkdir -p $LBAC_DATASTORE/tmp/ui_conf

    uuidgen -r | tr -d $'\n' > $LBAC_DATASTORE/tmp/ui_conf/keypass
    uuidgen -r | tr -d $'\n' > $LBAC_DATASTORE/tmp/ui_conf/trustpass

    cp $LBAC_DATASTORE/etc/clouds.cfg $LBAC_DATASTORE/tmp/ui_conf
    CLOUDS=`cat $LBAC_DATASTORE/etc/clouds.cfg | cut -d';' -f1`
    for c in $CLOUDS ; do
        downloadCloudPackages $c
    done

    LBAC_KEYSTORE_PASSWORD=`cat $LBAC_DATASTORE/tmp/ui_conf/keypass`
    LBAC_TRUSTSTORE_PASSWORD=`cat $LBAC_DATASTORE/tmp/ui_conf/trustpass`

    cat <<EOF > $LBAC_DATASTORE/tmp/ui_conf/lbac_properties.xml
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE properties SYSTEM "http://java.sun.com/dtd/properties.dtd">
<properties>
<!-- 
  * CRIMSy Properties 
  * (c) 2020 CRIMSy Development Team 
  -->
<comment>Properties for CRIMSy UI</comment>

<!--
  Mutual ssl authentication
-->
<entry key="SecureWebClient.KEYSTORE_TYPE">PKCS12</entry>
<entry key="SecureWebClient.KEYSTORE_PATH">/data/conf</entry>
<entry key="SecureWebClient.KEYSTORE_PASSWORD">$LBAC_KEYSTORE_PASSWORD</entry>
<entry key="SecureWebClient.TRUSTSTORE_TYPE">JKS</entry>
<entry key="SecureWebClient.TRUSTSTORE_PATH">/data/conf</entry>
<entry key="SecureWebClient.TRUSTSTORE_PASSWORD">$LBAC_TRUSTSTORE_PASSWORD</entry>
<entry key="SecureWebClient.SSL_PROTOCOL">TLSv1.2</entry>
<!-- key alias -->
<entry key="LBAC_INTERNET_FQHN">$LBAC_INTERNET_FQHN</entry>
<!-- default admin password -->
<entry key="DEFAULT_ADMIN_PASSWORD">`cat $LBAC_DATASTORE/etc/$LBAC_ADMIN_PWFILE`</entry>
</properties>
EOF

}

# download certificates and CRLs for a single CA
function downloadCRL {
    NAME=`echo "$1" | cut -f1`
    HASH=`echo "$1" | cut -f2`
    FP=`echo "$1" | cut -f3`
    CACERT=`echo "$1" | cut -f4`
    CRL=`echo "$1" | cut -f5`

    output=$LBAC_DATASTORE/tmp/proxy_conf/crl/$NAME.$FP.crl
    curl --silent --output $output $CRL
    openssl crl -in $output -CApath $LBAC_DATASTORE/tmp/proxy_conf/crt -verify -noout 2>&1 |\
        grep -q "verify OK" || exit 255
}
export -f downloadCRL

# download cloud certificates for UI
function downloadCloudPackages {
    CLOUD=$1
    url=`grep "$CLOUD;" $LBAC_DATASTORE/etc/clouds.cfg | cut -d';' -f2`

    pushd $LBAC_DATASTORE/tmp >/dev/null
    curl --silent --output $CLOUD.asc.sig $url/$LBAC_INSTITUTION_MD5.asc.sig || \
        (echo "Download fehlgeschlagen" && exit 1)
    openssl smime -verify -in $CLOUD.asc.sig -certfile ../etc/$CLOUD/devcert.pem \
        -CAfile ../etc/$CLOUD/chain.pem | openssl smime -decrypt -inform PEM \
        -inkey ../etc/lbac_cert.key -passin file:../etc/lbac_cert.passwd \
        -out $CLOUD.tar.gz || (echo "Entschlüsselung oder Signaturprüfung fehlgeschlagen" && \
        rm $CLOUD.asc.sig $CLOUD.tar.gz && exit 1)

    popd >/dev/null
    tar -xzf tmp/$CLOUD.tar.gz
    rm tmp/$CLOUD.asc.sig tmp/$CLOUD.tar.gz

    #
    # note: JDK 8 does not support PKCS12 truststores!
    # --> see workaround for JKS truststore creation below;
    #     refer also to docker/ui/setup.sh, .../createTruststores.sh
    #
#   openssl pkcs12 -export -in $LBAC_DATASTORE/dist/etc/$CLOUD/$CLOUD.cert \
#       -certfile $LBAC_DATASTORE/dist/etc/$CLOUD/chain.pem \
#       -out $LBAC_DATASTORE/tmp/ui_conf/$CLOUD.truststore \
#       -passout file:$LBAC_DATASTORE/tmp/ui_conf/trustpass \
#       -nokeys

    mkdir -p $LBAC_DATASTORE/tmp/ui_conf/$CLOUD
    cp $LBAC_DATASTORE/dist/etc/$CLOUD/chain.pem $LBAC_DATASTORE/tmp/ui_conf/$CLOUD
    cp $LBAC_DATASTORE/dist/etc/$CLOUD/$CLOUD.cert $LBAC_DATASTORE/tmp/ui_conf/$CLOUD/$LBAC_INTERNET_FQHN.pem
    pushd $LBAC_DATASTORE/tmp/ui_conf/$CLOUD >/dev/null
    cat chain.pem | $LBAC_DATASTORE/dist/bin/chainsplit.py $CLOUD
    rm chain.pem
    popd > /dev/null


    # set up PKCS12 keystore
    # OpenSSL-Bug: passin and passout must not be identical files!
    openssl pkcs12 -export -in  $LBAC_DATASTORE/dist/etc/$CLOUD/$CLOUD.cert \
        -inkey $LBAC_DATASTORE/etc/lbac_cert.key \
        -out $LBAC_DATASTORE/tmp/ui_conf/$CLOUD.keystore \
        -passin file:$LBAC_DATASTORE/etc/lbac_cert.passwd \
        -passout file:$LBAC_DATASTORE/tmp/ui_conf/keypass \
        -name $LBAC_INTERNET_FQHN 
}

function error {
        echo $1
        exit 1
}

#
# do m4 preprocessing of XML files (and SQL files?)
#
function preprocess {
        SRC=$0
        DESTDIR=`dirname $SRC`
        DESTFILE=`basename $SRC ".m4"`
        m4 $SRC > $DESTDIR/$DESTFILE
        rm $SRC

}
export -f preprocess

#
# prepare the certificates for the entire CA chain 
# of a single cloud, download and verify CRLs 
function proxyProcessCloud {
    CLOUD=$1

    # get the certificates
    cp $LBAC_DATASTORE/dist/etc/$CLOUD/chain.pem $LBAC_DATASTORE/tmp/proxy_conf/crt/

    pushd $LBAC_DATASTORE/tmp/proxy_conf/crt >/dev/null
    cat chain.pem | $LBAC_DATASTORE/dist/bin/chainsplit.py $CLOUD
    rm chain.pem
    c_rehash .
    popd >/dev/null

    # get the CRLs
    cat $LBAC_DATASTORE/dist/etc/$CLOUD/addresses.txt |\
        sort | uniq -f2 -w41 |\
        xargs -i /bin/bash -c "downloadCRL '{}'" || error "CRL verification failed"
}

#
# restore the entire system
#
function restoreFunc {
    "$LBAC_DATASTORE/dist/bin/lbacInit.sh" stopService ui

    pushd "$LBAC_DATASTORE/backup/ui" > /dev/null
    tar -C "$LBAC_DATASTORE/data" -xvf ui.$LABEL.tar.gz
    popd > /dev/null

    restoreDB

    "$LBAC_DATASTORE/dist/bin/lbacInit.sh" startService ui
}

#
# restore a database snapshot only
#
function restoreDB {
    echo "restoring database snapshot: $LABEL"
    cat "$LBAC_DATASTORE/backup/db/dump.$LABEL.sql" |\
        docker exec -i -u postgres dist_db_1 psql ||\
        error "Error during database restore"

    if [ $LABEL = "INSTALL" ] ; then
        docker exec dist_db_1 /usr/local/bin/post-restore.sh || \
            error "Error during database restore"
    fi

    (docker inspect dist_ui_1 | grep Status | grep -q running ) && \
        docker restart dist_ui_1
}

#
#
#
function snapshotCleanup {
        pushd "$LBAC_DATASTORE/backup" > /dev/null
        find db/ ui/ -type f -mtime +$BACKUP_AGE -exec rm {} \;
        popd > /dev/null
}

#
# take a snapshot of the database. Run the pre-snapshot Skript
# if the snapshot is taken during an installation
#
function snapshotDB {
        mkdir -p "$LBAC_DATASTORE/backup/db"
        pushd "$LBAC_DATASTORE/backup/db" > /dev/null
        if [ $LABEL = "INSTALL" ] ; then
            docker exec dist_db_1 pg_dump --create --clean --if-exists \
                -U lbac > "$LBAC_DATASTORE/backup/db/dump.PREINSTALL.sql" || \
                error "Error during database pre-install dump"
            docker exec dist_db_1 /usr/local/bin/pre-snapshot.sh || \
                error "Error during pre-snapshot script"
        fi
        docker exec dist_db_1 pg_dump --create --clean --if-exists \
          -U lbac > "$LBAC_DATASTORE/backup/db/dump.$LABEL.sql" || \
          error "Error during database dump"
        rm -f dump.latest.sql
        ln -s dump.$LABEL.sql dump.latest.sql
        popd > /dev/null
}

#
# snapshot
#
function snapshotFunc {
    "$LBAC_DATASTORE/dist/bin/lbacInit.sh" startService db
    snapshotDB
    snapshotUI
    snapshotCleanup
    docker exec -i dist_db_1 /usr/local/bin/getversion.sh
    cp "$LBAC_DATASTORE/data/db/CURRENT_PG_VERSION" "$LBAC_DATASTORE/tmp/OLD_PG_VERSION"
}

#
#
#
function snapshotUI {
        mkdir -p "$LBAC_DATASTORE/backup/ui"
        pushd "$LBAC_DATASTORE/backup/ui" > /dev/null
        rm -f ui.latest.tar.gz
        tar -C "$LBAC_DATASTORE/data" -czf ui.$LABEL.tar.gz ui
        ln -s ui.$LABEL.tar.gz ui.latest.tar.gz
        popd > /dev/null
}

#
# Refresh the docker containers by forcing a pull from remote 
# and rebuilding. Usually called once per week.
#
function superDoContainer {
    pushd dist >/dev/null
    if [ $LBAC_UPDATE_LEVEL != NONE ] ; then 
        docker-compose down --rmi all --volumes --remove-orphans
        docker-compose up -d
        if [ $LBAC_DOCKER_EXCLUSIVE = "ON" ] ; then
            echo "pruning images ..."
            docker image prune -f
        fi
    fi
    popd >/dev/null
}

function superDoDb {
    "$LBAC_DATASTORE/dist/bin/lbacInit.sh" startService db
    echo "Waiting 40 sek. for database to come up ..."
    sleep 40

    docker exec dist_db_1 /usr/local/bin/getversion.sh

    if [ -e "$LBAC_DATASTORE/tmp/OLD_PG_VERSION" ] ; then
        OLD_PG_VERSION=`cat "$LBAC_DATASTORE/tmp/OLD_PG_VERSION"`
        CURRENT_PG_VERSION=`cat "$LBAC_DATASTORE/data/db/CURRENT_PG_VERSION"`
        if [ $CURRENT_PG_VERSION != $OLD_PG_VERSION ] ; then
            echo "Restoring snapshot ..."
            LABEL=latest
            restoreDB
            docker exec dist_db_1 /usr/local/bin/post-restore.sh || error "Error during post-restore script"
        fi
    fi

    echo "Performing schema updates ..."
    docker exec -i -u postgres dist_db_1 /usr/local/bin/dbupdate.sh

    echo "Installing cloud information ..."
    cat $LBAC_DATASTORE/tmp/clouds.sql |
        docker exec -i -u postgres dist_db_1 psql -Ulbac lbac
}

function superDoProxy {
    # alternatively chown -R --reference=...
    chown -R 80:80 $LBAC_DATASTORE/tmp/proxy_conf
    rsync -a --del $LBAC_DATASTORE/tmp/proxy_conf/ $LBAC_DATASTORE/data/proxy_conf
    rm -rf $LBAC_DATASTORE/tmp/proxy_conf
    (docker inspect dist_proxy_1 | grep Status | grep -q running ) && \
        docker exec -ti dist_proxy_1 apachectl -k graceful
}

function superDoUI {
    chown -R 8080:8080 $LBAC_DATASTORE/tmp/ui_conf
    chown 8080:8080 $LBAC_DATASTORE/data/ui
    rsync -a --del $LBAC_DATASTORE/tmp/ui_conf/ $LBAC_DATASTORE/data/ui_conf
    rm -rf $LBAC_DATASTORE/tmp/ui_conf

    mkdir -p $LBAC_DATASTORE/data/reports/
    cp $LBAC_DATASTORE/dist/reports/*.prpt $LBAC_DATASTORE/data/reports/
    chown -R 8080:8080 $LBAC_DATASTORE/data/reports/

    mkdir -p $LBAC_DATASTORE/data/tmp/reports/
    chown -R 8080:8080 $LBAC_DATASTORE/data/tmp/reports/

    (docker inspect dist_ui_1 | grep Status | grep -q running ) && \
        docker restart dist_ui_1
}

#
#==========================================================
#
. $HOME/.lbac || error "Local cloud node is configured properly"
. $LBAC_DATASTORE/etc/config.sh
export LBAC_DATASTORE
export LC_ALL=C

cd $LBAC_DATASTORE

case $1 in
    backup)
        test `id -u` -eq 0 || error "'update.sh backup' function must be called as root"
        LABEL=`date "+%Y%m%d%H%M"`
        snapshotFunc
        ;;
    container)
        test `id -u` -eq 0 || error "'update.sh container' function must be called as root"
        superDoContainer 
        ;;
    db)
        createDbConfig
        sudo dist/bin/update.sh superDb 
        ;;
    log)
        docker exec dist_ui_1 /usr/local/bin/logpurge.sh
        ;;
    proxy)
        createProxyConfig
        sudo dist/bin/update.sh superProxy
        ;;
    ui)
        createUiConfig
        sudo dist/bin/update.sh superUI
        ;;
    restore)
        test `id -u` -eq 0 || error "'update.sh restore' function must be called as root"
        echo "restoring snapshot"
        LABEL=$2
        restoreFunc
        ;;
    restoreDB)
        echo "restoring database snapshot"
        LABEL=$2
        restoreDB
        ;;
    snapshot)
        test `id -u` -eq 0 || error "'update.sh snapshot' function must be called as root"
        echo "Taking snapshot"
        LABEL=$2
        snapshotFunc
        ;;
    superDb)
        test `id -u` -eq 0 || error "'update.sh superDb' function must be called as root"
        superDoDb
        ;;
    superProxy)
        test `id -u` -eq 0 || error "'update.sh superProxy' function must be called as root"
        superDoProxy
        ;;
    superUI)
        test `id -u` -eq 0 || error "'update.sh superUI' function must be called as root"
        superDoUI
        ;;
    *)
        echo "Usage: update.sh backup|container|db|proxy|restore|restoreDB|"
        echo "                 snapshot|ui|superDb|superProxy|superUI"
        ;;
esac

