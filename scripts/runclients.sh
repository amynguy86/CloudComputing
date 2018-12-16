#!/bin/bash


cd /root/CloudComputing/client_simulator

numclients=$1
clientType=$2

if [ "$clientType" != "CephMDS" ] && [ "$clientType" != "CassandraMDS" ]; then
	echo "Incorrect client type"
	exit 1
fi

if [ "$3" != "create" ] && [ "$3" != "delete" ]; then
	echo "fail. allowed options are create and delete"
	exit 1
fi

ip=$4

filename=/root/partial.txt
numclients=$((numclients+0))
counter=1
while [[ $counter -le numclients ]]
do
	if [ "$3" = "create" ]; then
		mvn spring-boot:run -Dspring-boot.run.arguments="${filename},--server=$ip,--cloud.client.type=cs6343.${clientType},--cloud.command.line=false,--logging.level.root=info,--logging.file=/root/${clientType}Client/client${counter},--logging.file.max-size=50MB,--test.type=RequestTest,--test.should.delete=false,--test.should.add=true" &> /dev/null &
	else
		mvn spring-boot:run -Dspring-boot.run.arguments="${filename},--server=$ip,--cloud.client.type=cs6343.${clientType},--cloud.command.line=false,--logging.level.root=info,--logging.file=/root/${clientType}Client/client${counter},--logging.file.max-size=50MB,--test.type=RequestTest,--test.should.delete=true,--test.should.add=false" &> /dev/null &
fi

((counter++))
done
