package ensen.ml;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.api.services.customsearch.model.Result;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.SimpleSelector;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;

import ensen.controler.DBpediaSpotlightClient;
import ensen.controler.DocumentAnalyzer;
import ensen.controler.RDFManager;
import ensen.controler.Searcher;
import ensen.entities.Concept;
import ensen.entities.Document;
import ensen.entities.EnsenDBpediaResource;
import ensen.entities.Group;
import ensen.entities.Query;
import ensen.util.HTMLhandler;
import ensen.util.Printer;
import ensen.util.PropertiesManager;

public class MLManager {
	public static void main(String[] args) {

		//MLDataset();
		mergeDataWithEvaluation();
		//retriveHTMLfiles();
		//runOverHTMLfiles();
	}

	private static void runOverHTMLfiles() {
		System.out.println("Start ML generation");

		//read files.txt
		String files = HTMLhandler.readURLTxtasString("http://localhost:8080/ensenTensorielWeb/ml/files.txt");
		String out2 = "id,tct,cb,cn,cbl,cnl,ca,cp,qt,qr,mr,ar,arl,mrl,pt,cas,as,len,tt,se,ss,hl,sss,csc,cs,nch,d,php,fph,lph,selected\n";//concepts Sentences.csv
		String out3 = "id,qt,qr,qmr,mr,ar,cns,arl,mrl,pt,as,len,tt,se,ss,hl,sss,cs,nch,d,php,fph,lph,selected\n";//concepts Sentences.csv
		String Html1 = "<html>  <head>  <meta http-equiv=\"content-type\" content=\"text/html; charset=windows-1250\">  <meta name=\"generator\" content=\"PSPad editor, www.pspad.com\">  <script src='jquery.1.9.1.js'></script>   <script src='jquery-ui.js'></script>   <link rel='stylesheet' href='jquery-ui.css' type='text/css' charset=''utf-8' />   <link rel='stylesheet' href='jquery.ui.dialog.css' type='text/css' charset='utf-8' /><title></title>  </head>  <body>";
		Html1 += " <script type='text/javascript'>";
		Html1 += "$(function() {             $( '#dialog' ).dialog({                 autoOpen: false, height: 700,               width:750,               modal: true,               position:['middle',20],            });            $( '#opener' ).click(function() {             save();              $( '#dialog' ).dialog( 'open' );            });          });";
		Html1 += "var all1='';var all2='';";
		Html1 += "function save(){ all1='';all2='';     $('input:checkbox:checked').each(function()  {  all2+= $(this).val()+' '; }); var res2 = all2.split(' '); document.getElementById('res2').value = (res2.sort()+''); $('input:radio:checked').each(function()  {  if($(this).val()!='') all1+= $(this).val()+' '; }); var res1 = all1.split(' '); document.getElementById('res1').value = res1.sort(); }";
		Html1 += " </script>";
		Html1 += "  <div id='dialog' title='Results'>  <p>Please copy this to <b>\"main-sentences-evaluation.txt\"</b>:<textarea name='res1' id='res1' cols='35' rows='10'></textarea> </p>";
		Html1 += "   <p>Please copy this to <b>\"concepts-sentences-evaluation.txt\"</b>:<textarea name='res2' id='res2' cols='35' rows='10'></textarea> </p></div>";
		String Html2 = "  </body> </html>";
		/*int phCounter=0;
		int conceptCounter=0;*/
		String currQ = "";
		Query query = null;
		int index = 0;
		int i = 0;
		String[] lines = files.split("\n");
		for (int lineN = 0; lineN < lines.length; lineN++) {
			/*if (index == 2)
				break;*/
			String line = lines[lineN];
			if (line.contains("Q:"))//query
			{
				currQ = line.split(":")[1];
				System.out.println(currQ);
				query = new Query(currQ);
				query.id = index;
				i = 0;
				index++;

			} else { //file
				String id = line.split(",")[0];
				String title = line.split(",")[1];
				String url="http://localhost:8080/ensenTensorielWeb/ml/"+id+".html";
				System.out.println("Start id: " + id + ", title: " + title);
				DocumentAnalyzer analyzer = new DocumentAnalyzer();
				String out = "";//for HTML
				out += Html1;
				out += "<h1>Q" + query.id + " - " + currQ + "<a href='#' id='opener'>click here to save</a></h1></hr> <h2>Q" + query.id + "D" + i + " (<a target='blank' href='" + url + "'> " + title + "</a>)</h2>";
				analyzer.run(url, query, title, i);
				System.out.println("Q" + analyzer.Doci.q.id + ", doc" + analyzer.Doci.Rank + " analyzed");

				String[] results = generateConcepts(analyzer, id);

				if (results != null) {
					out += results[0];
					out2 += results[1];
					out3 += results[2];
					out += Html2;
					Printer.printToFile("ml/dataset/" + id + "-sentences.html", out);
					out = "";
				}

				System.out.println("finished id: " + id + ", title: " + title);
				System.out.println();
				System.out.println();
				System.out.println();
				i++;


			}
		}
		Printer.printToFile("ml/dataset/conceptSentences.csv", out2);
		Printer.printToFile("ml/dataset/mainSentences.csv", out3);
	}

	private static void retriveHTMLfiles() {
		Printer.createFolder("dataset", "ML_dataset_htmls");
		String pidouQs = "eliza turing test,dzogchen mahamudra,coltrane olatunji concert,systema martial art,bourbaki category theory,robin milner sml ocaml,spinoza substance,freud nietzsche,nietzsche sister,heidegger being and time,free will consciousness,david bohm thought,david bohm implicate order,wolfram new kind of science,computational irreducibility,jhana theravada,mont sainte victoire cezanne,first film louis auguste lumiere,star wars kurosawa influence,matrix jean baudrillard";
		String mazenQs = "Karl Marx paris,french revolution causes,playstation history,the godfather,stephen hawking theories,noam chomsky syria,whitney houston death,Android L,Maya cities,best paid soccer players,machine learning algorithms,Ramadan,Islamic State of Iraq and the Levant,Steve Jobs syrian,Supernatural powers,noah sons,Francis Albert Sinatra,SWOT analysis,Proxy server,string theory";
		String qs = pidouQs + "," + mazenQs;
		String out = "";
		int qindex = 0;
		for (String q : qs.split(",")) {
			out += "Q: " + q + "\n";
			Searcher S = new Searcher();
			List<Result> Results = S.search(q, 5);
			int dindex = 0;
			for (Result r : Results) {
				try {
				String fileName = "Q" + qindex + "D" + dindex;
				String htmlCode = HTMLhandler.readURLHTMLasString(r.getLink());
				if (htmlCode.trim() != "") {
					Printer.printToFile("dataset/ML_dataset_htmls/" + /*q + "/" +*/fileName + ".html", htmlCode);
						System.out.println(fileName);
					out += fileName + "," + r.getTitle().replace(",", " ") + "\n";
					dindex++;
				}
				} catch (Exception e) {

				}
			}

			qindex++;
		}
		Printer.printToFile("dataset/ML_dataset_htmls/files.txt", out);

	}

	static void MLDataset() {
		//String qs = "Karl Marx, French revolution,Bermuda Triangle,Pineal Gland,Gray wolf,PlayStation,Eurovision Song Contest,Flu,Gucci,the godfather";
		String qs = "Karl Marx, French revolution";
		int index = 0;
		String out2 = "id,tct,cb,cn,cbl,cnl,ca,cp,qt,qr,mr,ar,arl,mrl,pt,cas,as,len,tt,se,ss,hl,sss,csc,cs,selected\n";//concepts Sentences.csv
		String out3 = "id,qt,qr,qmr,mr,ar,cns,arl,mrl,pt,as,len,tt,se,ss,hl,sss,cs,selected\n";//concepts Sentences.csv
		for (String q : qs.split(",")) {
			Query query = new Query(q);
			query.id = index++;
			Searcher S = new Searcher();
			List<Result> Results = S.search(q, 5);
			ArrayList<Document> documents = new ArrayList<Document>();
			DocumentAnalyzer analyzer = new DocumentAnalyzer();
			int i = 0;
			String out = "";//for HTML

			String Html1 = "<html>  <head>  <meta http-equiv=\"content-type\" content=\"text/html; charset=windows-1250\">  <meta name=\"generator\" content=\"PSPad editor, www.pspad.com\">  <script src='jquery.1.9.1.js'></script>   <script src='jquery-ui.js'></script>   <link rel='stylesheet' href='jquery-ui.css' type='text/css' charset=''utf-8' />   <link rel='stylesheet' href='jquery.ui.dialog.css' type='text/css' charset='utf-8' /><title></title>  </head>  <body>";
			Html1 += " <script type='text/javascript'>";
			Html1 += "$(function() {             $( '#dialog' ).dialog({                 autoOpen: false, height: 700,               width:750,               modal: true,               position:['middle',20],            });            $( '#opener' ).click(function() {             save();              $( '#dialog' ).dialog( 'open' );            });          });";
			Html1 += "var all1='';var all2='';";
			Html1 += "function save(){ all1='';all2='';     $('input:checkbox:checked').each(function()  {  all2+= $(this).val()+' '; }); var res2 = all2.split(' '); document.getElementById('res2').value = (res2.sort()+''); $('input:radio:checked').each(function()  {  if($(this).val()!='') all1+= $(this).val()+' '; }); var res1 = all1.split(' '); document.getElementById('res1').value = res1.sort(); }";
			Html1 += " </script>";
			Html1 += "  <div id='dialog' title='Results'>  <p>Please copy this to <b>\"main-sentences-evaluation.txt\"</b>:<textarea name='res1' id='res1' cols='35' rows='10'></textarea> </p>";
			Html1 += "   <p>Please copy this to <b>\"concepts-sentences-evaluation.txt\"</b>:<textarea name='res2' id='res2' cols='35' rows='10'></textarea> </p></div>";

			String Html2 = "  </body> </html>";

			for (Result res : Results) {
				out += Html1;
				out += "<h1>Q" + query.id + " - " + q + "<a href='#' id='opener'>click here to save</a></h1></hr> <h2>Q" + query.id + "D" + i + " (<a target='blank' href='" + res.getLink() + "'> " + res.getTitle() + "</a>)</h2>";
				analyzer.run(res, i, query);
				String id = "Q" + query.id + "D" + analyzer.Doci.Rank;
				String[] results = generateConcepts(analyzer, id);
				if (results != null) {
					out += results[0];
					out2 += results[1];
					out3 += results[2];
					out += Html2;
					Printer.printToFile("ML/Q" + query.id + "D" + i + ".html", out);
					out = "";
				}
				i++;
			}

		}
		Printer.printToFile("ML/conceptSentences.csv", out2);
		Printer.printToFile("ML/mainSentences.csv", out3);
	}

	public static String[] generateConcepts(DocumentAnalyzer analyzer, String id) {

		if (analyzer.Doci.concepts != null && analyzer.Doci.concepts.size() > 0) {
			List<Concept> MainConcepts = analyzer.Doci.concepts.subList(0, Math.min(analyzer.Doci.concepts.size(), analyzer.MaxNumOfConcepts));
			String out = "";
			String outForConceptPh = "";// by concept
			String outForMainPh = "";// for main senatance			
			ArrayList<String> phs = analyzer.Doci.sentences;
			System.out.println("Start generating features for " + id + " with n of sentences: " + phs.size() + " and main concepts: " + MainConcepts.size());
			if (phs != null)
				for (int i = 0; i < phs.size(); i++) {
					try {
						String oneOutForMainPh = "";// for one ph for main senatance

						String ph = phs.get(i);

						String[] phTerms = ph.toLowerCase().split(" ");
						String stemedPh = "";
						for (String term : phTerms) {
							stemedPh += " " + analyzer.Doci.SBStemmer.stem(term);
						}

						ArrayList<EnsenDBpediaResource> ress = analyzer.Doci.resourcesInSentenses.get(i);
						//build csv for main sentence
						//id
						oneOutForMainPh += id + "ph" + i;

						//QT
						int qtScore = 0;
						for (String term1 : analyzer.Doci.q.Text.split(" ")) {
							if (term1.length() > 3 && ph.toLowerCase().contains(" " + term1.toLowerCase() + " "))
								qtScore += ph.toLowerCase().split(term1.toLowerCase()).length - 1;
						}
						oneOutForMainPh += "," + qtScore;

						//QR / QMR
						int qrScore = 0;
						int qmrScore = 0;
						int ptScore = 0;
						double csScore = 0.0;
						int cns = 0;
						double asScore = 0.0;
						for (EnsenDBpediaResource res : ress) {
							//cs sum of scores
							int rank = getRank(analyzer.Doci.concepts, res.getFullUri());
							if (rank > -1)
								csScore += (1 / (1.0 * rank));
							asScore += res.similarityScore;
							//q's resources
							if (DBpediaSpotlightClient.EnsenDBpediaResourceListContains(analyzer.Doci.q.Resources, res.getFullUri()) > -1) {
								qrScore++;
								for (int ccIndex = 0; ccIndex < MainConcepts.size(); ccIndex++) {
									if (MainConcepts.get(ccIndex).URI.contains(res.getFullUri())) {
										qmrScore++;
									}
								}

							}

							//groups
							for (Group g : analyzer.groups) {
								for (Entry<String, Double> pred : g.predicates.entrySet()) {
									String[] predicatWords = pred.getKey().replace("_", " ").split("(?=\\p{Upper})|/| ");
									for (int j = 0; j < predicatWords.length; j++) {
										if (predicatWords[j].length() > 3 && ph.toLowerCase().contains(predicatWords[j].toLowerCase())) {
											ptScore++;
										}
									}
								}
							}

							//couble of concepts
							Model graph = analyzer.Doci.fullGraph;
							StmtIterator stms = graph.listStatements(new SimpleSelector(graph.getResource(res.getFullUri()), null, (RDFNode) null));
							while (stms.hasNext()) {
								Statement next = stms.next();
								if (next.getObject().isResource()) {
									if (DBpediaSpotlightClient.EnsenDBpediaResourceListContains(ress, next.getObject().asResource().getURI()) > -1)
										cns++;
								}

							}
						}
						oneOutForMainPh += "," + qrScore;
						oneOutForMainPh += "," + qmrScore;

						//mr
						int MRScore = 0;
						for (int ccIndex = 0; ccIndex < MainConcepts.size(); ccIndex++) {
							if (DBpediaSpotlightClient.EnsenDBpediaResourceListContains(ress, MainConcepts.get(ccIndex).URI) > -1) {
								MRScore++;
							}
						}
						oneOutForMainPh += "," + MRScore;

						//ar
						oneOutForMainPh += "," + ress.size();

						//cns
						oneOutForMainPh += "," + cns;

						int arlScore = 0;
						int mrlScore = 0;
						for (int k = 0; k < analyzer.Doci.concepts.size(); k++) {
							Concept c = analyzer.Doci.concepts.get(k);
							String[] terms = c.name.toLowerCase().split(" ");
							for (String term : terms) {
								term = analyzer.Doci.SBStemmer.stem(term);
								if (stemedPh.contains(" " + term)) {
									arlScore++;
									if (k < MainConcepts.size())//main concept
										mrlScore++;
								}
							}

						}
						//arl				
						oneOutForMainPh += "," + arlScore;

						//mrl
						oneOutForMainPh += "," + mrlScore;

						//pt
						oneOutForMainPh += "," + ptScore;

						ptScore = 0;

						//as
						if (ress.size() > 0)
							oneOutForMainPh += "," + (asScore / ress.size() * 1.0);
						else
							oneOutForMainPh += ",0";

						//len
						oneOutForMainPh += "," + ph.length();

						//tt
						int ttscore = 0;
						String[] Tterms = analyzer.Doci.content.getTitle().toLowerCase().split(" ");
						for (String term : Tterms) {
							term = analyzer.Doci.SBStemmer.stem(term);
							if (stemedPh.contains(" " + term))
								ttscore++;
						}
						oneOutForMainPh += "," + ttscore;

						//se
						oneOutForMainPh += "," + ph.trim().charAt(ph.trim().length() - 1);

						//ss
						char c = ph.trim().charAt(0);
						if (c >= '0' && c <= '9')
							oneOutForMainPh += ",n";
						else if (c >= 'a' && c <= 'z')
							oneOutForMainPh += ",c";
						else if (c >= 'A' && c <= 'A')
							oneOutForMainPh += ",c";
						else
							oneOutForMainPh += ",o";

						//hl
						int https = ph.split("http").length - 1;
						https += ph.split("www.").length - 1;
						https += ph.split(".html").length - 1;
						if (https > 0)
							oneOutForMainPh += ",true";
						else
							oneOutForMainPh += ",false";

						//sss						
						int smallSentence = 0;
						for (String s : ph.split(",")) {
							if (s.split(" ").length < 3) {
								smallSentence++;
							}
						}
						oneOutForMainPh += "," + smallSentence;

						//CS
						if (ress.size() > 0)
							oneOutForMainPh += "," + csScore / ress.size();
						else
							oneOutForMainPh += ",0.0";

						//nch
						double nch = 0;
						Pattern p = Pattern.compile("[^A-Za-z ]");
						Matcher matcher = p.matcher(ph);
						int nchi = 0;
						while (matcher.find()) {
							matcher.group();
							nchi++;
						}

						nch = nchi * 100.0 / ph.length();

						oneOutForMainPh += "," + nch;

						//d
						boolean d = false;
						String DATE_PATTERN = "([0-9][0-9][0-9][0-9])|([0-9]{2})/([0-9]{2})/([0-9]{4})|([0-9]{2})-([0-9]{2})-([0-9]{4})|([0-9]{4})-([0-9]{2})-([0-9]{2})|([0-9]{4})/([0-9]{2})/([0-9]{2})";
						Pattern pattern = Pattern.compile(DATE_PATTERN);
						matcher = pattern.matcher(ph);
						if (matcher.find()) {
							d = true;
						}
						oneOutForMainPh += "," + d;

						//php
						double php = i * 100 / phs.size();
						oneOutForMainPh += "," + php;

						//fph
						boolean fph = (i == 0);
						oneOutForMainPh += "," + fph;

						//lph
						boolean lph = (i == phs.size() - 1);
						oneOutForMainPh += "," + lph;



						//newline
						oneOutForMainPh += ",\n";

						//build csv for concept-sentence & html files

						if (ress != null) {
							boolean firstTime = true;
							for (int cIndex = 0; cIndex < Math.min(analyzer.Doci.concepts.size(), analyzer.MaxNumOfConcepts); cIndex++) {
								Concept con = analyzer.Doci.concepts.get(cIndex);

								if (DBpediaSpotlightClient.EnsenDBpediaResourceListContains(ress, con.URI) > -1) {
									//For HTML
									if (firstTime) {
										out += "<fieldset>  <legend>" + i + "- select for the Snippet: yes <input type='radio' name='Q" + analyzer.Doci.q.id + "D" + analyzer.Doci.Rank + "ph" + i + "' value='Q" + analyzer.Doci.q.id + "D" + analyzer.Doci.Rank + "ph" + i + "'>, no<input type='radio' name='Q" + analyzer.Doci.q.id + "D" + analyzer.Doci.Rank + "ph" + i + "' value='' checked>    </legend><p>" + ph + "</p></hr>";
									}
									out += "<input type='checkbox' value='Q" + analyzer.Doci.q.id + "D" + analyzer.Doci.Rank + "C" + cIndex + "ph" + i + "'><span><a target='blank' href='" + con.URI + "'>" + con.name + "</a></span> ";
									firstTime = false;

									//for csv

									//id
									con.phsFeatures += "Q" + analyzer.Doci.q.id + "D" + analyzer.Doci.Rank + "C" + cIndex + "ph" + i;

									//tct
									int tempScore = 0;
									int position = -1;
									double sim = 0.0;//spotlight similarity
									double count = 0.0;//this concept in this paragraph instances counter
									for (String term1 : con.name.split(" ")) {
										if (term1.length() > 3)
											if (ph.toLowerCase().contains(" " + term1.toLowerCase() + " ")) {
												if (position == -1)
													position = ph.indexOf(" " + term1.toLowerCase() + " ");
												tempScore += 1;
											}
									}
									con.phsFeatures += "," + tempScore;

									//cb
									int cbScore = 0;
									int cnScore = 0;

									Set<String> brothers = new HashSet<String>();
									Set<String> neighbors = new HashSet<String>();
									if (ress != null)
										for (EnsenDBpediaResource res : ress) {

											//get position & spotlight similarity
											if (res.getFullUri().equals(con.URI)) {
												position = ph.indexOf(res.originalText);
												sim += res.similarityScore;
												count++;
											}
											//groups
											for (Group g : con.groups) {
												if (g.auths != null && g.auths.keySet().contains(res.getFullUri()))
													brothers.add(res.getFullUri());
												//g.auths.keySet().toArray().toString().
												if (g.hubs != null && g.hubs.keySet().contains(res.getFullUri()))
													brothers.add(res.getFullUri());

												for (Entry<String, Double> pred : g.predicates.entrySet()) {
													String[] predicatWords = pred.getKey().replace("_", " ").split("(?=\\p{Upper})|/| ");
													for (int j = 0; j < predicatWords.length; j++) {
														if (predicatWords[j].length() > 3 && ph.toLowerCase().contains(predicatWords[j].toLowerCase())) {
															ptScore++;
														}
													}
												}
											}

											//graph
											Model g = analyzer.Doci.fullGraph;
											StmtIterator res1 = g.listStatements(new SimpleSelector(g.getResource(res.getFullUri()), null, g.getResource(con.URI)));
											StmtIterator res2 = g.listStatements(new SimpleSelector(g.getResource(con.URI), null, g.getResource(res.getFullUri())));
											while (res1.hasNext()) {
												Statement next = res1.next();
												neighbors.add(next.getSubject().getURI());
											}
											while (res2.hasNext()) {
												Statement next = res2.next();
												neighbors.add(next.getObject().asResource().getURI());
											}

										}
									brothers.remove(con.URI);
									cbScore = brothers.size();
									con.phsFeatures += "," + cbScore;

									//cn
									neighbors.remove(con.URI);
									cnScore = neighbors.size();
									con.phsFeatures += "," + cnScore;

									//cbl	
									int cblScore = 0;

									Model m = RDFManager.createRDFModel();

									for (String oneBrother : brothers) {
										Resource R = m.createResource(oneBrother);
										String[] terms = R.getLocalName().toLowerCase().split(" ");
										for (String term : terms) {
											term = analyzer.Doci.SBStemmer.stem(term);
											if (stemedPh.contains(" " + term))
												cblScore++;
										}
									}

									con.phsFeatures += "," + cblScore;
									//cnl
									int cnlScore = 0;
									for (String oneNeighbor : neighbors) {
										Resource R = m.createResource(oneNeighbor);
										String[] terms = R.getLocalName().toLowerCase().split(" ");
										for (String term : terms) {
											term = analyzer.Doci.SBStemmer.stem(term);
											if (stemedPh.contains(" " + term))
												cnlScore++;
										}
									}
									con.phsFeatures += "," + cnlScore;

									//ca
									String stemedAbstract = "";
									for (String term : con.abstractTxt.split(" ")) {
										term = analyzer.Doci.SBStemmer.stem(term);
										stemedAbstract += term + " ";
									}
									double caScore = jaccardSimilarity(stemedAbstract, stemedPh);

									con.phsFeatures += "," + caScore;

									//cp
									double cpScore = 0;
									if (position > -1)
										cpScore = (ph.length() - position) * 1.0 / ph.length();
									if (cpScore > 1)
										cpScore = 1.0;
									con.phsFeatures += "," + cpScore;

									//qt
									con.phsFeatures += "," + qtScore;

									//qr
									con.phsFeatures += "," + qrScore;

									//mr

									con.phsFeatures += "," + MRScore;

									//ar
									con.phsFeatures += "," + ress.size();

									//arl

									con.phsFeatures += "," + arlScore;

									//mrl
									con.phsFeatures += "," + mrlScore;

									//pt
									con.phsFeatures += "," + ptScore;

									//cas
									con.phsFeatures += "," + sim / count * 1.0;

									//as							
									if (ress.size() > 0)
										con.phsFeatures += "," + (asScore / ress.size() * 1.0);
									else
										con.phsFeatures += ",0";

									//len
									con.phsFeatures += "," + ph.length();

									//tt

									con.phsFeatures += "," + ttscore;

									//se
									con.phsFeatures += "," + ph.trim().charAt(ph.trim().length() - 1);

									//ss
									c = ph.trim().charAt(0);
									if (c >= '0' && c <= '9')
										con.phsFeatures += ",n";
									else if (c >= 'a' && c <= 'z')
										con.phsFeatures += ",c";
									else if (c >= 'A' && c <= 'A')
										con.phsFeatures += ",c";
									else
										con.phsFeatures += ",o";

									//hl
									if (https > 0)
										con.phsFeatures += ",true";
									else
										con.phsFeatures += ",false";

									//sss	
									con.phsFeatures += "," + smallSentence;

									//csc 
									double cscScore = 0.0;
									int rank = getRank(analyzer.Doci.concepts, con.URI);
									if (rank > -1)
										cscScore = (1 / (1.0 * rank));
									con.phsFeatures += "," + cscScore;

									//cs
									if (ress.size() > 0)
										con.phsFeatures += "," + csScore / ress.size();
									else
										con.phsFeatures += ",0.0";

									con.phsFeatures += "," + nch;

									//d
									con.phsFeatures += "," + d;

									//php
									con.phsFeatures += "," + php;

									//fph
									con.phsFeatures += "," + fph;

									//lph
									con.phsFeatures += "," + lph;


									//newline
									con.phsFeatures += ",\n";

								}

								if (MRScore == 0)
									oneOutForMainPh = "";
								outForMainPh += oneOutForMainPh;
								oneOutForMainPh = "";
							}
						}
						out += " </fieldset>";
						//Printer.printToFile("ML/Q" + analyzer.Doci.q.id + "D" + analyzer.Doci.Rank + ".csv", out2);
					} catch (Exception e) {

					}
				}

			for (Concept c : analyzer.Doci.concepts) {
				outForConceptPh += c.phsFeatures;
			}

			outForConceptPh = outForConceptPh.replace("\"", "”").replace(",,,", ",\",\",").replace("'", "`");
			outForMainPh = outForMainPh.replace("\"", "”").replace(",,,", ",\",\",").replace("'", "`");

			String[] results = { "", "", "" };
			results[0] = out;
			results[1] = outForConceptPh;
			results[2] = outForMainPh;
			System.out.println("finish generating features for " + id);

			return results;
		}
		return null;
	}

	private static int getRank(ArrayList<Concept> concepts, String fullUri) {
		for (int ccIndex = 0; ccIndex < concepts.size(); ccIndex++) {
			if (concepts.get(ccIndex).URI.contains(fullUri))
				return ccIndex + 1;
		}
		return -1;
	}

	private static double jaccardSimilarity(String similar1, String similar2) {
		HashSet<String> h1 = new HashSet<String>();
		HashSet<String> h2 = new HashSet<String>();

		for (String s : similar1.split("\\s+")) {
			h1.add(s);
		}

		for (String s : similar2.split("\\s+")) {
			h2.add(s);
		}


		int sizeh1 = h1.size();
		//Retains all elements in h3 that are contained in h2 ie intersection
		h1.retainAll(h2);
		//h1 now contains the intersection of h1 and h2

		h2.removeAll(h1);
		//h2 now contains unique elements


		//Union 
		int union = sizeh1 + h2.size();
		int intersection = h1.size();

		return (double) intersection / union;

	}

	private static void mergeDataWithEvaluation() {
		String physicalFolder = PropertiesManager.getProperty("webRootPath");

		//Main sentence
		String mainSentenceDataFilePath = physicalFolder + "/ml/tomerge/mainSentences.csv";
		String mainSentenceEvaluationFilePath = physicalFolder + "/ml/tomerge/main-sentences-evaluation.txt";
		String mainSentenceData = readTextDocument(mainSentenceDataFilePath);
		String mainSentenceEval = readTextDocument(mainSentenceEvaluationFilePath);
		mainSentenceEval = mainSentenceEval.substring(0, mainSentenceEval.length() - 2) + ",";

		String mainSentenceOut = "";
		System.out.println(mainSentenceEval);
		for (String line : mainSentenceData.split(System.lineSeparator())) {
			if (!line.contains("selected")) {

				String id = line.split(",")[0];
				//System.out.println(id);
				if (mainSentenceEval.contains(id + ",")) {
					mainSentenceOut += line + "true" + System.lineSeparator();
				} else {
					mainSentenceOut += line + "false" + System.lineSeparator();
				}
			} else {
				mainSentenceOut += line + System.lineSeparator();
			}
		}


		Printer.printToFile("ml/tomerge/mainSentenceMLDataset.csv", mainSentenceOut);

		//concepts' sentences
		String conceptSentenceDataFilePath = physicalFolder + "/ml/tomerge/conceptSentences.csv";
		String conceptSentenceEvaluationFilePath = physicalFolder + "/ml/tomerge/concepts-sentences-evaluation.txt";
		String conceptSentenceData = readTextDocument(conceptSentenceDataFilePath);
		String conceptSentenceEval = readTextDocument(conceptSentenceEvaluationFilePath);
		conceptSentenceEval = conceptSentenceEval.substring(0, conceptSentenceEval.length() - 2) + ",";

		String conceptSentenceOut = "";
		for (String line : conceptSentenceData.split(System.lineSeparator())) {
			if (!line.contains("selected")) {
				String id = line.split(",")[0];
				if (conceptSentenceEval.contains(id + ",")) {
					conceptSentenceOut += line + "true" + System.lineSeparator();
				} else {
					conceptSentenceOut += line + "false" + System.lineSeparator();
				}
			} else {
				conceptSentenceOut += line + System.lineSeparator();
			}
		}

		//conceptSentenceOut = conceptSentenceOut.replace("\"", "”").replace(",,,", ",\",\",").replace("'", "`");
		Printer.printToFile("ml/tomerge/conceptSentenceMLDataset.csv", conceptSentenceOut);

	}

	private static String readTextDocument(String path) {
		try (BufferedReader br = new BufferedReader(new FileReader(path))) {
			StringBuilder sb = new StringBuilder();
			String line = br.readLine();
			while (line != null) {
				sb.append(line);
				sb.append(System.lineSeparator());
				line = br.readLine();
			}
			String everything = sb.toString();
			return everything;
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return "";
	}

}
