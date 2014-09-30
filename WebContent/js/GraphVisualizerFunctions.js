function redraw() {	
	svg.attr("transform", "translate(" + d3.event.translate + ")" + " scale("
			+ d3.event.scale + ")");
};
function fixAllNodes() {
	for ( var i = 0, count = force.nodes().length; i < count; i++) {
		force.nodes()[i].fixed = true;
	}
};
function unfixAllNodes() {
	for ( var i = 0, count = force.nodes().length; i < count; i++) {
		if (force.nodes()[i]['is'] == "node")
			force.nodes()[i].fixed = false;
	}
};

function tick(){
	node.attr("transform", function(d) {
		var xm;
		if (d.group == 0)
			xm = 50;
		else if (d.group == 1)
			xm = 180;
		else
			xm = 220+d.x;
		
		var ym;
		if (d.group == 0)
			ym = 30;
		else if (d.group == 1)
			ym = d.index * 75 ;
		else
			ym = ((d.index%6)+1) * 75;
		return "translate(" + xm + "," + ym + ")";
	});

	link.attr("x1", function(d) {
		if (d.source.group == 0)
			return 50;
		else if (d.source.group == 1)
			return 180;
		else
			return 220+d.source.x;
	}).attr("y1", function(d) {
		if (d.source.group == 0)
			return 30;
		else if (d.source.group == 1)
			return d.source.index * 75;
		else
			return  ((d.source.index%6)+1) * 75;
	}).attr("x2", function(d) {
		if (d.target.group == 0)
			return 50;
		else if (d.target.group == 1)
			return 180;
		else
			return 220+d.target.x;
	}).attr("y2", function(d) {
		if (d.target.group == 0)
			return 30;
		else if (d.target.group == 1)
			return d.target.index * 75;
		else
			return ((d.target.index%6)+1) * 75;
	});
	
}

function zoomIn() {	
}

function zoomOut() {	
}

function reset() {	
}

function fade(opacity) {
	return function(d) {
		node.style("stroke-opacity", function(o) {
			thisOpacity = isConnected(d, o) ? 1 : opacity;
			this.setAttribute('fill-opacity', thisOpacity);
			return thisOpacity;
		});

		link.style("stroke-opacity", function(o) {
			return o.source === d || o.target === d ? 1 : opacity;
		});
	};
}

function isConnected(a, b) {
    return linkedByIndex[a.index + "," + b.index] || linkedByIndex[b.index + "," + a.index] || a.index == b.index;
}