<%@page import="java.io.FileReader"%>
<%@page import="java.io.BufferedReader"%>
<%@page import="java.io.StringWriter"%>
<%@page import="ensen.util.Printer"%>
<%@page import="ensen.controler.RDFManager"%>
<%@page import="ensen.entities.Document"%>
<%@page import="java.util.TreeMap"%>
<%@ page language="java" contentType="text/plain; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%
HttpSession ensenSession = request.getSession();
int id = Integer.parseInt(request.getParameter("id"));
long sessionId = Long.parseLong(request.getParameter("sessionId"));//sessionId
TreeMap <Integer,Document>  documents = (TreeMap <Integer,Document> ) ensenSession.getAttribute("documents"+sessionId);
Document doc=documents.get(id);

String syntax = "Turtle";
StringWriter out2 = new StringWriter();
doc.graphWithoutLiteral.write(out2, syntax);
String out3 = out2.toString();
%> 
<%=out3 %>
