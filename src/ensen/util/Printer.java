package ensen.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;

import org.apache.log4j.Logger;

import Jama.Matrix;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Selector;
import com.hp.hpl.jena.rdf.model.SimpleSelector;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;

import ensen.entities.Document;
import ensen.entities.EnsenDBpediaResource;

public class Printer {
	static Logger log = Logger.getLogger(Printer.class.getName());
	static long timeRegister = 0;

	public static void setLogFile(String name) {
		/*String physicalFolder = PropertiesManager.getProperty("webRootPath");
		try {
			System.setOut(new PrintStream(new File(physicalFolder + "/log/" + name + ".txt")));
		} catch (FileNotFoundException e1) {

			e1.printStackTrace();
		} catch (IOException e) {

			e.printStackTrace();
		}*/

	}

	public static void setOutPutLogFileToDefault() {
		String physicalFolder = PropertiesManager.getProperty("webRootPath");
		try {
			System.setOut(new PrintStream(new File(physicalFolder + "/log/default.log")));
		} catch (FileNotFoundException e1) {

			e1.printStackTrace();
		} catch (IOException e) {

			e.printStackTrace();
		}
	}

	public static void printJamaMatrix(Matrix inp) {
		int rows = inp.getRowDimension();
		int columns = inp.getColumnDimension();
		System.out.println("Jama: " + rows + " X " + columns);
		StringBuffer sb = new StringBuffer();
		for (int r = 0; r < rows; r++) {
			for (int c = 0; c < columns; c++) {
				try {
					sb.append(Double.valueOf(inp.get(r, c))).append("\t");
				} catch (Exception e) {

				}
			}
			sb.append("\n");
		}
		System.out.println(sb.toString());
	}

	public static String JamaMatrixToStringUsingArrays(Matrix inp, boolean asInt) {
		String out = "";
		int rows = inp.getRowDimension();
		int columns = inp.getColumnDimension();
		out += rows + " X " + columns + "\n";
		double[][] arr = inp.getArray();
		if (asInt)
			out += Arrays.deepToString(arr).replace("], [", "\n").replace(",", "").replace("[", "").replace("]", "").replace(".0", "") + "\n";
		else
			out += Arrays.deepToString(arr).replace("], [", "\n").replace(",", "").replace("[", "").replace("]", "") + "\n";

		return out;
	}

	public static String JamaMatrixToString(Matrix inp, boolean asInt) {
		String out = "";
		int rows = inp.getRowDimension();
		int columns = inp.getColumnDimension();
		out += rows + " X " + columns + "\n";
		StringBuffer sb = new StringBuffer();
		for (int r = 0; r < rows; r++) {
			for (int c = 0; c < columns; c++) {
				try {
					String value = "";
					if (asInt)
						value = (Double.valueOf(inp.get(r, c)).longValue()) + "";
					else
						value = Double.valueOf(inp.get(r, c)) + "";

					sb.append(value).append(" ");
				} catch (Exception e) {

				}
			}
			sb.append("\n");
		}
		out += sb.toString();
		return out;
	}

	public static void printGrid(double[][] a) {
		for (int i = 0; i < a.length; i++) {
			for (int j = 0; j < a[i].length; j++) {
				System.out.printf("%2.2f ", a[i][j]);
			}
			System.out.println("");
		}

	}

	public static void printTriples(Model m) {
		StmtIterator sts = m.listStatements();
		System.out.println("This Model contains: " + sts.toList().size() + " Statement");
		sts = m.listStatements();
		while (sts.hasNext()) {
			Statement st = sts.next();
			System.out.println(st);
		}
	}

	public static void printVirtualAndNotVirtualRelations(Model m) {
		Property p = m.createProperty("http://ensen.org/data#virtualProperty");
		Selector selector = new SimpleSelector(null, p, (RDFNode) null);
		StmtIterator lst = m.listStatements(selector);
		for (Iterator iterator = lst; iterator.hasNext();) {
			Statement st = (Statement) iterator.next();
			Selector selector2 = new SimpleSelector(st.getSubject(), null, st.getObject());
			StmtIterator lst2 = m.listStatements(selector2);
			for (Iterator iterator2 = lst2; iterator2.hasNext();) {
				Statement st2 = (Statement) iterator2.next();
				System.out.println(st2);
			}
			if (st.getObject().isResource()) {
				Selector selector3 = new SimpleSelector(st.getObject().asResource(), null, st.getSubject().asNode());
				StmtIterator lst3 = m.listStatements(selector3);
				for (Iterator iterator3 = lst3; iterator3.hasNext();) {
					Statement st3 = (Statement) iterator3.next();
					System.out.println(st3);
				}
			}
		}
	}

	/*
	 * path: is the path to directory father
	 * name: directory name
	 */
	public static void createFolder(String Path, String name) {

		String physicalFolder = PropertiesManager.getProperty("webRootPath");
		File theDir = new File(physicalFolder + "/" + Path + "/" + name);

		// if the directory does not exist, create it
		if (!theDir.exists()) {
			System.out.println("creating directory: " + name);
			boolean result = theDir.mkdir();

			if (result) {
				System.out.println(name + "created");
			}
		}

	}

	public static void printToFile(String filePath, String txt) {

		PrintWriter writer;
		try {
			String physicalFolder = PropertiesManager.getProperty("webRootPath");
			writer = new PrintWriter(physicalFolder + "/" + filePath, "UTF-8");
			writer.println(txt);
			writer.close();
		} catch (FileNotFoundException e) {
			System.err.println(e.getMessage());
		} catch (UnsupportedEncodingException e) {
			System.err.println(e.getMessage());
		}
	}

	public static String logToFile(String fileName, String txt) {
		boolean logging = Boolean.parseBoolean(PropertiesManager.getProperty("logging"));
		if (logging) {
			PrintWriter writer;
			try {
				String physicalFolder = PropertiesManager.getProperty("webRootPath")/*.replace("\\", "/")*/;
				fileName = physicalFolder + "/log/logFiles/" + fileName + ".log";
				writer = new PrintWriter(new FileOutputStream(new File(fileName), true /* append = true */));
				writer.println(txt);
				writer.close();
			} catch (FileNotFoundException e) {
				System.err.println(e.getMessage());
			}
		}

		return fileName;
	}

	public static void registerTime(String msg) {
		if (timeRegister == 0) {
			timeRegister = System.currentTimeMillis();
			SimpleDateFormat sdf = new SimpleDateFormat("MMM dd,yyyy HH:mm");
			Date resultdate = new Date(timeRegister);

			System.out.println("Time register: Just started (" + msg + ") in " + sdf.format(resultdate));
		} else {
			long period = System.currentTimeMillis() - timeRegister;
			System.out.println("Time register: " + msg + " takes " + period / 1000 + "," + (period % 1000) + " s");
			timeRegister = System.currentTimeMillis();
		}
	}

	public static void printGraphAsTensor(Model g, int rank, String coreUrl) {
		String tensorName = "/out/" + rank + "Tensor" + System.currentTimeMillis();
		ArrayList<String> resources = new ArrayList<String>();
		ArrayList<String> predicats = new ArrayList<String>();

		String resourcesList = "";
		String predicatsList = "";
		StmtIterator sts = g.listStatements();
		while (sts.hasNext()) {
			Statement st = sts.next();
			String s = st.getSubject().getURI();
			String p = st.getPredicate().getURI();
			if (!resources.contains(s) && !s.equals(coreUrl)) {
				resources.add(s);
				resourcesList += s + " ";
			}
			if (st.getObject().isResource()) {
				String o = st.getObject().asResource().getURI();
				if (!resources.contains(o)) {
					resources.add(o);
					resourcesList += o + " ";
				}
			}
			if (!predicats.contains(p) && !p.equals("http://ensen.org/data#has-a")) {
				predicats.add(p);
				predicatsList += p + " ";
			}
		}

		Printer.logToFile(tensorName, resourcesList);
		Printer.logToFile(tensorName, predicatsList);

		//DenseDoubleMatrix3D Tensor = new DenseDoubleMatrix3D(predicats.size(), resources.size(), resources.size());

		for (String P : predicats) {
			Matrix slice = new Matrix(resources.size(), resources.size());
			Selector selector = new SimpleSelector(null, g.getProperty(P), (RDFNode) null);
			StmtIterator stms = g.listStatements(selector);
			int pid = predicats.indexOf(P);
			while (stms.hasNext()) {
				Statement st = stms.next();
				String s = st.getSubject().getURI();
				String p = st.getPredicate().getURI();
				if (st.getObject().isResource() && !p.equals("http://ensen.org/data#has-a")) {
					String o = st.getObject().asResource().getURI();
					//Tensor.set(pid, resources.indexOf(s), resources.indexOf(o), 1.0);
					slice.set(resources.indexOf(s), resources.indexOf(o), 1.0);
				}
			}
			Printer.logToFile(tensorName, JamaMatrixToStringUsingArrays(slice, true));
		}

		//System.err.println(Tensor.toString());

	}

	public static String prepareCinput(Document Doci, ArrayList<String> allResource, ArrayList<String> terms, Matrix R) {

		String serielizedMatrix = allResource.toString().replace("[", "").replace("]", "").replace(", ", " ") + "\n" + terms.toString().replace("[", "").replace("]", "").replace(", ", " ") + "\n";
		serielizedMatrix += JamaMatrixToStringUsingArrays(R, true);
		//query's resources
		String usedRes = "";
		if (Doci.q.Resources != null)
			for (EnsenDBpediaResource r : Doci.q.Resources) {
				int index = allResource.indexOf(r.getFullUri());
				if (index > 0 && !usedRes.contains(index + " ")) {
					serielizedMatrix += index + " ";
					usedRes += index + " ";
				}
			}
		serielizedMatrix += "\n";

		//query's terms

		String usedTerms = "";
		for (String t : Doci.q.ExtendedText.split(" ")) {
			//System.err.print("query's trerm: " + t);
			t = Doci.SBStemmer.stem(t);
			//System.err.print("--> " + t);
			int index = terms.indexOf(t);
			//System.err.println("-->(index) " + index);
			if (index > -1 && !usedTerms.contains(t + ", ")) {
				serielizedMatrix += index + " ";
				usedTerms += t + " ";
			}
		}
		serielizedMatrix += "\n";

		/* graph structure*/
		for (int i = 0; i < allResource.size(); i++) {
			serielizedMatrix += Arrays.toString(Doci.graphStructure[i]).replace(",", "").replace("[", "").replace("]", "") + "\n";
		}

		String fileName = "cInput/" + Doci.Rank + "ResourcesTermsMatrix" + System.currentTimeMillis();
		PrintWriter writer;
		try {
			String physicalFolder = PropertiesManager.getProperty("webRootPath");
			fileName = physicalFolder + "/" + fileName + Thread.currentThread().getId() + ".log";
			writer = new PrintWriter(new FileOutputStream(new File(fileName), true /* append = true */));
			writer.println(serielizedMatrix);
			writer.close();
		} catch (FileNotFoundException e) {

			e.printStackTrace();
		}

		return fileName;
	}
}
