#!/bin/bash
#
#

function assert {
    if [ ! $1 ] ; then
        echo "Assertion '$2' failed in file $0, line $3"
        exit 1
    fi
    echo "Assertion '$2' succeeded."
    exit 0
}

function error {
    echo "$1 in file $0, line $2"
    exit 1
}
