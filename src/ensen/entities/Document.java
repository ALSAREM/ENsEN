package ensen.entities;

import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import javax.servlet.http.HttpSession;

import com.google.api.services.customsearch.model.Result;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.NodeIterator;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Selector;
import com.hp.hpl.jena.rdf.model.SimpleSelector;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;

import ensen.control.PropertiesManager;
import ensen.control.RDFManager;
import ensen.control.RDFizer;
import ensen.control.SparqlManager;
import ensen.control.db.DBcontroler;
import ensen.threads.ResourceScoreThread;
import ensen.util.Calculate;
import ensen.util.HTMLhandler;
import ensen.util.MathTools;

public class Document {
	public Result content;
	public String text;
	public ArrayList<Entity> entities;
	public Model graph;
	public Model subGraph;
	public Model extendedGraph;
	public Model fullGraph;
	public List<EnsenDBpediaResource> triplets;
	public List<EnsenDBpediaResource> oldSnippetResources;
	public List<Triplet> bestTriplets;
	public Model annotationSemantic;
	public ArrayList<String> SemanticAnnotationResources;
	public TreeMap<String, Double> ProjectionRes;
	public Double ProjectionSimRes;
	public ArrayList<String> linkUrls;
	public String newSnippet;
	public String url;
	public Map<String, Integer> topicRelatedFreqResources = null;
	public Map<String, Integer> topicInRelatedResources = null;
	public Map<String, Integer> topicOutRelatedResources = null;
	public Map<String, Integer> topicAllRelatedResources = null;
	public Set<String> queryRelatedResources = null;
	public Model TopicSubGraph;
	public Model intrestedModel;
	public ArrayList<String> allRes;
	public TreeMap<String, Model> SubGraphs;
	public Model QuerySubGraph;
	public Map<String, Map<Triplet, Double>> rankedClusters;
	public Map<String, Double> allRankedResources;
	public ArrayList<String> usedImages;

	// Parameters

	public Map<String, String> Snippets;
	public String AllSnippets;

	public String topSubjects = "";
	public Map<Triplet, Double> TopTriples;
	public MultiZoneSnippet multiZoneSnippet;
	public int Rank = -1;
	public int MaximumIn = 1;
	public int MaximumOut = 1;
	public int MaximumFreq = 1;
	public double maxSimilarity;
	public double avgSimilarity;
	public HttpSession ensenSession = null;
	public double inImportance;
	public double outImportance;
	public double freqImportance;
	public double qImportance;
	public double SimilarityImportance;
	public boolean rerank;
	public Query q;
	public String queryText;
	Map<String, List<Map<String, Object>>> pageMap;
	public String mainImage = "";
	public double semanticAnnotationImportance;
	private String tags;
	public String html;
	public double freqMean;
	public double simMean;
	public double inMean;
	public double outMean;
	public double freqSD;// standard deviation
	public double simSD;
	public double inSD;
	public double outSD;
	ArrayList<Double> simData = new ArrayList<Double>();// similarity values

	public Document() {

	}

	public Document(String url, int rank, Map<String, List<Map<String, Object>>> map) {
		init();
		pageMap = map;
		Rank = rank;
		this.url = url;
		System.out.println("Load Content(HTML) for: " + url);
		// get content
		String docString = HTMLhandler.loadContent(url);
		html = docString;
		// get Text only
		text = HTMLhandler.CleanContent(docString);
		// inforce by main content
		// text = HTMLhandler.inforceMainContentByBoiler(url, text);
		// get Annotation semantic
		SemanticAnnotationResources = new ArrayList<String>();
		if (Boolean.parseBoolean(PropertiesManager.getProperty("AnnotationSemantic"))) {
			annotationSemantic = RDFManager.getAnnotationSemantic(url);
			ResIterator ss = annotationSemantic.listSubjects();
			NodeIterator oo = annotationSemantic.listObjects();
			while (ss.hasNext()) {
				Resource s = ss.next();
				SemanticAnnotationResources.add(s.getURI());
			}
			while (oo.hasNext()) {
				RDFNode o = oo.next();
				if (o.isResource())
					SemanticAnnotationResources.add(o.asResource().getURI());
			}
		}

		// transform meta tag to semantic annotations
		// from map to text
		tags = "";
		if (pageMap != null)
			for (Map.Entry<String, List<Map<String, Object>>> entry1 : pageMap.entrySet()) {
				for (Map<String, Object> entry2 : entry1.getValue()) {
					for (Map.Entry<String, Object> entry3 : entry2.entrySet()) {
						if (entry1.getKey().contains("cse_image") && entry3.getKey().contains("src"))
							if (mainImage.isEmpty())
								mainImage = entry3.getValue() + "";
						if (entry1.getKey().contains("cse_thumbnail") && entry3.getKey().contains("src"))
							if (mainImage.isEmpty())
								mainImage = entry3.getValue() + "";
						if (entry1.getKey().contains("metatags") && entry3.getKey().contains("og:image"))
							if (mainImage.isEmpty())
								mainImage = entry3.getValue() + "";
						if (entry1.getKey().contains("imageobject") && entry3.getKey().contains("contenturl"))
							if (mainImage.isEmpty())
								mainImage = entry3.getValue() + "";

						try {
							URL u = new URL(entry3.getValue().toString());
						} catch (Exception e) {
							if (!entry3.getValue().toString().contains("http"))
								tags += entry3.getValue().toString().replaceAll("[^a-zA-Z]", " ") + " ";
						}

					}
				}
			}

	}

	public ArrayList<String> pridecatStopList;
	public ArrayList<String> pridecatForPhotos;
	public ArrayList<String> pridecatForLinks;
	public HashMap<String, Integer> queryRelatedResourcesMap;
	public double qMean;
	public double qSD;

	private void init() {
		pridecatStopList = new ArrayList<String>();
		pridecatForPhotos = new ArrayList<String>();
		pridecatForLinks = new ArrayList<String>();
		pridecatStopList.add("http://www.w3.org/2002/07/owl#sameAs");
		pridecatStopList.add("http://dbpedia.org/property/wikiPageUsesTemplate");
		pridecatStopList.add("wordnet");

		pridecatForPhotos.add("http://dbpedia.org/ontology/thumbnail");
		pridecatForPhotos.add("http://xmlns.com/foaf/0.1/depiction");
		pridecatForPhotos.add("http://xmlns.com/foaf/0.1/thumbnail");
		pridecatForPhotos.add("http://purl.org/dc/elements/1.1/rights");
		// pridecatForPhotos.add("http://dbpedia.org/property/caption");
		pridecatForPhotos.add("http://dbpedia.org/property/mapCaption");
		pridecatForPhotos.add("http://dbpedia.org/property/imageMap");
		pridecatForPhotos.add("http://dbpedia.org/property/cover");
		pridecatForPhotos.add("http://dbpedia.org/property/pushpinMap");
		pridecatForPhotos.add("http://dbpedia.org/property/flag");
		pridecatForPhotos.add("http://dbpedia.org/property/imageFlag");
		pridecatForPhotos.add("http://dbpedia.org/property/logo");
		pridecatForPhotos.add("http://dbpedia.org/property/imageSkyline");
		pridecatForPhotos.add("http://dbpedia.org/property/imageCoat");
		pridecatForPhotos.add("http://dbpedia.org/property/companyLogo");
		pridecatForPhotos.add("http://dbpedia.org/property/image");
		pridecatForPhotos.add("http://dbpedia.org/property/partyLogo");
		pridecatForPhotos.add("http://dbpedia.org/property/staticImage");
		pridecatForPhotos.add("http://dbpedia.org/property/stationLogo");

		pridecatForLinks.add("http://dbpedia.org/ontology/wikiPageExternalLink");
		pridecatForLinks.add("http://dbpedia.org/property/hasPhotoCollection");
		pridecatForLinks.add("http://xmlns.com/foaf/0.1/primaryTopic");
		pridecatForLinks.add("http://xmlns.com/foaf/0.1/isPrimaryTopicOf");
		pridecatForLinks.add("http://purl.org/dc/elements/1.1/language");
		pridecatForLinks.add("http://dbpedia.org/ontology/wikiPageWikiLink");
		pridecatForLinks.add("http://dbpedia.org/property/website");
		pridecatForLinks.add("http://xmlns.com/foaf/0.1/homepage");
		pridecatForLinks.add("http://dbpedia.org/property/url");
		pridecatForLinks.add("http://dbpedia.org/ontology/related");
		pridecatForLinks.add("http://dbpedia.org/property/web");
		pridecatForLinks.add("http://dbpedia.org/property/source");

	}

	/*
	 * Main RDFizer for document
	 */
	public void RDFize() {

		String allText = content.getSnippet() + text; /*
														* RDFize the old snippet
														* and the main content
														*/
		String cachePath = PropertiesManager.getProperty("cachePath") + queryText.toLowerCase().replace(" ", "_") + "_" + this.content.getTitle().replace(" ", "_").replaceAll("[^A-Za-z0-9-_ ]", "");
		System.out.println("RDFize " + this.content.getTitle());
		triplets = RDFizer.rdfizeTextWithoutThreads(allText, cachePath);
		System.out.println("--------------------------finish RDFizing --------------------------");
		oldSnippetResources = new ArrayList<EnsenDBpediaResource>();
		int currentOffset = 0;
		int counter = 0;
		if ((triplets != null) && (triplets.size() > 0)) {
			currentOffset = triplets.get(counter).offset;
			while (currentOffset < content.getSnippet().length()) {
				if (currentOffset < triplets.get(counter).offset) {
					oldSnippetResources.add(triplets.get(counter));
					currentOffset = triplets.get(counter).offset + triplets.get(counter).originalText.length();
				}
				counter++;
				if (counter >= triplets.size())
					break;
			}
		}

		ArrayList<Object> returnList = RDFManager.generateModelFromDocument(url, triplets);
		graph = (Model) returnList.get(0);
		maxSimilarity = (Double) returnList.get(1);
		avgSimilarity = (Double) returnList.get(2);

		if (!tags.trim().isEmpty()) {
			String tagsCachePath = PropertiesManager.getProperty("cachePath") + queryText.toLowerCase().replace(" ", "_") + "_" + this.content.getTitle().replace(" ", "_").replaceAll("[^A-Za-z0-9-_ ]", "") + "_tags";
			List<EnsenDBpediaResource> resources = RDFizer.rdfizeText(tags, tagsCachePath);
			for (EnsenDBpediaResource R : resources) {
				SemanticAnnotationResources.add(R.getFullUri());
			}
		}

		if (annotationSemantic != null)
			try {
				graph.add(annotationSemantic);
			} catch (Exception e2) {
			}
		fullGraph = RDFManager.createRDFModel();
		fullGraph.add(graph);

	}

	public void extendDocument() {
		if (triplets != null) {
			NodeIterator it = graph.listObjects();
			fullGraph = RDFManager.createRDFModel();
			fullGraph.add(graph);
			// extend documents graph
			fullGraph.add(SparqlManager.getDocModel(it));
		}
	}

	public void extendDocumentUsingResourcesList(Map<String, Double> in, ArrayList<String> semanticAnnotationResources) {
		System.out.println("Size before expansion: " + fullGraph.size());
		fullGraph = RDFManager.createRDFModel();
		fullGraph.add(graph);
		ArrayList<String> resources = new ArrayList<String>();
		resources.addAll(semanticAnnotationResources);
		for (Map.Entry<String, Double> entry : in.entrySet()) {
			resources.add(entry.getKey());
		}
		fullGraph.add(SparqlManager.getResourcesModel(resources));

		System.out.println("Size after expansion: " + fullGraph.size());

	}

	/*
	 * limit related resources lists size (take the top)
	 */
	public Map<String, Integer> putFirstEntries(long max, Map<String, Integer> source) {
		int count = 0;
		Map<String, Integer> target = new HashMap<String, Integer>();
		// TreeMap<String, Integer> target = new TreeMap<String, Integer>();
		for (Map.Entry<String, Integer> entry : source.entrySet()) {
			if (count >= max)
				break;
			target.put(entry.getKey(), entry.getValue());
			count++;
		}

		Map out = sortByValueInt(target);

		return out;
	}

	/*
	 * limit related resources lists size (take the top)
	 */
	public Map<String, Double> putFirstEntriesDouble(long max, Map<String, Double> source) {
		int count = 0;
		Map<String, Double> target = new HashMap<String, Double>();
		// TreeMap<String, Double> target = new TreeMap<String, Double>();
		for (Map.Entry<String, Double> entry : source.entrySet()) {
			if (count >= max)
				break;

			target.put(entry.getKey(), entry.getValue());
			count++;
		}
		Map<String, Double> out = sortByValueDouble(target);
		out.putAll(target);
		return out;
	}

	/*
	 * Analyzing the document Must call Document(String url, Query q) first
	 */
	public void analyzeDocument(Query q, double inImportance, double outImportance, double freqImportance, double qImportance, double SimilarityImportance, double semanticAnnotationImportance, boolean rerank) {
		System.out.println("****************************************************************");
		System.out.println("Analyzing: " + content.getTitle());

		long start = System.currentTimeMillis();
		long firstrank = start;
		this.inImportance = inImportance;
		this.outImportance = outImportance;
		this.freqImportance = freqImportance;
		this.qImportance = qImportance;
		this.SimilarityImportance = SimilarityImportance;
		this.rerank = rerank;
		this.q = q;
		this.semanticAnnotationImportance = semanticAnnotationImportance;

		if (triplets != null) {

			System.out.println("----- (1) First Ranking: get best resources for expansion ---------");

			firstrank = System.currentTimeMillis();
			generateTopicRelatedResByInOut();
			generateTopicRelatedResByFreq();
			queryRelatedResources = q.getResourcesFromDoc(this);
			System.out.println("----------First Ranking----------");
			// Take Top n=5%
			double percent = 0.1;

			Map<String, Double> rankedResources = resourcesSelector(percent, inImportance, outImportance, freqImportance, qImportance, SimilarityImportance);
			System.err.println(rankedResources);
			System.out.println("----- (2) Extend document's graph using best Resources, then clean it ---------");

			extendDocumentUsingResourcesList(rankedResources, SemanticAnnotationResources);

			// delete added resource (document URL), i.e. remove application
			// added resources
			fullGraph = RDFManager.removeSubjectWithAllStatements(fullGraph, url);

			System.out.println("-------- (3)Second Ranking: reRanking after graph expansion-----------");
			long secondrank = System.currentTimeMillis();
			generateTopicRelatedResByInOut();
			generateQueryRelatedRes();
			queryRelatedResources = q.getResourcesFromDoc(this);

			System.out.println("----------------Calculate Means--------------------");
			calculateMeans();
			System.out.println("Means: inMean(" + inMean + ") outMean(" + outMean + ") freqMean(" + freqMean + ") simMean(" + simMean + ")");

			System.out.println("----------------Calculate Standard Deiation--------------------");
			calculateSD();
			System.out.println("Standard Deiations: inSD(" + inSD + ") outSD(" + outSD + ") freqSD(" + freqSD + ") simSD(" + simSD + ")");

			allRankedResources = resourcesRanker(inImportance, outImportance, freqImportance, qImportance, SimilarityImportance);

			long triplesrank = System.currentTimeMillis();
			TopTriples = clusterTriplesRanker(fullGraph, q);
			long generateSnippet = System.currentTimeMillis();
			multiZoneSnippet = new MultiZoneSnippet(this, TopTriples);
			long end = System.currentTimeMillis();
		}

	}

	private void generateQueryRelatedRes() {
		queryRelatedResourcesMap = new HashMap<String, Integer>();
		for (Map.Entry<String, Integer> entry : topicAllRelatedResources.entrySet()) {
			if (queryRelatedResources.contains(entry.getKey())) {
				queryRelatedResourcesMap.put(entry.getKey(), 1);
			} else {
				queryRelatedResourcesMap.put(entry.getKey(), 0);
			}
		}

	}

	private void calculateMeans() {
		inMean = MathTools.calculateMean(topicInRelatedResources);
		outMean = MathTools.calculateMean(topicOutRelatedResources);
		freqMean = MathTools.calculateMean(topicRelatedFreqResources);
		qMean = MathTools.calculateMean(queryRelatedResourcesMap);

		simMean = 0.0;
		for (EnsenDBpediaResource r : triplets) {
			simMean += r.similarityScore;
			simData.add(r.similarityScore);
		}
		if (triplets.size() > 0)
			simMean = simMean / triplets.size();
	}

	private void calculateSD() {
		inSD = MathTools.calculateStandardDeviation(topicInRelatedResources.values(), inMean);
		outSD = MathTools.calculateStandardDeviation(topicOutRelatedResources.values(), outMean);
		freqSD = MathTools.calculateStandardDeviation(topicRelatedFreqResources.values(), freqMean);
		simSD = MathTools.calculateStandardDeviationDouble(simData, simMean);
		qSD = MathTools.calculateStandardDeviation(queryRelatedResourcesMap.values(), qMean);
	}

	/*
	 * public void analyzeDocument(Query q, double inImportance, double
	 * outImportance, double freqImportance, double qImportance, double
	 * SimilarityImportance, double semanticAnnotationImportance, boolean
	 * rerank) { System.out.println(
	 * "****************************************************************");
	 * System.out.println("Analyzing: " + content.getTitle());
	 * 
	 * long start = System.currentTimeMillis(); long firstrank = start;
	 * this.inImportance = inImportance; this.outImportance = outImportance;
	 * this.freqImportance = freqImportance; this.qImportance = qImportance;
	 * this.SimilarityImportance = SimilarityImportance; this.rerank = rerank;
	 * this.q = q; this.semanticAnnotationImportance =
	 * semanticAnnotationImportance;
	 * 
	 * if (triplets != null) { if (!rerank) { firstrank =
	 * System.currentTimeMillis(); generateTopicRelatedResByInOut();
	 * generateTopicRelatedResByFreq(); queryRelatedResources =
	 * q.getResourcesFromDoc(this);
	 * System.out.println("----------------Calculate freq Mean--------------------"
	 * ); freqMean = 0.0; int i = 0; for (Map.Entry<String, Integer> entry :
	 * topicRelatedFreqResources.entrySet()) { freqMean += entry.getValue();
	 * i++; } if (i > 0) freqMean = freqMean / i;
	 * System.out.println("freqMean: " + freqMean + " for (" + i + ") values");
	 * 
	 * System.out.println("----------------Calculate sim Mean--------------------"
	 * ); simMean = 0.0; for (EnsenDBpediaResource r : triplets) { simMean +=
	 * r.similarityScore; simData.add(r.similarityScore); } if (triplets.size()
	 * > 0) simMean = simMean / triplets.size(); System.out.println("simMean: "
	 * + simMean + " for (" + triplets.size() + ") values");
	 * 
	 * System.out.println("----------------Calculate freq SD--------------------"
	 * ); freqSD =
	 * MathTools.calculateStandardDeviation(topicRelatedFreqResources.values(),
	 * freqMean); System.out.println("freqSD: " + freqSD + " for (" +
	 * triplets.size() + ") values");
	 * 
	 * System.out.println("----------------Calculate Sim SD--------------------")
	 * ; simSD = MathTools.calculateStandardDeviationDouble(simData, simMean);
	 * System.out.println("simSD: " + simSD + " for (" + triplets.size() +
	 * ") values");
	 * 
	 * System.out.println("----------First Ranking----------");
	 * 
	 * Map<String, Double> rankedResources = resourcesRanker(inImportance,
	 * outImportance, freqImportance, qImportance, SimilarityImportance);
	 * 
	 * //Take Top n=5% long n = Math.round(0.05 *
	 * rankedResources.entrySet().size()); long size =
	 * rankedResources.entrySet().size(); //test take just 100 res //n =
	 * Math.min(size, 100);
	 * System.out.println("Extend document using just top n Resources");
	 * extendDocumentUsingResourcesList(rankedResources,
	 * SemanticAnnotationResources, n); //To do enrich the graph with relation
	 * from LD (all triples where the S & O are in the graph) //delete added
	 * resource (document url) //remove application added resources Selector
	 * selector = new SimpleSelector(fullGraph.getResource(url), null, (RDFNode)
	 * null);//as subject StmtIterator stms =
	 * fullGraph.listStatements(selector); fullGraph.remove(stms.toList());
	 * 
	 * }
	 * 
	 * long secondrank = System.currentTimeMillis(); //
	 * System.out.println("regenerate Topic Related Res By In-Out");// freq not
	 * changed generateTopicRelatedResByInOut(); queryRelatedResources =
	 * q.getResourcesFromDoc(this);
	 * //System.out.println("query related resources: " +
	 * queryRelatedResources.size());
	 * 
	 * System.out.println("--------Second Ranking-----------");
	 * allRankedResources = resourcesRanker(inImportance, outImportance,
	 * freqImportance, qImportance, SimilarityImportance);
	 * 
	 * //limit related resources lists size (take the top) long max2 =
	 * Math.round(0.02 * allRankedResources.entrySet().size());
	 * topicInRelatedResources = putFirstEntries(max2, topicInRelatedResources);
	 * topicOutRelatedResources = putFirstEntries(max2,
	 * topicOutRelatedResources); topicRelatedFreqResources =
	 * putFirstEntries(max2, topicRelatedFreqResources);
	 * topicAllRelatedResources = putFirstEntries(max2,
	 * topicAllRelatedResources); allRankedResources =
	 * putFirstEntriesDouble(max2, allRankedResources);
	 * 
	 * //generate Query subGraph //QuerySubGraph = createQuerySubgraph(q);
	 * 
	 * long triplesrank = System.currentTimeMillis();
	 * 
	 * TopTriples = clusterTriplesRanker(fullGraph, q);
	 * 
	 * long generateSnippet = System.currentTimeMillis(); multiZoneSnippet = new
	 * MultiZoneSnippet(this, TopTriples);
	 * 
	 * long end = System.currentTimeMillis();
	 * 
	 * 
	 * }
	 * 
	 * }
	 */
	private Map<Triplet, Double> getTopTriples(Map<Triplet, Double> all, long max) {
		HashMap<Triplet, Double> topTriples = new HashMap<Triplet, Double>();
		int counter = 0;
		for (Map.Entry<Triplet, Double> entry : all.entrySet()) {
			if (counter < max) {
				if (!entry.getKey().statement.getPredicate().asResource().getURI().contains("http://ensen.org")) {
					topTriples.put(entry.getKey(), entry.getValue());
					// System.out.println(entry.getValue() + ": " +
					// entry.getKey().statement.getSubject() + " -->" +
					// entry.getKey().statement.getPredicate() + " -->" +
					// entry.getKey().statement.getObject());
					counter++;
				}
			}
		}

		return sortTriples(topTriples);
	}

	/*
	 * private void generateSnippetsForClusters() {
	 * 
	 * Snippets = new TreeMap<String, String>(); for (Map.Entry<String,
	 * Map<Triplet, Double>> entry : rankedClusters.entrySet()) { if
	 * (entry.getValue().size() > 0) Snippets.put(entry.getKey(),
	 * generateOneSnippet(entry.getValue()).AccordingSnippet); } }
	 */
	/*
	 * private MultiZoneSnippet generateOneSnippet(Map<Triplet, Double> cluster)
	 * { String snippet = ""; //group predicates (same subject + same
	 * predicates)
	 * 
	 * ArrayList<String> SPSnippetsIndex = new ArrayList<String>();
	 * ArrayList<String> subjects = new ArrayList<String>(); ArrayList<String>
	 * predicates = new ArrayList<String>(); ArrayList<String> objects = new
	 * ArrayList<String>(); ArrayList<String> sUrls = new ArrayList<String>();
	 * ArrayList<String> photos = new ArrayList<String>(); ArrayList<String>
	 * links = new ArrayList<String>();
	 * 
	 * for (Map.Entry<Triplet, Double> entry : cluster.entrySet()) { Resource S
	 * = entry.getKey().statement.getSubject(); Property P =
	 * entry.getKey().statement.getPredicate(); RDFNode O =
	 * entry.getKey().statement.getObject();
	 * 
	 * String s = S.getLocalName().replace("_", " "); String p =
	 * P.getLocalName().replace("_", " "); String finalP = ""; String[] ps =
	 * p.split("[A-Z]");
	 * 
	 * int index = 0; for (int i = 0; i < ps.length; i++) { index +=
	 * ps[i].length(); char ch = ' '; if (index < p.length()) ch =
	 * p.charAt(index); finalP += ps[i] + " " + ch; index += 1; } if
	 * (!pridecatStopList.contains(p)) if (pridecatForPhotos.contains(p))//photo
	 * { photos.add(O.toString()); } else if (pridecatForLinks.contains(p)) {
	 * links.add("<a href='" + O.toString() + "'>" + s + " " + p + "</a>"); }
	 * else { String o = ""; if (O.isResource()) { o =
	 * O.asResource().getLocalName().replace("_", " "); if (o.trim() == "") o =
	 * O.asResource().getURI(); } else { if
	 * (O.asLiteral().getLanguage().equals(PropertiesManager
	 * .getProperty("lang"))) { String value =
	 * O.asLiteral().getValue().toString(); o = value.substring(0,
	 * Math.min(value.length(),
	 * Integer.parseInt(PropertiesManager.getProperty("maxSnippetObjectDescription"
	 * )))); } } if (s.trim() == "") s = S.getURI(); if (s.trim().toLowerCase()
	 * != o.trim().toLowerCase()) { if (SPSnippetsIndex.contains(s + p)) { int k
	 * = SPSnippetsIndex.indexOf(s + p); if (!objects.get(k).contains(", " + o))
	 * objects.set(k, objects.get(k) + ", " + o); } else { SPSnippetsIndex.add(s
	 * + p); sUrls.add(S.getURI()); subjects.add(s); predicates.add(finalP);
	 * objects.add(", " + o);
	 * 
	 * }
	 * 
	 * } } }
	 * 
	 * for (int i = 0; i < objects.size(); i++) { for (int j = i + 1; j <
	 * objects.size(); j++) {
	 * 
	 * if ((subjects.get(i) == subjects.get(j)) && (objects.get(i) ==
	 * objects.get(j))) { predicates.set(i, predicates.get(i) + "(" +
	 * predicates.get(j) + ")"); predicates.set(j, ""); }
	 * 
	 * if ((predicates.get(i).equals(predicates.get(j))) &&
	 * (objects.get(i).equals(objects.get(j)))) { subjects.set(i,
	 * subjects.get(i) + "," + subjects.get(j) + " "); predicates.set(j, ""); }
	 * } }
	 * 
	 * //generate according items ArrayList<String> itemHeader = new
	 * ArrayList<String>(); ArrayList<String> itemContent = new
	 * ArrayList<String>();
	 * 
	 * for (int i = 0; i < predicates.size(); i++) { if (predicates.get(i) !=
	 * "") { if (itemHeader.contains(subjects.get(i))) {
	 * itemContent.set(itemHeader.indexOf(subjects.get(i)),
	 * itemContent.get(itemHeader.indexOf(subjects.get(i))) + "<br/><b> " +
	 * predicates.get(i) + ": </b>" + objects.get(i).substring(1) + "."); } else
	 * { if (!topSubjects.contains(subjects.get(i) + ",")) topSubjects +=
	 * subjects.get(i) + ", "; itemHeader.add(subjects.get(i));
	 * itemContent.add("<b> " + predicates.get(i) + ": </b>" +
	 * objects.get(i).substring(1) + ".");
	 * 
	 * } } }
	 * 
	 * MultiZoneSnippet MZS = new MultiZoneSnippet(this, photos, links, sUrls,
	 * subjects, predicates, objects, itemHeader, itemContent);
	 * 
	 * return MZS; }
	 */

	private double getRankingPriority(Triplet t, Query q) {
		int max = 7;
		int RP = max;
		Selector SasS = new SimpleSelector(t.statement.getSubject(), null, (RDFNode) null);
		Selector SasO = new SimpleSelector(null, null, t.statement.getSubject());
		Selector OasO = new SimpleSelector(null, null, t.statement.getObject());
		Selector OasS = null;
		if (t.statement.getObject().isResource()) {
			OasS = new SimpleSelector(t.statement.getObject().asResource(), null, (RDFNode) null);// as
																									// subject
		}

		if (QuerySubGraph.contains(t.statement)) // this triple is in the
													// intersection of Q & Doc
			RP = 0;
		else {
			int Ssize = QuerySubGraph.listStatements(SasS).toList().size() + QuerySubGraph.listStatements(SasO).toList().size();
			int Osize = QuerySubGraph.listStatements(OasO).toList().size();
			if (OasS != null) {
				Osize += QuerySubGraph.listStatements(OasS).toList().size();
			}

			// S & o of this triple are in the intersection of Q & Doc
			if (Ssize > 0 && Osize > 0)
				RP = 1;
			else {
				// S & o of this triple are in the union of Q & Doc
				if ((allRankedResources.containsKey(t.statement.getSubject().getURI())) && (t.statement.getObject().isResource()) && (allRankedResources.containsKey(t.statement.getObject().asResource().getURI()))) {
					// S & o in the query
					if ((q.RelatedResLD.contains(t.statement.getSubject().getURI())) && (t.statement.getObject().isResource()) && (q.RelatedResLD.contains(t.statement.getObject().asResource().getURI()))) {
						RP = 2;
					} else
						RP = 3;
				} else {
					// S || o of this triple are in the intersection of Q & Doc
					if (Ssize > 0 || Osize > 0)
						RP = 4;
					else {
						// S || o of this triple are in the union of Q & Doc
						if ((allRankedResources.containsKey(t.statement.getSubject().getURI())) || ((t.statement.getObject().isResource()) && (allRankedResources.containsKey(t.statement.getObject().asResource().getURI())))) {
							// S || o in the query
							if ((q.RelatedResLD.contains(t.statement.getSubject().getURI())) || ((t.statement.getObject().isResource()) && (q.RelatedResLD.contains(t.statement.getObject().asResource().getURI())))) {
								RP = 5;
							} else
								RP = 6;
						} else {
							RP = 7;
						}
					}
				}
			}
		}
		return ((max - RP) * 1.0) / max;
	}

	public Map<String, Map<Triplet, Double>> triplesRanker(Query q) {
		Map<String, Map<Triplet, Double>> res = new HashMap<String, Map<Triplet, Double>>();
		for (Map.Entry<String, Model> entry : SubGraphs.entrySet()) {
			// System.err.println("For (" + entry.getKey() +
			// ") we get a subGraph: " + entry.getValue().size());

			res.put(entry.getKey(), clusterTriplesRanker(entry.getValue(), q));
		}
		return res;
	}

	public Map<Triplet, Double> clusterTriplesRanker(Model clusterGraph, Query q) {

		double a = 0.45;// subject importance
		double b = 0.45;// Object importance
		double c = 0.1;// Predicate

		HashMap<Triplet, Double> res = new HashMap<Triplet, Double>();

		DBcontroler db = new DBcontroler();
		StmtIterator stmts = clusterGraph.listStatements();
		Property typePredicate = fullGraph.createProperty("http://www.w3.org/1999/02/22-rdf-syntax-ns#type");
		int max = 10;
		System.out.println("----------------Ranking Triples--------------------");
		while (stmts.hasNext()) {

			Statement st = stmts.next();
			Triplet t = new Triplet();
			t.statement = st;

			// Subject, object Score
			Double SR = allRankedResources.get(t.statement.getSubject().getURI());
			Double OR = 0.0;

			if (t.statement.getObject().isResource()) {
				OR = allRankedResources.get(t.statement.getObject().asResource().getURI());
			}
			if (OR == null)
				OR = 0.0;
			if (SR == null)
				SR = 0.0;

			//Predicate Score		
			double predicateZscore = 0.0;
			try {
				Property p = t.statement.getPredicate();
				Selector typeSelector = new SimpleSelector(t.statement.getSubject(), typePredicate, (RDFNode) null);
				StmtIterator types = fullGraph.listStatements(typeSelector);
				//NodeIterator types = fullGraph.listObjectsOfProperty(typePredicate);
				int size = 0;
				while (types.hasNext()) {
					String type = types.next().getObject().asResource().getURI();
					double z = db.getPredicatZScoreByType(p.getURI(), type);
					predicateZscore += z;
					//if (z > 0)
					size++;
					//System.out.println("found a zscore for (" + p.getURI() + ") with type (" + type + ") equals to: " + z);
					if (size > max)
						break;
				}

				if (size == 0)
					System.out.println("no types for (" + t.statement.getSubject() + " - " + p.getURI() + ")");
				else
					predicateZscore = predicateZscore / size;

			} catch (Exception e) {
				e.printStackTrace();
			}

			t.score = a * SR + b * OR + c * predicateZscore;
			//System.err.println("SR: " + SR + "OR: " + OR + "predicateZscore: " + predicateZscore);
			res.put(t, t.score);

		}
		System.out.println("----------------End of Ranking Triples--------------------");

		Map<Triplet, Double> output = sortTriples(res);

		return output;
	}

	private TreeMap<String, Model> mergeSubgraphs(TreeMap<String, Model> A) {
		// System.out.println(A.firstKey());
		TreeMap<String, Model> Res = new TreeMap<String, Model>();
		Map.Entry<String, Model> FE = A.pollFirstEntry();// (Entry<String,
															// Model>)
															// A.entrySet().toArray()[0];

		if (FE == null) {
			Res = null;
		} else {
			String FEKey = FE.getKey();
			Model FEModel = FE.getValue();
			TreeMap<String, Model> M = mergeSubgraphs(A);
			if (M != null)
				for (Map.Entry<String, Model> outerEntry : M.entrySet()) {
					Model model1 = outerEntry.getValue();
					if (/* model1.intersection(FE.getValue()) */RDFManager.intersection(model1, FE.getValue()).size() > 0) {
						FEKey = FEKey + " " + outerEntry.getKey();
						FEModel = FEModel.add(model1);
					} else {
						Res.put(outerEntry.getKey(), outerEntry.getValue());
					}
				}
			Res.put(FEKey, FEModel);

		}

		return Res;
	}

	public int ResourceScoreCounter;
	public int TripletsScoreCounter;

	public Map<String, Double> resourcesRanker(double inImportance, double outImportance, double freqImportance, double qImportance, double SimilarityImportance) {
		HashMap<String, Double> Res = new HashMap<String, Double>();
		ArrayList<String> allRess = new ArrayList<String>();
		allRess = allRes;
		System.out.println("----------------Ranking Reources in (" + content.getTitle() + ")--------------------");

		for (String entry : allRess) {
			ResourceScoreThread thread = new ResourceScoreThread(entry, this);
			thread.run();
			if (thread.score != null)
				if (Res.get(thread.URI) != null)
					Res.put(thread.URI, Res.get(thread.URI) + thread.score);
				else
					Res.put(thread.URI, thread.score);
		}
		System.out.println("----------------END Ranking Reources--------------------");

		Map<String, Double> output = sortByValueDouble(Res);

		return output;
	}

	public Map<String, Double> resourcesSelector(double percent, double inImportance, double outImportance, double freqImportance, double qImportance, double SimilarityImportance) {
		HashMap<String, Double> Res = new HashMap<String, Double>();
		ArrayList<String> allRess = new ArrayList<String>();
		allRess = allRes;
		System.out.println("----------------Ranking Reources in (" + content.getTitle() + ")--------------------");

		for (String entry : allRess) {
			int inValue = 0;
			int outValue = 0;
			int freqValue = 1;
			int qValue = 0;
			int saValue = 0;
			double SimilarityScore = 0.0;

			String URI;
			try {
				URI uri = new URI(entry);
				URI = uri.toASCIIString();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				URI = entry;
			}
			System.out.println("----------------Calculate Score for (" + URI + ")--------------------");
			System.out.println("----------------Calculate values--------------------");
			if (topicInRelatedResources.get(URI) != null)
				inValue = topicInRelatedResources.get(URI) + 0;
			if (topicOutRelatedResources.get(URI) != null)
				outValue = topicOutRelatedResources.get(URI) + 0;
			if (topicRelatedFreqResources.get(URI) != null)
				freqValue = topicRelatedFreqResources.get(URI) + 0;
			if (freqValue != 0)
				SimilarityScore = Calculate.getSimilarityScore(triplets, maxSimilarity, URI) / freqValue;
			if (queryRelatedResources.contains(URI))
				qValue = 1;

			if (SemanticAnnotationResources.contains(URI))
				saValue = 1;

			System.out.println(URI + ": Selector Values:	in " + (inValue * 1.0) + ", out " + (outValue * 1.0) + " ,SimilarityScore	" + (SimilarityScore) + " ,freqValue	" + (freqValue * 1.0) + " ,qValue	" + qValue);
			if ((freqValue == 1) && (qValue == 0) && (saValue == 0))
				;
			else {
				double score = (inImportance * inValue) + (outImportance * outValue) + (SimilarityImportance * SimilarityScore) + (freqImportance * freqValue) + (qImportance * qValue) + (semanticAnnotationImportance * saValue);
				System.out.println(URI + ": Score: " + score);
				if (Res.get(URI) != null)
					Res.put(URI, Res.get(URI) + score);
				else
					Res.put(URI, score);
			}
		}
		System.out.println("----------------END Ranking Reources--------------------");

		// long n = Math.round(percent * Res.entrySet().size());
		// Map<String, Double> output = sortByValueDoubleWithLimit(Res, n);
		Map<String, Double> output = sortByValueDouble(Res);

		return output;
	}

	public void generateTopicRelatedResByInOut() {
		Map<String, Integer> RSsIn = new HashMap<String, Integer>();
		Map<String, Integer> RSsOut = new HashMap<String, Integer>();
		Map<String, Integer> RSsAll = new HashMap<String, Integer>();
		allRes = new ArrayList<String>();// all subjects

		ResIterator ss = fullGraph.listSubjects();
		MaximumOut = 1;
		MaximumIn = 1;
		while (ss.hasNext()) {
			Resource resource = (Resource) ss.next();
			Selector selector1 = new SimpleSelector(null, null, resource);// as
																			// object
			Selector selector2 = new SimpleSelector(resource, null, (RDFNode) null);// as
																					// subject
			int asObjectSize = fullGraph.listStatements(selector1).toList().size();
			int asSubjectSize = fullGraph.listStatements(selector2).toList().size();

			String RURI;
			try {
				URI uri = new URI(resource.getURI());
				RURI = uri.toASCIIString();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				RURI = resource.getURI();
			}
			if (asObjectSize > MaximumIn)
				MaximumIn = asObjectSize;
			if (asSubjectSize > MaximumOut)
				MaximumOut = asSubjectSize;
			RSsIn.put(RURI, asObjectSize);
			RSsOut.put(RURI, asSubjectSize);
			RSsAll.put(RURI, asSubjectSize + asObjectSize);
			if (!allRes.contains(RURI))
				allRes.add(RURI);
		}

		topicInRelatedResources = sortByValueInt(RSsIn);
		topicOutRelatedResources = sortByValueInt(RSsOut);
		topicAllRelatedResources = sortByValueInt(RSsAll);

		NodeIterator oo = fullGraph.listObjects();
		while (oo.hasNext()) {
			RDFNode obj = (RDFNode) oo.next();
			if (obj.isResource())
				if (!allRes.contains(obj.asResource().getURI()))
					allRes.add(obj.asResource().getURI());
		}

	}

	public void generateTopicRelatedResByFreq() {
		HashMap<String, Integer> FreqRSs = new HashMap<String, Integer>();
		Iterator<EnsenDBpediaResource> iterator = triplets.iterator();

		while (iterator.hasNext()) {
			EnsenDBpediaResource DBR = iterator.next();
			String URI;
			try {
				URI uri = new URI(DBR.getFullUri());
				URI = uri.toASCIIString();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				URI = DBR.getFullUri();
			}

			if (!FreqRSs.containsKey(URI)) {
				FreqRSs.put(URI, 1);
				if (MaximumFreq < 1)
					MaximumFreq = 1;
			} else {
				int oldFreq = FreqRSs.get(URI);
				FreqRSs.remove(URI);
				FreqRSs.put(URI, oldFreq + 1);
				if (MaximumFreq < (oldFreq + 1))
					MaximumFreq = oldFreq + 1;
			}
		}

		topicRelatedFreqResources = sortByValueInt(FreqRSs);
	}

	public Map<String, Model> generateTopicSubgraphs(Map<String, Double> allRankedResources) {
		SubGraphs = new TreeMap<String, Model>();
		int counter = 0;
		for (Map.Entry<String, Double> entry : allRankedResources.entrySet()) {
			Model sGraph = RDFManager.getSubgraph(fullGraph, entry.getKey());
			SubGraphs.put(entry.getKey(), sGraph);
		}
		return SubGraphs;
	}

	public Model createTopicSubgraph() {
		TopicSubGraph = RDFManager.createRDFModel();
		StmtIterator sts = fullGraph.listStatements();
		while (sts.hasNext()) {
			Statement s = sts.next();

			try {
				// topic
				if ((topicInRelatedResources.keySet().contains(s.getSubject().getURI())) || (topicOutRelatedResources.keySet().contains(s.getSubject().getURI()))) {
					if ((s.getObject().isResource()) && ((topicInRelatedResources.keySet().contains(s.getObject().asResource().getURI())) || (topicOutRelatedResources.keySet().contains(s.getObject().asResource().getURI())))) {
						TopicSubGraph.add(s);
					} else {
						TopicSubGraph.add(TopicSubGraph.createStatement(TopicSubGraph.createResource("http://ensen.org/resource/MainTopic"), TopicSubGraph.createProperty("http://ensen.org/property/has-a"), s.getSubject().getURI()));

					}
				} else {
					if ((s.getObject().isResource()) && (topicInRelatedResources.keySet().contains(s.getObject().asResource().getURI()) || topicOutRelatedResources.keySet().contains(s.getObject().asResource().getURI()))) {
						TopicSubGraph.add(TopicSubGraph.createStatement(TopicSubGraph.createResource("http://ensen.org/resource/MainTopic"), TopicSubGraph.createProperty("http://ensen.org/property/has-a"), s.getObject().asResource().getURI()));
					} else {
						;
					}
				}
			} catch (Exception e1) {
				e1.printStackTrace();

			}
		}
		return TopicSubGraph;
	}

	public Model createQuerySubgraph(Query Q) {
		QuerySubGraph = RDFManager.createRDFModel();
		StmtIterator sts = fullGraph.listStatements();
		while (sts.hasNext()) {
			Statement s = sts.next();
			try {
				if (Q.RelatedResDoc.contains(s.getSubject().getURI())) {

					if ((s.getObject().isResource()) && (Q.RelatedResDoc.contains(s.getObject().asResource().getURI()))) {
						QuerySubGraph.add(s);
					} else {
						QuerySubGraph.add(QuerySubGraph.createStatement(QuerySubGraph.createResource("http://ensen.org/resource/MainTopic"), QuerySubGraph.createProperty("http://ensen.org/property/has-a"), s.getSubject().getURI()));
					}
				} else {
					if ((s.getObject().isResource()) && (Q.RelatedResDoc.contains(s.getObject().asResource().getURI()))) {
						QuerySubGraph.add(QuerySubGraph.createStatement(QuerySubGraph.createResource("http://ensen.org/resource/MainTopic"), QuerySubGraph.createProperty("http://ensen.org/property/has-a"), s.getObject().asResource().getURI()));

					} else {
						;
					}
				}
			} catch (Exception e1) {

			}
		}

		return QuerySubGraph;
	}

	public Model createIntrestedSubgraph() {
		intrestedModel = RDFManager.createRDFModel();
		intrestedModel.add(TopicSubGraph);
		intrestedModel.add(QuerySubGraph);
		return intrestedModel;
	}

	public String printAllResources() {
		String HTML = "<aside><ul>";
		for (String s : allRes) {
			HTML += "<li><a href=\"" + s + "\">" + s + "</a> </li>";
		}
		return HTML + "</ul></aside>";
	}

	public String printSemanticAnnotations() {
		String HTML = "<aside><ul>";
		StmtIterator annotationSemanticsts = annotationSemantic.listStatements();
		while (annotationSemanticsts.hasNext()) {
			try {

				Statement s = annotationSemanticsts.next();
				String sTxt = "";
				if (s.getSubject().getLocalName() == null)
					if (s.getSubject().getURI() == null)
						sTxt = s.getSubject().toString();
					else
						sTxt = s.getSubject().getURI();
				else
					sTxt = s.getSubject().getLocalName();

				if (sTxt.trim() == "")
					sTxt = "No Text";
				HTML += "<li>(<a href=\"" + s.getSubject().getURI() + "\" >" + sTxt + "</a>)-->(<a href=\"" + s.getPredicate().getURI() + "\" >" + s.getPredicate().getLocalName() + "</a>)-->(" + s.getObject() + "</a>)</li>";
			} catch (Exception e) {
				// TODO: handle exception
			}
		}
		return HTML + "</ul></aside>";
	}

	public String printTopicInRelatedResources() {
		String HTML = "<aside><ul>";
		Map<String, Integer> tempTRR = topicInRelatedResources/* .descendingMap() */;
		if (topicInRelatedResources != null) {
			int limit = 0;
			for (Map.Entry<String, Integer> R : tempTRR.entrySet()) {

				HTML += "<li><a href=\"" + R.getKey() + "\">" + R.getValue() + "- " + R.getKey() + "</a></li>";
			}
		}
		return HTML + "</ul></aside>";
	}

	public String printTopicOutRelatedResources() {
		String HTML = "<aside><ul>";
		Map<String, Integer> tempTRR = topicOutRelatedResources/*
																 * .descendingMap
																 * ()
																 */;
		if (topicOutRelatedResources != null) {
			int limit = 0;
			for (Map.Entry<String, Integer> R : tempTRR.entrySet()) {

				HTML += "<li><a href=\"" + R.getKey() + "\">" + R.getValue() + "- " + R.getKey() + "</a></li>";
			}
		}
		return HTML + "</ul></aside>";
	}

	public String printTopicAllRelatedResources() {
		String HTML = "<aside><ul>";
		Map<String, Integer> tempTRR = topicAllRelatedResources/*
																 * .
																 * descendingMap
																 * ()
																 */;
		if (topicAllRelatedResources != null) {
			int limit = 0;
			for (Map.Entry<String, Integer> R : tempTRR.entrySet()) {
				HTML += "<li><a href=\"" + R.getKey() + "\">" + R.getValue() + "- " + R.getKey() + "</a></li>";
			}
		}
		return HTML + "</ul></aside>";
	}

	public String printFreqRelatedResources() {
		String HTML = "<aside><ul>";
		Map<String, Integer> tempTRR1 = topicRelatedFreqResources/*
																	* .descendingMap
																	* ()
																	*/;
		if (topicRelatedFreqResources != null) {
			for (Map.Entry<String, Integer> R : tempTRR1.entrySet()) {
				HTML += "<li><a href=\"" + R.getKey() + "\">" + R.getValue() + "- " + R.getKey() + "</a></li>";
			}
		}
		return HTML + "</ul></aside>";
	}

	private Map sortByValueInt(Map map) {
		List list = new LinkedList(map.entrySet());
		Collections.sort(list, new Comparator() {
			public int compare(Object o1, Object o2) {
				// System.err.println(((Map.Entry) (o1)).getKey() + " : " +
				// ((Map.Entry) (o2)).getKey());
				if (((Integer) ((Map.Entry) (o1)).getValue()) > ((Integer) (((Map.Entry) (o2)).getValue())))
					return -1;
				else if (((Integer) ((Map.Entry) (o1)).getValue()) < ((Integer) (((Map.Entry) (o2)).getValue())))
					return 1;
				else
					return 0;

			}
		});

		Map result = new LinkedHashMap();
		for (Iterator it = list.iterator(); it.hasNext();) {
			Map.Entry entry = (Map.Entry) it.next();
			result.put(entry.getKey(), entry.getValue());
		}
		return result;
	}

	private Map sortByValueDouble(Map map) {
		List list = new LinkedList(map.entrySet());
		Collections.sort(list, new Comparator() {
			public int compare(Object o1, Object o2) {
				if (((Double) ((Map.Entry) (o1)).getValue()) > ((Double) (((Map.Entry) (o2)).getValue())))
					return -1;
				else if (((Double) ((Map.Entry) (o1)).getValue()) < ((Double) (((Map.Entry) (o2)).getValue())))
					return 1;
				else
					return 0;
			}
		});

		Map result = new LinkedHashMap();
		for (Iterator it = list.iterator(); it.hasNext();) {
			Map.Entry entry = (Map.Entry) it.next();
			result.put(entry.getKey(), entry.getValue());
		}
		return result;
	}

	private Map sortByValueDoubleWithLimit(Map map, long n) {
		List list = new LinkedList(map.entrySet());
		Collections.sort(list, new Comparator() {
			public int compare(Object o1, Object o2) {
				if (((Double) ((Map.Entry) (o1)).getValue()) > ((Double) (((Map.Entry) (o2)).getValue())))
					return -1;
				else if (((Double) ((Map.Entry) (o1)).getValue()) < ((Double) (((Map.Entry) (o2)).getValue())))
					return 1;
				else
					return 0;
			}
		});

		Map result = new LinkedHashMap();
		int counter = 0;
		for (Iterator it = list.iterator(); it.hasNext();) {
			if (counter < n) {
				Map.Entry entry = (Map.Entry) it.next();
				result.put(entry.getKey(), entry.getValue());
				counter++;
			} else
				break;
		}
		return result;
	}

	private TreeMap<Triplet, Double> sortTriples(HashMap<Triplet, Double> map) {
		TriplesValueComparator bvc = new TriplesValueComparator(map);
		TreeMap<Triplet, Double> sorted_map = new TreeMap<Triplet, Double>(bvc);
		sorted_map.putAll(map);
		return sorted_map;
	}

	public static void main(String[] args) {
		HashMap<Triplet, Double> map = new HashMap<Triplet, Double>();
		Triplet t1 = new Triplet();
		t1.type = 1;
		Triplet t2 = new Triplet();
		t2.type = 2;
		Triplet t3 = new Triplet();
		t3.type = 3;
		map.put(t1, 334.96024869531396);
		map.put(t2, 334.59503998234874);
		map.put(t3, 334.047001233697);

		TriplesValueComparator bvc = new TriplesValueComparator(map);
		TreeMap<Triplet, Double> sorted_map = new TreeMap<Triplet, Double>(bvc);
		sorted_map.putAll(map);
		for (Map.Entry<Triplet, Double> entry : map.entrySet()) {
			System.err.println(entry.getKey().type + " : " + entry.getValue());
		}
		System.err.println("------------------");
		for (Map.Entry<Triplet, Double> entry : sorted_map.entrySet()) {
			System.err.println(entry.getKey().type + " : " + entry.getValue());
		}

	}

}

class TriplesValueComparator implements Comparator<Triplet> {

	Map<Triplet, Double> base;

	public TriplesValueComparator(Map<Triplet, Double> base) {
		this.base = base;
	}

	// Note: this comparator imposes orderings that are inconsistent with
	// equals.
	public int compare(Triplet a, Triplet b) {
		if (base.get(a) >= base.get(b)) {
			return -1;
		} else {
			return 1;
		} // returning 0 would merge keys
	}
}