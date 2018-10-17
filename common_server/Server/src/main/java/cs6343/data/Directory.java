package cs6343.data;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Directory{
    public  String remoteServer;
    public  String user;
    public  String group;
    public  Map<String, Directory> subdirectories;
    public  String name;
    public  Map<String, File> files;

    @Override
    public String toString(){
       return "{ \"remoteServer\":\""+remoteServer+"\"," +
               "\"user\":\"" + user +"\"," +
               "\"group\":\"" + group +"\"," +
               "\"directories\":\"[" + directoryNames() +"\"]," +
               "\"files\":\"[" + fileNames() +"\"]}";
    }

    public String directoryNames(){
        return subdirectories.keySet().stream().map(d -> "\"" + d + "\"").collect(Collectors.joining(","));
    }

    public String fileNames(){
        return files.keySet().stream().map(f -> "\"" + f + "\"").collect(Collectors.joining(","));
    }
}