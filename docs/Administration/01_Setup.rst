**********************
Administrator-Handbuch
**********************
Die Administrator- und Entwicklerdokumentation des Projekts setzt sich aus der JavaDoc-Dokumentation, der Systembeschreibung und diesem Handbuch zusammen. Die JavaDoc-Dokumentation spezifiziert dabei Details, während die Systembeschreibung einen größeren Zusammenhang herstellt. Dieses Handbuch beschreibt, wie man aus den Projektquellen eine neue Cloud aufsetzt, d.h. die Quellen übersetzt, die Cloud konfiguriert und die Softwareverteilung organisiert. Das Handbuch fokussiert dabei auf die praktischen Aspekte dieser Tätigkeiten. 

Setup
=====
Zur Einrichtung und zum Betrieb einer Cloud-Instanz mit ein oder mehreren Knoten sind folgende Komponenten notwendig:

* Ein Linux-Rechner mit Java, Maven, Git, OpenSSL usw. für den Build-Prozess; dieser Rechner sollte gut abgesichert sein (Backup, sichere Konfiguration), da die Zertifizierungsstelle der Cloud-Instanz auf diesem Rechner betrieben wird.
* Ein Web-Server mit SSH-Zugang für die Softwareverteilung; der SSH-Zugang kann auf den Buildrechner beschränkt sein.
* Ein oder mehrere Cloud-Knoten; einer dieser Knoten muss die Rolle des Master-Knotens übernehmen

Notfalls können alle Komponenten auf einem Rechner zusammengefasst werden - dies sollte bei Produktivsystemen aber vermieden werden. Das Handbuch setzt grundlegende Fertigkeiten in der Administration von Linux-Rechnern voraus. Soweit nicht explizit anders gefordert, sollten alle Schritte mit einem unprivilegierten Nutzeraccount durchgeführt werden.

Playground
----------
Eine Tabelle

.. tabularcolumns:: |l|p{8cm}|

+-------------+------------------------------------------------------+
| Typ         | Beschreibung                                         |
+=============+======================================================+
| `BUILTIN`   | vordefinierte Nutzer und Gruppen,                    |
|             | z.B. `Public Account`; keine Änderungen möglich      |
|             |                                                      |
|             | Das ist ein sehr langer Tabelleneintrag, so dass     |
|             | die Zeile eigentlich umgebrochen werden sollte.      |
|             | Ob Sphinx das drauf hat?                             |
+-------------+------------------------------------------------------+
| `LOCAL`     | lokale Nutzer und Gruppen des Knotens                |
+-------------+------------------------------------------------------+
| `LDAP`      | lokale Nutzer und Gruppen aus einem LDAP-Verzeichnis |
+-------------+------------------------------------------------------+
| `REMOTE`    | remote Nutzer, d.h. Nutzer eines anderen Knotens     |
+-------------+------------------------------------------------------+


