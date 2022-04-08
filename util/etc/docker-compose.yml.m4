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
    image: LBAC_IMAGE_REGISTRY/crimsydb:LBAC_VERSION
    environment:
      - PGDATA=/data/db/pgsql_12
      - POSTGRES_PASSWORD=LBAC_DB_PASSWD
    labels:
      de.ipb-halle.lbac.docker-container: "db"
    networks:
      - lbac_private
LBAC_PGSQL_PORT_ENABLE   ports:
LBAC_PGSQL_PORT_ENABLE     - "5432:5432"
    volumes:
      - LBAC_DATASTORE/data/db:/data/db

  fasta:
    image: LBAC_IMAGE_REGISTRY/crimsyfasta:LBAC_VERSION
    depends_on:
      - db
    labels:
      de.ipb-halle.lbac.docker-container: "fasta"
    networks:
      - lbac_private

  ui:
    image: LBAC_IMAGE_REGISTRY/crimsyui:LBAC_VERSION
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
      - LBAC_DATASTORE/data/ui_conf:/data/conf
      - LBAC_DATASTORE/data/reports:/data/reports

  proxy:
    image: LBAC_IMAGE_REGISTRY/crimsyproxy:LBAC_VERSION
    environment:
      - REDIRECT_FQHN=LBAC_INTRANET_FQHN
    labels:
      de.ipb-halle.lbac.docker-container: "proxy"
    networks:
      - lbac_private
    ports:
      - "80:80"
      - "443:443"
      - "8443:8443"
    volumes:
      - LBAC_DATASTORE/data/proxy_conf:/usr/local/apache2/conf
