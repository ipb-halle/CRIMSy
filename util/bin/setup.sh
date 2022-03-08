#!/bin/bash
#
# Cloud Resource & Information Management System (CRIMSy)
# Setup
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
#
# 
#==========================================================
#
LBAC_EXPECTED_CONFIG_VERSION=8
LBAC_CONFIG=etc/config.sh

LBAC_ADMIN_PWFILE=admin.passwd
LBAC_DB_PWFILE=db.passwd

LBAC_SSL_KEYFILE=lbac_cert.key
LBAC_SSL_PWFILE=lbac_cert.passwd

LBAC_OFFICIAL_CERT=official_cert.pem
LBAC_OFFICIAL_KEYFILE=official_cert.key
LBAC_OFFICIAL_PWFILE=official_cert.passwd

LBAC_M4=dist/etc/config_m4.inc
LBAC_PGSQL_PORT_ENABLE="dnl"
LBAC_TOMEE_PORT_ENABLE="dnl"
LBAC_HSTS_ENABLE="dnl"
LBAC_CLOUD_MODE="AUTO";
LBAC_SKIP_SNAPSHOT="OFF"
#
#==========================================================
#

function getVersion {
    #
    # Note: not all levels of automatic updates are 
    # fully implemented yet. Major level updates will 
    # certainly require manual intervention.
    #
    REVISION=`cat $LBAC_DATASTORE/dist/etc/revision_info.cfg | tail -1 | cut -d';' -f1`
    MAJOR=`echo $REVISION | cut -d. -f1`
    MINOR=`echo $REVISION | cut -d. -f2`
    case $LBAC_UPDATE_LEVEL in
        NONE)
            LBAC_VERSION=$REVISION
            ;;
        PATCH)
            LBAC_VERSION=$MAJOR.$MINOR
            ;;
        MINOR)
            LBAC_VERSION=$MAJOR
            ;;
        MAJOR)
            LBAC_VERSION=LATEST 
            ;;
        *)
            error "Undefined update level"
            ;;
    esac
}

#
# set up the pkcs12 keystore
#
function keySetup {
	#
	# Primitive test for official certificate, private key and key password
	# future revisisions might recognize if the official certificate 
	# uses the same private key as the LBAC certificate.
	#
	if test -s "$LBAC_DATASTORE/etc/$LBAC_OFFICIAL_CERT" \
	  -a -s "$LBAC_DATASTORE/etc/$LBAC_OFFICIAL_KEYFILE" \
	  -a -s "$LBAC_DATASTORE/etc/$LBAC_OFFICIAL_PWFILE" ; then

		echo "INFO: Using official certificate"
		cp "$LBAC_DATASTORE/etc/$LBAC_OFFICIAL_CERT" "$LBAC_DATASTORE/dist/etc"

		openssl pkey -in "$LBAC_DATASTORE/etc/$LBAC_OFFICIAL_KEYFILE" \
		  -passin "file:$LBAC_DATASTORE/etc/$LBAC_OFFICIAL_PWFILE" \
		  -out "$LBAC_DATASTORE/dist/etc/$LBAC_OFFICIAL_KEYFILE"

                LBAC_OFFICIAL_IS_VALID="YES"
	else
		echo
		echo "***************************************************************"
		echo "*                                                             *"
		echo "* WARNING: Could not find an official certificate. Falling    *"
		echo "* back to the LBAC certificate! To replace it, you may re-run *"
		echo "* the installer after installing an official certificate.     *"
                if test $LBAC_PROXY_HSTS = "ON" ; then 
		    echo "*                                                             *"
		    echo "* Cannot enable HSTS on fallback certificates.                *"
                fi
		echo "*                                                             *"
		echo "* Please consult the handbook.                                *"
		echo "*                                                             *"
		echo "***************************************************************"
		echo 
		echo "Installing fallback certificates ... "
                cat "$LBAC_DATASTORE/dist/etc/$LBAC_CLOUD/$LBAC_CLOUD.cert" \
                  "$LBAC_DATASTORE/dist/etc/$LBAC_CLOUD/chain.pem" \
                  > "$LBAC_DATASTORE/dist/etc/$LBAC_OFFICIAL_CERT"

		openssl pkey -in "$LBAC_DATASTORE/etc/lbac_cert.key" \
		  -passin "file:$LBAC_DATASTORE/etc/lbac_cert.passwd" \
		  -out "$LBAC_DATASTORE/dist/etc/$LBAC_OFFICIAL_KEYFILE"

                LBAC_OFFICIAL_IS_VALID="NO"
	fi
}

#
# Initialize Directories
# NOTE: directories must be chowned in sudo part!
#
function makeDataDir {
	mkdir -p "$LBAC_DATASTORE/data/db"
        mkdir -p "$LBAC_DATASTORE/data/proxy_conf"
	mkdir -p "$LBAC_DATASTORE/data/ui"
        mkdir -p "$LBAC_DATASTORE/data/ui_conf"
}

#
# Prepare m4 configuration
#
function mfour {
        . "$LBAC_DATASTORE/dist/etc/$LBAC_CLOUD/master.sh"

        case $LBAC_CLOUD_MODE in
            STANDALONE)
                LBAC_MASTER_SKIP="dnl"
            ;;
        esac

        #
        # Enable HSTS only with valid official certificate
        #
        if test $LBAC_PROXY_HSTS = "ON" -a $LBAC_OFFICIAL_IS_VALID = 'YES' ; then
            echo "Enabling HSTS ..."
            LBAC_HSTS_ENABLE=" "
        fi

	cat > "$LBAC_DATASTORE/$LBAC_M4" <<EOF
dnl
dnl Automatically generated. Please consider  
dnl running configure.sh or install.sh instead of
dnl editing this file!
dnl
define(\`LBAC_DATASTORE',\`$LBAC_DATASTORE')dnl
define(\`LBAC_IMAGE_REGISTRY',\`$LBAC_IMAGE_REGISTRY')dnl
define(\`LBAC_MANAGER_EMAIL',\`$LBAC_MANAGER_EMAIL')dnl
define(\`LBAC_INTRANET_FQHN',\`$LBAC_INTRANET_FQHN')dnl
define(\`LBAC_INTERNET_FQHN',\`$LBAC_INTERNET_FQHN')dnl
define(\`LBAC_PGSQL_PORT_ENABLE',\`$LBAC_PGSQL_PORT_ENABLE')dnl
define(\`LBAC_TOMEE_PORT_ENABLE',\`$LBAC_TOMEE_PORT_ENABLE')dnl
define(\`LBAC_HSTS_ENABLE',\`$LBAC_HSTS_ENABLE')dnl
define(\`LBAC_DB_PASSWD',\``cat $LBAC_DATASTORE/etc/$LBAC_DB_PWFILE`')dnl
define(\`LBAC_VERSION',\`$LBAC_VERSION')dnl
EOF
}


#
# extract the archive from this file and call all the 
# other functions
#
function setup {
	cd $LBAC_DATASTORE

        if test $LBAC_SKIP_SNAPSHOT = "OFF" ; then
            snapshot 
            echo "Done."
        else
            echo "Skipping snapshot / backup"
        fi

        echo "shutting down instance ..."
        sudo "$LBAC_DATASTORE/dist/bin/setupROOT.sh" shutdown

        LBAC_CLOUD=`cat $LBAC_DATASTORE/etc/primary.cfg`

	echo "Preparing configuration ... "
        makeDataDir
        getVersion

        echo "Key setup ... "
        keySetup

        echo "M4 ..."
	mfour 
        m4 dist/etc/docker-compose.yml.m4 > dist/docker-compose.yml
        m4 dist/etc/lbac.service.m4 > dist/etc/lbac.service

	echo "Done."

        echo "Configuring root access,  please provide authorization ..."
        sudo "$LBAC_DATASTORE/dist/bin/setupROOT.sh" initROOT "$LBAC_DATASTORE" "$USER"
        sudo "$LBAC_DATASTORE/dist/bin/setupROOT.sh" remove 

        dist/bin/update.sh ui
        dist/bin/update.sh proxy
        dist/bin/update.sh db

        echo "Doing superuser actions ..."
        sudo "$LBAC_DATASTORE/dist/bin/setupROOT.sh" installInit
        sudo "$LBAC_DATASTORE/dist/bin/setupROOT.sh" installCron
        sudo "$LBAC_DATASTORE/dist/bin/setupROOT.sh" start

	echo "Setup is finished" 
}

#
# test if a previous installation of LBAC exists
# and make a full backup 
#
function snapshot {
    if [ -d "$LBAC_DATASTORE/data/db" ] ;then
        echo "  scheduling snapshot / backup ..." 
        sudo "$LBAC_DATASTORE/dist/bin/update.sh" snapshot INSTALL
    else
        echo "  No previous installation found":
    fi
}

function error {
    echo $1
    exit 1
}

#
#===========================================================
#
# MAIN
#
test `id -u` -eq 0 && error "This script must NOT called as root"

if test -r $HOME/.lbac ; then
	. $HOME/.lbac
else
	error "configuration missing (~/.lbac)."
fi

if test -n "$LBAC_DATASTORE" -a -r "$LBAC_DATASTORE/$LBAC_CONFIG" ; then
	. "$LBAC_DATASTORE/$LBAC_CONFIG"
else 
	error "configuration missing ($LBAC_CONFIG)"
fi

if test "$LBAC_CONFIG_VERSION" != "$LBAC_EXPECTED_CONFIG_VERSION" ; then
    error "configuration version mismatch. Please re-run LBAC_DATASTORE/bin/configure.sh"
fi

for i in $* ; do
    case "$i" in
        
        --debug)
            echo "Activating debug mode ..."
            LBAC_PGSQL_PORT_ENABLE=" "
            LBAC_TOMEE_PORT_ENABLE=" "
            ;;
        --help)
            echo "Usage: setup.sh [--debug] [--skip-snapshot] [--standalone]"
            echo 
            echo "  --debug            enable debugging (i.e. open ports)"
            echo "  --skip-snapshot    skip preinstallation procedure (snapshot / backup)"
            echo "  --standalone       configure node to run standalone"
            echo 
            echo "Please consult manual for further information"
            exit 1
            ;;
        --skip-snapshot)
            LBAC_SKIP_SNAPSHOT="ON"
            ;;
        --standalone)
            echo "Activating standalone mode ..."
            echo "PLEASE NOTE: This option might not work on updates."
            echo "PLEASE NOTE: Automatic upgrade to cloud mode not possible."
            LBAC_CLOUD_MODE="STANDALONE"
            ;;
        *)
            error "Unknown option $i; try --help"
            ;;
    esac
done

setup `realpath $0`
