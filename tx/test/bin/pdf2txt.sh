#!/bin/bash
#
# transform PDF documents into txt
#
p=`dirname $0`
export INSTALL=`realpath $p/..`

function transform {
    file="$0"
    dir=`dirname "$file"`
    if `echo $file | grep -qE '\.pdf$'` ; then 
        base=`basename "$file" .pdf`
    else 
        base=`basename "$file" .PDF`
    fi
    output="$dir/$base.txt"

    if test ! -s "$output" ; then
        echo "Processing $file"
        java -jar $INSTALL/../target/tx-1.0-jar-with-dependencies.jar \
          de.ipb_halle.tx.Tx -module TextExtractor \
          --filter $INSTALL/etc/toText.js \
          --input "$file" --output "$output"
    fi
}

export -f transform

find $1 -type f '(' -name "*.pdf" -or -name "*.PDF" ')' \
  -exec /bin/bash -c transform {} \;
