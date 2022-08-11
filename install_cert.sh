#!/bin/bash
# set -e
for host in $HOST_CERTS
do
	echo $host
	if [ ! -z ${host+x} ] && [ ! -f $host.cert ]; then
		echo $host
		openssl s_client -showcerts -servername $host -connect $host:443 </dev/null | openssl x509 -outform pem > $host.cert && \
		keytool -import -noprompt -trustcacerts -alias $host -file $host.cert -keystore $JAVA_HOME/lib/security/cacerts -storepass changeit
	  
	fi
done