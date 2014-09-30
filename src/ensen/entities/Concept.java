package ensen.entities;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import javax.imageio.ImageIO;

import org.apache.commons.lang.WordUtils;

import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.NodeIterator;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.SimpleSelector;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;

import ensen.controler.RDFManager;
import ensen.ml.MlControler;
import ensen.util.Printer;
import ensen.util.PropertiesManager;

public class Concept {
	final int minNofTriples = 5;
	final int maxNofTriples = 30;
	final int objectLimit = 50;//max length of an object
	public String name;
	public String URI = "";
	public String image;
	public String image2;
	public String wikiURL;
	public int id;
	public String Descs = "";
	public ArrayList<String> linkedParagraphs;
	//public ArrayList<String> predicates;
	//public ArrayList<String> objects;

	public Document doc;
	public String mainPh;
	public String mainPh2;
	public String mainPhHtml;
	public String mainPhHtml2;
	public ArrayList<Group> groups;
	public ArrayList<Statement> triples;
	public String abstractTxt = "";
	public double maxScore;//score for best sentence 
	public double maxScore2 = 0;;//score for 2 second best sentence 
	public double score;//score from tensor (from groups: the sum)
	public double generalScore = 0.0;//used in concepts ranking
	public boolean annotated = false;
	int phId = 0;
	public String mapLink = null;
	public ArrayList<String> photos;
	private ArrayList<String> notToBeUsedObjects;
	public String phsFeatures = "";

	public Concept(String uri, Document d) {
		doc = d;
		URI = uri;
		linkedParagraphs = new ArrayList<String>();
		/*predicates = new ArrayList<String>();
		objects = new ArrayList<String>();*/
		groups = new ArrayList<>();
		buildTitle();
		buildIcon();
		buildWiki();
	}

	public void buildSnippet() {
		findPh(false);
		cutPh();
		mainPhHtml = highlightMainSentence(mainPh);
		if (mainPh2 != "")
			mainPhHtml2 = highlightMainSentence(mainPh2);

		findMap();
		findPhotos();
		buildDesc();
	}

	private void cutPh() {
		int limit = Integer.parseInt(PropertiesManager.getProperty("maxMainConceptSentenceLength"));
		if (mainPh.length() > limit)
			mainPh = mainPh.substring(0, limit) + "...";
		if (mainPh2.length() > limit)
			mainPh2 = mainPh2.substring(0, limit) + "...";
	}

	private void findPhotos() {
		Resource s = doc.fullGraph.getResource(URI);
		photos = new ArrayList<>();
		notToBeUsedObjects = new ArrayList<>();
		StmtIterator sts = doc.fullGraph.listStatements(new SimpleSelector(s, null, (RDFNode) null));
		if (sts != null)
			while (sts.hasNext()) {
				Statement st = sts.next();
				String phurl = st.getObject().toString();
				if (doc.pridecatForPhotos.contains(st.getPredicate().asResource().getURI())) {
					if (!photos.contains(phurl) && testPhoto(phurl))
						photos.add(phurl);
					notToBeUsedObjects.add(phurl);
				}

				if (phurl.endsWith(".svg") || phurl.endsWith(".png") || phurl.endsWith(".jpg") || phurl.endsWith(".gif")) {
					if (!photos.contains(phurl) && testPhoto(phurl))
						photos.add(phurl);
					notToBeUsedObjects.add(phurl);
				}
			}

		photos.remove(image);

	}

	private boolean testPhoto(String src) {
		if (src.contains("http")) {
			URL url;
			try {
				url = new URL(src);
				HttpURLConnection huc = (HttpURLConnection) url.openConnection();
				huc.setRequestMethod("GET"); //OR  huc.setRequestMethod ("HEAD"); 
				huc.connect();
				int code = huc.getResponseCode();
				if ((code != 404) && (code != 500)) {
					BufferedImage bimg = ImageIO.read(url);
					bimg.getWidth();
					bimg.getHeight();
					return true;
				}
			} catch (MalformedURLException e) {
				return false;
			} catch (IOException e) {
				return false;
			} catch (Exception e) {
				return false;
			}
		}
		return false;
	}

	private void findMap() {
		try {
			Resource subject = doc.fullGraph.getResource(URI);
			if (subject != null) {
				/*long zoom = 8;
				int value = 0;
				Property areaProperty = doc.fullGraph.getProperty("http://dbpedia.org/ontology/PopulatedPlace/areaTotal");
				if (areaProperty != null) {
					NodeIterator objs = doc.fullGraph.listObjectsOfProperty(subject, areaProperty);
					if (objs.hasNext()) {
						RDFNode obj = objs.next();
						value = RDFManager.getIntValue(obj);
					}
				}

				if (value == 0) {
					areaProperty = doc.fullGraph.getProperty("http://dbpedia.org/ontology/areaTotal");
					if (areaProperty != null) {
						NodeIterator objs = doc.fullGraph.listObjectsOfProperty(subject, areaProperty);
						if (objs.hasNext()) {
							RDFNode obj = objs.next();
							value = RDFManager.getIntValue(obj);
						}
					}
				}
				if (value == 0) {
					areaProperty = doc.fullGraph.getProperty("http://dbpedia.org/property/areaKm");
					if (areaProperty != null) {
						NodeIterator objs = doc.fullGraph.listObjectsOfProperty(subject, areaProperty);
						if (objs.hasNext()) {
							RDFNode obj = objs.next();
							value = RDFManager.getIntValue(obj);
						}
					}
				}
				if (value == 0) {
					areaProperty = doc.fullGraph.getProperty("http://dbpedia.org/property/areaTotalKm");
					if (areaProperty != null) {
						NodeIterator objs = doc.fullGraph.listObjectsOfProperty(subject, areaProperty);
						if (objs.hasNext()) {
							RDFNode obj = objs.next();
							value = RDFManager.getIntValue(obj);
						}
					}
				}

				if (value > 0) {
						//syria area =186475.0 , damascus area=105.0 AND syria zoom=5 , damascus zoom=12 ==> zoom= (8-N)*2.3 +1 where N is the power of 10					
						System.err.println(value);
					int n = (int) (Math.log10(value) + 1);
						System.err.println(n);
						zoom = Math.round((8 - n) * 2.3 + 1);
						System.err.println(zoom);
						if (zoom <= 1)
							zoom = 8;
				}
				 */
				String preLink = "https://maps.googleapis.com/maps/api/staticmap?center=";
				//String postLink = "&zoom=" + zoom + "&size=250x250&sensor=false";
				String postLink = "&size=250x250&sensor=false";

				/*Property latProperty = doc.fullGraph.getProperty("http://www.w3.org/2003/01/geo/wgs84_pos#lat");
				Property longProperty = doc.fullGraph.getProperty("http://www.w3.org/2003/01/geo/wgs84_pos#long");
				if (latProperty != null && longProperty != null) {
					NodeIterator objs1 = doc.fullGraph.listObjectsOfProperty(subject, latProperty);
					NodeIterator objs2 = doc.fullGraph.listObjectsOfProperty(subject, longProperty);
					if (objs1.hasNext() && objs2.hasNext()) {
						mapLink = preLink + RDFManager.getDoubleValue(objs1.next()) + "," + RDFManager.getDoubleValue(objs2.next()) + postLink;
					}
				}*//*else {
					Property geometryProperty = doc.fullGraph.getProperty("http://www.w3.org/2003/01/geo/wgs84_pos#geometry");
					if (geometryProperty == null)
						geometryProperty = doc.fullGraph.getProperty("http://www.georss.org/georss/point");
					}*/
				if (mapLink == null) {
					String mapTypes = "http://dbpedia.org/property/place http://dbpedia.org/ontology/Place http://schema.org/Place http://dbpedia.org/class/yago/YagoGeoEntity http://umbel.org/umbel/rc/PopulatedPlace	http://dbpedia.org/ontology/PopulatedPlace http://www.w3.org/2003/01/geo/wgs84_pos#lat http://www.w3.org/2003/01/geo/wgs84_pos#long http://www.georss.org/georss/point";
					Property typeProperty = doc.fullGraph.getProperty("http://www.w3.org/1999/02/22-rdf-syntax-ns#type");
					if (typeProperty != null) {
						NodeIterator objs = doc.fullGraph.listObjectsOfProperty(subject, typeProperty);
						while (objs.hasNext()) {
							String type = objs.next().toString();
							if (mapTypes.contains(type)) {
								mapLink = preLink + name + postLink;
								break;
							}
						}
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public String getAbstract() {
		int max = Integer.parseInt(PropertiesManager.getProperty("maxShowenAbstractLength"));
		return highlightMainSentence(abstractTxt.substring(0, Math.min(abstractTxt.length(), max)));
	}

	private String highlightMainSentence(String in) {
		//String HTMLStopWord = "class li ul a style fieldset legend span div";
		String out = in;
		out = replace(out, name.trim(), name.trim(), "mainRes", this.URI);//e.g.   "Marie Antoinette"
		out = replace(out, name.replace(" ", "-").trim(), name.replace(" ", "-").trim(), "mainRes", this.URI);//e.g.   "Marie-Antoinette"
		out = replace(out, name.replace(" ", "_").trim(), name.replace(" ", "_").trim(), "mainRes", this.URI);//e.g.   "Marie_Antoinette"
		out = replace(out, name.replace(" ", ".").trim(), name.replace(" ", ".").trim(), "mainRes", this.URI);//e.g.   "Marie.Antoinette"
		out = replace(out, name.toLowerCase().trim(), name.toLowerCase().trim(), "mainRes", this.URI);//e.g.   "marie antoinette"

		for (String term1 : name.split(" ")) {
			if (term1.length() > 3)
				out = replace(out, term1.trim(), term1.trim(), "mainRes", "");
		}

		for (EnsenDBpediaResource res : doc.q.Resources) {
			out = replace(out, res.originalText, res.originalText, "qRes", res.getFullUri());
		}

		for (String term1 : doc.q.ExtendedText.split(" ")) {
			if (term1.length() > 3)
				out = replace(out, term1.trim(), term1.trim(), "qRes", "");
		}

		ArrayList<EnsenDBpediaResource> ress = doc.resourcesInSentenses.get(phId);
		for (EnsenDBpediaResource res : ress) {
			if (res.getFullUri().equals(this.URI))
				out = replace(out, res.originalText, res.originalText, "mainRes", this.URI);
			else
				out = replace(out, res.originalText, res.originalText, "resInSent", res.getFullUri());
		}

		out = out.replaceAll("  ", " ");
		return out;
	}

	private String replace(String text, String from, String to, String className, String link) {
		String pre = "";
		String pre_rep = "";
		String post = "";
		String post_rep = "";

		if (text.indexOf(from) == 0) {
			pre = "";
			pre_rep = "<a target='blank' href='" + link + "' class='" + className + "'>";
		} else {
			if (text.indexOf(from) > 0 && ":,.('\"[{|-*+/\\=@^#~&".contains("" + text.charAt(text.indexOf(from) - 1))) {
				pre = text.charAt(text.indexOf(from) - 1) + "";
				pre_rep = text.charAt(text.indexOf(from) - 1) + "<a target='blank' href='" + link + "' class='" + className + "'>";
			} else {
				pre = " ";
				pre_rep = "<a target='blank' href='" + link + "' class='" + className + "'>&nbsp;";
			}
		}

		if (text.indexOf(from) == text.length() - from.length()) {
			post = "";
			post_rep = "</a>";
		} else {
			if (text.indexOf(from) > 0 && ";!?:,.('\"[{|-*+/\\=@^#~&".contains("" + text.charAt(text.indexOf(from) + from.length()))) {
				post = text.charAt(text.indexOf(from) + from.length()) + "";
				post_rep = "</a>" + text.charAt(text.indexOf(from) + from.length());
			} else {
				post = " ";
				post_rep = "&nbsp;</a>";
			}
		}

		String FinalText = "";
		String[] fSplit = text.split("</a>");
		for (String s : fSplit) {
			if (!s.contains("<a"))
				FinalText += s.replace(pre + from + post, pre_rep + to + post_rep);
			else {
				String[] sSplit = s.split("<a");
				if (!sSplit[0].contains(">")) {
					FinalText += sSplit[0].replace(pre + from + post, pre_rep + to + post_rep) + "<a" + sSplit[1] + "</a>";
				} else {
					FinalText += "<a" + sSplit[0] + "</a>" + sSplit[1];
				}
			}
		}

		//text = text.replace(pre + from + post, pre_rep + to + post_rep);

		return FinalText;
	}

	public void calculateScore(Map<String, Double> resourcesFoundedByClib, Double maxScore) {
		annotated = true;
		if (doc.url.contains(URI))
			annotated = false;
		else {
			Double scoreFromSVD = resourcesFoundedByClib.get(URI);
			//System.err.println(name + ": scoreFromSVD  = " + scoreFromSVD);
			if (scoreFromSVD == null) { //not ranked by first algo  
				scoreFromSVD = 0.0;
			}
			if (maxScore == 0.0)
				maxScore = 1.0;

			//first algo ranking
			generalScore = 100.0 * scoreFromSVD / maxScore;// normalized [0-100]
			//System.out.println(name + ": first algo  = " + generalScore);
			/*
								//groups i.e. second algo
								if (groups != null) {
									double groupsScore = (groups.size() / 2) * 100.0 / 20;// (/2) to make it low effect , and 20: 10 as auth + 10 as hubs is the num max of groups,normalized [0-100]					
									generalScore += groupsScore;
										//System.err.println(name + ": groupsScore  = " + groupsScore);
								}

								//in graph
								Model m = RDFManager.createRDFModel();
								m.add(doc.graphWithoutLiteral);
								if (m.listSubjects().toList().size() > 10) {//small!graphs give bad results i.e.100% for bad concepts
									m = m.removeAll(m.getResource(doc.url), null, null);
									StmtIterator sts = m.listStatements(new SimpleSelector(null, null, m.getResource(URI)));
									double size = sts.toList().size() * 100.0 / m.listSubjects().toList().size();
									generalScore += size;
										//System.err.println(name + ": graph  = " + size);
								}

								//shared with query
								int qRes = 0;
								for (EnsenDBpediaResource r : doc.q.Resources) {
									if (r.getFullUri().equals(URI)) {
										qRes += 100;
									}
									//linkedtoquery
									if (doc.graphWithoutLiteral.listStatements(new SimpleSelector(doc.graphWithoutLiteral.getResource(r.getFullUri()), null, doc.graphWithoutLiteral.getResource(URI))).toList().size() > 0)
										qRes += 25;
									if (doc.graphWithoutLiteral.listStatements(new SimpleSelector(doc.graphWithoutLiteral.getResource(URI), null, doc.graphWithoutLiteral.getResource(r.getFullUri()))).toList().size() > 0)
										qRes += 25;

								}
								generalScore += qRes;
									//System.err.println(name + ": qRes  = " + qRes);

								int qTerms = 0;
								for (String term1 : doc.q.Text.split(" ")) {
									if (term1.trim().length() > 3 && name.toLowerCase().contains(term1.trim().toLowerCase()))
										qTerms += 50;
								}
								generalScore += qTerms;
									//System.err.println(name + ": qTerms  = " + qTerms);
									//System.err.println(name + ": generalScore  = " + generalScore);
									 
			*/
			//by spotlight
			/*for (EnsenDBpediaResource r : doc.Resources) {
				if (r.getFullUri().equals(URI)) {
					annotated = true;
					generalScore += (r.similarityScore + 1) * 3;// 3 is the weight of this factor
				}
			}*/

			/*if (URI != null)
				for (Group g : groups) {
					if (g != null) {
						if (g.auths != null)
							if (g.auths.get(URI) != null)
								generalScore += g.auths.get(URI).score;
						if (g.hubs != null)
							if (g.hubs.get(URI) != null)
								generalScore += g.hubs.get(URI).score;
					}
				}*/
		}
	}

	private void buildWiki() {
		wikiURL = RDFManager.getWikiPage(doc.fullGraph, doc.fullGraph.createResource(URI));
	}

	private void buildDesc() {
		triples = getTriplesUsingGroups();
		if (triples.size() < minNofTriples) {
			Resource s = doc.fullGraph.getResource(URI);
			StmtIterator sts = doc.fullGraph.listStatements(new SimpleSelector(s, null, (RDFNode) null));
			if (sts != null)
				while (sts.hasNext()) {
					Statement st = sts.next();
					if (!notToBeUsedObjects.contains(st.getObject().toString()))
						if (!doc.pridecatNotImportant.contains(st.getPredicate().asResource().getURI())) {
							if (!st.getPredicate().asResource().getURI().contains("http://ensen.org/"))
								triples.add(st);
							if (triples.size() > maxNofTriples)
								break;
						}
				}
		}

		String[] descriptionByGroup = new String[groups.size() + 1];
		Model m = RDFManager.createRDFModel();
		Map<String, HashMap<String, String>> desc = RDFManager.generateStringMapFromTriples(triples);
		for (Map.Entry<String, HashMap<String, String>> entry : desc.entrySet()) {
			for (Map.Entry<String, String> entry2 : entry.getValue().entrySet()) {
				String objectValues = entry2.getValue();
				if (objectValues.length() > 2) {
					//	if (entry.getKey().equals(URI))
					//conceptDesc += "<b>" + WordUtils.capitalize(entry2.getKey()) + ":</b> " + objectValues + ".<br /> ";
					//	else
					int index = getGroupIDByPredicate(entry2.getKey());
					if (index < 0)
						index = groups.size();
					if (descriptionByGroup[index] == null)
						descriptionByGroup[index] = "";

					String subject = RDFManager.getLabel(doc.fullGraph, m.createResource(entry.getKey()));

					descriptionByGroup[index] += "<b>" + subject + " " + WordUtils.capitalize(entry2.getKey()) + ":</b> " + objectValues.replace("_", " ") + ".<br /> ";
				}
			}
		}

		for (int index = 0; index < descriptionByGroup.length; index++) {
			String s = descriptionByGroup[index];
			if (s != null && s.trim() != "")
				Descs += "<div class='group" + index + " groupOfTriples'>" + s + "</div>";

		}

	}

	private int getGroupIDByPredicate(String pIn) {
		int index = 0;
		Model m = RDFManager.createRDFModel();
		for (Group g : groups) {
			for (String P : g.predicates.keySet()) {
				String p = RDFManager.SplitPredicate(m.createProperty(P));
				if (p.toLowerCase().contains(pIn.toLowerCase().trim())) {
					//	System.err.println(p + " vs " + pIn + " --> " + p.toLowerCase().contains(pIn.toLowerCase().trim()) + ": " + index);
					return index;
				}
			}
			index++;
		}
		return -1;
	}

	private ArrayList<Statement> getTriplesUsingGroups() {
		ArrayList<Statement> res = new ArrayList<>();
		Resource Subject = doc.fullGraph.getResource(URI);
		for (Group g : groups) {
			if (g != null) {
				//statements S->p->auth
				if (g.auths != null)
					for (String auth : g.auths.keySet()) {
						for (String p : g.predicates.keySet()) {
							Property pp = doc.fullGraph.getProperty(p);
							StmtIterator sts = null;
							try {
								Resource o = doc.fullGraph.getResource(auth);
								sts = doc.fullGraph.listStatements(new SimpleSelector(Subject, pp, o));
							} catch (Exception e) {
								try {
									Literal o = doc.fullGraph.createLiteral(auth);
									sts = doc.fullGraph.listStatements(new SimpleSelector(Subject, pp, o));
								} catch (Exception e2) {

								}
							}
							if (sts != null)
								while (sts.hasNext()) {
									res.add(sts.next());
								}
						}
					}

				//statements hub->p->o
				if (g.hubs != null)
					for (String hub : g.hubs.keySet()) {
						for (String p : g.predicates.keySet()) {
							Property pp = doc.fullGraph.getProperty(p);
							Resource s = doc.fullGraph.getResource(hub);
							StmtIterator sts = doc.fullGraph.listStatements(new SimpleSelector(s, pp, Subject.asNode()));
							if (sts != null)
								while (sts.hasNext()) {
									res.add(sts.next());
								}
						}
					}
			}
		}
		return res;
	}

	private void buildIcon() {
		ArrayList<String> images = RDFManager.getImagesForResource(doc, URI);
		if (images.size() > 0) {
			image = images.get(0);
			if (images.size() > 1)
				image2 = images.get(1);
			else
				image2 = null;
		} else {
			image2 = PropertiesManager.getProperty("defaultThumb");
		}
	}

	/*
	 * Forced: we can't find a main sentence so we re-apply find ph, with ignorance of (if it is bad sentence or not)
	 */
	public void findPh(boolean forced) {
		Printer.logToFile(doc.Rank + "-findConceptPH", "findPh for : (" + name + ") with forced=" + forced);
		mainPh = "";
		mainPh2 = "";
		double max = 0.0;
		double max2 = 0.0;
		HashMap<Integer, Double> map = new HashMap<Integer, Double>();
		Comparator bvc = new ValueComparator(map);
		TreeMap<Integer, Double> sorted_map = new TreeMap<Integer, Double>(bvc);
		for (String onePh : phsFeatures.split("\n")) {
			String[] values = onePh.split(",");
			int phId = Integer.parseInt(values[0].split("ph")[1]);
			//get ml score
			MlControler mlctr = new MlControler();
			double[] mlPhResults = mlctr.evaluateForConceptSentence(values, "bayes");
			System.err.println("ph: " + doc.sentences.get(phId) + " Score: " + mlPhResults[1]);
			map.put(phId, mlPhResults[1]);
			}
		sorted_map.putAll(map);


		Entry<Integer, Double> first = sorted_map.pollFirstEntry();
		if (first != null) {
		max = first.getValue();
		mainPh = doc.sentences.get(first.getKey());
		}
		Entry<Integer, Double> second = sorted_map.pollFirstEntry();
		if (second != null) {
			max2 = second.getValue();
			mainPh2 = doc.sentences.get(second.getKey());
		}
		System.out.println("Selected 1 Sentence: (" + max + ") : " + mainPh);
		System.out.println("Selected 2 Sentence: (" + max2 + ") : " + mainPh2);

	}

	private void buildTitle() {
		Resource r = doc.fullGraph.getResource(URI);
		name = RDFManager.getLabel(doc.fullGraph, r);

	}

}

class ValueComparator implements Comparator<Integer> {

	Map<Integer, Double> base;

	public ValueComparator(Map<Integer, Double> base) {
		this.base = base;
	}

	// Note: this comparator imposes orderings that are inconsistent with equals.    
	public int compare(Integer a, Integer b) {
		if (base.get(a) >= base.get(b)) {
			return -1;
		} else {
			return 1;
		} // returning 0 would merge keys
	}
}
