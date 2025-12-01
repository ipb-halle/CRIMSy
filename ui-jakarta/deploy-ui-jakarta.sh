#!/bin/bash
set -e

echo "============================"
echo "1. Generating OpenAPI classes (ui-jakrta)"
echo "============================"

mvn -f ./pom.xml clean generate-sources


echo "============================"
echo "2. Packaging ui-jakrta /jar)"
echo "============================"

mvn -f ./pom.xml -DskipTests package

echo "ui-jakrta build complete!"


