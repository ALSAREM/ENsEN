package ensen.control;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.PrintStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import com.hp.hpl.jena.ontology.OntTools;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.util.FileManager;

public class LinkedDataDistanceManager {
	public LinkedDataDistanceManager() {
		// TODO Auto-generated constructor stub
	}

	/*
	 * LDSD: Direct distance
	 */

	/**
	 *  Cd is a function that computes the number of direct and distinct links between resources in a graph G.
	 *  if p or R1 or R2 =="" then calculate all other possibilities
	 */
	public static double Cd(Model G, String p, String R1, String R2) {
		double score = 0.0;
		String queryString = " select (count(distinct *) as ?c) where {?s ?p ?o. filter( true ";

		if (R1 != "") {
			queryString += "&&(?s = <" + R1 + ">)";
		}

		if (p != "") {
			queryString += "&&(?p = <" + p + ">)";
		}
		if (R2 != "") {
			queryString += "&&(?o = <" + R2 + ">)";
		}

		queryString += "). } ";

		//System.out.println(queryString);
		ResultSet res = SparqlManager.sparqlQueryOverJenaModel(queryString, G);

		while (res.hasNext()) {
			QuerySolution QS = res.next();
			try {
				score = QS.get("c").asLiteral().getDouble();
			} catch (Exception e) {
				// TODO: handle exception
			}
			break;
		}

		return score;
	}

	/**
	 *  LDSD_d is a function that computes the number of direct(in or out) and distinct links between resources in a graph G.
	 *  
	 */
	public static double LDSD_d(Model G, String R1, String R2) {
		return 1 / (1 + Cd(G, "", R1, R2) + Cd(G, "", R2, R1));
	}

	/**
	 *  LDSD_dw is like LDSD_d but with weight
	 *  
	 */
	public static double LDSD_dw(Model G, String R1, String R2) {
		double a = 0.0;
		double b = 0.0;
		String QueryString = "select distinct ?p where {?s ?p ?o. filter(?s=<" + R1 + "> || ?s=<" + R2 + "> || ?o=<" + R1 + "> || ?o=<" + R2 + "> )}";
		ResultSet res = SparqlManager.sparqlQueryOverJenaModel(QueryString, G);
		while (res.hasNext()) {
			QuerySolution QS = res.next();
			a += Cd(G, QS.get("p").asResource().getURI(), R1, R2) / (1 + Math.log(Cd(G, QS.get("p").asResource().getURI(), R1, "")));
			b += Cd(G, QS.get("p").asResource().getURI(), R2, R1) / (1 + Math.log(Cd(G, QS.get("p").asResource().getURI(), R2, "")));
		}

		/*		System.out.println("a=" + a);
				System.out.println("b=" + a);*/
		return 1 / (1 + a + b);
	}

	/**
	 *  Cio is a function that computes the number of (incoming + indirect + distinct) links between resources in a graph G.
	 *  if p or R1 or R2 =="" then calculate all other possibilities
	 */
	public static double Cio(Model G, String p, String R1, String R2) {
		double score = 0.0;

		String queryString = " select (count(distinct *) as ?c) where { ";

		if (R1 != "") {
			queryString += "<" + R1 + "> ?p1 ?o.";
		} else
			queryString += "?r1 ?p1 ?o. ";

		if (R2 != "") {
			queryString += "<" + R2 + "> ?p2 ?o.";
		} else
			queryString += "?r2 ?p2 ?o. ";

		if (p != "") {
			queryString += " filter((?p1 = <" + p + ">) && (?p2 = <" + p + ">)) . } ";
		} else
			queryString += " } ";

		//System.out.println(queryString);
		ResultSet res = SparqlManager.sparqlQueryOverJenaModel(queryString, G);

		while (res.hasNext()) {
			QuerySolution QS = res.next();
			try {
				score = QS.get("c").asLiteral().getDouble();
			} catch (Exception e) {
				// TODO: handle exception
			}
			break;
		}

		return score;
	}

	/**
	 *  Cii is a function that computes the number of (outcoming + indirect + distinct) links between resources in a graph G.
	 *  if p or R1 or R2 =="" then calculate all other possibilities
	 */
	public static double Cii(Model G, String p, String R1, String R2) {
		double score = 0.0;
		String queryString = " select (count(distinct *) as ?c) where { ";

		if (R1 != "") {
			queryString += "?s ?p1 <" + R1 + ">. ";
		} else
			queryString += "?s ?p1 ?r1. ";

		if (R2 != "") {
			queryString += "?s ?p2 <" + R2 + ">";
		} else
			queryString += "?s ?p2 ?r2. ";

		if (p != "") {
			queryString += " filter((?p1 = <" + p + ">) && (?p2 = <" + p + ">)) . } ";
		} else
			queryString += " } ";

		//System.out.println(queryString);
		ResultSet res = SparqlManager.sparqlQueryOverJenaModel(queryString, G);

		while (res.hasNext()) {
			QuerySolution QS = res.next();
			try {
				score = QS.get("c").asLiteral().getDouble();
			} catch (Exception e) {
				// TODO: handle exception
			}

			break;
		}
		return score;
	}

	/**
	 *  LDSD_i is a function that computes the distance between 2 resources in a graph G. (using Cio,Cii)
	 * 
	 */
	public static double LDSD_i(Model G, String R1, String R2) {
		return 1 / (1 + Cio(G, "", R1, R2) + Cii(G, "", R1, R2));
	}

	/**
	 *  LDSD_iw is Like LDSD_i but with weight
	 * 
	 */
	public static double LDSD_iw(Model G, String R1, String R2) {
		double a = 0.0;
		double b = 0.0;

		String QueryString = "select distinct ?p where {?s ?p ?o. filter(?s=<" + R1 + "> || ?s=<" + R2 + "> || ?o=<" + R1 + "> || ?o=<" + R2 + "> )}";
		ResultSet res = SparqlManager.sparqlQueryOverJenaModel(QueryString, G);

		while (res.hasNext()) {
			QuerySolution QS = res.next();
			a += Cii(G, QS.get("p").asResource().getURI(), R1, R2) / (1 + Math.log(Cii(G, QS.get("p").asResource().getURI(), R1, "")));
			b += Cio(G, QS.get("p").asResource().getURI(), R1, R2) / (1 + Math.log(Cio(G, QS.get("p").asResource().getURI(), R1, "")));
		}

		/*		System.out.println("a=" + a);
				System.out.println("b=" + a);*/
		return 1 / (1 + a + b);
	}

	/**
	 *  LDSD_i is a function that computes the distance between 2 resources in a graph G, using direct and indirect distances
	 *  ie: using LDSD_d and LDSD_i
	 * 
	 */

	public static double LDSD_c(Model G, String R1, String R2) {
		return 1 / (1 + LDSD_d(G, R1, R2) + LDSD_i(G, R1, R2));
	}

	/**
	 *  LDSD_cw is Like LDSD_c but with weight
	 *  ie: using LDSD_dw and LDSD_iw
	 */
	public static double LDSD_cw(Model G, String R1, String R2) {
		double a = 0.0;
		double b = 0.0;
		double c = 0.0;
		double d = 0.0;

		String QueryString = "select distinct ?p where {?s ?p ?o. filter(?s=<" + R1 + "> || ?s=<" + R2 + "> || ?o=<" + R1 + "> || ?o=<" + R2 + "> )}";
		ResultSet res = SparqlManager.sparqlQueryOverJenaModel(QueryString, G);

		while (res.hasNext()) {
			QuerySolution QS = res.next();
			a += Cd(G, QS.get("p").asResource().getURI(), R1, R2) / (1 + Math.log(Cd(G, QS.get("p").asResource().getURI(), R1, "")));
			b += Cd(G, QS.get("p").asResource().getURI(), R2, R1) / (1 + Math.log(Cd(G, QS.get("p").asResource().getURI(), R2, "")));
			c += Cii(G, QS.get("p").asResource().getURI(), R1, R2) / (1 + Math.log(Cii(G, QS.get("p").asResource().getURI(), R1, "")));
			d += Cio(G, QS.get("p").asResource().getURI(), R1, R2) / (1 + Math.log(Cio(G, QS.get("p").asResource().getURI(), R1, "")));
		}

		/*		System.out.println("a=" + a);
				System.out.println("b=" + a);*/
		return 1 / (1 + a + b + c + d);
	}

	/**
	 *  simRank is a function that computes the distance between 2 resources in a graph G, using simRank method
	 *  simRank implement the naive method without any additions
	 *  simRank is recursive function
	 *  K is the iteration number
	 */

	Map<String, Double> simRankMap = new HashMap<String, Double>();

	public double simRank(int k, Model G, String R1, String R2) {
		if (simRankMap.get(R1 + "-" + R2 + "-" + k) != null) {
			double R = simRankMap.get(R1 + "-" + R2 + "-" + k);
			//System.out.println("SimRank " + k + " for    R1= " + R1 + " and   R2= " + R2 + " is " + R);
			return R;
		} else if (simRankMap.get(R2 + "-" + R1 + "-" + k) != null) {
			double R = simRankMap.get(R2 + "-" + R1 + "-" + k);
			//System.out.println("SimRank " + k + " for    R1= " + R1 + " and   R2= " + R2 + " is " + R);
			return R;
		} else {
			double c = 0.8;
			if (k == 0)//fin
			{
				if (R1 == R2)
					return 1;
				else
					return 0;
			} else {
				ArrayList<String> NeighborsOfR1 = getURINeighbors(R1, G);
				ArrayList<String> NeighborsOfR2 = getURINeighbors(R2, G);

				double R = 0.0;
				for (String Neighbor1 : NeighborsOfR1) {
					for (String Neighbor2 : NeighborsOfR2) {
						R += simRank(k - 1, G, Neighbor1, Neighbor2);

					}
				}
				R = (c / (NeighborsOfR1.size() * NeighborsOfR2.size())) * R;

				simRankMap.put(R1 + "-" + R2 + "-" + k, R);
				return R;
			}
		}
	}

	public static ArrayList<String> getNeighbors(String R1, Model G) {
		ArrayList<String> uris = new ArrayList<String>();

		String queryString = " select distinct ?s where { ";
		queryString += "{ ?s ?p1 <" + R1 + ">.} union { <" + R1 + ">  ?p2 ?s.}";
		queryString += "}";

		//System.out.println(queryString);
		ResultSet res = SparqlManager.sparqlQueryOverJenaModel(queryString, G);

		while (res.hasNext()) {

			uris.add(res.next().get("s").toString());
		}

		return uris;
	}

	public static ArrayList<String> getURINeighbors(String R1, Model G) {
		ArrayList<String> uris = new ArrayList<String>();

		String queryString = " select distinct ?s where { ";
		queryString += "{ ?s ?p1 <" + R1 + ">. filter( isURI(?s) ).} union { <" + R1 + ">  ?p2 ?s. filter( isURI(?s) ).}";
		queryString += "}";

		//System.out.println(queryString);
		ResultSet res = SparqlManager.sparqlQueryOverJenaModel(queryString, G);

		while (res.hasNext()) {

			uris.add(res.next().get("s").toString());
		}

		return uris;
	}

	/**
	 * simpleDistance is a function that gives the numbers of paths possibles
	 */

	public static double simpleDistance(Model G, String R1, String R2) {
		double dis = 0.0;
		String Q = "select (count(distinct *) as ?c) where {";

		//level 1 (direct)
		Q += "{<" + R1 + "> ?p <" + R2 + ">.} union";
		Q += "{<" + R2 + "> ?p <" + R1 + ">.} union";

		//level 2
		Q += "{<" + R1 + "> ?p1 ?o. ?o ?p2 <" + R2 + ">.} union ";
		Q += "{?o ?p1 <" + R1 + ">. ?o ?p2 <" + R2 + ">.} union ";
		Q += "{<" + R1 + "> ?p1 ?o. <" + R2 + "> ?p2 ?o.} union ";
		Q += "{?o ?p1 <" + R1 + ">. <" + R2 + "> ?p2 ?o.} union ";

		//level 3
		Q += "{<" + R1 + "> ?p1 ?o1. ?o1 ?p2 ?o2. ?o2 ?p3 <" + R2 + ">.} union ";

		Q += "{<" + R1 + "> ?p1 ?o1. ?o1 ?p2 ?o2. <" + R2 + "> ?p3 ?o2.} union ";
		Q += "{<" + R1 + "> ?p1 ?o1. ?o2 ?p2 ?o1. ?o2 ?p3 <" + R2 + ">.} union ";
		Q += "{?o1 ?p1 <" + R1 + ">. ?o1 ?p2 ?o2. ?o2 ?p3 <" + R2 + ">.} union ";

		Q += "{<" + R1 + "> ?p1 ?o1. ?o2 ?p2 ?o1. <" + R2 + "> ?p3 ?o2.} union ";
		Q += "{?o1 ?p1 <" + R1 + ">. ?o2 ?p2 ?o1. ?o2 ?p3 <" + R2 + ">.} union ";
		Q += "{?o1 ?p1 <" + R1 + ">. ?o1 ?p2 ?o2.  <" + R2 + "> ?p3 ?o2.} union ";

		Q += "{?o1 ?p1 <" + R1 + ">. ?o2 ?p2 ?o1.  <" + R2 + "> ?p3 ?o2.} }";

		ResultSet res = SparqlManager.sparqlQueryOverJenaModel(Q, G);
		while (res.hasNext()) {
			QuerySolution QS = res.next();
			dis = QS.get("c").asLiteral().getDouble();
			break;
		}

		/*		System.out.println("a=" + a);
				System.out.println("b=" + a);*/
		return dis;

	}

	/**
	 * simpleDistance_w is Like simpleDistance but with weight
	 * p1= direct connection important
	 * p2= 2 step important
	 * p3= 3 step important
	 */
	public static double simpleDistance_w(Model G, String R1, String R2) {
		double p1 = 0.55;
		double p2 = 0.3;
		double p3 = 0.15;

		double dis1 = 0.0;
		String Q = "select (count(distinct *) as ?c) where {";
		//level 1 (direct)
		Q += "{<" + R1 + "> ?p <" + R2 + ">.} union";
		Q += "{<" + R2 + "> ?p <" + R1 + ">.} } ";
		ResultSet res = SparqlManager.sparqlQueryOverJenaModel(Q, G);
		while (res.hasNext()) {
			QuerySolution QS = res.next();
			dis1 = QS.get("c").asLiteral().getDouble();
		}

		double dis2 = 0.0;
		Q = "select (count(distinct *) as ?c) where {";
		//level 2
		Q += "{<" + R1 + "> ?p1 ?o. ?o ?p2 <" + R2 + ">.} union ";
		Q += "{?o ?p1 <" + R1 + ">. ?o ?p2 <" + R2 + ">.} union ";
		Q += "{<" + R1 + "> ?p1 ?o. <" + R2 + "> ?p2 ?o.} union ";
		Q += "{?o ?p1 <" + R1 + ">. <" + R2 + "> ?p2 ?o.} } ";
		res = SparqlManager.sparqlQueryOverJenaModel(Q, G);
		while (res.hasNext()) {
			QuerySolution QS = res.next();
			dis2 = QS.get("c").asLiteral().getDouble();
		}

		double dis3 = 0.0;
		Q = "select (count(distinct *) as ?c) where {";

		//level 3
		Q += "{<" + R1 + "> ?p1 ?o1. ?o1 ?p2 ?o2. ?o2 ?p3 <" + R2 + ">.} union ";

		Q += "{<" + R1 + "> ?p1 ?o1. ?o1 ?p2 ?o2. <" + R2 + "> ?p3 ?o2.} union ";
		Q += "{<" + R1 + "> ?p1 ?o1. ?o2 ?p2 ?o1. ?o2 ?p3 <" + R2 + ">.} union ";
		Q += "{?o1 ?p1 <" + R1 + ">. ?o1 ?p2 ?o2. ?o2 ?p3 <" + R2 + ">.} union ";

		Q += "{<" + R1 + "> ?p1 ?o1. ?o2 ?p2 ?o1. <" + R2 + "> ?p3 ?o2.} union ";
		Q += "{?o1 ?p1 <" + R1 + ">. ?o2 ?p2 ?o1. ?o2 ?p3 <" + R2 + ">.} union ";
		Q += "{?o1 ?p1 <" + R1 + ">. ?o1 ?p2 ?o2.  <" + R2 + "> ?p3 ?o2.} union ";

		Q += "{?o1 ?p1 <" + R1 + ">. ?o2 ?p2 ?o1.  <" + R2 + "> ?p3 ?o2.} }";

		res = SparqlManager.sparqlQueryOverJenaModel(Q, G);
		while (res.hasNext()) {
			QuerySolution QS = res.next();
			dis3 = QS.get("c").asLiteral().getDouble();
		}

		return ((dis1 * p1) + (dis2 * p2) + (dis3 * p3));

	}

	public static void test() {
		long startTime = System.currentTimeMillis();
		Model model = RDFManager.readFile(PropertiesManager.getProperty("rootPath")+"\\RDF\\Temp\\QEGraph.rdf");
		LinkedDataDistanceManager LDM = new LinkedDataDistanceManager();

		// test simRank

		/*double res = LDM.simRank(3, model, "http://dbpedia.org/resource/Information_retrieval", "http://dbpedia.org/resource/Information_technology");
		System.out.println("Result(k=3): http://dbpedia.org/resource/Information_technology = " + res);

		res = LDM.simRank(3, model, "http://dbpedia.org/resource/Information_retrieval", "http://dbpedia.org/resource/Information");
		System.out.println("Result (k=3): http://dbpedia.org/resource/Information = " + res);
		*/

		//test get Neighbors

		/*ArrayList<String> ar = getURINeighbors("http://dbpedia.org/resource/Information_retrieval", model);
		for (int i = 0; i < ar.size(); i++) {
			System.out.println(ar.get(i));
		}*/

		//test Sparql

		/*String queryString = " select * {  <http://dbpedia.org/resource/Information_retrieval> $path <http://dbpedia.org/resource/Information_technology>.  }";

		System.out.println(queryString);
		ResultSet res = SparqlManager.sparqlQueryOverJenaModel(queryString, model);

		while (res.hasNext()) {
			//System.out.println(res.next().get("path"));
			System.out.println(res.next().toString());
		}*/

		double avg = 0.0;
		//test with ranking
		Map<String, Double> res = new HashMap<String, Double>();
		ValueComparator bvc = new ValueComparator(res);
		TreeMap<String, Double> sorted_res = new TreeMap<String, Double>(bvc);

		ResIterator l1 = model.listSubjects();
		while (l1.hasNext()) {
			Resource R1 = l1.next();
			double score_dw = LDM.LDSD_c(model, "http://dbpedia.org/resource/Information_retrieval", R1.getURI());
			if (score_dw > 0) {
				res.put(R1.getURI(), score_dw);
				avg += score_dw;
			}

		}

		sorted_res.putAll(res);
		System.out.println("results: " + sorted_res);
		System.out.println("AVG: " + (avg / res.size()));

		long endTime = System.currentTimeMillis();
		long totalTime = endTime - startTime;
		//System.out.println("Test time= " + (totalTime / 1000) + " Sec");
		System.out.println("Test time= " + (totalTime) + " mSec");
	}

	public static void main(String[] args) {

		try {
			System.setErr(new PrintStream(new File("errlog.txt")));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		test();

	}
}
