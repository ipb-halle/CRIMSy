#!/bin/bash
#
#

function assert {
    if [ ! $1 ] ; then
        echo "Assertion failed: '$2', file $0, line $3"
        exit 1
    fi
    echo "Assertion succeeded: '$2'."
    exit 0
}

function error {
    echo "$1 in file $0, line $2"
    exit 1
}
