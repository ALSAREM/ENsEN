package ensen.threads;

import java.util.List;

import org.dbpedia.spotlight.exceptions.AnnotationException;
import org.dbpedia.spotlight.model.Text;

import ensen.control.DBpediaSpotlightClient;
import ensen.control.PropertiesManager;
import ensen.entities.EnsenDBpediaResource;

public class DBpediaSpotlightThread extends Thread {

	private DBpediaSpotlightClient spotlightClient;
	String inText;
	public List<EnsenDBpediaResource> results;

	public DBpediaSpotlightThread(String inText, String cachePathParm, int offset) {
		super(); // Store the thread name
		this.spotlightClient = new DBpediaSpotlightClient();
		this.spotlightClient.offset = offset;
		this.spotlightClient.local = Boolean.parseBoolean(PropertiesManager.getProperty("DBpediaSpotlightUseLocal"));
		this.inText = inText;
		this.spotlightClient.cachePath = cachePathParm;
		start();
		/*	try {
				sleep(100);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}*/
	}

	public String toString() {
		return "entry" + this.getState().toString();
	}

	public void run() {
		try {
			results = this.spotlightClient.ensenExtract(new Text(inText));
			stop();
			interrupt();
		} catch (AnnotationException e) {
			e.printStackTrace();
		}

	}

}