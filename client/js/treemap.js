
	var chartWidth = 589;
    var chartHeight = 587;
    var headerHeight = 22;
    var headerColor = "#555555";
    var transitionDuration = 500;
    var xscale = d3.scale.linear().range([0, chartWidth]);
    var yscale = d3.scale.linear().range([0, chartHeight]);
    var color = d3.scale.category10();
    var root;
    var node;
	var data2;
	
    var treemap = d3.layout.treemap()
    .size([chartWidth, chartHeight])
    .sticky(true)
    .value(function(d) { return d.size; });

	var chart = d3.select("#body")
	.append("div")
	.attr("id", "tree")
    .style("position", "relative")
    .style("width", chartWidth + "px")
    .style("height", chartHeight + "px");
		
function switchData(dataFn, chartWidthNew, chartHeightNew) {
	d3.select("#tree").remove();
	chartWidth = chartWidthNew;
	chartHeight = chartHeightNew;
	
    xscale = d3.scale.linear().range([0, chartWidth]);
    yscale = d3.scale.linear().range([0, chartHeight]);
	
	treemap = d3.layout.treemap()
    .size([chartWidth, chartHeight - 60])
    .sticky(true)
    .value(function(d) { return d.size; });
		
	chart = d3.select("#body")
	.append("div")
	.attr("id", "tree")
    .style("position", "relative")
    .style("width", chartWidth + "px")
    .style("height", chartHeight + "px");
		
	drawTreemap(dataFn);
}
function drawTreemap(datFn01) {

		root = JSON.parse(datFn01);
        //node = data = root;

		var nodes = treemap.nodes(root);
		
		var children = nodes.filter(function(d) {
            return !d.children;
        });
        var parents = nodes.filter(function(d) {
            return d.children;
        });

        // create parent cells
		var counter = 0;
        var parentCells = chart.selectAll("div.cell.parent")
            .data(parents, function(d) {
				counter += 1;
                return "p-" + d.name + "-" + counter;
            });
        var parentEnterTransition = parentCells.enter()
            .append("div")
            .attr("class", "cell parent");
        parentEnterTransition.append("div")
			.attr("class", "rect")
            .style("height", headerHeight + 'px')
            .style("background-color", headerColor)
            .style("width", function(d) {
                return Math.max(0.01, d.dx) + 'px';
            })
			.style("position", "absolute")
			.style("z-index" , "10")
			.append('div')
            .attr("class", "foreignObject")
            .append("div")
            .attr("class", "labelbody")
            .append("div")
            .attr("class", "label");
			
        // update transition
        var parentUpdateTransition = parentCells.transition().duration(transitionDuration);
        parentUpdateTransition.select(".rect")
			.style("position", "absolute")
            .style("width", function(d) {
                return Math.max(0.01, d.dx) + 'px';
            })
            .style("height", headerHeight + 'px')
            .style("transform", function(d) {
				var e = d;
				var deep = 0;
				while (e.parent) {
					deep += 20;
					e = e.parent;
				}
				var parentHeight = d.y + (d.children ? deep : 0);
                return "translate(" + d.x + "px," + (parentHeight) + "px)";
            });
        parentUpdateTransition.select(".rect")
			.style("position", "absolute")
            .style("background-color", headerColor);
        parentUpdateTransition.select(".foreignObject")
            .style("width", function(d) {
                return Math.max(0.01, d.dx) + 'px';
            })
            .style("height", headerHeight)
            .select(".labelbody .label")
            .text(function(d) {
                return d.name;
            });
        // remove transition
        parentCells.exit()
            .remove();

        // create children cells
        var childrenCells = chart.selectAll("div.cell.child")
            .data(children, function(d) {
				counter += 1;
                return "c-" + d.name + "-" + counter;
            });
        // enter transition
        var childEnterTransition = childrenCells.enter()
            .append("div")
            .attr("class", "cell child");
        childEnterTransition.append("div")
			.attr("class", "rect")
            .style("background-color:", function(d) {
                return color(d.parent.parent.name);
            })
			.style("opacity", function(d) {
					return d.children ? 1 : d.similarity/100*2;
			})
			.append('div')
            .attr("class", "foreignObject")
            .style("width", function(d) {
                return Math.max(0.01, d.dx) + 'px';
            })
            .style("height", function(d) {
                return Math.max(0.01, d.dy) + 'px';
            })
			.style("position", "absolute")
            .append("div")
            .attr("class", "labelbody")
            .append("div")
            .attr("class", "label")
            .html(function(d) {
                return '<div style="margin-top: 24px"><img src="images/' + (d.parent.mood ? (d.parent.mood == '+' ? 'p' : (d.parent.mood == '-' ? 'b' : 'n')) : 'n') + '.png"> <img align="right" src="'+d.parent.icon+'"> <br /><div id="newsarticle" style="display: none"><b>' + d.parent.name + '</b><br /><br />' + d.name + '</div></div>';
            });

            //childEnterTransition.selectAll(".foreignObject")
            //    .style("display", "none");
        

        // update transition
        var childUpdateTransition = childrenCells.transition().duration(transitionDuration);
        childUpdateTransition.select(".rect")
            .style("width", function(d) {
                return Math.max(0.01, d.dx) + 'px';
            })
            .style("height", function(d) {
                return d.dy + 'px';
            })
			.style("position", "absolute")
            .style("transform", function(d) {
				var e = d;
				var deep = 0;
				while (e.parent) {
					deep += 20;
					e = e.parent;
				}
				var parentHeight = d.y + (d.children ? deep +20 : 0);
                return "translate(" + d.x  + "px," + (d.y+60) + "px)";
            });
        childUpdateTransition.select(".rect")
            .style("background-color", function(d) {
                return color(d.parent.parent.name);
            })
			.style("opacity", function(d) {
					return d.children ? 1 : d.similarity/100*2;
			});
        
        // exit transition
        childrenCells.exit()
            .remove();
	d3.selectAll("input").on("change", function change() {
		var value = this.value === "count"
			? function() { return 1; }
			: function(d) { return d.size; };

		node
			.data(treemap.value(value).nodes)
		  .transition()
			.duration(1500)
			.call(position);
	});
	
	// Klick auf eine Kachel lädt den Inhalt in die Textbox rechts neben der Treemap
	$('.child .rect .foreignObject').on('click', function() { setNewsText($(this).find('#newsarticle').html()); });
}

// RGB Komponenten aufteilen
function getRGBComponents (color) {
	var r = color.substring(1, 3);
	var g = color.substring(3, 5);
	var b = color.substring(5, 7);
	return {
		R: parseInt(r, 16),
		G: parseInt(g, 16),
		B: parseInt(b, 16)
	};
}

// Ideale Text Farbe berechnen
function idealTextColor (bgColor) {
	var nThreshold = 105;
	var components = getRGBComponents(bgColor);
	var bgDelta = (components.R * 0.299) + (components.G * 0.587) + (components.B * 0.114);
	return ((255 - bgDelta) < nThreshold) ? "#000000" : "#ffffff";
}

// Größe der Kachel berechnen
function position() {
  this.style("left", function(d) { return d.x + "px"; })
      .style("top", function(d) { return d.y + "px"; })
      .style("width", function(d) { return Math.max(0, d.dx - 1 - 10) + "px"; })
      .style("height", function(d) { return Math.max(0, d.dy - 1 - 10) + "px"; });
}

function setNewsText(textVal) {
	$("#article").html(textVal);
}