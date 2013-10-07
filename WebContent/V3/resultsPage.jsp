<%@page import="ensen.control.PropertiesManager"%>
<%@page import="ensen.control.Searcher"%>
<%@page import="ensen.evaluation.db.EvaluationDBControler"%>
<%@page import="ensen.entities.Query"%>
<%@page import="org.apache.log4j.Logger"%>
<%@page import="ensen.entities.Document"%>
<%@page import="java.util.ArrayList"%>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%
	long start = System.currentTimeMillis();

/* 	preparation */
	double a = 0.3;//in
	double b = 0.2;//out
	double c = 0.25;//freq
	double d = 0.1;//Query
	double e = 0.075;//sim	
	double f = 0.075;//sa	
	
	boolean rerank=false;
	ArrayList<Document> documents=null;
	Query Q =null;
	HttpSession ensenSession = request.getSession();
	

/* get data*/
	String Rerank=request.getParameter("rerank");
	String A=request.getParameter("a");
	String B=request.getParameter("b");	
	String C=request.getParameter("c");
	String D=request.getParameter("d");
	String E=request.getParameter("e");
	String F=request.getParameter("f");
	String query = request.getParameter("q");
	Logger LOG = Logger.getLogger(this.getClass());	
	if(Rerank!=null)	rerank=true;
	
/* Get From Search Engine (google)*/
	if(!rerank){
		Searcher S = new Searcher();
		documents = S.search(query, Integer.parseInt(PropertiesManager.getProperty("nOfRes")));
		
	}else
	{
		documents = (ArrayList<Document>) ensenSession.getAttribute("documents");
	}
	long search = System.currentTimeMillis();
	
/* Query Analysis */
	if(!rerank){
		Q = new Query(query);
		Q.extendQueryLexicaly();
		Q.getResourcesFromLD();
	}else
	{
		Q = (Query) ensenSession.getAttribute("Query");
	}
	long qAnal = System.currentTimeMillis();
	
	if(A!=null)	a=Double.parseDouble(A);	
	if(B!=null)	b=Double.parseDouble(B);
	if(C!=null)	c=Double.parseDouble(C);	
	if(D!=null)	d=Double.parseDouble(D);	
	if(E!=null)	e=Double.parseDouble(E);
	if(F!=null)	f=Double.parseDouble(F);
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<link rel="stylesheet" type="text/css" href="../css/style.css">
<link rel="stylesheet" type="text/css" href="../css/simple-slider.css">
<link rel="stylesheet" type="text/css" href="../css/simple-slider-volume.css">
<link rel="stylesheet" type="text/css" href="../css/jquery.ui.accordion.css">
<link rel="stylesheet" type="text/css" href="../css/jquery-ui.css">
<link rel="stylesheet" type="text/css" href="../css/prettyPhoto.css" media="screen" title="prettyPhoto main stylesheet" charset="utf-8" />
<link rel="stylesheet" href="../css/ensen.css" />
<link rel="stylesheet" href="../css/enlarge.css" />
<script src="../js/ensen.js"></script>
<script src="../js/css-pop.js"></script>
<script src="../js/anaa.js"></script>
<script src="../js/ajax.js"></script>
<script src="../js/jquery.1.9.1.js"></script>
<script src="../js/jquery-ui.js"></script>
<script src="../js/simple-slider.js"></script>
<script src="../js/jquery.tagsinput.js"></script>
<script src="../js/jquery.prettyPhoto.js" type="text/javascript" charset="utf-8"></script>
<style type="text/css">
[class^=slider] {
	display: inline-block;
	margin-bottom: 30px;
}
</style>
<title>ENsEN: Final Snippets</title>
</head>
<body>
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
				<table>
					<tr>
						<td>
							<div class="slidingDiv" style="display: none;">
								<form action="">
									In Links Importance: <br /> <input type="text" data-slider="true" data-slider-range="0.0,1.0" name="a" value="<%=a%>" /><br /> Out Links Importance: <br /> <input type="text" data-slider="true" data-slider-range="0.0,1.0" name="b" value="<%=b%>" /><br /> Frequency Importance: <br /> <input type="text" data-slider="true" data-slider-range="0.0,1.0" name="c" value="<%=c%>" /><br /> Query Importance: <br /> <input type="text" data-slider="true" data-slider-range="0.0,1.0"
										name="d" value="<%=d%>"
									/><br /> <input type="submit" value="Apply" name="rerank" />
								</form>								
							</div>
							<form action="resultsPage.jsp" class="searchform">
								<input type="search" results="10" size="50" name="q" placeholder="Search..." value="<%=query%>" /> <input type="submit" value="Search" />
							</form> <!-- .searchform -->
							<hr />
						</td>
					</tr>
					<script>
						$(document).ready(function() {
							$(document).tooltip();
							$(".slidingDiv").hide();
							$(".show_hide").show();

							$('.show_hide').click(function() {
								$(".slidingDiv").slideToggle();
							});

						});

						$("[data-slider]").each(
								function() {
									var input = $(this);
									$("<span>").addClass("output").insertAfter(
											$(this));
								}).bind(
								"slider:ready slider:changed",
								function(event, data) {
									$(this).nextAll(".output:first").html(
											data.value.toFixed(3));
								});
					</script>
					<%
						ArrayList<String> usedImages = new ArrayList<String>();
						for (int i = 0; i < documents.size(); i++) {
							try {
								Document Doc = documents.get(i);
								Doc.usedImages = usedImages;
								//Doc.ensenSession = ensenSession;
								Doc.analyzeDocument(Q, a, b, c, d, e, f, rerank);
								out.print("<tr><td>" + Doc.multiZoneSnippet + "</td></tr>");
								usedImages = Doc.usedImages;
							} catch (Exception ex) {
								ex.printStackTrace();
							}
						}
					%>
				</table>
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
<%
	long docsAnal = System.currentTimeMillis();

	ensenSession.setAttribute("Query", Q);
	ensenSession.setAttribute("documents", documents);
	EvaluationDBControler EDBC = new EvaluationDBControler();
	EDBC.insert(Q, documents);

	long end = System.currentTimeMillis();
	out.println("Execution time was " + (end - start) + " ms.");
	System.out.println("Execution time was " + (end - start) + " ms.");
	System.out.println("-------- Time details -------- ");
	System.out.println("Search: " + (search - start) + " ms.");
	System.out.println("Query: " + (qAnal - search) + " ms.");
	System.out.println("Docs: " + (docsAnal - qAnal) + " ms.");
	System.out.println("DB Update: " + (end - docsAnal) + " ms.");
%>