#
#  Leibniz Bioactives Cloud
#  Copyright 2017 Leibniz-Institut f. Pflanzenbiochemie
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
#
#==========================================================
#
# build from project root directory:
#	docker build -f docker/db/Dockerfile -t db .
#
# run command: 
#	docker run \
#	  -e "POSTGRES_PASSWORD=password" \
#	  -p 5432:5432 db
#
# Database dump:
# 	docker exec CONTAINER_ID su -l postgres -c "/usr/bin/pg_dump -Ccb lbac"
#
FROM pgchem:latest

#
COPY ./db/*.sql /docker-entrypoint-initdb.d/
COPY ./db/dbupdate.sh /usr/local/bin/

RUN chmod +x /usr/local/bin/dbupdate.sh

