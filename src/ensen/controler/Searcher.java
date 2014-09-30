package ensen.controler;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import org.apache.log4j.Logger;

import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson.JacksonFactory;
import com.google.api.services.customsearch.Customsearch;
import com.google.api.services.customsearch.model.Result;
import com.google.api.services.customsearch.model.Search;

import ensen.entities.Document;
import ensen.util.GoogleTools;
import ensen.util.PropertiesManager;

public class Searcher {
	static Logger log = Logger.getLogger(Searcher.class.getName());

	public Searcher() {

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
			
			e.printStackTrace();
		}
		return items;
	}

	public List<Result> search(String q, int num) {
		boolean APIMode = Boolean.parseBoolean(PropertiesManager.getProperty("ActiveGoogleAPIMode"));
		if (APIMode)
			return searchUsingGoogleAPI(q, num);
		else
			return searchUsingGoogleHTTP(q, num);

	}

	public String getTimeFormated(long ms) {
		DateFormat df = new SimpleDateFormat("mm 'mins,' ss 'seconds', SSS 'ms'");
		df.setTimeZone(TimeZone.getTimeZone("GMT+0"));
		return df.format(new Date(ms));
	}

	public List<Result> searchUsingGoogleHTTP(String q, int num) {
		long start = System.currentTimeMillis();
		List<Result> Results = GoogleTools.getGoogleResults(q, num);// get results by html
		long end1 = System.currentTimeMillis();
		System.out.println("get results from google:" + getTimeFormated(end1 - start));

		return Results;
	}

	public List<Result> searchUsingGoogleAPI(String q, int num) {
		long start = System.currentTimeMillis();
		List<Result> items = searchInGoogle(q, num);// get results by API
		System.out.println("Get Results Content(HTML)");
		long end = System.currentTimeMillis();
		System.out.println("spot:" + (end - start));
		return items;
	}
}
