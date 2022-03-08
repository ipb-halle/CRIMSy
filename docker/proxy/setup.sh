#!/bin/bash
#
#
APACHE_HOME=/usr/local/apache2

addgroup -gid 80 www
adduser --no-create-home --gecos Apache2 --home /usr/local/apache2 \
  --shell /bin/bash --ingroup www --uid 80 \
  --disabled-password wwwrun

mkdir $APACHE_HOME/extern
cat <<EOF > $APACHE_HOME/extern/index.html
<!DOCTYPE html>
<html lang="en"><head><meta charset="utf-8"></head><body>Restricted access!</body></html>
EOF

chown -R wwwrun:www $APACHE_HOME
rm /setup.sh

