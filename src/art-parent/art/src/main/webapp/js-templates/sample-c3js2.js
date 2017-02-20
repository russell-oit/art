//https://stackoverflow.com/questions/30886203/pie-chart-from-json-using-c3-js
var finalData = {};
var values = [];

jsonData.forEach(function(e) {
    values.push(e.Item);
    finalData[e.Item] = e.Volume;
});

var c3data = {
	json: [finalData],
	keys: {
		value: values
	},
	type: 'pie'
};

//https://stackoverflow.com/questions/30603381/d3-show-number-instead-of-percentages-on-pie-chart
$.extend(options, {
	data: c3data,
	pie: {
        label: {
            format: function (value, ratio, id) {
                //https://stackoverflow.com/questions/35702188/how-to-center-labels-inside-a-c3-js-piechart/35750311
				var percent = ratio * 100;
                return value + ' (' + percent.toFixed(1) + '%)';
            }
        }
    }
});