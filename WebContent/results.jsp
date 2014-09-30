<%@page import="java.util.TreeMap"%>
<%@page import="ensen.util.Printer"%>
<%@page import="ensen.entities.Query"%>
<%@page import="ensen.entities.Document"%>
<%@page import="java.util.ArrayList"%>
<%@page import="com.google.api.services.customsearch.model.Result"%>
<%@page import="java.util.List"%>
<%@page import="ensen.util.PropertiesManager"%>
<%@page import="ensen.controler.Searcher"%>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%
	Printer.setOutPutLogFileToDefault();
	request.setCharacterEncoding("UTF-8");
	long sessionId=System.currentTimeMillis();
	String sessionName="documents"+sessionId;
	String q=request.getParameter("q");
	Query query=new Query(q);
	Searcher S = new Searcher();
	int nOfRes=Integer.parseInt(PropertiesManager.getProperty("nOfRes"));//with or without snippet
	int nOfResInOnePage=Integer.parseInt(PropertiesManager.getProperty("nOfResInOnePage"));// results with snippets
	
	List<Result> Results = S.search(q,nOfRes );
	HttpSession ensenSession = request.getSession();
	ensenSession.setAttribute("Results"+sessionId, Results);
	TreeMap <Integer,Document>  documents =new TreeMap <Integer,Document> ();
	ensenSession.setAttribute(sessionName, documents);	
	ensenSession.setAttribute("query"+sessionId, query);
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="X-UA-Compatible" content="IE=edge,chrome=1" />
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<link rel="stylesheet" href="./css/style.css" type="text/css" charset="utf-8" />
<link rel="stylesheet" href="./css/ensen.css" type="text/css" charset="utf-8" />
<link rel="stylesheet" href="./css/jquery/jquery-ui.css" type="text/css" charset="utf-8" />
<link rel="stylesheet" href="./css/jquery/jquery.powertip.css" type="text/css" charset="utf-8" />
<link rel="stylesheet" href="./css/jquery/jquery.powertip-light.css" type="text/css" charset="utf-8" />
<link rel="stylesheet" href="./css/ribbon.css" type="text/css" charset="utf-8" />
<link rel="stylesheet" href="./css/flip.css" type="text/css" charset="utf-8" />
<link href='http://fonts.googleapis.com/css?family=Ubuntu+Mono|Ubuntu|Ubuntu+Condensed' rel='stylesheet' type='text/css'>
<script src="./js/modernizr-1.7.min.js"></script>
<script src="./js/respond.min.js"></script>
<script src="./js/jquery.1.9.1.js"></script>
<script src="./js/jquery-ui.js"></script>
<script src="./js/ensen.js"></script>
<script src="./js/jquery.powertip.js"></script>
<!-- <script type="text/javascript"   src="https://maps.googleapis.com/maps/api/js?key=AIzaSyDQC3psebuYlScH-I18VEThT09Lm5f6I60&sensor=false">  </script> -->
<style>
.ui-autocomplete-category {
	padding: .2em .4em;
	margin: .8em 0 .2em;
	line-height: 1.5;
	font-size: 9px;
}

.ui-autocomplete {
	width: 400px;
	cursor: pointer;
}

.ui-autocomplete-category span {
	font-size: 9px;
}
</style>
<script>
/*******more results*************/
 function loadMore(){		
	 $("#loadingNext").css("display", "");
	 $("#loadMoreBtn").css("display", "none");
	 maxSuccessAjax +=<%=nOfResInOnePage%>;	
	 callAjax(currAjax);
	 currAjax++;
	}
	/*******ajax*********/
	function callAjax(resultId) {
		$("#result" + resultId).show("slide",1000);
		$("#result" + resultId).load("snippet.jsp?id=" + resultId+"&sessionId=<%=sessionId%>");
		
		$("#result" + resultId).find("script").each(function(i) {
			eval($(this).text());
		});
	}

	var currAjax = 0;
	var successAjax=0;
	var maxAjax =<%=Results.size()%>;
	var maxSuccessAjax =<%=nOfResInOnePage%>;

	$(document).ready(function() {
		if (maxAjax > 0) {
			callAjax(currAjax);
			currAjax++;
		}
		
	});
	
	var checkedPhotos = 0;
	$(document).ajaxComplete(function(event, xhr, settings) {
		/* check snippet */
		$(".bubble:contains('|Not enough information|')").css("display", "none");		
		$( ".results:contains('|Not enough information|')" ).hide("slide",3000);
		$(".bubble:contains('|Not enough information|')").addClass("err");
		$(".bubble").addClass("show");

	
		/* remove loading*/
		successAjax = $('.show').length;
		if ($("#loading").is(':visible')) {			
			
			if (successAjax > 0)
				{
					$("#loading").css("display", "none");
					
				}
			else
				$("#loading").css("display", "");
		}
		/* check photos*/
		$('.thumb').error(function() {
			if($(this).attr("src").indexOf("commons") > -1)    
			{
				$(this).attr("src",$(this).attr("src").replace("/commons/","/en/"));
				$(this).attr("src2",$(this).attr("src2").replace("/commons/","/en/"));
				}
			else
				{
					if($(this).attr("src2").indexOf("null") > -1)
						$(this).attr("src",$(this).attr("src2"));
					else
						{
						$(this).replaceWith("<canvas id='thumb"+checkedPhotos+"' width='160' height='110'></canvas>");						
						var ctx = document.querySelector("#thumb"+checkedPhotos).getContext('2d');
						ctx.font = "16pt Arial";	
						ctx.strokeText($(this).attr("alt"), 5, 40);
						checkedPhotos++;
							//$(this).attr("src","<%=PropertiesManager.getProperty("defaultThumb")%>");
						}
				}
	        
	      });
		
		/**load another**/ 	//check if no more results		
		if (successAjax < maxSuccessAjax && currAjax < maxAjax) {
			$("#loadingNext").css("display", "");
			$( ".isa_success" ).hide("slide",1000);
			
			$("#loadMoreBtn").css("display", "none");
			callAjax(currAjax);
			currAjax++;			
		} else {
			$("#loadingNext").css("display", "none");
			$("#loadMoreBtn").css("display", "");
			
		}
		
		
	});

	/******auto complete******/
	$.widget("custom.catcomplete", $.ui.autocomplete, {
		_renderMenu : function(ul, items) {
			var that = this, currentLabel = "";
			$.each(items, function(index, item) {
				if (item.label != currentLabel) {
					if (currentLabel != "")
						ul.append("</span></li><hr/>");
					that._renderItemData(ul, item);
					ul.append("<li>");
					currentLabel = item.label;
				}
				ul.append("<span class='ui-autocomplete-category'>"
						+ item.category + ", ");

			});
			ul.append("</span></li>");
		}
	});
</script>
<title>ENsEN: Results</title>
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
			<div id="wideContent" role="main">
				<!-- start searchform -->
				<form action="results.jsp" class="sf" accept-charset="UTF-8" >
					<input type="text" id="search" name="q" placeholder="<%=q%>" value="<%=q%>" required />
					<!-- <input type="submit" value="Search" /> -->
				</form>
				<!-- end searchform -->
				<hr />
				<!-- content-Start -->
				<div id="loading" style="text-align: center;">
					<br />
					<br /> <img src="./images/loading/ajax-loader2.gif" /><br />
					<img src="./images/loading/ajax-loader1.gif" />
				</div>
				<div class="allresults">
					<%
						int i = 0;
						for (Result r : Results) {
					%>
					<div id="result<%=i%>" class="results" style="display: none"><br/><hr/><center><span class="isa_success"> Analyzing "<%=r.getTitle() %>"</span></center></div>					
					<%
						i++;
						}
					%>
				</div>
				<br />
				<div>
					<center>
					<img id="loadMoreBtn"    src="./images/icons/more.png" onClick="loadMore();"  style="display: none; cursor: pointer;" alt="show more results" title="show more results"/>
					<img id="loadingNext" src="./images/loading/103.gif" style="display: none;" />
					</center>
				</div>
				<!-- content-End -->
			</div>
		</div>
		<!-- #main -->
		<footer id="footer"> <!-- You're free to remove the credit link to Jayj.dk in the footer, but please, please leave it there :) -->
		<p>
			Copyright &copy; 2014 <a href="#" target="_blank">INSA-Lyon</a> <span class="sep">|</span> Developped by <a href="http://liris.cnrs.fr/drim/projects/ensen/index.html" title="Developped  by  DRIM-LIRIS" target="_blank">DRIM-LIRIS</a>	| <a href="examples.html">Examples</a>  | <a href="legal.html">Legal Information</a>
		</p>
		</footer>
		<!-- #footer -->
		<div class="clear"></div>
	</div>
	<!-- #wrapper -->
	<script src="./js/autocomplete.js"></script>
</body>
</html>