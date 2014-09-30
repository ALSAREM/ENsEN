package ensen.util;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.xml.sax.InputSource;

import de.l3s.boilerpipe.BoilerpipeProcessingException;
import de.l3s.boilerpipe.document.TextDocument;
import de.l3s.boilerpipe.extractors.ArticleExtractor;
import de.l3s.boilerpipe.extractors.CommonExtractors;
import de.l3s.boilerpipe.extractors.NumWordsRulesExtractor;
import de.l3s.boilerpipe.sax.BoilerpipeSAXInput;
import de.l3s.boilerpipe.sax.HTMLDocument;
import de.l3s.boilerpipe.sax.HTMLFetcher;

public class HTMLhandler {
	static Logger log = Logger.getLogger(HTMLhandler.class.getName());

	public HTMLhandler() {

	}

	public static Object[] getTopWords(Object[] wordsList) {
		int top = 0;
		FileInputStream in;
		Properties applicationProps = new Properties();
		try {
			in = new FileInputStream("appProperties");
			applicationProps.load(in);
			in.close();
			top = Integer.parseInt(applicationProps.getProperty("WordCount"));

		} catch (Exception e) {
			e.printStackTrace();
		}

		Map<String, Integer> WordsCounter = new HashMap<String, Integer>();
		int max = 0;
		for (Object word : wordsList) {
			Integer oldCount = WordsCounter.get(word);
			if (oldCount == null) {
				oldCount = 0;
			}
			WordsCounter.put((String) word, oldCount + 1);
			if (max < (oldCount + 1))
				max = oldCount + 1;
		}
		int size = WordsCounter.size();
		if (top == 0)
			top = size;

		Object[] resFinal = new Object[top];
		int counter = 0;
		for (int i = max; i >= 0; i--) {
			for (String key : WordsCounter.keySet()) {
				int value = WordsCounter.get(key);
				if ((value == i) && (counter < top)) {
					resFinal[counter++] = key;
				}

			}

		}

		for (int i = counter; i < top; i++) {
			resFinal[i] = "";
		}
		return resFinal;

	}

	public static HTMLDocument getHTMLDoc(String uriIn) {
		String uri = uriIn;
		URL url;
		HTMLDocument docHTML = null;
		try {
			uri = URLDecoder.decode(uri);
			url = new URL(uri);
			try {
				docHTML = HTMLFetcher.fetch(url);
			} catch (Exception e) {
				System.err.println("retry to fetch " + uriIn);
				url = new URL(uriIn);
				docHTML = HTMLFetcher.fetch(url);
			}
		} catch (Exception e) {
			System.err.println("Error in loading the content of " + uri + " " + e.getMessage());
		}
		return docHTML;
	}

	public static String loadContent(String uriIn, HTMLDocument docHTML) {
		String text = "";
		try {
			InputSource is = docHTML.toInputSource();
			is.setEncoding("UTF-8");
			BoilerpipeSAXInput in = new BoilerpipeSAXInput(is);
			String charset = docHTML.getCharset().toString();

			if (charset == null)
				charset = "UTF-8";

			Document pageDocument = Jsoup.parse(new String(docHTML.getData()));
			TextDocument doc = in.getTextDocument();

			if (uriIn.contains("wikipedia.org/wiki/")) {
				//0- wikipedia pages
				text = CommonExtractors.CANOLA_EXTRACTOR.INSTANCE.getText(doc);

			}

			//1- try using boilerpipe --> ArticleExtractor (SAX)
			if (text.length() < Integer.parseInt(PropertiesManager.getProperty("HtmlMinTextLength"))) {
				text = ArticleExtractor.INSTANCE.getText(doc);

			}
			if (text.length() < Integer.parseInt(PropertiesManager.getProperty("HtmlMinTextLength"))) {
				//2- try using boilerpipe --> CANOLA_EXTRACTOR (SAX)
				text += " " + CommonExtractors.CANOLA_EXTRACTOR.INSTANCE.getText(doc);

			}
			if (text.length() < Integer.parseInt(PropertiesManager.getProperty("HtmlMinTextLength"))) {
				//3- try JSoup
				text = HTMLhandler.CleanContent(pageDocument.html());

			}


			text += getMeta(pageDocument);

		} catch (Exception e) {
			System.err.println("Error in loading the content of " + uriIn + " " + e.getMessage());
		}

		return text;
		//return HTMLreader.load(uri);

	}

	private static String getMeta(Document pageDocument) {
		String out = "";
		try {
			Elements meta = pageDocument.select("meta[name*=description], meta[name*=keywords], meta[name*=Title], meta[property*=title], meta[property*=keywords], meta[property*=description]");
			for (Element m : meta) {
				out += " " + m.attr("content");
			}
			out += " " + pageDocument.select("title").get(0).ownText();
		} catch (Exception e) {

		}
		return out;
	}

	public static String inforceMainContentByBoiler(String url, String text) {
		String res = text;
		String mainContent = getTextFromHtmlByBoiler(url);
		/*	String[] mainPhs = mainContent.split(".");
			for (String MainPh : mainPhs) {
				if (!res.contains(MainPh)) {
					res += MainPh;
				}
			}*/
		try {
			if ((mainContent.length() * 1.0 / res.length()) < 0.4)
				res += " " + mainContent;
		} catch (Exception e) {

		}

		return res;
	}

	public static String getTextFromHtmlByBoiler(String u) {
		URL url = null;
		String text = "";
		try {
			//System.out.println("-------Boiler------------");
			url = new URL(u);
			text = NumWordsRulesExtractor.INSTANCE.getText(url).replaceAll("(\r\n|\n)", ".\n").replace(". .", ".").replace("..", ".").replaceAll("[^A-Za-z0-9-_ .]", "").replaceAll("(https?://([-\\w\\.]+)+(:\\d+)?(/([\\w/_\\.]*(\\?\\S+)?)?)?)", "");
			//System.out.println(text);

		} catch (MalformedURLException e) {
			System.out.println("error in getTextFromHtmlByBoiler (" + u + "): " + e.getMessage());
		} catch (BoilerpipeProcessingException e) {
			System.out.println("error in getTextFromHtmlByBoiler (" + u + "): " + e.getMessage());
		}

		return text;
	}

	public static String CleanContent(String fc) {
		String FileContent = fc;
		//preparsing treatment
		Document doc = Jsoup.parse(FileContent);
		Elements lis = doc.select("li");
		for (Element li : lis) {
			li.wrap("<span><span> </span>.</span>");
		}

		Elements uls = doc.select("ul");
		for (Element ul : uls) {
			ul.wrap("<span>.<span> </span></span>");
		}

		/*Elements Ps = doc.select("p");
		for (Element p : Ps) {
			p.wrap("<p><span> </span>.</p>");
		}*/

		// get text
		FileContent = doc.text();
		FileContent = FileContent.replaceAll("\\[[0-9]*\\]", " ");
		//FileContent = FileContent.replaceAll("[^A-Za-z0-9-_ .,']", " ");

		FileContent = FileContent.replaceAll("(https?://([-\\w\\.]+)+(:\\d+)?(/([\\w/_\\.]*(\\?\\S+)?)?)?)", " ");
		FileContent = FileContent.replaceAll("  ", " ");
		/*
				// remove specials chars
				FileContent = FileContent.replaceAll("[^\\p{L}\\ ]", "   ");

				// remove words start with: http
				FileContent = FileContent.replaceAll("http\\p{L}+ ", "");

				// remove small words
				FileContent = FileContent.replaceAll("\\W", " ");

				FileContent = FileContent.replaceAll(" \\S{1,4} ", " ");

				FileContent = FileContent.replaceAll(" \\s{1,} ", " ");

		*/
		return FileContent;
	}

	public static Object[] splitContent(String FileContent) {
		// split content
		String[] wordsArray = FileContent.split(" ");
		HashSet<String> words = new HashSet<String>();
		for (int i = 0; i < wordsArray.length; i++) {
			words.add(wordsArray[i]);
		}

		Object[] wordsList = words.toArray();

		return wordsList;
	}

	public static String readURLHTMLasString(String url) {
		String out = "";
		URL oracle;
		try {
			oracle = new URL(url);
			BufferedReader in = new BufferedReader(new InputStreamReader(oracle.openStream()));
			String inputLine;
			while ((inputLine = in.readLine()) != null)
				out += inputLine;
			in.close();
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return out;
	}

	public static String readURLTxtasString(String url) {
		String out = "";
		URL oracle;
		try {
			oracle = new URL(url);
			BufferedReader in = new BufferedReader(new InputStreamReader(oracle.openStream()));
			String inputLine;
			while ((inputLine = in.readLine()) != null)
				out += inputLine + "\n";
			in.close();
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return out;
	}
}
