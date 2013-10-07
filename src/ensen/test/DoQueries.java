package ensen.test;

import java.util.ArrayList;

import org.apache.log4j.Logger;

import ensen.control.PropertiesManager;
import ensen.control.Searcher;
import ensen.entities.Document;
import ensen.entities.Query;
import ensen.evaluation.db.EvaluationDBControler;

public class DoQueries {
	public DoQueries() {
		String queriesTogthers = "Plays Shakespeare@French Revolution@difference Marxism Communism@Colon rectum Cancers@History Barack Obama@Yoga Exercises@Periodic table elements@pineal gland functionality@mystery Bermuda Triangle@Al-Andalus taifa kingdoms@Scenic spot Beijing@Nobel peace prize 2012@Nelson Mandela ill@Homer Simpson";
		String[] queries = queriesTogthers.split("@");
		for (String q : queries) {
			System.err.println("DoQueries: " + q);
			try {
				excuteOneQuery(q);
			} catch (Exception e) {

			}
		}
	}

	void excuteOneQuery(String query) {
		double a = 0.125;//in
		double b = 0.05;//out
		double c = 0.2;//freq
		double d = 0.3;//Query
		double e = 0.075;//sim	
		double f = 0.25;//sa	
		Logger LOG = Logger.getLogger(this.getClass());
		Searcher S = new Searcher();
		ArrayList<Document> documents = S.search(query, Integer.parseInt(PropertiesManager.getProperty("nOfRes")));

		Query Q = new Query(query);
		Q.extendQueryLexicaly();
		Q.getResourcesFromLD();

		ArrayList<String> usedImages = new ArrayList<String>();
		for (int i = 0; i < documents.size(); i++) {
			try {
				Document Doc = documents.get(i);
				Doc.usedImages = usedImages;
				//Doc.ensenSession = ensenSession;
				Doc.analyzeDocument(Q, a, b, c, d, e, f, false);
				usedImages = Doc.usedImages;
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
		saveInDB(documents, Q);
	}

	void saveInDB(ArrayList<Document> documents, Query Q) {
		EvaluationDBControler EDBC = new EvaluationDBControler();
		EDBC.insert(Q, documents);
	}

	public static void main(String[] args) {
		DoQueries DQ = new DoQueries();
	}
}
