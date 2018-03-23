Array.prototype.max = function() {
  return Math.max.apply(null, this);
};

Array.prototype.min = function() {
  return Math.min.apply(null, this);
};

function render(start, end) {
	google.charts.load('current', {
		'packages': ['corechart']
	});
	google.charts.setOnLoadCallback(drawChart);

	function drawChart() {
		var ssm = allPoints.filter(function(mileAndAlt) { return mileAndAlt[0] > start && mileAndAlt[0] < end })
		ssm.unshift(["Miles", "Alt"])
		var alts = ssm.map(function(mileAndAlt) { return mileAndAlt[1] })
		alts.shift()
		var min = alts.min()
		var data = google.visualization.arrayToDataTable(ssm)
		
		var options = {
			title: 'May vs. October',
			hAxis: {
				title: 'Mile',
				titleTextStyle: {
					color: '#333'
				}
			},
			vAxis: {
				minValue: min
			}
		};

		var chart = new google.visualization.AreaChart(document.getElementById('chart_div'));
		chart.draw(data, options);
	}
}