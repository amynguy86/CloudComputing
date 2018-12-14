package cs6343;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Scanner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class CommandLine {

	public static Logger logger = LoggerFactory.getLogger(CommandLine.class);
	@Autowired
	IMetaData client;
	@Autowired
	StatCollector collector;
	RandomTest randomTest;
	@Value("${delay}")
	boolean delay;

	public void begin() {
		int depth=20;
		int times=1000;
		double prob=0.25;
		if(delay)
			client.delayMe();
		while (true) {
			try {
				System.out.print("ENTER COMMAND HERE:--------->");
				Scanner scanner = new Scanner(System.in);
				String command = scanner.nextLine();
				String[] args = command.trim().split(" ", 2);
				String cmd = args[0].toLowerCase();
				String toPrint;
				switch (cmd) {
				case "ls":
					List list = client.ls(args[1]);
					if (list != null)
						logger.info(list.toString());
					break;
				case "mkdir":
					if (client.mkdir(args[1]))
						logger.info("Success");
					else
						logger.info("Fail");
					break;
				case "rmdir":
					if (client.rmdir(args[1]))
						logger.info("Success");
					else
						logger.info("Fail");
					break;

				case "touch":
					if (client.touch(args[1]))
						logger.info("Success");
					else
						logger.info("Fail");
					break;

				case "rm":
					if (client.rm(args[1]))
						logger.info("Success");
					else
						logger.info("Fail");
					break;
				case "partition":
					if (client.partition(args[1]))
						logger.info("Success");
					else
						logger.info("Fail");
					break;
				case "walk":
					TreeParser p = new TreeParser();
					FileNode out = p.readFile(args[1]);
					randomTest = new RandomTest(out, client);
					randomTest.walk(depth, times);
					break;
				case "dwalk":
					if (randomTest == null)
						logger.error("You must walk first");
					else
						randomTest.destructiveWalk(depth, times, prob);
					break;
				case "print":
					if (args[1].indexOf("-file=") != -1) {
						String dir = args[1].substring(0, args[1].indexOf("-file=")).trim();
						String file = args[1].substring(args[1].indexOf("-file=") + 6).trim();
						Path path = Paths.get(file);
						logger.info("Output file: " + path.toUri());
						String tree = client.printTree(dir);
						Files.write(path, tree.getBytes());
					} else {
						String tree = client.printTree(args[1].trim());
						logger.info("\n" + tree);
					}
					break;

				case "deletechildren":
					logger.info("FILE" + args[1]);
					if (client.deleteChildren(args[1]))
						logger.info("ok");
					else
						logger.info("fail");
					break;
				case "stats":
					System.out.println("LS: " + collector.getSummaryStatistics(Operation.LS));
					System.out.println("MKDIR: " + collector.getSummaryStatistics(Operation.MKDIR));
					System.out.println("TOUCH: " + collector.getSummaryStatistics(Operation.TOUCH));
					System.out.println("RM: " + collector.getSummaryStatistics(Operation.RM));
					System.out.println("RMDIR: " + collector.getSummaryStatistics(Operation.RMDIR));
					break;
				case "setup":
					String d[]=args[1].split(" ");
					depth=Integer.parseInt(d[0]);
					times=Integer.parseInt(d[1]);
					if(d.length==3)
						prob=Double.parseDouble(d[2]);
					
					logger.info("depth:{}, times:{},prob:{}",depth,times,prob);
					break;
				default:
					logger.info("Incorrect command");
				}
			} catch (Exception ex) {
				ex.printStackTrace();

			}
		}
	}
}
