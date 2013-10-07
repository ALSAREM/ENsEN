<%@page import="com.hp.hpl.jena.rdf.model.Model"%>
<%@page import="java.util.List"%>
<%@page import="com.hp.hpl.jena.rdf.model.Property"%>
<%@page import="com.hp.hpl.jena.rdf.model.RDFNode"%>
<%@page import="com.hp.hpl.jena.rdf.model.NodeIterator"%>
<%@page import="com.hp.hpl.jena.rdf.model.SimpleSelector"%>
<%@page import="com.hp.hpl.jena.rdf.model.Selector"%>
<%@page import="com.hp.hpl.jena.rdf.model.Resource"%>
<%@page import="com.hp.hpl.jena.rdf.model.ResIterator"%>
<%@page import="ensen.control.DBpediaSpotlightClient"%>
<%@page import="ensen.control.PropertiesManager"%>
<%@page import="ensen.entities.Query"%>
<%@page import="ensen.control.QueryHandler"%>
<%@page import="java.io.StringWriter"%>
<%@page import="ensen.entities.Document"%>
<%@page import="java.util.ArrayList"%>
<%@page import="org.apache.log4j.Logger"%>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%
	/* get data*/
	HttpSession ensenSession = request.getSession();
	String query = request.getParameter("q");
	ArrayList<Document> documents = (ArrayList<Document>) ensenSession.getAttribute("documents");
	Query Q = (Query) ensenSession.getAttribute("Query");	
	
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
<title>ENsEN: Optimal Graphs Generation</title>
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
				</form>
				<!-- .searchform -->
				<hr />
				<h2 class="entry-title">Step 4: Optimal Graphs Generation</h2>
				
				<a href="projection.jsp?q=<%= query %>" class="more-link">Next Step: RDF Graphs Projection</a>
				<hr/>		
					
				
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