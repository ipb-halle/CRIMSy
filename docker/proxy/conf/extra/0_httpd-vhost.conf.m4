include(dist/etc/config_m4.inc)dnl
# 
# Default Virtual Host
# Required modules: mod_log_config
#
<VirtualHost *:80>
    ServerAdmin LBAC_MANAGER_EMAIL
    ServerName LBAC_INTRANET_FQHN
    Redirect / https://LBAC_INTRANET_FQHN/

#    DocumentRoot /usr/local/apache2/htdocs 
#    HostnameLookups Off
#    UseCanonicalName Off

    # configures the footer on server-generated documents
    ServerSignature On

    <IfModule headers_module>
        Header always set X-Frame-Options "deny"
LBAC_HSTS_ENABLE   Header always set Strict-Transport-Security "max-age=3600"
    </IfModule>

</VirtualHost>

