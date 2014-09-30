package ensen.controler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import ensen.util.PropertiesManager;
import ensen.util.SystemCommandExecutor;

public class CControler {
	public Double maxScore = 0.0;
	public HashMap<String, Double> resources;
	public String output;
	public String stderr;

	public String callCLib(String fileName) {
		System.out.println("Run C Lib over " + fileName);
		fileName = fileName.replace(PropertiesManager.getProperty("webRootPath"), PropertiesManager.getProperty("cygwinPath"));
		output = "";
		String clibCommand = PropertiesManager.getProperty("clibCommand");
		try {
			List<String> command = new ArrayList<String>();
			command.add(clibCommand);
			command.add(fileName);
			System.out.println(command.toString());
			SystemCommandExecutor commandExecutor = new SystemCommandExecutor(command);
			int result = commandExecutor.executeCommand();

			// get the output from the command
			output = commandExecutor.getStandardOutputFromCommand();
			stderr = commandExecutor.getStandardErrorFromCommand();

			// print the output from the command
			//System.out.println("STDOUT");
			//System.err.println(output);
			//System.err.println("STDERR");
			//System.err.println(stderr);
			if (!stderr.trim().equals(""))
				System.err.println("Error in C lib: /" + stderr.trim() + "/" + "file: " + fileName);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return output;
	}

	public Map<String, Double> readCoutput(String cOutput) {
		try {
		resources = new HashMap<String, Double>();
		Scanner scanner = new Scanner(cOutput);
		boolean ok = false;
		String line = "";
		while (scanner.hasNextLine()) {
			line = scanner.nextLine();
			if (line.startsWith("http")) {
				ok = true;
				break;
			}
		}

		if (ok) {
				String[] keyValue = null;
				Double score = 0.0;
				while (ok) {
					try {
						if (line.trim() != "") {
							keyValue = line.split(" ");
							if (keyValue.length > 1) {
								score = Double.parseDouble(keyValue[1]);
								resources.put(keyValue[0], score);
								if (score > maxScore)
									maxScore = score;
							}
						}
					} catch (Exception e) {
					}
					ok = scanner.hasNextLine();
					if (ok)
						line = scanner.nextLine().replace(",", "");

			}

		}
		scanner.close();
		} catch (Exception e) {
			System.err.println("Error" + e.getMessage());
			System.err.println("cOutput:" + cOutput);

		}
		return resources;
	}

}
