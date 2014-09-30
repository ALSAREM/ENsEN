package ensen.entities;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.NodeIterator;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Selector;
import com.hp.hpl.jena.rdf.model.SimpleSelector;

import ensen.controler.DBpediaLookupClient;
import ensen.controler.RDFManager;
import ensen.controler.RDFizer;
import ensen.controler.SparqlManager;
import ensen.controler.WordNetManager;
import ensen.util.PropertiesManager;
import ensen.util.permute;

public class Query {
	static Logger log = Logger.getLogger(Query.class.getName());
	public String Text;
	public String URI;
	public String ExtendedText = "";
	public Model graph;
	public Model ExtendedGraph;
	public List<EnsenDBpediaResource> triplets;
	public List<Document> results;
	public Set<String> RelatedResLD = null;
	public Set<String> RelatedResDoc = null;
	private ArrayList<String> QRelRes;
	public ArrayList<String> QExtendedRes;
	private int maximumNOfExWordForEachWord = 2;
	private int minimumLengthOfExtendedWord = 3;
	public List<EnsenDBpediaResource> Resources;
	public int id = 0;

	public Query() {

	}

	public Query(String text) {
		Text = text;
		extendQueryLexicaly();
		getResourcesFromSpotlight();
	}

	public void extendQueryLexicaly() {
		WordNetManager WN = new WordNetManager();
		ExtendedText = WN.extendTextWithLimit(Text, maximumNOfExWordForEachWord);
		Set<String> LHS = new LinkedHashSet<String>(Arrays.asList(ExtendedText.toLowerCase().split(" ")));
		ExtendedText = "";
		Iterator<String> It = LHS.iterator();
		while (It.hasNext()) {
			String str = It.next();
			if (str.length() > minimumLengthOfExtendedWord) {
				ExtendedText += str + " ";
				//System.out.print(str + ",");

			}

		}

		ExtendedText += Text;
		System.out.println("Extended Query" + ExtendedText);

	}

	public void getResourcesFromSpotlight() {
		Resources = RDFizer.rdfizeTextWithoutThreads(ExtendedText, "", "");
		System.out.println("Query's resources");
		System.out.println(Resources);
	}

	public void getResourcesFromLD() {
		System.out.println("getAllCominations");
		String[] cominations = permute.getAllCominations(3, ExtendedText);
		//System.out.println("fin of getAllCominations" + cominations);
		String TextCominations = "";
		for (int i = 0; i < cominations.length; i++) {
			if ((cominations[i] != null) && (cominations[i].trim().compareTo("") != 0)) {
				if (!TextCominations.contains(cominations[i] + " , "))
					TextCominations += cominations[i] + " , ";
			}
		}
		TextCominations = ExtendedText;
		//System.out.println(cominations.length + "-->" + TextCominations);
		String cachePath = PropertiesManager.getProperty("cachePath") + Text.toLowerCase().replace(" ", "_") + "_text";
		//System.out.println("Annotate Q Text from DBpedia Lookup, Q: (" + ExtendedText + "): " + TextCominations);

		QRelRes = new ArrayList<String>();
		QExtendedRes = new ArrayList<String>();
		List<EnsenDBpediaResource> ResourcesFromLD = RDFizer.rdfizeTextWithoutThreads(TextCominations, cachePath, "");

		if (ResourcesFromLD != null) {
			for (int i = 0; i < ResourcesFromLD.size(); i++) {
				EnsenDBpediaResource DBR = ResourcesFromLD.get(i);
				if (!QRelRes.contains(DBR.getFullUri()))
					QRelRes.add(DBR.getFullUri());
			}

			//extend query
			QExtendedRes = extendQuery(QRelRes);
			for (String uri : QExtendedRes) {
				if (!QRelRes.contains(uri))
					QRelRes.add(uri);
			}
		}

		RelatedResLD = new HashSet<String>(QRelRes);
	}

	private ArrayList<String> extendQuery(ArrayList<String> qRelRes2) {
		ArrayList<String> pridecats = new ArrayList<String>();
		pridecats.add("http://www.w3.org/2002/07/owl#sameAs");
		pridecats.add("http://www.w3.org/2002/07/owl#equivalentClass");
		pridecats.add("http://www.w3.org/2002/07/owl#FunctionalProperty");
		pridecats.add("http://www.w3.org/2002/07/owl#InverseFunctionalProperty");
		pridecats.add("http://xmlns.com/foaf/0.1/primaryTopic");
		pridecats.add("http://xmlns.com/foaf/0.1/isPrimaryTopicOf");
		pridecats.add("http://www.w3.org/2000/01/rdf-schema#seeAlso");
		pridecats.add("http://www.w3.org/2000/01/rdf-schema#isDefinedBy");

		Model m = RDFManager.createRDFModel();
		for (String R : qRelRes2) {
			for (String PURI : pridecats) {
				m.add(SparqlManager.searchSparql(R, PURI, ""));
			}
		}

		NodeIterator objects = m.listObjects();
		ArrayList<String> res = new ArrayList<String>();
		while (objects.hasNext()) {
			RDFNode O = objects.next();
			if (O.isResource())
				res.add(O.asResource().getURI());
		}

		return res;

	}

	public void getResourcesFromLDUsingLookup() {
		String[] cominations = permute.getAllCominations(3, ExtendedText);
		DBpediaLookupClient DBLC = new DBpediaLookupClient();
		QRelRes = new ArrayList<String>();
		for (int i = 0; i < cominations.length; i++) {
			if (cominations[i] != null)
				if (cominations[i].trim().compareTo("") != 0) {
					//System.out.println(cominations[i]);
					String URL = null;
					try {
						URL = DBLC.getOneResource(cominations[i], "", 1);
					} catch (Exception e) {
						e.printStackTrace();
					}
					if ((URL != null) && (URL.trim() != "")) {
						QRelRes.add(URL);
					}
				}
		}
		RelatedResLD = new HashSet<String>(QRelRes);
	}

	public Set<String> getResourcesFromDoc(Document D) {
		ArrayList<String> qRelResFinal = new ArrayList<String>();
		for (String element : RelatedResLD) {
			Selector selector1 = new SimpleSelector(null, null, element);
			Selector selector2 = new SimpleSelector(D.fullGraph.createResource(element), null, (RDFNode) null);
			if ((D.fullGraph.listStatements(selector1).toList().size() > 0) || (D.fullGraph.listStatements(selector2).toList().size() > 0)) {
				qRelResFinal.add(element);
			}
		}
		RelatedResDoc = new HashSet<String>(qRelResFinal);
		return RelatedResDoc;
	}

	public String printRelatedResFromLD() {
		String HTML = "<aside><ul>";
		Iterator<String> It3 = RelatedResLD.iterator();
		while (It3.hasNext()) {
			String res = It3.next();
			HTML += "<li><a href=\"" + res + "\">" + res + "</a> </li>";
		}
		return HTML + "</ul></aside>";
	}

	public String printRelatedResInDoc() {
		String HTML = "<aside><ul>";
		Iterator<String> It2 = RelatedResDoc.iterator();
		while (It2.hasNext()) {
			String res = It2.next();
			HTML += "<li><a href=\"" + res + "\">" + res + "</a> </li>";
		}
		return HTML + "</ul></aside>";
	}
}
