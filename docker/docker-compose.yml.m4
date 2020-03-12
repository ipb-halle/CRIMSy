include(dist/etc/config_m4.inc)dnl
version: '2.0'

#
# start cloud:
#  docker-compose up --build --remove-orphans
#
# stop cloud:
#  docker-compose down -v --remove-orphans
#

networks:
  lbac_private:
    driver: bridge

services:
  db:
    build: 
      context: .
      dockerfile: ./db/Dockerfile
    environment:
      - PGDATA=/data/db/pgsql
    labels:
      de.ipb-halle.lbac.docker-container: "db"
    networks:
      - lbac_private
LBAC_PGSQL_PORT_ENABLE   ports:
LBAC_PGSQL_PORT_ENABLE     - "5432:5432"
    volumes:
      - LBAC_DATASTORE/data/db:/data/db

  ui:
    build: 
      context: .
      dockerfile: ./ui/Dockerfile
    depends_on:
      - db
    labels:
      de.ipb-halle.lbac.docker-container: "ui"
    networks:
      - lbac_private
LBAC_TOMEE_PORT_ENABLE   ports:
LBAC_TOMEE_PORT_ENABLE     - "8080:8080"
    volumes:
      - LBAC_DATASTORE/data/ui:/data/ui

LBAC_PROXY_ENABLE proxy:
LBAC_PROXY_ENABLE   build:
LBAC_PROXY_ENABLE     context: .
LBAC_PROXY_ENABLE     dockerfile: ./proxy/Dockerfile
LBAC_PROXY_ENABLE   labels:
LBAC_PROXY_ENABLE     de.ipb-halle.lbac.docker-container: "proxy"
LBAC_PROXY_ENABLE   networks:
LBAC_PROXY_ENABLE     - lbac_private
LBAC_PROXY_ENABLE   ports:
LBAC_PROXY_ENABLE     - "80:80"
LBAC_PROXY_ENABLE     - "443:443"
LBAC_PROXY_ENABLE     - "8443:8443"

  lbacsolr:
    build:
      context: .
      dockerfile: ./solr/Dockerfile
    entrypoint: 
      - /bin/bash
      - /install/setup.sh
    labels:
      de.ipb-halle.lbac.docker-container: "lbacsolr"
    networks:
      - lbac_private
LBAC_SOLR_PORT_ENABLE   ports:
LBAC_SOLR_PORT_ENABLE     - "8983:8983"
    volumes:
      - LBAC_DATASTORE/data/solr:/data/solr

