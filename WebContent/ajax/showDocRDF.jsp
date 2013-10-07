<%@page import="java.io.StringWriter"%>
<%@page import="org.dbpedia.spotlight.model.DBpediaResource"%>
<%@page import="java.util.List"%>
<%@page import="ensen.entities.Document"%>
<%@page import="java.util.ArrayList"%>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>Insert title here</title>
</head>
<body>
	<%
		HttpSession ensenSession = request.getSession();
		int i = Integer.parseInt(request.getParameter("i"));
		ArrayList<Document> documents = (ArrayList<Document>) ensenSession.getAttribute("documents");
	%>
	<%
		String resString = "";
		String syntax = "RDF/XML-ABBREV"; // also try "N-TRIPLE"					
		StringWriter outW = new StringWriter();
		try {
			documents.get(i).fullGraph.write(outW, syntax);
			resString = outW.toString();
		} catch (Exception e) {
			e.printStackTrace();
			resString = "";
		}
	%>
	<textarea rows="30" cols="137"><%=resString.trim()%></textarea>
</body>
</html>