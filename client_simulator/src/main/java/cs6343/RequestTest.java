package cs6343;

import cs6343.IMetaData;

import java.io.File;
import java.lang.ref.ReferenceQueue;
import java.util.*;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RequestTest {

    public IMetaData mds;
    public static Logger logger = LoggerFactory.getLogger(RequestTest.class);

    public RequestTest(IMetaData mds){
        this.mds = mds;
    }

    public void makeRequest(int depth, double[] action_probs){
        String path = goToDepth(depth);
        if(path==null) return;
        Action action = getAction(action_probs);
        switch(action){
            case RM:
                String filePath = findChildFile(path);
                if(filePath != null)
                    mds.rm(filePath);
                break;
            case TOUCH:
                mds.touch(path + UUID.randomUUID().toString().replaceAll("-", "").substring(12));
                break;
            case MKDIR:
                mds.mkdir(path + UUID.randomUUID().toString().replaceAll("-", "").substring(12));
                break;
            case RMDIR:
                mds.rmdir(path);
                break;
            default:
            	break;
        }
    }

    private Action getAction(double[] action_probs) {
        Random r = new Random();
        double val = r.nextDouble();
        Action[] actions = Action.values();
        int index = 0;
        for(int i = 0; i < action_probs.length; i++){
            if(val < action_probs[i]){
                return actions[i];
            } else {
                val -= action_probs[i];
            }
        }
        return actions[actions.length];
    }

    private String findChildFile(String start){
    	List<FileNode> result = mds.ls(start);
    	if(result==null) 
    		return null;
    	
        List<FileNode> files = result.stream().filter(x->!x.isDirectory).collect(Collectors.toList());
        if(files.isEmpty()){
            List<String> stuff = getShuffledDirectories(start);
            if(stuff.isEmpty()) return null;
            return findChildFile(stuff.get(0));
        }
        Collections.shuffle(files);
        return !files.get(0).getFileName().startsWith("/")?start + "/" + files.get(0).getFileName():start + files.get(0).getFileName();
    }

    public String goToDepth(int depth){
    	logger.info("goToDepth:"+depth);
        Stack<String> options = new Stack<>();
        for(String option : getShuffledDirectories("/")) options.push(option);
        while(!options.isEmpty()){
            String option = options.pop();
            logger.info("Option:"+option);
            if(option.split("/").length >= depth) return option;
            for(String child : getShuffledDirectories(option)) options.push(child);
        }
        logger.info("Could not find anything at depth: {}",depth);
        return null;
        //throw new IllegalArgumentException("Fuck I can't find anything");
    }

    public List<String> getShuffledDirectories(String path){
        List<FileNode> files = mds.ls(path);
        if(files==null)
        	return Collections.emptyList();
        
        Collections.shuffle(files);
        final String prefix = "/".equals(path) ? "" : path;
        
        
        return files.stream().filter(x->x.isDirectory).map(x->!x.getFileName().startsWith("/")? prefix + "/" + x.getFileName():prefix + x.getFileName()).collect(Collectors.toList());
    }

}

enum Action{
    MKDIR, TOUCH, RM, RMDIR;
}

class State{
    String prefix;
    List<FileNode> options;

    public State(String prefix, List<FileNode> options){this.prefix = prefix; this.options = options;}
}
