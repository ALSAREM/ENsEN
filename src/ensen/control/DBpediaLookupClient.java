package ensen.control;

import java.awt.EventQueue;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;

import ensen.entities.DBpediaLookupResModel;

/**
 * Copyright Mark Watson 2008-2010. All Rights Reserved.
 * License: LGPL version 3 (http://www.gnu.org/licenses/lgpl-3.0.txt)
 */

// Use Georgi Kobilarov's DBpedia lookup web service
//    ref: http://lookup.dbpedia.org/api/search.asmx?op=KeywordSearch
//    example: http://lookup.dbpedia.org/api/search.asmx/KeywordSearch?QueryString=Flagstaff&QueryClass=XML&MaxHits=10

/**
 * Searches return results that contain any of the search terms. I am going to
 * filter the results to ignore results that do not contain all search terms.
 */

public class DBpediaLookupClient extends DefaultHandler {

	String gatway = PropertiesManager.getProperty("DBpediaLookupGatway");

	private List<Map<String, String>> variableBindings = new ArrayList<Map<String, String>>();
	private Map<String, String> tempBinding = null;
	private String lastElementName = null;
	private ArrayList<DBpediaLookupResModel> Results = new ArrayList<DBpediaLookupResModel>();

	public DBpediaLookupClient() {
	}

	public Model qetAllEntities(String query, String classes, int numberOfRes) throws Exception {
		Model model = ModelFactory.createDefaultModel();
		model.getNsPrefixMap().put("ns", "http://dbpedia.org/namespace");
		String[] queryList = query.split(" ");
		// all the query
		model.add(this.qetEntities(query, classes, numberOfRes));

		// query word by word
		for (int i = 0; i < queryList.length; i++) {
			model.add(this.qetEntities(queryList[i], classes, numberOfRes));
			// model.write(System.out);
		}

		return model;
	}

	public Model qetEntities(String query, String classes, int numberOfRes) throws Exception {

		HttpClient client = new HttpClient();
		// client.getHttpConnectionManager().getParams().setConnectionTimeout(10000);
		String query2 = URLEncoder.encode(query, "utf-8");
		HttpMethod method = new GetMethod(gatway + "QueryString=" + query2 + "&QueryClass=" + classes + "&MaxHits=" + numberOfRes);
		// method.setFollowRedirects(true);
		try {
			client.executeMethod(method);
			InputStream ins = method.getResponseBodyAsStream();
			SAXParserFactory factory = SAXParserFactory.newInstance();
			SAXParser sax = factory.newSAXParser();
			sax.parse(ins, this);
		} catch (HttpException he) {
			System.err.println("Http error connecting to lookup.dbpedia.org");
		} catch (IOException ioe) {
			System.err.println("Unable to connect to lookup.dbpedia.org");
		} catch (Exception e) {
			System.err.println("Error: " + e.getMessage());
		}
		method.releaseConnection();

		Model model = ModelFactory.createDefaultModel();
		Resource core = model.createResource("http://ensen.org/data#q-" + query2);

		int num_results = Results.size();
		if (num_results > 0) {
			for (DBpediaLookupResModel res : Results) {
				// System.out.println("URI: " + res.uri);
				if (res.uri.contains("http://")) {
					Resource O = model.createResource(res.uri);
					Property P = model.createProperty("http://ensen.org/data#has-a");
					Literal O1 = model.createLiteral(res.label + "");
					Literal O2 = model.createLiteral(res.desc + "");
					Property P1 = model.createProperty("http://www.w3.org/2000/01/rdf-schema#label");
					Property P2 = model.createProperty("http://www.w3.org/1999/02/22-rdf-syntax-ns#description");
					O.addProperty(P1, O1);
					O.addProperty(P2, O2);

					for (String cat : res.cats) {
						Resource OO = model.createResource(cat);
						Property PP = model.createProperty("http://purl.org/dc/terms/subject");
						O.addProperty(PP, OO);
					}
					for (String c : res.classes) {
						Resource OO = model.createResource(c);
						Property PP = model.createProperty("http://www.w3.org/1999/02/22-rdf-syntax-ns#type");
						O.addProperty(PP, OO);
					}
					core.addProperty(P, O);
				}
			}
		}

		return model;
	}

	public String getOneResource(String query, String classes, int numberOfRes) throws Exception {

		HttpClient client = new HttpClient();
		// client.getHttpConnectionManager().getParams().setConnectionTimeout(10000);
		String query2 = URLEncoder.encode(query, "utf-8");
		HttpMethod method = new GetMethod(gatway + "QueryString=" + query2 + "&QueryClass=" + classes + "&MaxHits=" + numberOfRes);
		// method.setFollowRedirects(true);
		try {
			client.executeMethod(method);
			InputStream ins = method.getResponseBodyAsStream();
			SAXParserFactory factory = SAXParserFactory.newInstance();
			SAXParser sax = factory.newSAXParser();
			sax.parse(ins, this);
		} catch (HttpException he) {
			System.err.println("Http error connecting to lookup.dbpedia.org");
		} catch (IOException ioe) {
			System.err.println("Unable to connect to lookup.dbpedia.org");
		} catch (Exception e) {
			System.err.println("Error: " + e.getMessage());
		}
		method.releaseConnection();

		String resource = "";
		int num_results = Results.size();
		if (num_results > 0) {
			for (DBpediaLookupResModel res : Results) {
				resource = URLDecoder.decode(res.uri);
				/*
				 * if (res.uri.contains("http://")) { Resource O =
				 * model.createResource(res.uri); Property P =
				 * model.createProperty("http://ensen.org/data#has-a"); Literal
				 * O1 = model.createLiteral(res.label + ""); Literal O2 =
				 * model.createLiteral(res.desc + ""); Property P1 =
				 * model.createProperty
				 * ("http://www.w3.org/2000/01/rdf-schema#label"); Property P2 =
				 * model.createProperty(
				 * "http://www.w3.org/1999/02/22-rdf-syntax-ns#description");
				 * O.addProperty(P1, O1); O.addProperty(P2, O2);
				 * 
				 * for (String cat : res.cats) { Resource OO =
				 * model.createResource(cat); Property PP =
				 * model.createProperty("http://purl.org/dc/terms/subject");
				 * O.addProperty(PP, OO); } for (String c : res.classes) {
				 * Resource OO = model.createResource(c); Property PP =
				 * model.createProperty
				 * ("http://www.w3.org/1999/02/22-rdf-syntax-ns#type");
				 * O.addProperty(PP, OO); } core.addProperty(P, O);
				 * 
				 * }
				 */
			}
		}

		return resource;
	}

	DBpediaLookupResModel tempRes;
	ArrayList<String> tempClasses;
	ArrayList<String> tempCats;
	int currType = 0;// 0 =res, 1=class, 2=cat

	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
		if (qName.equalsIgnoreCase("result")) {
			tempBinding = new HashMap<String, String>();
			tempRes = new DBpediaLookupResModel();
			currType = 0;
		}
		if (qName.equalsIgnoreCase("Classes")) {
			tempClasses = new ArrayList<String>();
			currType = 1;
		}
		if (qName.equalsIgnoreCase("Categories")) {
			tempCats = new ArrayList<String>();
			currType = 2;
		}

		lastElementName = qName;
	}

	public void endElement(String uri, String localName, String qName) throws SAXException {
		if (qName.equalsIgnoreCase("result")) {
			if (!variableBindings.contains(tempBinding) && containsSearchTerms(tempBinding))
				variableBindings.add(tempBinding);
		}
		if (qName.equalsIgnoreCase("result")) {
			tempRes.classes = tempClasses;
			tempRes.cats = tempCats;
			Results.add(tempRes);

		} else if (qName.equalsIgnoreCase("Label")) {
			if (currType == 0)
				tempRes.label = tmpValue;

		} else if (qName.equalsIgnoreCase("Description")) {
			tempRes.desc = tmpValue;
		} else if (qName.equalsIgnoreCase("URI")) {
			switch (currType) {
			case 0:
				tempRes.uri = tmpValue;
				break;
			case 1:
				tempClasses.add(tmpValue);
				break;
			case 2:
				tempCats.add(tmpValue);
				break;

			default:
				break;
			}
		}

	}

	String tmpValue;

	public void characters(char[] ch, int start, int length) throws SAXException {
		String s = new String(ch, start, length).trim();
		tmpValue = s;
		if (s.length() > 0) {
			if ("Description".equals(lastElementName))
				tempBinding.put("Description", s);
			if ("URI".equals(lastElementName))
				tempBinding.put("URI", s);
			if ("Label".equals(lastElementName))
				tempBinding.put("Label", s);
		}
	}

	public List<Map<String, String>> variableBindings() {
		return variableBindings;
	}

	private boolean containsSearchTerms(Map<String, String> bindings) {
		StringBuilder sb = new StringBuilder();
		for (String value : bindings.values())
			sb.append(value); // do not need white space
		String text = sb.toString().toLowerCase();
		StringTokenizer st = new StringTokenizer(this.query);
		while (st.hasMoreTokens()) {
			if (text.indexOf(st.nextToken().toLowerCase()) == -1) {
				return false;
			}
		}
		return true;
	}

	private String query = "";

	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					DBpediaLookupClient lookup = new DBpediaLookupClient();
					Model m = lookup.qetEntities("Syria", "", 5);
					m.write(System.out, "RDF/XML-ABBREV");

				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}
}