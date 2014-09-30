package ensen.controler;

import java.util.ArrayList;
import java.util.List;

import org.dbpedia.spotlight.exceptions.AnnotationException;
import org.dbpedia.spotlight.model.Text;

import ensen.entities.EnsenDBpediaResource;
import ensen.util.PropertiesManager;

public class RDFizer {

	public static List<EnsenDBpediaResource> rdfizeTextWithoutThreads(String inText, String confiance, String file) {

		List<EnsenDBpediaResource> out = new ArrayList<EnsenDBpediaResource>();
		int maxTextLen = Integer.parseInt(PropertiesManager.getProperty("maxTextLen"));
		int windowMax = 0;

		int counter = 0;
		while (windowMax < inText.length()) {
			DBpediaSpotlightClient spotlightClient = new DBpediaSpotlightClient();
			spotlightClient.offset = windowMax;
			spotlightClient.local = Boolean.parseBoolean(PropertiesManager.getProperty("DBpediaSpotlightUseLocal"));
			String text = inText.substring(windowMax, Math.min(windowMax + maxTextLen, inText.length()));
			try {
				List<EnsenDBpediaResource> results = null;
				if (confiance == "")
					results = spotlightClient.ensenExtract(new Text(text), file);
				else
					results = spotlightClient.ensenExtract(new Text(text), confiance, "", file);
				if (results != null)
					out.addAll(results);
			} catch (AnnotationException e) {

				e.printStackTrace();
			}

			windowMax += maxTextLen;
			counter++;
			/*int max = Integer.parseInt(PropertiesManager.getProperty("MaxDBpediaSpotlightQ"));
			if (counter == max)
				break;*/
		}

		return out;

	}

}
