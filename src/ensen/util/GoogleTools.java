package ensen.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Map;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;

import com.google.api.services.customsearch.model.Result;

import ensen.entities.Document;

public class GoogleTools {
	public static Map<String, String> getGooglePageParts(String q) {
		String html = "";
		try {
			URL url = new URL("http://www.google.com/search?q=" + q.replace(" ", "%20"));//%20
			URLConnection conn = url.openConnection();
			conn.setRequestProperty("User-Agent", "Mozilla/5.0 (X11; U; Linux x86_64; en-GB; rv:1.8.1.6) Gecko/20070723 Iceweasel/2.0.0.6 (Debian-2.0.0.6-0etch1)");
			BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
			String str;

			while ((str = in.readLine()) != null) {
				html += str;
			}
			html = html.replace("src=\"/", "src=\"http://www.google.com/");
			html = html.replace("href=\"/", "href=\"http://www.google.com/");
			html = html.replace("/images/nav_logo124.png", "http://www.google.com/images/nav_logo123.png");
			html = html.replace("\"\"", "\"");
			html = html.replace("no-repeat 0 -245px", "no-repeat -100px -275px");

			in.close();
		} catch (MalformedURLException e) {
		} catch (IOException e) {
		}
		Map<String, String> map = new HashMap<String, String>();
		int index = 0;
		while (index != -1) {
			index = html.indexOf("<li class=\"g\"><h3 class=\"r\"><a href=\"http://www.google.com/url", index);
			if (index != -1) {
				int index2 = html.indexOf("</li", index);
				String node = html.substring(index, index2 + 5);
				//get title
				String a = node.substring(node.indexOf("<a"), node.indexOf("</a") + 4);
				String title = Jsoup.parse(a).text().toLowerCase();
				System.err.println("Jsoup title" + title);
				map.put(title, node + "</br>");
				index = index + node.length();
			} else
				break;
		}
		return map;
	}

	public static Map<String, Document> getGoogleResults(String q, int num) {
		String html = "";
		try {
			URL url = new URL("http://www.google.com/search?q=" + q.replace(" ", "%20"));//%20
			URLConnection conn = url.openConnection();
			conn.setRequestProperty("User-Agent", "Mozilla/5.0 (X11; U; Linux x86_64; en-GB; rv:1.8.1.6) Gecko/20070723 Iceweasel/2.0.0.6 (Debian-2.0.0.6-0etch1)");
			BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
			String str;

			while ((str = in.readLine()) != null) {
				html += str;
			}
			html = html.replace("src=\"/", "src=\"http://www.google.com/");
			html = html.replace("href=\"/", "href=\"http://www.google.com/");
			html = html.replace("/images/nav_logo124.png", "http://www.google.com/images/nav_logo123.png");
			html = html.replace("\"\"", "\"");
			html = html.replace("no-repeat 0 -245px", "no-repeat -100px -275px");

			in.close();
		} catch (MalformedURLException e) {
		} catch (IOException e) {
		}

		Map<String, Document> map = new HashMap<String, Document>();
		int index = 0;
		int rank = 1;
		while (index != -1) {
			index = html.indexOf("<li class=\"g\"><h3 class=\"r\"><a href=\"http://www.google.com/url", index);
			if ((index != -1) && (rank <= num)) {
				Result res = new Result();

				int index2 = html.indexOf("</li", index);
				String node = html.substring(index, index2 + 5);

				//get title
				String a = node.substring(node.indexOf("<a"), node.indexOf("</a") + 4);
				Element aElement = Jsoup.parse(a).select("a").first();
				String title = aElement.text();
				res.setTitle(title);
				//System.err.println("Jsoup title: " + res.getTitle());

				//get link				
				String link = aElement.attr("href").replace("http://www.google.com/url?q=", "");
				int temp = link.indexOf("&sa=U");
				link = link.substring(0, temp);
				res.setLink(link);
				//System.err.println("Jsoup link: " + res.getLink());

				//get Snippet
				//Elements tempNode = Jsoup.parse(node).select("cite").remove();
				res.setSnippet(Jsoup.parse(node).getElementsByClass("st").first().text());
				//System.err.println("Jsoup Snippet: " + res.getSnippet());

				//getHtmlTitle
				res.setHtmlTitle(aElement.html());
				//System.err.println("Jsoup HtmlTitle: " + res.getHtmlTitle());

				//getHtmlSnippet
				res.setHtmlSnippet(node);
				//System.err.println("Jsoup Html Snippet: " + res.getHtmlSnippet());
				Document D = new Document(res.getLink(), rank, null);
				D.content = res;
				D.queryText = q;
				map.put(title, D);
				index = index + node.length();
				rank++;
			} else
				break;
		}
		return map;
	}
}
