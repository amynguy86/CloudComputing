package cs6343;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class TimedMDS implements IMetaData {

	private static final Logger logger = LoggerFactory.getLogger(TimedMDS.class);
	private IMetaData wrapped;
	private String mkdirMsg;
	private String lsMsg;
	private String touchMsg;
	private String rmMsg;
	private String rmdirMsg;
	private String partitionMsg;
	private StatCollector collector;

	public TimedMDS(IMetaData wrapped, String type, StatCollector collector) {
		this.wrapped = wrapped;
		this.mkdirMsg = "MKDIR for " + type + " time (ms): ";
		this.lsMsg = "LS for " + type + " time (ms): ";
		this.touchMsg = "Touch for " + type + " time (ms): ";
		this.rmMsg = "RM for " + type + " time (ms): ";
		this.rmdirMsg = "RMDIR for " + type + " time (ms): ";
		this.partitionMsg="PARTITION for " + type + " time (ms): ";
		this.collector = collector;

	}

	@Override
	public boolean mkdir(String dirName) {
		long nanoTimeStart = System.nanoTime();
		boolean result = wrapped.mkdir(dirName);
		long nanoTimeEnd = System.nanoTime();
		double time = ((nanoTimeEnd - nanoTimeStart) / 1000000);
		logger.info(mkdirMsg + time);
		collector.addStat(Operation.MKDIR, time);
		return result;
	}

	@Override
	public List<String> ls(String dirName) {
		long nanoTimeStart = System.nanoTime();
		List<String> result = wrapped.ls(dirName);
		long nanoTimeEnd = System.nanoTime();
		double time = ((nanoTimeEnd - nanoTimeStart) / 1000000);
		logger.info(lsMsg + time);
		collector.addStat(Operation.LS, time);
		return result;
	}

	@Override
	public boolean touch(String filePath) {
		long nanoTimeStart = System.nanoTime();
		boolean result = wrapped.touch(filePath);
		long nanoTimeEnd = System.nanoTime();
		double time = ((nanoTimeEnd - nanoTimeStart) / 1000000);
		logger.info(touchMsg + time);
		collector.addStat(Operation.TOUCH, time);
		return result;
	}

	@Override
	public boolean rm(String filePath) {
		long nanoTimeStart = System.nanoTime();
		boolean result = wrapped.rm(filePath);
		long nanoTimeEnd = System.nanoTime();
		double time = ((nanoTimeEnd - nanoTimeStart) / 1000000);
		logger.info(rmMsg + time);
		collector.addStat(Operation.RM, time);
		return result;
	}

	@Override
	public boolean rmdir(String dirname) {
		long nanoTimeStart = System.nanoTime();
		boolean result = wrapped.rmdir(dirname);
		long nanoTimeEnd = System.nanoTime();
		double time = ((nanoTimeEnd - nanoTimeStart) / 1000000);
		logger.info(rmdirMsg + time);
		collector.addStat(Operation.RMDIR, time);
		return result;
	}

	@Override
	public boolean partition(String data) {
		// TODO Auto-generated method stub
		long nanoTimeStart = System.nanoTime();
		boolean result = wrapped.partition(data);
		long nanoTimeEnd = System.nanoTime();
		double time = ((nanoTimeEnd - nanoTimeStart) / 1000000);
		logger.info(partitionMsg + time);
		collector.addStat(Operation.PARTITION, time);
		return result;
	}
}
