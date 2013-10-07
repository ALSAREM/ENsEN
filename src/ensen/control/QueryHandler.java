package ensen.control;

import java.util.ArrayList;
import java.util.HashSet;

import org.dbpedia.spotlight.model.Text;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.NodeIterator;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;

import ensen.entities.Query;

public class QueryHandler {
	public QueryHandler() {
		// TODO Auto-generated constructor stub
	}

	public Query createQuery(String in, boolean WordNet, boolean TextAnnotation, int nOfRes, DBpediaSpotlightClient c) {
		Query Q = new Query();
		Q.graph = RDFManager.createRDFModel();
		Q.graph.getNsPrefixMap().put("ns", "http://dbpedia.org/namespace");
		Q.Text = in;
		Q.URI = "http://ensen.org/data#q";
		// extend text by WordNet
		if (WordNet) {
			System.out.println("Extend text by WordNet");
			WordNetManager WN = new WordNetManager();
			Q.ExtendedText = WN.extendText(Q.Text);
		} else
			Q.ExtendedText = Q.Text;

		// Annotate Text from DBpedia Spotlight
		if (TextAnnotation) {
			System.out.println("Annotate Q Text from DBpedia Spotlight");
			try {

				Q.triplets = c.ensenExtract(new Text(Q.ExtendedText));
				Q.graph.add((Model) RDFManager.generateModelFromDocument(Q.URI, Q.triplets).get(0));

			} catch (Exception e1) {
				// TODO Auto-generated catch block

				System.out.println("Error: " + e1.getMessage());
			}
		}

		// Annotate Text from DBpedia Lookup (local)
		DBpediaLookupClient DBLC = new DBpediaLookupClient();

		System.out.println("Annotate Q Text from DBpedia Lookup, Q: " + Q.ExtendedText);
		try {
			Q.graph.add(DBLC.qetAllEntities(Q.ExtendedText, "", nOfRes));
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		return Q;

	}

	public Query Qexpansion(Query Q) {

		Q.ExtendedGraph = RDFManager.createRDFModel();
		Q.ExtendedGraph.add(Q.graph);

		// statistics before expansion
		int objects = Q.ExtendedGraph.listObjects().toList().size();
		int trips = Q.ExtendedGraph.listStatements().toList().size();
		int subjects = Q.ExtendedGraph.listSubjects().toList().size();

		System.out.println("Statistics before expansion: Triples (" + trips + "),Entities(" + (objects + subjects) + ") ,Subjects(" + subjects + ") ,Objects(" + objects + ")");

		int n = Integer.parseInt(PropertiesManager.getProperty("ExpansionLevel"));

		HashSet<String> doneObjURIs = new HashSet<String>();
		HashSet<String> donePridecateURIs = new HashSet<String>();

		for (int i = 0; i < n; i++) {
			// A: Expansion by Subject
			// Get all info for graph's entities (for URI objects)
			HashSet<String> ObjURIS = getObjectsURIsFromGraph(Q.ExtendedGraph);
			ObjURIS.removeAll(doneObjURIs);// delete uris that we had get it, in
											// last interations
			Q.ExtendedGraph.add(SparqlManager.getDocModel(ObjURIS));
			doneObjURIs.addAll(ObjURIS);

			// statistics after i expansion
			objects = Q.ExtendedGraph.listObjects().toList().size();
			trips = Q.ExtendedGraph.listStatements().toList().size();
			subjects = Q.ExtendedGraph.listSubjects().toList().size();
			System.out.println("Statistics after (" + (i + 1) + ") Subject expansion: Triples (" + trips + "),Entities(" + (objects + subjects) + ") ,Subjects(" + subjects + ") ,Objects(" + objects + ")");

			// B: Expansion by predicate
			// SameAS , Refere to , IFPs(inverse functional properties)...
			ArrayList<String> pridecats = new ArrayList<String>();
			pridecats.add("http://www.w3.org/2002/07/owl#sameAs");
			pridecats.add("http://www.w3.org/2002/07/owl#equivalentClass");
			pridecats.add("http://www.w3.org/2002/07/owl#FunctionalProperty");
			pridecats.add("http://www.w3.org/2002/07/owl#InverseFunctionalProperty");
			pridecats.add("http://xmlns.com/foaf/0.1/primaryTopic");
			pridecats.add("http://xmlns.com/foaf/0.1/isPrimaryTopicOf");
			pridecats.add("http://www.w3.org/2000/01/rdf-schema#seeAlso");
			pridecats.add("http://www.w3.org/2000/01/rdf-schema#isDefinedBy");

			HashSet<String> URIs = getURIsFromGraphForPredicats(Q.ExtendedGraph, pridecats);
			URIs.removeAll(donePridecateURIs);
			Q.ExtendedGraph.add(SparqlManager.getDocModel(URIs));
			donePridecateURIs.addAll(URIs);

			// statistics after i expansion
			objects = Q.ExtendedGraph.listObjects().toList().size();
			trips = Q.ExtendedGraph.listStatements().toList().size();
			subjects = Q.ExtendedGraph.listSubjects().toList().size();
			System.out.println("Statistics after (" + (i + 1) + ") predicate expansion: Triples (" + trips + "),Entities(" + (objects + subjects) + ") ,Subjects(" + subjects + ") ,Objects(" + objects + ")");

			// Repeting A&B
		}

		return Q;
	}

	public HashSet<String> getURIsFromGraphForPredicats(Model m, ArrayList<String> Pridecats) {
		HashSet<String> URIs = new HashSet<String>();

		for (String PURI : Pridecats) {
			Property p = m.createProperty(PURI);
			NodeIterator OL = m.listObjectsOfProperty(p);
			while (OL.hasNext()) {
				RDFNode o = OL.next();
				if (o.isURIResource()) {
					URIs.add(o.asResource().getURI());
					// System.out.println("P: " + PURI + "  URI: " +
					// o.asResource().getURI());
				}
			}
		}

		return URIs;
	}

	public HashSet<String> getObjectsURIsFromGraph(Model m) {
		HashSet<String> URIs = new HashSet<String>();
		NodeIterator OL = m.listObjects();
		while (OL.hasNext()) {
			RDFNode o = OL.next();
			if (o.isURIResource())
				URIs.add(o.asResource().getURI());
		}

		return URIs;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
