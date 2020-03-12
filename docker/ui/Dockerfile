#
# User Interface Container
# (c) 2017 Frank Broda
#
# build from project root dir:
#	docker build -f docker/ui/Dockerfile -t ui .
#
# run command: 
#	docker run \
#	  -p 8080:8080 \
#	  ui
#
# etc. pp.
#
FROM tomee:8-jre-7.1.0-plume

#
WORKDIR /install

#
COPY ./etc /install/etc
COPY ./extralib /install/extralib
COPY ./ui/ui.war /install/ 
COPY ./ui/setup.sh /install/
COPY ./ui/importKeystores.sh /install/
COPY ./ui/logpurge.sh /install/
COPY ./ui/tomcat-users.xml /install/
COPY ./ui/tomee.xml /install/

#
EXPOSE 8080 

#
RUN ["/bin/bash", "/install/setup.sh"]

#
# switch to unprivileged user
CMD ["su", "-", "tomee", "/usr/local/tomee/bin/catalina.sh", "run"]
