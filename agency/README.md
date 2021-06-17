# CRIMSY-agency
external job scheduler / manager for CRIMSy


## Testing
1. Install agency-api:

    pushd ../agency-api 
    mvn install agency-api
    popd

2. Compile and package agency:

    mvn package

3. Run `./util/bin/testSetup.sh`
4. Obtain the truststore and the truststore password from your CRIMSy node
5. Configure the shared secret of your CRIMSy node and modify `test/agency_secret.txt` accordingly
6. Test drive using `./test/run.sh`

**Work in progress - has bugs**
