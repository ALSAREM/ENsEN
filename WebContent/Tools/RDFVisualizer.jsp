<%@page import="ensen.entities.Document"%>
<%@page import="java.util.ArrayList"%>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@page import="com.hp.hpl.jena.rdf.model.Property"%>
<%@page import="com.hp.hpl.jena.rdf.model.Resource"%>
<%@page import="ensen.control.RDFManager"%>
<%@page import="com.hp.hpl.jena.rdf.model.Model"%>
<%
	HttpSession ensenSession = request.getSession();
int i = Integer.parseInt(request.getParameter("i"));
ArrayList<Document> documents = (ArrayList<Document>) ensenSession.getAttribute("documents");

/*ArrayList<Model> graphs = (ArrayList<Model>) ensenSession.getAttribute("graphs");
ensenSession.setAttribute("graphs", graphs);*/
ensenSession.setAttribute("documents", documents);
%>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<script src="./js/d3.v3.min.js"></script>
<style>	
// CSS
div.tooltip {
  position: absolute;
  width: 130px;
  height: 60px;
  background: #ddd;
}
</style>
<title>RDF Visualizer</title>
</head>
<body>
	<a onclick='fixAllNodes();' href="#">fix nodes</a>
	<hr />
	<script>
		var width = 1060, height = 800;
		if (document.compatMode == 'CSS1Compat' && document.documentElement
				&& document.documentElement.offsetWidth) {
			width = document.documentElement.offsetWidth;
			height = document.documentElement.offsetHeight;
		}
		if (window.innerWidth && window.innerHeight) {
			width = window.innerWidth;
			height = window.innerHeight;
		}

		var color = d3.scale.category20();

		var force = d3.layout.force().charge(-500).linkDistance(40).size(
				[ width, height ]).linkStrength(0.8);

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
	<%out.print("var myjson ='"+RDFManager.Model2D3Json(documents.get(i).fullGraph)+"';");%>
		json = JSON.parse(myjson);

		force.nodes(json.nodes).links(json.links).start();

		var link = svg.selectAll("line.link").data(json.links).enter().append(
				"line").attr("class", "link").style("stroke-width",
				function(d) {
					return Math.sqrt(d.value);
				}).attr("title", function(d) {
			return d.value;
		}).attr("style", "stroke:#999999;stroke-width:1px");

		var node = svg.selectAll("circle.node").data(json.nodes).enter()
				.append("circle").attr("class", "node").attr("r", 15).style(
						"fill", function(d) {
							return color(d.group);
						}).call(force.drag);

	/*	node.append("title").text(function(d) {
			return d.name;
		});*/

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
		var tooltip = d3.select("body").append("div")
		  .attr("class", "tooltip").style('position', 'absolute') 
                .style('z-index', 1001);
		
		svg.selectAll("circle.node").on("mouseover", function(d) {	
			tooltip.style("visibility","true");
			tooltip.transition()
		      .duration(100)
		      .style("opacity", 1);
		    tooltip.html(d.name)
		     .style("left", d.x + "px")
		     .style("top", d.y + "px");
		    
			svg.selectAll("circle.node").filter(function(n) {				
				return !isConnected(d,n);
			}).transition().style("opacity",0.1);
			
			svg.selectAll("line.link").filter(function(l) {
				return l.source.index != d.index && l.target.index != d.index; 
			}).transition().style("opacity", 0.1);
			
			
			
		});
		
		function isConnected(a,b) {	
			return svg.selectAll("line.link").filter(function(t) {
				return((t.source.index == a.index && t.target.index == b.index)|| (t.source.index == b.index && t.target.index == a.index)|| (a.index==b.index));	
			}) !="";
		}
		
	/*	function countLinks(a) {			
			return svg.selectAll("circle.node").select(function(t) {
				return isConnected(a,t);	
			}).length;
		}*/
		
		svg.selectAll("circle.node").on("mouseout", function(d) {
			tooltip.style("visibility","false");
			svg.selectAll("circle.node").transition().style("opacity", 1);
			svg.selectAll("line.link").transition().style("opacity", 1);
		});

		svg.selectAll("circle.node").on("click", function(d) {
			
			window.open(d.name);
			self.focus();
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

</body>
</html>
