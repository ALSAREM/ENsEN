<%@page import="ensen.entities.EnsenDBpediaResource"%>
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

		try {
			List<EnsenDBpediaResource> responses = documents.get(i).triplets;
			for (int j = 0; j < responses.size(); j++) {
				EnsenDBpediaResource R = responses.get(j);
				out.print("<a href='" + R.getFullUri() + "' target='_blank'>" + j + " - " + R.toString() + "</a><br/>");

			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	
	ensenSession.setAttribute("documents", documents);
	%>
</body>
</html>