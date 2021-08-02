Management des Knotens
======================
.. warning:: Das initiale Passwort des Administrators (bei der Konfiguration vergeben) sollte umgehend nach der Installation geändert werden!

Einige Einstellungen des Knotens können zur Laufzeit mit dem Webbrowser konfiguriert werden. Dazu gehören

* Nutzer- und Gruppenverwaltung
    - Passwortänderungen für lokale Accounts (z.B. lokaler Administrator)
    - Hinzufügen bzw. Deaktivierung von lokalen Nutzern und Gruppen
    - Ändern von Gruppenmitgliedschaften
    - Anbindung an LDAP-Verzeichnisse (z.B. Active Directory)
* Ändern von Systemeinstellungen
* Konfiguration eines externen Job-Schedulers
* Verwaltung von Barcode-Druckern
* Verwaltung von Collections (Anlegen, Ändern, Löschen, ...)

Firewall
--------
Sofern CRIMSy nicht in einer demilitarisierten Zone (DMZ) betrieben wird, sollte mindestens ein Paketfilter installiert werden, um den Zugang zu den einzelnen Diensten zu regulieren. Während Port 8443 öffentlich erreichbar sein muss, dürfen die Ports 80 und 443 (HTTP und HTTPS) nur aus dem internen Netz erreichbar sein. Für Ubuntu- und Debian-Systeme mit installierter 'Uncomplicated Firewall' (ufw) könnten die nachfolgend wiedergegebenen Regeln einen Basis-Schutz darstellen. Dazu müssen die Regeln nach lokaler Anpassung an die Datei `/etc/ufw/after.rules` angefügt werden.::

    ###
    ### Block worldwide access to CRIMSy user interface
    ### Filter rules according to: https://github.com/chaifeng/ufw-docker
    ### 
    ###
    *filter
    :ufw-user-forward - [0:0]
    :ufw-docker-logging-deny - [0:0]
    :DOCKER-USER - [0:0]
    -A DOCKER-USER -j ufw-user-forward

    #
    #=====================================================================
    #
    # - global access to port 8443
    # - unlimited traffic among docker containers
    #
    -A DOCKER-USER -j RETURN -p tcp --dport 8443
    -A DOCKER-USER -j RETURN -s 172.16.0.0/12

    #
    #=====================================================================
    #
    # repeat the following two lines for each subnet to allow internal 
    # HTTP / HTTPS traffic (HTTP will be redirected to HTTPS by proxy container)
    # Replace '192.168.1.80/28' by the actual subnet address.
    #
    -A DOCKER-USER -j RETURN -p tcp -s 192.168.1.80/28 --dport 80
    -A DOCKER-USER -j RETURN -p tcp -s 192.168.1.80/28 --dport 443

    #
    #=====================================================================
    #
    # reject or log malformed traffic
    #
    -A DOCKER-USER -p udp -m udp --sport 53 --dport 1024:65535 -j RETURN

    -A DOCKER-USER -j ufw-docker-logging-deny -p tcp -m tcp --tcp-flags FIN,SYN,RST,ACK SYN -d 172.16.0.0/12
    -A DOCKER-USER -j ufw-docker-logging-deny -p udp -m udp --dport 0:32767 -d 172.16.0.0/12

    -A DOCKER-USER -j RETURN

    -A ufw-docker-logging-deny -m limit --limit 3/min --limit-burst 10 -j LOG --log-prefix "[UFW DOCKER BLOCK] "
    -A ufw-docker-logging-deny -j DROP

    COMMIT


Bislang liegen nur Anforderungen und Betriebserfahrungen mit dem IPv4-Protokoll vor. Der Betrieb in IPv6-Netzen sollte prinzipiell möglich sein. Mangels Testmöglichkeiten und aufgrund der zusätzlichen Komplexität wurde bislang von Versuchen in diese Richtung abgesehen. 

.. warning:: Administratoren müssen durch Einführung entsprechender Regeln sicherstellen, dass sich durch das IPv6-Protokoll keine Angriffsvektoren (z.B. bezüglich unkontrollierter Datenabflüsse) ergeben.


Systemeinstellungen
-------------------
.. image:: img/systemSettings.png
    :width: 80%
    :align: center
    :alt: Systemeinstellungen

In den Systemeinstellungen können momentan 4 Einstellungen verwaltet werden:

* Die Kontaktinformationen des Datenschutzbeauftragten (HTML-formatiert), wie sie in der Datenschutzerklärung des Knotens angezeigt werden sollen
* Die Homepage der den Knoten betreibenden Institution
* Das *shared secret* für die Jobverwaltung (**Achtung:** wird im Klartext angezeigt)
* Ein Flag, ob die anonyme Nutzung des Knotens möglich ist oder ob die Anmeldung obligatorisch ist

Die Änderung von Systemeinstellungen ist den Mitgliedern der lokalen Administratoren-Gruppe vorbehalten. Da einige Eingaben in den Systemeinstellungen im Kontext des Nutzerbrowsers interpretiert werden, müssen die Mitgliedschaften in der Administratorengruppe restriktiv gehandhabt werden.

Barcode-Drucker
---------------
Für klassisches Reporting setzt CRIMSy auf die Erzeugung von PDF-Dateien, die im Nutzerbrowser angezeigt werden können und die der Nutzer herunterladen oder über die ihm zugänglichen Drucker ausdrucken kann. Barcode-Drucker sind jedoch eine spezielle Klasse von Geräten, die sich unter anderem dadurch auszeichnen, dass sie auf ungewöhnliche Papierformate drucken und zum Teil recht komplex konfiguriert werden müssen. Beispielsweise müssen unterschiedliche Label-Formate, Druckgeschwindigkeiten, das Etikettenmaterial und der Schneidemechanismus für Etiketten konfiguriert werden. Die Installation und Konfiguration spezieller Treiber auf jedem einzelnen Nutzer-PC ist daher mit hohem Aufwand verbunden. Außerdem würde diese Herangehensweise die Nutzer mit unnötiger Komplexität konfrontieren.
Bei der Entwicklung von CRIMSy wurde daher ein anderer Ansatz gewählt: Barcode-Drucker werden von CRIMSy über eigene Treiber direkt angesteuert. Der Systemadministrator konfiguriert die Drucker einmalig zentral. Nutzer können den für sie passenden Drucker aus einem Drop-Down-Menü auswählen. 

Da CRIMSy üblicherweise in einer DMZ betrieben wird, können die Drucker nicht direkt angesteuert werden. Alle Druckjobs landen daher in einer Warteschlange, die von einem externen Dienst (CRIMSy Agency) abgearbeitet wird. CRIMSy Agency fragt den Knoten dabei regelmäßig ab, ob neue Druckaufträge vorliegen und leitet diese dann an einen Druckspooler (z.B. CUPS) weiter. Die Zuordnung erfolgt dabei über den Queue-Namen.

.. image:: img/printerList.png
    :width: 80%
    :align: center
    :alt: Druckerliste 

.. image:: img/printerDetails.png
    :width: 80%
    :align: center
    :alt: Druckereinstellungen 

Die Druckertreiber sind recht flexibel gehalten und können konfiguriert werden. Dadurch ist es möglich, das Layout lokaen Wünschen anzupassen. Die Konfiguration erfolgt durch `Schlüssel=Wert`-Paare, wobei die Werte Hexadezimal eingegeben werden. Zeilen die mit einem Doppelkreuz als erstem Zeichen beginnen, werden ignoriert; ein Backslash als letztes Zeichen auf einer Zeile signalisiert, dass weitere Hexwerte auf der nächsten Zeile folgen (siehe Abbildung).
