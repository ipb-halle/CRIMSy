# Randbedingungen

Das Projekt unterliegt verschiedenen Randbedingungen, die unmittelbaren Einfluß auf die Architektur und Implementierung des Projekts ausüben. Dieses Kapitel versucht diese Randbedingungen möglichst vollständig zu erfassen geht auf ihre Implikationen ein:

* **Budget:** Das Budget für das Projekt Leibniz Bioactives Cloud beträgt ungefähr 3 Mannjahre. Dem Projekt stehen 1 Entwickler in Vollzeit und ein weiterer Entwickler mit zusätzlichen anderen Aufgaben zur Verfügung. Damit sind die Möglichkeiten des Projekts sehr begrenzt und es kommt darauf an, die knappen Ressourcen möglichst effizient zu nutzen, indem weitgehend auf vorgefertigte Open Source Komponenten zurückgegriffen wird. Die Entwicklung soll weitgehend agil erfolgen - viele Vorgehensmodelle (bspw. Scrum) setzen jedoch größere Teams voraus, so dass eine Anpassung stattfinden muss.

* **Programmiersprache:** Prinzipiell gibt es eine Reihe von Frameworks in verschiedenen Programmiersprachen (Java, PHP, Python, Ruby, ...), mit denen man die geplante Webanwendung umsetzen könnte. Ausschlaggebend für die Entscheidung für Java waren folgende Gesichtspunkte:
  * die bereits vorliegenden Erfahrungen mit der Entwicklung von Web-Anwendungen
  * das Vorhandensein einer breiten Auswahl allgemeiner (Nutzerinterfaces, Datenbankzugriffe, ...) und fachspezifischer (Chemie, Natural Language Processing, Ontologien, ...) Bibliotheken
  * aktiver Support, große Community
  * Möglichkeit der Realisierung in einer einzigen Programmiersprache (d.h. Vermeidung von Sprachmix)

* **Dezentralisierung:** Die Rechtsformen der am Leibniz Forschungsverbund "Wirkstoffe und Biotechnologie" beteiligten Institute sind sehr unterschiedlich: eingetragene Vereine (e.V.), Gesellschaften mit beschränkter Haftung (GmbH), Stiftungen öffentlichen Rechts. Die Institute sind zudem in unterschiedlichen Bundesländern angesiedelt, so dass die Rechtslage zu einzelnen Fragen unterschiedlich sein dürfte. Ein zentraler Ansatz würde bedeuten, dass entweder eine eigene Rechtspersönlichkeit gegründet werden müsste, die die Haftung und Finanzierung der Leibniz Bioactives Cloud übernimmt oder dass eines der beteiligten Institute diese Verbindlichkeiten schultert. Mit einem dezentralen Ansatz behält jedes Institut die Hoheit über seine Daten (präziser: über die bei ihm liegenden Daten) und ist selbst verantwortlich für die Sicherheit seiner Daten und der Infrastruktur sowie deren Finanzierung.  Der im Rahmen des Projekts verfolgte dezentrale Ansatz hat zudem den Charme, dass weitere Interessenten eigene verteilte Clouds betreiben können. Jeder Teilnehmer kann Mitglied in einer oder mehreren solcher Clouds sein und muss dafür nur eine lokale Instanz betreiben.

* **Übertragungsprotokolle:** Der Austausch mit den IT-Verantwortlichen hat gezeigt, dass der Definition von Schnittstellen und Übertragungsprotokollen eine besondere Bedeutung zukommt. Als Beispiele für auftretende Hürden seinen genannt:
  * Am HKI gibt es starke Vorbehalte gegenüber dem CIFS-Protokoll, das in einem frühen Prototypen für das Hochladen von Dokumenten genutzt wurde.
  * Das DSMZ besitzt keine Hoheit über seine DMZ; alle Dienste müssen eine Prüfung durch das Helmholtz-Zentrum für Infektionsforschung (HZI) durchlaufen und die verwendeten Schnittstellen, Protokolle und die Art der übertragenen Daten deklarieren
  * Das Leibniz-Institut für Lebensmittelsystembiologie an der TU München (Leibniz LSB@TUM) verfügt nicht über eine demilitarisierte Zone für den Betrieb seines Knotens. Die Firewall (Paketfilter) muss entsprechend manuell für das System konfiguriert werden.


