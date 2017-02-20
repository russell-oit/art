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

$.extend(options, {
	data: c3data
});