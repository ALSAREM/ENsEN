package ensen.entities;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;

import cern.colt.matrix.impl.SparseDoubleMatrix3D;

import com.google.api.services.customsearch.model.Result;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.NodeIterator;
import com.hp.hpl.jena.rdf.model.RDFNode;

import de.l3s.boilerpipe.sax.HTMLDocument;
import ensen.util.HTMLhandler;
import ensen.util.PropertiesManager;
import ensen.util.Stemmer;

public class Document {
	static Logger log = Logger.getLogger(Document.class.getName());
	public Result content;
	public String text;
	public Model graph;
	public Model fullGraph;
	public Model conceptsMetainfoGraph;
	public List<EnsenDBpediaResource> Resources;
	public List<EnsenDBpediaResource> oldSnippetResources;
	public Model annotationSemantic;
	public ArrayList<String> SemanticAnnotationResources;
	public TreeMap<String, Double> ProjectionRes;
	public Double ProjectionSimRes;
	public ArrayList<String> linkUrls;
	public String newSnippet;
	public String url;
	public Set<String> queryRelatedResources = null;
	public ArrayList<String> allRes;
	public Model QuerySubGraph;
	public int Rank = -1;
	public HttpSession ensenSession = null;
	public Query q;
	Map<String, List<Map<String, Object>>> pageMap;
	public String mainImage = "";
	public double semanticAnnotationImportance;
	private String tags;
	public String html;
	public String usedText;
	public ArrayList<String> pridecatStopList;
	public ArrayList<String> pridecatForPhotos;
	public ArrayList<String> pridecatForLinks;
	public ArrayList<String> pridecatForWikis;
	public ArrayList<String> pridecatNotImportant;
	public HashMap<String, Integer> queryRelatedResourcesMap;
	public double qMean;
	public double qSD;
	public ArrayList<String> sentences;
	public ArrayList<String> notGoodSentences;
	public ArrayList<ArrayList<EnsenDBpediaResource>> resourcesInSentenses;
	public ArrayList<ArrayList<EnsenDBpediaResource>> resourcesInNotGoodSentenses;
	public ArrayList<Concept> concepts;
	public Map<String, Concept> conceptsMap;
	public int[][] graphStructure;
	public SparseDoubleMatrix3D Tensor;
	public Stemmer SBStemmer = new Stemmer();
	public String CgeneratedMatrix;
	public Map<String, Double> resourcesFoundedByClib;
	public Model graphWithoutLiteral;
	public String originalText;
	public HTMLDocument docHtml;
	public String internalFilename;
	public String internalFilePath;
	public String mlMainSentenceFeatures;
	public String mlConceptsFeatures;
	public Document() {

	}

	public Document(String url, int rank, Map<String, List<Map<String, Object>>> map) {
		init();
		pageMap = map;
		Rank = rank;
		this.url = url;
		//System.out.println("Load Content(HTML) for: " + url);
		// get content
		docHtml = HTMLhandler.getHTMLDoc(url);
		String docString = HTMLhandler.loadContent(url, docHtml);
		html = docString;

		// get Text only
		text = HTMLhandler.CleanContent(docString);
		originalText = text;
		// inforce by main content
		// text = HTMLhandler.inforceMainContentByBoiler(url, text);
		// get Annotation semantic
		/*SemanticAnnotationResources = new ArrayList<String>();
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
		}*/

		// transform meta tag to semantic annotations
		// from map to text
		/*tags = "";
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
							new URL(entry3.getValue().toString());
						} catch (Exception e) {
							if (!entry3.getValue().toString().contains("http"))
								tags += entry3.getValue().toString().replaceAll("[^a-zA-Z]", " ") + " ";
						}

					}
				}
			}*/

	}

	/*
	 * this method delete split Sentences in:
	 * a good sentences's list called Sentences
	 * a bad sentences's list called notGoodSentences
	 * 
	 * Bad= contains small subsentences or contains a lot of www, http ...
	 *  
	 */

	public void splitSentences() {
		//System.out.println("splitSentences");
		//System.out.println("Before: " + sentences.size() + ", " + resourcesInSentenses.size() + ", 0, 0");
		notGoodSentences = new ArrayList<String>();
		resourcesInNotGoodSentenses = new ArrayList<ArrayList<EnsenDBpediaResource>>();
		for (int i = 0; i < sentences.size(); i++) {
			String ph = sentences.get(i);

			//find how much small sub-sentences
			int smallSentence = 0;
			for (String s : ph.split(",")) {
				if (s.split(" ").length < 3) {
					smallSentence++;
				}
			}
			int badContent = 2 * (ph.split("http").length - 1);
			badContent += 2 * (ph.split("www.").length - 1);
			badContent += 2 * (ph.split(".html").length - 1);
			//at this point if it is a bad ph we don't use it
			int MaxSmallTextInSentence = Integer.parseInt(PropertiesManager.getProperty("MaxSmallTextInSentence"));
			if ((smallSentence + badContent) > MaxSmallTextInSentence) {
				notGoodSentences.add(ph);
				resourcesInNotGoodSentenses.add(resourcesInSentenses.get(i));
			}
		}
		sentences.removeAll(notGoodSentences);
		resourcesInSentenses.removeAll(resourcesInNotGoodSentenses);
		//System.out.println("After: " + sentences.size() + ", " + resourcesInSentenses.size() + ", " + notGoodSentences.size() + ", " + resourcesInNotGoodSentenses.size());
	}

	private void init() {
		pridecatStopList = new ArrayList<String>();
		pridecatForPhotos = new ArrayList<String>();
		pridecatForLinks = new ArrayList<String>();
		pridecatForWikis = new ArrayList<String>();
		pridecatNotImportant = new ArrayList<String>();

		pridecatStopList.add("http://www.w3.org/2002/07/owl#sameAs");
		pridecatStopList.add("http://dbpedia.org/property/wikiPageUsesTemplate");
		pridecatStopList.add("wordnet");

		pridecatForPhotos.add("http://dbpedia.org/ontology/thumbnail");
		pridecatForPhotos.add("http://xmlns.com/foaf/0.1/depiction");
		pridecatForPhotos.add("http://xmlns.com/foaf/0.1/thumbnail");
		pridecatForPhotos.add("http://purl.org/dc/elements/1.1/rights");
		pridecatForPhotos.add("http://dbpedia.org/property/caption");
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

		pridecatForWikis.add("http://dbpedia.org/ontology/wikiPageExternalLink");
		pridecatForWikis.add("http://xmlns.com/foaf/0.1/primaryTopic");
		pridecatForWikis.add("http://xmlns.com/foaf/0.1/isPrimaryTopicOf");
		pridecatForWikis.add("http://dbpedia.org/ontology/wikiPageWikiLink");

		//pridecatForLinks.add("http://dbpedia.org/property/hasPhotoCollection");
		//pridecatForLinks.add("http://purl.org/dc/elements/1.1/language");
		pridecatForLinks.add("http://dbpedia.org/property/website");
		pridecatForLinks.add("http://xmlns.com/foaf/0.1/homepage");
		pridecatForLinks.add("http://dbpedia.org/property/url");
		//pridecatForLinks.add("http://dbpedia.org/ontology/related");
		pridecatForLinks.add("http://dbpedia.org/property/web");
		pridecatForLinks.add("http://dbpedia.org/property/source");


		pridecatNotImportant.addAll(pridecatForWikis);
		pridecatNotImportant.add("http://dbpedia.org/ontology/wikiPageEditLink");
		pridecatNotImportant.add("http://dbpedia.org/ontology/wikiPageExtracted");
		pridecatNotImportant.add("http://dbpedia.org/ontology/wikiPageHistoryLink");
		pridecatNotImportant.add("http://dbpedia.org/ontology/wikiPageID");
		pridecatNotImportant.add("http://dbpedia.org/ontology/wikiPageModified");
		pridecatNotImportant.add("http://dbpedia.org/ontology/wikiPageRevisionID");
		pridecatNotImportant.add("http://dbpedia.org/ontology/wikiPageRevisionLink");
		pridecatNotImportant.add("http://dbpedia.org/ontology/wikiPageDisambiguates");
		pridecatNotImportant.add("http://dbpedia.org/ontology/wikiPageRedirects");
		pridecatNotImportant.add("http://dbpedia.org/property/wikiPageUsesTemplate");
		pridecatNotImportant.add("http://dbpedia.org/ontology/abstract");
		pridecatNotImportant.add("http://www.w3.org/2002/07/owl#sameAs");
		pridecatNotImportant.add("http://www.w3.org/ns/prov#wasDerivedFrom");
		pridecatNotImportant.add("http://www.w3.org/2000/01/rdf-schema#comment");
		pridecatNotImportant.add("http://dbpedia.org/ontology/meshId");
		/* map predicates*/
		pridecatNotImportant.add("http://www.w3.org/2003/01/geo/wgs84_pos#lat");
		pridecatNotImportant.add("http://www.w3.org/2003/01/geo/wgs84_pos#long");
		pridecatNotImportant.add("http://www.w3.org/2003/01/geo/wgs84_pos#geometry");
		pridecatNotImportant.add("http://www.georss.org/georss/point");

		/* photos */
		pridecatNotImportant.addAll(pridecatForPhotos);

	}


	public ArrayList<String> builResourcesStringList() {
		ArrayList<String> resources = new ArrayList<String>();
		NodeIterator os = fullGraph.listObjects();
		while (os.hasNext()) {
			RDFNode o = os.next();
			String uri = o.asResource().getURI();
			if (resources.indexOf(uri) == -1)
				resources.add(uri);
		}
		/*for (EnsenDBpediaResource r : Resources) {
			if (resources.indexOf(r.getFullUri()) == -1)
				resources.add(r.getFullUri());
		}*/
		return resources;
	}

	public EnsenDBpediaResource getFirstResourceWithUri(String uri) {
		for (EnsenDBpediaResource R : Resources) {
			if (R.getFullUri().contains(uri))
				return R;
		}
		return null;
	}

	public ArrayList<EnsenDBpediaResource> getAllResourceInstances(String uri) {
		ArrayList<EnsenDBpediaResource> res = new ArrayList<>();
		for (EnsenDBpediaResource R : Resources) {
			if (R.getFullUri().contains(uri))
				res.add(R);
		}
		return res;
	}

}
