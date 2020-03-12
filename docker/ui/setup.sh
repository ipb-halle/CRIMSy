#!/bin/bash
#
# UI container setup 
#
#
TOMCAT_HOME=/usr/local/tomee
TOMCAT_USER=lbac
TOMCAT_PASSWORD=`dd if=/dev/urandom bs=32 count=1 | sha1sum - | cut -d' ' -f1`
APP=/ui
WAR=/install/ui.war
PRIMARY=`cat /install/etc/primary.cfg`
PRIMARY_PASSWD=`cat /install/etc/$PRIMARY/$PRIMARY.keypass`

. /install/etc/config.sh

#
# Install extra libs
#
mv /install/extralib $TOMCAT_HOME
pushd $TOMCAT_HOME/lib
for i in $TOMCAT_HOME/extralib/*.jar ; do
	ln -s $i `basename $i`
done
popd

#
# Remove default webapps
#
pushd $TOMCAT_HOME/webapps
rm -r ROOT docs host-manager manager
popd

#
# Config, Webapp
#
mv /install/tomcat-users.xml $TOMCAT_HOME/conf/tomcat-users.xml
mv /install/tomee.xml $TOMCAT_HOME/conf/tomee.xml
mv $WAR $TOMCAT_HOME/webapps/ui.war

#
# passwords and helper programs
#
cp /install/etc/$PRIMARY/$PRIMARY.trustpass $TOMCAT_HOME/conf/trustpass
cp /install/etc/$PRIMARY/$PRIMARY.keypass $TOMCAT_HOME/conf/keypass
cp /install/importKeystores.sh /usr/local/bin
cp /install/logpurge.sh /usr/local/bin
chmod +x /usr/local/bin/*.sh 

#
# copy keystores & truststores and process them with importKeystores.sh
#
pushd /install/etc
find . -type f \( -name "*.truststore" \
  -o -name "*.trustpass" \
  -o -name "*.pkcs12" \
  -o -name "*.keypass" \) \
  -exec cp {} /install \;
popd 
/usr/local/bin/importKeystores.sh

#
# Users and groups
#
addgroup -gid 8080 tomee 
adduser --no-create-home --gecos TomEE --home /usr/local/tomee \
  --shell /bin/bash --ingroup tomee --uid 8080 \
  --disabled-password tomee

#
# Modify conf/system.properties
#
cat <<EOF >> $TOMCAT_HOME/conf/system.properties
#
# Disable CDI for Hibernate JPA
# (http://http://tomee-openejb.979440.n4.nabble.com/CDI-issues-tomee-7-0-2-td4680584.html)
#
tomee.jpa.cdi = false
de.ipb_halle.lbac.cloud.servlet.FileUploadExec.Path = /data/ui
EOF

chown -R tomee:tomee $TOMCAT_HOME

#
#
#
cat <<EOF > $TOMCAT_HOME/bin/setenv.sh
#!/bin/sh
#
# created by /install/setup.sh
#
export CATALINA_HOME=$TOMCAT_HOME
export JRE_HOME=/usr/local/openjdk-8
EOF

chmod +x $TOMCAT_HOME/bin/setenv.sh

