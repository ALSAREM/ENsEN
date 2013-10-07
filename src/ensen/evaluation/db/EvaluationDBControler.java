package ensen.evaluation.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.lang.StringEscapeUtils;

import cern.colt.Version;
import ensen.entities.Document;
import ensen.entities.Query;

public class EvaluationDBControler {
	Connection con = null;
	Statement st = null;
	ResultSet rs = null;

	public EvaluationDBControler() {
		String url = "jdbc:mysql://localhost:3306/ensen.evaluation";
		String user = "root";
		String password = "m7850348";
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
				ex.printStackTrace();
			}
		}
	}

	public int insert(Query q, ArrayList<Document> docs) {

		try {
			String dString = "DELETE from `ensen.evaluation`.`query` where `text`='" + q.Text + "' ";

			String qString = "INSERT INTO `ensen.evaluation`.`query` (`idquery`,`text`,`type`) VALUES(null,'" + q.Text + "','general')";

			//System.out.println(qString);
			Statement stmt = con.createStatement();
			stmt.executeUpdate(dString);
			stmt.executeUpdate(qString, Statement.RETURN_GENERATED_KEYS);
			ResultSet generatedKeys = stmt.getGeneratedKeys();
			if (generatedKeys.next()) {
				int index = generatedKeys.getInt(1);
				int counter = 0;

				for (Document doc : docs) {
					try {
						if (doc.content.getHtmlSnippet() != null) {
							byte[] bytes = doc.content.getHtmlSnippet().getBytes();
							//qString = "INSERT INTO `ensen.evaluation`.`result` (`qid`,`url`,`oldSnippet`,`newSnippet`,`rank`) VALUES(" + index + ",\"" + doc.url + "\",\"" + bytes + ",\"" + StringEscapeUtils.escapeHtml(doc.multiZoneSnippet.toString()) + "\"," + doc.Rank + ")";
							PreparedStatement st = con.prepareStatement("INSERT INTO `ensen.evaluation`.`result` (`qid`,`url`,`oldSnippet`,`newSnippet`,`rank`,`html`) VALUES(?,?,?,?,?,?)");
							st.setInt(1, index);
							st.setString(2, doc.url);
							st.setBytes(3, bytes);
							st.setString(4, StringEscapeUtils.escapeHtml(doc.multiZoneSnippet.toString()));
							st.setInt(5, doc.Rank);
							st.setString(6, StringEscapeUtils.escapeHtml(doc.html));

							st.executeUpdate();
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
					counter++;
				}
				return index;
			}
			return -1;
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return -1;
		}

	}

}
