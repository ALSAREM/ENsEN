package ensen.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;

import com.google.api.services.customsearch.model.Result;

public class GoogleTools {
	static Logger log = Logger.getLogger(GoogleTools.class.getName());

	public static Map<String, String> getGooglePageParts(String q) {
		String html = "";
		try {
			URL url = new URL(PropertiesManager.getProperty("GoogleURL") + "search?q=" + q.replace(" ", "%20"));//%20
			URLConnection conn = url.openConnection();
			conn.setRequestProperty("User-Agent", "Mozilla/5.0 (X11; U; Linux x86_64; en-GB; rv:1.8.1.6) Gecko/20070723 Iceweasel/2.0.0.6 (Debian-2.0.0.6-0etch1)");
			BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
			String str;

			while ((str = in.readLine()) != null) {
				html += str;
			}
			html = html.replace("src=\"/", "src=\"" + PropertiesManager.getProperty("GoogleURL"));
			html = html.replace("href=\"/", "href=\"" + PropertiesManager.getProperty("GoogleURL"));
			html = html.replace("/images/nav_logo124.png", PropertiesManager.getProperty("GoogleURL") + "images/nav_logo123.png");
			html = html.replace("\"\"", "\"");
			html = html.replace("no-repeat 0 -245px", "no-repeat -100px -275px");

			in.close();
		} catch (MalformedURLException e) {
		} catch (IOException e) {
		}
		Map<String, String> map = new HashMap<String, String>();
		int index = 0;
		while (index != -1) {
			index = html.indexOf("<li class=\"g\"><h3 class=\"r\"><a href=\"" + PropertiesManager.getProperty("GoogleURL") + "url", index);
			if (index != -1) {
				int index2 = html.indexOf("</li", index);
				String node = html.substring(index, index2 + 5);
				//get title
				String a = node.substring(node.indexOf("<a"), node.indexOf("</a") + 4);
				String title = Jsoup.parse(a).text().toLowerCase();
				System.out.println("Jsoup title" + title);
				map.put(title, node + "</br>");
				index = index + node.length();
			} else
				break;
		}
		return map;
	}

	public static List<Result> getGoogleResults(String q, int num) {
		String html = "";
		try {
			int googleNofRes=Integer.parseInt(PropertiesManager.getProperty("nOfResForGoogleQueryClient"));
			String googleFilters = PropertiesManager.getProperty("googleFilters");
			q += " " + googleFilters;
			q = q.replace(" ", "%20");
			URL url = new URL(PropertiesManager.getProperty("GoogleURL") + "search?q=" + q + "&num=" + googleNofRes + "&lr=lang_en");//%20
			URLConnection conn = url.openConnection();
			conn.setRequestProperty("User-Agent", "Mozilla/5.0 (X11; U; Linux x86_64; en-GB; rv:1.8.1.6) Gecko/20070723 Iceweasel/2.0.0.6 (Debian-2.0.0.6-0etch1)");
			BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
			String str;

			while ((str = in.readLine()) != null) {
				html += str;
			}
			html = html.replace("src=\"/", "src=\"" + PropertiesManager.getProperty("GoogleURL"));
			html = html.replace("href=\"/", "href=\"" + PropertiesManager.getProperty("GoogleURL"));
			html = html.replace("/images/nav_logo124.png", PropertiesManager.getProperty("GoogleURL") + "images/nav_logo123.png");
			html = html.replace("\"\"", "\"");
			html = html.replace("no-repeat 0 -245px", "no-repeat -100px -275px");

			in.close();
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}

		ArrayList<Result> Results = new ArrayList<Result>();
		int index = 0;
		int rank = 1;
		while (index != -1 && (rank <= num)) {
			try {
				index = html.indexOf("<li class=\"g\"><h3 class=\"r\"><a href=\"" + PropertiesManager.getProperty("GoogleURL") + "url", index);
				if (index < 0)
					break;
				Result res = new Result();

				int index2 = html.indexOf("<li class=\"g\">", index + 10);
				if (index2 < 0)
					index2 = html.length();
				String node = html.substring(index, Math.min(index2 + 5, html.length()));
				//System.out.println(node.length());
				//get title
				String a = node.substring(node.indexOf("<a"), node.indexOf("</a") + 4);
				Element aElement = Jsoup.parse(a).select("a").first();
				String title = aElement.text();
				res.setTitle(title);
				//System.out.println("Jsoup title: " + res.getTitle());

				//get link				
				String link = aElement.attr("href").replace(PropertiesManager.getProperty("GoogleURL") + "url?q=", "");
				int temp = link.indexOf("&sa=U");
				link = link.substring(0, temp);
				link = URLDecoder.decode(link);
				res.setLink(link);
				//System.out.println("Jsoup link: " + res.getLink());

				//get Snippet
				//Elements tempNode = Jsoup.parse(node).select("cite").remove();
				//System.out.println(node);
				res.setSnippet(Jsoup.parse(node).getElementsByClass("st").first().text());
				//System.out.println("Jsoup Snippet: " + res.getSnippet());

				//getHtmlTitle
				res.setHtmlTitle(aElement.html());
				//System.out.println("Jsoup HtmlTitle: " + res.getHtmlTitle());

				//getHtmlSnippet
				res.setHtmlSnippet(node);
				//System.out.println("Jsoup Html Snippet: " + res.getHtmlSnippet());

				Results.add(res);
				index = index + node.length() + 1;

				rank++;

			} catch (Exception e) {
				e.printStackTrace();
				index = -1;
			}
			//System.out.println("index: " + index + "  rank: " + rank);
		}
		System.out.println("end Jsoup Html with succes, return res: " + Results.size());
		/* inverse first and second results (avoid wikipedia pages in first)*/
		if (Results.size() > 1 && Boolean.parseBoolean(PropertiesManager.getProperty("inverst1and2"))) {
			Result temp = Results.get(0);
			Results.set(0, Results.get(1));
			Results.set(1, temp);
		}
		return Results;
	}
}
