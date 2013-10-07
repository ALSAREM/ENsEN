package ensen.entities;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;

import org.apache.commons.lang.WordUtils;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.NodeIterator;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;

import ensen.control.PropertiesManager;
import ensen.control.RDFManager;

public class MultiZoneSnippet {
	public String HTML = "";
	public String mediaPart = "";
	public String modifiedSnippetPart = "";
	public String newSnippetPart = "";
	public String linksPart = "";
	public String oldSnippet = "";
	private ArrayList<EnsenDBpediaResource> mainInfoSubjectsInstances;

	public MultiZoneSnippet(Document D, Map<Triplet, Double> cluster) {

		oldSnippet = D.content.getSnippet();
		//annotate the old snippet
		//List<EnsenDBpediaResource> resources = RDFizer.rdfizeText(oldSnippet);
		ArrayList<String> forEnhancingEntities = new ArrayList<String>();
		for (EnsenDBpediaResource EDR : D.oldSnippetResources) {
			if (!forEnhancingEntities.contains(EDR.getFullUri()))
				forEnhancingEntities.add(EDR.getFullUri());
		}

		ArrayList<Triplet> enhanceInfo = new ArrayList<Triplet>();
		ArrayList<Triplet> mainInfo = new ArrayList<Triplet>();
		ArrayList<Triplet> moreInfo = new ArrayList<Triplet>();
		ArrayList<Triplet> links = new ArrayList<Triplet>();
		ArrayList<Triplet> photos = new ArrayList<Triplet>();
		ArrayList<String> usedConcepts = new ArrayList<String>();

		int oldSnippetLimit = 1000;
		int oldSnippetCounter = 0;
		int mainInfoLimit = 400;
		int mainInfoCounter = 0;
		int moreInfoShowenLimit = 350;
		int moreInfoShowenCounter = 0;
		int moreInfoHiddenLimit = 2500;
		int moreInfoHiddenCounter = 0;
		int linksLimit = 250;
		int linksCounter = 0;
		int phLinkLimit = 200;
		int objectLimit = 100;
		int moreInfoOneConceptLimit = 500;
		HashMap<String, Integer> usedPredicatesInMoreInfo = new HashMap<String, Integer>();
		HashMap<String, Integer> usedSubjectsInMoreInfo = new HashMap<String, Integer>();

		long start = System.currentTimeMillis();

		for (Map.Entry<Triplet, Double> entry : cluster.entrySet()) {
			Resource S = entry.getKey().statement.getSubject();
			Property P = entry.getKey().statement.getPredicate();
			RDFNode O = entry.getKey().statement.getObject();
			String s = S.getLocalName().replace("_", " ");
			String p = SplitPredicate(P);
			//System.out.println("Triplet:" + s + " --> " + p + " --> " + O.toString());

			/* media Part*/
			if (!D.pridecatStopList.contains(P.getURI()))
				if (D.pridecatForPhotos.contains(P.getURI())) {
					photos.add(entry.getKey());
				} else
				/* link Part*/
				if (D.pridecatForLinks.contains(P.getURI())) {
					if (linksCounter < linksLimit) {
						links.add(entry.getKey());
						linksCounter += s.length() + p.length() + 2;
					}

				} else {
					/*enhance Info*/
					if (forEnhancingEntities.contains(S.getURI()) && (oldSnippetCounter < oldSnippetLimit)) {
						//if ((oldSnippet.toLowerCase().contains(S.getLocalName().replace("_", " ").toLowerCase())) ) {
						enhanceInfo.add(entry.getKey());
						oldSnippetCounter += p.length() + getObjectLength(O);
						usedConcepts.add(S.getURI());

					} else {
						if (!O.toString().contains("com.hp.hpl.jena.datatypes.BaseDatatype$TypedValue@")) {
							/* Main Info */
							if (mainInfoCounter < mainInfoLimit) {
								mainInfo.add(entry.getKey());
								usedConcepts.add(S.getURI());
								mainInfoCounter += s.length() + p.length() + getObjectLength(O) + 4;//4 spaces
							} else {
								/* More Info  */
								if ((moreInfoShowenCounter < moreInfoShowenLimit) && (moreInfoHiddenCounter < moreInfoHiddenLimit)) {
									//if (!usedConcepts.contains(S.getURI())) {

									Integer sleng = usedSubjectsInMoreInfo.get(s);
									if (sleng == null)
										sleng = 0;

									if (sleng < moreInfoOneConceptLimit) {
										moreInfo.add(entry.getKey());
										moreInfoShowenCounter += s.length() + 2;
										moreInfoHiddenCounter += p.length();
										Integer leng = usedPredicatesInMoreInfo.get(p);
										if (leng == null)
											leng = 0;
										moreInfoHiddenCounter += Math.min(leng + getObjectLength(O), objectLimit);
										usedPredicatesInMoreInfo.put(p, leng + getObjectLength(O));
										usedSubjectsInMoreInfo.put(s, sleng + leng + getObjectLength(O));
									}
									//}
								}
							}
						}
					}
				}
			//if ((mediaPart != "") && (oldSnippetCounter >= oldSnippetLimit) && (mainInfoCounter >= mainInfoLimit) && (linksCounter >= linksLimit) && ((moreInfoShowenCounter >= moreInfoShowenLimit) || (moreInfoHiddenCounter >= moreInfoHiddenLimit)))
			//break;

		}

		long buildData = System.currentTimeMillis();

		/* media Part */

		mediaPart = buildMediaPart(photos, D);
		long mediaPartt = System.currentTimeMillis();

		/*enhance Info*/
		modifiedSnippetPart = buildEnhancedSnippetPart(enhanceInfo, /*resources,*/D);
		long modifiedSnippetPartt = System.currentTimeMillis();

		/*new Snippet Part*/

		newSnippetPart = "<div class='newSnippetPart'> ";
		newSnippetPart += buildMainInfoPart(mainInfo, D) + "<br/>";
		newSnippetPart += buildMoreInfoPart(moreInfo, mainInfo, enhanceInfo, D, objectLimit);
		newSnippetPart += "</div>";
		long newSnippetPartt = System.currentTimeMillis();

		/* links Part */
		linksPart = buildLinksPart(links);
		long linksPartt = System.currentTimeMillis();

		/* main Paragraph*/
		String mainPh = getMainPh(usedConcepts, D);

		HTML = "<div class='snippet'> ";
		HTML += "	<div class='sn_header'>";
		HTML += "		<div class='sn_title'> ";
		HTML += "			<a href='" + D.url + "' target='_blank'>" + D.content.getTitle() + "</a>";
		HTML += "		</div> ";
		HTML += "	</div>";
		HTML += "	<div class='sn_body'>" + mediaPart + "";
		HTML += "		<div class='sn_text'> ";
		HTML += "			<div class='sn_modified_snippet'> " + modifiedSnippetPart;
		if (!mainPh.trim().isEmpty())
			HTML += "				<br/><span class='mainPh'>“" + mainPh + "„</span><hr/>";
		HTML += "<hr/>";
		HTML += newSnippetPart;
		HTML += "			</div>";
		HTML += "		</div>";
		HTML += "		<div class='sn_links'>" + linksPart + "</div>";
		HTML += "	</div>";
		HTML += "</div>  ";
	}

	private int getObjectLength(RDFNode O) {
		if (O.isResource())
			return O.asResource().getLocalName().length();
		else
			return Math.min(O.toString().length(), Integer.parseInt(PropertiesManager.getProperty("maxTextInOneObject")));

	}

	private String SplitPredicate(RDFNode P) {
		try {
			String tempP = P.asResource().getLocalName().replace("_", " ");
			String p = "";
			String[] ps = tempP.split("[A-Z]");

			/* Split Predicate*/
			int index = 0;
			for (int j = 0; j < ps.length; j++) {
				index += ps[j].length();
				char ch = ' ';
				if (index < tempP.length())
					ch = tempP.charAt(index);
				p += ps[j] + " " + ch;
				index += 1;
			}

			return p;
		} catch (Exception e) {
			return P.toString();
		}
	}

	private String buildMediaPart(ArrayList<Triplet> mediaInfo, Document D) {
		String mediaPart = "";
		String firstOne = "";

		int counter = 0;
		int triplesCounter = 0;
		int maxPhoto = Integer.parseInt(PropertiesManager.getProperty("maxPhotoForOneRes"));
		if (mediaInfo.size() > 0) {
			boolean first = true;//need to first
			for (Triplet t : mediaInfo) {
				triplesCounter++;
				String s = t.statement.getSubject().getLocalName().replace("_", " ");
				String p = SplitPredicate(t.statement.getPredicate());

				try {
					URL url = new URL(t.statement.getObject().toString());
					HttpURLConnection huc = (HttpURLConnection) url.openConnection();
					huc.setRequestMethod("GET"); //OR  huc.setRequestMethod ("HEAD"); 
					huc.connect();
					int code = huc.getResponseCode();

					if ((code != 404) && (code != 500)) {
						BufferedImage bimg = ImageIO.read(url);
						int width = bimg.getWidth();
						int height = bimg.getHeight();
						if (first) {
							if ((!D.usedImages.contains(url.toString())) || (triplesCounter == mediaInfo.size())) {
								firstOne = " class='firstPhoto' ";
								D.usedImages.add(url.toString());
								first = false;
							} else
								firstOne = " class='hiddenPhoto' ";

						} else {
							firstOne = " class='hiddenPhoto' ";
						}

						mediaPart += "<li id='doc" + D.Rank + "li" + counter + "'><a href='" + url + "' rel='prettyPhoto" + D.Rank + "[ajax]' title='" + s + " " + p + "'><span id='doc" + D.Rank + "span" + counter + "' " + firstOne + " ><img id='doc" + D.Rank + "img" + counter + "' src='" + url + "'  /></span></a></li>";

						counter++;
					}
				} catch (Exception e) {

				}
				if (counter > maxPhoto)
					break;

			}
		}

		String JS = "<script>";
		JS += " $(document).ready(function() {  buildGallary('" + D.Rank + "');});</script>";

		/* media part */
		if (mediaPart != "") {
			mediaPart = JS + "<div class='sn_media'><ul class='gallery clearfix' id='ul" + D.Rank + "' >" + mediaPart + "</ul></div>";
		} else if (D.mainImage != null && !D.mainImage.isEmpty())
			mediaPart = JS + "<div class='sn_media'><ul class='gallery clearfix' ><li><a href='" + D.mainImage + "' rel='prettyPhoto" + D.Rank + "[ajax]' title='" + D.content.getTitle() + "'><img src='" + D.mainImage + "' width='150' /></a></li></ul></div>";

		return mediaPart;
	}

	private String getMainPh(ArrayList<String> usedConcepts, Document D) {
		String mainPh = "";
		String phQ = "";
		//System.out.println("usedConcepts: " + usedConcepts);
		//Get phs
		for (EnsenDBpediaResource entry : mainInfoSubjectsInstances) {
			if (!phQ.contains(entry.originalText + "|"))
				phQ += entry.originalText + "|";
		}
		if (phQ.length() > 1)
			phQ = phQ.substring(0, phQ.length() - 1);
		//System.out.println("phQ: " + phQ);

		ArrayList<String> phs = findParagraphsByRegex(D.text, phQ);
		//System.out.println("phs: " + phs);

		//Rank
		int maxLength = 150;
		int minLength = 40;
		int maxScore = 0;
		for (String ph : phs) {
			if (ph.length() < maxLength) {
				int score = 0;
				ArrayList<String> mainInfoSubjectsInstancesOriginalText = new ArrayList<String>();
				for (EnsenDBpediaResource entry : mainInfoSubjectsInstances) {
					if (!mainInfoSubjectsInstancesOriginalText.contains(entry.originalText))
						mainInfoSubjectsInstancesOriginalText.add(entry.originalText);
				}
				for (String entry : mainInfoSubjectsInstancesOriginalText) {
					if (ph.toLowerCase().contains(entry.toLowerCase())) {
						score++;
					}
					ph = ph.replace(entry, "<b>" + entry + "</b>");
				}

				//System.out.println("Score (" + score + ") for Ph: " + ph);
				if (score > maxScore) {
					maxScore = score;
					mainPh = ph;
				} else if (score == maxScore) {
					if (ph.length() < mainPh.length()) {
						maxScore = score;
						mainPh = ph;
					}
				} else if (mainPh.length() < minLength) {
					maxScore = score;
					mainPh = ph;
				}
			}
		}

		return mainPh;
	}

	private String buildEnhancedSnippetPart(ArrayList<Triplet> enhanceInfo, Document D) {
		String EnhancedOldSnippet = "<span class='modifiedSnippetPart'>";

		Map<String, HashMap<String, String>> strings = generateStringMap(enhanceInfo); //<s,Ps> ,  Ps= <p,o>

		int currentOffset = 0;
		int counter = 0;

		for (EnsenDBpediaResource Resource : D.oldSnippetResources) {
			EnhancedOldSnippet += D.content.getSnippet().substring(currentOffset, Math.min(D.content.getSnippet().length(), Resource.offset));
			if (Resource.offset >= D.content.getSnippet().length()) {
				break;
			} else {
				String oneSubjectTextDesc = "";
				for (Map.Entry<String, HashMap<String, String>> entry : strings.entrySet()) {
					if (entry.getKey().contains(Resource.getFullUri())) {
						for (Map.Entry<String, String> entry2 : entry.getValue().entrySet()) {
							oneSubjectTextDesc += "<b>" + entry2.getKey() + "</b> " + entry2.getValue() + ", ";
						}
					}
				}
				if (oneSubjectTextDesc != "") {
					oneSubjectTextDesc = "<span onclick=\"showHide('D" + D.Rank + "Res" + counter + "')\" class=\"plusinfoClick\" style=\"cursor:pointer\" title=\"Click here for more informations\">" + Resource.originalText + "</span> <span id='D" + D.Rank + "Res" + counter + "' class='plusinfoText' style='display: none;'><img src='../images/Info.png' alt='Information' height='14' width='14' /> " + oneSubjectTextDesc + " </span>";
					counter++;
				} else
					oneSubjectTextDesc = Resource.originalText + " ";

				EnhancedOldSnippet += oneSubjectTextDesc;
				currentOffset = Resource.offset + Resource.originalText.length() + 1;
			}
		}

		EnhancedOldSnippet += "</span>";

		return EnhancedOldSnippet;
	}

	private String buildEnhancedSnippetPartByTextMatching(ArrayList<Triplet> enhanceInfo, Document D) {
		int currentOffset = 0;
		String EnhancedOldSnippet = "<div class='modifiedSnippetPart'>" + oldSnippet;
		Map<String, HashMap<String, String>> strings = generateStringMap(enhanceInfo); //<s,Ps> Ps= <p,o>		
		Model m = RDFManager.createRDFModel();
		//System.err.println(strings);
		for (Map.Entry<String, HashMap<String, String>> entry : strings.entrySet()) {
			String s = m.createResource(entry.getKey()).getLocalName().replace("_", " ");
			HashMap<String, String> desc = strings.get(s);
			String textDesc = "";
			if (desc != null) {
				for (Map.Entry<String, String> entry2 : desc.entrySet()) {
					textDesc += "<b>" + entry2.getKey() + "</b> " + entry2.getValue() + ", ";
				}
				int Sindex = EnhancedOldSnippet.indexOf(s);
				if (textDesc != "") {
					textDesc = "<span onclick=\"toggleInfo('D" + D.Rank + "Res" + Sindex + "')\" class=\"plusinfoClick\" style=\"cursor:pointer\" title=\"Click here for more informations\">" + s + "</span> <span id='D" + D.Rank + "Res" + Sindex + "' class='plusinfoText' style='display: none;'><img src='../images/Info.png' alt='Information' height='14' width='14' /> " + textDesc + " </span>";
				} else
					textDesc = s;
				EnhancedOldSnippet = EnhancedOldSnippet.replace(s, textDesc);
			}
		}
		EnhancedOldSnippet += "</div>";

		return EnhancedOldSnippet;
	}

	private String buildMainInfoPart(ArrayList<Triplet> mainInfo, Document D) {
		String mainInfoString = "<span class='snippetInfoTitle'>Main info:&nbsp;</span>";
		Map<String, HashMap<String, String>> strings = generateStringMap(mainInfo); //<s,Ps> Ps= <p,o>		
		Model m = RDFManager.createRDFModel();

		for (Map.Entry<String, HashMap<String, String>> entry : strings.entrySet()) {
			String textDesc = "";
			String s = m.createResource(entry.getKey()).getLocalName().replace("_", " ");

			for (Map.Entry<String, String> entry2 : entry.getValue().entrySet()) {
				textDesc += "<b >" + entry2.getKey() + "</b> " + entry2.getValue() + ", ";
			}
			//get wikipedia link
			Property wikipediaProperty = m.createProperty("http://xmlns.com/foaf/0.1/isPrimaryTopicOf");
			Resource subject = m.createResource(entry.getKey());
			String wiki = "";
			NodeIterator objs = D.fullGraph.listObjectsOfProperty(subject, wikipediaProperty);
			if (objs.hasNext())
				wiki = objs.next().toString();
			else {
				wikipediaProperty = m.createProperty("http://xmlns.com/foaf/0.1/primaryTopic");
				objs = D.fullGraph.listObjectsOfProperty(subject, wikipediaProperty);
				if (objs.hasNext())
					wiki = objs.next().toString();
			}
			mainInfoString += "<b style='color:black'>" + s + "</b> " + textDesc.substring(0, textDesc.length() - 2);
			if (wiki != "")
				mainInfoString += " <a href='" + wiki + "' target='_blank'><img src='../images/link.png' width='16px' height='16px'/></a>";
			mainInfoString += ".";
		}

		return mainInfoString;
	}

	private String buildMoreInfoPart(ArrayList<Triplet> moreInfo, ArrayList<Triplet> mainInfo, ArrayList<Triplet> enhanceInfo, Document D, int objectLimit) {
		//build more info subjects instances list		
		ArrayList<EnsenDBpediaResource> moreInfoSubjectsInstances = new ArrayList<EnsenDBpediaResource>();
		//build main concepts subjects instances
		mainInfoSubjectsInstances = new ArrayList<EnsenDBpediaResource>();
		for (EnsenDBpediaResource DBR : D.triplets) {
			for (Triplet t : moreInfo) {
				String s = t.statement.getSubject().getURI();
				//System.out.println("More: " + s + " VS " + DBR.getFullUri());
				if (DBR.getFullUri().contains(s)) {
					moreInfoSubjectsInstances.add(DBR);
					break;
				}
			}

			for (Triplet t : mainInfo) {
				String s = t.statement.getSubject().getURI();
				//System.out.println("Main: " + s + " VS " + DBR.getFullUri());
				if (DBR.getFullUri().contains(s)) {
					mainInfoSubjectsInstances.add(DBR);
					break;
				}
			}

			for (Triplet t : enhanceInfo) {
				String s = t.statement.getSubject().getURI();
				//System.out.println("Main: " + s + " VS " + DBR.getFullUri());
				if (DBR.getFullUri().contains(s)) {
					mainInfoSubjectsInstances.add(DBR);
					break;
				}
			}
		}

		//find paragraphs
		int maxDistance = 100;
		HashMap<String, String> moreInfoPhs = new HashMap<String, String>();
		for (EnsenDBpediaResource DBR1 : moreInfoSubjectsInstances) {
			ArrayList<String> phs = new ArrayList<String>();
			for (EnsenDBpediaResource DBR2 : mainInfoSubjectsInstances) {
				if ((DBR1.offset >= D.content.getSnippet().length()) && (DBR2.offset >= D.content.getSnippet().length()) && (DBR1.originalText != DBR2.originalText)) {
					int a = DBR2.offset - DBR1.offset;
					int b = DBR1.offset - DBR2.offset;
					String ph = null;

					if ((a > 0) && (a < maxDistance)) {
						ph = findParagraph((D.content.getSnippet() + D.text), DBR1, DBR2, D.content.getSnippet());
						if (ph != null)
							ph = ph.replace(DBR1.originalText, "<b>" + DBR1.originalText + "</b>").replace(DBR2.originalText, "<b>" + DBR2.originalText + "</b>");
					}

					if ((b > 0) && (b < maxDistance)) {
						ph = findParagraph((D.content.getSnippet() + D.text), DBR2, DBR1, D.content.getSnippet());
						if (ph != null)
							ph = ph.replace(DBR1.originalText, "<b>" + DBR1.originalText + "</b>").replace(DBR2.originalText, "<b>" + DBR2.originalText + "</b>");
					}

					if (ph != null)
						phs.add(ph);
				}
			}
			if (phs.size() > 0)
				moreInfoPhs.put(DBR1.getFullUri(), "“" + selectOnePh(phs, 250) + "„<br/> ");
		}

		//Build HTML
		String moreInfoString = "<ul class='enlarge'>";//<span class='snippetInfoTitle'>Related Concepts:</span>
		int toggleIndex = 0;

		Map<String, HashMap<String, String>> strings = generateStringMap(moreInfo); //<s,Ps> Ps= <p,o>		
		Model m = RDFManager.createRDFModel();

		for (Map.Entry<String, HashMap<String, String>> entry : strings.entrySet()) {
			Resource subject = m.createResource(entry.getKey());
			//Get name or label
			Property labelProperty = m.createProperty("http://www.w3.org/2000/01/rdf-schema#label");
			String s = subject.getLocalName().replace("_", " ");
			NodeIterator objs = D.fullGraph.listObjectsOfProperty(subject, labelProperty);
			if (objs.hasNext())
				try {
					s = objs.next().asLiteral().getString().replace("@en", "");
				} catch (Exception e) {

				}

			//get photo
			String photo = "../images/noPhoto.png";
			for (String pred : D.pridecatForPhotos) {
				Property mainPhotoProperty = m.createProperty(pred);
				objs = D.fullGraph.listObjectsOfProperty(subject, mainPhotoProperty);
				boolean ok = false;
				while (objs.hasNext()) {
					String src = objs.next().toString();
					URL url;
					try {
						url = new URL(src);
						HttpURLConnection huc = (HttpURLConnection) url.openConnection();
						huc.setRequestMethod("GET"); //OR  huc.setRequestMethod ("HEAD"); 
						huc.connect();
						int code = huc.getResponseCode();
						if ((code != 404) && (code != 500)) {
							BufferedImage bimg = ImageIO.read(url);
							int width = bimg.getWidth();
							int height = bimg.getHeight();
							photo = src;
							ok = true;
							break;
						}
					} catch (MalformedURLException e) {
					} catch (IOException e) {
					} catch (Exception e) {
					}
				}
				if (ok)
					break;

			}

			//get wikipedia link
			Property wikipediaProperty = m.createProperty("http://xmlns.com/foaf/0.1/isPrimaryTopicOf");
			String wiki = "";
			try {
				objs = D.fullGraph.listObjectsOfProperty(subject, wikipediaProperty);
				if (objs.hasNext())
					wiki = objs.next().toString();
				else {
					wikipediaProperty = m.createProperty("http://xmlns.com/foaf/0.1/primaryTopic");
					objs = D.fullGraph.listObjectsOfProperty(subject, wikipediaProperty);
					if (objs.hasNext())
						wiki = objs.next().toString();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			//build description			
			String textDesc = "";
			String ph = moreInfoPhs.get(entry.getKey());
			if (ph != null)
				textDesc = "<span class='linkedPh'>" + ph + "</span><hr>";
			for (Map.Entry<String, String> entry2 : entry.getValue().entrySet()) {
				String objectValues = entry2.getValue();
				if (objectValues.length() > objectLimit)
					objectValues = objectValues.substring(0, objectLimit);
				textDesc += "<b>" + WordUtils.capitalize(entry2.getKey()) + ":</b> " + objectValues + ".<br /> ";

			}
			moreInfoString += "<li alt='" + s + "'>";
			moreInfoString += "	<h5>";
			moreInfoString += "		<img src='" + photo + "' width='80px' height='50px' style='cursor:pointer' alt='Click here for more informations' onclick=\"showHide('D" + D.Rank + "ResPlus" + toggleIndex + "')\" />";
			moreInfoString += "		<br />";
			moreInfoString += "<div> " + s + "</div>";
			moreInfoString += "	</h5>";
			moreInfoString += "	<span id='D" + D.Rank + "ResPlus" + toggleIndex + "'  class='plusinfoText' style='display: none;'>";
			moreInfoString += "		<a href='" + wiki + "' target='_blank'>";
			moreInfoString += "			<img src='../images/link.png' width='16px' height='16px' style='float:right'/>";
			moreInfoString += "		</a>";
			moreInfoString += textDesc;
			moreInfoString += "	</span>";
			moreInfoString += "</li>";
			toggleIndex++;
		}
		moreInfoString += "</ul>";

		return moreInfoString;
	}

	private String selectOnePh(ArrayList<String> phs, int maxPhlimit) {
		String selected = "";
		String backupPh = "";
		if ((phs != null) && (phs.size() > 0))
			backupPh = phs.get(0);
		int maxPhlength = maxPhlimit;

		for (String ph : phs) {
			if (ph.length() < maxPhlength) {
				selected = ph;
				maxPhlength = selected.length();
			}
			if (ph.length() < backupPh.length()) {
				backupPh = ph;
			}
		}
		if (selected.trim().isEmpty())
			selected = backupPh;
		return selected;
	}

	private String findParagraph(String inText, EnsenDBpediaResource dBR1, EnsenDBpediaResource dBR2, String oldSnippet) {
		String section = inText.substring(dBR1.offset - Math.min(dBR1.offset - oldSnippet.length(), 500), dBR2.offset + Math.min(inText.length() - dBR2.offset, 500));

		String ph = findParagraphByRegex(section, dBR1.originalText);
		if (ph != null) {
			ph = findParagraphByRegex(ph, dBR2.originalText);
			if ((ph != null) && (ph.length() == section.length()))
				ph = inText.substring(dBR1.offset - Math.min(dBR1.offset, 30), dBR2.offset + Math.min(inText.length() - dBR2.offset, 30));
		}
		return ph;
	}

	private String findParagraphByRegex(String inText, String word) {
		Pattern p = Pattern.compile("(?i)([a-zA-Z0-9][^.?!]*?)?(?<!\\w)(" + word + ")(?!\\w)[^.?!]*?[.?!]");
		Matcher m = p.matcher(inText);
		while (m.find()) {
			return m.group();
		}
		return null;
	}

	private ArrayList<String> findParagraphsByRegex(String inText, String word) {
		Pattern p = Pattern.compile("(?i)([a-zA-Z0-9][^.?!]*?)?(?<!\\w)(" + word + ")(?!\\w)[^.?!]*?[.?!]");
		Matcher m = p.matcher(inText);
		ArrayList<String> phs = new ArrayList<String>();
		while (m.find()) {
			phs.add(m.group());
		}
		return phs;
	}

	private Map<String, HashMap<String, String>> generateStringMap(ArrayList<Triplet> Info) {
		Map<String, HashMap<String, String>> strings = new HashMap<String, HashMap<String, String>>(); //<s,Ps> Ps= <p,o>
		for (Triplet t : Info) {
			Resource S = t.statement.getSubject();
			Property P = t.statement.getPredicate();
			RDFNode O = t.statement.getObject();
			String s = S.getURI();// getLocalName().replace("_", " ");
			String p = SplitPredicate(P);
			String o = O.toString();
			if (O.isResource())
				o = SplitPredicate(O);//O.asResource().getLocalName().replace("_", " ");
			else {
				try {
					URL url = new URL(O.toString());
					o = "<a href='" + url + "'>" + url + "</a>";
				} catch (MalformedURLException e) {
					try {
						o = O.asLiteral().getString();
						o = o.substring(0, Math.min(o.length(), Integer.parseInt(PropertiesManager.getProperty("maxTextInOneObject"))));
					} catch (Exception e2) {
						o = "";
					}

				}
			}

			//union same subjects text
			if (strings.containsKey(s)) {
				HashMap<String, String> Ps = strings.get(s);
				//union same predicates
				if (Ps.containsKey(p)) {
					//union same object
					if (!Ps.get(p).contains(o))
						Ps.put(p, Ps.get(p).replace("And,", ",") + " And, " + o);
				} else {
					if (o != "")
						Ps.put(p, o);
				}
				strings.put(s, Ps);

			} else {
				if (o != "") {
					HashMap<String, String> Ps = new HashMap<String, String>();
					Ps.put(p, o);
					strings.put(s, Ps);
				}
			}

		}

		return strings;
	}

	public String getDomainName(String url) {
		try {
			URI uri = new URI(url);
			String domain = uri.getHost();
			return domain.startsWith("www.") ? domain.substring(4) : domain;
		} catch (Exception e) {
			return "";
		}
	}

	private String buildLinksPart(ArrayList<Triplet> links) {
		//wikipedia
		String wiki = "";

		for (Triplet t : links) {
			RDFNode O = t.statement.getObject();
			if (O.toString().contains("wikipedia.org")) {
				Resource S = t.statement.getSubject();
				Property P = t.statement.getPredicate();
				String s = S.getLocalName().replace("_", " ");
				String p = SplitPredicate(P).replace("is Primary Topic Of", "").replace("Primary Topic", "").replace("subject", "");
				wiki += "<a href='" + O.toString() + "' title='" + getDomainName(O.toString()) + "' class='extLink' >" + s + " " + p + "</a>, ";
			}
		}

		String LinksS = "";
		if (!wiki.trim().isEmpty()) {
			LinksS += "<div> <span class='snippetInfoTitle' >Wikipedia:</span><br/>";
			LinksS += wiki;
			LinksS += "</div>";
		}

		//else
		String elseL = "";
		for (Triplet t : links) {
			RDFNode O = t.statement.getObject();
			if (!O.toString().contains("wikipedia.org")) {
				if (!elseL.contains(O.toString())) {
					Resource S = t.statement.getSubject();
					Property P = t.statement.getPredicate();
					String s = S.getLocalName().replace("_", " ");
					String p = SplitPredicate(P).replace("is Primary Topic Of", "").replace("Primary Topic", "").replace("subject", "");
					p = p.replace("wiki Page External Link", ": " + getDomainName(O.toString()));
					elseL += "<a href='" + O.toString() + "' title='" + getDomainName(O.toString()) + "' class='extLink' >" + s + " " + p + "</a>, ";
				}
			}
		}

		if (!elseL.trim().isEmpty()) {
			LinksS += "<div> <span class='snippetInfoTitle'>Related Websites: </span> <br/>";
			LinksS += elseL;
			LinksS += "</div>";
		}

		return LinksS;
	}

	@Override
	public String toString() {
		return HTML;
	}

}
