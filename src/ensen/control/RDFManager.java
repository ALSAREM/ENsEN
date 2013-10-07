package ensen.control;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.deri.any23.Any23;
import org.deri.any23.extractor.ExtractionException;
import org.deri.any23.http.HTTPClient;
import org.deri.any23.source.DocumentSource;
import org.deri.any23.source.HTTPDocumentSource;
import org.deri.any23.writer.RDFXMLWriter;
import org.deri.any23.writer.TripleHandler;
import org.deri.any23.writer.TripleHandlerException;

import com.hp.hpl.jena.graph.GraphExtract;
import com.hp.hpl.jena.graph.TripleBoundary;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.NodeIterator;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Selector;
import com.hp.hpl.jena.rdf.model.SimpleSelector;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.util.FileManager;

import ensen.entities.EnsenDBpediaResource;

public class RDFManager {
	public RDFManager() {
		// TODO Auto-generated constructor stub
	}

	public static Model createRDFModel() {
		return ModelFactory.createDefaultModel();
	}

	public static Model createRDFTriplet(Model m, String s, String p, String o, boolean objectLitteral) {
		// create the resource
		Resource R = m.createResource(s);

		// create the property
		Property P = m.createProperty(p);

		if (objectLitteral) {
			// create the Literal Object
			Literal O = m.createLiteral(o);
			// add the property
			R.addProperty(P, O);

		} else {
			// create the Resource Object
			Resource O = m.createResource(s);
			// add the property
			R.addProperty(P, O);
		}

		return m;
	}

	public static Model getSubgraph(Model G, String uri) {
		Model m = createRDFModel();
		GraphExtract GE = new GraphExtract(TripleBoundary.stopNowhere);
		m = ModelFactory.createModelForGraph(GE.extract(G.createResource(uri).asNode(), G.getGraph()));
		return m;
	}

	/*
	 * return list contains: 0 the model, 1 maximum sim, 2 avarage sim
	 */
	public static ArrayList<Object> generateModelFromDocument(String coreURI, List<EnsenDBpediaResource> triplets) {
		ArrayList<Object> returnList = new ArrayList<Object>();
		ArrayList<String> Resources = new ArrayList<String>();
		double maxSim = 0.0;
		double avgSim = 0.0;
		Model m = createRDFModel();
		m.getNsPrefixMap().put("ns", "http://dbpedia.org/namespace");
		// create the resource
		Resource R = m.createResource(coreURI);
		if (triplets != null) {
			for (int i = 0; i < triplets.size(); i++) {
				EnsenDBpediaResource DBR = triplets.get(i);
				if (DBR.similarityScore > maxSim)
					maxSim = DBR.similarityScore;
				avgSim += DBR.similarityScore;
				// create the Resource Object
				Resource O = m.createResource(DBR.getFullUri());
				// create the property
				Property P = m.createProperty("http://ensen.org/data#has-a");
				// add the property
				R.addProperty(P, O);
				Resources.add(DBR.getFullUri());
			}
			if (triplets.size() > 0)
				avgSim = avgSim / triplets.size();
			//add relations between resources
			//m.add(SparqlManager.addRelationBetweenResources(Resources));
		}
		returnList.add(m);
		returnList.add(maxSim);
		returnList.add(avgSim);
		return returnList;
	}

	public static void createRDFfile(String name, Model graph) {
		try {
			FileOutputStream fout = new FileOutputStream(PropertiesManager.getProperty("rootPath") + "\\RDF\\" + name + ".rdf");
			if (graph != null)
				graph.write(fout);

		} catch (Exception e) {
			System.out.println("Exception caught" + e.getMessage());
		}

	}

	public static Model extendSameAs(Model in, String uri) {
		Model res = createRDFModel();
		res.add(in);
		try {
			NodeIterator sameAs = in.listObjectsOfProperty(in.createResource(uri), in.createProperty("http://www.w3.org/2002/07/owl#sameAs"));
			while (sameAs.hasNext()) {
				res.add(SparqlManager.getResourceRDF(sameAs.next().asResource().getURI()));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		try {
			NodeIterator redir = in.listObjectsOfProperty(in.createResource(uri), in.createProperty("http://dbpedia.org/ontology/wikiPageRedirects"));
			while (redir.hasNext()) {
				res.add(SparqlManager.getResourceRDF(redir.next().asResource().getURI()));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return res;
	}

	public static Model readFile(String path) {
		Model model = RDFManager.createRDFModel();
		try {
			InputStream in = FileManager.get().open(path);
			if (in == null) {
				System.out.println("Error reading: " + path);
			} else {
				model.read(in, null);
				in.close();
			}
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}

		return model;
	}

	public static Model getObtimalGraph(Model in, String lang) {
		Model Tempmodel = RDFManager.createRDFModel();
		StmtIterator list = in.listStatements();
		while (list.hasNext()) {
			Statement st = list.next();
			RDFNode O = st.getObject();
			Resource S = st.getSubject();

			if (!O.isLiteral()) {
				String o = O.asResource().getURI() + " ";
				String s = S.asResource().getURI() + " ";
				if (s.contains(".dbpedia.org/"))
					if (s.contains(lang + ".dbpedia.org/")) {
						if (o.contains(".dbpedia.org/"))
							if (o.contains(lang + ".dbpedia.org/")) {
								Tempmodel.add(st);
							} else
								;// don't add
						else
							Tempmodel.add(st);
					} else
						;// don't add
				else {
					if (o.contains(".dbpedia.org/"))
						if (o.contains(lang + ".dbpedia.org/")) {
							Tempmodel.add(st);
						} else
							;// don't add
					else if ((o + s).trim() != "")
						Tempmodel.add(st);
				}

			}
		}
		return Tempmodel;
	}

	public static Model removeLiteral(Model in) {
		Model Tempmodel = RDFManager.createRDFModel();
		StmtIterator list = in.listStatements();
		while (list.hasNext()) {
			Statement st = list.next();
			RDFNode O = st.getObject();
			if (!O.isLiteral()) {
				Tempmodel.add(st);
			}
		}
		return Tempmodel;
	}

	public static Model removeSubjectWithAllStatements(Model in, String url) {
		Selector selector = new SimpleSelector(in.getResource(url), null, (RDFNode) null);//as subject
		StmtIterator stms = in.listStatements(selector);
		in.remove(stms.toList());
		return in;
	}

	public static Model removeResourceWithAllStatements(Model in, String url) {
		Selector selector = new SimpleSelector(in.getResource(url), null, (RDFNode) null);//as subject
		StmtIterator stms = in.listStatements(selector);
		in.remove(stms.toList());
		Selector selector1 = new SimpleSelector(null, null, in.getResource(url));//as object
		StmtIterator stms1 = in.listStatements(selector1);
		in.remove(stms1.toList());

		return in;
	}

	public static Model getAnnotationSemantic(String url) {
		Model m = RDFManager.createRDFModel();
		try {
			Any23 runner = new Any23();
			runner.setHTTPUserAgent("test-user-agent");

			HTTPClient httpClient;
			TripleHandler handler = null;
			try {
				httpClient = runner.getHTTPClient();
				DocumentSource source = new HTTPDocumentSource(httpClient, url);

				ByteArrayOutputStream out = new ByteArrayOutputStream();
				handler = new RDFXMLWriter(out);
				try {
					runner.extract(source, handler);
				} catch (ExtractionException e) {
					System.out.println("AnnotationSemantic Extraction Exception" + e.getMessage());
				}

				String doc = out.toString("UTF-8");
				m.read(new ByteArrayInputStream(doc.getBytes("UTF-8")), "");

			} catch (Exception e) {
				System.out.println("AnnotationSemantic Exception" + e.getMessage());
			} finally {
				if (handler != null)
					try {
						handler.close();
					} catch (TripleHandlerException e) {
						System.out.println("handler.close Exception" + e.getMessage());
					}
			}
		} catch (Exception e) {
			System.out.println("getAnnotationSemantic Exception" + e.getMessage());
		}
		//m.write(System.out);
		return m;
	}

	public static double getGraphConnectivity(Model G) {
		ResIterator su = G.listSubjects();
		StmtIterator st = G.listStatements();
		int subjects = su.toList().size();
		int statments = st.toList().size();
		//System.out.println(subjects);
		//System.out.println(statments);
		double d = statments * 1.0 / (subjects * 1.0 * (subjects * 1.0 - 1));
		// System.out.println(" ff" + d);
		// max => statments= subjects*(subjects-1) ==> Max=1
		return d;

	}

	public static Model projection(Model A, Model B) {
		/*
		 * String queryString =
		 * " select (count(distinct *) as ?c) where {?s ?p ?o. filter( true ";
		 * ResultSet res = SparqlManager.sparqlQueryOverJenaModel(queryString,
		 * A);
		 * 
		 * while (res.hasNext()) { QuerySolution QS = res.next(); score =
		 * QS.get("c").asLiteral().getDouble(); break; }
		 * 
		 * resA=A.listSubjects();
		 */
		return A.intersection(B);
	}

	/**
	 * Intersect two graph RDF A,B intersect just URIResources
	 * 
	 * @param A
	 * @param B
	 * @return list of URI that exist in both graphs
	 */
	public static ArrayList<String> intersection(Model A, Model B) {

		// A
		ArrayList<String> a = new ArrayList<String>();
		ArrayList<String> res = new ArrayList<String>();

		ResIterator resS = A.listSubjects();
		while (resS.hasNext()) {
			Resource resource = resS.next();
			a.add(resource.getURI());
		}
		NodeIterator resO = A.listObjects();
		while (resO.hasNext()) {
			RDFNode resource = resO.next();
			if (resource.isURIResource())
				a.add(resource.asResource().getURI());
		}

		// B
		ArrayList<String> b = new ArrayList<String>();
		resS = B.listSubjects();
		while (resS.hasNext()) {
			Resource resource = resS.next();
			if (a.contains(resource.getURI()))
				res.add(resource.getURI());
		}

		resO = B.listObjects();
		while (resO.hasNext()) {
			RDFNode resource = resO.next();
			if (resource.isURIResource())
				if (a.contains(resource.asResource().getURI()))
					res.add(resource.asResource().getURI());

		}

		HashSet<String> hs = new HashSet<String>();
		hs.addAll(res);
		res.clear();
		res.addAll(hs);
		return res;
	}

	public static ArrayList<Statement> getConnected(Statement st, Model in, ArrayList<Statement> visited) {
		ArrayList<Statement> res = new ArrayList<Statement>();
		try {
			if ((visited == null) || !visited.contains(st)) {
				Resource s = st.getSubject();
				RDFNode o = st.getObject();
				ArrayList<Statement> Statements = new ArrayList<Statement>();
				// get statments connected to the subject
				Selector selector = new SimpleSelector(s, null, (RDFNode) null);
				Statements.addAll(in.listStatements(selector).toList());
				selector = new SimpleSelector(null, null, s);
				Statements.addAll(in.listStatements(selector).toList());

				// get statments connected to the subject
				if (o.isResource()) {
					selector = new SimpleSelector(o.asResource(), null, (RDFNode) null);
					Statements.addAll(in.listStatements(selector).toList());
					selector = new SimpleSelector(null, null, o.asResource());
					Statements.addAll(in.listStatements(selector).toList());
				}

				// add them to visited list (eviter les loops)
				if (visited == null)
					visited = new ArrayList<Statement>();
				visited.addAll(Statements);
				res.addAll(Statements);
				for (Statement statement : Statements) {
					res.addAll(getConnected(statement, in, visited));
				}
			}
			/*
			 * if (st.getSubject().isURIResource() &&
			 * st.getObject().isURIResource() &&
			 * (st.getObject().asResource().getURI().contains("http://"))) {
			 * String Q = "select * where {?s ?p ?o. filter(?s=<" +
			 * st.getSubject().getURI() + "> || ?s=<" +
			 * st.getObject().asResource().getURI() + ">|| ?o=<" +
			 * st.getSubject().getURI() + ">|| ?o=<" +
			 * st.getObject().asResource().getURI() + ">) }";
			 * //System.out.println(Q);
			 * 
			 * ResultSet res1 = SparqlManager.sparqlQueryOverJenaModel(Q, in);
			 * while (res1.hasNext()) { QuerySolution QS = res1.next();
			 * Statement stt =
			 * in.createStatement(in.createResource(QS.get("s").toString()),
			 * in.createProperty(QS.get("p").toString()),
			 * in.createResource(QS.get("o").toString())); Statements.add(stt);
			 * }
			 * 
			 * in.remove(Statements); ArrayList<Statement> temp = new
			 * ArrayList<Statement>(); for (int i = 0; i < Statements.size();
			 * i++) { temp.addAll(getConnected(Statements.get(i), in)); }
			 * 
			 * Statements.addAll(temp); }
			 */
		} catch (Exception e) {
			e.printStackTrace();
		}
		return res;
	}

	public static ArrayList<Model> splitModel(Model in) {
		ArrayList<Model> res = new ArrayList<Model>();
		// System.out.println(in.size());
		if (in.size() == 0)
			return res;
		else {
			// ArrayList<Statement> StatementsForDelete = new
			// ArrayList<Statement>();
			StmtIterator sts = in.listStatements();
			Model temp = RDFManager.createRDFModel();
			while (sts.hasNext()) {
				Statement st = sts.next();
				// System.out.println("Statments:" + st);
				ArrayList<Statement> Statements = getConnected(st, in, null);
				// System.out.println("Connected graph Size:" +
				// Statements.size());
				temp.add(Statements);
				break;
			}
			if (temp.size() > 0) {
				in.remove(temp);
				res.add(temp);
				res.addAll(splitModel(in));
			}

		}

		return res;

	}

	public static String Model2D3Json(Model in) {
		String res = "";
		String nodes = "";
		String links = "";
		if (in != null) {
			StmtIterator sts = in.listStatements();
			Map<String, Integer> Nodes = new HashMap<String, Integer>();
			ResIterator ss = in.listSubjects();
			int nodsCounter = 0;
			while (ss.hasNext()) {
				Resource resource = (Resource) ss.next();
				Nodes.put(resource.getURI(), nodsCounter);
				nodes += "{\"name\":\"" + resource.getURI() + "\",\"group\":" + (nodsCounter++) + ",\"cluster\":" + 1 + "},";

			}

			NodeIterator oo = in.listObjects();
			while (oo.hasNext()) {
				RDFNode rdfNode = (RDFNode) oo.next();
				System.err.println(rdfNode);
				System.err.println(rdfNode.getClass());
				if (rdfNode.isResource()) {
					if (Nodes.get(rdfNode.asResource().getURI()) == null) {
						Nodes.put(rdfNode.asResource().getURI(), nodsCounter);
						nodes += "{\"name\":\"" + rdfNode.asResource().getURI() + "\",\"group\":" + (nodsCounter++) + "},";
					}
				} else {
					if (rdfNode.isLiteral()) {
						//if literal and starting by http://

						try {
							String value = rdfNode.asLiteral().getValue().toString();
							String txt = value.substring(0, Math.min(7, value.length()));
							if (txt.contains("http://")) {
								Nodes.put(value, nodsCounter);
								nodes += "{\"name\":\"" + value + "\",\"group\":" + (nodsCounter++) + "},";
							}
						} catch (Exception e) {
						}
					}

				}
			}

			int from = -1;
			int to = -1;

			while (sts.hasNext()) {
				from = -1;
				to = -1;
				Statement statement = (Statement) sts.next();
				System.err.println(statement);
				Resource S = statement.getSubject();
				RDFNode O = statement.getObject();

				if (Nodes.get(S.getURI()) != null)
					from = Nodes.get(S.getURI());

				if (O.isResource()) {
					if (Nodes.get(O.asResource().getURI()) != null)
						to = Nodes.get(O.asResource().getURI());
				} else {
					if (O.isLiteral()) {
						//if literal and starting by http://

						try {
							String value = O.asLiteral().getValue().toString();
							String txt = value.substring(0, Math.min(7, value.length()));
							if (txt.contains("http://")) {
								to = Nodes.get(value);
							}
						} catch (Exception e) {
						}
					}
				}
				if (from != -1 && to != -1)
					links += "{\"source\":" + from + ",\"target\":" + to + ",\"value\":\"" + statement.getPredicate().getLocalName() + "\"},";
			}
			if (nodes.length() == 0)
				nodes += " ";
			if (links.length() == 0)
				links += " ";
			res = "{ \"nodes\" :[ " + nodes.substring(0, nodes.length() - 1).replace("'", " ") + "], \"links\" :[" + links.substring(0, links.length() - 1).replace("'", " ") + "]}";
			res = res.replace("\n", " ").replace("\"\"", "\"empty\"");
			System.out.println("res: " + res);
		}
		return res;
	}

	public static String Model2D3JsonWithClusters(TreeMap<String, Model> subGraphs) {
		String res = "";
		String nodes = "";
		String links = "";
		ArrayList<String> nodeNames = new ArrayList<String>();
		ArrayList<String> nodeClusters = new ArrayList<String>();
		if (subGraphs != null) {
			int clusterCounter = 0;
			for (Map.Entry<String, Model> entry : subGraphs.entrySet()) {
				clusterCounter++;
				Model in = entry.getValue();
				StmtIterator sts = in.listStatements();
				Map<String, Integer> Nodes = new HashMap<String, Integer>();
				ResIterator ss = in.listSubjects();
				int nodsCounter = 0;
				while (ss.hasNext()) {
					Resource resource = (Resource) ss.next();
					Nodes.put(resource.getURI(), nodsCounter);
					if (nodeNames.contains(resource.getURI()))
						nodeClusters.set(nodeNames.indexOf(resource.getURI()), nodeClusters.get(nodeNames.indexOf(resource.getURI())) + "#" + clusterCounter);
					else {
						nodeNames.add(resource.getURI());
						nodeClusters.add(clusterCounter + "");
					}
					nodes += "{\"name\":\"" + resource.getURI() + "\",\"group\":" + (nodsCounter++) + ",\"cluster\":" + (clusterCounter) + "},";
				}

				NodeIterator oo = in.listObjects();
				while (oo.hasNext()) {
					RDFNode rdfNode = (RDFNode) oo.next();
					System.err.println(rdfNode);
					System.err.println(rdfNode.getClass());
					if (rdfNode.isResource()) {
						if (Nodes.get(rdfNode.asResource().getURI()) == null) {
							Nodes.put(rdfNode.asResource().getURI(), nodsCounter);
							if (nodeNames.contains(rdfNode.asResource().getURI()))
								nodeClusters.set(nodeNames.indexOf(rdfNode.asResource().getURI()), nodeClusters.get(nodeNames.indexOf(rdfNode.asResource().getURI())) + "#" + clusterCounter);
							else {
								nodeNames.add(rdfNode.asResource().getURI());
								nodeClusters.add(clusterCounter + "");
							}
							nodes += "{\"name\":\"" + rdfNode.asResource().getURI() + "\",\"group\":" + (nodsCounter++) + ",\"cluster\":" + (clusterCounter) + "},";
						}
					} else {
						if (rdfNode.isLiteral()) {
							//if literal and starting by http://

							try {
								String value = rdfNode.asLiteral().getValue().toString();
								String txt = value.substring(0, Math.min(7, value.length()));
								if (txt.contains("http://")) {
									Nodes.put(value, nodsCounter);
									if (nodeNames.contains(value))
										nodeClusters.set(nodeNames.indexOf(value), nodeClusters.get(nodeNames.indexOf(value)) + "#" + clusterCounter);
									else {
										nodeNames.add(value);
										nodeClusters.add(clusterCounter + "");
									}
									nodes += "{\"name\":\"" + value + "\",\"group\":" + (nodsCounter++) + ",\"cluster\":" + (clusterCounter) + "},";
								}
							} catch (Exception e) {
							}
						}

					}
				}

				int from = -1;
				int to = -1;

				while (sts.hasNext()) {
					from = -1;
					to = -1;
					Statement statement = (Statement) sts.next();
					System.err.println(statement);
					Resource S = statement.getSubject();
					RDFNode O = statement.getObject();

					if (Nodes.get(S.getURI()) != null)
						from = Nodes.get(S.getURI());

					if (O.isResource()) {
						if (Nodes.get(O.asResource().getURI()) != null)
							to = Nodes.get(O.asResource().getURI());
					} else {
						if (O.isLiteral()) {
							//if literal and starting by http://

							try {
								String value = O.asLiteral().getValue().toString();
								String txt = value.substring(0, Math.min(7, value.length()));
								if (txt.contains("http://")) {
									to = Nodes.get(value);
								}
							} catch (Exception e) {
							}
						}
					}
					if (from != -1 && to != -1)
						links += "{\"source\":" + from + ",\"target\":" + to + ",\"value\":\"" + statement.getPredicate().getLocalName() + "\",\"cluster\":" + (clusterCounter) + "},";
				}
			}
			nodes = "";
			int nodsCounter = 0;
			for (String s : nodeNames) {
				nodes += "{\"name\":\"" + s + "\",\"group\":" + (nodsCounter++) + ",\"cluster\":\"#" + (nodeClusters.get(nodeNames.indexOf(s))) + "#\"},";
			}

			if (nodes.length() == 0)
				nodes += " ";
			if (links.length() == 0)
				links += " ";
			res = "{ \"nodes\" :[ " + nodes.substring(0, nodes.length() - 1).replace("'", " ") + "], \"links\" :[" + links.substring(0, links.length() - 1).replace("'", " ") + "]}";
			res = res.replace("\n", " ").replace("\"\"", "\"empty\"");
			System.out.println("res: " + res);

		}
		return res;
	}

	public static Model removeHasApredicates(Model in) {
		Model out = RDFManager.createRDFModel();
		StmtIterator sts = in.listStatements();
		while (sts.hasNext()) {
			Statement st = sts.next();
			if (!st.getPredicate().getURI().contains("http://ensen.org/data#has-a"))
				out.add(st);

		}
		return out;
	}

}
