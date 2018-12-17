cd /root/CloudComputing/client_simulator
rlwrap mvn spring-boot:run -Dspring-boot.run.arguments="--server=192.168.29.229,--cloud.client.type=cs6343.CassandraMDS,--cloud.command.line=true,--logging.level.root=info"
