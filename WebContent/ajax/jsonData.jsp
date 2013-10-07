<%@page language="java" contentType="application/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@page import="com.hp.hpl.jena.rdf.model.Property"%>
<%@page import="com.hp.hpl.jena.rdf.model.Resource"%>
<%@page import="ensen.control.RDFManager"%>
<%@page import="com.hp.hpl.jena.rdf.model.Model"%>
<%@page import="ensen.entities.Document"%>
<%@page import="java.util.ArrayList"%>
<%
	Model m = RDFManager.createRDFModel();
	Resource A = m.createResource("http://mazen.org/A");
	Resource B = m.createResource("http://mazen.org/B");
	Resource C = m.createResource("http://mazen.org/C");
	Property p1 = m.createProperty("http://mazen.org/linkto1");
	Property p2 = m.createProperty("http://mazen.org/linkto2");
	A.addProperty(p1, B);
	B.addProperty(p2, C);
%>

<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<script src="../js/d3.v3.min.js"></script>
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
<title>Insert title here</title>
</head>
<body>
	<a onclick='fixAllNodes();' href="#">fix nodes</a>
	<hr />
	<script>
		var width = 1060, height = 800;

		var color = d3.scale.category20();

		var force = d3.layout.force().charge(-220).linkDistance(40).size(
				[ width, height ]).linkStrength(0.8);

		var svg = d3.select("body").append("svg").attr("width", width).attr(
				"height", height).attr("pointer-events", "all").append('svg:g')
				.call(d3.behavior.zoom().on("zoom", redraw)).append('svg:g');
		
		svg.append('svg:rect')
	    .attr('width', width)
	    .attr('height', height)
	    .attr('fill', 'white');

	function redraw() {
	  console.log("here", d3.event.translate, d3.event.scale);
	  svg.attr("transform",
	      "translate(" + d3.event.translate + ")"
	      + " scale(" + d3.event.scale + ")");
	};
	
	<%out.print("var myjson ='" + RDFManager.Model2D3Json(m) + "';");%>
		json = JSON.parse(myjson);

		force.nodes(json.nodes).links(json.links).start();

		var link = svg.selectAll("line.link").data(json.links).enter().append(
				"line").attr("class", "link").style("stroke-width",
				function(d) {
					return Math.sqrt(d.value);
				}).attr("title",function(d){return d.value;})
			      .attr("style", "stroke:#00d1d6;stroke-width:4px");

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
		      for (var i = 0, count = force.nodes().length ; i < count ; i ++ ) {
		        force.nodes()[i].fixed = true;
		      }
		    }
		 function unfixAllNodes() {
		      for (var i = 0, count = force.nodes().length ; i < count ; i ++ ) {
		        if (force.nodes()[i]['is'] == "node") force.nodes()[i].fixed = false;
		      }
		    }
	</script>
</body>
</html>