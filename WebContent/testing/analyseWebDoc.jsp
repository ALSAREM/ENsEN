<%@page import="java.util.Map"%>
<%@page import="com.hp.hpl.jena.rdf.model.Model"%>
<%@page import="java.util.ArrayList"%>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@page import="ensen.entities.Document"%>
<%@page import="ensen.entities.Query"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<link rel="stylesheet" type="text/css" href="../css/style.css">
<link rel="stylesheet" type="text/css" href="../css/popup.css">
<script src="../js/d3.v3.min.js"></script>
<title>ENsEn: Analyze a Web document</title>
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
			<div class="wideContent" role="main">
				<table>
					<tr>
						<td>
							<h2>Inputs:</h2>
							<form action="">
								URL <input type="text" name="url" size="80" value="<%=request.getParameter("url")%>" /> Query <input type="text" name="q" value="<%=request.getParameter("q")%>" /> <input type="submit" name="go" value="Go" />
							</form>
						</td>
					</tr>
					<%
						String url = request.getParameter("url");
						String query = request.getParameter("q");
						if (url != null) {
							Query Q = new Query(query);
							Q.extendQueryLexicaly();
							Q.getResourcesFromLD();

							Document D = new Document();
							//D.analyzeDocument(Q);

							//prepare graphs
							HttpSession ensenSession = request.getSession();
							ArrayList<Model> graphs = new ArrayList<Model>();
							graphs.add(D.fullGraph);//0
							graphs.add(D.createTopicSubgraph());//1							
							graphs.add(D.createQuerySubgraph(Q));//2
							graphs.add(D.createIntrestedSubgraph());//3	
							ensenSession.setAttribute("graphs", graphs);
							ensenSession.setAttribute("graphClusters", D.SubGraphs);
					%>
					<tr>
						<td>
							<h2>
								Document Text (<%=D.text.split(" ").length%>
								words)
							</h2>
							<div style="overflow: scroll; width: 900px; height: 300px;">
								<%=D.text%>
							</div>
						</td>
					</tr>
					<tr>
						<td>
							<h2>
								Document RDFizer Results (<%=D.allRes.size()%>
								Resources)
							</h2>
							<div style="overflow: scroll; width: 900px; height: 300px;">
								<%=D.printAllResources()%>
							</div>
						</td>
					</tr>
					<% 	if (D.annotationSemantic != null) { %>
					<tr>
						<td>
							<h2>
								Document Semantic Annotations (<%							
										out.print(D.annotationSemantic.size());
									
							%>
								Annotations)
							</h2>
							<div style="overflow: scroll; width: 900px; height: 300px;">
								<% if (D.annotationSemantic != null)
									out.print(D.printSemanticAnnotations());%>
							</div>
						</td>
					</tr>
					<% } %>
					<tr>
						<td>
							<h2>
								Query Extended Text (<%=Q.ExtendedText.split(" ").length%>
								Terms)
							</h2>
							<div style="overflow: scroll; height: 100px;">
								<%=Q.ExtendedText%>
							</div>
						</td>
					</tr>
					<tr>
						<td>
							<h2>
								Query RDFizer Results (<%=Q.RelatedResLD.size()%>
								Resources)
							</h2>
							<div style="overflow: scroll; width: 900px; height: 300px;">
								<%=Q.printRelatedResFromLD()%>
							</div>
						</td>
					</tr>
					<tr>
						<td>
							<h2>
								Topic-Related Resources (in) (<%=D.topicInRelatedResources.size()%>
								Resources)
							</h2>
							<div style="overflow: scroll; width: 900px; height: 300px;">
								<%=D.printTopicInRelatedResources()%>
							</div>
						</td>
					</tr>
					<tr>
						<td>
							<h2>
								Topic-Related Resources (out) (<%=D.topicOutRelatedResources.size()%>
								Resources)
							</h2>
							<div style="overflow: scroll; width: 900px; height: 300px;">
								<%=D.printTopicOutRelatedResources()%>
							</div>
						</td>
					</tr>
					<tr>
						<td>
							<h2>
								Topic-Related Resources (in-out) (<%=D.topicAllRelatedResources.size()%>
								Resources)
							</h2>
							<div style="overflow: scroll; width: 900px; height: 300px;">
								<%=D.printTopicAllRelatedResources()%>
							</div>
						</td>
					</tr>
					<tr>
						<td>
							<h2>
								Topic-Related Resources (Freq)(<%=D.topicRelatedFreqResources.size()%>
								Resources)
							</h2>
							<div style="overflow: scroll; width: 900px; height: 300px;">
								<%=D.printFreqRelatedResources()%>
							</div>
						</td>
					</tr>
					<tr>
						<td>
							<h2>
								Query-Related Resources (<%=Q.RelatedResDoc.size()%>
								Resources)
							</h2>
							<div style="overflow: scroll; width: 900px; height: 300px;">
								<%=Q.printRelatedResInDoc()%>
							</div>
						</td>
					</tr>
					<tr>
						<td>
							<h2>
								General Snippets
							</h2>
							<div style="overflow: scroll; width: 900px; height: 300px;">
								<%=D.AllSnippets.replace("\r", "<br/>") %>
							</div>
						</td>
					</tr>
					<tr>
						<td>
							<h2>
								Snippets By Clusters
							</h2>
							<div style="overflow: scroll; width: 900px; height: 300px;">
								<% 
								int i = 0;
								for (Map.Entry<String, String> entry : D.Snippets.entrySet()) {
									out.println("Cluster (" + (i++) + "): " + entry.getValue().replace("\r", "<br/>")+"<hr/>");
								}
								%>
							</div>
						</td>
					</tr>
					<tr>
						<td><aside>
							<ul>
								<li><a target="_blank" href="../Tools/RDFVisualizer.jsp?i=0">Show Full RDF graph</a></li>
								<li><a target="_blank" href="../Tools/RDFClusterVisualizer.jsp">Show Full RDF graph Clusters</a></li>
								<li><a target="_blank" href="../Tools/RDFVisualizer.jsp?i=1">Show Topic sub graph</a></li>
								<li><a target="_blank" href="../Tools/RDFVisualizer.jsp?i=2">Show Query sub graph</a></li>
								<li><a target="_blank" href="../Tools/RDFVisualizer.jsp?i=3">Show Interested graph</a></li>
							</ul>
							</aside>
					</tr>
					<%
						}
					%>
					<tr>
						<td></td>
					</tr>
				</table>
			</div>
		</div>
		<footer id="footer"> <!-- You're free to remove the credit link to Jayj.dk in the footer, but please, please leave it there :) -->
		<p>
			Copyright &copy; 2013 <a href="#" target="_blank">INSA-Lyon</a> <span class="sep">|</span> Design by <a href="http://liris.cnrs.fr/" title="Design by DRIM-LIRIS" target="_blank">DRIM-LIRIS</a>
		</p>
		</footer>
	</div>
</body>
</html>
