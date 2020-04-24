# Risiken und Datenschutz

## Risikoanalyse
Dieses Kapitel nimmt eine Risikoanalyse vor und versucht, die mit dem Betrieb der Leibniz Bioactives Cloud verbundenen Risiken und die Bedrohungen, denen die Leibniz Bioactives Cloud ausgesetzt ist, möglichst vollständig darzustellen und die Maßnahmen zu ihrer Minimierung zu skizzieren.

**Es ist ausdrücklich nicht Ziel des Projekts, eine hochsichere oder hochverfügbare Lösung zu schaffen. Die Betreiber und Nutzer der Leibniz Bioactives Cloud sollen jedoch darauf vertrauen können, dass alle Maßnahmen zur Risikominimierung ergriffen wurden, die man vernünftigerweise erwarten kann.**

Die Risiken bzw. Bedrohungen können dabei aus verschiedenen Blickwinkeln betrachtet werden. So kann man zunächst zwischen Risiken für den Betrieb der Leibniz Bioactives Cloud an sich und darüberhinausgehenden Risiken unterscheiden, wobei das Schadenpotential der zweiten Klasse leider erheblich höher ist. Zweitens ist eine Unterscheidung nach der Quelle der Bedrohung, nähmlich durch anonyme Angreifer aus dem Internet oder durch Insider aus dem Intranet möglich. Drittens sind bei der Betrachtung die mögliche Schadenshöhe und die geschätzte Eintrittswahrscheinlichkeit hilfreiche Kennziffern. Insgesamt ist der Betrieb der Leibniz Bioactives Cloud durch folgende Bedrohungen gekennzeichnet:

* Feindliche Übernahme der Infrastruktur
* Unautorisierter lesender Zugriff auf Daten
* Unautorisierte Datenmanipulation
* Denial of Service
* technisches Versagen

Mit diesen Bedrohungen sind die Risiken

* Sach- und Vermögensschäden / Haftung
* Reputationsschaden
* Betriebsunterbrechung
* Datenverlust

verbunden, die durch technische und organisatorische Maßnahmen zur Bedrohungsabwehr minimiert werden sollen. Bei allen Betrachtungen wird angenommen, dass aktuelle kryptographische Verfahren und speziell auch die Implementierungen sicher sind. Diese Einschätzung der Algorithmen beruht dabei auf der Beurteilung durch das Bundesamt für Sicherheit in der Informationstechnik (BSI). Sollten während des Betriebs der Cloud Schwachstellen bekannt werden, so muss mit entsprechenden Updates darauf reagiert werden.

Da strenggenommen personenbezogene Daten verarbeitet werden ist auch eine Betrachtung zum Datenschutz notwendig (Kapitel im Anschluss).

### Feindliche Übernahme
Die feindliche Übernahme der Infrastruktur hat insgesamt das höchste Schadenpotential, da der Angreifer damit die volle Kontrolle über das System erlangt und es für seine Zwecke mißbrauchen kann. Denkbar sind unter anderem:

* Spam-Versand
* Teilnehmer in einem Bot-Net
* Mining von Crypto-Währungen
* Abfluß aller Daten
* Nutzung als Ausgangsbasis für weitere Angriffe

Die feindliche Übernahme muss daher unter allen Umständen vermieden werden. Im Falle eines Falles sollte ein Einbruch bzw. ein Einbruchsversuch frühzeitig durch entsprechende Sensoren (intrusion detection) entdeckt werden. Eine zentrale Rolle kommt hier den Firewalls der DMZ zu, die die Möglichkeiten des Angreifers wirksam beschränken sollen. Ebenso wichtig sind eine sichere Grundkonfiguration des Knotens, auf dem nur die tatsächlich benötigte Software installiert sein soll, und das regelmäßige Einspielen von Sicherheitsupdates.

Neben direkten Angriffen über das Netzwerk (von Außen über das Internet bzw. von Innen über das Intranet) besteht ein weiterer Angriffsvektor über bösartige Dokumente, die durch autorisierte Nutzer hochgeladen, z.B. durch Pufferüberläufe während der Indexierung, das System kompromittieren. Neben intrusion detection besteht die Gegenwehr in diesem Szenario hauptsächlich im regelmäßigen Update der eingesetzten Softwarekomponenten.

### Unautorisierter Lesezugriff
Der Lesezugriff auf Dokumente darf nur über definierte Schnittstellen (DocumentServlet) möglich sein. Die Schnittstelle muss dabei prüfen, ob der Zugriff autorisiert ist. Die Autorisierung kann beispielsweise mittels eines Access-Tokens erfolgen, dass eine zeitlich begrenzte Gültigkeit hat. Auf diese Weise soll sichergestellt werden, dass Links auf Dokumente (z.B. aus dem Browser-Cache) nicht von unautorisierten Personen mißbraucht werden können. Die Infrastruktur für die Autorisierung ist im System selbst verankert und funktioniert über die Tripel (Asset, Zugriffsprivileg, Person/Gruppe). Die Authentifizierung als Person bzw. Gruppenmitglied kann gegen eine interne Nutzerdatenbank erfolgen. Zusätzlich besteht die Möglichkeit der Anbindung an externe Systeme (LDAP / Active Directory).

Die Datenübertragung von Knoten zu Knoten oder zum Nutzer muss verschlüsselt erfolgen, so dass es nicht zum Datenabfluß während des Transits kommen kann.

Ebenso muss das System gegen Lesezugriffe auf Zertifikatsschlüssel oder Passwörter geschützt sein. Der Zugriff auf Passwörter wird dadurch verhindert, dass Nutzerpasswörter nur gesalzen und mehrfach gehasht gespeichert werden. Verbindungen zu externen Authentifizierungssystemen (LDAP) werden verschlüsselt aufgebaut.

Die Speicherung der Daten in einem verschlüsselten Dateisystem wird nicht für notwendig erachtet; die Leibniz Bioactives Cloud ist nicht für die Speicherung von Daten vorgesehen, die solche Sicherheitsmaßnahmen erfordern.

### Unautorisierte Datenmanipulation
Ebenso wie Lesezugriffe dürfen Schreiboperationen (einschließlich Löschen) nur über definierte Schnittstellen (z.B. FileUpload) und nur mit Autorisierung möglich sein. Das im Abschnitt "Unautorisierter Lesezugriff" geschriebene gilt entsprechend analog.

### Denial of Service
Für die Bedrohung Denial of Service sind verschiedene Szenarien zu betrachten:

1. **Denial of Service gegen die Leibniz Bioactives Cloud**, indem die Ressourcen (Speicher, CPU, Bandbreite) einzelner oder mehrerer Knoten durch gezielte Abfragen erschöpft werden Das Risiko dieses Szenarios soll dadurch minimiert werden, dass Eingabeparameter (z. B. in URLs oder Post-Requests) einer sorgfältigen Zulässigkeitsprüfung (Minimale und maximale Länge, Zeichenvorrat, Vollständigkeit , reguläre Ausdrücke, usw.) unterzogen werden. Ausserdem müssen sinnvolle Begrenzungen für Prozessorzeit und Upload-Dateigrößen definiert werden.  
2. **Denial of Service gegen andere interne (LDAP-Server, DNS-Server, usw.) oder externe (Software-Repositories, sonstige Server) Dienste:** Hier sind vor allem Maßnahmen zum Schutz des LDAP-Servers gegen die Blockierung von Nutzer-Accounts zu nennen. Durch Intruder Lockout verhindert die Infrastruktur, dass Nutzeraccounts über den Cloud-Knoten gezielt angegriffen werden. Die Risiko für sonstige Server und Dienste (DNS, Software-Repositories, sonstige Server) wird als gering eingestuft, so dass bislang keine Maßnahmen geplant sind. Im Zweifel können auch hier die Firewalls eine Schutzfunktion übernehmen.
3. **Denial of Service gegen Nutzer**, z.B. durch Browser-Absturz oder Computerviren Im normalen Betrieb der Cloud sind die Nutzer vor allem durch bösartige Dokumente gefährdet. Hierunter werden Dokumente (PDF, DOCX, DOC, ...) verstanden, in deren aktiven Inhalten sich Schadcode verbirgt. Solche Dokumente können (feindliche Übernahme des Systems ausgeschlossen) die Nutzer nur erreichen, wenn sie zuvor von einem autorisierten Nutzer hochgeladen wurden. Idealerweise wird dieses Risiko dadurch minimiert, dass sowohl der hochladende als auch der herunterladende Benutzer Virenscanner auf ihren Computern installiert haben. Unter Umständen kann auch die Firewall zum Intranet helfen, das Risiko dieser Bedrohung zu senken, wenn sie in der Lage ist, den Datenverkehr (als man-in-the-middle) transparent zu entschlüsseln und auf Viren zu scannen.

### Technisches Versagen
Gegen technisches Versagen sollte die Installation durch adäquate Maßnahmen wie ECC-RAM oder fortschrittliche Storage-Systeme (RAID, ZFS, ...) geschützt werden. Sollte es dennoch zu einer Betriebsunterbrechung bei einem einzelnen Knoten kommen, so bleibt die Leibniz Bioactives Cloud als ganzes durch ihre dezentrale Organisiation weiterhin nutzbar. Gegen Datenverluste des Knotens ist ein externes Backup vorzusehen. Die Software der Leibniz Bioactives Cloud wird hierfür regelmäßig Datenbank-Dumps erzeugen, die von individuell zu konfigurierenden Backup-Agenten gesichert werden können. Sofern ein funktionsfähiges Backup existiert, ist der zu erwartende Schaden bei Ausfall eines Knotens vergleichsweise gering und erfordert somit keine weiteren Maßnahmen.


## Datenschutz
Die Betrachtung der Datenverarbeitung durch die Leibniz Bioactives Cloud kann unter verschiedenen Aspekten erfolgen:

* aus der Perspektive der Anwender
* aus der Perspektive der gespeicherten Daten
* aus der Perspektive der Betreiber

### Anwenderperspektive
Für die Anwender der Leibniz Bioactives Cloud wurde eine Datenschutzerklärung verfasst, die über den jeweiligen Knoten abgerufen werden kann. Darin ist erklärt, welche Daten der Anwender zu welchem Zweck gespeichert und verarbeitet werden und nach welchen Fristen diese Daten gelöscht werden. Im Wesentlichen handelt es sich bei anonymen Zugriffen um die üblichen Verkehrsdaten (IP-Adresse usw.); bei authentifizierter Nutzung kommen noch der Name, Nutzername ("Login"), evtl. eindeutige externe Kennungen, Gruppenmitgliedschaften und ggf. dienstliche Telefonnummern bzw. dienstliche Emailadressen hinzu. Bei authentifizierter Nutzung eines Knotens werden diese Daten an die übrigen Knoten der Cloud annonciert, damit dem Nutzer Rechte an Objekten eingeräumt werden können. Der Nutzer kann vom Betreiber seines Knotens die Anonymisierung seines Nutzerkontos verlangen. Die Anonymisierung erfolgt dadurch, dass jegliche personenbezogene Daten (Name, Login, Telefonnummer, Emailadresse, ggf. eindeutige externe Kennungen) aus einem Nutzerkonto entfernt werden.

### Datenperspektive
Auf den Knoten werden wissenschaftliche Daten und Dokumente (u.a. wissenschaftliche Veröffentlichungen) gespeichert. Diese Dokumente können personenbezogene Daten enthalten. Üblicherweise umfasst dies die Name der Autoren, ihre Affiliation und Kontaktdaten. In einigen wissenschaftlichen Publikationen finden sich gelegentlich auch Bilder und Lebensläufe. Die Leibniz Bioactives Cloud ist jedoch keinesfalls zur Speicherung darüberhinausgehender personenbezogener Informationen (Patientendaten, nicht anonymisierte Umfragedaten) geeignet.

### Betreiberperspektive
Dem Betreiber der Leibniz Bioactives Cloud ist für die Einhaltung der datenschutzrechtlichen Vorschriften auf seinem Knoten verantwortlich. Er muss insbesondere sicherstellen, dass keine unzulässigen Dokumente in die Leibniz Bioactives Cloud hochgeladen werden und dass eventuellen Anonymisierungsbegehren von Nutzern Rechnung getragen wird. Darüber hinaus steht er in der Verantwortung der ordnungsgemäßen Datenlöschung bei Ausscheiden seines Knotens.

Die im laufenden Betrieb anfallenden Log-Dateien, die personenbezogene Daten der Nutzer enthalten können, werden automatisch nach 7 Tagen gelöscht.

