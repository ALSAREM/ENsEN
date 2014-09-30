<%@page import="ensen.util.HTMLreader"%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<style>
#ensenSentense0:HOVER {
	text-decoration: none;
	
}
</style>
<script src="./js/find.js"></script>
<script src="./js/jquery.1.9.1.js"></script>
<%
String url=request.getParameter("url");
String txt=request.getParameter("sent");
%>
<script type='text/javascript'>
$(function() {	findTheSentence('<%=txt %>');});
</script>

<%
out.print(HTMLreader.load(url));
%>