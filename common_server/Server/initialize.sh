mvn spring-boot:run -Dlockserver.port=8081   
sleep 4
curl --request POST \
  --url http://localhost:8080/command \
  --header 'content-type: application/json' \
  --data 'mkdir /aa'

curl --request POST \
  --url http://localhost:8080/command \
  --header 'content-type: application/json' \
  --data 'mkdir /aa/b'

