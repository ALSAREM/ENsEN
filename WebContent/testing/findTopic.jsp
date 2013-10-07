<%@page import="java.util.HashSet"%>
<%@page import="java.util.Iterator"%>
<%@page import="java.util.Arrays"%>
<%@page import="java.util.LinkedHashSet"%>
<%@page import="java.util.Set"%>
<%@page import="java.util.ArrayList"%>
<%@page import="ensen.control.DBpediaLookupClient"%>
<%@page import="ensen.util.permute"%>
<%@page import="ensen.control.WordNetManager"%>
<%@page import="java.io.File"%>
<%@page import="java.util.Map.Entry"%>
<%@page import="java.util.TreeMap"%>
<%@page import="java.util.Comparator"%>
<%@page import="com.hp.hpl.jena.rdf.model.RDFNode"%>
<%@page import="com.hp.hpl.jena.rdf.model.SimpleSelector"%>
<%@page import="com.hp.hpl.jena.rdf.model.Selector"%>
<%@page import="java.util.HashMap"%>
<%@page import="java.util.Map"%>
<%@page import="com.hp.hpl.jena.rdf.model.Resource"%>
<%@page import="com.hp.hpl.jena.rdf.model.ResIterator"%>
<%@page import="ensen.control.RDFManager"%>
<%@page import="com.hp.hpl.jena.rdf.model.Model"%>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<link rel="stylesheet" type="text/css" href="./css/style.css">
<link rel="stylesheet" type="text/css" href="./css/popup.css">
<title>ENsEN: Find the topic of RDF Document</title>
</head>
<body>
		<header id="header" class="clearfix" role="banner"> <hgroup>
		<h1 id="site-title">
			<a href="index.jsp">Enhanced Search Engine</a>
		</h1>
		<h2 id="site-description">A better engine with Linked Data</h2>
		</hgroup> </header>
		<!-- #header -->
		<div id="main" class="clearfix" style="width:100%;" >
			<div class="wideContent" role="main" width="100%">
				<table>
		<tr>
			<td valign="top">
			<h1 id="site-title">
			The Document
		</h1>
			Find topic of a RDF by URI
				<form action="">
					<input type="text" name="file"> <input type="submit" value="Go">
				</form> <br />
				 <aside class="widget" style="overflow:scroll; width:500px; height:600px;">
            <h3>Examples</h3> 
				
				 <ul>
				  <%
				 Model m=null;
 	String files;
 	File folder = new File("D:\\Development\\workspace\\ensenWebsite\\RDF\\");
 	File[] listOfFiles = folder.listFiles();

 	for (int i = 0; i < listOfFiles.length; i++) {

 		if (listOfFiles[i].isFile()) {
 			files = listOfFiles[i].getAbsolutePath();
 			out.print("<li><a href=\"findTopic.jsp?file=" + files + "\" title=\"" + files + "\">" + listOfFiles[i].getName() + "</a></li>");
 		}
 	}
 %>
  </ul>
 </aside>
			</td>
			<td valign="top">
		<h1 id="site-title">
			The Document-Related Resources
		</h1>
				<%
					class ValueComparator implements Comparator<String> {

						Map<String, Integer> base;

						public ValueComparator(Map<String, Integer> base) {
							this.base = base;
						}

						// Note: this comparator imposes orderings that are inconsistent with equals.    
						public int compare(String a, String b) {
							if (base.get(a) >= base.get(b)) {
								return -1;
							} else {
								return 1;
							} // returning 0 would merge keys
						}
					}

					String file = request.getParameter("file");
					
					if ((file != null)&&(file.trim() != "")) {
						%>
						<h4><%=file %></h4> 	
						<aside class="widget" style="overflow:scroll; width:500px; height:750px;">
            			
				<h3>Topics</h3> 				
				 <ul>
				 <%				
						file=file.trim();
						m = RDFManager.readFile(file);
						Map<String, Integer> RSs = new HashMap<String, Integer>();
						ResIterator ss = m.listSubjects();
						int nodsCounter = 0;
						while (ss.hasNext()) {
							Resource resource = (Resource) ss.next();
							Selector selector1 = new SimpleSelector(null, null, resource);
							Selector selector2 = new SimpleSelector(resource, null, (RDFNode) null);
							RSs.put(resource.getURI(), m.listStatements(selector1).toList().size() + m.listStatements(selector2).toList().size());
						}

						ValueComparator bvc = new ValueComparator(RSs);
						TreeMap<String, Integer> sorted_map = new TreeMap<String, Integer>(bvc);
						sorted_map.putAll(RSs);

						while (!sorted_map.isEmpty()) {
							Entry<String, Integer> R = sorted_map.pollFirstEntry();
							out.print("<li><a href=\"findTopic.jsp?file=" + R.getKey() + "\">" + R.getKey() + "</a>" + R.getValue() + "</li>");
						 	
						}

					}
				%>
				</ul>
				</aside>
				</div>
			</td>
			<td width="10">  |
			</td>
			<td valign="top">
		<h1>The Query</h1>
				<form action="">
					<input type="hidden" name="file" value="<%=file%>"> Query: <input type="text" value="" name="q"> <input type="submit" value="Go">
				</form>		<hr/>	
					<div style="overflow:scroll;  width:500px; height:600px;">
			
				<%
					String q = request.getParameter("q");
					if ((file != null) && (q != null)) {
						m = RDFManager.readFile(file);

						out.print("<b>Extend text by WordNet: </b><hr/> ");
						WordNetManager WN = new WordNetManager();
						String ExtendedText = WN.extendTextWithLimit(q,4);					
						Set<String> LHS= new LinkedHashSet(Arrays.asList(ExtendedText.toLowerCase().split(" ")));
						ExtendedText="";
						Iterator<String> It=LHS.iterator();
						
						while(It.hasNext())
							{
								String str=It.next();
								if(str.length()>3)
									ExtendedText+=str+" ";
							}
						System.out.println(ExtendedText);						
						out.print(ExtendedText + "<hr/>");
						out.print("<b>Find Resources in LD: </b><hr/> ");
						String[] cominations = permute.permuteAll(ExtendedText);
						DBpediaLookupClient DBLC = new DBpediaLookupClient();
						ArrayList<String> qRelRes = new ArrayList<String>();

						for (int i = 0; i < cominations.length; i++) {
							if (cominations[i].trim().compareTo("") != 0) {
								System.out.println(cominations[i]);	
								String directAnswerURL = DBLC.getOneResource(cominations[i], "", 1);
								System.out.println(directAnswerURL);	
								if ((directAnswerURL != null)&&(directAnswerURL.trim()!="")) {
									qRelRes.add(directAnswerURL);
									//out.print(directAnswerURL + "<br/>");
								}
							}
						}
						
						Set<String> RelatedResLD = new HashSet(qRelRes);
						Iterator<String> It1=RelatedResLD.iterator();
						while(It1.hasNext())
							out.print(It1.next() + "<br/>");
						
						out.print("<hr/><h1>The Query-Related Resources </h1> <hr/>");
						ArrayList<String> qRelResFinal = new ArrayList<String>();

						for (String element : qRelRes) {

							Selector selector1 = new SimpleSelector(null, null, element);
							Selector selector2 = new SimpleSelector(m.createResource(element), null, (RDFNode) null);
							if ((m.listStatements(selector1).toList().size() > 0) || (m.listStatements(selector2).toList().size() > 0)) {
								qRelResFinal.add(element);
								System.out.print(element + "<br/>");
							}

						}
						
						Set<String> RelatedResDoc = new HashSet(qRelResFinal);
						Iterator<String> It2=RelatedResDoc.iterator();
						while(It2.hasNext())
							out.print(It2.next() + "<br/>");

					}
				%>
				</div>
			</td>
		</tr>
		<tr>
		<td colspan="6">
		<a onclick='fixAllNodes();'>fix nodes</a>
	<hr />
	<div id="graph" name="graph">
		<script src="./js/d3.v3.min.js"></script>
<style>
.node {
	stroke: #fff;
	stroke-width: 1.5px;
}

.link {
	stroke: #999;
	stroke-opacity: .6;
}
</style>
<script>
		var width = 1600, height = 800;

		var color = d3.scale.category20();

		var force = d3.layout.force().charge(-220).linkDistance(40).size(
				[ width, height ]).linkStrength(0.8);

		var svg = d3.select("#graph").append("svg").attr("width", width).attr(
				"height", height).attr("pointer-events", "all").append('svg:g')
				.call(d3.behavior.zoom().on("zoom", redraw)).append('svg:g');

		svg.append('svg:rect').attr('width', width).attr('height', height)
				.attr('fill', 'white');

		function redraw() {
			console.log("here", d3.event.translate, d3.event.scale);
			svg.attr("transform", "translate(" + d3.event.translate + ")"
					+ " scale(" + d3.event.scale + ")");
		};
	<%out.print("var myjson ='"+RDFManager.Model2D3Json(m)+"';");%>
		json = JSON.parse(myjson);

		force.nodes(json.nodes).links(json.links).start();

		var link = svg.selectAll("line.link").data(json.links).enter().append(
				"line").attr("class", "link").style("stroke-width",
				function(d) {
					return Math.sqrt(d.value);
				}).attr("title", function(d) {
			return d.value;
		}).attr("style", "stroke:#00d1d6;stroke-width:4px");

		var node = svg.selectAll("circle.node").data(json.nodes).enter()
				.append("circle").attr("class", "node").attr("r", 10).style(
						"fill", function(d) {
							return color(d.group);
						}).call(force.drag);

		node.append("title").text(function(d) {
			return d.name;
		});

		link.append("title").text(function(d) {
			return d.value;
		});

		force.on("tick", function() {
			link.attr("x1", function(d) {
				return d.source.x;
			}).attr("y1", function(d) {
				return d.source.y;
			}).attr("x2", function(d) {
				return d.target.x;
			}).attr("y2", function(d) {
				return d.target.y;
			});

			node.attr("cx", function(d) {
				return d.x;
			}).attr("cy", function(d) {
				return d.y;
			});
			node.fixed = true;
		});

		function fixAllNodes() {
			for ( var i = 0, count = force.nodes().length; i < count; i++) {
				force.nodes()[i].fixed = true;
			}
		}
		function unfixAllNodes() {
			for ( var i = 0, count = force.nodes().length; i < count; i++) {
				if (force.nodes()[i]['is'] == "node")
					force.nodes()[i].fixed = false;
			}
		}
	</script>
	</div>
		</td>
		
		</tr>
	</table>
	</div>
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
	<!-- JavaScript at the bottom for fast page loading -->
	<script src="http://ajax.googleapis.com/ajax/libs/jquery/1.6/jquery.min.js"></script>
	<script src="js/script.js"></script>
</body>
</html>
</body>
</html>