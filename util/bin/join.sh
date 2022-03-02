#!/bin/bash
#
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
#==========================================================
#
#
LBAC_SSL_KEYFILE=lbac_cert.key
LBAC_SSL_PWFILE=lbac_cert.passwd
#
#
#

function cleanUp {
    rm $TMP_RESULT
}

function dialogCheckCert {
        tmp=`sha256sum etc/$CLOUD/chain.pem | cut -d' ' -f1`
        dialog --backtitle "Join Cloud" \
          --yesno "Please compare the checksum of the CA certificate:

$tmp 

Does this checksum match the official checksum?" 15 72

        case $? in
        0)
                echo $CLOUD 
                ;;
        *)
                # rollback
                rm -r etc/$CLOUD
                error "Certificate mismatch"
                ;;
        esac

        openssl verify -CAfile etc/$CLOUD/chain.pem etc/$CLOUD/devcert.pem || error "Invalid developer certificate"

        # ToDo: crl check?
}

function dialogDownload {
        tmp=$1
        dialog --backtitle "Join Cloud" \
          --inputbox "Please specify download address for additional cloud" \
          15 72 "URL" 2>$TMP_RESULT || error "Aborted"
        DOWNLOAD_URL=`cat $TMP_RESULT`
        downloadFunc
}

#
# The header of the encrypted configuration file contains 
# the name of the institution and the "X509v3 Subject Key Identifier"
# of the key used to encrypt the configuration file. Both
# information is needed by the distributor to a) select and 
# b) decrypt the configuration file in the process of creating 
# the individualized software package.
#
function dialogEncrypt {

        dialog --backtitle "Cloud Resource & Information Management System (CRIMSy)" \
          --msgbox "Die Konfigurationsdatei wird jetzt verschlüsselt. Bitte senden Sie dem Cloud-Administrator die Datei $LBAC_DATASTORE/etc/$CLOUD/config.sh.asc per Email zu. Sie erhalten dann von ihm Nachricht, wann und wie Sie mit dem Installationsprozess fortfahren können. " \
        15 72
        case $? in
                0)
                        encrypt
                        ;;
                *)
                        error "Aborted."
                        ;;
        esac

}

function downloadFunc {
        curl --silent --output etc/$CLOUD/chain.pem $DOWNLOAD_URL/chain.pem
        curl --silent --output etc/$CLOUD/devcert.pem $DOWNLOAD_URL/devcert.pem
        curl --silent --output etc/$CLOUD/$CLOUD.crl $DOWNLOAD_URL/crl.pem
}

function encrypt {
    rm -f etc/$CLOUD/config.sh.asc && \
    cat << EOF > "etc/$CLOUD/config.sh.asc" 
#
# LBAC_INSTITUTION=$LBAC_INSTITUTION
# CERTIFICATE_ID=`openssl x509 -in etc/$CLOUD/devcert.pem -text | \
          grep -A1 "X509v3 Subject Key Identifier" | tail -1 | tr -d $' \n'`
# `date`
#
# ----- SMIME ENCRYPTED CONFIG BEGIN -----
`openssl smime -encrypt -binary -outform PEM -aes-256-cbc \
  -in etc/config.sh etc/$CLOUD/devcert.pem`
# ----- SMIME ENCRYPTED CONFIG END -----
EOF

}

function error {
        cleanUp
        echo $1
        exit 1
}

function printHelp {
BOLD=$'\x1b[1m'
REGULAR=$'\x1b[0m'
cat <<EOF
${BOLD}NAME${REGULAR}
    join.sh

${BOLD}SYNOPSIS${REGULAR}
    testSetup.sh [-r|request CLOUD] [-h|--help] [-j|--join CLOUD] 
        [-l|--leave CLOUD] [-u|--url URL] 

${BOLD}DESCRIPTION${REGULAR}
    no description available

${BOLD}OPTIONS${REGULAR}
-j|--join CLOUD
    no description available

EOF

}

function saveCloudInfo {

        echo /$CLOUD$';/d\ni\n'$CLOUD$';'$DOWNLOAD_URL$'\n.\nw\nq\n' | \
            ed etc/clouds.cfg
        rm etc/$CLOUD/cloud.tmp
}

function saveCloudTmp {
    echo "$CLOUD;$DOWNLOAD_URL" > etc/$CLOUD/cloud.tmp
}

function joinFunc {

    if [ -n "$REQUEST" ] ; then
        CLOUD="$REQUEST"
        mkdir -p etc/$CLOUD
        if [ -z "$DOWNLOAD_URL" ] ; then
                dialogDownload
                dialogCheckCert
                dialogEncrypt
                saveCloudTmp
                cleanUp
        else 
            downloadFunc
            encrypt
            saveCloudTmp
            cleanUp
        fi
        exit 0
    fi

    if [ -n "$JOIN" ] ; then
        CLOUD="$JOIN"
        DOWNLOAD_URL=`cat etc/$CLOUD/cloud.tmp | cut -d';' -f2`
        if [ -n "$DOWNLOAD_URL" ] ; then
            saveCloudInfo
            dist/bin/update.sh proxy
            dist/bin/update.sh ui
            dist/bin/update.sh db
            cleanUp
        else 
            error "Please run 'join.sh --request $CLOUD' first."
        fi
        exit 0
    fi

    if [ -n "$LEAVE" ] ; then
        CLOUD="$LEAVE"
        error "Leaving not implemented"
    fi
}
#
#==========================================================
#
if test ! -r $HOME/.lbac ; then
        echo "CRIMSy ist nicht richtig konfiguriert"
        exit 1
fi
. $HOME/.lbac
if test "!" "(" -n "$LBAC_DATASTORE" -a -d "$LBAC_DATASTORE"   -a -w "$LBAC_DATASTORE" ")" ; then
        echo "LBAC_DATASTORE ist nicht definiert oder nicht schreibbar"
        exit 1
fi
. $LBAC_DATASTORE/etc/config.sh > /dev/null

pushd $LBAC_DATASTORE >/dev/null


TMP_RESULT=`mktemp /tmp/lbac_join.XXXXXX`

REQUEST=''
JOIN=''
LEAVE=''
DOWNLOAD_URL=''

GETOPT=$(getopt -o 'hj:l:r:u:' --longoptions 'help,join:,leave:,request:,url:' -n 'testSetup.sh' -- "$@")
if [ $? -ne 0 ]; then
        echo 'Error in commandline evaluation. Terminating...' >&2
        exit 1
fi

eval set -- "$GETOPT"
unset GETOPT

while true ; do
    case "$1" in
        '-h'|'--help')
            printHelp
            exit 0
            ;;
        '-j'|'--join')
            JOIN="$2"
            shift 2
            continue
            ;;
        '-l'|'--leave')
            LEAVE="$2"
            shift 2
            continue
            ;;
        '-r'|'--request')
            REQUEST="$2"
            shift 2
            continue
            ;;
        '-u'|'--url')
            DOWNLOAD_URL="$2"
            shift 2
            continue
            ;;
        '--')
            shift
            break;
            ;;
        *)
            error
    esac
done

joinFunc

popd > /dev/null

