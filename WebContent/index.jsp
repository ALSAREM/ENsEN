<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<!-- Always force latest IE rendering engine (even in intranet) & Chrome Frame -->
<meta http-equiv="X-UA-Compatible" content="IE=edge,chrome=1" />
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<link rel="stylesheet" href="./css/style.css" type="text/css" charset="utf-8" />
<link rel="stylesheet" href="./css/jquery-ui.css" type="text/css" charset="utf-8" />
<link href="http://fonts.googleapis.com/css?family=Droid+Serif:regular,bold" rel="stylesheet" />
<script src="./js/modernizr-1.7.min.js"></script>
<script src="./js/respond.min.js"></script>
<script src="./js/jquery.1.9.1.js"></script>
<script src="./js/jquery-ui.js"></script>
<style>
.ui-autocomplete-category {
	padding: .2em .4em;
	margin: .8em 0 .2em;
	line-height: 1.5;
	font-size: 9px;
}

.ui-autocomplete-category span {
	font-size: 9px;
}
</style>
<script>
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
		<div id="main" class="clearfix">
			<div id="content" role="main">
				<form action="V3/resultsPage.jsp" class="searchform">
					<input id="search" type="search" results="10" size="50" name="q" placeholder="Search..." /> <input type="submit" value="Search" />
				</form>
				<!-- .searchform -->
				<hr />
				<h3>ENsEN: Enhanced Search Engine</h3>
				<p>Linked Data is becoming one of the most important source of information of the Web. Many projects (such as Linking Open Data -LoD-) start to provide a huge amount of open data by following the key principles for publishing Linked Data. At the same time, making use of Linked Data for improving information retrieval is becoming an important field of research, especially with the widespread use of semantic search engines.
				<p>
					In this work we propose <a>ENsEn</a>: a Search engine based on Linked Data for improving user satisfaction during his interactions with an information retrieval system. We provide the users with more intelligent search results snippets, and offer them a clustering of the results. We focus on the application of a graph ranking algorithm after having transformed the RDF graphs into a bipartite graph representation.
				</p>
				<footer class="post-meta">
				<p>
					By <a href="#">Mazen Alsarem</a>, <a href="#">Pierre-Edouard Portier</a> and <a href="#">Sylvie Calabretto</a>
				</p>
				</footer>
				<br>
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
	<script src="./js/autocomplete.js"></script>
</body>
</html>