#!/bin/bash
#
# Import per cloud keystores from /install/
#
#==========================================================
#
# config (LBAC_INTERNET_FQHN, ...)
#
. /install/etc/config.sh

TOMCAT_HOME=/usr/local/tomee
KEYSTORE_PASSOUT=`cat $TOMCAT_HOME/conf/keypass`
TRUSTSTORE_PASSOUT=`cat $TOMCAT_HOME/conf/trustpass`

#
# change password of truststore
#
function importTruststore {

    PASSIN=`cat $CLOUD.trustpass`

    keytool -storepasswd -keystore $CLOUD.truststore -storepass "$PASSIN" \
          -new "$TRUSTSTORE_PASSOUT"

    chmod go+r $CLOUD.truststore
    mv $CLOUD.truststore $TOMCAT_HOME/conf/
}

#
# transform pkcs12 keystore into JKS keystore
#
function importKeystore {

    PASSIN=`cat $CLOUD.keypass`

    keytool -importkeystore -srckeystore $CLOUD.pkcs12 \
      -destkeystore $CLOUD.keystore -srcstoretype pkcs12 \
      -destalias $LBAC_INTERNET_FQHN -deststorepass "$KEYSTORE_PASSOUT" \
      -srcalias 1 -srcstorepass "$PASSIN"

    chmod go+r $CLOUD.keystore
    mv $CLOUD.keystore $TOMCAT_HOME/conf/$CLOUD.keystore
}

#
#==========================================================
#
pushd /install
for i in *.truststore ; do
    CLOUD=`basename $i .truststore`
    importTruststore $CLOUD
    importKeystore $CLOUD
    rm $CLOUD.trustpass $CLOUD.pkcs12 $CLOUD.keypass
done
popd

