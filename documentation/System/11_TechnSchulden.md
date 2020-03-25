# Technische Schulden
Dieses Kapitel listet technische Schulden auf, die mit bestimmten Design-Entscheidungen in Zusammenhang stehen. Den IT-Verantwortlichen und Systemadministratoren der Institute soll damit die Einschätzung des Betriebsrisikos ermöglicht werden. Gleichzeitig sollen regelmäßige Reviews dieser Liste den Abbau technischer Schulden ermöglichen. Desweiteren werden einige offene Punkte gelistet, deren Umsetzung mangels Ressourcen bislang noch nicht möglich war.

* **Verschlüsselung der Konfigurationsdatei** Die Konfigurationsdatei wird vor dem Versand nicht signiert sondern nur verschlüsselt, weil zum Zeitpunkt der Konfiguration noch kein Zertifikat für den Absender generiert wurde. Im Prinzip könnte daher jedermann eine Konfigurationsdatei erzeugen und versenden. Das Risiko wird als gering beurteilt, da zusätzliche Kommunikationskanäle (Email, Telefon) bestehen, über die eine Abstimmung des Versands erfolgt. Außerdem sind die Cloud-Administratoren gehalten, die Konfigurationsdateien vor dem Einlesen gründlich zu prüfen.
* **Härtung** Die Härtung des Knotens durch Mechanismen wie SELinux wurde aufgrund mangelnder Ressourcen noch nicht unternommen.
* **Init-System** Das System unterstützt momentan hauptsächlich Systemd. Alternativ können SystemV-Init-Skripte ausgewählt werden, die jedoch praktisch ungetestet sind.

## Offene Punkte
Folgende Punkte konnten bislang nicht umgesetzt werden, die zukünftige Umsetzung ist jedoch vorgesehen:

* Verwendung des TomEE-internen Mechanismus für Zugriffsbeschränkung

