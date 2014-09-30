package ensen.controler;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.TreeMap;

import org.apache.log4j.Logger;

import Jama.Matrix;

import com.hp.hpl.jena.rdf.model.Model;

import ensen.entities.Concept;
import ensen.entities.Document;
import ensen.entities.Group;
import ensen.util.Printer;
import ensen.util.PropertiesManager;
import ensen.util.SystemCommandExecutor;

public class PythonControler {
	static Logger log = Logger.getLogger(PythonControler.class.getName());
	private static long timeoutInSeconds = 60;

	public static String runCP(Model graph) {
		//write RDF to temp file then pass it to Python		
		System.out.println("Prepare Python input file");
		String fileName = "RDFfile_" + System.currentTimeMillis();
		String createdFilePath = RDFManager.createRDFfile(fileName + ".rdf", graph, "RDF/XML");
		Printer.registerTime("Prepare Python input file ");
		System.out.println("Run Python Script");
		String output = runPyScript(PropertiesManager.getProperty("CPScriptPath"), PropertiesManager.getProperty("rootPath") + "RDF/" + fileName + ".rdf");
		System.out.println("Python Script Finished");
		Printer.registerTime("Run Python Script ");
		//RDFManager.deleteFile(createdFilePath);

		return output;

	}

	public static String runPyScript(String pyFile, String pyArgs) {
		String output = "";
		String pyPath = PropertiesManager.getProperty("pyPath");
		try {
			/*Runtime r = Runtime.getRuntime();
				System.out.println("run the exe");
				Process p = r.exec(pyPath + " " + pyFile + " " + pyArgs);
				BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
				BufferedReader error = new BufferedReader(new InputStreamReader(p.getErrorStream()));
				System.out.println("waitFor");
			p.waitFor();
				String line = "";
				System.out.println("read output");
				while (br.ready()) {
					line = br.readLine();
					output += line + "\n";
				}
				while (error.ready()) {
					//System.out.println(error.toString());
				System.err.println(error.readLine());
				}
			*/
			/*
			Process process = new ProcessBuilder(pyPath, pyFile, pyArgs).start();
			InputStream is = process.getInputStream();
			InputStreamReader isr = new InputStreamReader(is);
			BufferedReader br = new BufferedReader(isr);
			long now = System.currentTimeMillis();
			long timeoutInMillis = 1000L * timeoutInSeconds;
			long finish = now + timeoutInMillis;
			System.err.print("Waiting for Pyhton: ");
			while (isAlive(process) && (System.currentTimeMillis() < finish)) {
				Thread.sleep(100);
				System.err.print(((finish - System.currentTimeMillis()) * 100 / timeoutInMillis) + "|");
			}
			System.out.println();
			if (isAlive(process)) {
			
				throw new InterruptedException("Process timeout out after " + timeoutInSeconds + " seconds");
			}

			String line;

			System.err.printf("Output of runningis:");

			while ((line = br.readLine()) != null) {
				System.out.println(line);
			}
			*/
			List<String> command = new ArrayList<String>();
			command.add(pyPath);
			command.add(pyFile);
			command.add(pyArgs);
			SystemCommandExecutor commandExecutor = new SystemCommandExecutor(command);
			Printer.registerTime("Prepare command");
			int result = commandExecutor.executeCommand();
			Printer.registerTime("Python executeCommand ");
			// get the output from the command
			output = commandExecutor.getStandardOutputFromCommand();
			String stderr = commandExecutor.getStandardErrorFromCommand();
			Printer.registerTime("Python getStandardOutputFromCommand ");
			// print the output from the command
			//System.out.println("STDOUT");
			//System.out.println(output);
			//System.out.println("STDERR");
			//System.out.println(stderr);
			if (!stderr.trim().equals(""))
				System.err.println("Error in Python: /" + stderr.trim() + "/");
		} catch (Exception e) {
			e.printStackTrace();

		}

		return output;
	}

	public static boolean isAlive(Process p) {
		try {
			p.exitValue();
			return false;
		} catch (IllegalThreadStateException e) {
			return true;
		}
	}

	public static void main(String[] args) {
		runCP(RDFManager.readFile("D:\\LIRIS\\workspace\\ensenTensorielWeb\\RDF\\RDFfile_1392306636986.rdf"));
	}

	public static ArrayList<Object> fromPythonOutputToTensor(String output, Document Doci) {
		try {
			Scanner scanner = new Scanner(output);
			String line = scanner.nextLine(); //entities
			if (!line.contains("None")) {
				ArrayList<String> entities = new ArrayList<>();
				while (scanner.hasNextLine()) {
					line = scanner.nextLine();

					if (line.contains("predicates"))//predicates
						break;
					else
						entities.add(line);
				}
				/*System.out.println("entities");
				System.out.println(entities);*/

				ArrayList<String> predicates = new ArrayList<>();
				while (scanner.hasNextLine()) {
					line = scanner.nextLine();
					if (line.contains("U0"))
						break;
					else
						predicates.add(line);
				}
				/*System.out.println("predicates");
				System.out.println(predicates);*/

				line = scanner.nextLine();
				int U0x = Integer.parseInt(line);
				line = scanner.nextLine();
				int U0y = Integer.parseInt(line);

				Matrix U0 = new Matrix(U0x, U0y, 0.0);
				int U0Counter = 0;
				while (scanner.hasNextLine()) {
					line = scanner.nextLine();
					if (line.contains("U1"))
						break;
					else {
						String[] values = line.split(" ");
						for (int i = 0; i < values.length; i++) {
							String v = values[i];
							U0.set(U0Counter, i, Double.parseDouble(v));
						}
						U0Counter++;
					}
				}

				line = scanner.nextLine();
				int U1x = Integer.parseInt(line);
				line = scanner.nextLine();
				int U1y = Integer.parseInt(line);

				Matrix U1 = new Matrix(U1x, U1y, 0.0);
				int U1Counter = 0;
				while (scanner.hasNextLine()) {
					line = scanner.nextLine();
					if (line.contains("U2"))
						break;
					else {
						String[] values = line.split(" ");
						for (int i = 0; i < values.length; i++) {
							String v = values[i];
							U1.set(U1Counter, i, Double.parseDouble(v));
						}
						U1Counter++;
					}
				}

				line = scanner.nextLine();
				int U2x = Integer.parseInt(line);
				line = scanner.nextLine();
				int U2y = Integer.parseInt(line);

				Matrix U2 = new Matrix(U2x, U2y, 0.0);
				int U2Counter = 0;
				while (scanner.hasNextLine()) {
					line = scanner.nextLine();
					if (line.contains("U3"))
						break;
					else {
						String[] values = line.split(" ");
						for (int i = 0; i < values.length; i++) {
							String v = values[i];
							U2.set(U2Counter, i, Double.parseDouble(v));
						}
						U2Counter++;
					}
				}

				line = scanner.nextLine();
				int U3y = Integer.parseInt(line);

				Matrix U3 = new Matrix(1, U3y, 0.0);
				line = scanner.nextLine();
				String[] values = line.split(" ");
				for (int i = 0; i < values.length; i++) {
					String v = values[i];
					U3.set(0, i, Double.parseDouble(v));
				}

				/* read groups*/
				Doci.conceptsMap = new TreeMap<String, Concept>();
				ArrayList<Group> groups = new ArrayList<Group>();
				line = scanner.nextLine(); // first group name
				for (int i = 0; i < 10; i++) {
					Group g = new Group(line, Doci);
					line = scanner.nextLine(); // predicates title					
					g.predicates = new TreeMap<String, Double>();
					line = scanner.nextLine(); // first predicate or "auths"
					while (line.contains("http://")) {
						g.predicates.put(line.split(" : ")[0], Double.parseDouble(line.split(" : ")[1]));
						line = scanner.nextLine(); // one predicate	
					}

					if (scanner.hasNext()) {
						line = scanner.nextLine(); // first authority or "hubs"
						g.auths = new TreeMap<String, Concept>();
						while (line.contains("http://")) {
							Concept c = Doci.conceptsMap.get(line.split(" : ")[0]);
							if (c != null) {
								c.score += Double.parseDouble(line.split(" : ")[1]);
								c.groups.add(g);

							} else {
								c = new Concept(line.split(" : ")[0], Doci);
								c.score = Double.parseDouble(line.split(" : ")[1]);
								c.groups = new ArrayList<>();
								c.groups.add(g);
							}

							g.auths.put(c.URI, c);
							Doci.conceptsMap.put(c.URI, c);
							line = scanner.nextLine();
						}
					}

					if (scanner.hasNext()) {
						line = scanner.nextLine();//first hub or "new group"
						g.hubs = new TreeMap<String, Concept>();
						while (line.contains("http://")) {
							Concept c = Doci.conceptsMap.get(line.split(" : ")[0]);
							if (c != null) {
								c.score += Double.parseDouble(line.split(" : ")[1]);
								c.groups.add(g);

							} else {
								c = new Concept(line.split(" : ")[0], Doci);
								c.score = Double.parseDouble(line.split(" : ")[1]);
								c.groups = new ArrayList<>();
								c.groups.add(g);
							}

							g.hubs.put(c.URI, c);
							Doci.conceptsMap.put(c.URI, c);
							if (scanner.hasNextLine())
								line = scanner.nextLine(); // one Hub	
							else
								break;
						}
					}
					if (g.predicates.size() > 0)
						groups.add(g);
				}

				ArrayList<Object> decompositionResults = new ArrayList<>();
				decompositionResults.add(entities);
				decompositionResults.add(predicates);
				decompositionResults.add(U0);
				decompositionResults.add(U1);
				decompositionResults.add(U2);
				decompositionResults.add(U3);
				decompositionResults.add(groups);
				return decompositionResults;
			} else
				return null;
		} catch (Exception e) {
			System.err.println("error reading python results: " + e.getMessage());
			e.printStackTrace();
			return null;
		}
	}
}
