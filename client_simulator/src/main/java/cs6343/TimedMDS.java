package cs6343;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class TimedMDS implements IMetaData{

    private static final Logger logger = LoggerFactory.getLogger(TimedMDS.class);
    private IMetaData wrapped;
    private String mkdirMsg;
    private String lsMsg;
    private String touchMsg;
    private String rmMsg;
    private String rmdirMsg;

    public TimedMDS(IMetaData wrapped, String type){
        this.wrapped = wrapped;
        this.mkdirMsg = "MKDIR for " + type + " time (ms): ";
        this.lsMsg = "LS for " + type + " time (ms): ";
        this.touchMsg = "Touch for " + type + " time (ms): ";
        this.rmMsg = "RM for " + type + " time (ms): ";
        this.rmdirMsg = "RMDIR for " + type + " time (ms): ";
    }


    @Override
    public boolean mkdir(String dirName) {
        long nanoTimeStart = System.nanoTime();
        boolean result = wrapped.mkdir(dirName);
        long nanoTimeEnd = System.nanoTime();
        logger.info(mkdirMsg + ((nanoTimeEnd - nanoTimeStart) / 1000000));
        return result;
    }

    @Override
    public List<String> ls(String dirName) {
        long nanoTimeStart = System.nanoTime();
        List<String> result = wrapped.ls(dirName);
        long nanoTimeEnd = System.nanoTime();
        logger.info(lsMsg + ((nanoTimeEnd - nanoTimeStart) / 1000000));
        return result;
    }

    @Override
    public boolean touch(String filePath) {
        long nanoTimeStart = System.nanoTime();
        boolean result = wrapped.touch(filePath);
        long nanoTimeEnd = System.nanoTime();
        logger.info(touchMsg + ((nanoTimeEnd - nanoTimeStart) / 1000000));
        return result;
    }

    @Override
    public boolean rm(String filePath) {
        long nanoTimeStart = System.nanoTime();
        boolean result = wrapped.rm(filePath);
        long nanoTimeEnd = System.nanoTime();
        logger.info(rmMsg + ((nanoTimeEnd - nanoTimeStart) / 1000000));
        return result;
    }

    @Override
    public boolean rmdir(String dirname) {
        long nanoTimeStart = System.nanoTime();
        boolean result = wrapped.rmdir(dirname);
        long nanoTimeEnd = System.nanoTime();
        logger.info(rmdirMsg + ((nanoTimeEnd - nanoTimeStart) / 1000000));
        return result;
    }
}
