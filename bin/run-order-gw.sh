#!/bin/bash

if [ "$#" -ne 6 ]; then
	echo ""
	echo "USAGE: $0 <solace-ip> <vpn> <user> <pass> <pub-topic> <start-id>"
	echo ""
	exit
fi
host=$1
vpn=$2
user=$3
pass=$4
topic=$5
sid=$6

cd `dirname $0`/..

 export LD_LIBRARY_PATH=/Datos/solclientj/lib:$LD_LIBRARY_PATH


java -cp /Datos/solclientj/lib/solclientj.jar:target/clustered-app2-1.0-SNAPSHOT.jar \
	-Djava.library.path=/Datos/solclientj/lib \
	com.solacesystems.poc.MockOrderGateway $host $vpn $user $pass $topic $sid

