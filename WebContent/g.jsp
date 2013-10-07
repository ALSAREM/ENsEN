<%@page import="com.google.api.services.customsearch.model.Result"%>
<%@page import="java.util.List"%>
<%@page import="ensen.entities.Document"%>
<%@page import="java.util.ArrayList"%>
<%@page import="ensen.control.Searcher"%>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>Insert title here</title>
</head>
<style>
.title {
	color: rgb(17, 34, 204);
cursor: pointer;
display: inline;
font-family: arial, sans-serif;
font-size: 16px;
font-weight: normal; 
height: auto;
line-height: 19px;
list-style-image: none;
list-style-position: outside;
list-style-type: none;
text-align: left;
text-decoration: underline;
visibility: visible;
white-space: nowrap;
width: auto;
zoom: 1;
}
.citeP{
color: rgb(0, 153, 51);
display: inline-block;
font-family: arial, sans-serif;
font-size: 14px;
font-style: normal;
font-weight: normal;
height: 16px;
line-height: 16px;
list-style-image: none;
list-style-position: outside;
list-style-type: none;
margin-bottom: 1px;
text-align: left;
visibility: visible;
white-space: nowrap;
width: 196px;
zoom: 1;
}
</style>
<body>
	<%
		Searcher S = new Searcher();
		List<Result> docs = S.searchInGoogle("Syria", 3);
		for (Result d : docs) {
	%>
	<br />
	<h3 class="title">
		<a href="<%=d.getFormattedUrl()%>"><%=d.getHtmlTitle()%></a>
	</h3>
	<div>
		<cite class="citeP"><%=d.getFormattedUrl()%></cite>
		<div><%=d.getHtmlSnippet()%></div>
	</div>
	<%=d.getPagemap() %>
	<%
	
		}
	%>
</body>
</html>