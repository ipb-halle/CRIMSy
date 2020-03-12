include(dist/etc/config_m4.inc)dnl
#!/bin/bash
#
#
APACHE_HOME=/usr/local/apache2
PRIMARY=LBAC_PRIMARY_CLOUD

addgroup -gid 80 www
adduser --no-create-home --gecos Apache2 --home /usr/local/apache2 \
  --shell /bin/bash --ingroup www --uid 80 \
  --disabled-password wwwrun

mv $APACHE_HOME/conf $APACHE_HOME/conf.old
mv $APACHE_HOME/htdocs $APACHE_HOME/htdocs.old

cp -r /install/conf $APACHE_HOME/
cp -r /install/htdocs $APACHE_HOME/

cp /install/etc/$PRIMARY/$PRIMARY.cert $APACHE_HOME/conf/lbac_cert.pem
cp /install/etc/$PRIMARY/$PRIMARY.key $APACHE_HOME/conf/lbac_cert.key
cat /install/etc/$PRIMARY/$PRIMARY.chain >> $APACHE_HOME/conf/lbac_cert.pem

cp /install/etc/official_cert.pem $APACHE_HOME/conf/
cp /install/etc/official_cert.key $APACHE_HOME/conf/

chown -R wwwrun:www $APACHE_HOME

#
# install CRL update script
#
cat <<EOF > /usr/local/bin/ca_update.sh
#!/bin/bash
#
# CA and CRL Update Script
#
cd /usr/local/apache2/conf/
tar -xf /install/ca_update.tar
chown -R wwwrun:www $APACHE_HOME/conf/crt $APACHE_HOME/conf/crl
apachectl -k graceful
EOF

chmod +x /usr/local/bin/ca_update.sh

#
# cleanup
#
rm -r /install/*
mkdir /install/etc/

