Technischer Kontext
===================

Der Knoten wird als einzelner Docker-Host realisiert, der in eine DMZ eingebettet ist. Ein Scale-Out (z.B. mit Kubernetes oder Docker Swarm) ist momentan nicht geplant. Wie bei Docker üblich stellt der Host Speicherplatz zur Verfügung und sorgt für die (Netzwerk-)Konnektivität der Komponenten. Dabei ist wichtig, dass der Speicherplatz nicht über Protokolle wie NFS oder CIFS angebunden ist, sondern als interner Plattenspeicher, DAS oder SAN-Speicher zur Verfügung gestellt wird. Das System kann als physischer Server oder virtuelle Maschine realisiert sein. Sämtliche Kommunikation findet über die Ethernet-Schnittstelle(n) des Hosts statt. Die Unterscheidung zwischen institutsinterner und externer Kommunikation wird dabei durch die Firewalls der DMZ getroffen. Eine Unterscheidung durch mehrere Schnittstellen des Hosts ist im Docker-Szenario nicht vorgesehen und müsste manuell durch einen Paketfilter (Firewall) im Docker-Host realisiert werden.

.. image:: img/DMZ-Networking.svg
    :width: 80%
    :align: center
    :alt: DMZ: network traffic


Die Netzwerkverbindungen des Knotens (Docker Host) lassen sich wie folgt charakterisieren:

.. tabularcolumns:: |l|p{8cm}|

+---+----------------------------------------------------------------------------+
|   | **Erläuterung**                                                            |
+---+----------------------------------------------------------------------------+
| A | Der Knoten initiiert und empfängt HTTPS-Verbindungen zu bzw. auf           |
|   | Port 8443 für die Maschine-zu-Maschine-Kommunikation. Die Verbindungen     |
|   | sind durch gegenseitige zertifikatsbasierte Authentifizierung abgesichert. |
+---+----------------------------------------------------------------------------+
| B | Der Knoten initiiert HTTP- und HTTPS-Verbindungen zu mehreren              |
|   | Software-Repositories.                                                     |
+---+----------------------------------------------------------------------------+
| C | Der Knoten empfängt HTTPS-Verbindungen auf Port 443 von den Nutzern seines |
|   | Instituts. Anfragen auf Port 80 (HTTP) werden auf Port 443 (HTTPS)         |
|   | umgeleitet.  Sofern vom IT-Verantwortlichen ein "offizielles" Zertifikat   |
|   | zur Verfügung gestellt wurde, wird dieses verwendet. Ansonsten ist eine    |
|   | Fallback-Lösung implementiert, die allerdings zu Warnungen in den          |
|   | Browsern der Nutzer (bei einigen Browsern auch Unbenutzbarkeit) führt.     |
|   | Die Auswirkungen des Fallbacks sind auf das jeweilige Institut             |
|   | beschränkt.                                                                |
|   | Unter Umständen fallen in die Kategorie C auch SSH-Verbindungen, die       |
|   | von IT-Verantwortlichen initiiert werden.                                  |
+---+----------------------------------------------------------------------------+
| D | Diverse, zum Teil institutsspezifische Verbindungen (DNS, NTP,             |
|   | LDAP, DHCP), die nur teilweise durch Verschlüsselung abgesichert sind.     |
+---+----------------------------------------------------------------------------+

Abhängig von den lokalen Gegebenheiten können noch weitere Verbindungen ins Intranet existieren, die hier nicht betrachtet werden und nur beispielhaft und stichpunktartig aufgeführt werden:

* SNMP-Agenten für das Monitoring
* Backup-Agenten
* SAN-Verbindungen (iSCSI, ...)

