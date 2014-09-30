<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
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
<link href='http://fonts.googleapis.com/css?family=Ubuntu+Mono|Ubuntu|Ubuntu+Condensed' rel='stylesheet' type='text/css'>
<script src="./js/modernizr-1.7.min.js"></script>
<script src="./js/respond.min.js"></script>
<script src="./js/jquery.1.9.1.js"></script>
<script src="./js/jquery-ui.js"></script>
<script type='text/javascript'>

$(function() {	
	$("#main").show(2000);
});

	$.widget("custom.catcomplete", $.ui.autocomplete, {
		_renderMenu : function(ul, items) {
			var that = this, currentLabel = "";
			$.each(items, function(index, item) {
				if (item.label != currentLabel) {
					if (currentLabel != "")
						ul.append("</span></li><hr/>");
					that._renderItemData(ul, item);
					ul.append("<li class='ui-autocomplete-item'>");
					currentLabel = item.label;
				}
				ul.append("<span class='ui-autocomplete-category'>"
						+ item.category + ", ");

			});
			ul.append("</span></li>");
		}
	});
</script>
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
	z-index: 101;
}

.ui-autocomplete-category span {
	font-size: 9px;
}
</style>
<title>Enhanced Search Engine (ENsEN)</title>
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
		<div id="main" class="clearfix" style="display: none">
			<div id="content" role="main">
				<!-- start searchform -->
				<form action="results.jsp" class="sf" accept-charset="UTF-8" method="post">
					<input type="text" id="search" name="q" placeholder="Enter your query..." required />
					<!--	<input type="submit" value="Search" />	 -->
				</form>
				<!-- end searchform -->
				<hr />
				<h3 id="main-title">ENsEN: Enhanced Search Engine</h3>
				<p>
					In this work we propose <b>ENsEN</b>, where we enhance an existing search engine's snippet with linked data (LD) in order to highlight non trivial relationships between the information need of the user and LD resources related to the result page.
				</p>
				<img src="./images/logos/lod-cloud.png" style="margin-left: 10px;    width: 200px;    float: right;    box-shadow: 1px 1px 1px 1px rgba(119, 41, 83, 0.37);" />
				
				<p>
				<b>Linked Data</b> is becoming one of the most important source of information of the Web. Many projects (such as Linking Open Data -LoD-) start to provide a huge amount of open data by following the key principles for publishing Linked Data. 
				</p>
				<p><b>Information retrieval</b> is the activity of obtaining information resources relevant to an information need from a collection of information resources. Searches can be based on metadata or on full-text (or other content-based) indexing.
				</p>
				<p>
				<b>Keywords:</b>Linked Data, Information retrieval, Snippets, Co-Clustering, Tensor Decomposition 
				</p>
				
			</div>
			<!-- #content -->
			<aside id="sidebar" role="complementary"> 
			<aside class="widget">			
			<a href="http://liris.cnrs.fr"><img src="./images/logos/logo_liris_160.png"/></a>
			</aside> <!-- .widget --> <aside class="widget">			
			<ul>
				<li><a href="http://linkeddata.org/" target="_blank">Linked Data</a></li>
				<li><a href="http://en.wikipedia.org/wiki/Linked_data" target="_blank">Linked Data in Wikipedia</a></li>
				<li><a href="http://liris.cnrs.fr/" target="_blank">LIRIS</a></li>
				<li><a href="http://www.mazen-alsarem.com/ " target="_blank">Mazen ALSAREM</a></li>
			</ul>
			<hr/><br/>
			<fieldset class='ensen_fieldset'>
					<legend>By: </legend>
					<a href="http://liris.cnrs.fr/membres?idn=malsarem">Mazen Alsarem</a>, <a href="http://liris.cnrs.fr/~peportie/">Pierre-Edouard Portier</a> and <a href="http://liris.cnrs.fr/sylvie.calabretto/">Sylvie Calabretto</a>
				</fieldset>
			</aside> <!-- .widget --> </aside>
			<!-- #sidebar -->
		</div>
		<!-- #main -->
		<footer id="footer"> <!-- You're free to remove the credit link to Jayj.dk in the footer, but please, please leave it there :) -->
		<p>
			Copyright &copy; 2014 <a href="#" target="_blank">INSA-Lyon</a> <span class="sep">|</span> Developped by <a href="http://liris.cnrs.fr/drim/projects/ensen/index.html" title="Developped  by  DRIM-LIRIS" target="_blank">DRIM-LIRIS</a>
			| <a href="examples.html">Examples</a> | <a href="legal.html">Legal Information</a>
		</p>
		</footer>
		<!-- #footer -->
		<div class="clear"></div>
	</div>
	<!-- #wrapper -->
	<script src="./js/autocomplete.js"></script>
</body>
</html>