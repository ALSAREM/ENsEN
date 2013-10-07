<%@page import="ensen.control.PropertiesManager"%>
<%@page import="java.io.FileOutputStream"%>
<%@page import="java.io.OutputStreamWriter"%>
<%@page import="java.io.Writer"%>
<%@page import="java.util.Iterator"%>
<%@page import="java.util.List"%>
<%@page import="ensen.entities.Query"%>
<%@page import="ensen.control.QueryHandler"%>
<%@page import="ensen.entities.Document"%>
<%@page import="java.util.ArrayList"%>
<%@page import="org.apache.log4j.Logger"%>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%
	/* get data*/
	HttpSession ensenSession = request.getSession();
	String query = request.getParameter("q");
	ArrayList<Document> documents = (ArrayList<Document>) ensenSession.getAttribute("documents");

	/* Query Analysis */
	Query Q = new Query(query);
	Q.extendQueryLexicaly();
	Q.getResourcesFromLD();
	/*Writer QOut = new OutputStreamWriter(new FileOutputStream(PropertiesManager.getProperty("testingPath") + "files\\q.txt"));
	String LS = System.getProperty("line.separator");
	try {
		QOut.write(Q.Text + LS);
		QOut.write(Q.ExtendedText + LS);	
		for(String res:Q.RelatedResLD){
			QOut.write(res + LS);	
		}
		QOut.write("?@?" + LS);
		for(String res:Q.QExtendedRes){
			QOut.write(res + LS);	
		}
		
	} catch (Exception e) {
		e.printStackTrace();
	} finally {
		QOut.close();
	}
	
	*/

	ensenSession.setAttribute("documents", documents);
	ensenSession.setAttribute("Query", Q);
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<link rel="stylesheet" type="text/css" href="../css/style.css">
<link rel="stylesheet" type="text/css" href="../css/popup.css">
<script src="../js/css-pop.js"></script>
<title>ENsEN: Query Analysis</title>
</head>
<body>
	<div id="wrapper">
		<header id="header" class="clearfix" role="banner"> <hgroup>
		<h1 id="site-title">
			<a href="index.jsp">Enhanced Search Engine</a>
		</h1>
		<h2 id="site-description">A better engine with Linked Data</h2>
		</hgroup> </header>
		<!-- #header -->
		<div id="main" class="clearfix">
			<div role="main" class="wideContent">
				<form action="resList.jsp" class="searchform">
					<input type="search" results="10" size="50" name="q" placeholder="Search..." value="<%=query%>" /> <input type="submit" value="Search" />
				<a href="resultsPage.jsp" class="more-link">Finish:Documents Analysis</a>
				</form>
				<!-- .searchform -->
				<hr />
								<h2 class="entry-title">Query Analysis</h2>
				
				
				<hr />
				<h3 class="entry-title">
					Query Extended Text (<%=Q.ExtendedText.split(" ").length%>Terms)
				</h3>
				<div>
					<%=Q.ExtendedText%>
				</div>
				<hr />
				<aside>
				<ul>
					<%
						if (Q.RelatedResLD != null) {
							Iterator<String> It3 = Q.RelatedResLD.iterator();
							while (It3.hasNext()) {
								String res = It3.next();
								out.print("<li><a href=\"" + res + "\" target='_blank'>" + res + "</a> </li>");
							}
						} else
							out.print("No Results");
					%>
				</ul>
				</aside>
				<hr />
			</div>
			<!-- #content -->
		</div>
		<!-- #main -->
		<footer id="footer"> <!-- You're free to remove the credit link to Jayj.dk in the footer, but please, please leave it there :) -->
		<p>
			Copyright &copy; 2013 <a href="#" target="_blank">INSA-Lyon</a> <span class="sep">|</span> Design by <a href="http://liris.cnrs.fr/" title="Design by DRIM-LIRIS" target="_blank">DRIM-LIRIS</a>
		</p>
		</footer>
		<!-- #footer -->
		<div class="clear"></div>
	</div>
	<!-- #wrapper -->
</body>
</html>