package ensen.control;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.NodeIterator;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.sparql.engine.http.QueryEngineHTTP;

public class SparqlManager {
	public SparqlManager() {
		// TODO Auto-generated constructor stub
	}

	public static String convertStreamToString(InputStream is) throws IOException {
		//
		// To convert the InputStream to String we use the
		// Reader.read(char[] buffer) method. We iterate until the
		// Reader return -1 which means there's no more data to
		// read. We use the StringWriter class to produce the string.
		//
		if (is != null) {
			Writer writer = new StringWriter();

			char[] buffer = new char[1024];
			try {
				Reader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
				int n;
				while ((n = reader.read(buffer)) != -1) {
					writer.write(buffer, 0, n);
				}
			} finally {
				is.close();
			}
			return writer.toString();
		} else {
			return "";
		}
	}

	public static Model getResourceRDF(String uri) {

		//System.out.println("Getting Res: " + uri);
		Model model = ModelFactory.createDefaultModel();
		ExecutorService executor = Executors.newSingleThreadExecutor();
		Future<String> future = executor.submit(new Task());

		try {
			System.out.println("Starte loading " + uri);
			System.out.println(future.get(10, TimeUnit.SECONDS));
			model.read(uri);
			System.out.println("Finished!");
		} catch (TimeoutException e) {
			System.out.println("Timeout :(");
			model = ModelFactory.createDefaultModel();
		} catch (InterruptedException e) {
			//e.printStackTrace();
			System.out.println("InterruptedException");
			model = ModelFactory.createDefaultModel();
		} catch (ExecutionException e) {
			//e.printStackTrace();
			System.out.println("ExecutionException");
			model = ModelFactory.createDefaultModel();
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("not RDF");
			model = ModelFactory.createDefaultModel();
		}

		executor.shutdownNow();

		return model;
	}

	public static Model getResourceModel(String uri) {
		// create an empty model
		Model model = ModelFactory.createDefaultModel();
		try {
			String queryString = "SELECT DISTINCT ?p ?o " + "WHERE {" + " <" + uri + "> ?p ?o. }";
			// System.out.println(queryString);
			// now creating query object
			Query query = QueryFactory.create(queryString);
			// initializing queryExecution factory with remote service.
			// **this actually was the main problem I couldn't figure out.**
			QueryExecution qexec = QueryExecutionFactory.sparqlService(PropertiesManager.getProperty("DBpediaSparql"), query);

			// after it goes standard query execution and result processing
			// which can
			// be found in almost any Jena/SPARQL tutorial.
			try {
				ResultSet results = qexec.execSelect();

				for (; results.hasNext();) {
					QuerySolution QS = results.nextSolution();
					Resource R = model.createResource(uri);
					Property P = model.createProperty(QS.get("p").toString());
					R.addProperty(P, QS.get("o"));
					// System.out.println(QS.get("p").toString());
				}
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				qexec.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return model;
	}

	public static ResultSet querySparql(String queryString) {
		ResultSet results = null;
		//System.out.println(queryString);
		Query query = QueryFactory.create(queryString);
		QueryExecution qexec = QueryExecutionFactory.sparqlService(PropertiesManager.getProperty("DBpediaSparql"), query);

		try {
			results = qexec.execSelect();
		} catch (Exception e) {
			e.printStackTrace();
			System.err.println(e.getMessage());
		}
		return results;
	}

	public static ResultSet queryCount(String queryString) {
		ResultSet res = null;
		QueryEngineHTTP qe = new QueryEngineHTTP(PropertiesManager.getProperty("DBpediaSparql"), queryString);
		try {
			res = qe.execSelect();

		} catch (Exception e) {
			e.printStackTrace();
			System.err.println(e.getMessage());
		}
		return res;
	}

	public static Model addRelationBetweenResources(ArrayList<String> it) {
		Model model = ModelFactory.createDefaultModel();

		System.out.println("Get Relations Between Resources from Linked Data");
		// prepare Q
		String FILTERString = "FILTER((?##@@&& in (";
		int counter = 0;
		while (counter < it.size()) {
			String list = "";
			for (int i = 0; i < Math.min(counter + 50, it.size()); i++) {
				list += "<" + it.get(i) + "> ,";
				counter++;
			}
			if (it.size() > counter)
				FILTERString += list.substring(0, list.length() - 1) + ")) || (?##@@&& in (";
			else
				FILTERString += list;
		}

		if (counter > 0) {
			FILTERString = FILTERString.substring(0, FILTERString.length() - 1);
			FILTERString += "))).";
		}

		if (counter > 0) {
			String FILTERString2 = FILTERString;
			String queryString = "SELECT DISTINCT ?s ?p ?o " + "WHERE { ?s ?p ?o. " + FILTERString.replace("##@@&&", "s") + " " + FILTERString2.replace("##@@&&", "o") + " }";// FILTER(?s in (" + queryString + ")). FILTER (?o in (" + queryString + ")).} ";
			System.out.println(queryString);
			Query query = QueryFactory.create(queryString);
			QueryExecution qexec = QueryExecutionFactory.sparqlService(PropertiesManager.getProperty("DBpediaSparql"), query);
			try {
				ResultSet results = qexec.execSelect();
				for (; results.hasNext();) {
					QuerySolution QS = results.nextSolution();
					Resource R = model.createResource(QS.get("s").toString());
					Property P = model.createProperty(QS.get("p").toString());
					R.addProperty(P, QS.get("o"));
				}
			} catch (Exception e) {
				System.out.println("Error: Sparql execSelect:" + e.getMessage());
			} finally {
				qexec.close();
			}
		}
		return model;
	}

	public static Model getResourcesModel(ArrayList<String> it) {
		Model model = ModelFactory.createDefaultModel();

		System.out.println("Get resources from Linked Data for " + it.size());
		// prepare Q
		String queryString = "";
		for (String entry : it) {
			queryString += "<" + entry + "> ,";
		}

		if (queryString.length() > 0) {
			queryString = queryString.substring(0, queryString.length() - 1);

			queryString = "SELECT DISTINCT ?s ?p ?o " + "WHERE { ?s ?p ?o. FILTER(?s in (" + queryString + ")). FILTER(isURI(?o)||( (!isURI(?o))  &&   (LANG(?o) = \"\" || LANGMATCHES(LANG(?o), \"" + PropertiesManager.getProperty("lang") + "\")))). }";
			//System.out.println(queryString);
			// create an empty model

			// now creating query object
			Query query = QueryFactory.create(queryString);
			// initializing queryExecution factory with remote service.			
			QueryExecution qexec = QueryExecutionFactory.sparqlService(PropertiesManager.getProperty("DBpediaSparql"), query);

			// after it goes standard query execution and result processing which
			// can
			// be found in almost any Jena/SPARQL tutorial.
			try {
				ResultSet results = qexec.execSelect();

				for (; results.hasNext();) {
					QuerySolution QS = results.nextSolution();
					Resource R = model.createResource(QS.get("s").toString());
					Property P = model.createProperty(QS.get("p").toString());
					R.addProperty(P, QS.get("o"));
					// System.out.println(QS.get("s").toString() + " --> " +
					// QS.get("p").toString() + " --> " + QS.get("o").toString());
				}
			} catch (Exception e) {
				System.out.println("Error: Sparql execSelect:" + e.getMessage());
			} finally {
				qexec.close();
			}
		}
		return model;
	}

	public static Model getResTriples(String in) {
		String queryString = "SELECT DISTINCT ?s ?p ?o " + "WHERE { ?s ?p ?o. FILTER(?s in (<" + in + ">))} ";

		// create an empty model
		Model model = ModelFactory.createDefaultModel();

		// now creating query object
		Query query = QueryFactory.create(queryString);
		// initializing queryExecution factory with remote service.
		// **this actually was the main problem I couldn't figure out.**
		QueryExecution qexec = QueryExecutionFactory.sparqlService(PropertiesManager.getProperty("DBpediaSparql"), query);

		// after it goes standard query execution and result processing which
		// can
		// be found in almost any Jena/SPARQL tutorial.
		try {
			ResultSet results = qexec.execSelect();

			for (; results.hasNext();) {
				QuerySolution QS = results.nextSolution();
				Resource R = model.createResource(QS.get("s").toString());
				Property P = model.createProperty(QS.get("p").toString());
				R.addProperty(P, QS.get("o").toString());
				// System.out.println(QS.get("s").toString() + " --> " +
				// QS.get("p").toString() + " --> " + QS.get("o").toString());
			}
		} catch (Exception e) {
			System.out.println("Error: Sparql execSelect:" + e.getMessage());
		} finally {
			qexec.close();
		}

		return model;
	}

	public static Model getDocModel(NodeIterator it) {
		System.out.println("Get resources from Linked Data");
		// prepare Q
		String queryString = "";

		while (it.hasNext()) {
			RDFNode s = it.next();
			if (!s.isLiteral()) {
				// System.out.println("Get resource(" + s.asResource().getURI()
				// + ") from Linked Data");
				queryString += "<" + s.asResource().getURI() + ">";
				if (it.hasNext())
					queryString += " , ";
			}

		}

		queryString = "SELECT DISTINCT ?s ?p ?o " + "WHERE { ?s ?p ?o. FILTER(?s in (" + queryString + "))} ";
		// System.out.println(queryString);
		// create an empty model
		Model model = ModelFactory.createDefaultModel();

		// now creating query object
		Query query = QueryFactory.create(queryString);
		// initializing queryExecution factory with remote service.
		// **this actually was the main problem I couldn't figure out.**
		QueryExecution qexec = QueryExecutionFactory.sparqlService(PropertiesManager.getProperty("DBpediaSparql"), query);

		// after it goes standard query execution and result processing which
		// can
		// be found in almost any Jena/SPARQL tutorial.
		try {
			ResultSet results = qexec.execSelect();

			for (; results.hasNext();) {
				QuerySolution QS = results.nextSolution();
				Resource R = model.createResource(QS.get("s").toString());
				Property P = model.createProperty(QS.get("p").toString());
				R.addProperty(P, QS.get("o").toString());
				// System.out.println(QS.get("s").toString() + " --> " +
				// QS.get("p").toString() + " --> " + QS.get("o").toString());
			}
		} catch (Exception e) {
			System.out.println("Error: Sparql execSelect:" + e.getMessage());
		} finally {
			qexec.close();
		}

		return model;
	}

	public static Model getDocModel(HashSet<String> it) {
		// System.out.println("Get resources from Linked Data");
		// prepare Q
		String queryString = "";
		for (String PURI : it) {
			try {
				new URL(PURI);
				queryString += "<" + PURI + "> ,";
			} catch (MalformedURLException e) {
				//not valid URL
			}
		}
		Model model = ModelFactory.createDefaultModel();
		if (it.size() > 0) {
			queryString = queryString.substring(0, queryString.length() - 1);

			queryString = "SELECT DISTINCT ?s ?p ?o " + "WHERE { ?s ?p ?o. FILTER(?s in (" + queryString + "))} ";
			// System.out.println(queryString);

			// now creating query object
			Query query = QueryFactory.create(queryString);
			// initializing queryExecution factory with remote service.
			// **this actually was the main problem I couldn't figure out.**
			QueryExecution qexec = QueryExecutionFactory.sparqlService(PropertiesManager.getProperty("DBpediaSparql"), query);

			// after it goes standard query execution and result processing
			// which can
			// be found in almost any Jena/SPARQL tutorial.
			try {
				ResultSet results = qexec.execSelect();

				for (; results.hasNext();) {
					QuerySolution QS = results.nextSolution();
					Resource R = model.createResource(QS.get("s").toString());
					Property P = model.createProperty(QS.get("p").toString());
					R.addProperty(P, QS.get("o"));
					// System.out.println(QS.get("s").toString() + " --> " +
					// QS.get("p").toString() + " --> " +
					// QS.get("o").toString());
				}
			} catch (Exception e) {
				System.out.println("Error: Sparql execSelect:" + e.getMessage());
			} finally {
				qexec.close();
			}
		}
		return model;
	}

	public static Model searchSparql(String S, String P, String O) {
		// create an empty model
		Model model = ModelFactory.createDefaultModel();
		String select = "SELECT DISTINCT ";
		String where = " WHERE { ";
		if (S == "") {
			select += " ?s ";
			where += " ?s ";
		} else {
			select += " ";
			where += " <" + S + "> ";
		}
		if (P == "") {
			select += " ?p ";
			where += " ?p ";
		} else {
			select += " ";
			where += " <" + P + "> ";
		}
		if (O == "") {
			select += " ?o ";
			where += " ?o ";
		} else {
			select += " ";
			where += " <" + O + "> ";
		}
		String queryString = select + " " + where + ". }";
		//System.out.println(queryString);
		// now creating query object
		Query query = QueryFactory.create(queryString);
		// initializing queryExecution factory with remote service.
		QueryExecution qexec = QueryExecutionFactory.sparqlService(PropertiesManager.getProperty("DBpediaSparql"), query);

		try {
			ResultSet results = qexec.execSelect();

			for (; results.hasNext();) {
				QuerySolution QS = results.nextSolution();
				Resource SS = null;
				Property PP = null;

				if (S == "") {
					SS = model.createResource(QS.get("s").toString());

				} else {
					SS = model.createResource(S);
				}

				if (P == "") {
					PP = model.createProperty(QS.get("p").toString());

				} else {
					PP = model.createProperty(P);
				}

				if (O == "") {
					SS.addProperty(PP, QS.get("o").toString());

				} else {
					SS.addProperty(PP, O);
				}

			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			qexec.close();
		}
		return model;
	}

	public static Model getByPredicat(String coreURI, String P, ArrayList<String> entities) {
		Model model = ModelFactory.createDefaultModel();
		String uriSet = "(";
		if (entities != null && entities.size() > 0) {
			for (String uri : entities) {
				if (uri.contains("http"))
					uriSet += "<" + uri + ">,";
			}
			uriSet = uriSet.substring(0, uriSet.length() - 1) + ")";

			/*
			 * with language String select =
			 * "SELECT DISTINCT ?s ?o where { { ?s <" + P +
			 * "> ?o. FILTER ( ?s in " + uriSet +
			 * "). ?o <http://www.w3.org/2000/01/rdf-schema#label> ?label.  FILTER(lang(?label) = 'en').}  UNION  { ?o <"
			 * + P + "> ?s. FILTER ( ?o in " + uriSet +
			 * "). ?s  <http://www.w3.org/2000/01/rdf-schema#label> ?label.  FILTER(lang(?label) = 'en').} }"
			 * ;
			 */
			String select = "SELECT DISTINCT ?s ?o where { { ?s <" + P + "> ?o. FILTER ( ?s in " + uriSet + ").}  UNION  { ?o <" + P + "> ?s. FILTER ( ?o in " + uriSet + ").} }";
			System.out.println("Sparql: " + select);
			Query query = null;
			try {
				query = QueryFactory.create(select);
			} catch (Exception e) {
				e.printStackTrace();
			}

			if (query != null) {
				// initializing queryExecution factory with remote service.
				QueryExecution qexec = QueryExecutionFactory.sparqlService(PropertiesManager.getProperty("DBpediaSparql"), query);

				try {

					ResultSet results = qexec.execSelect();
					Resource R = model.createResource(coreURI);
					for (; results.hasNext();) {
						QuerySolution QS = results.nextSolution();

						Resource S = model.createResource(QS.get("s").toString());
						Resource O = model.createResource(QS.get("o").toString());
						// create the property
						Property Pp = model.createProperty("http://ensen.org/data#has-a");
						Property Ppp = model.createProperty(P);
						// add the property
						R.addProperty(Pp, S);
						S.addProperty(Ppp, O);

					}
				} finally {
					qexec.close();
				}
			}
		}
		return model;

	}

	public static Model getByPredicats(String coreURI, String P, ArrayList<String> entities) {
		Model model = ModelFactory.createDefaultModel();
		String uriSet = "(";
		if (entities != null && entities.size() > 0) {
			for (String uri : entities) {
				if (uri.contains("http"))
					uriSet += "<" + uri + ">,";
			}
			uriSet = uriSet.substring(0, uriSet.length() - 1) + ")";

			String select = "SELECT DISTINCT ?s ?o where { { ?s ?p ?o. FILTER ( (?s in " + uriSet + ") && (?p in (" + P + "))).}  UNION  { ?o ?p ?s. FILTER ( (?o in " + uriSet + ") && (?p in (" + P + "))).} }";
			System.out.println("Sparql: " + select);
			Query query = null;
			try {
				query = QueryFactory.create(select);
			} catch (Exception e) {
				e.printStackTrace();
			}

			if (query != null) {
				// initializing queryExecution factory with remote service.
				QueryExecution qexec = QueryExecutionFactory.sparqlService(PropertiesManager.getProperty("DBpediaSparql"), query);

				try {

					ResultSet results = qexec.execSelect();
					Resource R = model.createResource(coreURI);
					for (; results.hasNext();) {
						QuerySolution QS = results.nextSolution();

						Resource S = model.createResource(QS.get("s").toString());
						Resource O = model.createResource(QS.get("o").toString());
						// create the property
						Property Pp = model.createProperty("http://ensen.org/data#has-a");
						Property Ppp = model.createProperty(P);
						// add the property
						R.addProperty(Pp, S);
						S.addProperty(Ppp, O);

					}
				} finally {
					qexec.close();
				}
			}
		}
		return model;
	}

	public static ResultSet sparqlQueryOverJenaModel(String queryString, Model m) {

		Query query = QueryFactory.create(queryString);

		QueryExecution qexec = QueryExecutionFactory.create(query, m);

		/*
		 * //temporaire with DBpeia QueryExecution qexec =
		 * QueryExecutionFactory.
		 * sparqlService(PropertiesManager.getProperty("DBpediaSparql"), query);
		 */
		return qexec.execSelect();
	}
}

class Task implements Callable<String> {
	@Override
	public String call() throws Exception {
		Thread.sleep(500);
		return "Ready!";
	}
}