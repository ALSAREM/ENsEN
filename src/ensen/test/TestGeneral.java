package ensen.test;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.TreeMap;

import com.google.api.services.customsearch.model.Result;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Model;

import ensen.control.GraphMatching;
import ensen.control.PropertiesManager;
import ensen.control.RDFManager;
import ensen.control.RDFizer;
import ensen.control.Searcher;
import ensen.control.SparqlManager;
import ensen.entities.Document;
import ensen.entities.EnsenDBpediaResource;
import ensen.entities.Query;
import ensen.evaluation.db.EvaluationDBControler;
import ensen.util.permute;

public class TestGeneral {
	public TestGeneral() {
		// TODO Auto-generated constructor stub
	}

	public static void testVirtuoso() {
		ResultSet res = SparqlManager.querySparql("select distinct *  where {?a ?b ?c} limit 100");
		int counter = 0;
		for (; res.hasNext();) {
			QuerySolution QS = res.nextSolution();
			System.out.println(QS.toString());
			counter++;

		}
		System.out.println(counter);
	}

	/**
	 * @param args
	 */
	public static void testMatching() {
		long startTime = System.currentTimeMillis();

		Model a = RDFManager.readFile("C:\\Users\\mazen\\workspace\\EBsEN\\RDF\\Temp\\QEGraph.rdf");
		System.out.println("Get A, Size=" + a.size());
		Model b = RDFManager.readFile("C:\\Users\\mazen\\workspace\\EBsEN\\RDF\\Temp\\D0_fullGraph.rdf");
		System.out.println("Get B, Size=" + b.size());

		String lang = PropertiesManager.getProperty("lang");
		a = RDFManager.getObtimalGraph(a, lang);
		System.out.println("Get Optimal A, Size=" + a.size());
		b = RDFManager.getObtimalGraph(b, lang);
		System.out.println("Get Optimal B, Size=" + b.size());
		a = RDFManager.removeHasApredicates(a);
		System.out.println("removeHasApredicates A, Size=" + a.size());

		TreeMap<String, Double> Res = GraphMatching.Match(a, b);
		System.out.println("results: " + Res);

		long endTime = System.currentTimeMillis();
		long totalTime = endTime - startTime;
		System.out.println("Test time= " + (totalTime) + " mSec");
	}

	public static void testRDFIntersection() {
		long startTime = System.currentTimeMillis();

		Model a = RDFManager.readFile("C:\\Users\\mazen\\workspace\\EBsEN\\RDF\\Temp\\QEGraph.rdf");
		System.out.println("Get A, Size=" + a.size());
		Model b = RDFManager.readFile("C:\\Users\\mazen\\workspace\\EBsEN\\RDF\\Temp\\D0_fullGraph.rdf");
		System.out.println("Get B, Size=" + b.size());

		String lang = PropertiesManager.getProperty("lang");
		a = RDFManager.getObtimalGraph(a, lang);
		System.out.println("Get Optimal A, Size=" + a.size());
		b = RDFManager.getObtimalGraph(b, lang);
		System.out.println("Get Optimal B, Size=" + b.size());
		a = RDFManager.removeHasApredicates(a);
		System.out.println("removeHasApredicates A, Size=" + a.size());

		ArrayList<String> resIntersect = RDFManager.intersection(a, b);
		System.out.println(resIntersect);
		System.out.println("intersection A,B Size=" + resIntersect.size());

		long endTime = System.currentTimeMillis();
		long totalTime = endTime - startTime;
		System.out.println("Test time= " + (totalTime) + " mSec");
	}

	public static void testJenaRDFIntersection() {
		long startTime = System.currentTimeMillis();

		Model a = RDFManager.readFile("C:\\Users\\mazen\\workspace\\EBsEN\\RDF\\Temp\\QEGraph.rdf");
		System.out.println("Get A, Size=" + a.size());
		Model b = RDFManager.readFile("C:\\Users\\mazen\\workspace\\EBsEN\\RDF\\Temp\\D0_fullGraph.rdf");
		System.out.println("Get B, Size=" + b.size());

		String lang = PropertiesManager.getProperty("lang");
		a = RDFManager.getObtimalGraph(a, lang);
		System.out.println("Get Optimal A, Size=" + a.size());
		b = RDFManager.getObtimalGraph(b, lang);
		System.out.println("Get Optimal B, Size=" + b.size());
		a = RDFManager.removeHasApredicates(a);
		System.out.println("removeHasApredicates A, Size=" + a.size());

		Model res = a.intersection(b);
		res.write(System.out);
		System.out.println("results: " + res.size());

		long endTime = System.currentTimeMillis();
		long totalTime = endTime - startTime;
		System.out.println("Test time= " + (totalTime) + " mSec");
	}

	public static void testSplitModel() {
		Model b = RDFManager.readFile("C:\\Users\\mazen\\workspace\\EBsEN\\RDF\\Temp\\QEGraph.rdf");
		//copy without http://ensen.org/data#has-a
		Model c = RDFManager.removeHasApredicates(b);
		System.out.println("c Size= " + c.size());
		String lang = PropertiesManager.getProperty("lang");
		ArrayList<Model> res = RDFManager.splitModel(RDFManager.getObtimalGraph(c, lang));
		Model inter = res.get(0);
		for (int i = 0; i < res.size(); i++) {
			System.out.println("a graph size=" + res.get(i).size());
			inter = inter.intersection(res.get(i));
		}
		inter.write(System.out);
	}

	public static void testSubgraph() {
		Model b = RDFManager.readFile("D:\\Development\\workspace\\EBsEN\\RDF\\D0_fullGraph.rdf");
		//System.out.println(b.getGraph());
		//b.getGraph(). write(System.out);
		Model c = RDFManager.getSubgraph(b, "http://dbpedia.org/resource/Debian");
		System.out.println("c Size= " + c.size());
		c.write(System.out);
	}

	public static void testCoOcurrance() {

		HashMap<String, Integer> map = new HashMap<String, Integer>();

		String str = "Lyon Wikipedia free encyclopedia From Wikipedia free encyclopedia to navigation search article about French other Lyon disambiguation Motto Avant avant le melhor Franco Proven Forward forward the the city the foreground Centre Pont Bonaparte night the Lafayette Bottom Place Bellecour the Basilique Notre de Fourvi and Tour Metal the background flag coat arms Location within ne Alpes region Administration Country France Region ne Alpes Department ne Arrondissement Subdivisions arrondissements Intercommunality Urban Community Lyon Mayor rard Collomb Statistics Elevation area Population Ranking in France Density Urban Population Metro Population zone INSEE Postal Website French Register which excludes lakes ponds glaciers or acres river estuaries Population without double counting residents multiple communes students military personnel counted Coordinates French pronunciation listen locally Occitan u Arpitan Liyon English traditionally ";
		str = str.toLowerCase();
		int count = -1;
		for (int i = 0; i < str.length(); i++) {
			if ((!Character.isLetter(str.charAt(i))) || (i + 1 == str.length())) {
				if (i - count > 1) {
					if (Character.isLetter(str.charAt(i)))
						i++;
					String word = str.substring(count + 1, i);
					if (map.containsKey(word)) {
						map.put(word, map.get(word) + 1);
					} else {
						map.put(word, 1);
					}
				}
				count = i;
			}
		}
		ArrayList<Integer> list = new ArrayList<Integer>();
		list.addAll(map.values());
		Collections.sort(list, Collections.reverseOrder());
		int last = -1;
		for (Integer i : list) {
			if (last == i)
				continue;
			last = i;
			for (String s : map.keySet()) {
				if (map.get(s) == i)
					System.out.println(s + ":" + i);
			}
		}
	}

	private static void testSerialization() {
		Searcher S = new Searcher();
		String q = "Syria";
		List<Result> documents = S.searchInGoogle(q, Integer.parseInt(PropertiesManager.getProperty("nOfRes")));
		int counter = 0;
		for (Result d : documents) {
			FileOutputStream fout;
			try {
				fout = new FileOutputStream("d:\\cache\\" + q.toLowerCase().replace(" ", "_") + "\\doc" + counter + ".ser");
				ObjectOutputStream oos = new ObjectOutputStream(fout);
				oos.writeObject(d.getPagemap());

			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			counter++;
		}

	}

	static void callQueris() {
		//String qs = "Karl Marx;Pineal Gland;Syria;The Beatles;Barack Obama;World War II;India;Transformers;Heroes;Smart glass;Gangnam Style;Steve Jobs;UEFA Euro 2012;Olympic Games;Oasis";
		//String qs = "Game of Thrones;Justin Timberlake;Selena Gomez;William Shakespeare;Opel Vectra;Ford Mustang;Massachusetts Institute of Technology";
		//inex
		String qs = "Nobel prize; best movie;yoga exercise;  alchemists periodic table elements; opera singer italian;   natural disaster; israeli director;applications bayesian networks bioinformatics;olive oil health benefit;vitiligo pigment disorder cause treatment;native american indian;content based image retrieval;Voice over IP;cycle road skill race;rent buy home;Dwyane Wade;Latent semantic indexing;IBM computer;wonder girls;Szechwan dish food cuisine;plays of Shakespeare;cloud computing;scenic spot in Beijing;generalife gardens;Zhang Yimou;vehicles fastest speed;personality famous;popular dog cartoon character;sabre;evidence theory dempster schafer;Al-Andalus taifa kingdoms;the evolution of the moon;Bermuda Triangle;";
		double a = 0.125;//in
		double b = 0.05;//out
		double c = 0.2;//freq
		double d = 0.3;//Query
		double e = 0.075;//sim	
		double f = 0.25;//sa	
		boolean rerank = false;

		String[] Queries = qs.split(";");
		for (String q : Queries) {
			ArrayList<Document> documents = null;
			Query Q = null;
			Searcher S = new Searcher();
			documents = S.search(q, Integer.parseInt(PropertiesManager.getProperty("nOfRes")));
			Q = new Query(q);
			Q.extendQueryLexicaly();
			Q.getResourcesFromLD();
			ArrayList<String> usedImages = new ArrayList<String>();
			for (int i = 0; i < documents.size(); i++) {
				try {
					Document Doc = documents.get(i);
					Doc.usedImages = usedImages;
					Doc.analyzeDocument(Q, a, b, c, d, e, f, rerank);
					usedImages = Doc.usedImages;
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}

			EvaluationDBControler EDBC = new EvaluationDBControler();
			EDBC.insert(Q, documents);
		}

	}

	private static void compareSpotlight() {
		String text = "Marx became co-editor of a new radical leftist newspaper, the Deutsch-Französische Jahrbücher (German-French Annals), then being set up by German socialist Arnold Ruge to bring together German and French radicals.[51] Based in Paris, France, it was here that Marx and his wife moved in October 1843. Initially living with Ruge and his wife communally at 23 Rue Vaneau, they found the living conditions difficult, so moved out following the birth of their daughter Jenny in 1844.[52] Although intended to attract writers from both France and the German states, the Jahrbücher was dominated by the latter; the only non-German writer was the exiled Russian anarcho-communist Michael Bakunin.[53] Marx contributed two essays to the paper, Introduction to a Contribution to the Critique of Hegel's Philosophy of Right and On the Jewish Question, the latter introducing his belief that the proletariat were a revolutionary force and marking his embrace of communism.";

		long s = System.currentTimeMillis();
		//local
		String cachePath = PropertiesManager.getProperty("cachePath") + Math.random() + "test_text";

		PropertiesManager.setProperty("DBpediaSpotlightUseLocal", "true");
		//List<EnsenDBpediaResource> ResourcesFromLD = RDFizer.rdfizeTextWithoutThreads(text + text + text + text + text + text, cachePath);
		long l = System.currentTimeMillis();

		//server
		String cachePath1 = PropertiesManager.getProperty("cachePath") + Math.random() + "test_text";

		PropertiesManager.setProperty("DBpediaSpotlightUseLocal", "false");
		List<EnsenDBpediaResource> ResourcesFromLD1 = RDFizer.rdfizeTextWithoutThreads(text + text + text + text + text + text, cachePath1);
		long o = System.currentTimeMillis();

		System.out.println("local time:" + (l - s));
		System.out.println("server time:" + (o - l));
		System.out.println("Quality:" + ((o - l) * 1.0 / (l - s)));

	}

	public static void main(String[] args) {
		String[] cominations = permute.getAllCominations(3, "mazen alsarem syria france lyon salam julia");
		for (int i = 0; i < cominations.length; i++) {
			System.err.println(cominations[i]);
		}

	}
}
