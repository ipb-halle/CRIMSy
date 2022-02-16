#!/bin/bash
#
# Create JKS truststores from certificates in 
# /install/conf/$CLOUD 
#
#==========================================================
#
#
# change password of truststore
#
function createTruststore {
    CLOUD=$1
    pushd $CLOUD > /dev/null

    for cert in *.pem ; do
        $JRE_HOME/bin/keytool -importcert \
            -keystore ../$CLOUD.truststore \
            -storepass "$TRUSTSTORE_PASSWD" \
            -file "$cert" \
            -alias `basename $cert .pem` \
            -trustcacerts \
            -noprompt
    done

    popd >/dev/null
}

#
#==========================================================
#
cd /install/conf
TRUSTSTORE_PASSWD=`cat trustpass`

CLOUDS=`cat clouds.cfg | cut -d';' -f1`
for c in $CLOUDS ; do
    createTruststore $c
done
chown 8080:8080 $c.truststore

