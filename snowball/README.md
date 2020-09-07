# Mavenized Snowball Stemmer  
This module brings the Snowball stemmer [https://snowballstem.org/](https://snowballstem.org/) 
to the local Maven cache. Just run

    mvn install

The build process downloads the current `libstemmer_java.tgz` from GitHub, 
extracts it, compiles, tests, packages, and finally installs the jar in 
your local maven cache. 

The snowball stemmer can then be used by including 

```
  <dependency>
    <groupId>org.tartarus</groupId>
    <artifactId>snowball</artifactId> 
    <version>2.0.0</version>
  </dependency>
```

into the `pom.xml`. 

**Note 1:** The artifact will be only available locally; there is currently no 
deployment to central repositories (Maven Central Repository etc.)!

**Note 2:** The download of the .tgz archive via [https://github.com/snowballstem/snowballstem.github.io/blob/master/dist/libstemmer_java.tgz?raw=true](https://github.com/snowballstem/snowballstem.github.io/blob/master/dist/libstemmer_java.tgz?raw=true) did not work in GitHub Actions (failure with error 404). Therefore we placed a fallback copy of the .tgz archive in this directory and added some workaround to the `pom.xml` file.

# Licensing 
This code is released under the same BSD-3-Clause license as the snowball stemmer code
[https://github.com/snowballstem/snowball](https://github.com/snowballstem/snowball)
See COPYING
