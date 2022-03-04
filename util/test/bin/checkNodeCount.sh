#!/bin/bash
#
# Check the count of nodes in database of node
#
# arg1 - HOSTLIST
# arg2 - argument from job: "node;expected"
#
p=`dirname $0`
. $p/util.sh

HOSTLIST="$1"
arg=$2
node=`echo $arg | cut -d';' -f1`
expected=`echo $arg | cut -d';' -f2`

if [ ! -s $HOSTLIST ] ; then
    error "Missing HOSTLIST" $LINENO
fi

if [ -z $node ] ; then
    error "Missing node argument" $LINENO
fi

if [ -z $expected ] ; then
    error "Missing expected outcome" $LINENO
fi

remote=`grep -vE "^#" $HOSTLIST | grep $node | cut -d';' -f2`
login=`grep -vE "^#" $HOSTLIST | grep $node | cut -d';' -f3`

cmd="echo \$'\\\\pset tuples_only on\nSELECT COUNT(*) FROM nodes;' | docker exec -i -u postgres  dist_db_1 psql -Ulbac lbac | head -1 | tr -d ' '"
cnt=`ssh -o "StrictHostKeyChecking no" "$login@$remote" "$cmd"` || \
    error "checkNodeCound.sh failed" $LINENO

assert "$expected -eq $cnt" checkNodeCound.sh $LINENO
exit $?
