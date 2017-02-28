//http://bl.ocks.org/markmarkoh/11331459
$.extend(options,{
	scope: 'usa',
	 fills: {
		  'Visited': '#306596',
		  'neato': '#0fa0fa',
		  'Trouble': '#bada55',
		  defaultFill: '#dddddd'
		}
});

$.extend(geographyConfig,{
	popupTemplate: function(geo, data) {
		if (!data) {			
			return;
		}
		
		return "<div class='hoverinfo'><strong>" + geo.properties.name + "</strong><br>" + data.info + "</div>";
	  },
	  highlightOnHover: false
});

var map = new Datamap(options);