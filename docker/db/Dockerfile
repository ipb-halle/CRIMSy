#
#  Cloud Resource & Information Management System (CRIMSy)
#  Database container
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
# Database dump:
# 	docker exec CONTAINER_ID su -l postgres -c "/usr/bin/pg_dump -Ccb lbac"
#
FROM ipbhalle/crimsydb:bingo_pg12

#
COPY *.sql /docker-entrypoint-initdb.d/
COPY *.sh /usr/local/bin/

RUN chmod +x /usr/local/bin/*.sh

