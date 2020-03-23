## Webanwendung
Die Webanwendung im TomEE-Container ihrerseits läßt sich wiederum in folgende Bestandteile gliedern:

* Konfigurationsdaten der Webanwendung (Verzeichnisse WEB-INF, META-INF, ...)
* statische Ressourcen (Icons, CSS-Dateien, Javascript-Bibliotheken, ...)
* Templates für das Rendering der (X)HTML-Seiten
* Geschäftslogik in Form von Java-Klassen

Zur Buildtime kommen hierzu noch Unit-Tests zur Qualitätssicherung. Alternativ besteht die Webanwendung in folgende Funktionskomponenten:

* Nutzer- und Berechtigungsverwaltung
* Verwaltung lokaler Ressourcen (Collections, ...)
* Cloud-weite Synchronisierung der Ressourcen
* Daten-Upload
* Verteilte Suche (Simple Search, Word Cloud Search) und Document Delivery (Downloads)

###  Nutzer- und Berechtigungsverwaltung
Während einige Inhalte der Leibniz Bioactives Cloud innerhalb der teilnehmenden Institutionen frei zugänglich sein sollen, ist für andere Ressourcen (Dokumente, Daten, Services) eine Zugriffsbeschränkung notwendig. In jedem Fall darf die Änderung administrativer Einstellungen oder das Hochladen von Dokumenten nur für authorisierte Benutzer möglich sein. Um einerseits eine feingranulare Steuerung und andererseits eine bequeme Verwaltung zu ermöglichen, gibt es im System folgende Objekte bzw. Interfaces (für Details siehe Java-Dokumentation):

* Nutzer (User)
* Gruppen (Group); die Gruppen können Nutzer und Gruppen als Mitglieder (Membership) haben
* Access Controlled Objects (Interface ACObject); alle Objekte die Zugriffsbeschränkungen erweitern diese abstrakte Klasse
* Permissions (enum ACPermission) regeln die Art des erlaubten Zugriffs (Lesen, Anlegen, Ändern, Löschen, Eigentum Übertragen, ...)
* Access Control Entries (ACEntry); legen die Zugriffsberechtigung für einen Nutzer bzw. eine Gruppe fest
* Access Control Lists (ACList); sammeln alle Access Control Entries für ein Access Controlled Object

Um Administratoren die Verwaltung von Nutzern und Gruppen zu vereinfachen, wurde die von LDAP (bzw. Active Directory) bekannte Verschachtelung von Gruppen implementiert (nested group memberships). Gruppen können also ihrerseits Mitglieder von Gruppen sein; die Mitgliedschaften vererben sich auf die Mitglieder. Bei den Nutzern und Gruppen werden dabei aktuell vier verschiedene Typen unterschieden:

-------------- -----------------------------------------------------
   Typ         Beschreibung
-------------- -----------------------------------------------------
`BUILTIN`       vordefinierte Nutzer und Gruppen, 
                z.B. `Public Account`; keine Änderungen möglich

`LOCAL`         lokale Nutzer und Gruppen des Knotens

`LDAP`          lokale Nutzer und Gruppen aus einem LDAP-Verzeichnis  

`LBAC_REMOTE`   remote Nutzer, d.h. Nutzer eines anderen Knotens

-------------- -----------------------------------------------------

Schwierigkeiten ergeben sich daraus, dass die Verwaltung dieser Objekte nicht nur auf dem lokalen Knoten erfolgen muss, sondern dass insbesondere die Information über Nutzer und Gruppen(mitgliedschaften) auch auf anderen Knoten der Cloud vorliegen muss. Zur Sicherstellung der Konsistenz gibt es daher Regeln, zwischen welchen Nutzer- bzw. Gruppentypen Mitgliedschaften eingerichtet werden können:

------------------- ------------ ---------- ---------  ----------------
X Mitglied in Y?    Y=`BUILTIN`  Y=`LOCAL`  Y=`LDAP`   Y=`LBAC_REMOTE`
------------------- ------------ ---------- ---------  ----------------
X = `BUILTIN`        Auto         Nein      Nein        Nein

X = `LOCAL`          Auto         Ja        Nein        Nein

X = `LDAP`           Auto         Ja        Auto        Nein

X = `LBAC_REMOTE`    Auto         Ja        Nein        Auto

------------------- ------------ ---------- ---------  ----------------

> **Tip:** Es ergibt z.B. keinen Sinn, einer LDAP-Gruppe weitere Mitglieder hinzuzufügen, da Mitgliedschaften dieser Gruppe ausschließlich im LDAP verwaltet werden sollen.

Ein Administrator kann also nur zu Gruppen vom Typ LOCAL Mitglieder hinzufügen. Andere Mitgliedschaften werden entweder automatisch verwaltet oder sind prinzipiell ausgeschlossen.

Zur weiteren Vereinfachung ist die Authentifizierung von Nutzern auf den lokalen Knoten beschränkt. Ein Nutzer (egal ob knoteneigener Nutzer oder LDAP) kann sich nur an seinem Heimatknoten anmelden. Die Anmeldung von Nutzern externer Knoten wird nicht unterstützt. Der Knoten kann auch ohne Anmeldung genutzt werden, in diesem Fall wird der Public Account benutzt. Mit der Anmeldung eines Nutzers erfolgt die (asynchrone) Übertragung der Nutzer- und Gruppeninformationen zu den übrigen Knoten der Cloud. Gruppenmitgliedschaften werden dabei "flach", d. h. ohne Verschachtelung, übertragen. Soweit auf dem empfangenden Knoten weitere Mitgliedschaften bestehen, kann es jedoch wieder zu einer Verschachtelung kommen. Der sendende Knoten stellt sicher, dass keine sensitiven Informationen (Passworthashes) gesendet werden, der empfangende Knoten stellt sicher, dass keine lokal gemanagten Entitäten überschrieben werden. Die entsprechenden Entitäten implementieren hierzu das Interface Obfuscatable. Außerdem führt der empfangende Knoten eine Sicherheitsfilterung durch und akzeptiert nur Nutzer- oder Gruppenobjekte vom sendenden Knoten. Dadurch werden einerseits die Mitgliedschaftsregeln nach obiger Tabelle sichergestellt und andererseits unterbunden, dass Objekte von Drittknoten beeinflusst werden und die Komplexität der Nutzerverwaltung unbeherrschbar wird.

Access Controlled Objects (z.B. Collections) werden nur zur Laufzeit ausgetauscht und nicht in der Datenbank des Systems persistiert. Sie stehen dem jeweils angemeldeten Nutzer im Rahmen seiner Session zur Verfügung. Damit wird die Menge der zu synchronisierenden Daten begrenzt. Eine Persistierung würde ansonsten die Synchronisierung aller verbundenen Objekte (ACLs, Nutzer, Gruppen, ggf. Mitgliedschaften) erfordern. Dies würde die Komplexität massiv erhöhen und ist deswegen nicht erwünscht. Der Abruf der Access Controlled Objects erfolgt, nachdem die Nutzerdaten übertragen wurden. Damit ist sichergestellt, dass die für den Nutzer zum aktuellen Zeitpunkt effektiven Berechtiungen angewendet werden. Im Übrigen verfolgt die Leibniz Bioactives Cloud das Konzept der eventuellen Konsistenz, d. h. Berechtigungsänderungen die während der Laufzeit einer Session vorgenommen werden, werden erst bei der nächsten Anmeldung berücksichtigt. Dies schließt ein, dass ein Nutzer Zugriff auf Ressourcen erlangt, für die Ihm der Zugriff kürzlich entzogen wurde.

Ein schwerwiegendes Problem stellt das Löschen von Nutzern oder Gruppen dar, da andere Objekte - auch auf entfernten Knoten - direkt oder indirekt davon abhängen (referentielle Integrität). Zudem könnte ein entfernter Knoten zum Löschzeitpunkt vorübergehend nicht erreichbar sein. Aus diesem Grund werden Objekte nicht gelöscht sondern lediglich deaktiviert und unsichtbar geschaltet. Evtl. könnte dies über ein Ablaufdatum erfolgen.

> **Info:** Die obfuscate()-Methode könnte entweder durch eine Annotation ersetzt werden oder als Implementierung eines Interfaces formalisiert werden. Die Nutzung der @Transient-Annotation bzw. des transient-Keywords ist leider nicht zielführend. Das Problem liegt darin, dass die sensitiven Daten lokal persistiert und ggf. auch für lokale Browser / Administratoren serialisiert werden müssen, andererseits die lokale Einrichtung jedoch nicht in serialisierter Form verlassen dürfen.
