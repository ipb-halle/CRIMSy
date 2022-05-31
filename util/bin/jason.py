#!/bin/env python3
#
# extract information from JSON 
#
# Example: extract the volume name of a docker container
#
#   docker inspect crimsyreg_service | jason.py "[0]['Mounts'][0]['Name']"
#
import sys,json

obj = json.load(sys.stdin)
print(eval('obj' + sys.argv[1]))
