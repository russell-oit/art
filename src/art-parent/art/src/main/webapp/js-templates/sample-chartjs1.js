var finalData = [];
var labels = [];

jsonData.forEach(function(e) {
    labels.push(e.Item);
    finalData.push(e.Volume);
});

//https://github.com/chartjs/Chart.js/issues/815
var colors=randomColor({
	count: labels.length
});

var data = {
    labels:labels,
    datasets: [
        {
            data: finalData,
			backgroundColor: colors
        }
	]
};

//http://www.cryst.co.uk/2016/06/03/adding-percentages-chart-js-pie-chart-tooltips/
$.extend(config,{
	type: 'pie',
	data: data
});
