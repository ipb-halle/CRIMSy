# Querschnittliche Konzepte
In diesem Kapitel werden übergreifende Konzepte und Regelungen beschrieben.

## Entwicklungskonzepte

### Programmiersprache 
Der Hauptteil der Entwicklung erfolgt mit Java, wobei zunächst keine Festlegung auf eine bestimmte Implementierung (Oracle JDK, OpenJDK) getroffen wird. Die Lizenzbedingungen einiger JDKs zwingen jedoch quasi zur Benutzung von OpenJDK. Hinzu kommen – vor allem für Konfiguration, Installation und Betrieb – einige Shell-Skripte und SQL für das Datenbank-Setup.

### Coding-Conventions
n.n.

### Entwicklungsumgebung (IDE)
Das Projekt ist nicht an eine bestimmte IDE gebunden, jeder Entwickler entscheidet selbständig mit welchem Werkzeug er am besten zurecht kommt. Nach einer Phase mit IntelliJ IDEA findet die Entwicklung derzeit (Frühjahr 2020) mit NetBeans statt. Darüber hinaus findet Entwicklung auch ohne IDE mit vi unter Linux statt.

### Build-System
Als Build-System wurde Maven ausgewählt, weil damit bereits Erfahrungen vorliegen und es in seiner Funktionalität weit über das ebenfalls bekannte Build-System Ant hinausgeht. Andere Build-Systeme wie Gradle, Grape, Buildr usw. wurden nicht betrachtet; eine Umstellung, sollte sie notwendig werden, dürfte aufgrund der geringen Projektgröße unkritisch sein.

### Code-Repository
Der Quellcode wird mit Git verwaltet. Das zentrale Repository liegt auf GitHub [https://github.com/ipb-halle/CRIMSy](https://github.com/ipb-halle/CRIMSy). 

> **Info:** In der Anfangsphase des Projekts wurde der Quellcode mit Subversion (und das Gesamtprojekt mit Trac) verwaltet und später nach Git / Bitbucket / Confluence / Jira migriert. Um die Sichtbarkeit des Projekts zu verbessern,  den Administrationsaufwand im IPB zu minimieren und die Aktivitäten des IPB an einer Stelle zu bündeln, wurde entschieden, das Projekt in einem öffentlichen Repository (GitHub) weiterzuentwickeln. Dies zieht entsprechende Migrationsarbeiten für die Dokumentation und die Projektverwaltung nach sich. Zur Vertuschung unserer "Verbrechen" haben wir jedoch nicht die gesamte Projekthistorie nach GitHub übertragen ;-).

### Qualitätssicherung
Um Qualität einer komplexen Software sicher zu stellen, sind umfangreiche Tests unerlässlich. Dieses Projekt pflegt zu diesem Zweck eine ganze Reihe von Testfällen und fügt dem Quelltext laufend neue Testfälle hinzu. Da es sich um eine verteilte Webanwendung handelt, sind einfach Unit-Tests nicht ausreichend. Bestimmte Szenarien erfordern das Zusammenspiel mehrerer Komponenten - das Framework Arquillian [https://arquillian.org](https://arquillian.org) ist in diesem Zusammenhang sehr nützlich. Die Testabdeckung des Projekts beträgt über 40 Prozent (Stand März 2020). Vollautomatische Browser-Tests (Selenium-Framework o.ä.) sind momentan in Vorbereitung (siehe Abschnitt Integration Tests). 

### Dokumentation
Die Dokumentation erfolgt als Markup innerhalb des Projekts, damit das bisherige interne Projekt-Wiki (Confluence) abgelöst werden kann. Alle wesentlichen Informationen (inkl. Graphiken) wurden bzw. werden aus Confluence übertragen. Dabei wird die Trennung zwischen Handbüchern und Wiki aufrecht erhalten werden.

### Projektverwaltung
Die Projektverwaltung wird mittelfristig von Jira auf die bei GitHub verfügbare Projektverwaltung umgestellt (siehe auch den Info-Kasten im Abschnitt Code-Repository).

### Continuous Integration
CI im eigentlichen Sinne, d.h. inclusive Deployment, findet nicht statt. Das Projekt nutzt jedoch GitHub Actions, um das fehlerfreie Durchlaufen der Unit-Tests sicherzustellen. Das bislang genutzte Produkt Bamboo wurde abgelöst.

## Integration Tests
Für Integrationstests wird ein Rechner mit der üblichen Softwareausstattung (siehe Handbuch Konfiguration & Installation) und zusätzlich folgenden Komponenten benötigt:

- OpenJDK 8
- git
- ssh
- nodejs
- npm
- selenium-side-runner (installieren mit `npm install -g selenium-side-runner`)
- eine Liste der Zielhosts (im weiteren Verlauf `HOSTLIST`)

Der durchführende Nutzer muss sich mit `ssh` zu allen Rechnern betreffenden Rechnern verbinden können und mittels `sudo` Superuser-Privilegien erlangen können. Dies muss jeweils ohne Passwortabfrage funktionieren. Die Liste der Zielhosts enthält Schlüssel-Wert-Paare, jeweils ein Schlüssel und Wert durch Leerzeichen getrennt auf einer Zeile. Die Schlüssel spezifizieren dabei den jeweiligen Testknoten (`node1` usw.) und die Werte den (vollqualifizierten) Hostnamen des Zielhosts. Die Zeilenzahl bestimmt, wieviele Knoten für die Tests instantiiert werden. Nach dem Auschecken der Quellen von github kann der Integrationstest mit

        ./util/bin/testSetup.sh HOSTLIST 

angestoßen werden. 
