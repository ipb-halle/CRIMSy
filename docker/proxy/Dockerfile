#
# HTTPS Proxy 
# (c) 2017 Frank Broda
#
# build from project root dir:
#	docker build -f proxy/Dockerfile -t proxy .
#
# run command: 
#	docker run \
#	  -p 80:80 \
#         -p 443:443 \
# 	  -p 8443:8443 \
#	  proxy
#
# etc. pp.
#
FROM httpd 

#
RUN mkdir -p /install/etc

#
WORKDIR /install

#
COPY ./etc /install/etc/
COPY ./proxy /install

#
EXPOSE 80 443 8443

#
RUN ["/bin/bash", "/install/setup.sh", "init"]

#
# switch to unprivileged user is done by httpd and
# the account to use is defined in the config file
#
