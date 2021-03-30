var finalData = [];
var labels = [];

var map={};

jsonData.forEach(function(e) {
	var category=e.x;
	var value=map[category];
	if(value == undefined){
		value=0;
	}
	
	value = value + e.y;
	map[category] = value;
});

for (var key in map) {
   if (map.hasOwnProperty(key)) {
	   labels.push(key);
      finalData.push(map[key]);
   }
}

$.extend(true,options,{
	chart: {
		type: 'pie',
		width: 400
	},
	series: finalData,
	labels: labels	
});