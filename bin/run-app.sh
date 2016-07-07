#!/bin/bash

if [ "$#" -ne 9 ]; then
	echo ""
	echo "USAGE: $0 <solace-ip> <appname> <instance#> <vpn> <user> <pass> <queue> <lvq> <out-topic>"
	echo ""
	exit
fi
host=$1
app=$2
inst=$3
vpn=$4
user=$5
pass=$6
queue=$7
lvq=$8
outTopic=$9

cd `dirname $0`/..

export LD_LIBRARY_PATH=/Datos/solclientj/lib:$LD_LIBRARY_PATH

classpath="/Datos/solclientj/lib/solclientj.jar:target/clustered-app2-1.0-SNAPSHOT.jar"
java -cp $classpath -Djava.library.path=/Datos/solclientj/lib \
	com.solacesystems.poc.SampleClusteredApp $host $app $inst $vpn $user $pass $queue $lvq $outTopic

