package ensen.control;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;

import ensen.entities.BipartiteGraph;

public class Bipartite {
	public Model rdfGraph;
	public ArrayList<String> v1;
	public ArrayList<String> v2;
	public int[][] M;
	public int Mt[][];
	public int Ma[][];
	public double[][] Pa;
	private int triplesCounter;
	private int entitesSize;
	ArrayList<BipartiteGraph> subGraphs;
	private BipartiteGraph selectedGraph;

	public Bipartite(Model in) {
		System.out.println("Transforming Model to Bipartite Graph, original Size=" + in.size());
		subGraphs = new ArrayList<BipartiteGraph>();
		ArrayList<Model> res = RDFManager.splitModel(in);
		System.out.println("Spliting Model to subGraphs, count(subGraphs)=" + res.size());

		System.out.println("Transforming subGraphs to Bipartite Graphs");
		for (Model m : res) {
			BipartiteGraph BG = new BipartiteGraph();
			BG.rdfGraph = m;
			//BG = calculateMatricesForSubGraph(BG);
			subGraphs.add(BG);
		}

	}

	private BipartiteGraph calculateMatricesForSubGraph(BipartiteGraph BG) {
		BG.indexed = true;
		BG.v1 = new ArrayList<String>();// entities vector
		BG.v2 = new ArrayList<String>();// triplets vector

		//get all URIs
		String Q = "select distinct ?s where {{?s ?p ?o.} union {?s1 ?s ?o1.} union {?s2 ?p2 ?s. filter(!isliteral(?s))}}";
		ResultSet res = SparqlManager.sparqlQueryOverJenaModel(Q, BG.rdfGraph);
		BG.entitesSize = 0;
		while (res.hasNext()) {
			QuerySolution QS = res.next();
			BG.entitesSize++;
			BG.v1.add(QS.get("s").asResource().getURI());

		}

		StmtIterator sts = BG.rdfGraph.listStatements();
		BG.triplesCounter = BG.rdfGraph.listStatements().toList().size();

		//Fill Ma (adjacent matrix)
		BG.Ma = new int[BG.triplesCounter + BG.entitesSize][BG.triplesCounter + BG.entitesSize];

		//Fill M Matrix
		BG.M = new int[BG.entitesSize][BG.triplesCounter];
		int Counter = 0;
		while (sts.hasNext()) {
			BG.v2.add("T" + Counter);
			Statement st = sts.next();
			String s = st.getSubject().getURI();
			String p = st.getPredicate().getURI();
			String o = st.getObject().asResource().getURI();

			int Si = BG.v1.indexOf(s);
			if (Si == -1)
				BG.v1.add(s);
			Si = BG.v1.indexOf(s);

			int Pi = BG.v1.indexOf(p);
			if (Pi == -1)
				BG.v1.add(s);
			Pi = BG.v1.indexOf(p);

			int Oi = BG.v1.indexOf(o);
			if (Oi == -1)
				BG.v1.add(s);
			Oi = BG.v1.indexOf(o);

			BG.M[Si][Counter] = BG.M[Pi][Counter] = BG.M[Oi][Counter] = 1;
			BG.Ma[Si][BG.entitesSize + Counter] = BG.Ma[Pi][BG.entitesSize + Counter] = BG.Ma[Oi][BG.entitesSize + Counter] = 1;

			Counter++;
		}

		//Fill Mt (the transpose of M)
		BG.Mt = new int[BG.triplesCounter][BG.entitesSize];
		for (int i = 0; i < BG.M.length; i++) {
			for (int j = 0; j < BG.M[i].length; j++) {
				BG.Mt[j][i] = BG.M[i][j];
				BG.Ma[BG.entitesSize + j][i] = BG.Mt[j][i];
			}
		}

		//Fill Pa (the transpose of M) with row normalizing
		BG.Pa = new double[BG.triplesCounter + BG.entitesSize][BG.triplesCounter + BG.entitesSize];
		for (int i = 0; i < BG.Ma.length; i++) {
			int rowSum = 0;
			for (int j = 0; j < BG.Ma[i].length; j++) {
				rowSum += BG.Ma[i][j];
			}

			for (int j = 0; j < BG.Ma[i].length; j++) {
				if (rowSum == 0)
					BG.Pa[i][j] = 0;
				else
					BG.Pa[i][j] = (BG.Ma[i][j] * 1.0) / rowSum;
			}
		}

		//Fill Pa (the transpose of M) with col normalizing
		/*	int size = triplesCounter + entitesSize;
			Pa = new double[size][size];
			for (int i = 0; i < size; i++) {
				int colSum = 0;
				for (int j = 0; j < size; j++) {
					colSum += Ma[j][i];
				}

				for (int j = 0; j < size; j++) {
					if (colSum == 0)
						Pa[j][i] = 0;
					else
						Pa[j][i] = (Ma[j][i] * 1.0) / colSum;
				}
			}*/
		return BG;
	}

	private void calculateMatrices(Model m) {
		rdfGraph = m;
		v1 = new ArrayList<String>();// entities vector
		v2 = new ArrayList<String>();// triplets vector

		//get all URIs
		String Q = "select distinct ?s where {{?s ?p ?o.} union {?s1 ?s ?o1.} union {?s2 ?p2 ?s. filter(!isliteral(?s))}}";
		ResultSet res = SparqlManager.sparqlQueryOverJenaModel(Q, rdfGraph);
		entitesSize = 0;
		while (res.hasNext()) {
			QuerySolution QS = res.next();
			entitesSize++;
			v1.add(QS.get("s").asResource().getURI());

		}

		StmtIterator sts = rdfGraph.listStatements();
		triplesCounter = rdfGraph.listStatements().toList().size();

		//Fill Ma (adjacent matrix)
		Ma = new int[triplesCounter + entitesSize][triplesCounter + entitesSize];

		//Fill M Matrix
		M = new int[entitesSize][triplesCounter];
		int Counter = 0;
		while (sts.hasNext()) {
			v2.add("T" + Counter);
			Statement st = sts.next();
			String s = st.getSubject().getURI();
			String p = st.getPredicate().getURI();
			String o = st.getObject().asResource().getURI();

			int Si = v1.indexOf(s);
			if (Si == -1)
				v1.add(s);
			Si = v1.indexOf(s);

			int Pi = v1.indexOf(p);
			if (Pi == -1)
				v1.add(s);
			Pi = v1.indexOf(p);

			int Oi = v1.indexOf(o);
			if (Oi == -1)
				v1.add(s);
			Oi = v1.indexOf(o);

			M[Si][Counter] = M[Pi][Counter] = M[Oi][Counter] = 1;
			Ma[Si][entitesSize + Counter] = Ma[Pi][entitesSize + Counter] = Ma[Oi][entitesSize + Counter] = 1;

			Counter++;
		}

		//Fill Mt (the transpose of M)
		Mt = new int[triplesCounter][entitesSize];
		for (int i = 0; i < M.length; i++) {
			for (int j = 0; j < M[i].length; j++) {
				Mt[j][i] = M[i][j];
				Ma[entitesSize + j][i] = Mt[j][i];
			}
		}

		//Fill Pa (the transpose of M) with row normalizing
		Pa = new double[triplesCounter + entitesSize][triplesCounter + entitesSize];
		for (int i = 0; i < Ma.length; i++) {
			int rowSum = 0;
			for (int j = 0; j < Ma[i].length; j++) {
				rowSum += Ma[i][j];
			}

			for (int j = 0; j < Ma[i].length; j++) {
				if (rowSum == 0)
					Pa[i][j] = 0;
				else
					Pa[i][j] = (Ma[i][j] * 1.0) / rowSum;
			}
		}

		//Fill Pa (the transpose of M) with col normalizing
		/*	int size = triplesCounter + entitesSize;
			Pa = new double[size][size];
			for (int i = 0; i < size; i++) {
				int colSum = 0;
				for (int j = 0; j < size; j++) {
					colSum += Ma[j][i];
				}

				for (int j = 0; j < size; j++) {
					if (colSum == 0)
						Pa[j][i] = 0;
					else
						Pa[j][i] = (Ma[j][i] * 1.0) / colSum;
				}
			}*/

	}

	public TreeMap<String, Double> applyRS(String URI, Model model) {
		Map<String, Double> res = new HashMap<String, Double>();
		ValueComparator bvc = new ValueComparator(res);
		TreeMap<String, Double> sorted_res = new TreeMap<String, Double>(bvc);

		double[][] values = RS(URI, model);
		for (int i = 0; i < v1.size(); i++) {
			if (values[i][0] > 0.002)
				res.put(v1.get(i), values[i][0]);
		}
		sorted_res.putAll(res);
		return sorted_res;
	}

	public TreeMap<String, Double> applyRSWithSubjects(String URI, Model model) {
		Map<String, Double> res = new HashMap<String, Double>();
		ValueComparator bvc = new ValueComparator(res);
		TreeMap<String, Double> sorted_res = new TreeMap<String, Double>(bvc);
		ResIterator subjectsIt = model.listSubjects();
		ArrayList<String> subjects = new ArrayList<String>();
		while (subjectsIt.hasNext()) {
			subjects.add(subjectsIt.next().getURI());
		}
		double[][] values = RS(URI, model);
		for (int i = 0; i < v1.size(); i++) {
			if (subjects.contains(v1.get(i)))
				if (values[i][0] > 0.002)
					res.put(v1.get(i), values[i][0]);
		}
		sorted_res.putAll(res);
		return sorted_res;
	}

	public double[][] RS(String URI, Model m) {
		calculateMatrices(m);
		int index = v1.indexOf(URI);
		double c = 0.15;

		int[][] qa = new int[triplesCounter + entitesSize][1];
		qa[index][0] = 1;

		double[][] ua = new double[triplesCounter + entitesSize][1];
		//mprint(Pa);
		double delta = 5.0;//big number
		while (delta > 0.05) {
			double[][] temp = multiply(Pa, ua);
			delta = 0.0;
			for (int j = 0; j < ua.length; j++) {
				double tempDelta = ua[j][0];
				ua[j][0] = ((1 - c) * temp[j][0]) + (c * qa[j][0]);
				delta += (ua[j][0] - tempDelta);
			}
			//System.out.println("delta= " + delta);
		}

		return ua;
	}

	public TreeMap<String, Double> applyRS_Approx(String URI, Model model) {
		Map<String, Double> res = new HashMap<String, Double>();
		ValueComparator bvc = new ValueComparator(res);
		TreeMap<String, Double> sorted_res = new TreeMap<String, Double>(bvc);
		//System.out.println("RS_Approx: " + URI + " in graph with size= " + model.size());
		double[][] values = RS_Approx(URI, model);
		for (int i = 0; i < selectedGraph.v1.size(); i++) {
			if (values[i][0] > 0.0)
				res.put(selectedGraph.v1.get(i), values[i][0]);
		}
		sorted_res.putAll(res);
		return sorted_res;
	}

	public TreeMap<String, Double> applyRS_ApproxWithSubjects(String URI, Model model) {
		Map<String, Double> res = new HashMap<String, Double>();
		ValueComparator bvc = new ValueComparator(res);
		TreeMap<String, Double> sorted_res = new TreeMap<String, Double>(bvc);
		ResIterator subjectsIt = model.listSubjects();
		ArrayList<String> subjects = new ArrayList<String>();
		while (subjectsIt.hasNext()) {
			subjects.add(subjectsIt.next().getURI());
		}
		double[][] values = RS_Approx(URI, model);
		for (int i = 0; i < v1.size(); i++) {
			if (subjects.contains(v1.get(i)))
				//if (values[i][0] > 0.002)
				res.put(v1.get(i), values[i][0]);
		}
		sorted_res.putAll(res);
		return sorted_res;
	}

	public double[][] RS_Approx(String URI, Model m) {

		Model tempM = RDFManager.createRDFModel();
		selectedGraph = new BipartiteGraph();
		System.out.println("Search graph contains " + URI);
		for (int i = 0; i < subGraphs.size(); i++) {
			if (subGraphs.get(i).rdfGraph.containsResource(tempM.createResource(URI))) {
				selectedGraph = subGraphs.get(i);
				System.out.println("Found in graph with size = " + selectedGraph.rdfGraph.size());
				if (!selectedGraph.indexed) {
					System.out.println(" Calculating Matrices Now...");
					calculateMatricesForSubGraph(selectedGraph);
				}
				break;
			}
		}

		//System.out.println(selectedGraph.v1);
		int index = selectedGraph.v1.indexOf(URI);
		int allSize = selectedGraph.triplesCounter + selectedGraph.entitesSize;
		double[][] ua = new double[allSize][1];
		int[][] qa = new int[allSize][1];
		if (index != -1) {
			double c = 0.15;
			qa[index][0] = 1;
			//mprint(Pa);
			double delta = 5.0;//big number
			while (delta > 0.05) {
				double[][] temp = multiply(selectedGraph.Pa, ua);
				delta = 0.0;
				for (int j = 0; j < ua.length; j++) {
					double tempDelta = ua[j][0];
					ua[j][0] = ((1 - c) * temp[j][0]) + (c * qa[j][0]);
					delta += (ua[j][0] - tempDelta);
				}
				//System.out.println("delta= " + delta);
			}
		}
		return ua;
	}

	public static double[][] multiply(double[][] m1, double[][] m2) {
		int m1rows = m1.length;
		int m1cols = m1[0].length;
		int m2rows = m2.length;
		int m2cols = m2[0].length;
		if (m1cols != m2rows)
			throw new IllegalArgumentException("matrices don't match: " + m1cols + " != " + m2rows);
		double[][] result = new double[m1rows][m2cols];

		// multiply
		for (int i = 0; i < m1rows; i++)
			for (int j = 0; j < m2cols; j++)
				for (int k = 0; k < m1cols; k++)
					result[i][j] += m1[i][k] * m2[k][j];

		return result;
	}

	public static int[][] multiply(int[][] m1, int[][] m2) {
		int m1rows = m1.length;
		int m1cols = m1[0].length;
		int m2rows = m2.length;
		int m2cols = m2[0].length;
		if (m1cols != m2rows)
			throw new IllegalArgumentException("matrices don't match: " + m1cols + " != " + m2rows);
		int[][] result = new int[m1rows][m2cols];

		// multiply
		for (int i = 0; i < m1rows; i++)
			for (int j = 0; j < m2cols; j++)
				for (int k = 0; k < m1cols; k++)
					result[i][j] += m1[i][k] * m2[k][j];

		return result;
	}

	/** Matrix print.
	   */
	public static void mprint(double[][] a) {
		int rows = a.length;
		int cols = a[0].length;
		System.out.println("array[" + rows + "][" + cols + "] = {");
		for (int i = 0; i < rows; i++) {
			System.out.print("{");
			for (int j = 0; j < cols; j++)
				System.out.print(" " + a[i][j] + ",");
			System.out.println("},");
		}
		System.out.println(":;");
	}

	public static void mprint(int[][] a) {
		int rows = a.length;
		int cols = a[0].length;
		System.out.println("array[" + rows + "][" + cols + "] = {");
		for (int i = 0; i < rows; i++) {
			System.out.print("{");
			for (int j = 0; j < cols; j++)
				System.out.print(" " + a[i][j] + ",");
			System.out.println("},");
		}
		System.out.println(":;");
	}

	/**
	 * @param args
	 */

	public static void main(String[] args) {
		long startTime = System.currentTimeMillis();
		Model model = RDFManager.readFile(PropertiesManager.getProperty("rootPath")+"\\RDF\\Temp\\QEGraph.rdf");
		model = RDFManager.removeHasApredicates(model);
		model = RDFManager.getObtimalGraph(model, "en");
		Bipartite Bi = new Bipartite(model);

		TreeMap<String, Double> sorted_res = Bi.applyRS_Approx("http://dbpedia.org/resource/Information_retrieval", model);

		System.out.println("results: " + sorted_res);

		long endTime = System.currentTimeMillis();
		long totalTime = endTime - startTime;
		System.out.println("Test time= " + (totalTime) + " mSec");

	}

}
