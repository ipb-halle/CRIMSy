# Fachlicher Kontext
Dieses Kapitel listet alle beteiligten Kommunikationspartner (Nutzer und IT-Systeme) auf und beschreibt die verwendeten Ein- und Ausgabedaten sowie die verwendeten Protokolle. Die Darstellung beginnt mit dem Blick auf das Gesamtsystem geht im weiteren Verlauf auf die Details ein.

![Cloud Architecture](img/BigPicture.svg "Big picture: cloud architecture")

Die Leibniz Bioactives Cloud wird durch die beteiligten Institute gebildet, indem jedes Institut einen Knoten zur Verfügung stellt. Dieser Knoten kann von den Nutzern des jeweiligen Instituts per Internet-Browser kontaktiert werden (orange Pfeile) und steht mit allen Knoten der übrigen Institute über eine verschlüsselte Verbindung (blaue Pfeile) in Verbindung. Die Knoten sind dabei weitgehend gleichberechtigt. Die einzige Ausnahme ist, dass ein Server als Master-Knoten fungiert, indem er ein Verzeichnis aller Knoten zur Verfügung stellt. Die Master-Rolle kann jedoch prinzipiell von jedem Knoten übernommen werden.

Als Verbindungsprotokoll kommt ausschließlich HTTP mit TLSv1.2-Verschlüsselung (ggf. auch aktuellere Versionen) zum Einsatz. Die Zertifikate für Maschine-zu-Maschine-Kommunikation (also der Knoten untereinander) werden von einer speziellen CA (der Leibniz Bioactives Cloud CA) ausgestellt, während für die Kommunikation mit den Browsern der Nutzer Zertifikate weithin anerkannter CAs (z.B. DFN PKI) verwendet werden sollen. Als weitere Sicherung erfolgt die Maschine-zu-Maschine-Kommunikation ausschließlich mit gegenseitiger zertifikatsbasierter Authentifizierung.

Bei den ausgetauschten Daten handelt es sich einerseits um Suchanfragen, entsprechende Antworten und ggf. um die entsprechenden Dokumente. Andererseits tauschen die Systeme technische Informtionen, wie z.B. Adressen der verfügbaren Knoten, die durchsuchbaren Collections oder für die Berechtigungsermittlung notwendige Informationen (Name, Emailadresse, Session-Token usw.) aus.

Im Schema nicht gezeigt sind Verbindungen zu öffentlichen Repositories, die die Knoten für Updates herstellen.

## Knotensicht
Jeder Knoten seinerseits besteht aus einem Docker-Host (Abbildung Mitte), der die verschiedenen Komponenten in sich vereint.

![Architektur](img/Architektur.svg "Knotenarchitektur")

> **Info**:
> _Die ursprüngliche Trennung der TomEE-Applikationen in Backend und UI wurde aufgegeben, da dieser Architekturansatz bei der Speicherung und Auslieferung von Dokumenten unpraktikabel ist. Die weitgehende Trennung der Nutzerkommunikation von der Maschine-zu-Maschine-Kommunikation wird jetzt durch die Komponente HTTPS-Proxy erreicht._

Bevor auf die einzelnen Komponenten eingegangen wird, werden zunächst die verschiedenen Kommunikationspfade analysiert. Bei der Betrachtung der Kommunikation müssen zunächst folgende grundsätzliche Szenarien unterschieden werden:

* die Kommunikation innerhalb eines Cloud-Knotens
* die Kommunikation der Nutzer mit ihrem Cloud-Knoten
* die Kommunikation des Knotens mit lokalen Ressourcen (LDAP, DNS, NTP, ...) des Instituts
* die Kommunikation der Cloud-Knoten untereinander
* die Kommunikation mit Software-Repositories für Update-Zwecke (nicht gezeigt)

Mit Ausnahme der Kommunikation innerhalb eines Knotens soll jegliche Kommunikation der Knoten verschlüsselt erfolgen. Es wird zudem dringend empfohlen, den Knoten durch eine externe Firewall abzusichern.

## Intra-Node Kommunikation
Die Kommunikation innerhalb eines Cloud-Knotens kann nach aktueller Einschätzung unverschlüsselt erfolgen, da Vertraulichkeit und Integrität wirksam durch die Container-Umgebung geschützt werden. Eine zusätzliche Verschlüsselung oder Authentifizierung erhöht die Sicherheit in diesem Szenario nur minimal und geht vermutlich mit meßbarem Ressourcenmehrverbrauch einher.

Die Kommunikation innerhalb eines Knotens umfasst folgende Anwendungsfälle:

* JDBC-Datenbankverbindung zu einer PostgreSQL-Instanz
* REST-API von SOLR
* HTTP-Kommunikation der TomEE-Webapplikation mit dem Reverse HTTPS-Proxy

## Nutzer-Kommunikation
Die Kommunikation der Nutzer mit einem Cloud-Knoten erfolgt ausschließlich über das HTTPS-Protokoll (Port 443); Zugriffe über HTTP (Port 80) werden durch Redirection automatisch auf Port 443 weitergeleitet.  Damit die Browser der Nutzer die Sicherheit der Verbindung erfolgreich überprüfen können, sollten bevorzugt öffentliche Zertifikate (z.B. von Zertifizierungsstellen des DFN-Vereins) zum Einsatz kommen. Die Verwendung der Leibniz Bioactives Cloud CA-Zertifikate wird lediglich als Fallback angeboten. Die Kommunikation der Nutzer umfaßt dabei die normale Interaktion von Nutzern mit einer Webseite und den Upload und Download von Dokumenten.

Durch Firewall-Regeln soll sichergestellt werden, dass der Zugriff auf die HTTP(S)-Schnittstelle (Ports 80 und 443) des Knotens nur aus dem Intranet des Instituts möglich ist.

## Lokale Ressourcen
Für die Nutzerauthentifizierung ist die Anbindung an einen lokalen LDAP-Server geplant. Die Kommunikation mit dem LDAP-Server sollte verschlüsselt (SSL, TLS, ...) erfolgen. Die Kommunikation mit weiteren Diensten (DNS, DHCP, NTP) erfolgt nach derzeitiger Planung unverschlüsselt. Für Wartungszwecke kann zudem auf dem Knoten ein SSH-Server installiert sein.

## Maschine-zu-Maschine-Kommunikation
Die Maschine-zu-Maschine-Kommunikation erfolgt nur verschlüsselt und nur nach gegenseitiger zertifikatsbasierter Authentifizierung. Die Zertifikate hierfür werden durch die Leibniz Bioactives Cloud CA ausgestellt, die auch eine Zertifikatssperrliste (CRL) pflegt. Die Kommunikation zwischen den Knoten erfolgt über Port 8443 und nutzt das HTTPS-Protokoll. Die Zugriffsmöglichkeiten sind auf definierte Schnittstellen (i.d.R. REST-API) beschränkt. Beispiele für ausgetauschte Inhalte sind:

* die Liste der an der Cloud beteiligten Knoten
* die Liste der verfügbaren Collections
* Authentifizierungsinformationen für einen angemeldeten Nutzer (nur Token, keine Passwörter!)
* Suchanfragen
* Dokumentenauslieferung (z.B. nach einer Suchanfrage)

## Software-Repositories
Für Updates der Software sind Kommunikationsverbindungen zu öffentlichen und nichtöffentlichen Repositories notwendig. Diese Verbindungen werden vom Docker-Host initiiert und durchweg über http / https abgewickelt. Die über diese Verbindungen übertragenen Daten sind üblicherweise durch digitale Signaturen vor Manipulation geschützt. Eine vollständige Liste der Endpunkte kann momentan nicht angegeben werden.

## Komponenten-Sicht

Wie bereits in der Knoten-Sicht erkennbar besteht ein Knoten aus in Docker-Containern verpackten Komponenten:

* **Apache HTTP Server**
  Der Apache HTTP Server nimmt als Proxy (mit Ausnahme einiger weniger dokumentierter Fälle) alle eingehenden Verbindungsanfragen entgegen.
* **TomEE Web und EE Application Server**
  Im TomEE Server verbirgt sich der Hauptteil der Cloudlogik. Die Webanwendung steuert und organisiert im Verbund mit den anderen Knoten die Cloud und verarbeitet Suchanfragen, Upload- und Download-Requests und alle sonstigen Nutzeranforderungen. Sie arbeitet dazu mit den übrigen Komponenten (v.a. s.u.) zusammen.
* **Apache SOLR**
  Apache SOLR übernimmt die Indexierung und das Retrieval von Dokumenten und stellt hierzu eine REST-Schnittstelle zur Verfügung. In späteren Projektphasen wird SOLR voraussichtlich um Funktionen zur semantischen Annotation (oder ggf. ähnlich gelagerte Funktionen) ergänzt werden.
* **PostgreSQL**
  Stellt eine fortgeschrittene relationale Datenbank zur Verfügung, auf die von der Webanwendung mittels JDBC-Schnittstelle (bzw. JPA / JPA2) zugegriffen wird.

und last but not least

* **Docker-Host**
  Stellt Speicher, Rechenleistung und Netzwerkverbindung bereit und orchestriert (mittels Docker-Compose) die Container.
