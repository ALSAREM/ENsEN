<%@page import="com.hp.hpl.jena.rdf.model.Statement"%>
<%@page import="com.hp.hpl.jena.rdf.model.StmtIterator"%>
<%@page import="com.hp.hpl.jena.rdf.model.RDFNode"%>
<%@page import="com.hp.hpl.jena.rdf.model.SimpleSelector"%>
<%@page import="com.hp.hpl.jena.rdf.model.Selector"%>
<%@page import="com.hp.hpl.jena.rdf.model.Resource"%>
<%@page import="java.util.Arrays"%>
<%@page import="ensen.control.DBpediaLookupClient"%>
<%@page import="ensen.util.permute"%>
<%@page import="ensen.entities.TypeFilter"%>
<%@page import="ensen.control.RDFManager"%>
<%@page import="com.hp.hpl.jena.rdf.model.Model"%>
<%@page import="java.text.DecimalFormat"%>
<%@page import="ensen.entities.Query"%>
<%@page import="ensen.entities.Document"%>
<%@page import="java.util.ArrayList"%>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%
	/* get data*/
	HttpSession ensenSession = request.getSession();
	String query = request.getParameter("q");
	ArrayList<Document> documents = (ArrayList<Document>) ensenSession.getAttribute("documents");
	Query Q = (Query) ensenSession.getAttribute("Query");
	DecimalFormat df = new DecimalFormat("#.###");

	/* Preparation  */
	Model directAnswerGraph = RDFManager.createRDFModel();
	String directAnswerURL = null;
	//get query main URL by permuteAll
	System.out.print(Arrays.toString(permute.permuteAll(query)));
	String[] cominations = permute.permuteAll(query);
	DBpediaLookupClient DBLC = new DBpediaLookupClient();

	for (int i = 0; i < cominations.length; i++) {
		if (cominations[i].trim().compareTo("") != 0) {
			//System.out.println(cominations[i]);
			directAnswerURL = DBLC.getOneResource(cominations[i], "", 1);
			//System.out.println(directAnswerURL);
			if (directAnswerURL != null)
				break;
		}
	}

	ArrayList<TypeFilter> TypeFilters = new ArrayList<TypeFilter>();
	for (int i = 0; i < documents.size(); i++) {
		Document doc = documents.get(i);
		//search for a part of direct answer
		if (doc.fullGraph != null && directAnswerURL != null) {
			Resource directAnswerResource = doc.fullGraph.getResource(directAnswerURL);
			StmtIterator sts = doc.fullGraph.listStatements(new SimpleSelector(directAnswerResource, null, (RDFNode) null));
			directAnswerGraph.add(sts);
		}
		//get types for filter
		//get links
		//generate new snippet

	}
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<!-- Always force latest IE rendering engine (even in intranet) & Chrome Frame -->
<meta http-equiv="X-UA-Compatible" content="IE=edge,chrome=1" />
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<link rel="stylesheet" href="../css/style.css" type="text/css" charset="utf-8" />
<link href="http://fonts.googleapis.com/css?family=Droid+Serif:regular,bold" rel="stylesheet" />
<script src="../js/modernizr-1.7.min.js"></script>
<script src="../js/respond.min.js"></script>
<title>ENsEN: Results Page</title>
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
				<form action="resList.jsp" class="searchform">
					<input type="search" results="10" size="50" name="q" placeholder="Search..." value="<%=query%>"/> <input type="submit" value="Search" />
				</form>
				<!-- .searchform -->
				<hr />
				<b>Tools:</b> <input type="checkbox" value="Cluster the results" /> &nbsp; Cluster the results
				<hr />
				<%
					for (int i = 0; i < documents.size(); i++) {
						Document result = documents.get(i);
				%>
				<a href='<%=result.content.getLink()%>'> <%=result.content.getHtmlTitle()%></a> <br />
				<footer class="post-meta">
				<p>New generated Snippet</p>
				</footer>
				<br>
				<ul class="tags">
					<li></li>
				</ul>
				<hr />
				<%
					}
				%>
			</div>
			<!-- #content -->
			<aside id="sidebar" role="complementary"> <aside class="widget">
			<h3>Direct Answer</h3>
			<p><a href="<%=directAnswerURL%>"><%= RDFManager.createRDFModel().createResource(directAnswerURL).getLocalName() %></a>
				<br />
				<%
				/*	StmtIterator stts = directAnswerGraph.listStatements();
					while (stts.hasNext()) {
						out.print(stts.next());
					}*/
				%>
			</p>
			</aside> <!-- .widget --> <aside class="widget">
			<h3>Filters</h3>
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