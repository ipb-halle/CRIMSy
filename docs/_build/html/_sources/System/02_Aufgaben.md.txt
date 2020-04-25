# Aufgabenstellung
Die Umsetzung des Gesamtprojekts gliedert sich in diverse Einzelaufgaben, die in der Abfolge der Projektphasen zunehmend komplexer miteinander wechselwirken. 

* **Dokumente indexieren:** Das System bietet eine Möglichkeit zur Volltext-Indexierung von Dokumenten. Verschiedene Produkte sollen auf ihre Eignung für diesen Zweck geprüft werden
* **Dokumente suchen:** Das System bietet eine Möglichkeit, Dokumente im Volltext-Index zu suchen (und zu finden)
* **Dokumente hochladen:** Das System bietet die Möglichkeit, Dokumente über ein geeignetes Interface hochzuladen. Die hochgeladenen Dokumente werden automatisch indexiert. Die Möglichkeit, Dokumente zu aktualisieren oder zu löschen muss geschaffen werden.
* **Dokumente herunterladen:** Gefundene Dokumente sollen von Benutzern heruntergeladen werden können.
* **Verteilte Operation:** Die Suche nach Dokumenten findet über mehrere Knoten verteilt statt, so dass mit einer Abfrage Dokumente aus mehreren beteiligten Instituten gefunden werden können
* **Verwaltung von Collections:** Dokumente werden in Collections (Kollektionen / Sammlungen / ...) organisiert und gemeinsam verwaltet, die jeweils in einem Institut lokalisiert sind. Damit übt das jeweils beherbergende Institut die Gewalt über sie aus. Üblicherweise enthalten die Collections nur Dokumente aus dem eigenen Institut. Falls im späteren Projektverlauf die Möglichkeit eingeräumt wird, auch von anderen Instituten in eine Collection hochzuladen, so hat die beherbergende Institution insoweit auch die Gewalt über diese Dokumente inne. Das System bietet die Möglichkeit Collections anzulegen und zu löschen.
* **Verwaltung von Nutzern:** Das System bietet die Möglichkeit, lokale Nutzer zu authentifizieren. Dies wird über eine lokale Nutzerdatenbank realisiert. Weiterhin gibt es die Möglichkeit zur Anbindung an externe Verzeichnisdienste (LDAP, AD). Die Nutzung von Diensten wie Shibboleth wird gelegentlich geprüft, ist aber momentan nicht geplant. Die Authentifizierung von externen Benutzern soll stattdessen immer durch ihr Heimatsystem erfolgen. Gegenüber einem Remote-System weisen sie sich ggf. durch ein Token aus.
* **Verwaltung von Nutzergruppen:** Zur Vereinfachung der Verwaltung können Nutzer zu Gruppen zusammengefasst werden. Außerdem soll das Verschachteln von Gruppen ermöglicht werden. Es muss sichergestellt werden, dass trotz der verschiedenen Kontexte von Nutzern und Gruppen (lokale Nutzerdatenbank, Verzeichnisdienst, remote) keine Inkonsistenzen verusacht werden können.
* **Verwaltung von Berechtigungen:** Es findet eine feingranulare Berechtigungsprüfung (Access Control Lists) für alle Ressourcen des Systems statt.
* **Installation, Betrieb, Update:** Das System unterstützt die Systemadministratoren bei der Konfiguration, der Installation, beim Betrieb und beim Update von Knoten der Leibniz Bioactives Cloud
* **Sicherheit** Das System ist durch aktuelle Software und aktuelle Protokolle gegen Angriffe und Manipulation geschützt. Alle Kommunikation mit externen Systemen findet verschlüsselt statt.
* **Semantische Annotation:** ...

