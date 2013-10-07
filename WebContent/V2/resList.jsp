<%@page import="ensen.control.PropertiesManager"%>
<%@page import="ensen.entities.Document"%>
<%@page import="java.util.ArrayList"%>
<%@page import="ensen.control.Searcher"%>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%
	String query = request.getParameter("q");
	Searcher S = new Searcher();
	ArrayList<Document> documents = S.search(query, Integer.parseInt(PropertiesManager.getProperty("nOfRes")));
	HttpSession ensenSession = request.getSession();
	ensenSession.setAttribute("documents", documents);
	ensenSession.setAttribute("q", query);
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<link rel="stylesheet" type="text/css" href="../css/style.css">
<script src="../js/modernizr-1.7.min.js"></script>
<script src="../js/respond.min.js"></script>
<title>ENsEN: Results List</title>
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
			<div id="content" role="main">
				<!-- .searchform -->
				<form action="resList.jsp" class="searchform">
					<input type="search" results="10" size="50" name="q" placeholder="Search..." value="<%=query%>" /> <input type="submit" value="Search" />
					<a href="queryAnalysis.jsp?q=<%=query%>" class="more-link">Next Step: Query Analysis </a>
				</form>			
				<hr />
				<h2 class="entry-title">
					<a href="resList.jsp?q=<%=query%>">“ <%=query%> ”
					</a>
				</h2>
				<%
					for (Document result : documents) {
						out.print("<a href='" + result.content.getLink() + "'>" + result.content.getHtmlTitle() + "</a> <br/>" + result.content.getHtmlSnippet().replace("<br>", " ") + "<hr/>");

					}
				%>
			</div>
			<!-- #content -->
			<aside id="sidebar" role="complementary"> <aside class="widget">
			<h3>Help</h3>
			<p>Enter your query, then choose your favorite search engine.</p>
			</aside> <!-- .widget --> <aside class="widget">
			<h3>Links</h3>
			<ul>
				<li><a href="http://linkeddata.org/" target="_blank">Linked Data</a></li>
				<li><a href="http://en.wikipedia.org/wiki/Linked_data" target="_blank">Linked Data in Wikipedia</a></li>
				<li><a href="http://liris.cnrs.fr/" target="_blank">LIRIS</a></li>
				<li><a href="http://www.mazen-alsarem.com/ " target="_blank">Mazen ALSAREM</a></li>
			</ul>
			</aside> <!-- .widget --> </aside>
			<!-- #sidebar -->
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