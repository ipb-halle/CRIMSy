include(dist/etc/config_m4.inc)dnl
##
## SSL Virtual Host Context
##

<VirtualHost _default_:443>

	#   General setup for the virtual host
	ServerName LBAC_INTRANET_FQHN:443
	ServerAdmin LBAC_MANAGER_EMAIL
	DocumentRoot "/usr/local/apache2/htdocs"

	SSLEngine on

	SSLCertificateFile "/usr/local/apache2/conf/official_cert.pem"
	SSLCertificateKeyFile "/usr/local/apache2/conf/official_cert.key"

	# CRL not necessary for intranet / offical certificate 


	<IfModule mod_proxy.c>

		ProxyPass         /ui http://ui:8080/ui
		ProxyPassReverse  /ui http://ui:8080/ui

	</IfModule>

        <IfModule headers_module>
                Header always set X-Frame-Options "deny"
LBAC_HSTS_ENABLE               Header always set Strict-Transport-Security "max-age=3600"
        </IfModule>

</VirtualHost>                                  
