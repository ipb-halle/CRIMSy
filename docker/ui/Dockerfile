#
#  Cloud Resource & Information Management System (CRIMSy)
#  TomEE application server
#  Copyright 2021 Leibniz-Institut f. Pflanzenbiochemie
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
FROM tomee:8-jre-7.1.4-plume

#
WORKDIR /data
COPY createTruststores.sh /usr/local/bin
COPY setup.sh /

#
COPY extralib /usr/local/tomee/extralib
COPY ui.war /usr/local/tomee/webapps/
COPY logpurge.sh /usr/local/bin/
COPY tomcat-users.xml /usr/local/tomee/conf/
COPY tomee.xml /usr/local/tomee/conf/


#
EXPOSE 8080 

#
RUN ["/bin/bash", "/setup.sh"]

#
# switch to unprivileged user
CMD ["su", "-", "tomee", "/usr/local/tomee/bin/catalina.sh", "run"]
