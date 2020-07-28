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
LBAC_EXPECTED_CONFIG_VERSION=4
LBAC_CONFIG=etc/config.sh

LBAC_SSL_KEYFILE=lbac_cert.key
LBAC_SSL_PWFILE=lbac_cert.passwd

LBAC_OFFICIAL_CERT=official_cert.pem
LBAC_OFFICIAL_KEYFILE=official_cert.key
LBAC_OFFICIAL_PWFILE=official_cert.passwd

LBAC_M4=dist/etc/config_m4.inc
LBAC_PGSQL_PORT_ENABLE="dnl"
LBAC_SOLR_PORT_ENABLE="dnl"
LBAC_TOMEE_PORT_ENABLE="dnl"
LBAC_HSTS_ENABLE="dnl"
LBAC_CLOUD_MODE="AUTO";
LBAC_SKIP_PREINSTALL="OFF"
#
#==========================================================
#
# configure the proxy 
#
function configProxy {

    # update current certificates and CRLs
    $LBAC_DATASTORE/dist/bin/updateCloud.sh install

}

#
# copy config file into dist directory
# Note: This function must be run prior to mfour()!
#
function copyConfig {

	# directory dist/etc already created by createProperties
	cp $LBAC_DATASTORE/$LBAC_CONFIG $LBAC_DATASTORE/dist/etc
}

#
# create a properties file
#
function createProperties {
	# LBAC_SSL_PWFILE copied already by keySetup
	LBAC_KEYSTORE_PASSWORD=`cat $LBAC_DATASTORE/dist/etc/$LBAC_CLOUD/$LBAC_CLOUD.keypass`
	LBAC_TRUSTSTORE_PASSWORD=`cat $LBAC_DATASTORE/dist/etc/$LBAC_CLOUD/$LBAC_CLOUD.trustpass`

	mkdir -p $LBAC_DATASTORE/dist/etc
	cat <<EOF > $LBAC_DATASTORE/dist/etc/lbac_properties.xml
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
<entry key="SecureWebClient.KEYSTORE_TYPE">JKS</entry>
<entry key="SecureWebClient.KEYSTORE_PATH">/usr/local/tomee/conf</entry>
<entry key="SecureWebClient.KEYSTORE_PASSWORD">$LBAC_KEYSTORE_PASSWORD</entry>
<entry key="SecureWebClient.TRUSTSTORE_TYPE">JKS</entry>
<entry key="SecureWebClient.TRUSTSTORE_PATH">/usr/local/tomee/conf</entry>
<entry key="SecureWebClient.TRUSTSTORE_PASSWORD">$LBAC_TRUSTSTORE_PASSWORD</entry>
<entry key="SecureWebClient.SSL_PROTOCOL">TLSv1.2</entry>
<!-- key alias -->
<entry key="LBAC_INTERNET_FQHN">$LBAC_INTERNET_FQHN</entry>
</properties>
EOF

}

#
# set up the pkcs12 keystore
#
function keySetup {
        # keystore password
	cp "$LBAC_DATASTORE/etc/$LBAC_SSL_PWFILE" "$LBAC_DATASTORE/dist/etc/$LBAC_CLOUD/$LBAC_CLOUD.keypass"

        #
	# store unencrypted keys for proxy 
        # according to http://httpd.apache.org/docs/2.0/ssl/ssl_faq.html#removepassphrase
        # this is as secure (not more, not less) as using the SSLPassPhraseDialog directive
        #
	openssl pkey -in "$LBAC_DATASTORE/etc/$LBAC_SSL_KEYFILE" \
                  -passin "file:$LBAC_DATASTORE/etc/$LBAC_SSL_PWFILE" \
                  -out "$LBAC_DATASTORE/dist/etc/$LBAC_CLOUD/$LBAC_CLOUD.key" 

	# OpenSSL-Bug: passin and passout must not be identical files!
	openssl pkcs12 -export -in "$LBAC_DATASTORE/dist/etc/$LBAC_CLOUD/$LBAC_CLOUD.cert" \
	  -inkey "$LBAC_DATASTORE/dist/etc/$LBAC_CLOUD/$LBAC_CLOUD.key" \
	  -passin "file:$LBAC_DATASTORE/etc/$LBAC_SSL_PWFILE" \
	  -passout "file:$LBAC_DATASTORE/dist/etc/$LBAC_CLOUD/$LBAC_CLOUD.keypass" \
	  -out "$LBAC_DATASTORE/dist/etc/$LBAC_CLOUD/$LBAC_CLOUD.pkcs12"

	# JKS will be set up inside Docker container
	# because we do not want JRE on Docker host

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
                  "$LBAC_DATASTORE/dist/etc/$LBAC_CLOUD/$LBAC_CLOUD.chain" \
                  > "$LBAC_DATASTORE/dist/etc/$LBAC_OFFICIAL_CERT"

		openssl pkey -in "$LBAC_DATASTORE/dist/etc/$LBAC_CLOUD/$LBAC_CLOUD.key" \
		  -passin "file:$LBAC_DATASTORE/dist/etc/$LBAC_CLOUD/$LBAC_CLOUD.keypass" \
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
	mkdir -p "$LBAC_DATASTORE/data/htdocs"
	mkdir -p "$LBAC_DATASTORE/data/solr"
	mkdir -p "$LBAC_DATASTORE/data/ui"
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
define(\`LBAC_INSTITUTION',\`$LBAC_INSTITUTION')dnl
define(\`LBAC_INSTITUTION_MD5',\`$LBAC_INSTITUTION_MD5')dnl
define(\`LBAC_INSTITUTION_SHORT',\`$LBAC_INSTITUTION_SHORT')dnl
define(\`LBAC_MANAGER_EMAIL',\`$LBAC_MANAGER_EMAIL')dnl
define(\`LBAC_GPG_IDENTITY',\`$LBAC_GPG_IDENTITY')dnl
define(\`LBAC_INTRANET_FQHN',\`$LBAC_INTRANET_FQHN')dnl
define(\`LBAC_INTERNET_FQHN',\`$LBAC_INTERNET_FQHN')dnl
define(\`LBAC_MASTER_SKIP',\`$LBAC_MASTER_SKIP')dnl
define(\`LBAC_MASTER_NODE_ID', \`$LBAC_MASTER_NODE_ID')dnl
define(\`LBAC_MASTER_URL', \`$LBAC_MASTER_URL')dnl
define(\`LBAC_MASTER_INSTITUTION', \`$LBAC_MASTER_INSTITUTION')dnl
define(\`LBAC_NODE_ID',\`$LBAC_NODE_ID')dnl
define(\`LBAC_NODE_RANK',\`$LBAC_NODE_RANK')dnl
define(\`LBAC_PGSQL_PORT_ENABLE',\`$LBAC_PGSQL_PORT_ENABLE')dnl
define(\`LBAC_SOLR_PORT_ENABLE',\`$LBAC_SOLR_PORT_ENABLE')dnl
define(\`LBAC_TOMEE_PORT_ENABLE',\`$LBAC_TOMEE_PORT_ENABLE')dnl
define(\`LBAC_HSTS_ENABLE',\`$LBAC_HSTS_ENABLE')dnl
define(\`LBAC_PRIMARY_CLOUD',\`$LBAC_CLOUD')dnl
EOF
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
# test if a previous installation of LBAC exists
# and act accordingly:
# - possibly shut down node
# - backup and make database dump
#
function preInstallTest {
    if [ -f "$LBAC_DATASTORE/dist/bin/setupROOT.sh" ] ;then
        LBAC_OLDINSTALL_AVAIL=TRUE
        echo "  scheduling snapshot / backup ..." 

        sudo "$LBAC_DATASTORE/dist/bin/setupROOT.sh" snapshot

        echo "  shutting down containers ..."
        sudo "$LBAC_DATASTORE/dist/bin/setupROOT.sh" shutdown
        
	if test -e $LBAC_DATASTORE/dist/dirty ; then
		rm -rf $LBAC_DATASTORE/dist
	fi
    else
        echo "  No previous installation found":
        LBAC_OLDINSTALL_AVAIL=FALSE
    fi
}

#
# extract the archive from this file and call all the 
# other functions
#
function setup {
	cd $LBAC_DATASTORE

        if test $LBAC_SKIP_PREINSTALL = "OFF" ; then
            echo "Checking of previous installation ..."
            preInstallTest
            echo "Done."
        else
            echo "Skipping preinstall test"
        fi

	echo "Extracting archive ... "
	cat $1 | uudecode | tar -xzf -
        chmod -R +x $LBAC_DATASTORE/dist/bin
        LBAC_CLOUD=`cat $LBAC_DATASTORE/dist/etc/primary.cfg`

	echo "Key setup ... "
	keySetup

	echo "Preparing configuration ... "
	createProperties
	copyConfig

        echo "Config proxy ..."
        configProxy

        echo "M4 ..."
	mfour 
	find dist/ -type f -name "*.m4" -exec /bin/bash -c preprocess {} \;
	echo "Done."

        makeDataDir

        echo "Configuring root access,  please provide authorization ..."
        sudo "$LBAC_DATASTORE/dist/bin/setupROOT.sh" initROOT "$LBAC_DATASTORE" "$USER"

        echo "Doing superuser actions ..."
        sudo "$LBAC_DATASTORE/dist/bin/setupROOT.sh" remove 
        sudo "$LBAC_DATASTORE/dist/bin/setupROOT.sh" buildPgChem
        sudo "$LBAC_DATASTORE/dist/bin/setupROOT.sh" setPermissions
        sudo "$LBAC_DATASTORE/dist/bin/setupROOT.sh" installInit
        sudo "$LBAC_DATASTORE/dist/bin/setupROOT.sh" installCron
        sudo "$LBAC_DATASTORE/dist/bin/setupROOT.sh" postInstall
        sudo "$LBAC_DATASTORE/dist/bin/setupROOT.sh" start

	echo "Setup is finished" 
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
            LBAC_SOLR_PORT_ENABLE=" "
            LBAC_TOMEE_PORT_ENABLE=" "
            ;;
        --help)
            echo "Usage: setup.sh [--debug] [--noproxy] [--proxy] [--standalone]"
            echo 
            echo "  --debug            enable debugging (i.e. open ports)"
            echo "  --noproxy          disable proxy (requires manual proxy configuration)"
            echo "  --proxy            enable proxy where manual configuration is default"
            echo "  --skip-preinstall  skip preinstallation procedure (backup, shutdown, ...)"
            echo "  --standalone       configure node to run standalone"
            echo 
            echo "Please consult manual for further information"
            exit 1
            ;;
        --skip-preinstall)
            LBAC_SKIP_PREINSTALL="ON"
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
exit
#
#===========================================================
#
# base64 encoded installation package goes here
#
