package ensen.util;

import java.io.FileInputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import de.l3s.boilerpipe.BoilerpipeProcessingException;
import de.l3s.boilerpipe.extractors.NumWordsRulesExtractor;

public class HTMLhandler {

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

	public static String loadContent(String uri) {

		return HTMLreader.load(uri);

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
			// TODO: handle exception
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
			System.err.println("error in getTextFromHtmlByBoiler (" + u + "): " + e.getMessage());
		} catch (BoilerpipeProcessingException e) {
			System.err.println("error in getTextFromHtmlByBoiler (" + u + "): " + e.getMessage());
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
		FileContent = FileContent.replaceAll("[^A-Za-z0-9-_ .]", "");
		FileContent = FileContent.replaceAll("(https?://([-\\w\\.]+)+(:\\d+)?(/([\\w/_\\.]*(\\?\\S+)?)?)?)", "");

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
}
