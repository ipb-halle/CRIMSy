# Externer Proxy
Der interne Proxy der Cloud basiert auf Apache httpd 2.4.x. Die Übernahme der Konfiguration in einen externen Proxy mit Apache httpd 2.4.x dürfte sich daher trivial gestalten. Beim Einsatz einer anderen Software wird der Aufwand naturgemäß höher ausfallen - eine Abschätzung können wir an dieser Stelle leider nicht liefern. Da der Apache httpd als (Reverse) Proxy mit Verschlüsselung betrieben wird, müssen zunächst die Module `mod_proxy`, `mod_proxy_http` und `mod_ssl` geladen sein.

> **Hinweis:** Das Modul `mod_proxy_wstunnel` ist mittlerweile entbehrlich, weil Websockets nicht mehr verwendet werden.

Als nächstes muss der Proxy Zugriff auf die Zertifikate des Knotens erhalten. Da ein Knoten Mitglied in mehreren Clouds sein kann, befinden sich die Dateien für jede Cloud in einem entsprechenden Unterordner unterhalb von `$LBAC_DATASTORE/dist/etc/`. Für eine Cloud `TEST` befinden sich das Zertifikat des Knotens und der dazugehörige private Schlüssel in den den Dateien `TEST.cert` und `TEST.key` im Ordner `$LBAC_DATASTORE/dist/etc/TEST/`.  Die URLS für den Download der Zertifikate der Zertifikatskette sowie der zugehörigen CRLs befinden sich in der Datei `addresses.txt` (jeweils im Unterordner der Cloud). Für offizielle Zertifikate gibt es bei Verwendung externer Proxies keinen vorgeschriebenen Speicherort.

Schließlich müssen zwei VHosts für den internen und externen Datenverkehr konfiguriert werden. Für den Verkehr mit dem Intranet kommt folgendes Template für die Konfiguration des VHosts zum Einsatz:

    <VirtualHost _default_:443>
        # General setup for the virtual host
        ServerName LBAC_INTRANET_FQHN:443
        ServerAdmin LBAC_MANAGER_EMAIL
        DocumentRoot "/usr/local/apache2/htdocs"

        SSLEngine on
        SSLCertificateFile "/usr/local/apache2/conf/official_cert.pem"
        SSLCertificateKeyFile "/usr/local/apache2/conf/official_cert.key"
        SSLCACertificateFile "/usr/local/apache2/conf/official_cacert.pem"

        <IfModule mod_proxy.c>
            ProxyPass         /ui http://DOCKER_HOST:8080/ui
            ProxyPassReverse  /ui http://DOCKER_HOST:8080/ui
        </IfModule>
    </VirtualHost>

Für die Kommunikation mit dem Internet übernimmt der Proxy die zertifikatsbasierte Authentifizierung der Clients. Der Bereich erlaubter URLs ist zudem gegenüber dem Intranet eingeschränkt. Das Template für die Konfiguration des Internet-VHosts lautet daher:

    <VirtualHost _default_:8443>
        # General setup for the virtual host
        ServerName LBAC_INTERNET_FQHN:8443
        ServerAdmin LBAC_MANAGER_EMAIL
        DocumentRoot "/usr/local/apache2/htdocs"

        SSLEngine on

        SSLCertificateFile "/usr/local/apache2/conf/TEST.pem"
        SSLCertificateKeyFile "/usr/local/apache2/conf/TEST.key"

        # Certificate Revocation Lists (CRL):
        SSLCARevocationCheck chain
        SSLCARevocationPath "/usr/local/apache2/conf/crl/"

        SSLVerifyClient require
        SSLVerifyDepth 3
        SSLCACertificatePath "/usr/local/apache2/conf/crt/"

        <IfModule mod_proxy.c>
            ProxyPass         /ui/rest  http://DOCKER_HOST:8080/ui/rest
            ProxyPassReverse  /ui/rest  http://DOCKER_HOST:8080/ui/rest
            ProxyPass         /ui/servlet/document http://ui:8080/ui/servlet/document
            ProxyPassReverse  /ui/servlet/document http://ui:8080/ui/servlet/document
        </IfModule>
    </VirtualHost>

Die Zertifikate aller Cloud-CAs eines Knotens werden im Verzeichnis `/usr/local/apache2/conf/crt/` abgelegt. Analog müssen die Sperrlisten aller beteiligten CAs im Verzeichnis `/usr/local/apache2/conf/crl/` abgelegt werden. Apache erwartet, dass die Zertifikate und Zertifikatssperrlisten über ihre _subject hashes_ gefunden werden können (siehe Apache Dokumentation und `c_rehash`). Wichtig ist, die Zertifikatssperrliste täglich zu aktualisieren. 

Optional (hier nicht gezeigt) kann ein dritter VHost zum Einsatz kommen, um bei unverschlüsseltem Zugriff aus dem Intranet auf die verschlüsselte Seite(n) umzuleiten.
