package ensen.control.dbpedia;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.sparql.engine.http.QueryEngineHTTP;

import ensen.control.PropertiesManager;
import ensen.control.SparqlManager;
import ensen.control.db.DBcontroler;
import ensen.util.MathTools;

public class Indexer {

	public static void predicateIndexing() {
		DBcontroler db = new DBcontroler();
		Connection con = db.con;
		SparqlManager sparq = new SparqlManager();
		// insertTypesInDB(con, sparq);
		// insertCatsInDB(con, sparq);
		indexPredicats(con, sparq);
	}

	static void insertTypesInDB(Connection con, SparqlManager sparq) {
		// get all types
		String sparqlQ = "select distinct ?t where {?t a <http://www.w3.org/2002/07/owl#Class>}";
		ResultSet res = sparq.querySparql(sparqlQ);
		while (res.hasNext()) {
			RDFNode t = res.next().get("t");
			if (t.isURIResource()) {
				String insertS = "INSERT INTO `dbpedia`.`type`(`url`,`total`)VALUES(?,?);";
				java.sql.PreparedStatement pst;
				try {
					pst = con.prepareStatement(insertS);
					pst.setString(1, t.asResource().getURI());
					pst.setInt(2, 0);
					pst.executeUpdate();
					System.out.println(t.toString());
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}

	static void insertCatsInDB(Connection con, SparqlManager sparq) {
		// get all cats
		String sparqlQ = "SELECT DISTINCT ?concept where {?s <http://purl.org/dc/terms/subject> ?concept.} offset 600157";
		ResultSet res = sparq.querySparql(sparqlQ);
		int i = 0;
		while (res.hasNext()) {
			System.err.println(i++);
			RDFNode t = res.next().get("concept");
			if (t.isURIResource()) {
				String insertS = "INSERT INTO `dbpedia`.`cat`(`url`,`total`)VALUES(?,?);";
				java.sql.PreparedStatement pst;
				try {
					pst = con.prepareStatement(insertS);
					pst.setString(1, t.asResource().getURI());
					pst.setInt(2, 0);
					pst.executeUpdate();
					System.out.println(t.toString());
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}

	static void indexPredicats(Connection con, SparqlManager sparq) {
		// get all types from DB
		PreparedStatement pst;
		try {
			pst = con.prepareStatement("select * from `dbpedia`.`type` where idtype not in (SELECT distinct idtype FROM dbpedia.pretype)");
			java.sql.ResultSet res = pst.executeQuery();
			int tCounter = 0;
			while (res.next()) {// for each type --> count predicats
				System.err.println(tCounter++);
				String type = res.getString("url");
				int tid = res.getInt("idtype");
				String q1 = "SELECT ?p (COUNT(*) AS ?Ps) WHERE{  ?s <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <" + type + "> .  ?s ?p ?o.} GROUP BY (?p)";
				System.out.println(q1);
				ResultSet ps = SparqlManager.queryCount(q1);

				System.err.println(ps.hasNext());
				int i = 0;
				while (ps.hasNext()) {
					System.err.println(i++);
					QuerySolution QS = ps.next();
					RDFNode p = QS.get("p");
					RDFNode count = QS.get("Ps");
					if (p != null) {
						int pId = -1;
						String select = "select * from `dbpedia`.`predicate` where `url` = ?";
						java.sql.PreparedStatement selectP = con.prepareStatement(select);
						selectP.setString(1, p.toString());
						java.sql.ResultSet pred = selectP.executeQuery();
						if (pred.next()) {
							pId = pred.getInt("idpre");
						} else {
							try {
								String insertP = "INSERT INTO `dbpedia`.`predicate` (`url`)VALUES(?);";
								java.sql.PreparedStatement inpstP;
								inpstP = con.prepareStatement(insertP, Statement.RETURN_GENERATED_KEYS);
								inpstP.setString(1, p.toString());
								inpstP.executeUpdate();
								java.sql.ResultSet rs = inpstP.getGeneratedKeys();
								if (rs != null && rs.next()) {
									pId = rs.getInt(1);
								}
							} catch (Exception e) {
								System.err.println(e.getMessage() + " in INSERT predicate:" + p.toString());
							}
						}
						System.out.println("Predicate: " + p + " id= " + pId);

						String insertS = "INSERT INTO `dbpedia`.`pretype` (`idtype`,`idpre`,`freq`) VALUES(?,?,?);";
						java.sql.PreparedStatement inpst;
						try {
							inpst = con.prepareStatement(insertS);
							inpst.setInt(1, tid);
							inpst.setInt(2, pId);
							inpst.setInt(3, (Integer) count.asLiteral().getValue());
							inpst.executeUpdate();
							System.out.println("type:" + type + " p:" + p.toString() + " freq:" + (Integer) count.asLiteral().getValue());

						} catch (Exception e) {
							System.err.println(e.getMessage() + " in type:" + type + " p:" + p.toString() + " freq:" + (Integer) count.asLiteral().getValue());
						}

					}
				}
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	void countCats() {
		String sparqlQ = "SELECT count(DISTINCT ?concept) as ?c where {?s <http://purl.org/dc/terms/subject> ?concept.}";
		QueryEngineHTTP qe = new QueryEngineHTTP(PropertiesManager.getProperty("DBpediaSparql"), sparqlQ);
		ResultSet res = qe.execSelect();
		while (res.hasNext()) {
			System.out.println(res.next().get("c"));
		}
	}

	private static void zScore(Connection con) {
		try {
			// get all types
			PreparedStatement pst = con.prepareStatement("select * from `dbpedia`.`type` where idtype = 1");
			java.sql.ResultSet res = pst.executeQuery();
			int tCounter = 0;
			while (res.next()) {// for each type
				System.out.println("idtype" + res.getInt("idtype"));
				// calculate Z-score
				zScoreInType(con, res.getInt("idtype"));
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private static void zScoreInType(Connection con, int typeId) {
		// get all predicates with freq
		HashMap<Integer, Integer> predicates = new HashMap<Integer, Integer>();
		double avg = 0;
		int counter = 0;
		String select = "select * from `dbpedia`.`pretype` where `idtype` = ?";
		java.sql.PreparedStatement selectP;
		try {
			selectP = con.prepareStatement(select);
			selectP.setInt(1, typeId);
			java.sql.ResultSet pred = selectP.executeQuery();
			while (pred.next()) {
				predicates.put(pred.getInt("idpre"), pred.getInt("freq"));

				avg += pred.getInt("freq");
				counter++;
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// calculate mean
		if (counter > 0)
			avg = avg / counter;
		System.out.println("avg: " + avg);
		// calculate SD
		double sd = MathTools.calculateStandardDeviation(predicates.values(), avg);
		System.out.println("SD: " + sd);
		counter = 0;
		// for each predicate
		for (Map.Entry<Integer, Integer> entry : predicates.entrySet()) {
			// calculate Z-score
			double zScore = MathTools.calculateZScore(sd, entry.getValue(), avg);
			// insert in DB
			String up = "update `dbpedia`.`pretype` set zscore= ? where `idtype` = ? and `idpre` = ? ";
			try {
				selectP = con.prepareStatement(up);
				selectP.setDouble(1, zScore);
				selectP.setInt(2, typeId);
				selectP.setInt(3, entry.getKey());
				selectP.executeUpdate();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			System.out.println(typeId + ": " + (counter++));
		}
		System.out.println("*********************************");

	}

	public static void main(String[] args) {
		//Indexer.predicateIndexing();
		DBcontroler db = new DBcontroler();
		Connection con = db.con;
		//zScore(con);
		zScoreInType(con, 124);
		zScoreInType(con, 125);
		zScoreInType(con, 126);
		zScoreInType(con, 330);

	}
}
