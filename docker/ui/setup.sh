#!/bin/bash
#
# UI container setup 
#
#
TOMCAT_HOME=/usr/local/tomee

#
# Install extra libs
#
pushd $TOMCAT_HOME/lib
for i in $TOMCAT_HOME/extralib/*.jar ; do
	ln -s $i `basename $i`
done
popd

#
# Remove default Faces installation (Mojarra 2.2.*);
# MyFaces is contained in extralib
#
rm $TOMCAT_HOME/lib/javax.faces-2.2.*.jar

#
# Remove default webapps
#
pushd $TOMCAT_HOME/webapps
rm -r ROOT docs host-manager manager
popd

#
# make helper programs executable
#
chmod +x /usr/local/bin/*.sh 

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
# created during install by /setup.sh
#
export CATALINA_HOME=$TOMCAT_HOME
export JRE_HOME=/usr/local/openjdk-8
/usr/local/bin/createTruststores.sh
EOF

chmod +x $TOMCAT_HOME/bin/setenv.sh
chmod +x /usr/local/bin/createTruststores.sh

#
# remove this install script
#
rm /setup.sh
