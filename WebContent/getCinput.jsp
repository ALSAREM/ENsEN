<%@page import="java.io.FileReader"%>
<%@page import="java.io.BufferedReader"%>
<%@page import="ensen.entities.Document"%>
<%@page import="java.util.TreeMap"%>
<%@ page language="java" contentType="text/plain; charset=UTF-8"   pageEncoding="UTF-8"%>
<%
String output="";
HttpSession ensenSession = request.getSession();
int id = Integer.parseInt(request.getParameter("id"));
long sessionId = Long.parseLong(request.getParameter("sessionId"));//sessionId
TreeMap <Integer,Document>  documents = (TreeMap <Integer,Document> ) ensenSession.getAttribute("documents"+sessionId);
Document doc=documents.get(id);
output=doc.CgeneratedMatrix;
BufferedReader br = new BufferedReader(new FileReader(output));
try {
    StringBuilder sb = new StringBuilder();
    String line = br.readLine();

    while (line != null) {
        sb.append(line);
        sb.append("\n");
        line = br.readLine();
    }
    output = sb.toString();
} finally {
    br.close();
}
%>
<%=output %>
