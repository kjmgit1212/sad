You can use the example Linux service scripts provided here to set up DB2 
Universal Database, WebSphere Application Server, and IBM Tivoli Directory 
Server as Linux services (to be started upon system reboot).

Use the RedHat scripts for RedHat Linux and SuSE scripts for SuSE Linux.

Please see the comments in these scripts for details on how to configure and use 
them.  These scripts start up these services after a reboot.

db2itim   - service script for DB2 Universal Database
websphere - service script for WebSphere Application Server
ibmdir    - service script for IBM Tivoli Directory Server

service   - helper script for SuSE Linux services
            (Similar to service command on RedHat)

Please note, if you use these scripts, then you need to remove or comment 
out the following lines in your /etc/inittab file to avoid start up conflicts:
 
fmc:2345:respawn:/opt/IBM/db2/V9.1/bin/db2fmcd #DB2 Fault Monitor Coordinator
ids0:2345:once:/opt/ibm/ldap/V6.1/sbin/ibmdiradm -I itimldap > /dev/null 2>&1 #Autostart IBM LDAP Admin Daemon Instance
 
We suggest you use these sample scripts instead of these lines in the inittab
file since these scripts provide a way to order the services for restart after a reboot.
(The chkconfig command handles the order.  See comments in the scripts for details.)
