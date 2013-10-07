package evaluation.linkedData;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import com.hp.hpl.jena.rdf.model.RDFNode;

import ensen.control.SparqlManager;
import ensen.control.db.DBcontroler;

public class PredicateStatistics {

	static void indexPredicateTypes() {
		HashMap<String, HashMap<String, Integer>> PredicateStatistics = new HashMap<String, HashMap<String, Integer>>();
		DBcontroler DBC = new DBcontroler();
		ResultSet Predicates = DBC.getAllPredicat(-1, -1);
		try {
			while (Predicates.next()) {
				String url = Predicates.getString("url");
				int pid = Predicates.getInt("id");
				HashMap<String, Integer> types = new HashMap<String, Integer>();
				System.out.println("-------------------------------");
				System.out.println(url + ": ");
				System.out.println("*******************************");
				com.hp.hpl.jena.query.ResultSet objects = SparqlManager.querySparql("SELECT distinct ?o  WHERE { ?s <" + url + "> ?o .} ");
				while (objects.hasNext()) {
					RDFNode Object = objects.next().get("o");
					if (Object.isURIResource()) {
						try {
							com.hp.hpl.jena.query.ResultSet objectTypes = SparqlManager.querySparql("SELECT distinct ?o  WHERE { <" + Object + "> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> ?o .} ");
							int TypesCount = 0;
							while (objectTypes.hasNext()) {
								TypesCount++;
								RDFNode T = objectTypes.next().get("o");
								Integer count = types.get(T.toString());
								if (count == null)
									types.put(T.toString(), 1);
								else
									types.put(T.toString(), count + 1);
								//System.out.println("the type of (" + Object + ") is Resource");
							}
							if (TypesCount == 0) {
								String tempType = getTypeFromString(Object.asResource().toString());
								Integer count = types.get(tempType);
								if (count == null)
									types.put(tempType, 1);
								else
									types.put(tempType, count + 1);
							}
						} catch (Exception e) {
							e.printStackTrace();
							String tempType = getTypeFromString(Object.asResource().toString());
							Integer count = types.get(tempType);
							if (count == null)
								types.put(tempType, 1);
							else
								types.put(tempType, count + 1);
						}
					} else {
						if (Object.isLiteral()) {
							if (Object.asLiteral().getDatatype() != null) {
								Integer count = types.get(Object.asLiteral().getDatatype().getURI());
								if (count == null)
									types.put(Object.asLiteral().getDatatype().getURI(), 1);
								else
									types.put(Object.asLiteral().getDatatype().getURI(), count + 1);
								//System.out.println("the type of (" + Object + ") is " + Object.asLiteral().getDatatype());
							} else {
								String tempType = getTypeFromString(Object.asLiteral().toString());
								Integer count = types.get(tempType);
								if (count == null)
									types.put(tempType, 1);
								else
									types.put(tempType, count + 1);
							}
						}
					}
				}
				//System.err.println(types);
				if (types != null)
					for (Map.Entry<String, Integer> entry : types.entrySet()) {
						String key = entry.getKey();
						Integer count = entry.getValue();
						DBC.insertPredicateType(pid, key, count);

					}
				DBC.setPredicateAsTyped(pid);
				PredicateStatistics.put(url, types);
			}

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		System.err.println("----------------------Types----------------------------------");
		System.out.println("Predicate Type Freq");

		for (Map.Entry<String, HashMap<String, Integer>> entry : PredicateStatistics.entrySet()) {
			String key = entry.getKey();
			HashMap<String, Integer> types = entry.getValue();
			if (types != null)
				for (Map.Entry<String, Integer> entry2 : types.entrySet()) {
					String key2 = entry2.getKey();
					Integer count = entry2.getValue();
					System.out.println(key + " " + key2 + " " + count);

				}
		}

	}

	static String getTypeFromString(String in) {
		//photo
		if (in.matches("([^\\s]+(\\.(?i)(jpg|png|gif|bmp|svg))$)")) {
			return "photo";
		}
		//Video
		if (in.matches("([^\\s]+(\\.(?i)(avi|mpg|flv|wmv))$)")) {
			return "Video";
		}
		//date
		if (in.matches("(0?[1-9]|[12][0-9]|3[01])/(0?[1-9]|1[012])/((19|20)\\d\\d)")) {
			return "Date";
		}
		//Color 
		if (in.matches("^#([A-Fa-f0-9]{6}|[A-Fa-f0-9]{3})$")) {
			return "Color";
		}
		//Email 
		if (in.matches("^[_A-Za-z0-9-]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$")) {
			return "Email";
		}
		//IP Address 
		if (in.matches("^([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.([01]?\\d\\d?|2[0-4]\\d|25[0-5])$")) {
			return "IP";
		}
		//Time
		if (in.matches("(1[012]|[1-9]):[0-5][0-9](\\s)?(?i)(am|pm)") || in.matches("([01]?[0-9]|2[0-3]):[0-5][0-9]")) {
			return "Time";
		}
		//other

		//link
		if (in.startsWith("http://") || in.startsWith("https://")) {
			return "Link";
		}
		return "else";
	}

	public static void main(String[] args) {
		PredicateStatistics.indexPredicateTypes();
		//System.out.println(PredicateStatistics.getTypeFromString("05:08 am"));

	}
}
