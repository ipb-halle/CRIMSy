include(dist/etc/config_m4.inc)dnl
[Unit]
Description=Leibniz Bioactives Cloud
After=docker.service 
Before=

[Service]
User=root
Group=root
Type=oneshot
RemainAfterExit=yes
TimeoutStartSec=600s
ExecStart=LBAC_DATASTORE/dist/bin/lbacInit.sh start
ExecStop=LBAC_DATASTORE/dist/bin/lbacInit.sh stop 

[Install]
WantedBy=multi-user.target

