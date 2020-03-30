# Phase 2
Die zweite Phase beginnt damit, dass der Distributor den Quellcode übersetzt:

    mvn package

Anschließend müssen einige zusätzliche Java-Bibliotheken im Verzeichnis `config/extralib/` bereitgestellt werden, damit Hibernate zusammen mit TomEE funktioniert (Näheres unter [https://stackoverflow.com/questions/10852035/how-to-use-tomee-with-hibernate](https://stackoverflow.com/questions/10852035/how-to-use-tomee-with-hibernate)). Im Einzelnen handelt es sich um die Bibliotheken (ggf. in neueren Versionen):

        antlr-2.7.7.jar
        classmate-1.3.0.jar
        dom4j-1.6.1.jar
        hibernate-commons-annotations-5.0.1.Final.jar
        hibernate-core-5.2.6.Final.jar
        hibernate-jpa-2.1-api-1.0.0.Final.jar
        hibernate-validator-5.2.4.Final.jar
        javassist-3.20.0-GA.jar
        jboss-logging-3.3.0.Final.jar
        postgresql-9.3-1102.jdbc4.jar

Zwischenzeitlich wurden (bzw. nachfolgend werden) die verschlüsselten Konfigurationsdateien der Teilnehmerknoten empfangen und im Verzeichnis config/nodes/ abspeichert. Diese Dateien sollten zuvor einer Sichtprüfung unterzogen werden. Das Format der Konfigurationsdateien sollte folgendem Muster entsprechen:

    #
    # LBAC_INSTITUTION=IPB Demo
    # CERTIFICATE_ID=06:30:D8:B9:86:54:B5:69:CF:79:3F:C9:A9:BC:D4:8F:D8:94:39:78
    # Wed Oct 10 14:30:29 UTC 2018
    #
    # ----- SMIME ENCRYPTED CONFIG BEGIN -----
    -----BEGIN PKCS7-----
    MIINnQYJKoZIhvcNAQcDoIINjjCCDYoCAQAxggLBMIICvQIBADCBpDCBnjELMAkG
    [...]
    YnVrNTz4AlCAvtlG+QDZrbaw/iwAUkaLVI+Hk/mMpB9e
    -----END PKCS7-----
    # ----- SMIME ENCRYPTED CONFIG END -----

Der entscheidende Schritt in Phase 2 ist die Festlegung des Master-Knotens und des Cloudnamens, wobei letzerer identisch mit dem Namen der Sub-CA ist. Ein Cloudname wie "TEST", "CLOUD" oder "PRODUKTION" ist vollkommen ausreichend; es dürfen jedoch innerhalb einer Multi-Cloud keine Kollisionen des Namens vorkommen. In den oben angeführten Beispielen wurde der Cloudname "CRIMSY Test Cloud" bzw. kurz "`TEST`" verwendet.

Der Masterknoten wird durch Aufruf des Scripts util/bin/package.sh festgelegt. Ohne weiteres Argument gibt das Script eine kurze Syntaxinformation aus. Ansonsten gibt es für das Script drei Einsatzszenarien:

1. Festlegung des Masterknotens: Aufruf mit `./util/bin/package.sh CLOUDNAME MASTER`
2. Hinzufügen eines Knotens zur Cloud: Aufruf mit `./util/bin/package.sh CLOUDNAME`
3. Neuzusammenstellung der Installationspakete aller aus dem Verzeichnis `config/CLOUDNAME/` bekannten Knoten, z.B. bei einer neuen Software-Version: `./util/bin/package.sh CLOUDNAME AUTO`

Während Szenario 3 später automatisch durchläuft, öffnen sich in den Szenarien 1 und 2 die nachfolgend dargestellten Dialoge zur Auswahl eines Knotens:

