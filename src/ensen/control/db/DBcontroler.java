package ensen.control.db;

import java.io.StringReader;
import java.io.StringWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import cern.colt.Version;
import ensen.control.RDFManager;
import ensen.entities.Document;
import ensen.entities.EnsenDBpediaResource;

public class DBcontroler {
	public Connection con = null;
	Statement st = null;
	ResultSet rs = null;

	public DBcontroler() {
		String url = ensen.control.PropertiesManager.getProperty("dbhost");
		String user = ensen.control.PropertiesManager.getProperty("dbuser");
		String password = ensen.control.PropertiesManager.getProperty("dbpass");
		try {
			Class.forName("com.mysql.jdbc.Driver");
			con = DriverManager.getConnection(url, user, password);
		} catch (SQLException ex) {
			ex.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			try {
				if (rs != null) {
					rs.close();
				}
				if (st != null) {
					st.close();
				}

			} catch (SQLException ex) {
				Logger lgr = Logger.getLogger(Version.class.getName());
				lgr.log(Level.WARNING, ex.getMessage(), ex);
			}
		}
	}

	public int insertDoc(String uri, String txt, String graph, String AS) {
		try {
			PreparedStatement pst = con.prepareStatement("INSERT INTO doc(uri,text,graph,annotations) VALUES(?,?,?,?)");
			pst.setString(1, uri);
			pst.setString(2, txt);
			pst.setString(3, graph);
			pst.setString(4, AS);
			int res = pst.executeUpdate();

			return res;

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return -1;
		}
	}

	public int getPredicatFreqByType(String predicate, String Type) {
		PreparedStatement pst;
		try {
			pst = con.prepareStatement("Select  pt.freq as f  From   `dbpedia`.`pretype` as pt Inner Join  `dbpedia`.`type` as t On pt.idtype = t.idtype Inner Join  `dbpedia`.`predicate` as p On pt.idpre = p.idpre");
			java.sql.ResultSet res = pst.executeQuery();
			while (res.next()) {
				int f = res.getInt("f");
				return f;
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return 0;
	}

	public double getPredicatZScoreByType(String predicate, String Type) {
		PreparedStatement pst;
		try {
			//pst = con.prepareStatement("Select  pt.zscore as z  From   `dbpedia`.`pretype` as pt Inner Join  `dbpedia`.`type` as t On pt.idtype = t.idtype Inner Join  `dbpedia`.`predicate` as p On pt.idpre = p.idpre");
			pst = con.prepareStatement("Select  zscore as z  From   `dbpedia`.`prediactsoftypes` where turl=? and purl=?");
			pst.setString(1, Type);
			pst.setString(2, predicate);

			java.sql.ResultSet res = pst.executeQuery();
			while (res.next()) {
				double f = res.getDouble("z");
				return f;
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return 0;
	}

	public int insertDoc(Document D) {
		StringWriter outW = new StringWriter();
		String syntax = "RDF/XML-ABBREV"; // also try "N-TRIPLE"
		D.fullGraph.write(outW, syntax);
		String gString = outW.toString();
		String asString = "";
		if (D.annotationSemantic != null) {
			D.annotationSemantic.write(outW, syntax);
			asString = outW.toString();
		}
		return insertDoc(D.url, D.text, gString, asString);
	}

	public Document getDocument(String uri) {
		try {
			PreparedStatement pst = con.prepareStatement("SELECT * FROM doc WHERE uri='" + uri + "' ");

			rs = pst.executeQuery();
			Document D = null;
			int docId = -1;
			while (rs.next()) {
				D = new Document();
				docId = rs.getInt("id");
				D.url = rs.getString("uri");
				D.text = rs.getString("text");
				StringReader SR = new StringReader(rs.getString("graph"));
				String syntax = "RDF/XML-ABBREV"; // also try "N-TRIPLE"
				D.fullGraph = RDFManager.createRDFModel();
				D.fullGraph.read(SR, syntax);
				if (rs.getString("annotations") != "") {
					SR = new StringReader(rs.getString("annotations"));
					D.annotationSemantic = RDFManager.createRDFModel();
					D.annotationSemantic.read(SR, syntax);
				}
				break;
			}

			// RDFizing results
			if (D != null) {
				pst = con.prepareStatement("SELECT * FROM docresource dr,resource r WHERE r.id=dr.resid and dr.docid= '" + docId + "' ");

				rs = pst.executeQuery();
				D.triplets = new ArrayList<EnsenDBpediaResource>();
				while (rs.next()) {
					EnsenDBpediaResource DBR = new EnsenDBpediaResource(rs.getString("uri"));
					DBR.setSupport(rs.getInt("support"));
					D.triplets.add(DBR);
				}
			}

			return D;
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}

	public int insertPredicate(String uri, double ptf, double tf, double pidf, double idf) {
		try {

			PreparedStatement pst = con.prepareStatement("INSERT INTO `ensen.predicats`.`Predicate` (`url`,`ptf`,`tf`,`pidf`,`idf`) VALUES(?,?,?,?,?)");
			pst.setString(1, uri);
			pst.setDouble(2, ptf);
			pst.setDouble(3, tf);
			pst.setDouble(4, pidf);
			pst.setDouble(5, idf);

			int res = pst.executeUpdate();

			return res;

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return -1;
		}
	}

	public int insertPredicateType(int pid, String type, int freq) {
		try {

			PreparedStatement pst = con.prepareStatement("INSERT INTO `ensen.predicats`.`types` (`type`,`freq`,`pid`) VALUES (?,?,?)");
			pst.setString(1, type);
			pst.setInt(2, freq);
			pst.setInt(3, pid);

			int res = pst.executeUpdate();

			return res;

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return -1;
		}
	}

	public int setPredicateAsTyped(int pid) {
		try {

			PreparedStatement pst = con.prepareStatement("UPDATE `ensen.predicats`.`predicate` SET `types` = 2 WHERE id=" + pid);
			int res = pst.executeUpdate();

			return res;

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return -1;
		}
	}

	public double getPredicatPIDF(String uri) {
		try {
			PreparedStatement pst = con.prepareStatement("SELECT * FROM `ensen.predicats`.`Predicate` where url=? ");
			pst.setString(1, uri);
			ResultSet res = pst.executeQuery();
			double idf = 0.0;
			if (res.next()) {
				idf = res.getDouble("pidf");

			}
			return idf;

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return 0;
		}
	}

	public double getPredicatTF(String uri) {
		try {

			PreparedStatement pst = con.prepareStatement("SELECT * FROM `ensen.predicats`.`Predicate` where url=? ");
			pst.setString(1, uri);

			ResultSet res = pst.executeQuery();
			double tf = 0.0;
			if (res.next())
				tf = res.getDouble("tf");
			return tf;

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return 0;
		}
	}

	public ResultSet getAllPredicat(int limit, int offset) {
		try {
			String QString = "SELECT * FROM `ensen.predicats`.`Predicate` where types!=2";
			if (limit != -1)
				QString += " limit " + limit;
			if (offset != -1)
				QString += " offset " + offset;

			PreparedStatement pst = con.prepareStatement(QString);
			ResultSet res = pst.executeQuery();
			return res;
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
	}

	public static void main(String[] args) {
		DBcontroler DBC = new DBcontroler();
		// System.out.println(DBC.getPredicatIDF("http://www.w3.org/2000/01/rdf-schema#label"));
	}

}
