package evaluation.linkedData;

import com.hp.hpl.jena.query.ResultSet;

import ensen.control.SparqlManager;
import ensen.control.db.DBcontroler;

public class PredicateTfIdf {
	double NofSubjects = 20309342.0;//3769926.0;
	double NofPredicates = 48293.0;
	double Nofstatments = 65143840.0;

	void calculatePredicateTFIDF() {
		double ptf = 0.0;
		double tf = 0.0;
		double pidf = 0.0;
		double idf = 0.0;
		int counter = 0;
		DBcontroler DBC = new DBcontroler();

		ResultSet res = SparqlManager.querySparql("SELECT distinct ?p WHERE { ?s ?p ?o .} limit 10000 offset 40000");
		while (res.hasNext()) {
			String p = res.next().get("p").toString();
			counter++;
			//TF
			ResultSet res2 = SparqlManager.querySparql("SELECT (count (?s) as ?tf ) WHERE { ?s <" + p + "> ?o .} ");
			ptf = res2.next().getLiteral("tf").getInt();
			tf = ptf / Nofstatments;
			//IDF
			ResultSet res3 = SparqlManager.querySparql("SELECT (count (distinct ?s) as ?pidf)  WHERE { ?s <" + p + "> ?o .} ");
			pidf = res3.next().getLiteral("pidf").getInt();

			idf = Math.log(NofSubjects / pidf);
			System.out.println((counter) + "- Predicat (" + p + "): ptf= " + ptf + ", tf= " + tf + ", pidf= " + pidf + ", idf= " + idf);
			DBC.insertPredicate(p, ptf, tf, pidf, idf);
			System.out.println("TF-IDF for (" + p + ")= " + (tf * idf));
		}

	}

	void calculatePredicateIDF() {
		ResultSet res = SparqlManager.querySparql("SELECT (count (distinct ?s) as ?pidf)  WHERE { ?s  ?p ?o .} ");
		System.err.println(res.next().get("counter"));
		/*	while (res.hasNext()) {
				int allcount = 0;
				int counter = 1;
				//calculate the total n of subjects (resources)
				while (counter > 0) {
					counter = 0;
					ResultSet res2 = SparqlManager.querySparql("SELECT distinct ?s WHERE { ?s <" + res.next().get("p") + "> ?o .  } limit 100000 offset " + allcount);
					while (res2.hasNext()) {
						counter++;
						res2.next();
					}
					allcount += counter;
				}
				System.err.println(allcount + ": " + res.next().get("p"));
				//System.err.println(counter + ": " + res.next().get("s"));
			}*/

	}

	public static void main(String[] args) {
		PredicateTfIdf PTFIDF = new PredicateTfIdf();
		PTFIDF.calculatePredicateTFIDF();

	}
}
