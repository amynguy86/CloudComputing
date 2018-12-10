package cs6343;

import java.util.*;
import java.util.stream.Collectors;

public class StatCollector {
    private Map<Operation, List<Double>> statMap;

    public StatCollector(){
        this.statMap = new HashMap<Operation, List<Double>>();
        statMap.put(Operation.LS, new ArrayList<>());
        statMap.put(Operation.MKDIR, new ArrayList<>());
        statMap.put(Operation.TOUCH, new ArrayList<>());
        statMap.put(Operation.RM, new ArrayList<>());
        statMap.put(Operation.RMDIR, new ArrayList<>());
        statMap.put(Operation.PARTITION, new ArrayList<>());
    }


    public void addStat(Operation op, Double val){
        statMap.get(op).add(val);
    }


    public DoubleSummaryStatistics getSummaryStatistics(Operation op){
        return statMap.get(op).stream().collect(Collectors.summarizingDouble(Double::doubleValue));
    }



}

enum Operation{
    LS,MKDIR,TOUCH,RM,RMDIR,PARTITION;
}
