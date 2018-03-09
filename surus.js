function render(start, end) {
	console.log([start, end])
	google.charts.load('current', {
		'packages': ['corechart']
	});
	google.charts.setOnLoadCallback(drawChart);

	function drawChart() {
		var ssm = allPoints.filter(function(mileAndAlt) { return mileAndAlt[0] > start && mileAndAlt[0] < end })
		ssm.unshift(["Miles", "Alt"])
		console.log(ssm)
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
				minValue: 0
			}
		};

		var chart = new google.visualization.AreaChart(document.getElementById('chart_div'));
		chart.draw(data, options);
	}
}