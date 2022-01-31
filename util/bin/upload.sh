#!/bin/bash
#
# Upload Script
# Cloud Resource & Information Management System
# Copyright 2020 Leibniz-Institut f. Pflanzenbiochemie 
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
#
p=`dirname $0`
LBAC_REPO=`realpath $p/../..`
if [ x"$1" = "x" ] ; then
    echo "Usage: upload.sh CLOUD"
else
    CLOUD=$1
fi

export LBAC_CA_DIR="$LBAC_REPO/config/$CLOUD/CA"
. $LBAC_CA_DIR/cloud.cfg

echo LBAC_REPO=$LBAC_REPO

mkdir -p $LBAC_REPO/target
sed -e "s,CLOUDCONFIG_DOWNLOAD_URL,$DOWNLOAD_URL," $LBAC_REPO/util/bin/configure.sh | \
sed -e "s,CLOUDCONFIG_CLOUD_NAME,$CLOUD," |\
openssl smime -sign -signer $LBAC_CA_DIR/$DEV_CERT.pem \
  -md sha256 -binary -out $LBAC_REPO/target/configure.sh.sig \
  -stream -nodetach \
  -inkey $LBAC_CA_DIR/$DEV_CERT.key \
  -passin file:$LBAC_CA_DIR/$DEV_CERT.passwd 

cp $LBAC_CA_DIR/chain.txt $LBAC_REPO/target/
cp $LBAC_CA_DIR/$DEV_CERT.pem  $LBAC_REPO/target/devcert.pem

pushd $LBAC_REPO/target > /dev/null

cat <<EOF >$LBAC_REPO/target/index.html
<!DOCTYPE html>
<html lang="en-US" ><head><meta charset="UTF-8"><title>CRIMSy Download Page</title></head>
<body bgcolor="#fff">
<h1>$CLOUD_NAME</h1>
Auf dieser Seite sind die für die Installation relevanten Ressourcen 
(Handbücher, das signierte Konfigurations-Skript und die zugehörigen 
Zertifikate) zum Herunterladen zusammengefasst. Ausführlichere Dokumentation 
finden Sie entweder <a href="https://www.leibniz-wirkstoffe.de/CRIMSy/docu/index.html">hier</a> oder auf 
unserer <a href="https://github.com/ipb-halle/CRIMSy">Projektseite</a>.

<h2>Installation</h2>
<ul>
<li><a href="https://www.leibniz-wirkstoffe.de/CRIMSy/docu/CRIMSy.pdf">Handbücher</a> (PDF)</li>
<li><a href="configure.sh.sig">configure.sh.sig</a> das PEM-kodierte und signierte Installationsskript</li>
<li><a href="chain.txt">chain.txt</a> LBAC Zertifikatskette</li>
<li><a href="devcert.pem">devcert.pem</a> Code-Signing-Zertifikat</li>
</ul>

Verwenden Sie am besten den folgenden Code-Schnipsel, der auch die Prüfung der Signatur übernimmt:

<div style="padding: 20px">
<div style="border-radius:25px ; padding: 20px; background: #d0ffd0">
<pre>
curl --output configure.sh.sig $DOWNLOAD_URL/configure.sh.sig
curl --output chain.txt $DOWNLOAD_URL/chain.txt
curl --output devcert.pem $DOWNLOAD_URL/devcert.pem
sha256sum chain.txt 
openssl verify -CAfile chain.txt devcert.pem
openssl smime -verify -in configure.sh.sig -certfile devcert.pem -CAfile chain.txt -out configure.sh 
</pre></div></div>

Idealerweise haben Sie die SHA-256 Prüfsumme der Zertifikatskette der <i>$CLOUD_NAME CA</i>
auf unabhängigem Weg (Email, Telefon, Fax) von uns bekommen. Vergleichen 
Sie bitte die Ausgaben der letzten drei Kommandos mit folgenden Werten (in Rot dargestellt). 
<b>Bitte informieren Sie uns, wenn es irgendeine Unstimmigkeit gibt.</b>

<div style="padding: 20px">
<div style="border-radius:25px ; padding: 20px; background: #ffffd0">
<pre>
[...]
~&gt; sha256sum chain.txt
<span style="color: #ff0000; font-weight: bold">`sha256sum chain.txt`</span>
~&gt; openssl verify -CAfile chain.txt devcert.pem
<span style="color: #ff0000; font-weight: bold">devcert.pem: OK</span>
~&gt; openssl smime -verify -in configure.sh.sig -certfile devcert.pem -CAfile chain.txt -out configure.sh
<span style="color: #ff0000; font-weight: bold">Verification successful</span>
</pre></div></div>

Falls Sie keine Abweichungen feststellen, können Sie das Skript aufrufen:

<div style="padding: 20px">
<div style="border-radius:25px ; padding: 20px; background: #d0ffd0">
<pre>
chmod +x configure.sh
./configure.sh
</pre></div></div>
</html>
</body>
EOF

chmod go+r configure.sh.sig index.html chain.txt devcert.pem
echo "Upload to: $SCP_ADDR"
scp -p index.html configure.sh.sig devcert.pem $SCP_ADDR
popd > /dev/null

