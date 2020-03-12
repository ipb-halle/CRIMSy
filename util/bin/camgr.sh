#!/bin/bash
#
# CA-Management-Skript
# Copyright 2017 Leibniz-Institut f. Pflanzenbiochemie 
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
# clean up temporary files and the terminal screen
#
function cleanUp {
	rm $TMP_RESULT
}

#
# Extracts the common name from a subject line
#
function commonName {
	echo $1 | sed -e s./.\\n.g | grep "CN=" | \
	  cut -d= -f2- | tr -d $'\n'
}

function createConfig {
	cat <<EOF > ca.cfg
   dir = \$ENV::CA_DIR

#
# CA, Policy, CSR, CRL
#
[ca]
   default_ca = ca
   certs = \$dir/certs
   crl_dir = \$dir/crl
   database = \$dir/index.txt
   new_certs_dir = \$dir/certs
   certificate = \$dir/cacert.pem
   serial = \$dir/serial.txt
   crl = \$dir/crl.pem
   private_key = \$dir/cacert.key
   RANDFILE = \$dir/.rand
   default_days = 1825 
   default_crl_hours = 240
   default_md = sha256
   preserve = no
   policy = policy_default
   nameopt = default_ca
   certopt = default_ca
   copy_extensions = none
   crl_extensions = v3_crl
   email_in_dn = no
   unique_subject = no

[policy_default]
   countryName = supplied
   stateOrProvinceName = supplied
   localityName = supplied
   organizationName = supplied
   organizationalUnitName = optional
   commonName = supplied
   emailAddress = optional

[req]
    distinguished_name = req_distinguished_name

[req_distinguished_name]

[v3_crl]
   authorityKeyIdentifier = keyid:always,issuer:always
   crlDistributionPoints=URI:$CA_CRL

#
# CA
#
[v3_ca]
   basicConstraints = critical, CA:true, pathlen:1
   nsComment = "$CA_CN" 
   nsCertType = sslCA, emailCA
   keyUsage = cRLSign, keyCertSign
   subjectKeyIdentifier = hash
   authorityKeyIdentifier = keyid:always,issuer:always
   subjectAltName = email:copy
   issuerAltName = issuer:copy
   crlDistributionPoints=URI:$CA_CRL

#
# SubCA certificates
#
[v3_subCA]
   basicConstraints = critical, CA:true
   nsComment = "SubCA certified by $CA_CN"
   nsCertType = sslCA, emailCA
   keyUsage = cRLSign, keyCertSign
   subjectKeyIdentifier = hash
   authorityKeyIdentifier = keyid:always,issuer:always
   subjectAltName = email:copy
   issuerAltName = issuer:copy
   crlDistributionPoints=URI:$CA_CRL

[v3_reqSubCA]
   basicConstraints = critical, CA:true
   nsComment = "SubCA  $CA_CN"
   nsCertType = sslCA, emailCA
   keyUsage = cRLSign, keyCertSign
   subjectKeyIdentifier = hash
   subjectAltName = email:copy
   crlDistributionPoints=URI:$CA_CRL

#
# Server
#
[v3_reqServer]
   basicConstraints = CA:FALSE
   nsCertType = client, server
   keyUsage = critical, digitalSignature, keyEncipherment, keyAgreement
   subjectAltName = @alt_names
   crlDistributionPoints=URI:$CA_CRL

[v3_server]
   basicConstraints = CA:FALSE
   nsComment = "Certificate by $CA_CN"
   nsCertType = client, server
   keyUsage = critical, digitalSignature, keyEncipherment, keyAgreement
   subjectKeyIdentifier = hash
   authorityKeyIdentifier = keyid,issuer:always
   subjectAltName = @alt_names 
   issuerAltName = issuer:copy
   extendedKeyUsage = clientAuth, serverAuth

#
# Developer certificates
#
[v3_devCert]
   basicConstraints = CA:FALSE
   nsComment = "Certificate by $CA_CN"
   nsCertType = client
   keyUsage = critical, digitalSignature
   extendedKeyUsage = codeSigning, emailProtection
   subjectKeyIdentifier = hash
   authorityKeyIdentifier = keyid,issuer:always
   issuerAltName = issuer:copy
   subjectAltName = $TMP_SUBJECT_ALT_NAME
   crlDistributionPoints=URI:$CA_CRL

[v3_reqDevCert]
   basicConstraints = CA:FALSE
   keyUsage = critical, digitalSignature
   extendedKeyUsage = codeSigning, emailProtection
   subjectKeyIdentifier = hash
   subjectAltName = $TMP_SUBJECT_ALT_NAME

[alt_names]
$TMP_SUBJECT_ALT_NAMES

EOF

}

#
# Create a self-signed certificate and a truststore
# as well as the complete CA directory and file infrstructure
#
function createCA {
	TMP_SUBJECT_ALT_NAME="email:copy"
        TMP_SUBJECT_ALT_NAMES=''

	CA_COUNTRY="$TMP_COUNTRY"
	CA_STATE="$TMP_STATE"
	CA_PLACE="$TMP_PLACE"
	CA_ORG="$TMP_ORG"
	CA_OU="$TMP_OU"
	CA_EMAIL="$TMP_EMAIL"
	CA_CN="$TMP_CN"

	createConfig

	uuidgen -r | tr -d $'\n' > cacert.passwd

	CA_SUBJECT="/C=$CA_COUNTRY/ST=$CA_STATE/L=$CA_PLACE"
	CA_SUBJECT="$CA_SUBJECT/O=$CA_ORG/OU=$CA_OU"
	CA_SUBJECT="$CA_SUBJECT/CN=$CA_CN/emailAddress=$CA_EMAIL"

	openssl req -x509 -outform PEM -out cacert.pem \
	  -new -keyout cacert.key -newkey rsa:4096 -sha256 -utf8 \
	  -passout "file:cacert.passwd" -days 3650 \
	  -config ca.cfg -extensions v3_ca \
	  -subj "$CA_SUBJECT" 

        # CA directories and infrastructure
	mkdir -p req
	mkdir -p cloud
	mkdir -p devcert
	mkdir -p crl
	mkdir -p certs
        mkdir -p revoked
        rm truststore*
	touch index.txt
	touch index.txt.attr
        echo $CA_SUBJECT > chain.txt
        cat cacert.pem >> chain.txt

	echo "01" > serial.txt

        # name, subject hash, fingerprint, cacert URL, CRL URL (tab separated)
        tmp=`openssl x509 -in cacert.pem -subject_hash -noout | tr -d $'\n'`
        echo -n $'ROOT\t'$tmp$'\t' > addresses.txt
        tmp=`openssl x509 -in cacert.pem -fingerprint -noout | cut -d= -f2 | tr -d $':\n'`
        echo $tmp$'\t'$DOWNLOAD_URL/cacert.pem$'\t'$CA_CRL >> addresses.txt

        # Truststore
        uuidgen -r | tr -d $'\n'  > truststore.passwd
        TRUST_PASSWD=`cat truststore.passwd`
        openssl ca -updatedb \
          -passin file:cacert.passwd -config ca.cfg

        keytool -importcert -keystore truststore -storepass $TRUST_PASSWD \
          -noprompt -trustcacerts -file cacert.pem -alias "$CA_CN"

	writeConfig
}

function createSubCA {
	TMP_SUBJECT_ALT_NAME="email:copy"
        TMP_SUBJECT_ALT_NAMES=''

	CA_COUNTRY="$TMP_COUNTRY"
	CA_STATE="$TMP_STATE"
	CA_PLACE="$TMP_PLACE"
	CA_ORG="$TMP_ORG"
	CA_OU="$TMP_OU"
	CA_EMAIL="$TMP_EMAIL"
	CA_CN="$TMP_CN"

	createConfig

	uuidgen -r | tr -d $'\n' > cacert.passwd

	CA_SUBJECT="/C=$CA_COUNTRY/ST=$CA_STATE/L=$CA_PLACE"
	CA_SUBJECT="$CA_SUBJECT/O=$CA_ORG/OU=$CA_OU"
	CA_SUBJECT="$CA_SUBJECT/CN=$CA_CN/emailAddress=$CA_EMAIL"

	openssl req -outform PEM -out cacert.req \
	  -new -keyout cacert.key -newkey rsa:4096 -sha256 -utf8 \
	  -passout "file:cacert.passwd" -days 3650 \
	  -config ca.cfg -reqexts v3_reqSubCA \
	  -subj "$CA_SUBJECT" 

	mkdir -p req
	mkdir -p cloud
	mkdir -p devcert
	mkdir -p crl
	mkdir -p certs
        mkdir -p revoked
	touch index.txt
	touch index.txt.attr
	echo "01" > serial.txt
        echo $CA_SUBJECT > newchain.txt

	writeConfig
}

#
# Create (duplicate and import to a) truststore
#
function createTruststore {

    openssl ca -updatedb -passin file:cacert.passwd -config ca.cfg

    # "copy" this CA's truststore
    tmp=`cat truststore.passwd`
    uuidgen -r | tr -d $'\n'  > $OUTPUT.passwd
    TRUST_PASSWD=`cat $OUTPUT.passwd`

    cp truststore $OUTPUT
    keytool -storepasswd -keystore $OUTPUT -storepass $tmp \
          -new $TRUST_PASSWD

    # This is a rudimentary validity test. Should we use openssl verify instead?
    SERIAL=`grep $HASH index.cloud | tail -1 | cut -d' ' -f1`
    grep -E "^V" index.txt | cut -f4 | grep -q $SERIAL || error 'Specified certificate is invalid'

    # import a certificate into the truststore
    ALIAS=`grep $HASH index.cloud | tail -1 | cut -d' ' -f4-`
    keytool -importcert -storepass $TRUST_PASSWD \
      -trustcacerts -file cloud/$HASH.pem \
      -keystore $OUTPUT -alias "$ALIAS" || error "keytool error"
}

#
# Transform date from index.txt into readable form
#
function dateFormat {
	echo $1 | cut --output-delimiter=" " -c1-2,3-4,5-6 | \
	  xargs -l1 printf "20%s-%s-%s" | \
	  tr -d $'\n'
}

function devCert {
	TMP_SUBJECT_ALT_NAME="email:copy"
        TMP_SUBJECT_ALT_NAMES=''

        DEV_COUNTRY="$TMP_COUNTRY"
        DEV_STATE="$TMP_STATE"
        DEV_PLACE="$TMP_PLACE"
        DEV_ORG="$TMP_ORG"
        DEV_OU="$TMP_OU"
        DEV_EMAIL="$TMP_EMAIL"
        DEV_CN="$TMP_CN"

	SUBJECT="/C=$DEV_COUNTRY/ST=$DEV_STATE/L=$DEV_PLACE"
	SUBJECT="$SUBJECT/O=$DEV_ORG/OU=$DEV_OU"
	SUBJECT="$SUBJECT/CN=$DEV_CN/emailAddress=$DEV_EMAIL"

	createConfig

	SERIAL=`cat serial.txt | tr -d $'\n'`
	uuidgen -r | tr -d $'\n'  > devcert/$SERIAL.passwd

	openssl req -outform PEM -out devcert/$SERIAL.req \
	  -new -keyout devcert/$SERIAL.key -newkey rsa:4096 -sha256 -utf8 \
	  -passout "file:devcert/$SERIAL.passwd" -days 3650 \
	  -config ca.cfg -reqexts v3_reqDevCert \
	  -subj "$SUBJECT" || error

	openssl ca -verbose -config ca.cfg -extensions v3_devCert \
	  -in devcert/$SERIAL.req -out devcert/$SERIAL.pem \
	  -passin "file:cacert.passwd" || error

        #
        # The Subject Key Identifier used by clients to identify, which 
        # developer certificate has been used to encrypt the configuration. 
        # The distributor needs this to select the matching decryption key. 
        #
	id=`openssl x509 -in devcert/$SERIAL.pem -text | \
	  grep -A1 "X509v3 Subject Key Identifier" | tail -1 | tr -d $' \n'`
	ln -s $SERIAL.pem devcert/$id.pem
	ln -s $SERIAL.key devcert/$id.key
	ln -s $SERIAL.passwd devcert/$id.passwd

	DEV_CERT=devcert/$SERIAL
	writeConfig
}

#
# CA dialog
#
function dialogCA {

        dialog --backtitle "CA-Management" \
          --yesno "Create new CA; this will destroy your current CA!" 15 76 || error "Aborted."

        rm index.txt.*
        rm serial.txt*
        rm -r certs
        rm -r crl
        rm -r devcert
        rm -r cloud
        rm -r req

        dialogCert "CA Name (CN)     :" "$CA_COUNTRY" "$CA_STATE" \
          "$CA_PLACE" "$CA_ORG" "$CA_OU" "$CA_EMAIL" "$CA_CN"
        dialogDownload 'this'
        CA_CRL=$TMP_DOWNLOAD_URL/crl.pem
        DOWNLOAD_URL=$TMP_DOWNLOAD_URL
        dialogUpload

        if test -z $CLOUD ; then
            createCA
            scp cacert.pem $SCP_ADDR/cacert.pem
            scp chain.txt $SCP_ADDR/chain.txt
            scp addresses.txt $SCP_ADDR/addresses.txt
            scp truststore $SCP_ADDR/truststore
            scp truststore.passwd $SCP_ADDR/truststore.passwd
            genCRL
            sleep 10
        else
            dialogDownload '*superior*'
            wget $TMP_DOWNLOAD_URL/chain.txt
            wget $TMP_DOWNLOAD_URL/addresses.txt
            wget $TMP_DOWNLOAD_URL/truststore
            wget $TMP_DOWNLOAD_URL/truststore.passwd
            createSubCA
        fi
        writeConfig
}

function dialogCert {
        dialog --backtitle "CA-Management" \
          --title "Zertifikatsdaten" \
          --ok-label "Ok" --cancel-label "Cancel" \
          --insecure \
          --mixedform "Please entery your CA data" \
          19 72 0 \
          "Country          :"  1 2 "$2"                     1 21  3  0 0 \
          "State            :"  2 2 "$3"                     2 21 43  0 0 \
          "Location         :"  3 2 "$4"                     3 21 43 64 0 \
          "Organisation     :"  4 2 "$5"                     4 21 43 64 0 \
          "Org.-Unit (OU)   :"  5 2 "$6"                     5 21 43 64 0 \
          "Email            :"  6 2 "$7"                     6 21 43 64 0 \
          "$1"                  7 2 "$8"                     7 21 43 64 0 \
	2>$TMP_RESULT || error

	TMP_COUNTRY=`head -1 $TMP_RESULT | tail -1 | tr -d $'\n'`
	TMP_STATE=`head -2 $TMP_RESULT | tail -1 | tr -d $'\n'`
	TMP_PLACE=`head -3 $TMP_RESULT | tail -1 | tr -d $'\n'`
	TMP_ORG=`head -4 $TMP_RESULT | tail -1 | tr -d $'\n'`
	TMP_OU=`head -5 $TMP_RESULT | tail -1 | tr -d $'\n'`
	TMP_EMAIL=`head -6 $TMP_RESULT | tail -1 | tr -d $'\n'`
	TMP_CN=`head -7 $TMP_RESULT | tail -1 | tr -d $'\n'`

	dialog --backtitle "CA-Management" \
	  --yesno "Is this correct?

  Country__: $TMP_COUNTRY
  State____: $TMP_STATE
  Location_: $TMP_PLACE
  Organiz~n: $TMP_ORG
  OU_______: $TMP_OU
  Email____: $TMP_EMAIL
  CN_______: $TMP_CN" 15 76 || error "Aborted."
}


function dialogDownload {
        tmp=$1
	dialog --backtitle "CA-Management" \
	  --inputbox "Please specify download address for $tmp CA (http url):" \
	  15 72 "$DOWNLOAD_URL" 2>$TMP_RESULT || error "Aborted"
	TMP_DOWNLOAD_URL=`cat $TMP_RESULT`
}

function dialogUpload {
	dialog --backtitle "CA-Management" \
          --inputbox "Please specify secure copy upload address (including colon, e.g. user@server:/path):" \
          15 72 "$SCP_ADDR" 2>$TMP_RESULT || error "Aborted"
        SCP_ADDR=`cat $TMP_RESULT`
}

function error {
	cleanUp
	echo $1
	exit 1
}

#
# generate a new certificat from certificate request
#
function genCert {
        
	TMP_CERT_ID=`md5sum $INPUT | cut -c1-32`

        if test -f "cloud/$TMP_CERT_ID.pem" ; then
            if test -n $OUTPUT ; then
                cp cloud/$TMP_CERT_ID.pem $OUTPUT
            fi
            return
        fi
	echo $TMP_CERT_ID

	tmp=`openssl req -in $INPUT -text | sed -e '/BEGIN CERTIFICATE REQUEST/q' |
	  grep -v emailAddress | grep -Ei 'email|dns' | sed -r -e's/\s+//g'`

        echo
        echo "tmp: $tmp"
        echo
        i=0
        TMP_SUBJECT_ALT_NAMES=''
        for j in `echo $tmp | tr ',' ' ' | tr $'\n' ' '` ; do
            if echo $j | grep -i dns ; then
                i=$(($i + 1))
                k=`echo $j | sed s/DNS:/DNS.$i=/`
            else
                k=`echo $j | sed s/:/=/`
            fi
           TMP_SUBJECT_ALT_NAMES=${TMP_SUBJECT_ALT_NAMES}$k$'\n'
        done

        echo 
        echo "TMP_SUBJECT_ALT_NAMES: $TMP_SUBJECT_ALT_NAMES"
        echo 
 
	TMP_HOSTNAME=`echo "$TMP_SUBJECT_ALT_NAMES" | grep "DNS.2" | cut -d= -f2`

        if test -z "$TMP_HOSTNAME" ; then
            # CSR's from Sub-CA's do not contain DNS records
            TMP_HOSTNAME=`openssl req -in $INPUT -text | 
              grep 'Subject:' | grep -E 'CN( )?=' | 
              sed -r -e's/.*CN( )?=( )?//' - | cut -d, -f1`
        fi

        echo 
        echo "TMP_HOSTNAME: $TMP_HOSTNAME"
        echo 

        cp $INPUT $CA_DIR/req/$TMP_CERT_ID.req
	createConfig
	
	SERIAL=`cat serial.txt`
	openssl ca -verbose -config ca.cfg -extensions $EXTENSION \
	  -in $INPUT -out cloud/$TMP_CERT_ID.pem \
	  -passin "file:cacert.passwd" || error

	echo "$SERIAL `date +%s` $TMP_CERT_ID $TMP_HOSTNAME" >> index.cloud

        if test -n "$OUTPUT" ; then
            cp cloud/$TMP_CERT_ID.pem $OUTPUT
        fi
}

#
# derive action from MODE variable or get desired 
# action interactively from user
#
function getAction {
        case $MODE in
                sign)
                        ACTION=1
                        MODE='quit'
                        ;;
                trust)
                        ACTION=3
                        MODE='quit'
                        ;;
                genCRL)
                        ACTION=8
                        MODE='quit'
                        ;;
                importSubCA)
                        ACTION=11
                        MODE='quit'
                        ;;
                testRevoked)
                        ACTION=12
                        MODE='quit'
                        ;;
                quit)
                        ACTION=10
                        return
                        ;;
                *)
                        dialog --backtitle "Leibniz Bioactives Cloud CA" \
                          --menu "Please choose your option" 17 72 12 \
          1 "Sign Certificate request (Node)" \
          2 "Create Certificate (Developer)" \
          3 "Create Truststore" \
          4 "View Certificate(s)" \
          5 "Set scp upload address" \
          6 "Set download address" \
          7 "Revoke Certificate" \
          8 "Create CRL" \
          9 "Create new (Sub)CA" \
          10 "Quit" 2> $TMP_RESULT || error "Aborted"
                        ACTION=`cat $TMP_RESULT`
                        ;;
        esac
}

#
# Perform the desired action
#
function performAction {
        case $ACTION in
                1)
                        test -f "$INPUT" || error "need request file"
                        genCert 
                        ;;
                2)
                        dialogCert "Developer (CN)   :"  "$DEV_COUNTRY" "$DEV_STATE" \
                          "$DEV_PLACE" "$DEV_ORG" "$DEV_OU" "$DEV_EMAIL" "$DEV_CN"
                        devCert
                        ;;
                3)
                        test -z "$OUTPUT" -o ! -d "$OUTPUT" || error "no output file given"
                        createTruststore 
                        ;;
                4)
                        selectDialog View
                        ;;
                5)
                        dialogUpload
	                writeConfig
                        ;;
                6)
                        dialogDownload 'this'
                        CA_CRL=$TMP_DOWNLOAD_URL/crl.pem
                        DOWNLOAD_URL=$TMP_DOWNLOAD_URL
                        writeConfig
                        ;;
                7)
                        selectDialog Revoke
                        ;;
                8)
                        genCRL
                        ;;
                9)
                        dialogCA
                        ;;
                10)
                        MODE='quit'
                        echo $'\x1b[2J'
                        ;;
                11)
                        importSubCA
                        ;;
                12)
                        testRevoked
                        ;;
                *)
                        error "Invalid action"
                        ;;
        esac
}

#
# Print short help text
#
function printHelp {
BOLD=$'\x1b[1m'
REGULAR=$'\x1b[0m'
cat <<EOF
${BOLD}NAME${REGULAR}
    camgr.sh

${BOLD}SYNOPSIS${REGULAR}
    camgr.sh [-c|--cloud CLOUD] [-d|--directory DIR] [-h|--help] 
    [-H|--hash HASH] [-m|mode MODE] [-o|--output NAME] 

${BOLD}DESCRIPTION${REGULAR}
    Operate the Cloud CA or a subCA. Sign certificate requests, manage 
    certificates and generate CRLs and truststores. 
    Input and output file names should be absolute or relative to the CA 
    directory.

${BOLD}OPTIONS${REGULAR}
-c CLOUD| --cloud CLOUD
    Name of the cloud (i.e. subCA) to operate; without this option, the 
    program operates on the toplevel CA directory (i.e. DIR/CA)

-d | --directory CONFIG
    Config directory containing the Sub-CAs (and possibly the full CA). 
    Defaults to config/ in the Git repository root.

-e | --extension EXT
    Certificate extension (defaults to v3_server), possible alternative: v3_subCA

-h | --help
    Prints this helptext.

-H | --hash HASH
    The hash of the certificate request as an identifier in certificate 
    revocation checks 

-m | --mode MODE
    The mode of operation, one of: genCRL, importSubCA, sign, testRevoked, trust

-i | --input NAME
    Name of a certificate signing request (CSR) to be signed by the 
    specified CA.

-o NAME | --output NAME
    Destination of the output certificate. Base name of the truststore 
    to be created. The password of the truststore will be copied to a 
    in a file named "NAME.passwd".

${BOLD}MODES${REGULAR}
genCRL
    Generates a certificate revocation list and uploads it to the specified 
    location. Uploading is performed using scp and a supplied destination.

importSubCA
    prepends the certificate to the chain file and uploads it to the 
    CRL distribution server. Generates an initial CRL

sign
    Signs the given certificate signing request. The path of the signing 
    request is expected in --input NAME, the granted certificate will be 
    copied to --output NAME.

testRevoked
    Test whether the certificate has been revoked. Return state is as follows 
        0 (success) valid certificate exists
        1 certificate has been revoked (invalid)
        2 unknown certificate

trust
    Copies the system truststore to the destination given by -o NAME and
    imports the certificate specified by -h HASH into it. A password file 
    is generated with suffix .passwd

EOF
}

#
# revokes a certificate, creates a CRL and updates 
# the index 
#
function revokeCert {
	
	openssl ca -updatedb \
	  -passin file:cacert.passwd -config ca.cfg

	dialog --backtitle "CA-Management" \
	  --inputbox "Do you want to revoke this certificate (Please type 'YES')? " \
	  2>$TMP_RESULT 15 72 || error "Aborted"
	tmp=`cat $TMP_RESULT`
	test "$tmp" = "YES" || error "Aborted"
 
	openssl ca -revoke certs/$SERIAL.pem -config ca.cfg \
	  -passin file:cacert.passwd -config ca.cfg

        genCRL

        #
        # camgr.sh uses the md5sum of the certificate request as 
        # certificate id. This id is used to check, whether a 
        # certificate has been granted or revoked.  Upon certificate 
        # revocation, the certificate as well as the request are 
        # moved to the 'revoked' directory.
        #
        TMP_CERTIFICATE_ID=`grep -E "^$SERIAL " index.cloud | cut -d' ' -f3` 
        mv cloud/$TMP_CERTIFICATE_ID.pem revoked/
        mv req/$TMP_CERTIFICATE_ID.req revoked/
}

#
# Create a CRL and upload it to the CRL distribution point
# (SCP_ADDR)
#
function genCRL {
	DATE=`date +"%Y%m%d%H%M%S"`
	openssl ca -gencrl -out crl/crl.$DATE.pem \
	  -passin file:cacert.passwd -config ca.cfg

        find crl/ -type f -mtime +10 -exec rm {} \;

        scp crl/crl.$DATE.pem $SCP_ADDR/crl.pem
}

#
# Import the certificate for a Sub-CA, update truststore
# and truststore password. If using this script, cacert.pem is 
# copied during genCert step of superior CA!
#
function importSubCA {
        cat cacert.pem | sed $'/BEGIN CERTIFICATE/,$p\nd' >> newchain.txt
        cat chain.txt >> newchain.txt
        mv newchain.txt chain.txt

        openssl ca -updatedb \
          -passin file:cacert.passwd -config ca.cfg

        # Truststore
        tmp=`cat truststore.passwd`
        TRUST_PASSWD=`uuidgen -r | tr -d $'\n'`
        echo $TRUST_PASSWD > truststore.passwd

        keytool -storepasswd -keystore truststore -storepass $tmp \
          -new $TRUST_PASSWD 

        keytool -importcert -keystore truststore -storepass $TRUST_PASSWD \
          -noprompt -trustcacerts -file cacert.pem -alias "$CA_CN"

        # name, subject hash, fingerprint, cacert url, crl url
        tmp=`openssl x509 -in cacert.pem -subject_hash -noout | tr -d $'\n'`
        echo -n $CLOUD$'\t'$tmp$'\t' >> addresses.txt
        tmp=`openssl x509 -in cacert.pem -fingerprint -noout | cut -d= -f2 | tr -d $':\n'`
        echo $tmp$'\t'$DOWNLOAD_URL/cacert.pem$'\t'$DOWNLOAD_URL/crl.pem >> addresses.txt

        scp cacert.pem $SCP_ADDR/cacert.pem
        scp chain.txt $SCP_ADDR/chain.txt
        scp addresses.txt $SCP_ADDR/addresses.txt
        scp truststore $SCP_ADDR/truststore 
        scp truststore.passwd $SCP_ADDR/truststore.passwd
        genCRL
}

#
# Select a certificate for revokation
#
function selectDialog {
	pattern=""
	repeat=1
	while test $repeat -eq 1 ; do
		x=$(($x + 1))
		if test -n "$pattern" ; then 
			dialog --backtitle "CA-Manager" \
			  --inputbox "Please specify pattern for certificate subject:" \
			  15 72 "$pattern" 2>$TMP_RESULT || error "Could not obtain pattern"
			pattern=`cat $TMP_RESULT`
			tmp=`grep -E "^V" $CA_DIR/index.txt | grep "$pattern"` 
		else
			tmp=`grep -E "^V" $CA_DIR/index.txt`
		fi

		oldIFS=$IFS
		IFS='
'
		IFS=${IFS:0:1}
		newIFS=IFS
		tmp2=( $tmp )

		MENU=( "Serial" "St|Expire Date| Certificate CN" )

		for i in ${tmp2[@]} ; do
			STATUS=`echo $i | cut -f1 | tr -d $'\n'`
			SERIAL=`echo $i | cut -f4 | tr -d $'\n'`
			DATE=`echo $i | cut -f2` 
			DATE=`dateFormat $DATE` 
			NAME=`echo $i | cut -f6`
			NAME=`commonName $NAME` 
			MENU=( "${MENU[@]}" $SERIAL "$STATUS |$DATE | $NAME" )
		done
		IFS=$oldIFS

		dialog --backtitle "CA-Manager" \
		  --title $1 \
		  --extra-button --extra-label "Search" \
		  --menu "Please select the certificate" 19 72 10 \
		  "${MENU[@]}" 2>$TMP_RESULT
		case $? in
			0)
				repeat=0
				SERIAL=`cat $TMP_RESULT`
				if test $SERIAL != "Serial" ; then
					case $1 in
						Revoke)
							viewCert $SERIAL
							revokeCert $SERIAL
							;;
						View)
							viewCert $SERIAL
							cleanUp
							exit
							;;
						*)
					esac
				fi
				;;
			3)
				pattern="pattern"
				repeat=1
				;;
			*)
				error "Aborted"
		esac
	done

}


#
# Perform basic CA setup
#
function setup() {

        mkdir -p $CONFIG_DIR/nodes
        tmp=$CONFIG_DIR/$CLOUD/CA
        mkdir -p $tmp
        export CA_DIR=`realpath $tmp`
        echo "CA_DIR=$CA_DIR"

        TMP_RESULT=`mktemp /tmp/camgr.XXXXXX`
        if test -f $CA_DIR/cloud.cfg ; then
                echo "reading cloud.cfg"
                . $CA_DIR/cloud.cfg
        else
                CA_CRL="CRL URL"
                CA_COUNTRY="DE"
                CA_STATE="State"
                CA_PLACE="Location"
                CA_ORG="Organization"
                CA_OU="OU"
                CA_EMAIL="Email"
                CA_CN="CA Name"
                DEV_COUNTRY="DE"
                DEV_STATE="State" 
                DEV_PLACE="Location"
                DEV_ORG="Organization"
                DEV_OU="OU"
                DEV_EMAIL="Email"
                DEV_CN="Developer Name"
        fi
}

#
# test whether certificate has been revoked
# Exit codes
# 0 - valid certificate
# 1 - invalid / revoked certificate
# 2 - unknown certificate / does not exist
#
function testRevoked {
    cleanUp
    echo $HASH
    if test -f $CA_DIR/cloud/$HASH.pem ; then
        if test -f $CA_DIR/revoked/$HASH.pem ; then
            exit 1
        fi
        exit 0
    fi
    exit 2
}

#
# display certificate information
#
function viewCert {
	openssl x509 -in $CA_DIR/certs/$SERIAL.pem -text > $TMP_RESULT
	dialog --backtitle "CA-Management" \
	  --title "Please review this certificate!" \
	  --exit-label "Ok" \
	  --textbox $TMP_RESULT 15 72 || error "Aborted"
}

function writeConfig {
cat <<EOF > cloud.cfg
#
# LBAC CA config
# `date`
#
CA_COUNTRY="$CA_COUNTRY"
CA_STATE="$CA_STATE"
CA_PLACE="$CA_PLACE"
CA_ORG="$CA_ORG"
CA_OU="$CA_OU"
CA_EMAIL="$CA_EMAIL"
CA_CN="$CA_CN"
CA_CRL="$CA_CRL"
DEV_COUNTRY="$DEV_COUNTRY"
DEV_STATE="$DEV_STATE"
DEV_PLACE="$DEV_PLACE"
DEV_ORG="$DEV_ORG"
DEV_OU="$DEV_OU"
DEV_EMAIL="$DEV_EMAIL"
DEV_CN="$DEV_CN"
DEV_CERT="$DEV_CERT"
DOWNLOAD_URL="$DOWNLOAD_URL"
SCP_ADDR="$SCP_ADDR"
CLOUD="$CLOUD"
EOF
}
#
#==========================================================
#
# line drawing in UTF-8 PuTTY
export NCURSES_NO_UTF8_ACS=1

umask 0077
p=`dirname $0`
GETOPT=$(getopt -o 'c:d:e:hH:i:m:o:' --long 'cloud:,director:,extension:,help,hash:,input:,mode:,output:' -n 'camgr.sh' -- "$@")

if [ $? -ne 0 ]; then
        echo 'Error in commandline evaluation. Terminating...' >&2
        exit 1
fi

eval set -- "$GETOPT"
unset GETOPT

APPEND=FALSE
CONFIG_DIR=`realpath -m $p/../../config/`
EXTENSION='v3_server'
HASH=''
INPUT=''
MODE='interactive'
OUTPUT=''
CLOUD=''

while true ; do

    case "$1" in
        '-c'|'--cloud')
            CLOUD="$2"
            shift 2
            continue
            ;;
        '-d'|'--directory')
            CONFIG_DIR="$2"
            shift 2
            continue
            ;;
        '-e'|'--extension')
            case "$2" in
                'v3_server')
                    EXTENSION="$2"
                    ;;
                'v3_subCA')
                    EXTENSION="$2"
                    ;;
                *)
                    echo 'Invalid extension. Terminating...' >&2
                    exit 1
                    ;;
            esac
            shift 2
            continue
            ;;
        '-H'|'--hash')
            HASH="$2"
            shift 2
            continue
            ;;
        '-h'|'--help')
            printHelp
            exit 0
            ;;
        '-i'|'--input')
            INPUT=`realpath "$2"`
            shift 2
            continue
            ;;
        '-m'|'--mode')
            case "$2" in
                'genCRL')
                    MODE=genCRL
                    ;;
                'importSubCA')
                    MODE=importSubCA
                    ;;
                'sign')
                    MODE=sign
                    ;;
                'testRevoked')
                    MODE=testRevoked
                    ;;
                'trust')
                    MODE=trust
                    ;;
                *)
                    echo 'Invalid mode. Terminating...' >&2
                    exit 1
                    ;;
            esac
            shift 2
            continue
            ;;
        '-o'|'--output')
            OUTPUT=`realpath "$2"`
            shift 2
            continue
            ;;
        '--')
            shift
            break
            ;;
        *)
            echo 'Internal error!' >&2
            exit 1
            ;;
    esac
done

setup
pushd $CA_DIR
while test $MODE != 'quit' ; do
    getAction
    performAction
done
cleanUp
popd >/dev/null

