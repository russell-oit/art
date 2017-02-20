var finalData = [];
var values = [];

//https://stackoverflow.com/questions/14379274/javascript-iterate-object
//https://stackoverflow.com/questions/4246980/how-to-create-a-simple-map-using-javascript-jquery
//https://stackoverflow.com/questions/6298169/how-to-create-a-hash-or-dictionary-object-in-javascript?noredirect=1&lq=1
var map={};

jsonData.forEach(function(e) {
	values.push(e["Name"]);
	
	var month=e.Month;
	var row=map[month];
	if(row == undefined){
		row={};
	}
	row["Month"]=month;
	row[e["Name"]] = e.Value;
	map[month] = row;
});

for (var key in map) {
   if (map.hasOwnProperty(key)) {
      finalData.push(map[key]);
   }
}

//https://stackoverflow.com/questions/11246758/how-to-get-unique-values-in-an-array
var unique= values.filter(function(itm, i){
    return values.indexOf(itm)== i; 
    // returns true for only the first instance of itm
});

var c3data = {
	json: finalData,
	keys: {
		x: "Month",
		value: unique
	},
	type: 'bar'
};

$.extend(options, {
	data: c3data,
	axis: {
		x: {
			type: 'category'
		}
	}
});