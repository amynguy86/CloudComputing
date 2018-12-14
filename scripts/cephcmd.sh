cd /root/CloudComputing/client_simulator
rlwrap mvn spring-boot:run -Dspring-boot.run.arguments="--server=192.168.29.100:8080,--cloud.client.type=cs6343.CephMDS,--cloud.command.line=true"
