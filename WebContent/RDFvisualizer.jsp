<%@page import="java.util.TreeMap"%>
<%@page import="ensen.controler.RDFManager"%>
<%@page import="ensen.entities.Document"%>
<%@page import="java.util.ArrayList"%>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@page import="com.hp.hpl.jena.rdf.model.Property"%>
<%@page import="com.hp.hpl.jena.rdf.model.Resource"%>
<%@page import="com.hp.hpl.jena.rdf.model.Model"%>
<%
try{
HttpSession ensenSession = request.getSession();
int i = Integer.parseInt(request.getParameter("i"));
long sessionId = Long.parseLong(request.getParameter("sessionId"));//sessionId
TreeMap <Integer,Document>  documents = (TreeMap <Integer,Document> ) ensenSession.getAttribute("documents"+sessionId);
Document doc=documents.get(i);
if(doc!=null)
	{
	//Model graph = RDFManager.readFile("D:\\LIRIS\\workspace\\ensenTensorielWeb\\RDF\\RDFfile_1395438902384.rdf");
	/* prepare the graph*/
	Model graph=doc.fullGraph;
	graph = RDFManager.getGraphWithNoOnlyObjectsResourecs(graph);
	graph = RDFManager.filterPredicates(graph, "ensen.org/cluster");
	
%>
<html>
<head>

<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">


<link rel="stylesheet" href="./css/ensen.css" />
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

<script src="./js/d3.v3.min.js"></script>

<title>Conceptual graph for:   <%="xxx"/*doc.content.getTitle()*/ %></title>
</head>
<body>
<div >
<script>
	
		var width = 1200, height = 1000;

		var color = d3.scale.category20();

		var force = d3.layout.force().charge(-400).linkDistance(150).size(
				[ width, height ]).linkStrength(0.1);

		var svg = d3.select("body").append("svg").attr("width", width).attr(
				"height", height).attr("pointer-events", "all").append('svg:g')
				.call(d3.behavior.zoom().on("zoom", redraw)).append('svg:g');

		svg.append('svg:rect').attr('width', width).attr('height', height)
				.attr('fill', 'white');

		function redraw() {
			console.log("here", d3.event.translate, d3.event.scale);
			svg.attr("transform", "translate(" + d3.event.translate + ")"
					+ " scale(" + d3.event.scale + ")");
		};
	<%out.print("var myjson ='"+RDFManager.Model2D3Json(graph)+"';");%>
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
				.append("circle").attr("class", "node").attr("r", function(d){return ((d.count!=null) ? d.count/5+15 : 15);}).style(
						"fill", function(d) {
							return color(d.count/10);
						}).call(force.drag);

		/*node.append("title").text(function(d) {
			return d.name;
		});*/
		
		node.append("text").attr("text-anchor", "middle").text(function(d) {
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
	<!-- #wrapper -->
</body>
</html>
<% }  else { 
		out.print("<img src='./images/wireless.svg' />");
 } 
}catch(Exception e){
	e.printStackTrace();
	out.print("<img src='./images/wireless.svg' />");
}
 %>