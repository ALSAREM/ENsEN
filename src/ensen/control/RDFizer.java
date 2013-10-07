package ensen.control;

import java.util.ArrayList;
import java.util.List;

import org.dbpedia.spotlight.exceptions.AnnotationException;
import org.dbpedia.spotlight.model.Text;

import ensen.entities.EnsenDBpediaResource;
import ensen.threads.DBpediaSpotlightThread;

public class RDFizer {

	public static List<EnsenDBpediaResource> rdfizeTextWithoutThreads(String inText, String cachePathBase) {
		//inText = URLEncoder.encode(inText);
		List<EnsenDBpediaResource> out = new ArrayList<EnsenDBpediaResource>();
		int maxTextLen = Integer.parseInt(PropertiesManager.getProperty("maxTextLen"));
		int windowMax = 0;
		int nOfThreads = inText.length() / maxTextLen;
		if (nOfThreads == 0)
			nOfThreads = 1;

		int counter = 0;
		while (windowMax < inText.length()) {
			String cachePath = cachePathBase + "_part" + counter;
			DBpediaSpotlightClient spotlightClient = new DBpediaSpotlightClient();
			spotlightClient.offset = windowMax;
			spotlightClient.local = Boolean.parseBoolean(PropertiesManager.getProperty("DBpediaSpotlightUseLocal"));
			spotlightClient.cachePath = cachePath;
			String text = inText.substring(windowMax, Math.min(windowMax + maxTextLen, inText.length()));
			try {
				List<EnsenDBpediaResource> results = spotlightClient.ensenExtract(new Text(text));
				if (results != null)
					out.addAll(results);
			} catch (AnnotationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			windowMax += maxTextLen;
			counter++;
		}

		return out;

	}

	public static List<EnsenDBpediaResource> rdfizeText(String inText, String cachePathBase) {
		List<EnsenDBpediaResource> out = null;
		int maxTextLen = Integer.parseInt(PropertiesManager.getProperty("maxTextLen"));
		int windowMax = 0;
		int nOfThreads = inText.length() / maxTextLen;
		if (nOfThreads == 0)
			nOfThreads = 1;
		//ExecutorService threadPool = Executors.newFixedThreadPool(nOfThreads);
		ArrayList<DBpediaSpotlightThread> threads = new ArrayList<DBpediaSpotlightThread>();
		int counter = 0;
		while (windowMax < inText.length()) {
			String cachePath = cachePathBase + "_part" + counter;
			DBpediaSpotlightThread thread = new DBpediaSpotlightThread(inText.substring(windowMax, Math.min(windowMax + maxTextLen, inText.length())), cachePath, windowMax);
			threads.add(thread);
			//threadPool.execute(thread);
			windowMax += maxTextLen;
			counter++;
		}

		/*try {
			threadPool.awaitTermination(5000, TimeUnit.MILLISECONDS);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/
		/*while (!threadPool.isShutdown()) {
			try {

				System.out.println(threadPool.toString());
				Thread.currentThread().sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			System.out.println("waiting...");
		}*/
		for (DBpediaSpotlightThread thread : threads) {
			try {
				thread.join();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		for (DBpediaSpotlightThread thread : threads) {

			if (out == null)
				out = thread.results;
			else
				out.addAll(thread.results);

			try {
				thread.interrupt();
				thread.stop();
			} catch (Exception e) {
				// TODO: handle exception
			}
		}

		//threadPool.shutdown();
		return out;
	}
}
