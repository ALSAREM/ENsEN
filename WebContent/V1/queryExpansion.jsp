<%@page import="com.hp.hpl.jena.rdf.model.RDFWriter"%>
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

	/* Query Expansion */

	/* 	preparation */
	Logger LOG = Logger.getLogger(this.getClass());
	String syntax = "RDF/XML-ABBREV"; // also try "N-TRIPLE"					
	StringWriter outW = new StringWriter();
	//DBpediaSpotlightClient spotlightClient = new DBpediaSpotlightClient();
	QueryHandler QHandler = new QueryHandler();

	LOG.warn("Step 4: Query Expansion");
	Query Q = (Query) ensenSession.getAttribute("Query");
	Q = QHandler.Qexpansion(Q);

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
<title>ENsEN: Query Expansion</title>
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
				<h2 class="entry-title">Step 3: Query Expansion</h2>
				<a onclick="popup('showRDFPopUp')" href="#" class="more-link">Show RDF</a>
				<!--POPUP-->
				<div id="showRDFPopUpblanket" class="blanket" style="display: none;" onclick="popup('showRDFPopUp')"></div>
				<div id="showRDFPopUp" class="popUpDiv" style="display: none;">
					<h4>
						RDF Graph<a href="#" onclick="popup('showRDFPopUp')">Close</a>
					</h4>
					<%
						RDFWriter fasterWriter = Q.ExtendedGraph.getWriter(syntax);
						fasterWriter.setProperty("allowBadURIs", "true");
						fasterWriter.setProperty("relativeURIs", "");
						fasterWriter.setProperty("tab", "0");
						outW = new StringWriter();
						fasterWriter.write(Q.ExtendedGraph, outW, null);

						String resString = "";

						resString = outW.toString();
					%>
					<textarea rows="30" cols="137"><%=resString.trim()%></textarea>
				</div>
				<!-- / POPUP-->
				<a onclick="popup('showClsPopUp')" href="#" class="more-link">Show Classes</a>
				<!--POPUP-->
				<div id="showClsPopUpblanket" class="blanket" style="display: none;" onclick="popup('showClsPopUp')"></div>
				<div id="showClsPopUp" class="popUpDiv" style="display: none;">
					<h4>
						Classes: <a href="#" onclick="popup('showClsPopUp')">Close</a>
					</h4>
					<ul class="tags">
						<%
							Property P = Q.ExtendedGraph.createProperty("http://www.w3.org/1999/02/22-rdf-syntax-ns#type");
							List<RDFNode> Ps = Q.ExtendedGraph.listObjectsOfProperty(P).toList();
							for (int i = 0; i < Ps.size(); i++) {
								out.print("<li><a target='_blank' href='" + Ps.get(i).asResource().getURI() + "'>" + Ps.get(i).asResource().getLocalName().replace("_", " ") + "</a></li>");
							}
						%>
					</ul>
					<br>
				</div>
				<!-- / POPUP-->
				<a onclick="popup('showCatsPopUp')" href="#" class="more-link">Show Categories</a>
				<!--POPUP-->
				<div id="showCatsPopUpblanket" class="blanket" style="display: none;" onclick="popup('showCatsPopUp')"></div>
				<div id="showCatsPopUp" class="popUpDiv" style="display: none;">
					<h4>
						Categories: <a href="#" onclick="popup('showCatsPopUp')">Close</a>
					</h4>
					<ul class="tags">
						<%
							P = Q.ExtendedGraph.createProperty("http://purl.org/dc/terms/subject");
							Ps = Q.ExtendedGraph.listObjectsOfProperty(P).toList();
							for (int i = 0; i < Ps.size(); i++) {
								out.print("<li><a target='_blank' href='" + Ps.get(i).asResource().getURI() + "'>" + Ps.get(i).asResource().getLocalName().replace("_", " ") + "</a></li>");
							}
						%>
					</ul>
					<br>
				</div>
				<!-- / POPUP-->
				<a href="optimalization.jsp?q=<%=query%>" class="more-link">Next Step: Optimal Graphs Generation(Documents) </a>
				<hr />
				<table>
					<tr>
						<th>ID</th>
						<th>Resource</th>
						<th>As Object</th>
						<th>As Subject</th>
					</tr>
					<%
						Model CM = Q.ExtendedGraph.difference(Q.graph);
						if (CM != null) {
							int objects = CM.listObjects().toList().size();
							int trips = CM.listStatements().toList().size();
							ResIterator S = CM.listSubjects();
							ArrayList<Resource> resSuList = (ArrayList<Resource>) S.toList();
							int Ss = resSuList.size();
							NodeIterator O = CM.listObjects();
							ArrayList<RDFNode> resObList = (ArrayList<RDFNode>) O.toList();
							for (int j = 0; j < resObList.size(); j++) {
								if (resObList.get(j).isResource())
									resSuList.add(resObList.get(j).asResource());
							}

							out.print("Statistics: New Triples (" + trips + "), New Entities(" + (resObList.size() + Ss) + "), New Subjects(" + Ss + "), New Objects(" + resObList.size() + ")");

							for (int j = 0; j < resSuList.size(); j++) {
								Resource res = resSuList.get(j);
					%>
					<tr>
						<td><%=j + 1%></td>
						<td><a href="<%=res.getURI()%>" target="_blank"> <%
 	if (res.getLocalName() != null && res.getLocalName().trim() != "")
 				out.print(res.getLocalName().replace("_", " "));
 			else {
 				String name = res.getURI();//.split("//")[res.getURI().split("//").length - 1];
 				if (name != null && name.trim() != "")
 					out.print(name.replace("_", " "));
 				else
 					out.print(res.toString().replace("_", " "));
 			}
 %></a></td>
						<td>
							<%
								Selector selector = new SimpleSelector(null, null, res);
										out.print(Q.ExtendedGraph.listStatements(selector).toList().size());
							%>
						</td>
						<td><%=res.listProperties().toList().size()%></td>
					</tr>
					<%
						j++;
							}

						} else
							out.print("No Results");
					%>
				</table>
				<br />
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