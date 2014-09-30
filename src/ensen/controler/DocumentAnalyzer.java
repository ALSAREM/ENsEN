package ensen.controler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import Jama.Matrix;

import com.google.api.services.customsearch.model.Result;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.NodeIterator;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;

import ensen.entities.Cluster;
import ensen.entities.Concept;
import ensen.entities.Document;
import ensen.entities.EnsenDBpediaResource;
import ensen.entities.Group;
import ensen.ml.MLManager;
import ensen.ml.MlControler;
import ensen.util.Printer;
import ensen.util.PropertiesManager;
import ensen.util.Texthandler;
import ensen.util.comparators.ConceptComparator;
import ensen.util.comparators.ValueComparator;

public class DocumentAnalyzer {
	private static final int MaxSmallTextInSentence = Integer.parseInt(PropertiesManager.getProperty("MaxSmallTextInSentence"));
	public static final int MaxNumOfConcepts = Integer.parseInt(PropertiesManager.getProperty("SnippetMaxNumOfConcepts"));

	public Document Doci;

	Logger log = Logger.getLogger(this.getClass());
	String allText;
	String usedText;
	ArrayList<String> terms;
	double[][] M;//term fragment Matrix
	String[] splitedText;

	Matrix R;// term Resource Matrix
	private ArrayList<String> allResource;
	private ArrayList<String> abstracts;
	private ArrayList<String> showenAbstracts;

	private int commonWordsLimit = 5;// % of related resources
	private String resourcesString;

	private Map<String, Double> resourcesFoundedByClib;

	private ArrayList<String> toBeExtendedResources;

	public ArrayList<Group> groups;

	private double groupItemLimit = 0.1;
	public ArrayList<String> links;
	public ArrayList<String> linksLabels;
	int linksLimit = 155;
	int wikisLimit = 155;
	public ArrayList<String> wikis;
	public ArrayList<String> wikisLabels;
	public ArrayList<String> wikisPhotos;
	private ArrayList<Cluster> selectedClusters;
	public String mainPh = "";
	public boolean nosnippet = false;
	public String seSnippet = "";
	private List<EnsenDBpediaResource> SnippetResources;
	public String indexFile = "";//temp to index Wikipedia
	public int retryFactor = 1;
	public int toBeAnnotatedTextLen;
	public String selectedConfiance = "";
	public CControler ccontroler;
	public int tryNumber = 1;

	public static void main(String[] args) {
		/*String qs = "eliza turing test;dzogchen mahamudra;coltrane olatunji concert;systema martial art;bourbaki category theory;robin milner sml ocaml;spinoza substance;freud nietzsche;nietzsche sister;heidegger being and time;free will consciousness;david bohm thought;david bohm implicate order;wolfram new kind of science;computational irreducibility;jhana theravada;mont sainte victoire cezanne;first film louis auguste lumiere;star wars kurosawa influence;matrix jean baudrillard;";
		//String qs = "Al Haramoun Mountain;Andalusian Music;French Revolution;Global Positioning System Technology;Karl Marx;Lyon;Markup Languages;Supernova Remnant;";
		String datasetStructure = "C:\\Users\\malsarem\\Google Drive\\LIRIS\\Confs\\CIKM\\Evaluation\\PP8-6_summery_3\\dataset\\";
		String datasetContent="http://liris-qir.insa-lyon.fr:8080/eval/dataset/";
		for (String qText : qs.split(";")) {
			ensen.entities.Query q = new ensen.entities.Query(qText);
			Printer.createFolder("dataset", qText);
			File dir = new File(datasetStructure+qText);
			File[] fList = dir.listFiles();
			int counter = 0;
			for (File file : fList) {
				if (file.isFile()) {
					if (file.getName().endsWith(".html")) {
						DocumentAnalyzer test = new DocumentAnalyzer();
						test.run((datasetContent + qText + "/" + file.getName()).replace(" ", "%20"), q, file.getName().replace(".html", ""));
					}
				}
			}


		}*/
		DocumentAnalyzer test = new DocumentAnalyzer();

		//test.run("http://global.britannica.com/EBchecked/topic/367265/Karl-Marx");
		//test.run("http://en.wikipedia.org/wiki/French_Revolution", q);
		//test.run("http://www.people.com/people/kate_middleton/");

		test.run("http://en.wikipedia.org/wiki/Systema", new ensen.entities.Query("systema martial art"), "Systema _ Wikipedia_ the free encyclopedia", 0);
		//test.run("http://www.bbc.co.uk/newsround/16979186");

	}

	public void run(String url, ensen.entities.Query q, String Title, int id) {
		Printer.createFolder("dataset", q.Text);
		Doci = generateDoc(url, q);
		Doci.Rank = id;
		if (Doci.text.length() > 10) {
			Doci.q = q;
			Doci.content = new Result();
			Doci.content.setTitle(Title);
			Doci.internalFilename = Title.replaceAll("[^a-zA-Z0-9\\s]", "_");
			Doci.internalFilePath = "dataset/" + q.Text + "/" + Doci.internalFilename;
			run();

		} else
			nosnippet = true;
	}

	public void run(Result result, int id, ensen.entities.Query q) {
		long start = System.currentTimeMillis();
		Document D = new Document(result.getLink(), id, null);

		Doci = D;
		if (Doci.text.length() > 10) {
			Doci.content = result;
			Doci.q = q;
			Doci.internalFilename = Doci.content.getTitle().replaceAll("[^a-zA-Z0-9\\s]", "_");
			Doci.internalFilePath = "dataset/" + q.Text + "/" + Doci.internalFilename;
			run();
			long end = System.currentTimeMillis();
			System.out.println("Total time for " + Doci.content.getTitle() + " = " + (end - start) / 1000.0 + " s");
			/* paths to generate dataset*/

		} else
			nosnippet = true;

	}

	public void run() {
		int stepCounter = 0;
		long start = System.currentTimeMillis();
		Printer.registerTime("Run on " + Doci.content.getTitle());
		Printer.setLogFile(Doci.Rank + "_Start");

		//1.	Annotation (DBpedia spotlight) ==>  list of resources LS		
		stepCounter++;
		Printer.setLogFile(Doci.Rank + "_1-Annotation");
		Annotation();
		Printer.registerTime(stepCounter + "- Annotation of " + Doci.content.getTitle());

		//2.	Build first RDF graph G1 by adding relation between LS’s resource		a.	Using Sparql 	
		Printer.setLogFile(Doci.Rank + "_2-buildTheRDFGraph");
		buildTheRDFGraph();
		stepCounter++;
		Printer.registerTime(stepCounter + "-Build The RDF Graph of " + Doci.content.getTitle());
		allText = "";
		buildResourceTextData();

		//4 call c library to find best resources
		stepCounter++;
		Printer.registerTime(stepCounter + "-Build Term Resource Matrix " + Doci.content.getTitle());

		Matrix R = buildTermResourceMatrix();
		String CgeneratedMatrix = Printer.prepareCinput(Doci, allResource, terms, R);

		//Printer.printToFile(, CgeneratedMatrix);
		Doci.CgeneratedMatrix = CgeneratedMatrix;
		stepCounter++;
		Printer.registerTime(stepCounter + "-Prepare Data for c library " + Doci.content.getTitle());
		ccontroler = new CControler();
		String cOut = ccontroler.callCLib(CgeneratedMatrix);

		stepCounter++;
		Printer.registerTime(stepCounter + "-Finding best resources Using C LIB " + Doci.content.getTitle());
		resourcesFoundedByClib = ccontroler.readCoutput(cOut);
		Doci.resourcesFoundedByClib = resourcesFoundedByClib;
		toBeExtendedResources = getToBeExtendedResources(resourcesFoundedByClib);
		System.out.println(resourcesFoundedByClib.values());

		stepCounter++;
		Printer.registerTime(stepCounter + "-get ToBeExtended Resources of " + Doci.content.getTitle());

		//6.	Extends graph using ToBeExtended resource  ==> Big connected graph G3
		Printer.setLogFile(Doci.Rank + "_6-Extends graph");
		extendGraph();

		stepCounter++;
		Printer.registerTime(stepCounter + "-Extend Graph of " + Doci.content.getTitle());

		Doci.graphWithoutLiteral = RDFManager.removeLiteral(Doci.fullGraph);

		/*here we decide to stop or not
		 * we stop if the number of connected resources is less than minNofConnectedResources (see ensen.properties)
		 */
		System.out.println("Try " + tryNumber);

		//not really connected
		/*if (Doci.graphWithoutLiteral.size() - Doci.graphWithoutLiteral.listStatements(new SimpleSelector(Doci.graphWithoutLiteral.getResource(Doci.url), null, (RDFNode) null)).toList().size() < Integer.parseInt(PropertiesManager.getProperty("minNofConnectedResources"))) {
			if (tryNumber == 3) {
				if (Doci.graphWithoutLiteral.size() > 50)
					nosnippet = false;
				else {
					nosnippet = true;
					return;
				}
			} else {
				nosnippet = true;
				return;
			}
		} else
			nosnippet = false;*/

		//Printer.printGraphAsTensor(Doci.fullGraph, Doci.Rank, Doci.url);
		String RDFFileName = Doci.q.Text.replace(" ", "_") + Doci.Rank + "fullgraph" + System.currentTimeMillis();

		RDFManager.createRDFfile(RDFFileName + ".rdf", Doci.graphWithoutLiteral, "RDF/XML");
		stepCounter++;
		Printer.registerTime(stepCounter + "-Print Graph As Tensor of " + Doci.content.getTitle());

		//7.	Re-Apply Tensor decomposition to generate snippets (How???)		a.	Or go back to step 3 (expensive)
		Printer.setLogFile(Doci.Rank + "_7-Tensor decomposition2 (Snippet)");
		generateTheSnippet();
		stepCounter++;
		Printer.registerTime(stepCounter + "-Generate The Snippet of " + Doci.content.getTitle());

		System.out.println("Finish, Total time: " + ((System.currentTimeMillis() - start) / 1000.0) + " s");
	}

	private void generateTheSnippet() {
		ArrayList<Object> decompositionResults = applyTensorDecomposition();
		if (decompositionResults != null) {
			ArrayList<String> entities = (ArrayList<String>) decompositionResults.get(0);
			ArrayList<String> predicates = (ArrayList<String>) decompositionResults.get(1);
			Matrix U0 = (Matrix) decompositionResults.get(2);//subjects
			Matrix U1 = (Matrix) decompositionResults.get(3);//objects
			Matrix U2 = (Matrix) decompositionResults.get(4);//Predicates
			//Matrix lamda = (Matrix) decompositionResults.get(5);//lamda - center diagonal matrix
			groups = (ArrayList<Group>) decompositionResults.get(6);
			Printer.setLogFile(Doci.Rank + "-7-2-concepts");
			generateConcepts();
			Printer.registerTime("GenerateConcepts of " + Doci.content.getTitle());

			//generate links

			int linksLength = 0;
			links = new ArrayList<String>();
			linksLabels = new ArrayList<String>();

			int wikisLength = 0;
			wikis = new ArrayList<String>();
			wikisLabels = new ArrayList<String>();
			wikisPhotos = new ArrayList<String>();
			Model m = Doci.fullGraph;
			StmtIterator sts = m.listStatements();
			while (sts.hasNext()) {
				Statement st = sts.next();
				Property P = st.getPredicate();
				Resource S = st.getSubject();
				RDFNode O = st.getObject();
				String url = "";
				if (O.isLiteral())
					url = O.asLiteral().getString();
				else
					url = O.asResource().getURI();

				//links
				if (Doci.pridecatForLinks.contains(P.getURI())) {
					String txt = S.asResource().getLocalName().replace("_", " ");
					String txt1 = S.asResource().getLocalName().replace("_", " ") + " " + P.asResource().getLocalName().replace("_", " ");
					if (!url.contains("@") && !linksLabels.contains(txt) && !links.contains(url) && ((linksLength + txt.length()) < linksLimit)) {
						linksLength += txt1.length() + 2;
						links.add(url);
						linksLabels.add(txt1.replace("homepage", "").replace("website", ""));
					}
				}

				//generate top links (wikipedia links)
				if (Doci.pridecatForWikis.contains(P.getURI())) {
					if (!Doci.conceptsMap.keySet().contains(S.asResource().getURI())) {
						String wikiUrl = RDFManager.getWikiPage(Doci.fullGraph, S);
						String txt = S.asResource().getLocalName().replace("_", " ");
						if (!wikiUrl.contains("@") && !wikisLabels.contains(txt) && ((wikisLength + txt.length()) < wikisLimit)) {
							wikisLength += txt.length() + 2;
							wikis.add(wikiUrl);
							wikisLabels.add(txt);
							wikisPhotos.add(RDFManager.getWikiPhoto(Doci.fullGraph, S, Doci.pridecatForPhotos));
						}
					}
				}
			}
			Printer.registerTime("Generate links of " + Doci.content.getTitle());
			buildMLFeatures();
			findMainSentence();
			cutPh();
			Printer.registerTime("Find Main Sentence of " + Doci.content.getTitle());
			annotateSESnippet();
			Printer.registerTime("Annotate SE Snippet of " + Doci.content.getTitle());

		} else {
			nosnippet = true;
		}

	}

	private void buildMLFeatures() {
		String[] mlResults = MLManager.generateConcepts(this, "doc" + this.Doci.Rank);
		Doci.mlMainSentenceFeatures = mlResults[2];
		Doci.mlConceptsFeatures = mlResults[1];
	}

	private void annotateSESnippet() {
		Printer.setLogFile(Doci.Rank + "_annotateSESnippet");
		String oldSnippet = Doci.content.getSnippet();
		if (oldSnippet != "") {
			String usedResources = "";
			for (EnsenDBpediaResource r : Doci.Resources) {
				if (r.originalText != null && r.originalText.length() > 3) {
					Pattern p = Pattern.compile("> (.)+" + r.originalText.toLowerCase() + " (.)+</a");
					Matcher m = p.matcher(oldSnippet.toLowerCase());

					if (!usedResources.contains(r.getFullUri()) && !m.find()) {
						oldSnippet = oldSnippet.replace(" " + r.originalText + " ", "<a href='" + r.getFullUri() + "' target='_blank' > " + r.originalText + " </a>");
						usedResources += r.getFullUri();
					}

				}
			}
			seSnippet = oldSnippet;

		}
		//System.err.println(seSnippet);
	}

	private void cutPh() {
		int limit = Integer.parseInt(PropertiesManager.getProperty("maxMainSentenceLength"));
		if (mainPh.length() > limit)
			mainPh = mainPh.substring(0, limit) + "...";

	}

	/*
	 * This method calculate a score for each sentence in the document
	 * then select the one with max score as main sentence 
	 */
	private void findMainSentence() {
		Printer.setLogFile(Doci.Rank + "_mainPh");
		System.out.println("FindMainSentence");
		ArrayList<String> phs = Doci.sentences;
		double max = 0.0;

		for (String onePh : Doci.mlMainSentenceFeatures.split("\n")) {
			String[] values = onePh.split(",");
			int phId = Integer.parseInt(values[0].split("ph")[1]);
			//get ml score
			MlControler mlctr = new MlControler();
			double[] mlPhResults = mlctr.evaluateForMainSentence(values, "bayes");
			System.err.println("ph: " + Doci.sentences.get(phId) + " Score: " + mlPhResults[1]);

			if (mlPhResults[1] > max) {
				mainPh = Doci.sentences.get(phId);
				max = mlPhResults[1];
			}

		}

		System.out.println("Selected Sentence: (" + max + ") : " + mainPh);

		/*for (int i = 0; i < phs.size(); i++) {
			Printer.logToFile(Doci.Rank + "-MainSentence", "Ph " + i);
			Printer.logToFile(Doci.Rank + "-MainSentence", "*********************");
			String ph = phs.get(i);
			Printer.logToFile(Doci.Rank + "-MainSentence", ph);
			double score = 0.0;

			//find how much small sub-sentnce
			int smallSentence = 0;
			for (String s : ph.split(",")) {
				if (s.split(" ").length < 3) {
					smallSentence++;
				}
			}

			Printer.logToFile(Doci.Rank + "-MainSentence", "smallSentence: " + smallSentence);

			int badContent = 2 * (ph.split("http").length - 1);
			badContent += 2 * (ph.split("www.").length - 1);
			badContent += 2 * (ph.split(".html").length - 1);
			Printer.logToFile(Doci.Rank + "-MainSentence", "badContent: " + badContent);

			if (smallSentence > MaxSmallTextInSentence) {
				score = 0;

			} else if (badContent > 0) {
				score = 0;
			} else {
				//By query text
				String[] qWords = Doci.q.Text.split("(?=\\p{Upper})|/| ");
				int matches = 0;
				for (int j = 0; j < qWords.length; j++) {
					if (qWords[j].length() > 3 && ph.toLowerCase().contains(qWords[j].toLowerCase())) {
						matches++;
					}
				}
				double qScore = matches / qWords.length;//Normalized
				score += qScore;
				Printer.logToFile(Doci.Rank + "-MainSentence", "By query," + matches + " matches, score" + qScore);

				//By DBpedia spotlight resources
				ArrayList<EnsenDBpediaResource> ress = null;
				double qr = 0;

				try {
					ress = Doci.resourcesInSentenses.get(i);
				} catch (Exception e) {
				}

				if (ress != null) {
					for (EnsenDBpediaResource res : ress) {
						//By query resource
						for (EnsenDBpediaResource r : Doci.q.Resources) {
							if (r.getFullUri().equals(res.getFullUri()))
								qr += 1;
						}
					}

					score += qr / qWords.length;//Normalized;
					Printer.logToFile(Doci.Rank + "-MainSentence", "query resources " + (qr / qWords.length));

					if (Doci.Resources != null) {
						double dbslScore = ress.size() * 10.0 / Doci.Resources.size();
						score += dbslScore;
						Printer.logToFile(Doci.Rank + "-MainSentence", "By DBpedia spotlight resources, score" + dbslScore);
					}
				}
				//by resources (text of resources)
				double resScore = 0;
				for (String res : allResource) {
					String name = Doci.fullGraph.getResource(res).getLocalName();
					String[] resWords = name.replace("_", " ").split("(?=\\p{Upper})|/| ");
					matches = 0;
					for (int j = 0; j < resWords.length; j++) {
						if (resWords[j].length() > 3 && ph.toLowerCase().contains(resWords[j].toLowerCase())) {
							matches++;
						}
					}
					resScore += matches * 1.0 / (resWords.length + 1);
				}
				score += resScore / allResource.size();
				Printer.logToFile(Doci.Rank + "-MainSentence", "By resources (text of resources), score" + resScore);

				//by groups
				double gScore = 0;
				if (ress != null)
					for (EnsenDBpediaResource res : ress) {
						for (Group g : groups) {
							try {
								for (Entry<String, Concept> auth : g.auths.entrySet()) {
									if (res.getFullUri().contains(auth.getKey()))
										gScore += 1;
								}
							} catch (Exception e) {
							}
							try {
								for (Entry<String, Concept> hub : g.hubs.entrySet()) {
									if (res.getFullUri().contains(hub.getKey()))
										gScore += 1;
								}
							} catch (Exception e) {
							}
							try {
								for (Entry<String, Double> pred : g.predicates.entrySet()) {
									String[] predicatWords = pred.getKey().replace("_", " ").split("(?=\\p{Upper})|/| ");
									matches = 0;
									for (int j = 0; j < predicatWords.length; j++) {
										if (predicatWords[j].length() > 3 && ph.toLowerCase().contains(predicatWords[j].toLowerCase())) {
											matches++;
										}
									}
									if (predicatWords.length > 0)
										gScore += matches * 1.0 / predicatWords.length;
								}
								if (g.predicates.entrySet().size() > 0)
									gScore /= g.predicates.entrySet().size();
							} catch (Exception e) {
							}
						}
					}

				score += gScore * 0.2;
				Printer.logToFile(Doci.Rank + "-MainSentence", "By groups, score" + gScore * 0.2);

				//many double spaces
				int spaces = ph.split("  ").length;
				if (spaces > 3) {
					score -= 2;
					Printer.logToFile(Doci.Rank + "-findConceptPH", "spaces -2");

				}

				//Percentage of numbers
				String s = ph.replaceAll("^\\D+|\\D+$", "").replaceAll("\\D+", ",");

				double numScore = s.split(",").length * 10.0 / ph.length();
				score -= numScore;
				Printer.logToFile(Doci.Rank + "-MainSentence", "Percentage of numbers -" + numScore);

				Printer.logToFile(Doci.Rank + "-MainSentence", "Total score: " + score);
				if (score > max) {
					mainPh = ph;
					max = score;
				}
			}
		}*/

	}

	private void generateConcepts() {
		Doci.concepts = new ArrayList<>();
		Collection<Concept> cs = Doci.conceptsMap.values();
		for (Concept c : cs) {
			//System.err.println("concept " + c.URI);
			int index = allResource.indexOf(c.URI);
			if (index != -1) {
				//System.err.println("founded concept " + c.URI);
				c.abstractTxt = abstracts.get(index).replace("@" + PropertiesManager.getProperty("lang"), "");
				if (c.abstractTxt.trim() == "")
					c.abstractTxt = c.name;
				c.abstractTxt = c.abstractTxt.replace("Template:Infobox", "");

			}

			c.calculateScore(resourcesFoundedByClib, ccontroler.maxScore);

			Doci.concepts.add(c);
		}

		// order by c.generalScore
		Collections.sort(Doci.concepts, new ConceptComparator());
		String conceptsText = "";
		for (Concept c : Doci.concepts) {
			conceptsText += c.name + ":  " + c.generalScore + " \n";
		}
		Printer.logToFile(Doci.Rank + "-rankedConcepts", conceptsText);
	}

	private void extractAuthConcepts(ArrayList<String> entities, ArrayList<String> predicates, Matrix Ss, Matrix Os, Matrix Ps, double limit) {
		System.out.println("extractAuthConcepts");
		ArrayList<Concept> res = new ArrayList<Concept>();
		//get i column
		for (int i = 0; i < Os.getColumnDimension(); i++) {
			//build Os column map
			HashMap<String, Double> OsMap = new HashMap<String, Double>();
			ValueComparator Obvc = new ValueComparator(OsMap);
			TreeMap<String, Double> auths = new TreeMap<String, Double>(Obvc);
			for (int j = 0; j < Os.getRowDimension(); j++) {
				double score = Os.get(j, i);
				if (score > limit)
					OsMap.put(entities.get(j), score);
			}
			//sort it
			auths.putAll(OsMap);
			System.out.println("Topic " + i);
			System.out.println(auths);
		}

	}

	private ArrayList<String> getToBeExtendedResources(Map<String, Double> resourcesFoundedByClib) {
		ArrayList<String> toBeExtendedResources = new ArrayList<>();
		toBeExtendedResources.addAll(resourcesFoundedByClib.keySet());
		return toBeExtendedResources;
	}

	private void extendGraph() {
		if (toBeExtendedResources != null) {
			System.out.println("model extended using " + toBeExtendedResources.size() + " resources");
			System.out.println("Graph size before: " + Doci.fullGraph.size());
			Model m = SparqlManager.getDocModel(toBeExtendedResources);
			System.out.println("adding triples: " + m.size());
			Doci.fullGraph.add(m);
			System.out.println("Graph size after: " + Doci.fullGraph.size());

		} else {
			System.out.println("No extension");
		}
	}

	private ArrayList<Object> applyTensorDecomposition() {
		String pythonOutput = PythonControler.runCP(Doci.fullGraph);
		Printer.registerTime("Apply Tensor Decomposition of " + Doci.content.getTitle());
		Printer.logToFile(Doci.Rank + "-Python out", pythonOutput);
		ArrayList<Object> res = PythonControler.fromPythonOutputToTensor(pythonOutput, Doci);
		Printer.registerTime("Read Tensor Decomposition results (create groups) of " + Doci.content.getTitle());

		return res;

	}

	private ArrayList<String> getAuthsFromGroups(ArrayList<Group> groups) {
		ArrayList<String> res = new ArrayList<>();
		for (Group g : groups) {
			for (Entry<String, Concept> auth : g.auths.entrySet()) {
				if (!res.contains(auth.getKey())) {
					res.add(auth.getKey());
				}
			}
		}
		return res;
	}

	private ArrayList<String> getHubsFromGroups(ArrayList<Group> groups) {
		ArrayList<String> res = new ArrayList<>();
		for (Group g : groups) {
			for (Entry<String, Concept> hub : g.hubs.entrySet()) {
				if (!res.contains(hub.getKey())) {
					res.add(hub.getKey());
				}
			}
		}
		return res;
	}

	private void printTensorResultMatrix(String name, ArrayList<String> entities, Matrix U) {
		//Printer.setLogFile("Matrix " + name);
		Printer.logToFile(Doci.Rank + "-TensorResult", "Matrix " + name);
		int n = U.getRowDimension();
		int k = U.getColumnDimension();

		System.out.print(String.format("%1$60s", " ") + " | ");
		for (int i = 1; i < k; i++) {
			System.out.print(String.format("%1$6s", i) + " | ");

		}
		Printer.logToFile(Doci.Rank + "-TensorResult", "");
		for (int i = 0; i < n; i++) {
			System.out.print(String.format("%1$60s", entities.get(i)) + " | ");
			for (int j = 0; j < k; j++) {
				System.out.printf("% ,.5f", U.get(i, j));
				System.out.print(" | ");
			}
			Printer.logToFile(Doci.Rank + "-TensorResult", "");
		}

	}

	private void buildTheRDFGraph() {
		allResource = Doci.builResourcesStringList();
		Model addRelations = SparqlManager.addRelationBetweenResources(allResource, Doci);
		System.out.println("            Added Relations: " + addRelations.size());
		Printer.logToFile(Doci.Rank + "-GraphAfterAddRelations ", addRelations.listStatements().toList().toString());

		Doci.fullGraph.add(addRelations);
		System.out.println("            Graph size after add Relations: " + Doci.fullGraph.size());
		//RDFManager.createRDFfile("RDFGraphWithLDrelations", Doci.fullGraph, "RDF/XML");

	}

	private void buildResourceTextData() {
		abstracts = new ArrayList<>();
		String txt = "";
		String cleanedTxt = "";

		if (allResource != null) {
			for (int j = 0; j < allResource.size(); j++) {
				String s = allResource.get(j);
				NodeIterator objects = Doci.conceptsMetainfoGraph.listObjectsOfProperty(Doci.conceptsMetainfoGraph.getResource(s), Doci.conceptsMetainfoGraph.getProperty("http://dbpedia.org/ontology/abstract"));
				String abstractTxt = null;
				while (objects.hasNext()) {
					txt = objects.next().toString();
					txt = txt.substring(0, Math.min(txt.length(), Integer.parseInt(PropertiesManager.getProperty("abstractLength"))));
					txt = txt.replaceAll("\\{(.*?)\\}", "").replaceAll("\\{", "").replaceAll("\\}", "");
					if (txt.length() > 10) {

						for (int i = 0; i < Doci.Resources.size(); i++) {
							EnsenDBpediaResource R = Doci.Resources.get(i);
							if (R.getFullUri().contains(s)) {
								if (R.abstractTxt.length() < 10)
									R.abstractTxt = txt;
								Doci.Resources.set(i, R);
							}
						}
						cleanedTxt = Texthandler.cleanText(txt);
						allText += cleanedTxt + " ";
						abstractTxt = cleanedTxt;
						Printer.logToFile(Doci.Rank + "- buildResourceTextData", s + " ====>  " + cleanedTxt);
						break;
					}
				}
				if (txt == null)
					txt = "";
				abstracts.add(txt);

			}
		}

	}

	private void Annotation() {
		//System.out.println("Annotation (" + this.Doci.content.getTitle() + ")");
		allText = Doci.text;
		Printer.logToFile(Doci.Rank + "-Text", " text before traitement (" + this.Doci.content.getTitle() + ")");
		Printer.logToFile(Doci.Rank + "-Text", allText);

		toBeAnnotatedTextLen = Integer.parseInt(PropertiesManager.getProperty("MaxDBpediaSpotlightQ")) * Integer.parseInt(PropertiesManager.getProperty("maxTextLen")) * retryFactor;
		toBeAnnotatedTextLen = Math.min(toBeAnnotatedTextLen, allText.length());

		usedText = allText.substring(0, toBeAnnotatedTextLen);
		//usedText = "French Revolution, also called Revolution of 1789,  the revolutionary movement that shook France between 1787 and 1799 and reached its first climax there in 1789. Hence the conventional term  Revolution of 1789,  denoting the end of the ancien r gime in France and serving also to distinguish that event from the later French revolutions of 1830 and 1848. Although historians disagree on the causes of the Revolution, the following reasons are commonly adduced   1  the increasingly prosperous elite of wealthy commoners merchants, manufacturers, and professionals, often called the bourgeoisie produced by the 18th century s economic growth resented its exclusion from political power and positions of honour   2  the peasants were acutely aware of their situation and were less and less willing to support the anachronistic and burdensome feudal system   3  the philosophes , who advocated social and political reform, had been read more widely in France than anywhere else   4  French participation in the American Revolution had driven the government to the brink of bankruptcy  and  5  crop failures in much of the country in 1788, coming on top of a long period of economic difficulties, made the population particularly restless.";

		Doci.sentences = Texthandler.textSpliter(usedText);
		/*rebuild-text*/
		StringBuilder listString = new StringBuilder();
		for (String s : Doci.sentences)
			listString.append(s);
		usedText = listString.toString();

		Doci.usedText = usedText;
		Printer.logToFile(Doci.Rank + "-Text", "Used text length:  (" + this.Doci.content.getTitle() + ") " + usedText.length() + " from " + allText.length());
		List<EnsenDBpediaResource> foundedResources = RDFizer.rdfizeTextWithoutThreads(usedText, selectedConfiance, Doci.internalFilePath);
		Doci.Resources = /* selectTopResourcesInThisDocument(*/foundedResources/*)*/;
		System.out.println("            FoundedResources: " + foundedResources.size());
		Doci.resourcesInSentenses = findResourcesInEachSentens(Doci.sentences);

		Doci.splitSentences();

		allText = Texthandler.cleanText(usedText);
		Printer.logToFile(Doci.Rank + "-Text", "text after traitement  (" + this.Doci.content.getTitle() + ")");
		Printer.logToFile(Doci.Rank + "-Text", allText);
		for (EnsenDBpediaResource r : Doci.Resources) {
			Printer.logToFile(Doci.Rank + "-resources", r.originalText + " ===> " + r.uri() + " ==> " + r.support() + " ==> " + r.similarityScore);
		}

		ArrayList<Object> returnList = RDFManager.generateModelFromDocument(Doci.url, Doci.Resources);
		Doci.graph = (Model) returnList.get(0);
		Doci.fullGraph = RDFManager.createRDFModel();
		Doci.fullGraph.add(Doci.graph);
		System.out.println("            n of resources founded in the document" + Doci.fullGraph.listObjects().toList().size());
		Printer.logToFile(Doci.Rank + "-graph", "First Graph size:  (" + this.Doci.content.getTitle() + ") " + Doci.fullGraph.size());
		Printer.logToFile(Doci.Rank + "-graph", Doci.fullGraph.listStatements().toList().toString());

	}

	/*
	 * this method use the spited text (in sentences: Doci.sentenses)
	 * and find in each one all resources using offsets sent by spotlight
	 * the output a list of sentences and for each one a list of resources as EnsenDBpediaResource
	 */
	private ArrayList<ArrayList<EnsenDBpediaResource>> findResourcesInEachSentens(ArrayList<String> sentences) {
		ArrayList<ArrayList<EnsenDBpediaResource>> res = new ArrayList<ArrayList<EnsenDBpediaResource>>();
		//fill with embty
		for (String ph : sentences) {
			ArrayList<EnsenDBpediaResource> senResources = new ArrayList<>();
			res.add(senResources);
		}

		for (EnsenDBpediaResource R : Doci.Resources) {

			int start = 0;
			int end = 0;

			for (int index = 0; index < sentences.size(); index++) {
				String ph = sentences.get(index);
				end += ph.length();
				if (start < R.offset && R.offset < end) {
					ArrayList<EnsenDBpediaResource> senResources;
					senResources = res.get(index);
					senResources.add(R);
					res.set(index, senResources);
					break;
				}
				start = end;
			}

		}

		return res;
	}

	/*
	 * this method build the Matrix R (Resources  X Terms )
	 * a Resources here is represented by his abstract + text around all its occurrences in the original text 
	 * value R(i,j) in this matrix are the frequency(occurrence) of the term i for the resource j 
	 */
	private Matrix buildTermResourceMatrix() {
		System.out.println("            Build the R Matrix ");
		//5.1	Build terms' arrays
		System.out.println("            build terms");
		Printer.setLogFile(Doci.Rank + "terms");
		System.out.println("            Build terms' array");
		terms = new ArrayList<String>();
		splitedText = allText.toLowerCase().split(" ");
		String newSplitedText = "";

		for (int j = 0; j < splitedText.length; j++) {
			String term = splitedText[j].trim();
			//rooting
			term = Doci.SBStemmer.stem(term);
			if (terms.indexOf(term) < 0 && term.length() > 3) {
				terms.add(term);
				newSplitedText += " " + term;
			}
		}
		splitedText = newSplitedText.split(" ");
		System.out.println("            terms array size: " + terms.size());
		Printer.logToFile(Doci.Rank + "-Terms", Arrays.toString(terms.toArray()));

		//resource  X terms matrix
		R = new Matrix(allResource.size(), terms.size(), 0.0);
		Printer.setLogFile(Doci.Rank + "resourcesRelatedText");
		for (int i = 0; i < allResource.size(); i++) {
			String resource = allResource.get(i);
			//Build related text as: 
			// 1- resource abstract
			// 2- sub-text from the original text, next to this resource, using window
			String resourceRelatedText = "";
			EnsenDBpediaResource Res = Doci.getFirstResourceWithUri(resource);
			resourceRelatedText = Res.abstractTxt + " # ";
			ArrayList<EnsenDBpediaResource> instances = Doci.getAllResourceInstances(resource);
			int resourceWindowSize = Integer.parseInt(PropertiesManager.getProperty("resourceAroundTextWindowSize")) / 2;
			for (EnsenDBpediaResource instance : instances) {
				int from = instance.offset - resourceWindowSize;
				if (from < 0)
					from = 0;
				if (from < Doci.usedText.length()) {
					int to = instance.offset + instance.originalText.length() + resourceWindowSize;
					if (to > Doci.usedText.length())
						to = Doci.usedText.length() - 1;
					try {
						resourceRelatedText += Doci.usedText.substring(from, to) + " $ ";
					} catch (Exception e) {
						System.err.printf("Error in getting text around %s, from %d  to %d , text length %d %n ", instance.getFullUri(), from, to, Doci.usedText.length());
					}
				}
			}
			//System.out.println("Resource: " + Res.getFullUri() + " ==> " + resourceRelatedText);
			resourceRelatedText = Texthandler.cleanText(resourceRelatedText);
			String[] splitedAbs = resourceRelatedText.toLowerCase().split(" ");
			for (int j = 0; j < splitedAbs.length; j++) {
				String absTerm = Doci.SBStemmer.stem(splitedAbs[j]);
				int index = terms.indexOf(absTerm);
				if (index >= 0)
					if (resource.toLowerCase().contains(splitedAbs[j].toLowerCase()))
						R.set(i, index, 1);
					else
						R.set(i, index, R.get(i, index) + 1);
			}

		}

		Printer.setLogFile(Doci.Rank + "ResourcesTermsMatrix");
		Printer.logToFile(Doci.Rank + "-Matrix", "The generated Matrix: " + allResource.size() + " X " + terms.size());
		return R;

	}

	public Document generateDoc(String url, ensen.entities.Query q) {
		Result res = new Result();
		res.setTitle(" title ");
		res.setHtmlTitle("   title ");
		res.setLink(url);
		res.setSnippet("snippet");
		res.setHtmlSnippet("Snippet");
		Document D = new Document(res.getLink(), 1, null);
		D.content = res;
		D.q = q;

		return D;
	}
}
