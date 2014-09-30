package ensen.indexers;

import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;

import ensen.controler.SparqlManager;

public class WikipediaIndexer {

	public static void index() {
		ResultSet Res = SparqlManager.querySparql("select distinct * where {?s <http://xmlns.com/foaf/0.1/isPrimaryTopicOf> ?l } limit 100");
		while (Res.hasNext()) {
			QuerySolution S = Res.next();
			String url = S.get("l").toString();
			System.out.println(url);
		}

	}

	public static void main(String[] args) {
		index();
	}
}
