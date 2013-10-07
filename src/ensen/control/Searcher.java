package ensen.control;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson.JacksonFactory;
import com.google.api.services.customsearch.Customsearch;
import com.google.api.services.customsearch.model.Result;
import com.google.api.services.customsearch.model.Search;

import ensen.entities.Document;
import ensen.util.GoogleTools;

public class Searcher {
	public Searcher() {
		// TODO Auto-generated constructor stub
	}

	public String HTMLresults(ArrayList<Document> results) {
		String HTML = "";
		for (Document result : results) {
			HTML += "<a href='" + result.content.getLink() + "'>" + result.content.getHtmlTitle() + "</a> <br/>" + result.content.getHtmlSnippet() + "<hr/>";
		}

		return HTML;
	}

	public List<Result> searchInGoogle(String q, int num) {
		System.out.println("Get Results from Google");
		Customsearch customsearch = new Customsearch(new NetHttpTransport(), new JacksonFactory());
		List<Result> items = new ArrayList<Result>();
		com.google.api.services.customsearch.Customsearch.Cse.List list;
		try {
			list = customsearch.cse().list(q);
			list.setKey("AIzaSyDQC3psebuYlScH-I18VEThT09Lm5f6I60");
			list.setCx("000537100864002557136:hfce3bl1aka");
			list.setNum((long) num);
			Search results = list.execute();
			items = results.getItems();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return items;
	}

	public ArrayList<Document> search(String q, int num) {
		boolean APIMode = Boolean.parseBoolean(PropertiesManager.getProperty("ActiveGoogleAPIMode"));
		if (APIMode)
			return searchUsingGoogleAPI(q, num);
		else
			return searchUsingGoogleHTTP(q, num);
	}

	public ArrayList<Document> searchUsingGoogleHTTP(String q, int num) {
		long start = System.currentTimeMillis();
		Map<String, Document> googlePageParts = GoogleTools.getGoogleResults(q, num);// get results by html

		ArrayList<Document> res = new ArrayList<Document>();
		Collection<Document> docs = googlePageParts.values();
		int counter = 0;
		for (Document D : docs) {
			if (!D.html.trim().isEmpty()) {
				if (counter < num) {
					D.RDFize();
					res.add(D);
					counter++;
				} else
					break;
			}

		}
		long end = System.currentTimeMillis();
		System.err.println("google:" + (end - start));

		return res;
	}

	public ArrayList<Document> searchUsingGoogleAPI(String q, int num) {
		long start = System.currentTimeMillis();
		List<Result> items = searchInGoogle(q, num);// get results by API

		long f = System.currentTimeMillis();
		ArrayList<Document> res = new ArrayList<Document>();
		System.out.println("Get Results Content(HTML)");
		int rank = 1;
		for (Result result : items) {
			Document D;
			D = new Document(result.getLink(), rank, result.getPagemap());
			D.content = result;
			D.queryText = q;
			D.RDFize();
			res.add(D);
			rank++;
		}

		long end = System.currentTimeMillis();
		System.err.println("google:" + (f - start));
		System.err.println("spot:" + (end - f));

		return res;
	}
}
