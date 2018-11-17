package cs6343;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class CentralizedMDS implements IMetaData {
    private String path;
    private RestTemplate rest;
    private HttpHeaders headers;

    public CentralizedMDS(String path){
        this.path = path;
        this.rest = new RestTemplate();
        this.headers = new HttpHeaders();
        this.headers.add("content-type", "application/json");
    }

    public List<String> ls(String dirname){
       HttpEntity<String> requestEntity = new HttpEntity<String>("ls " + dirname, headers);
       ResponseEntity<String> responseEntity = rest.exchange(path, HttpMethod.POST, requestEntity, String.class);
       JsonParser parser = new JsonParser();
       String body = responseEntity.getBody();
       JsonObject obj = parser.parse(body).getAsJsonObject();
       if(!obj.getAsJsonPrimitive("operationSuccess").getAsBoolean()){
           return null;
       }
       String result = obj.getAsJsonPrimitive("operationReturnVal").getAsString();
       String[] inodes = result.split("\n");
       return Arrays.stream(inodes).map(x -> x.substring(x.indexOf('=')+1, x.indexOf(']'))).collect(Collectors.toList());
    }

    @Override
    public boolean touch(String filePath) {
        return false;
    }

    @Override
    public boolean rm(String filePath) {
        return false;
    }

    @Override
    public boolean rmdir(String dirname) {
        return false;
    }

    public boolean mkdir(String dirname){
        HttpEntity<String> requestEntity = new HttpEntity<String>("mkdir " + dirname, headers);
        ResponseEntity<String> responseEntity = rest.exchange(path, HttpMethod.POST, requestEntity, String.class);
        JsonParser parser = new JsonParser();
        String body = responseEntity.getBody();
        System.out.println(body);
        JsonObject obj = parser.parse(responseEntity.getBody()).getAsJsonObject();
        return obj.getAsJsonPrimitive("operationSuccess").getAsBoolean();
    }
}
