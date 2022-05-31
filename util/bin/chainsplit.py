#!/usr/bin/env python3
#
# chainsplit.py
#
# Split a certificate chain from stdin into single 
# certificates The program expects each certificate 
# in the chain to be preceeded by its subject. The 
# name of the resulting certificates is constructed 
# from the cloud name (passed by command line) and 
# the common name of the subject.
#
import sys, re

patternCN = re.compile('/CN=')
patternBegin  = re.compile('---BEGIN CERTIFICATE---')
patternEnd = re.compile('---END CERTIFICATE---')

regCN = re.compile('^(.+)\/CN=([^\/]+)(.*)$')

cloud = sys.argv[1]

printing = False

for line in sys.stdin.readlines() :
    if patternCN.search(line) :
        cname = regCN.split(line.rstrip())[2]
        cname = re.sub(' ', '_', cname)
    elif patternBegin.search(line) :
        printing = True
        print("open file:" + cloud + '.' + cname + ".pem")
        cert = open(cloud + '.' + cname + '.pem', 'a') 
    elif patternEnd.search(line) :
        printing = False
        cert.write(line)
        cert.close()

    if printing :
        # print(line)
        cert.write(line)

