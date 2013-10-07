<%@page import="java.io.IOException"%>
<%@page import="java.io.ObjectOutputStream"%>
<%@page import="java.io.BufferedOutputStream"%>
<%@page import="java.io.ObjectOutput"%>
<%@page import="java.io.OutputStream"%>
<%@page import="ensen.control.PropertiesManager"%>
<%@page import="java.io.FileOutputStream"%>
<%@page import="java.io.OutputStreamWriter"%>
<%@page import="java.io.Writer"%>
<%@page import="com.hp.hpl.jena.rdf.model.Model"%>
<%@page import="java.io.StringWriter"%>
<%@page import="ensen.entities.Query"%>
<%@page import="org.apache.log4j.Logger"%>
<%@page import="ensen.entities.Document"%>
<%@page import="java.util.ArrayList"%>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%
	/* get data*/
	HttpSession ensenSession = request.getSession();
	String query = (String) ensenSession.getAttribute("q");
	ArrayList<Document> documents = (ArrayList<Document>) ensenSession.getAttribute("documents");

	/* Docs Analysis */

	/* 	preparation */
	double a = 0.3;//in
	double b = 0.05;//out
	double c = 0.25;//freq
	double d = 0.3;//Query
	double e = 0.1;//sim
	
	boolean rerank=false;
	String Rerank=request.getParameter("rerank");
	if(Rerank!=null)
		rerank=true;
	
	String A=request.getParameter("a");
	if(A!=null)
		a=Double.parseDouble(A);
	String B=request.getParameter("b");
	if(B!=null)
		b=Double.parseDouble(B);
	String C=request.getParameter("c");
	if(C!=null)
		c=Double.parseDouble(C);
	String D=request.getParameter("d");
	if(D!=null)
		d=Double.parseDouble(D);
	String E=request.getParameter("e");
	if(E!=null)
		e=Double.parseDouble(E);
	
	Logger LOG = Logger.getLogger(this.getClass());
	String resString = "";
	String syntax = "RDF/XML-ABBREV"; // also try "N-TRIPLE"					
	StringWriter outW = new StringWriter();
	Model Dproj = null;// RDFManager.createRDFModel();// partie en commun entre tous les docs	
	Query Q = (Query) ensenSession.getAttribute("Query");

	LOG.warn("Doc analysis");
	String tags="";
	for (int i = 0; i < documents.size(); i++) {
		Document Doc=documents.get(i);
		//Doc.analyzeDocument(Q,a,b,c,d,e,rerank);		
		//test save to files
		Writer RDFOut = new OutputStreamWriter(new FileOutputStream(PropertiesManager.getProperty("testingPath") + "files\\D" + (i+1) + "_fullGraph.rdf"));
		Doc.fullGraph.write(RDFOut, syntax);
		try{
		      //use buffering
		      OutputStream file = new FileOutputStream( PropertiesManager.getProperty("testingPath") + "files\\D" + (i+1) + "_triples.ser" );
		      OutputStream buffer = new BufferedOutputStream( file );
		      ObjectOutput output = new ObjectOutputStream( buffer );
		      try{
		        output.writeObject(Doc.triplets);
		      }
		      finally{
		        output.close();
		      }
		    }  
		    catch(IOException ex){
		     ex.printStackTrace();
		    }
		 
		if (Dproj == null) {
				Dproj = Doc.fullGraph;
		} else {
				Dproj = Dproj.intersection(Doc.fullGraph);
		}
		tags+=Doc.topSubjects;
	}

	ensenSession.setAttribute("Query", Q);
	ensenSession.setAttribute("documents", documents);
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<link rel="stylesheet" type="text/css" href="../css/style.css">
<link rel="stylesheet" type="text/css" href="../css/popup.css">
<link rel="stylesheet" type="text/css" href="../css/simple-slider.css">
<link rel="stylesheet" type="text/css" href="../css/simple-slider-volume.css">
<link rel="stylesheet" type="text/css" href="../css/jquery.ui.accordion.css">
<link rel="stylesheet" type="text/css" href="../css/jquery-ui.css">
<script src="../js/css-pop.js"></script>
<script src="../js/anaa.js"></script>
<script src="../js/ajax.js"></script>
<script src="../js/jquery.1.9.1.js"></script>
<script src="../js/jquery-ui.js"></script>
<script src="../js/simple-slider.js"></script>
<script src="../js/jquery.tagsinput.js"></script>
<style type="text/css">
[class^=slider] {
	display: inline-block;
	margin-bottom: 30px;
}

.output {
	color: #888;
	font-size: 14px;
	padding-top: 1px;
	margin-left: 5px;
	vertical-align: top;
}
</style>
<title>ENsEN: Documents Analysis</title>
</head>
<body>
<script language="javascript"> 
function toggleInfo(id) {
	var ele = document.getElementById(id);  
	if(ele.style.display == "") {
    		ele.style.display = "none";  	
  	}
	else {
		ele.style.display = ""; 	
	}
} 
</script>   
	<div id="wrapper">
		<header id="header" class="clearfix" role="banner"> <hgroup>
		<h1 id="site-title">
			<a href="../index.jsp">Enhanced Search Engine</a>
		</h1>
		<h2 id="site-description">A better engine with Linked Data</h2>
		<a href="#" class="show_hide"><img alt="Configration" src="../images/config.png" height="36"> </a> </hgroup> </header>
		<!-- #header -->
		<div id="main" class="clearfix">
			<div class="wideContent" role="main">
				<form action="resList.jsp" class="searchform">
					<input type="search" results="10" size="50" name="q" placeholder="Search..." value="<%=query%>" /> <input type="submit" value="Search" />
				</form>
				<!-- .searchform -->
				<hr />
				<div class="slidingDiv" style="display: none;">
					<form action="">
						In Links Importance: <br /> <input type="text" data-slider="true" data-slider-range="0.0,1.0" name="a" value="<%=a%>" /><br /> Out Links Importance: <br /> <input type="text" data-slider="true" data-slider-range="0.0,1.0" name="b" value="<%=b%>" /><br /> Frequency Importance: <br /> <input type="text" data-slider="true" data-slider-range="0.0,1.0" name="c" value="<%=c%>" /><br /> Query Importance: <br /> <input type="text" data-slider="true" data-slider-range="0.0,1.0" name="d"
							value="<%=d%>"
						/><br /> <input type="submit" value="Apply" name="rerank" />
					</form>
					<hr />
				</div>
				<script>					
					$(document).ready(function() {
						 $( document ).tooltip();
						$(".slidingDiv").hide();
						$(".show_hide").show();

						$('.show_hide').click(function() {
							$(".slidingDiv").slideToggle();
						});

					});

					$("[data-slider]").each(function() {
						var input = $(this);
						$("<span>").addClass("output").insertAfter($(this));
					}).bind(
							"slider:ready slider:changed",
							function(event, data) {
								$(this).nextAll(".output:first").html(
										data.value.toFixed(3));
							});
				</script>
				<input name="tags" id="tags" value="<%=tags%>" />
				<script type="text/javascript">$('#tags').tagsInput();</script>
				<%
for (int i = 0; i < documents.size(); i++) {
								try{
										Document result = documents.get(i);
				%>
				<hr />
				<%=result.multiZoneSnippet%>				
				<hr />
				<!-- 
				<div class="docTools">
					<b>Show:</b> <a onclick="displayHTML('../ajax/showDocTxt.jsp?i=<%=i%>', 'storage<%=i%>', 'displayed<%=i%>'); popup('showTxtPopUp<%=i%>')" href="#">Text | </a>
					
					<div id="showTxtPopUp<%=i%>blanket" class="blanket" style="display: none;" onclick="popup('showTxtPopUp<%=i%>')"></div>
					<div id="showTxtPopUp<%=i%>" class="popUpDiv" style="display: none;">
						<h4>Full Text</h4><%=result.content.getHtmlTitle()%><br />
						<div id="displayed<%=i%>"></div>
						<div id="storage<%=i%>" style="display: none;"></div>
					</div>
					
					
					<a onclick="displayHTML('../ajax/showDocEntities.jsp?i=<%=i%>', 'storageEnt<%=i%>', 'displayedEnt<%=i%>'); popup('showEntsPopUp<%=i%>')" href="#">Entities | </a>
					

					<div id="showEntsPopUp<%=i%>blanket" class="blanket" style="display: none;" onclick="popup('showEntsPopUp<%=i%>')"></div>
					<div id="showEntsPopUp<%=i%>" class="popUpDiv" style="display: none;">
						<h4>Entities</h4><%=result.content.getHtmlTitle()%><br />
						<div id="displayedEnt<%=i%>"></div>
						<div id="storageEnt<%=i%>" style="display: none;"></div>
					</div>
					


					<a onclick="displayHTML('../ajax/showDocRDF.jsp?i=<%=i%>', 'storageRDF<%=i%>', 'displayedRDF<%=i%>');  popup('showRDFPopUp<%=i%>')" href="#">RDF Doc |</a>
					

					<div id="showRDFPopUp<%=i%>blanket" class="blanket" style="display: none;" onclick="popup('showRDFPopUp<%=i%>')"></div>
					<div id="showRDFPopUp<%=i%>" class="popUpDiv" style="display: none;">
						<h4>RDF Doc</h4><%=result.content.getHtmlTitle()%><br />
						<div id="displayedRDF<%=i%>"></div>
						<div id="storageRDF<%=i%>" style="display: none;"></div>
					</div>
					


					<a onclick="popup('showTIPopUp<%=i%>')" href="#">Top-In Resources (<%=(result.topicInRelatedResources == null) ? 0 : result.topicInRelatedResources.size()%> Resources) |
					</a>
					


					<div id="showTIPopUp<%=i%>blanket" class="blanket" style="display: none;" onclick="popup('showTIPopUp<%=i%>')"></div>
					<div id="showTIPopUp<%=i%>" class="popUpDiv" style="display: none;">
						<h4>Top-In Resources</h4><%=result.content.getHtmlTitle()%><br />
						<div style="overflow: scroll; width: 900px;">
							<%=result.printTopicInRelatedResources()%>
						</div>
					</div>
				
					<a onclick="popup('showTOPopUp<%=i%>')" href="#">Top-Out Resources (<%=(result.topicOutRelatedResources == null) ? 0 : result.topicOutRelatedResources.size()%> Resources) |
					</a>
					

					<div id="showTOPopUp<%=i%>blanket" class="blanket" style="display: none;" onclick="popup('showTOPopUp<%=i%>')"></div>
					<div id="showTOPopUp<%=i%>" class="popUpDiv" style="display: none;">
						<h4>Top-Out Resources</h4><%=result.content.getHtmlTitle()%><br />
						<div style="overflow: scroll; width: 900px;">
							<%=result.printTopicOutRelatedResources()%>
						</div>
					</div>
					

					<a onclick="popup('showAllPopUp<%=i%>')" href="#">Top-In-Out Resources (<%=(result.topicAllRelatedResources == null) ? 0 : result.topicAllRelatedResources.size()%> Resources) |
					</a>
					

					<div id="showAllPopUp<%=i%>blanket" class="blanket" style="display: none;" onclick="popup('showAllPopUp<%=i%>')"></div>
					<div id="showAllPopUp<%=i%>" class="popUpDiv" style="display: none;">
						<h4>Top-In-Out Resources</h4><%=result.content.getHtmlTitle()%><br />
						<div style="overflow: scroll; width: 900px;">
							<%=result.printTopicAllRelatedResources()%>
						</div>
					</div>
					

					<a onclick="popup('showFreqPopUp<%=i%>')" href="#">Top-Freq Resources (<%=(result.topicRelatedFreqResources == null) ? 0 : result.topicRelatedFreqResources.size()%> Resources) |
					</a>
				

					<div id="showFreqPopUp<%=i%>blanket" class="blanket" style="display: none;" onclick="popup('showFreqPopUp<%=i%>')"></div>
					<div id="showFreqPopUp<%=i%>" class="popUpDiv" style="display: none;">
						<h4>Top-Freq Resources</h4><%=result.content.getHtmlTitle()%><br />
						<div style="overflow: scroll; width: 900px;">
							<%=result.printFreqRelatedResources()%>
						</div>
					</div>
				
					<a href="../Tools/RDFVisualizer.jsp?i=<%=i%>" target="_blank">Show RDF Graph | </a> <a href="../Tools/RDFClusterVisualizer.jsp?i=<%=i%>" target="_blank">Show RDF Graph with Clusters</a>
					<%
						if (result.annotationSemantic != null) {
					%>
					<a onclick="popup('showSAPopUp<%=i%>')" href="#">Semantic Annotation </a>
					
					<div id="showSAPopUp<%=i%>blanket" class="blanket" style="display: none;" onclick="popup('showSAPopUp<%=i%>')"></div>
					<div id="showSAPopUp<%=i%>" class="popUpDiv" style="display: none;">
						<h4>Semantic Annotation</h4><%=result.content.getHtmlTitle()%><br />
						<%
							outW = new StringWriter();
										result.annotationSemantic.write(outW, syntax);
										resString = outW.toString();
						%>
						<textarea rows="30" cols="137"><%=resString.trim()%></textarea>
					</div>
					-->
					
					<%
						}
					%>
				</div>
				<hr />
				<%
					} catch (Exception ex) {
							ex.printStackTrace();
						}
					}
				%>
				<hr />
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