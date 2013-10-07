<%@page import="java.io.StringWriter"%>
<%@page import="org.dbpedia.spotlight.model.DBpediaResource"%>
<%@page import="java.util.List"%>
<%@page import="ensen.control.SparqlManager"%>
<%@page import="com.hp.hpl.jena.rdf.model.NodeIterator"%>
<%@page import="com.hp.hpl.jena.rdf.model.Model"%>
<%@page import="ensen.control.RDFManager"%>
<%@page import="org.dbpedia.spotlight.model.Text"%>
<%@page import="org.dbpedia.spotlight.exceptions.AnnotationException"%>
<%@page import="ensen.control.DBpediaSpotlightClient"%>
<%@page import="org.apache.log4j.Logger"%>
<%@page import="ensen.control.PropertiesManager"%>
<%@page import="ensen.entities.Document"%>
<%@page import="java.util.ArrayList"%>
<%@page import="ensen.control.Searcher"%>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%
	/* get data*/
	HttpSession ensenSession = request.getSession();
	String query = request.getParameter("q");
	ArrayList<Document> documents = (ArrayList<Document>) ensenSession.getAttribute("documents");

	/* Docs Analysis */

	/* 	preparation */

	Logger LOG = Logger.getLogger(this.getClass());
	DBpediaSpotlightClient spotlightClient = new DBpediaSpotlightClient();
	spotlightClient.local = Boolean.parseBoolean(PropertiesManager.getProperty("DBpediaSpotlightUseLocal"));
	int maxTextLen = Integer.parseInt(PropertiesManager.getProperty("maxTextLen"));
	String resString = "";
	String syntax = "RDF/XML-ABBREV"; // also try "N-TRIPLE"					
	StringWriter outW = new StringWriter();
	Model Dproj = null;// RDFManager.createRDFModel();// partie en commun entre tous les docs

	LOG.warn("Annotate Text");
	for (int i = 0; i < documents.size(); i++) {
		try {
			LOG.warn("Annotate Doc : " + i + " -(" + documents.get(i).content.getTitle() + ") ");
			documents.get(i).triplets = spotlightClient.extractAnnotation(new Text(documents.get(i).text.substring(0, Math.min(maxTextLen, documents.get(i).text.length()))));
		} catch (Exception e1) {
			//e1.printStackTrace();
			//LOG.info("Error: (" + documents.get(i).content.getTitle() + ")" + e1.getMessage());
		}
	}

	LOG.warn("Generate graphs");
	for (int i = 0; i < documents.size(); i++) {
		LOG.warn("Generate graph:" + i + " -(" + documents.get(i).content.getTitle() + ") ");
	//	documents.get(i).graph = RDFManager.generateModelFromDocument(documents.get(i).content.getLink(), documents.get(i).triplets);
		//RDFManager.createRDFfile("D" + i + "_" + documents.get(i).content.getTitle().replace(" ", "_"), documents.get(i).graph);
	}

	LOG.warn("Generate Full Graph");
	for (int i = 0; i < documents.size(); i++) {
		if (documents.get(i).triplets != null) {
			LOG.warn("Generate Full Graph of: " + documents.get(i).content.getTitle());
			NodeIterator it = documents.get(i).graph.listObjects();
			documents.get(i).fullGraph = RDFManager.createRDFModel();
			documents.get(i).fullGraph.add(documents.get(i).graph);
			documents.get(i).fullGraph.add(SparqlManager.getDocModel(it));

			// add annotation semantic
			if (documents.get(i).annotationSemantic != null)
				try {

					documents.get(i).fullGraph.add(documents.get(i).annotationSemantic);
				} catch (Exception e2) {
					//e2.printStackTrace();
				}

			if (Dproj == null) {
				Dproj = documents.get(i).fullGraph;
			} else {
				Dproj = Dproj.intersection(documents.get(i).fullGraph);
			}
		}
	}

	ensenSession.setAttribute("documents", documents);
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<link rel="stylesheet" type="text/css" href="../css/style.css">
<link rel="stylesheet" type="text/css" href="../css/popup.css">
<script src="../js/css-pop.js"></script>
<script src="../js/anaa.js"></script>
<script src="../js/ajax.js"></script>
<title>ENsEN: Documents Analysis</title>
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
				<form action="resList.jsp" class="searchform">
					<input type="search" results="10" size="50" name="q" placeholder="Search..." value="<%=query%>" /> <input type="submit" value="Search" />
				</form>
				<!-- .searchform -->
				<hr />
				<h2 class="entry-title">Step 1: Documents Analysis</h2>
				<a onclick="popup('showProjPopUp')" href="#" class="more-link">Show Shared Resources</a>
				<!--POPUP-->
				<div id="showProjPopUpblanket" class="blanket" style="display: none;" onclick="popup('showProjPopUp')"></div>
				<div id="showProjPopUp" class="popUpDiv" style="display: none;">
					<h4>
						Shared Resources<a href="#" onclick="popup('showProjPopUp')">Close</a>
					</h4>
					<%
						outW = new StringWriter();
						Dproj.write(outW, syntax);
						resString = outW.toString();
					%>
					<textarea rows="30" cols="137"><%=resString.trim()%></textarea>
				</div>
				<!-- / POPUP-->
				<a href="queryAnalysis.jsp?q=<%=query%>" class="more-link">Next Step: Query Analysis </a>
				<hr />
				<%
					for (int i = 0; i < documents.size(); i++) {
						Document result = documents.get(i);
				%>
				<a href='<%=result.content.getLink()%>'> <%=result.content.getHtmlTitle()%>
				</a> <br />
				<%=result.content.getHtmlSnippet().replace("<br>", " ")%>
				<br />
				<!-- docTools -->
				<div class="docTools">
					<a onclick="displayHTML('../ajax/showDocTxt.jsp?i=<%=i%>', 'storage<%=i%>', 'displayed<%=i%>'); popup('showTxtPopUp<%=i%>')" href="#">Show Text | </a>
					<!--POPUP-->
					<div id="showTxtPopUp<%=i%>blanket" class="blanket" style="display: none;" onclick="popup('showTxtPopUp<%=i%>')"></div>
					<div id="showTxtPopUp<%=i%>" class="popUpDiv" style="display: none;">
						<h4><%=result.content.getHtmlTitle()%><a href="#" onclick="popup('showTxtPopUp<%=i%>')">Close</a>
						</h4>
						<div id="displayed<%=i%>"></div>
						<div id="storage<%=i%>" style="display: none;"></div>
					</div>
					<!-- / POPUP-->
					<a onclick="displayHTML('../ajax/showDocEntities.jsp?i=<%=i%>', 'storageEnt<%=i%>', 'displayedEnt<%=i%>'); popup('showEntsPopUp<%=i%>')" href="#">Show Entities | </a>
					<!--POPUP-->
					<div id="showEntsPopUp<%=i%>blanket" class="blanket" style="display: none;" onclick="popup('showEntsPopUp<%=i%>')"></div>
					<div id="showEntsPopUp<%=i%>" class="popUpDiv" style="display: none;">
						<h4><%=result.content.getHtmlTitle()%><a href="#" onclick="popup('showEntsPopUp<%=i%>')">Close</a>
						</h4>
						<div id="displayedEnt<%=i%>"></div>
						<div id="storageEnt<%=i%>" style="display: none;"></div>
					</div>
					<!-- / POPUP-->
					<a onclick="displayHTML('../ajax/showDocRDF.jsp?i=<%=i%>', 'storageRDF<%=i%>', 'displayedRDF<%=i%>');  popup('showRDFPopUp<%=i%>')" href="#">Show RDF |</a>
					<!--POPUP-->
					<div id="showRDFPopUp<%=i%>blanket" class="blanket" style="display: none;" onclick="popup('showRDFPopUp<%=i%>')"></div>
					<div id="showRDFPopUp<%=i%>" class="popUpDiv" style="display: none;">
						<h4><%=result.content.getHtmlTitle()%><a href="#" onclick="popup('showRDFPopUp<%=i%>')">Close</a>
						</h4>
						<div id="displayedRDF<%=i%>"></div>
						<div id="storageRDF<%=i%>" style="display: none;"></div>
					</div>
					<!-- / POPUP-->
					<a href="../ajax/RDFvisualizer.jsp?i=<%=i%>" target="_blank">Show RDF Graph</a>
					<%
						if (result.annotationSemantic != null) {
					%>
					<a onclick="popup('showSAPopUp<%=i%>')" href="#">Show Semantic Annotation </a>
					<!--POPUP-->
					<div id="showSAPopUp<%=i%>blanket" class="blanket" style="display: none;" onclick="popup('showSAPopUp<%=i%>')"></div>
					<div id="showSAPopUp<%=i%>" class="popUpDiv" style="display: none;">
						<h4><%=result.content.getHtmlTitle()%><a href="#" onclick="popup('showSAPopUp<%=i%>')">Close</a>
						</h4>
						<%
							outW = new StringWriter();
									result.annotationSemantic.write(outW, syntax);
									resString = outW.toString();
						%>
						<textarea rows="30" cols="137"><%=resString.trim()%></textarea>
					</div>
					<!-- / POPUP-->
					<%
						}
					%>
				</div>
				<hr />
				<%
					}
				%>
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