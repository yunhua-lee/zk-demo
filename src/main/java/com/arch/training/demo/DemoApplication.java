package com.arch.training.demo;

import com.arch.training.demo.server.Node;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.apache.commons.cli.*;

@SpringBootApplication
public class DemoApplication {

	public static void main(String[] args) throws Exception {
		SpringApplication.run(DemoApplication.class, args);

		final Options options = new Options();
		final Option option = new Option("z", true, "ZooKeeper address");
		options.addOption(option);

		final CommandLineParser parser = new DefaultParser();
		CommandLine cmd = null;
		try {
			cmd = parser.parse(options, args);
		} catch (final ParseException e) {
			throw new Exception("parser command line error",e);
		}

		String zkAddr = null;
		if (cmd.hasOption("z")) {
			zkAddr = cmd.getOptionValue("z");
		}else{
			System.err.println("please input the ZooKeeper address by -z option");
			System.exit(1);
		}

		Node.INSTANCE.start(zkAddr);
	}

}
