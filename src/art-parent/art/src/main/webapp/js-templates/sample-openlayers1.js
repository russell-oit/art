  var iconFeature = new ol.Feature({
	geometry: new ol.geom.Point(ol.proj.transform([15.9833, 45.8167], 'EPSG:4326', 'EPSG:3857')),
	name: 'Zagreb',	
	volume: 45
  });
  var iconStyle = new ol.style.Style({
	image: new ol.style.Icon(/** @type {olx.style.IconOptions} */ ({
	  anchor: [0.5, 46],
	  anchorXUnits: 'fraction',
	  anchorYUnits: 'pixels',
	  opacity: 0.75,
	  src: markerUrl
	}))
  });
  iconFeature.setStyle(iconStyle);

  var vectorSource = new ol.source.Vector({
	features: [iconFeature]
  });

  var vectorLayer = new ol.layer.Vector({
	source: vectorSource
  });

  var map = new ol.Map({
	target: document.getElementById(mapId),
	layers: [
	  new ol.layer.Tile({
		source: new ol.source.OSM()
	  }),
	  vectorLayer
	],
	controls: ol.control.defaults(),
	view: new ol.View({
	  center: ol.proj.transform([15.9833, 45.8167], 'EPSG:4326', 'EPSG:3857'),
	  zoom: 10
	})
  });
  
  var element = $('<div id="popup"></div>');
      var popup = new ol.Overlay({
        element: element[0],
        positioning: 'bottom-center',
        stopEvent: false
      });
      map.addOverlay(popup);

      map.on('click', function(evt) {
        var feature = map.forEachFeatureAtPixel(evt.pixel,
            function(feature, layer) {
              return feature;
            });
        if (feature) {
          var geometry = feature.getGeometry();
          var coord = geometry.getCoordinates();
          popup.setPosition(coord);
          $(element).popover({
            'placement': 'top',
            'html': true,
            'content': feature.get('name') + '<br>Volume: ' + feature.get('volume')
          });
          $(element).popover('show');
        } else {
          $(element).popover('destroy');
        }
      });

      map.on('pointermove', function(e) {
         if (e.dragging) {
          $(element).popover('destroy');
          return;
        }
        var pixel = map.getEventPixel(e.originalEvent);
        var hit = map.hasFeatureAtPixel(pixel);
        map.getTarget().style.cursor = hit ? 'pointer' : '';
      });
