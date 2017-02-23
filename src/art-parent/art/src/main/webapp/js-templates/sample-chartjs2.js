//https://stackoverflow.com/questions/10014271/generate-random-color-distinguishable-to-humans?noredirect=1&lq=1
//https://stackoverflow.com/questions/2328339/how-to-generate-n-different-colors-for-any-natural-number-n
//https://stackoverflow.com/questions/309149/generate-distinctly-different-rgb-colors-in-graphs
//http://colorbrewer2.org/

var colorIndex=-1;
var colors =[
		"FF0000", "00FF00", "0000FF", "FF00FF", "00FFFF", 
        "800000", "008000", "000080", "808000", "800080", "008080", "808080", 
        "C00000", "00C000", "0000C0", "C0C000", "C000C0", "00C0C0", "C0C0C0", 
        "400000", "004000", "000040", "404000", "400040", "004040", "404040", 
        "200000", "002000", "000020", "202000", "200020", "002020", "202020", 
        "600000", "006000", "000060", "606000", "600060", "006060", "606060", 
        "A00000", "00A000", "0000A0", "A0A000", "A000A0", "00A0A0", "A0A0A0", 
        "E00000", "00E000", "0000E0", "E0E000", "E000E0", "00E0E0", "E0E0E0"
		]
		
var colors=['#9e0142','#d53e4f','#f46d43','#fdae61','#fee08b','#ffffbf','#e6f598','#abdda4','#66c2a5','#3288bd','#5e4fa2'];
//var colors=['#8dd3c7','#ffffb3','#bebada','#fb8072','#80b1d3','#fdb462','#b3de69','#fccde5','#d9d9d9','#bc80bd','#ccebc5'];
		
function getColor(){
	colorIndex++;
	if(colorIndex>=colors.length-1){
		colorIndex=0;
	}
	//return '#' + colors[colorIndex];
	return colors[colorIndex];
}
		
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
	   
	   datasets.push({label: key, data: dataset, backgroundColor: getColor()});
	   //datasets.push({label: key, data: dataset, backgroundColor: randomColor()});
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
