package cs6343;

import java.util.List;
import java.util.Scanner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class CommandLine {

	public static Logger logger = LoggerFactory.getLogger(CommandLine.class);
	@Autowired
	IMetaData client;

	public void begin() {
		while (true) {
			try {
				System.out.print("ENTER COMMAND HERE:--------->");
				Scanner scanner = new Scanner(System.in);
				String command = scanner.nextLine();

				String[] args = command.split("\\s");
				String cmd = args[0].toLowerCase();
				String toPrint;
				switch (cmd) {
				case "ls":
					List list=client.ls(args[1]);
					if(list!=null)
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
				default:
					logger.info("Incorrent command");
				}
			} catch (Exception ex) {
				ex.printStackTrace();

			}
		}
	}
}
