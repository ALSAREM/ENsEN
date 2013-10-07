<%@page import="com.hp.hpl.jena.rdf.model.RDFNode"%>
<%@page import="ensen.entities.Triplet"%>
<%@page import="java.util.Map"%>
<%@page import="ensen.entities.EnsenDBpediaResource"%>
<%@page import="java.util.List"%>
<%@page import="java.io.ObjectInputStream"%>
<%@page import="java.io.ObjectInput"%>
<%@page import="java.io.BufferedInputStream"%>
<%@page import="java.io.InputStream"%>
<%@page import="java.io.OutputStream"%>
<%@page import="java.io.IOException"%>
<%@page import="java.io.BufferedOutputStream"%>
<%@page import="java.io.ObjectOutputStream"%>
<%@page import="java.io.ObjectOutput"%>
<%@page import="java.io.FileOutputStream"%>
<%@page import="com.google.api.services.customsearch.model.Result"%>
<%@page import="java.util.ArrayList"%>
<%@page import="java.util.HashSet"%>
<%@page import="ensen.entities.Query"%>
<%@page import="ensen.entities.Document"%>
<%@page import="ensen.entities.EnsenDBpediaResource"%>
<%@page import="ensen.control.RDFManager"%>
<%@page import="ensen.control.PropertiesManager"%>
<%@page import="java.io.FileInputStream"%>
<%@page import="java.util.Scanner"%>
<%@page import="com.hp.hpl.jena.rdf.model.Model"%>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>Test Algorithms</title>
<%
	//loading query 
	Query query = new Query();
	query.RelatedResLD = new HashSet<String>();
	query.QExtendedRes = new ArrayList<String>();
	String NL = "<br/>";
	boolean extended = false;
	Scanner scanner = new Scanner(new FileInputStream(PropertiesManager.getProperty("testingPath") + "files\\q.txt"));
	try {
		query.Text = scanner.nextLine();
		query.ExtendedText = scanner.nextLine();
		while (scanner.hasNextLine()) {
			String line = scanner.nextLine();
			if (line.contains("?@?")) {
				extended = true;
			} else {
				if (extended)
					query.QExtendedRes.add(line);
				else
					query.RelatedResLD.add(line);
			}
		}
	} finally {
		scanner.close();
	}

	//loading document
	Document Doc = new Document();
	Doc.content = new Result();
	int docId = 1;
	int counter = 1;
	scanner = new Scanner(new FileInputStream(PropertiesManager.getProperty("testingPath") + "files\\Docs.txt"));
	try {
		while ((counter < docId)) {
			scanner.nextLine();
			scanner.nextLine();
			scanner.nextLine();
			counter++;
		}
		Doc.content.setTitle(scanner.nextLine());
		Doc.content.setFormattedUrl(scanner.nextLine());
		Doc.content.setSnippet(scanner.nextLine());
		Doc.graph = RDFManager.readFile(PropertiesManager.getProperty("testingPath") + "files\\D" + docId + "_graph.rdf");
		Doc.fullGraph = Doc.graph;//RDFManager.readFile(PropertiesManager.getProperty("testingPath") + "files\\D" + docId + "_fullGraph.rdf");

	} catch (Exception ex) {
		ex.printStackTrace();
	} finally {
		scanner.close();
	}
	try {
		//use buffering
		InputStream file = new FileInputStream(PropertiesManager.getProperty("testingPath") + "files\\D" + docId + "_triples.ser");
		InputStream buffer = new BufferedInputStream(file);
		ObjectInput input = new ObjectInputStream(buffer);
		try {
			//deserialize the List
			Doc.triplets = (List<EnsenDBpediaResource>) input.readObject();

		} finally {
			input.close();
		}
	} catch (ClassNotFoundException ex) {
		ex.printStackTrace();
	} catch (IOException ex) {
		ex.printStackTrace();
	}

	//matching
	double a = 0.275;//in
	double b = 0.05;//out
	double c = 0.5;//freq
	double d = 0.175;//Query

	//Doc.analyzeDocument(query, a, b, c, d, false);
%>
</head>
<body>
	<b>The Query: </b><%=query.Text%>
	<hr />
	<b>The Extended Query:</b>
	<%=query.ExtendedText%>
	<hr />
	<b>Query's resources:</b>
	<br />
	<%
		for (String res : query.RelatedResLD) {
			out.print("<a href='" + res + "'>" + res + "</a><br />");
		}
	%>
	<br /> Extended:
	<%
		for (String res : query.QExtendedRes) {
			out.print("<a href='" + res + "'>" + res + "</a>");
		}
	%>
	<hr />
	<b>Document: <%=Doc.content.getTitle()%></b>
	<hr/>
	<b>Selected resources:</b><br />
	<%
	for (Map.Entry<String, Double> entry : Doc.allRankedResources.entrySet()) {
		out.print( entry.getValue() + " : <a href='" + entry.getKey() + "'>" + entry.getKey() + "</a><br />");
	}		
	%>
	<hr/>
	<b>Selected Triples:</b>
	<%
	for (Map.Entry<Triplet, Double> entry : Doc.TopTriples.entrySet()) {	
		out.print("<br />"+ entry.getValue() + " : <a href='" + entry.getKey().statement.getSubject().getURI() + "'>" + entry.getKey().statement.getSubject().getLocalName() + "</a>");
		out.print( "--> <a href='" + entry.getKey().statement.getPredicate().getURI() + "'>" + entry.getKey().statement.getPredicate().getLocalName() + "</a>");
		RDFNode O=entry.getKey().statement.getObject();
		if(O.isResource())
			out.print( "--> <a href='" + O.asResource().getURI() + "'>" +  O.asResource().getLocalName() + "</a>");
		else
			out.print( "--> \"" +  O.asLiteral().getString().substring(0,Math.min(O.asLiteral().getString().length(), 30))+"\"");		
		
			}		
	%>
</body>
</html>