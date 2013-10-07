package ensen.util;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;

import ensen.control.PropertiesManager;

public class AutoCompleteGenerator {

	public AutoCompleteGenerator() {
		// TODO Auto-generated constructor stub
	}

	public void generate() {
		try {
			String queryString = "select distinct ?label  ?cat FROM <http://dbpedia.org> where {?Concept <http://www.w3.org/2000/01/rdf-schema#label> ?label. ?Concept <http://purl.org/dc/terms/subject> ?cat. FILTER(langMatches(lang(?label), 'EN')) } offset 15500 limit 5000";
			//System.out.println(queryString);
			Query query = QueryFactory.create(queryString);
			QueryExecution qexec = QueryExecutionFactory.sparqlService(PropertiesManager.getProperty("DBpediaSparql"), query);
			try {
				ResultSet results = qexec.execSelect();
				int i = 0;
				for (; results.hasNext();) {
					QuerySolution QS = results.nextSolution();
					//System.out.println("{ label: \"" + QS.get("label").asLiteral().getString() + "\", category: \"" + QS.get("cat").asResource().getLocalName() + "\" },");
					i++;
				}
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				qexec.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		AutoCompleteGenerator ACG = new AutoCompleteGenerator();
		ACG.generate();

	}
}
