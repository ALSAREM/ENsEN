/**
 * Copyright 2011 Pablo Mendes, Max Jakob
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ensen.controler;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.log4j.Logger;
import org.dbpedia.spotlight.exceptions.AnnotationException;
import org.dbpedia.spotlight.model.DBpediaResource;
import org.dbpedia.spotlight.model.Text;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import ensen.entities.EnsenDBpediaResource;
import ensen.util.PropertiesManager;

/** Simple web service-based annotation client for DBpedia Spotlight.
 * 
 * @author pablomendes, Joachim Daiber */

public class DBpediaSpotlightClient extends AnnotationClient {
	Logger log = Logger.getLogger(this.getClass());
	private String API_URL = "";

	private String SPOTTER = "";
	public boolean local = false;
	public String cachePath = "";
	public int offset = 0;

	public List<EnsenDBpediaResource> ensenExtract(Text text) throws AnnotationException {

		if (Boolean.parseBoolean(PropertiesManager.getProperty("UseDBpediaSpotlightCandidate")))
			return extractCandidats(text);
		else
			return extractAnnotation(text, PropertiesManager.getProperty("CONFIDENCE"), PropertiesManager.getProperty("SUPPORT"), "");

	}

	public List<EnsenDBpediaResource> ensenExtract(Text text, String file) throws AnnotationException {

		if (Boolean.parseBoolean(PropertiesManager.getProperty("UseDBpediaSpotlightCandidate")))
			return extractCandidats(text);
		else
			return extractAnnotation(text, PropertiesManager.getProperty("CONFIDENCE"), PropertiesManager.getProperty("SUPPORT"), file);

	}

	public List<EnsenDBpediaResource> ensenExtract(Text text, String confiance, String support, String file) throws AnnotationException {
		if (Boolean.parseBoolean(PropertiesManager.getProperty("UseDBpediaSpotlightCandidate")))
			return extractCandidats(text);
		else
			return extractAnnotation(text, confiance, support, file);

	}

	private JSONArray getAnnotationsFromSpotlight(Text text, String confiance, String support, String file) {
		if (confiance == null || confiance.trim() == "")
			confiance = PropertiesManager.getProperty("CONFIDENCE");
		if (support == null || support.trim() == "")
			support = PropertiesManager.getProperty("SUPPORT");
		JSONArray entities = null;
		if (local) {
			double d = Math.random();
			if (d > 0.5)
				API_URL = PropertiesManager.getProperty("DBpediaSpotlightClientLocal");
			else
				API_URL = PropertiesManager.getProperty("DBpediaSpotlightClientLocalCopy");
		} else
			API_URL = PropertiesManager.getProperty("DBpediaSpotlightClient");
		SPOTTER = PropertiesManager.getProperty("spotter");
		String spotlightResponse = null;
		try {
			//System.out.println("Querying API: " + API_URL);
			//System.err.println(text.text());
			/** using post **/
			PostMethod post = new PostMethod(API_URL + "rest/annotate/");
			NameValuePair[] data = { new NameValuePair("coreferenceResolution", "false"),/**//*new NameValuePair("disambiguator", "Document"),new NameValuePair("spotter", SPOTTER),*/new NameValuePair("confidence", confiance), new NameValuePair("support", support), new NameValuePair("text", text.text()) };
			post.setRequestBody(data);
			post.addRequestHeader(new Header("Accept", "application/json"));
			post.setRequestHeader("Content-Type", PostMethod.FORM_URL_ENCODED_CONTENT_TYPE + "; charset=UTF-8");
			spotlightResponse = request(post);

		} catch (Exception e) {
			System.err.println("error in calling Spotlight.");
		}

		JSONObject resultJSON = null;

		try {
			resultJSON = new JSONObject(spotlightResponse);
			if (resultJSON.has("Resources")) {
				entities = resultJSON.getJSONArray("Resources");
			} else {
				System.err.println("No founded resources");
				System.err.println(resultJSON);
			}
		} catch (JSONException e) {
			System.err.println(spotlightResponse);
			System.err.println("Received invalid response from DBpedia Spotlight API.");
			e.printStackTrace();
		}
		if (resultJSON != null) {
			//System.err.println("print json to" + file + ".json");
			//Printer.printToFile(file + ".json", resultJSON.toString());
		}
		//if not enough resources ==> re-do with conf 0.15
		if ((entities == null || entities.length() < Integer.parseInt(PropertiesManager.getProperty("MinNofAnnotatedResourcesInDocument"))) && confiance != "0.15") {
			entities = getAnnotationsFromSpotlight(text, "0.15", support, file);

		}

		return entities;
	}

	public List<EnsenDBpediaResource> extractAnnotation(Text text, String confiance, String support, String file) throws AnnotationException {

		JSONArray entities = getAnnotationsFromSpotlight(text, confiance, support, file);

		LinkedList<EnsenDBpediaResource> resources = new LinkedList<EnsenDBpediaResource>();
		if (entities != null)
			for (int i = 0; i < entities.length(); i++) {
				try {
					JSONObject entity = entities.getJSONObject(i);
					String surfaceForm = entity.getString("@surfaceForm");
					if (surfaceForm.replace(".", "").replace("'", "'").length() > 3 && !surfaceForm.matches("^\\d+$")) /* we don't consider shortcuts or numbers and cases like: D.C. */
					{
						String url = URLDecoder.decode(entity.getString("@URI"), "utf-8");
						EnsenDBpediaResource EDBR = new EnsenDBpediaResource(url, Integer.parseInt(entity.getString("@support")), offset + Integer.parseInt(entity.getString("@offset")), entity.getString("@surfaceForm"), Double.parseDouble(entity.getString("@similarityScore")), entity.getString("@types"), Double.parseDouble(entity.getString("@percentageOfSecondRank")));
						resources.add(EDBR);
					}
				} catch (JSONException e) {

				} catch (UnsupportedEncodingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}
		//System.out.println("RDFizing: founded " + resources.size() + " resources");
		//Printer.registerTime("parse results from DBpedia spotlight ");
		return resources;
	}

	public List<EnsenDBpediaResource> extractCandidats(Text text) throws AnnotationException {
		if (local)
			API_URL = PropertiesManager.getProperty("DBpediaSpotlightClientLocal");
		else
			API_URL = PropertiesManager.getProperty("DBpediaSpotlightClient");
		SPOTTER = PropertiesManager.getProperty("spotter");
		System.out.println("Querying API: " + API_URL);
		String spotlightResponse;
		try {
			GetMethod getMethod = new GetMethod(API_URL + "rest/candidates/?" + "confidence=" + PropertiesManager.getProperty("CONFIDENCE") + "&support=" + PropertiesManager.getProperty("SUPPORT") + "&spotter=" + SPOTTER + "&text=" + URLEncoder.encode(text.text(), "utf-8"));
			getMethod.addRequestHeader(new Header("Accept", "application/json"));

			spotlightResponse = request(getMethod);
		} catch (UnsupportedEncodingException e) {
			throw new AnnotationException("Could not encode text.", e);
		}

		assert spotlightResponse != null;

		JSONObject resultJSON = null;
		JSONArray entities = null;

		try {
			resultJSON = new JSONObject(spotlightResponse);
			//System.out.println(resultJSON.toString());
			JSONObject annotationObj = resultJSON.getJSONObject("annotation");
			entities = annotationObj.getJSONArray("surfaceForm");
		} catch (JSONException e) {
			System.err.println(resultJSON.toString());
			e.printStackTrace();
			throw new AnnotationException("Received invalid response from DBpedia Spotlight API.");
		}

		LinkedList<EnsenDBpediaResource> resources = new LinkedList<EnsenDBpediaResource>();
		for (int i = 0; i < entities.length(); i++) {
			try {
				JSONObject entity = entities.getJSONObject(i);
				try {
					JSONArray resourcesArray = entity.getJSONArray("resource");
					for (int j = 0; j < resourcesArray.length(); j++) {
						resources.add(new EnsenDBpediaResource(((JSONObject) resourcesArray.get(j)).getString("@uri"), Integer.parseInt(((JSONObject) resourcesArray.get(j)).getString("@support"))));
					}
				} catch (Exception e) {

				}

				try {
					JSONObject resourcesArray = entity.getJSONObject("resource");
					resources.add(new EnsenDBpediaResource(resourcesArray.getString("@uri"), Integer.parseInt(resourcesArray.getString("@support"))));

				} catch (Exception e) {

				}

			} catch (JSONException e) {
				System.out.println("JSON exception " + e);
			}

		}

		return resources;
	}

	public static void main(String[] args) throws Exception {

		DBpediaSpotlightClient c = new DBpediaSpotlightClient();
		//c.local = true;
		List<EnsenDBpediaResource> res = c.ensenExtract(new Text("President Obama on Monday will call for a new minimum tax rate for individuals making more than $1 million a year to ensure that they pay at least the same percentage of their earnings as other taxpayers, according to administration officials."));

		for (int i = 0; i < res.size(); i++) {
			EnsenDBpediaResource R = res.get(i);
			//System.out.println(R.getFullUri());
		}
	}

	@Override
	public List<DBpediaResource> extract(Text text) throws AnnotationException {

		return null;
	}

	public static int EnsenDBpediaResourceListContains(List<EnsenDBpediaResource> list, String URI) {
		int index = 0;
		int times = 0;
		for (EnsenDBpediaResource res : list) {
			if (res.getFullUri().equals(URI))
				times++;
			index++;
		}
		if (times == 0)
			return -1;
		return times;
	}
}
