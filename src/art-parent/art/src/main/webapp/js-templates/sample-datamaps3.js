$.extend(options,{
	scope: 'kenya_outside_boundary',
	fills: {
        bubbleFill: '#1f77b4',
		defaultFill: '#ABDDA4'
	},
	  setProjection: function(element) {
    var projection = d3.geo.equirectangular()
      .center([36, 0.0])      
      .scale(1500)
      .translate([element.offsetWidth / 2, element.offsetHeight / 2]);
    var path = d3.geo.path()
      .projection(projection);

    return {path: path, projection: projection};
	  }
});

$.extend(geographyConfig,{
	popupTemplate: function(geo, data) {
		return;
	  },
	  highlightOnHover: false
});

var map = new Datamap(options);

var values=[
	{name: 'Nairobi', latitude: -1.28333, longitude: 36.81667, radius: 5, fillKey: 'bubbleFill', value: 245},	
	{name: 'Mombasa', latitude: -4.05466, longitude: 39.66359, radius: 5, fillKey: 'bubbleFill', value: 639},
	{name: 'Eldoret', latitude: 0.514277, longitude: 35.269779, radius: 5, fillKey: 'bubbleFill', value: 750},
	{name: 'Kisumu', latitude: -0.10221, longitude: 34.76171, radius: 5, fillKey: 'bubbleFill', value: 560}
];

map.bubbles(values, {
    popupTemplate: function (geo, data) {
		return ['<div class="hoverinfo"><strong>' +  data.name + '</strong>',
		'<br/>Value: ' +  data.value + '',           
		'</div>'].join('');
    }
});