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

package ensen.control;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.methods.GetMethod;
import org.dbpedia.spotlight.exceptions.AnnotationException;
import org.dbpedia.spotlight.model.DBpediaResource;
import org.dbpedia.spotlight.model.Text;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import ensen.entities.EnsenDBpediaResource;
import ensen.org.dbpedia.spotlight.evaluation.external.AnnotationClient;

/** Simple web service-based annotation client for DBpedia Spotlight.
 * 
 * @author pablomendes, Joachim Daiber */

public class DBpediaSpotlightClient extends AnnotationClient {

	private String API_URL = "";
	private static final double CONFIDENCE = 0.0;
	private static final int SUPPORT = 0;
	private String SPOTTER = "";
	public boolean local = false;
	public String cachePath = "";
	public int offset = 0;

	public List<EnsenDBpediaResource> ensenExtract(Text text) throws AnnotationException {
		/*	System.out.println("Rdfizing the Text: ");
			System.out.println(text);*/

		if (Boolean.parseBoolean(PropertiesManager.getProperty("UseDBpediaSpotlightCandidate")))
			return extractCandidats(text);
		else
			return extractAnnotation(text);

	}

	public List<EnsenDBpediaResource> extractAnnotation(Text text) throws AnnotationException {
		if (local)
			API_URL = PropertiesManager.getProperty("DBpediaSpotlightClientLocal");
		else
			API_URL = PropertiesManager.getProperty("DBpediaSpotlightClient");

		String spotlightResponse;
		Object obj = null;
		try {
			obj = ensen.util.Serializer.unSerialize(cachePath);
		} catch (Exception e) {

		}

		if (obj != null) {
			System.out.println("geted From cache");
			spotlightResponse = (String) obj;
		} else {
			try {
				LOG.info("Querying API: " + API_URL);

				GetMethod getMethod = new GetMethod(API_URL + "rest/annotate/?" + "confidence=" + CONFIDENCE + "&support=" + SUPPORT + "&text=" + URLEncoder.encode(text.text(), "utf-8"));
				getMethod.addRequestHeader(new Header("Accept", "application/json"));
				spotlightResponse = request(getMethod);
				ensen.util.Serializer.Serialize(spotlightResponse, cachePath);

			} catch (UnsupportedEncodingException e) {
				try {
					System.err.println("Mazen:" + API_URL + "rest/annotate/?" + "confidence=" + CONFIDENCE + "&support=" + SUPPORT + "&text=" + URLEncoder.encode(text.text(), "utf-8"));
				} catch (UnsupportedEncodingException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				throw new AnnotationException("Could not encode text.", e);
			}
		}
		assert spotlightResponse != null;

		JSONObject resultJSON = null;
		JSONArray entities = null;

		try {
			resultJSON = new JSONObject(spotlightResponse);
			entities = resultJSON.getJSONArray("Resources");
		} catch (JSONException e) {
			throw new AnnotationException("Received invalid response from DBpedia Spotlight API.");
		}

		LinkedList<EnsenDBpediaResource> resources = new LinkedList<EnsenDBpediaResource>();
		for (int i = 0; i < entities.length(); i++) {
			try {
				JSONObject entity = entities.getJSONObject(i);
				resources.add(new EnsenDBpediaResource(entity.getString("@URI"), Integer.parseInt(entity.getString("@support")), offset + Integer.parseInt(entity.getString("@offset")), entity.getString("@surfaceForm"), Double.parseDouble(entity.getString("@similarityScore")), entity.getString("@types"), Double.parseDouble(entity.getString("@percentageOfSecondRank"))));

			} catch (JSONException e) {
				LOG.error("JSON exception " + e);
			}

		}

		return resources;
	}

	public List<EnsenDBpediaResource> extractCandidats(Text text) throws AnnotationException {
		if (local)
			API_URL = PropertiesManager.getProperty("DBpediaSpotlightClientLocal");
		else
			API_URL = PropertiesManager.getProperty("DBpediaSpotlightClient");
		SPOTTER = PropertiesManager.getProperty("spotter");
		LOG.info("Querying API: " + API_URL);
		String spotlightResponse;
		try {
			GetMethod getMethod = new GetMethod(API_URL + "rest/candidates/?" + "confidence=" + CONFIDENCE + "&support=" + SUPPORT + "&spotter=" + SPOTTER + "&text=" + URLEncoder.encode(text.text(), "utf-8"));
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
					// TODO: handle exception
				}

				try {
					JSONObject resourcesArray = entity.getJSONObject("resource");
					resources.add(new EnsenDBpediaResource(resourcesArray.getString("@uri"), Integer.parseInt(resourcesArray.getString("@support"))));

				} catch (Exception e) {
					// TODO: handle exception
				}

			} catch (JSONException e) {
				LOG.error("JSON exception " + e);
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
		// TODO Auto-generated method stub
		return null;
	}

}
