#
#  Cloud Resource & Information Management System (CRIMSy)
#  Web-Proxy container
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
FROM ipbhalle/crimsyplugins

#
COPY ./setup.sh /
COPY ./httpd-foreground /usr/local/bin/

#
EXPOSE 80 443 8443

#
RUN ["/bin/bash", "/setup.sh" ]

#
# switch to unprivileged user is done by httpd and
# the account to use is defined in the config file
#
