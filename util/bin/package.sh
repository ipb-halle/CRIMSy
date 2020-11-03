#!/bin/bash
#
# Cloud Resource & Information Management System (CRIMSy)
# Packaging Tool
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
# Usage: package.sh CLOUDNAME [MASTER|AUTO]
# Generate an encrypted and signet installer from provided
# configuration data. The CLOUDNAME option allows to 
# run multiple clouds (i.e. test and production) from the 
# same configuration directory.
#
# The MASTER option allows to designate a node from the 
# directory 'nodes' as master.
#
# The AUTO option allows to generate packages for all 
# nodes from the CLOUDNAME directory
#
# If neither MASTER nor AUTO are given, a new node (from 
# the 'nodes' directory) can be added to the selected cloud.
# For each CLOUDNAME, the master node must be the first 
# node to be configured!
# 
# 
#==========================================================
#
LBAC_UI="ui.war"

MOLPAINTJS_REPO=https://github.com/ipb-halle/MolPaintJS
JBROWSE_REPO=https://github.com/GMOD/jbrowse/releases/download/1.16.9-release
JBROWSE_RELEASE_ZIP=JBrowse-1.16.9.zip
JBROWSE_RELEASE_DIR=JBrowse-1.16.9
#
#==========================================================
#

function dialog_CHECK_CONFIG {
	dialog --backtitle "$CLOUD_NAME" \
	  --title "Kontrolle" \
	  --exit-label "Ok" --textbox $LBAC_CONFIG 20 72 || error "Aborted"

	dialog --backtitle "$CLOUD_NAME" \
	  --yesno "Kann die Konfiguration gefahrlos eingelesen werden?" \
	  15 72 || error "Aborted"

	. $LBAC_CONFIG

        # move the encrypted config to the respective cloud
        mv $tmp $LBAC_REPO/config/$LBAC_CLOUD
}

function decryptConfig {
	id=`cat $tmp | grep CERTIFICATE_ID= | cut -d= -f2 | tr -d $'\n'`
	echo "ID: $id"
	cat $tmp | sed -n -e "/SMIME ENCRYPTED CONFIG BEGIN/,/SMIME ENCRYPTED CONFIG END/p" | \
	  tail -n +2 | head -n -1 | \
	  openssl smime -decrypt -inform PEM -inkey $LBAC_REPO/config/$LBAC_CLOUD/CA/devcert/$id.key \
	  -passin file:config/$LBAC_CLOUD/CA/devcert/$id.passwd \
          -out $LBAC_CONFIG || error "Error in decryption"

        CURRENT_CONFIG_FILE=$tmp
}

function dialog_MASTER {
                dialog --backtitle "$CLOUD_NAME" \
                   --msgbox "Erstelle neuen Master-Knoten" 15 72 || error "Aborted"
                setupMaster
}

function dialog_SELECT_CONFIG {

	# declare -A inst
	for i in $LBAC_REPO/config/nodes/*.asc ; do
		inst=( "${inst[@]}" "`basename $i`" \
		  "`grep "LBAC_INSTITUTION=" $i | cut -d= -f2 | tr -d $'\n'`" )
	done

	dialog --backtitle "$CLOUD_NAME" \
	  --menu "Bitte selektieren Sie die Konfiguration" \
	  15 72 10 "${inst[@]}" 2>$TMP_RESULT || error "Aborted"

        tmp="$LBAC_REPO/config/nodes/`cat $TMP_RESULT`"
        echo $tmp
	decryptConfig $tmp 
	dialog_CHECK_CONFIG $tmp
}
#
#==========================================================
#
function autoPackage {
	. $LBAC_REPO/config/$LBAC_CLOUD/master.sh || error "Master config not found"
        for tmp in $LBAC_REPO/config/$LBAC_CLOUD/*.asc ; do
            echo `basename $tmp`
            decryptConfig $tmp
            . $LBAC_CONFIG
            genPackage
        done
}

function makeCert {
	# extract request
	cat $LBAC_CONFIG | sed -n -e "/BEGIN CERTIFICATE REQUEST/,/END CERTIFICATE REQUEST/p" \
	  > $TMP_RESULT

    # the certificate identifier is the md5sum of the certificate request
	LBAC_CERT_IDENTIFIER=`md5sum $TMP_RESULT | cut -c1-32`

    # test whether certificate has been revoked
    $LBAC_REPO/util/bin/camgr.sh --cloud $LBAC_CLOUD --mode testRevoked --hash $LBAC_CERT_IDENTIFIER $BATCH
    case $? in 
        0)  # everything is fine - do nothing
            ;;
        1)  dialog --backtitle "$CLOUD_NAME" \
                --msgbox "Ein Zertifikat wurde widerrufen: 

  Institut___________: $LBAC_INSTITUTION 
  Instituts-MD5______: $LBAC_INSTITUTION_MD5
  Konfigurationsdatei: `basename $CURRENT_CONFIG_FILE` 

Für den Knoten muss mittels 'configure.sh' ein neuer Zertifikatsrequest erzeugt werden!" 15 72
            error "Revoked certificate"
            ;;
        2)  # certificate not found
            echo "certificate not found: $LBAC_CERT_IDENTIFIER"
            if [ -z $BATCH ] ; then 
                dialog --backtitle "$CLOUD_NAME" \
                  --msgbox "Im folgenden Schritt wird ggf. das Zertifikat ausgestellt. Bitte prüfen Sie gründlich." 15 72 || error "Aborted"
            fi 
            ;;
    esac

    # sign the certificate (or return an existing certificate)
    $LBAC_REPO/util/bin/camgr.sh --cloud $LBAC_CLOUD --mode sign \
      --output  $LBAC_REPO/target/dist/etc/$LBAC_CLOUD/$LBAC_CLOUD.cert \
      --input $TMP_RESULT $BATCH || \
      error "Error in makeCert"
}

function setupMaster {
		LBAC_MASTER_NODE_ID=$LBAC_NODE_ID
		LBAC_MASTER_URL="https://$LBAC_INTERNET_FQHN:8443/ui"
		LBAC_MASTER_INSTITUTION="$LBAC_INSTITUTION_SHORT"
		cat <<EOF > $LBAC_REPO/config/$LBAC_CLOUD/master.sh
#
# master.sh
# automatically generated by 'package.sh' script
#
LBAC_CLOUD=$LBAC_CLOUD
LBAC_DISTRIBUTION_POINT="$DOWNLOAD_URL"
LBAC_MASTER_NODE_ID=$LBAC_MASTER_NODE_ID
LBAC_MASTER_URL="$LBAC_MASTER_URL"
LBAC_MASTER_INSTITUTION="$LBAC_MASTER_INSTITUTION"
EOF

}

function cleanTmp {
	rm -f $TMP_RESULT
	rm -f $LBAC_CONFIG
}

function cleanUp {
	pushd target > /dev/null
	rm -r dist
	mkdir -p dist/etc/$LBAC_CLOUD
	mkdir -p dist/bin
	popd >/dev/null
}

function copyCert {
        cp $LBAC_CA_DIR/chain.txt         $LBAC_REPO/target/dist/etc/$LBAC_CLOUD/$LBAC_CLOUD.chain
        cp $LBAC_CA_DIR/addresses.txt $LBAC_REPO/target/dist/etc/$LBAC_CLOUD/addresses.txt
        cp $LBAC_CA_DIR/truststore        $LBAC_REPO/target/dist/etc/$LBAC_CLOUD/$LBAC_CLOUD.truststore
        cp $LBAC_CA_DIR/truststore.passwd $LBAC_REPO/target/dist/etc/$LBAC_CLOUD/$LBAC_CLOUD.trustpass
}

function copyDocker {
	cp docker/docker-compose.yml.m4 target/dist
	cp -r docker/db target/dist
	cp -r docker/proxy target/dist
	cp -r docker/ui target/dist
}

function copyFiles {

	# directories already created by function copyDocker
	cp ui/target/$LBAC_UI target/dist/ui
	cp -r $LBAC_REPO/config/extralib target/dist

	#
	cp $LBAC_REPO/config/$LBAC_CLOUD/master.sh target/dist/etc/$LBAC_CLOUD/
        cp util/systemd/system/lbac.service.m4 target/dist/etc

        #
	cp util/bin/backup.sh target/dist/bin
        cp util/bin/join.sh target/dist/bin
        cp util/bin/lbacInit.sh target/dist/bin
        cp util/bin/setupROOT.sh target/dist/bin
        cp util/bin/updateCloud.sh target/dist/bin
}

function copyPlugins {
        pushd target >/dev/null
        mkdir -p dist/proxy/htdocs/plugins
        cp -r jbrowse/$JBROWSE_RELEASE_DIR dist/proxy/htdocs/plugins/JBrowse
        cp -r MolPaintJS/docs dist/proxy/htdocs/plugins/MolPaintJS
        popd > /dev/null
}

function error {
	echo $1
	cleanTmp
	exit 1
}

function genPackage {
    cleanUp
    copyDocker
    copyFiles
    copyPlugins
    masterConfig
    makeTomcatUsers
    makeCert
    copyCert
    package
    cleanTmp
}

function getJBrowse {
        mkdir -p jbrowse
        pushd jbrowse >/dev/null
        if [ ! -f $JBROWSE_RELEASE_ZIP ] ; then
            curl -L --silent --output $JBROWSE_RELEASE_ZIP $JBROWSE_REPO/$JBROWSE_RELEASE_ZIP
        fi
        if [ ! -d $JBROWSE_RELEASE_DIR ] ; then
            unzip $JBROWSE_RELEASE_ZIP
        fi
        popd >/dev/null
}

function getMolPaintJS {
        if [ -d MolPaintJS ] ; then
            pushd MolPaintJS > /dev/null
            git pull 
            popd >/dev/null
        else
            git clone https://github.com/ipb-halle/MolPaintJS
        fi
}

function getResources {
    mkdir -p target
    pushd target >/dev/null
    getMolPaintJS
    getJBrowse
    popd >/dev/null
}

function makeTomcatUsers {
	cat << EOF > target/dist/ui/tomcat-users.xml
<?xml version='1.0' encoding='utf-8'?>
<tomcat-users xmlns="http://tomcat.apache.org/xml"
              xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
              xsi:schemaLocation="http://tomcat.apache.org/xml tomcat-users.xsd"
              version="1.0">
  <role rolename="admin"/>
</tomcat-users>
EOF
}

function masterConfig {
        if [ $LBAC_NODE_ID = $LBAC_MASTER_NODE_ID ] ; then
                echo "Configuring MASTER node: $LBAC_NODE_ID"
		LBAC_MASTER_SKIP="dnl"
		LBAC_NODE_RANK=10
	else
                echo "Configuring BASIC node: $LBAC_NODE_ID"
		LBAC_MASTER_SKIP=""
		LBAC_NODE_RANK=1
	fi
	echo "LBAC_MASTER_SKIP=\"$LBAC_MASTER_SKIP\"" >> target/dist/etc/$LBAC_CLOUD/master.sh
	echo "LBAC_NODE_RANK=\"$LBAC_NODE_RANK\"" >> target/dist/etc/$LBAC_CLOUD/master.sh
}

function package {
	pushd target > /dev/null

        #
        # Standard installation package
        #
	tar -czf - dist | uuencode -m - | cat ../util/bin/setup.sh - | \
	openssl smime -encrypt -binary -stream -outform PEM -aes-256-cbc \
	  $LBAC_CA_DIR/cloud/$LBAC_CERT_IDENTIFIER.pem | openssl smime -sign \
	  -signer $LBAC_CA_DIR/$DEV_CERT.pem -nocerts -stream \
	  -inkey $LBAC_CA_DIR/$DEV_CERT.key -passin file:$LBAC_CA_DIR/$DEV_CERT.passwd \
	  -md sha256 -out $LBAC_INSTITUTION_MD5.asc.sig

        #
        # Package for joining additional cloud
        #
        tar -czf - dist/etc/$LBAC_CLOUD | \
        openssl smime -encrypt -binary -stream -outform PEM -aes-256-cbc \
          $LBAC_CA_DIR/cloud/$LBAC_CERT_IDENTIFIER.pem | openssl smime -sign \
          -signer $LBAC_CA_DIR/$DEV_CERT.pem -nocerts -stream \
          -inkey $LBAC_CA_DIR/$DEV_CERT.key -passin file:$LBAC_CA_DIR/$DEV_CERT.passwd \
          -md sha256 -out $LBAC_INSTITUTION_MD5.$LBAC_CLOUD

        echo "Die verschlüsselten Installationspakete stehen bereit. Starte upload ..."
        chmod go+r $LBAC_INSTITUTION_MD5.* 
        scp -p $LBAC_INSTITUTION_MD5.asc.sig $SCP_ADDR
        scp -p $LBAC_INSTITUTION_MD5.$LBAC_CLOUD $SCP_ADDR

	popd >/dev/null
}

#
#==========================================================
#
# MAIN
#
p=`dirname $0`
LBAC_REPO=`realpath $p/../..`
LBAC_CLOUD=$1
BATCH=""
if [ x$LBAC_CLOUD = "x" ] ; then
    error "Usage: package.sh CLOUDNAME [MASTER | AUTO]"
fi

TMP_RESULT=/tmp/lbac_result
LBAC_CONFIG=/tmp/lbac_config
export LBAC_CA_DIR="$LBAC_REPO/config/$LBAC_CLOUD/CA"
. $LBAC_CA_DIR/cloud.cfg

pushd $LBAC_REPO > /dev/null
cleanTmp
getResources

#  . config/CA/lbac_ca.cfg
#  mkdir -p $LBAC_REPO/config/$LBAC_CLOUD
. config/$LBAC_CLOUD/CA/cloud.cfg

case $2 in
    MASTER)
        dialog_SELECT_CONFIG
        dialog_MASTER
        genPackage
        ;;
    MASTERBATCH)
        BATCH="--batch"
        tmp=`grep $LBAC_CLOUD $LBAC_REPO/util/test/etc/cloudnodes.txt | grep MASTER | cut -d" " -f2`
        tmp="$LBAC_REPO/config/nodes/$tmp.sh.asc"
        if [ -f $tmp ] ; then
            decryptConfig $tmp
            . $LBAC_CONFIG
            mv $tmp $LBAC_REPO/config/$LBAC_CLOUD
            setupMaster
            genPackage
        fi
        ;;
    AUTO)
        autoPackage
        ;;
    AUTOBATCH)
        BATCH="--batch"
        autoPackage
        ;;
    *)
        . $LBAC_REPO/config/$LBAC_CLOUD/master.sh || error "Master config not found"
        dialog_SELECT_CONFIG
        genPackage
        ;;
esac


popd >/dev/null
