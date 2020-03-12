include(dist/etc/config_m4.inc)dnl
##
## SSL Virtual Host Context
##

<VirtualHost _default_:8443>

	#   General setup for the virtual host
	ServerName LBAC_INTERNET_FQHN:8443
	ServerAdmin LBAC_MANAGER_EMAIL
	DocumentRoot "/usr/local/apache2/htdocs"

	SSLEngine on

	SSLCertificateFile "/usr/local/apache2/conf/lbac_cert.pem"
	SSLCertificateKeyFile "/usr/local/apache2/conf/lbac_cert.key"

	# Certificate Revocation Lists (CRL):
	SSLCARevocationCheck chain
        SSLCARevocationPath "/usr/local/apache2/conf/crl/"

	SSLVerifyClient require
	SSLVerifyDepth 3
        SSLCACertificatePath "/usr/local/apache2/conf/crt/"

	<IfModule mod_proxy.c>

		ProxyPass         /ui/rest  http://ui:8080/ui/rest
		ProxyPassReverse  /ui/rest  http://ui:8080/ui/rest
                ProxyPass         /ui/servlet/document http://ui:8080/ui/servlet/document
                ProxyPassReverse  /ui/servlet/document http://ui:8080/ui/servlet/document

	</IfModule>

</VirtualHost>                                  
