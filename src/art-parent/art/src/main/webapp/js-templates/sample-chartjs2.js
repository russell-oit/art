var labels = [];
var map={};

jsonData.forEach(function(e) {
	labels.push(e["Month"]);
	
	var name=e.Name;
	var row=map[name];
	if(row == undefined){
		row={};
	}
	var month=e.Month;	
	row[month]=e.Value;
	map[name] = row;
});

//https://stackoverflow.com/questions/11246758/how-to-get-unique-values-in-an-array
var unique= labels.filter(function(itm, i){
    return labels.indexOf(itm)== i; 
    // returns true for only the first instance of itm
});

var datasets=[];

for (var key in map) {
   if (map.hasOwnProperty(key)) {	   
	   var dataset=[];
	   var row=map[key];
	   
	   for(var i=0;i<unique.length;i++){
		var month = unique[i];		 
		  var value=row[month];
		  dataset.push(value);
	   }
	   
	   datasets.push({label: key, data: dataset, backgroundColor: randomColor()});	   
   }
}

var data = {
    labels: unique,
    datasets: datasets
};

$.extend(config,{
	type: 'bar',
	data: data
});
