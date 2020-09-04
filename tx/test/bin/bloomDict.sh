#!/bin/bash
#
# crete Bloom filter from aspell dictionary
#
p=`dirname $0`
export INSTALL=`realpath $p/..`

for job in de:22 en:21 fr:23 ; do

    lang=`echo $job | cut -d: -f1`
    size=`echo $job | cut -d: -f2`

    echo "Processing $lang ..."

    aspell -d $lang dump master | aspell -l $lang expand | \
    sed -e $'s/ /\\n/g' | \
    java -jar $INSTALL/lib/natbase-1.0-jar-with-dependencies.jar \
          de.ipb_halle.tx.Tx -module BloomFilter \
          --create --size $size --nKeys 8 \
          --filter $lang.bf 
done
