#!/bin/bash
#
# Collect installation data 
# Cloud Resource & Information Management System
#
# Copyright 2020 Leibniz-Institut f. Pflanzenbiochemie 
#
#  Licensed under the Apache License, Version 2.0 (the "License");
#  you may not use this file except in compliance with the License.
#  You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
#  Unless required by applicable law or agreed to in writing, software
#  distributed under the License is distributed on an "AS IS" BASIS,
#  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
#  See the License for the specific language governing permissions and
#  limitations under the License.
#
#
#==========================================================
#
# The variables LBAC_DISTRIBUTION_POINT and CLOUD_NAME are
# adjusted by the upload.sh script, which takes the actual 
# values from the CA configuration. 
#
# Currently only a single code signing key is supported
#

#
LBAC_DISTRIBUTION_POINT="CLOUDCONFIG_DOWNLOAD_URL"
CLOUD_NAME="CLOUDCONFIG_CLOUD_NAME"

#
LBAC_CONFIG=etc/config.sh
LBAC_CURRENT_CONFIG_VERSION=3
LBAC_INSTALLER=bin/install.sh

LBAC_SSL_DEVCERT=devcert.pem
LBAC_SSL_CHAIN=chain.txt
LBAC_SSL_KEYFILE=lbac_cert.key
LBAC_SSL_PWFILE=lbac_cert.passwd
LBAC_SSL_REQ=lbac_cert.req

LBAC_OFFICIAL_CERT=official_cert.pem
LBAC_OFFICIAL_KEYFILE=official_cert.key
LBAC_OFFICIAL_PWFILE=official_cert.passwd
#
#==========================================================
#
function dialog_START {
	dialog --backtitle "$CLOUD_NAME" \
	  --msgbox "Dieses Programm erhebt Daten für die Installation eines Knotens der $CLOUD_NAME. Bitte starten Sie dieses Programm auf dem Rechner und mit dem Account, mit dem später der Cloud-Knoten betrieben werden soll. Nähere Informationen und insbesondere Sicherheitshinweise entnehmen Sie bitte dem Konfigurationshandbuch: 

$LBAC_DISTRIBUTION_POINT/ConfigManual.pdf 

Sie können das Programm jederzeit mit ESC oder Ctrl-C abbrechen. " \
	15 72
	case $? in
	0)
		NEXT_FORM=DIALOG_DATASTORE
		;;
	*)
		NEXT_FORM=DIALOG_ABORT
		;;
	esac
}

function dialog_DATASTORE {
	if test -r $HOME/.lbac ; then
		. $HOME/.lbac
	fi
	if test -z "$LBAC_DATASTORE" ; then
		LBAC_DATASTORE=$HOME
	fi
	dialog --backtitle "$CLOUD_NAME" \
	  --cancel-label "Abbrechen" \
	  --inputbox "Welches Verzeichnis kann der Knoten als Speicherplatz verwenden? Es darf sich nicht um ein per NFS gemountetes Verzeichnis handeln. Bitte geben Sie den absoluten Pfad an. Default: $LBAC_DATASTORE" \
	15 72 "$LBAC_DATASTORE" 2>$TMP_RESULT
	case $? in
		0)
			LBAC_DATASTORE=`cat $TMP_RESULT`
			if test `echo $LBAC_DATASTORE | cut -c1` != "/" ; then
				NEXT_FORM="ERROR: Kein absoluter Pfad angegeben."
			else
				echo "LBAC_DATASTORE=\"$LBAC_DATASTORE\"" > $HOME/.lbac && \
				echo "LBAC_DATASTORE=\"$LBAC_DATASTORE\"" >> $TMP_CONFIG && \
				NEXT_FORM="ERROR: Funktion makeDirectories fehlgeschlagen."
				makeDirectories && NEXT_FORM=DIALOG_CONFIG_OLD
			fi
			;;
		*)
			NEXT_FORM=DIALOG_ABORT
			;;
	esac
}

function dialog_CONFIG_OLD {
	NEXT_FORM=DIALOG_OBJECT_IDS
	if test ! -r $LBAC_DATASTORE/$LBAC_CONFIG ; then
		return
	fi
	dialog --backtitle "$CLOUD_NAME" \
	  --yes-label "Ja" --no-label "Nein" \
	  --yesno "Es wurde eine Konfigurationsdatei gefunden. Soll die Konfigurationsdatei eingelesen werden?  

Die empfohlene Antwort ist 'Ja'" \
	15 72
	case $? in
	0)
		# safeguard if directory has moved
		tmp=$LBAC_DATASTORE
		. $LBAC_DATASTORE/$LBAC_CONFIG
		LBAC_DATASTORE=$tmp
		
		NEXT_FORM=DIALOG_OBJECT_IDS
		;;
	1)
		NEXT_FORM=DIALOG_OBJECT_IDS
		;;
	*)
		NEXT_FORM=DIALOG_ABORT
		;;
	esac
}

function dialog_OBJECT_IDS {
	test -n "$LBAC_NODE_ID" || LBAC_NODE_ID=`uuidgen -r | tr -d $'\n'`

	dialog --backtitle "$CLOUD_NAME" \
          --title "Object Identifier" \
          --ok-label "Ok" --cancel-label "Abbrechen" \
          --insecure \
          --mixedform "Bitte ändern Sie die Werte nur nach Aufforderung!" \
          15 72 0 \
          "Node ID             :"  2 2 "$LBAC_NODE_ID"              2 26 42 0 0 \
          2>$TMP_RESULT
	case $? in
		0)
			LBAC_NODE_ID=`cat $TMP_RESULT | head -1 | tr -d $'\n'`
			echo "LBAC_NODE_ID=\"$LBAC_NODE_ID\"" >> $TMP_CONFIG
			NEXT_FORM=DIALOG_HARDWARE_OK
			;;
		*)
			NEXT_FORM=DIALOG_ABORT
			;;
	esac
}

function dialog_HARDWARE_OK {
	dialog --backtitle "$CLOUD_NAME" \
	  --yes-label "Ja" --no-label "Nein" \
	  --yesno "Verfügt Ihr Knoten über mindestens 2 GByte RAM, 20 GByte Disk-Speicher und eine 10 MBit Netzwerkanbindung?" \
	15 72 
	case $? in
	0)
		# alles Ok.
		NEXT_FORM=DIALOG_PROXY_MODE 
		;;
	1)
		dialog --backtitle "$CLOUD_NAME" \
		  --infobox "Abbruch: Die Mindestanforderungen an Speicherplatz und Netzwerkanbindungen müssen erfüllt werden." \
		15 72
		NEXT_FORM=__END__
		;;
	*)
		NEXT_FORM=DIALOG_ABORT
		;;
	esac
}

function dialog_INSTITUTION_FULL {
	dialog --backtitle "$CLOUD_NAME" \
	  --cancel-label "Abbrechen" \
	  --inputbox "Bitte geben Sie den vollen Namen Ihrer Institution an:" \
	15 72 "$LBAC_INSTITUTION" 2>$TMP_RESULT

	case $? in
	0)
		# alles Ok
		LBAC_INSTITUTION=`cat $TMP_RESULT`
		echo "LBAC_INSTITUTION=\"$LBAC_INSTITUTION\"" >> $TMP_CONFIG
		echo -n "LBAC_INSTITUTION_MD5=\"" >> $TMP_CONFIG
		echo -n $LBAC_INSTITUTION | md5sum | cut -c1-32 | tr -d $'\n' >> $TMP_CONFIG
		echo "\"" >> $TMP_CONFIG
		NEXT_FORM=DIALOG_INSTITUTION_SHORT
		;;
	*)
		NEXT_FORM=DIALOG_ABORT
		;;
	esac
}

function dialog_INSTITUTION_SHORT {
	dialog --backtitle "$CLOUD_NAME" \
	  --cancel-label "Abbrechen" \
	  --inputbox "Bitte geben Sie einen Kurznamen Ihrer Institution an (z.B. 'DSMZ Braunschweig', 'IPB Halle', 'HKI Jena', ...):" \
	15 72 "$LBAC_INSTITUTION_SHORT" 2>$TMP_RESULT

	case $? in
	0)
		# alles Ok
		LBAC_INSTITUTION_SHORT=`cat $TMP_RESULT`
		echo "LBAC_INSTITUTION_SHORT=\"$LBAC_INSTITUTION_SHORT\"" >> $TMP_CONFIG
		NEXT_FORM=DIALOG_ADMIN_EMAIL
		;;
	*)
		NEXT_FORM=DIALOG_ABORT
		;;
	esac
}


function dialog_ADMIN_EMAIL {
	dialog --backtitle "$CLOUD_NAME" \
	  --cancel-label "Abbrechen" \
	  --inputbox "Bitte geben Sie eine Email-Adresse an, unter der wir Sie (bzw. den oder die Verantwortlichen für Ihren Knoten) erreichen können." \
	15 72 "$LBAC_MANAGER_EMAIL" 2>$TMP_RESULT

	case $? in
		0)
			# alles Ok.
			LBAC_MANAGER_EMAIL=`cat $TMP_RESULT`
			echo "LBAC_MANAGER_EMAIL=\"$LBAC_MANAGER_EMAIL\"" >> $TMP_CONFIG
			NEXT_FORM=DIALOG_INTRANET_FQHN 
			;;
		1)
			# Cancel
			NEXT_FORM=DIALOG_ABORT
			;;
		*)
			# Abort
			NEXT_FORM=DIALOG_ABORT
			;;
	esac
}


function dialog_INTRANET_FQHN {
	dialog --colors --backtitle "$CLOUD_NAME" \
	  --cancel-label "Abbrechen" \
	  --inputbox "Bitte geben Sie den vollqualifizierten Hostnamen an, unter dem Ihr Knoten aus dem \Z1\ZbIntranet\Zn erreichbar ist:" \
	15 72 "$LBAC_INTRANET_FQHN"  2>$TMP_RESULT 
	case $? in
		0)
			# alles Ok.
			LBAC_INTRANET_FQHN=`cat $TMP_RESULT`
			echo "LBAC_INTRANET_FQHN=\"$LBAC_INTRANET_FQHN\"" >> $TMP_CONFIG
			NEXT_FORM=DIALOG_INTERNET_FQHN 
			;;
		1)
			# Cancel
			NEXT_FORM=DIALOG_ABORT
			;;
		*)
			# Abort
			NEXT_FORM=DIALOG_ABORT
			;;
		esac
}


function dialog_INTERNET_FQHN {
	dialog --colors --backtitle "$CLOUD_NAME" \
	  --cancel-label "Abbrechen" \
	  --inputbox "Bitte geben Sie den vollqualifizierten Hostnamen an, unter dem Ihr Knoten für andere Knoten aus dem \Z1\ZbInternet\Zn erreichbar sein wird. Bitte geben Sie den Hostnamen auch an, wenn er mit dem Intranet-Hostnamen identisch ist." \
	15 72 "$LBAC_INTERNET_FQHN" 2>$TMP_RESULT 
	case $? in
		0)
			# alles Ok.
			LBAC_INTERNET_FQHN=`cat $TMP_RESULT`
			echo "LBAC_INTERNET_FQHN=\"$LBAC_INTERNET_FQHN\"" >> $TMP_CONFIG
			NEXT_FORM=DIALOG_CERT_REQUEST
			;;
		1)
			# Cancel
			NEXT_FORM=DIALOG_ABORT
			;;
		*)
			# Abort
			NEXT_FORM=DIALOG_ABORT
			;;
		esac
}

function dialog_PROXY_MODE {
	if test -z "$LBAC_PROXY_MODE" ; then
		LBAC_PROXY_MODE=ON 
	fi
        if test -z "$LBAC_PROXY_HSTS" ; then
                LBAC_PROXY_HSTS=OFF
        fi
	dialog --backtitle "$CLOUD_NAME" \
	  --cancel-label "Abbrechen" \
          --checklist "Falls Sie über eine fortgeschrittene Firewall mit Proxy-Funktion und ggf. Intrusion Detection verfügen, können Sie optional auf die Konfiguration eines Proxy-Containers verzichten. Sie müssen Ihren Proxy dann manuell konfigurieren. Bitte konsultieren Sie hierzu das Handbuch. " 15 72 2 \
          PROXY "Proxy-Container automatisch konfigurieren" $LBAC_PROXY_MODE \
          HSTS "HTTP Strict Transport Security (HSTS) aktivieren" $LBAC_PROXY_HSTS 2> $TMP_RESULT
	case $? in
		0)
                        if grep -q PROXY $TMP_RESULT ; then
                            echo "LBAC_PROXY_MODE=\"ON\"" >> $TMP_CONFIG
                        else
                            echo "LBAC_PROXY_MODE=\"OFF\"" >> $TMP_CONFIG
                        fi
                        if grep -q HSTS $TMP_RESULT ; then
                            echo "LBAC_PROXY_HSTS=\"ON\"" >> $TMP_CONFIG
                        else
                            echo "LBAC_PROXY_HSTS=\"OFF\"" >> $TMP_CONFIG
                        fi
			NEXT_FORM=DIALOG_INIT_TYPE
			;;
		*)
			NEXT_FORM=DIALOG_ABORT
			;;
	esac
}

function dialog_INIT_TYPE {
    case "$LBAC_INIT_TYPE" in
        SYSTEMD)
            tmp_systemd=ON
            tmp_sysv=OFF
            ;;
        SYSV)
            tmp_systemd=OFF
            tmp_sysv=ON
            ;;
        *)
            LBAC_INIT_TYPE=SYSTEMD
            tmp_systemd=ON
            tmp_sysv=OFF
            ;;
    esac
    dialog --backtitle "$CLOUD_NAME" \
      --cancel-label "Abbrechen" \
      --radiolist "Bitte wählen Sie den Typ des Init-Systems auf dem Knoten" 15 72 2 \
        SYSTEMD "SystemD (default)" $tmp_systemd \
        SYSV "SysV-Init (auch Fallback für andere Init-Systeme)" $tmp_sysv 2> $TMP_RESULT
    case $? in
        0)
            LBAC_INIT_TYPE=`cat $TMP_RESULT | head -1 | tr -d $'\n'`
            case "$LBAC_INIT_TYPE" in
                SYSTEMD|SYSV)
                    echo "LBAC_INIT_TYPE=\"$LBAC_INIT_TYPE\"" >> $TMP_CONFIG
                    ;;
                *)
                    echo "LBAC_INIT_TYPE=\"SYSTEMD\"" >> $TMP_CONFIG
            esac
            NEXT_FORM=DIALOG_DOCKER_HOST 
            ;;
        *)
            NEXT_FORM=DIALOG_ABORT
            ;;
    esac

}

function dialog_DOCKER_HOST {
    if test -z "$LBAC_DOCKER_EXCLUSIVE" ; then
        LBAC_DOCKER_EXCLUSIVE="ON"
    fi
    dialog --backtitle "$CLOUD_NAME" \
      --cancel-label "Abbrechen" \
      --checklist "Steht der Knoten / Docker-Host exklusiv für die $CLOUD_NAME zur Verfügung (empfohlene Einstellung: EXCLUSIV)? Falls auf dem dem Docker-Host weitere Container ausgeführt werden, müssen verwaiste Docker-Container, -Images und -Volumes manuell aufgeräumt werden." 15 72 1 \
      "DOCKER" "Exklusive Ausführung der Cloud" $LBAC_DOCKER_EXCLUSIVE 2>$TMP_RESULT
    case $? in
        0)
            if grep -q DOCKER $TMP_RESULT ; then
                echo "LBAC_DOCKER_EXCLUSIVE=\"ON\"" >> $TMP_CONFIG
            else
                echo "LBAC_DOCKER_EXCLUSIVE=\"OFF\"" >> $TMP_CONFIG
            fi
            NEXT_FORM=DIALOG_INSTITUTION_FULL
            ;;
        *)
            NEXT_FORM=DIALOG_ABORT
            ;;
    esac
}

function dialog_CERT_REQUEST {
	if test -r "$LBAC_DATASTORE/etc/$LBAC_SSL_KEYFILE" \
	  -a -r "$LBAC_DATASTORE/etc/$LBAC_SSL_REQ" ; then
		dialog --backtitle "$CLOUD_NAME" \
		  --extra-button --extra-label "Ansehen" \
		  --ok-label "Ja" --cancel-label "Nein" \
		  --yesno "Es wurde ein SSL-Zertifikatsantrag mit privatem Schlüssel gefunden. Antworten Sie bitte mit 'Ja', wenn Sie den Zertifikatsantrag weiter verwenden wollen und mit 'Nein', falls Sie die Zertifikatsdaten ändern wollen. Sie müssen einen neuen Zertifikatsantrag erstellen, wenn sich die Hostnamen geändert haben!" 15 72
		case $? in
			0)
				NEXT_FORM=DIALOG_CERT_INFO
				;;
			1)
				NEXT_FORM=DIALOG_CERT_DATA
				;;
			3)
				dialog_CERT_CHECK && dialog_CERT_OK DIALOG_CERT_INFO DIALOG_CERT_REQUEST
				;;
			*)
				NEXT_FORM=DIALOG_ABORT
				;;
		esac
	else
		NEXT_FORM=DIALOG_ABORT
		dialog --backtitle "$CLOUD_NAME" \
		  --msgbox "Das SSL-Zertifikat oder der private Schlüssel wurden nicht gefunden. In den nächsten Schritten wird für Sie ein neuer Zertifikatsantrag ausgestellt." 15 72 && NEXT_FORM=DIALOG_CERT_DATA
	fi
}

function dialog_CERT_DATA {
	if test -z "$LBAC_COUNTRY" ; then 
		LBAC_COUNTRY=DE
	fi

	if test -z "$LBAC_SSL_ORGANIZATION" ; then
		LBAC_SSL_ORGANIZATION="$LBAC_INSTITUTION"
	fi

	if test -z "$LBAC_SSL_OU" ; then
		LBAC_SSL_OU="Verwaltung"
	fi

	if test -z "$LBAC_SSL_EMAIL" ; then
		LBAC_SSL_EMAIL="$LBAC_MANAGER_EMAIL"
	fi

	dialog --backtitle "$CLOUD_NAME" \
	  --title "Zertifikatsdaten" \
	  --ok-label "Ok" --cancel-label "Abbrechen" \
	  --insecure \
	  --mixedform "Tragen Sie bitte die Daten für den Zertifikatsantrag ein" \
	  $((16 + (2 * $TS_FACTOR))) 72 0 \
	  "Staat            :"  $((1 * $TS_FACTOR)) 2 "$LBAC_COUNTRY"          $((1 * $TS_FACTOR)) 21  3  0 0 \
	  "Bundesland       :"  $((2 * $TS_FACTOR)) 2 "$LBAC_STATE"	       $((2 * $TS_FACTOR)) 21 43  0 0 \
	  "Ort              :"  $((3 * $TS_FACTOR)) 2 "$LBAC_CITY"	       $((3 * $TS_FACTOR)) 21 43 64 0 \
	  "Organisation     :"  $((4 * $TS_FACTOR)) 2 "$LBAC_SSL_ORGANIZATION" $((4 * $TS_FACTOR)) 21 43 64 0 \
	  "Org.-Einheit (OU):"  $((5 * $TS_FACTOR)) 2 "$LBAC_SSL_OU"           $((5 * $TS_FACTOR)) 21 43 64 0 \
	  "Email            :"  $((6 * $TS_FACTOR)) 2 "$LBAC_SSL_EMAIL"        $((6 * $TS_FACTOR)) 21 43 64 0 \
	  2>$TMP_SSL_DATA
	case $? in
		0)
			NEXT_FORM="ERROR: Funktion makeCertReq fehlgeschlagen."
			makeCertReq && dialog_CERT_CHECK && \
			   dialog_CERT_OK DIALOG_CERT_INFO DIALOG_INTRANET_FQHN 
			;;
		*)
			NEXT_FORM=DIALOG_ABORT
			;;
	esac
}

# to be called by other dialogs only
# sets NEXT_FORM to DIALOG_ABORT and
# returns return code of dialog
function dialog_CERT_CHECK {
	NEXT_FORM=DIALOG_ABORT
        openssl req -in "$LBAC_DATASTORE/etc/$LBAC_SSL_REQ" -text > $TMP_SSL_DATA

	dialog --backtitle "$CLOUD_NAME" \
	  --title "Bitte prüfen Sie die Antragsdaten" \
	  --exit-label "Weiter" \
	  --textbox $TMP_SSL_DATA 15 72
	return $?
}

# to be called by other dialogs only
# arg1 positive outcome
# arg2 negative outcome
function dialog_CERT_OK {
	dialog --backtitle "$CLOUD_NAME" \
	  --yes-label "Ja" --no-label "Nein" \
	  --yesno "Waren die Daten im Zertifikatsantrag korrekt? Sie haben ansonsten die Möglichkeit Korrekturen vorzunehmen." 15 72
	case $? in
		0)
			NEXT_FORM=$1
			;;
		1)
			NEXT_FORM=$2 
			;;
		*)
			NEXT_FORM=DIALOG_ABORT
			;;
	esac
}

function dialog_CERT_INFO {
	echo "cat <<EOF >/dev/null" >> $TMP_CONFIG
	cat "$LBAC_DATASTORE/etc/$LBAC_SSL_REQ" >> $TMP_CONFIG
	echo "EOF" >> $TMP_CONFIG

	dialog --colors --backtitle "$CLOUD_NAME" \
	  --msgbox "Der Zertifikatsantrag wurde für Sie in der Datei 

$LBAC_DATASTORE/etc/$LBAC_SSL_REQ

gespeichert. Optional können Sie damit ein Zertifikat bei einer offiziellen CA beantragen. Das offizielle Zertifikat (mit Zertifikatskette), den privaten Schlüssel und das Schlüsselpasswort speichern Sie bitte in den Dateien

$LBAC_DATASTORE/etc/$LBAC_OFFICIAL_CERT
$LBAC_DATASTORE/etc/$LBAC_OFFICIAL_KEYFILE
$LBAC_DATASTORE/etc/$LBAC_OFFICIAL_PWFILE

bevor Sie den Installationsprozess starten. \Z1\ZbBitte schlagen Sie für nähere Erläuterungen im Installationshandbuch nach!\Zn" 19 72
	case $? in
		0)
			NEXT_FORM=DIALOG_SAVE
			;;
		*)
			NEXT_FORM=DIALOG_ABORT
			;;
	esac
}

function dialog_SAVE {
	dialog --backtitle "$CLOUD_NAME" \
	  --title "Speichern" \
	  --yes-label "Ja" --no-label "Nein" \
	  --yesno "Konfiguration speichern?" 15 72
	case $? in
	0)
		NEXT_FORM="ERROR: Fehler beim Abspeichern."
		mv $TMP_CONFIG $LBAC_DATASTORE/$LBAC_CONFIG && \
		copyInstaller && upgradeOldConfig && NEXT_FORM=DIALOG_ENCRYPT
		;;
	1)
		dialog --infobox "Die Konfiguration wurde nicht gespeichert." 15 72
		NEXT_FORM=__END__
		;;
	*)
		NEXT_FORM=DIALOG_ABORT
                ;;
	esac
}

#
# The header of the encrypted configuration file contains 
# the name of the institution and the "X509v3 Subject Key Identifier"
# of the key used to encrypt the configuration file. Both
# information is needed by the distributor to a) select and 
# b) decrypt the configuration file in the process of creating 
# the individualized software package.
#
function dialog_ENCRYPT {

	dialog --backtitle "$CLOUD_NAME" \
	  --msgbox "Die Konfigurationsdatei wird jetzt verschlüsselt. Bitte senden Sie uns die Datei $LBAC_DATASTORE/$LBAC_CONFIG.asc per Email zu. Sie erhalten dann von uns Nachricht, wann und wie Sie mit dem Installationsprozess fortfahren können. " \
	15 72
	case $? in
		0)
			NEXT_FORM="ERROR: Fehler beim Verschlüsseln."
			rm -f "$LBAC_DATASTORE/$LBAC_CONFIG.asc" && \
			cat << EOF > "$LBAC_DATASTORE/$LBAC_CONFIG.asc" && echo > $TMP_SSL_DATA && NEXT_FORM=DIALOG_END
#
# LBAC_INSTITUTION=$LBAC_INSTITUTION
# CERTIFICATE_ID=`openssl x509 -in "$LBAC_DATASTORE/etc/$LBAC_SSL_DEVCERT" -text | \
          grep -A1 "X509v3 Subject Key Identifier" | tail -1 | tr -d $' \n'`
# `date`
#
# ----- SMIME ENCRYPTED CONFIG BEGIN -----
`openssl smime -encrypt -binary -outform PEM -aes-256-cbc \
  -in "$LBAC_DATASTORE/$LBAC_CONFIG" "$LBAC_DATASTORE/etc/$LBAC_SSL_DEVCERT"`
# ----- SMIME ENCRYPTED CONFIG END -----
EOF

			;;
		*)
			NEXT_FORM=DIALOG_ABORT
			;;
	esac
}

function dialog_END {
	dialog --backtitle "$CLOUD_NAME" \
	  --infobox "Die Konfiguration ist beendet. Lassen Sie uns bitte die Datei 

$LBAC_DATASTORE/$LBAC_CONFIG.asc

zukommen. Sie erhalten dann weitere Instruktionen für die Installation von uns." 15 72
	
	NEXT_FORM=__END__
}

#
#==========================================================
#
function checkTerminal {
	if test ! -w `tty` ; then
		dialog --backtitle "$CLOUD_NAME" \
		  --infobox "FEHLER: Es gibt ein Problem mit Ihrem Terminal. Möglicherweise arbeiten Sie in einer 'su'-Session." 15 72
		NEXT_FORM=__END__
		return
	fi
} 

#
# checks software requirements and on success sets
# NEXT_FORM variable to DIALOG_START
#
function checkSoftware {
	dialog --print-version > /dev/null
	if [ $? != 0 ] ; then
		echo "ERROR: dialog nicht installiert."
		exit 1
	fi

	NEXT_FORM="ERROR: m4 nicht installiert."
	echo TEST | m4 > /dev/null || return
	NEXT_FORM="ERROR: uudecode nicht installiert."
	echo $'begin-base64 644 -\nQQo=\n====' | uudecode > /dev/null || return
	NEXT_FORM="ERROR: openssl nicht installiert."
	echo TEST | openssl md5 > /dev/null || return
	NEXT_FORM="ERROR: docker-compose nicht installiert."
	docker-compose -v > /dev/null || return
	NEXT_FORM=DIALOG_START
}

function checkTerminalSize {
	dialog --print-maxsize 2> $TMP_RESULT
	if test 28 -gt `cut -d: -f2 $TMP_RESULT | cut -d, -f1` ; then
		TS_FACTOR=1
	else
		TS_FACTOR=2
	fi
}

function checkUser {
	if test `whoami | cut -f1` = "root" -o `id -u | cut -f1` -eq 0 ; then
		dialog --backtitle "$CLOUD_NAME" \
		  --infobox "FEHLER: Das Programm soll nicht als Superuser gestartet werden." 15 72
		NEXT_FORM=__END__
		return
	fi
}

function cleanUp {
	if test -n "$TMP_CONFIG" -a -f "$TMP_CONFIG" ; then
		rm $TMP_CONFIG 
	fi
	if test -n "$TMP_RESULT" -a -f "$TMP_RESULT" ; then
		rm $TMP_RESULT
	fi
	if test -n "$TMP_SSL_DATA" -a -f "$TMP_SSL_DATA" ; then
		rm $TMP_SSL_DATA
	fi
}

function copyInstaller {
	cat >$LBAC_DATASTORE/$LBAC_INSTALLER <<EOF
#!/bin/bash
#
# INSTALLER
# Cloud Resource & Information Management System (CRIMSy)
# 
# Copyright 2020 Leibniz-Institut f. Pflanzenbiochemie 
#
#  Licensed under the Apache License, Version 2.0 (the "License");
#  you may not use this file except in compliance with the License.
#  You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
#  Unless required by applicable law or agreed to in writing, software
#  distributed under the License is distributed on an "AS IS" BASIS,
#  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
#  See the License for the specific language governing permissions and
#  limitations under the License.
#
if test ! -r \$HOME/.lbac ; then
	echo "Ihr Knoten ist nicht richtig konfiguriert"
	exit 1
fi
. \$HOME/.lbac
if test "!" "(" -n "\$LBAC_DATASTORE" -a -d "\$LBAC_DATASTORE" \
  -a -w "\$LBAC_DATASTORE" ")" ; then
	echo "LBAC_DATASTORE ist nicht definiert oder nicht schreibbar"
	exit 1
fi
. \$LBAC_DATASTORE/etc/config.sh > /dev/null
cd \$LBAC_DATASTORE
mkdir -p \$LBAC_DATASTORE/tmp 
pushd \$LBAC_DATASTORE/tmp

wget -O configure.sh.sig \$LBAC_DISTRIBUTION_POINT/configure.sh.sig || (echo "Download fehlgeschlagen" && exit 1)
openssl smime -verify -in configure.sh.sig -certfile ../etc/devcert.pem \
  -CAfile ../etc/chain.txt -out configure.sh || (echo "Entschlüsselung oder Signaturprüfung fehlgeschlagen" \
  && rm configure.sh && exit 1)

wget -O setup.asc.sig \$LBAC_DISTRIBUTION_POINT/\$LBAC_INSTITUTION_MD5.asc.sig || (echo "Download fehlgeschlagen" && exit 1)
openssl smime -verify -in setup.asc.sig -certfile ../etc/devcert.pem \
 -CAfile ../etc/chain.txt | openssl smime -decrypt -inform PEM \
 -inkey ../etc/lbac_cert.key -passin file:../etc/lbac_cert.passwd \
 -out setup.sh || (echo "Entschlüsselung oder Signaturprüfung fehlgeschlagen" && rm setup.sh && exit 1)
chmod +x setup.sh configure.sh
mv configure.sh ../bin
DATE=\`date "+%Y%m%d%H%M%S"\`
./setup.sh \$* 2>&1 | tee "\$LBAC_DATASTORE/tmp/setup.\$DATE.log" \
 || ( echo "Setup abgebrochen" && exit 1)
popd >/dev/null
echo "Setup-Log: \$LBAC_DATASTORE/tmp/setup.\$DATE.log"
EOF
chmod +x "$LBAC_DATASTORE/$LBAC_INSTALLER"


}

#
# Upgrade from config version 2 to version 3
#
function upgradeOldConfig {
	if test $LBAC_CONFIG_VERSION = 2 ; then

		if test -f $LBAC_DATASTORE/etc/lbac_devcert.pem ; then 
			mv $LBAC_DATASTORE/etc/lbac_devcert.pem $LBAC_DATASTORE/etc/devcert.pem
		fi
		if test -f $LBAC_DATASTORE/etc/lbac_cacert.pem ; then

			cat <<EOF > $LBAC_DATASTORE/etc/chain.txt
/C=DE/ST=Sachsen-Anhalt/L=Halle (Saale)/O=Leibniz-Institut fuer Pflanzenbiochemie (IPB) Halle/OU=Abt. Natur- und Wirkstoffchemie/CN=Leibniz Bioactives Cloud CA 1/emailAddress=fbroda@ipb-halle.de

`cat $LBAC_DATASTORE/etc/lbac_cacert.pem`
EOF

			rm $LBAC_DATASTORE/etc/lbac_cacert.pem
		fi

		touch $LBAC_DATASTORE/dist/dirty
	fi
}

# function installKeys {
#	wget -O $LBAC_DATASTORE/etc/$LBAC_SSL_CHAIN $LBAC_DISTRIBUTION_POINT/$LBAC_SSL_CHAIN
#	wget -O $LBAC_DATASTORE/etc/$LBAC_SSL_DEVCERT $LBAC_DISTRIBUTION_POINT/$LBAC_SSL_DEVCERT
# }
 
function makeCertReq {
	LBAC_COUNTRY=`head -1 $TMP_SSL_DATA | tr -d $'\n'`
	LBAC_STATE=`head -2 $TMP_SSL_DATA | tail -1 | tr -d $'\n'`
	LBAC_CITY=`head -3 $TMP_SSL_DATA | tail -1 | tr -d $'\n'`
	LBAC_SSL_ORGANIZATION=`head -4 $TMP_SSL_DATA | tail -1 | tr -d $'\n'`
	LBAC_SSL_OU=`head -5 $TMP_SSL_DATA | tail -1 | tr -d $'\n'`
	LBAC_SSL_EMAIL=`head -6 $TMP_SSL_DATA | tail -1 | tr -d $'\n'`

	echo "LBAC_COUNTRY=\"$LBAC_COUNTRY\"" >> $TMP_CONFIG
	echo "LBAC_STATE=\"$LBAC_STATE\"" >> $TMP_CONFIG
	echo "LBAC_CITY=\"$LBAC_CITY\"" >> $TMP_CONFIG
	echo "LBAC_SSL_ORGANIZATION=\"$LBAC_SSL_ORGANIZATION\"" >> $TMP_CONFIG
	echo "LBAC_SSL_OU=\"$LBAC_SSL_OU\"" >> $TMP_CONFIG
	echo "LBAC_SSL_EMAIL=\"$LBAC_SSL_EMAIL\"" >> $TMP_CONFIG

	TMP_SSL_SUBJECT="/C=$LBAC_COUNTRY/ST=$LBAC_STATE/L=$LBAC_CITY"
	TMP_SSL_SUBJECT="$TMP_SSL_SUBJECT/O=$LBAC_SSL_ORGANIZATION/OU=$LBAC_SSL_OU"
	TMP_SSL_SUBJECT="$TMP_SSL_SUBJECT/CN=$LBAC_INTRANET_FQHN"

	cat <<EOF > $TMP_SSL_DATA
[req]
  default_bits = 4096
  default_md = sha256
  req_extensions = v3_req
  distinguished_name = req_distinguished_name

[req_distinguished_name]

[v3_req]
  basicConstraints = CA:FALSE
  nsCertType = server
  keyUsage = critical, digitalSignature, keyEncipherment, keyAgreement
  subjectAltName = @altNames

[altNames]
  email = $LBAC_SSL_EMAIL
  DNS.1 = $LBAC_INTRANET_FQHN
  DNS.2 = $LBAC_INTERNET_FQHN
EOF
	if test $LBAC_INTRANET_FQHN != $LBAC_INTERNET_FQHN ; then
		echo "  DNS.2 = $LBAC_INTERNET_FQHN" >> $TMP_SSL_DATA
	fi

	uuidgen -r | tr -d $'\n' > "$LBAC_DATASTORE/etc/$LBAC_SSL_PWFILE" 

	openssl req -outform PEM -out "$LBAC_DATASTORE/etc/$LBAC_SSL_REQ" \
	  -new -keyout "$LBAC_DATASTORE/etc/$LBAC_SSL_KEYFILE" \
	  -newkey rsa:4096 -sha256 -utf8 \
	  -passout "file:$LBAC_DATASTORE/etc/$LBAC_SSL_PWFILE" \
	  -subj "$TMP_SSL_SUBJECT" \
	  -config $TMP_SSL_DATA

#
#	deactivated!
#	installKeys
#
}

function makeDirectories {
	mkdir -p $LBAC_DATASTORE/etc && \
	mkdir -p $LBAC_DATASTORE/bin

	# clean up certificates from download and 
	# verification (see upload.sh)
	test -s "$LBAC_SSL_CHAIN" && mv -f "$LBAC_SSL_CHAIN" "$LBAC_DATASTORE/etc" 
	test -s "$LBAC_SSL_DEVCERT" &&  mv -f "$LBAC_SSL_DEVCERT" "$LBAC_DATASTORE/etc"
	return 0
}

function makeTempConfig {
	TMP_RESULT=`mktemp /tmp/lbac_result.XXXXXX`
	TMP_SSL_DATA=`mktemp /tmp/lbac_ssl_data.XXXXXX`
	TMP_CONFIG=`mktemp /tmp/lbac_config.XXXXXX`
	cat >$TMP_CONFIG <<EOF
#
# $CLOUD_NAME 
# public configuration
# `date`
#
LBAC_CONFIG_VERSION="$LBAC_CURRENT_CONFIG_VERSION"
LBAC_DISTRIBUTION_POINT="$LBAC_DISTRIBUTION_POINT"
EOF
}

#
#==========================================================
#
# line drawing in UTF-8 PuTTY
export NCURSES_NO_UTF8_ACS=1

umask 0077
checkSoftware
if test $NEXT_FORM = DIALOG_START ; then
	checkTerminal
	checkUser
	makeTempConfig
	checkTerminalSize
fi

while true ; do 

	case $NEXT_FORM in

	DIALOG_START)
		dialog_START
		;;
	DIALOG_CONFIG_OLD)
		dialog_CONFIG_OLD
		;;
	DIALOG_INSTITUTION_FULL)
		dialog_INSTITUTION_FULL
		;;
	DIALOG_INSTITUTION_SHORT)
		dialog_INSTITUTION_SHORT
		;;
	DIALOG_OBJECT_IDS)
		dialog_OBJECT_IDS
		;;
	DIALOG_HARDWARE_OK)
		dialog_HARDWARE_OK
		;;
	DIALOG_ADMIN_EMAIL)
		dialog_ADMIN_EMAIL
		;;
	DIALOG_CERT_REQUEST)
		dialog_CERT_REQUEST
		;;
	DIALOG_CERT_DATA)
		dialog_CERT_DATA
		;;
	DIALOG_CERT_INFO)
		dialog_CERT_INFO
		;;
	DIALOG_INTRANET_FQHN)
		dialog_INTRANET_FQHN
		;;
	DIALOG_INTERNET_FQHN)
		dialog_INTERNET_FQHN
		;;
	DIALOG_PROXY_MODE)
		dialog_PROXY_MODE
		;;
	DIALOG_DATASTORE)
		dialog_DATASTORE
		;;
        DIALOG_INIT_TYPE)
                dialog_INIT_TYPE
                ;;
        DIALOG_DOCKER_HOST)
                dialog_DOCKER_HOST
                ;;
	DIALOG_SAVE)
		dialog_SAVE
		;;
	DIALOG_ENCRYPT)
		dialog_ENCRYPT
		;;
	DIALOG_END)
		dialog_END
		;;
	DIALOG_ABORT)
		dialog --backtitle "$CLOUD_NAME" \
		  --infobox "Das Programm wurde abgebrochen" 15 72
		cleanUp
		exit 1
		;;
	__END__)
		cleanUp
                echo "cleaning up intermediate files"
                rm configure.sh.sig chain.txt devcert.pem
                echo "Moving configure.sh to $LBAC_DATASTORE/bin"
                mv $0 "$LBAC_DATASTORE/bin"
		exit 0
		;;
	*)
		dialog --backtitle "$CLOUD_NAME" \
		  --infobox "Ein Fehler ist aufgetreten: $NEXT_FORM" 15 72
		cleanUp
		exit 1
		;;
	esac
done


