$.extend(options,{
	fills: {
        bubbleFill: '#1f77b4',
		defaultFill: '#ABDDA4'
	},
	  setProjection: function(element) {
    var projection = d3.geo.equirectangular()
      .center([14, 43])
      //.rotate([4.4, 0])
      .scale(600)
      .translate([element.offsetWidth / 2, element.offsetHeight / 2]);
    var path = d3.geo.path()
      .projection(projection);

    return {path: path, projection: projection};
	  }
});

$.extend(geographyConfig,{
	popupTemplate: function(geo, data) {
		return "<div class='hoverinfo'><strong>" + geo.properties.name + "</strong></div>";
	  },
	  highlightOnHover: false
});

var map = new Datamap(options);

var values=[
	{name: 'Vilnius', latitude: 54.68916, longitude: 25.2798, radius: 5, fillKey: 'bubbleFill', value: 145},	
	{name: 'Rome', latitude: 41.89193, longitude: 12.51133, radius: 5, fillKey: 'bubbleFill', value: 450}
];

map.bubbles(values, {
    popupTemplate: function (geo, data) {
		return ['<div class="hoverinfo"><strong>' +  data.name + '</strong>',
		'<br/>Value: ' +  data.value + '',           
		'</div>'].join('');
    }
});