#!/bin/bash
#
# set up a test environment for CRIMSY Agency
#
p=`dirname $0`
pushd `realpath $p/../..`
mkdir -p test

cat <<EOF > test/run.sh
#!/bin/bash
#
p=\`dirname \$0\`
pushd \`realpath \$p\`

password=\`cat trustpass\`

java -Djavax.net.ssl.trustStore=./truststore \\
  -Djavax.net.ssl.trustStorePassword=\$password \\
  -Djavax.net.ssl.trustStoreType=JKS \\
  -jar ../target/crimsy-agency-1.0.jar \\
  -p ./agency_secret.txt \\
  -s ./agency.sh \\
  -u https://biocloud.somewhere.invalid/ui/rest/jobs
EOF

cat <<EOF > test/agency.sh
#!/bin/sh
#
date >> output.txt
echo "type: \$1 queue: \$2 data:" >> output.txt
cat >> output.txt
echo == END DATA == >> output.txt
EOF

echo "supersecret" > test/agency_secret.txt

chmod +x test/agency.sh test/run.sh

cat <<EOF

Please note: 
1. This setup is not for production runs.
2. You still need to tweak the configuration:
   - truststore and truststore password
   - shared secret of your CRIMSy node
   - URL of your CRIMSY node

EOF
