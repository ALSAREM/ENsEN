<%@page import="ensen.entities.Document"%>
<%@page import="java.util.ArrayList"%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
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
	<%=documents.get(i).text%>
</body>
</html>