<%-- 
    Document   : showDatamaps
    Created on : 24-Feb-2017, 12:12:59
    Author     : Timothy Anyona
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page trimDirectiveWhitespaces="true" %>

<div id="container" style="position: relative; width: 500px; height: 300px; margin: 0 auto;">
	
</div>

<script type="text/javascript" src="${pageContext.request.contextPath}/js/d3-3.5.17/d3.min.js"></script>
<script type="text/javascript" src="${pageContext.request.contextPath}/js/topojson-1.6.27/topojson.min.js"></script>
<!--<script type="text/javascript" src="${pageContext.request.contextPath}/js/topojson-2.2.0/topojson.min.js"></script>-->
<script type="text/javascript" src="${pageContext.request.contextPath}/js/datamaps-0.5.8/datamaps.ken.js"></script>
<!--<script type="text/javascript" src="${pageContext.request.contextPath}/js/datamaps-0.5.8/datamaps.none.js"></script>-->
<!--<script type="text/javascript" src="${pageContext.request.contextPath}/js/datamaps-0.5.8/datamaps.world.min.js"></script>-->

<script>
	//https://blog.basilesimon.fr/2014/04/24/draw-simple-maps-with-no-effort-with-d3-js-and-datamaps-js/
	//https://stackoverflow.com/questions/41482906/states-not-highlighting
	//https://stackoverflow.com/questions/40640634/creating-heatmap-of-states-in-brazil
	//https://github.com/d3/d3-3.x-api-reference/blob/master/Geo-Projections.md
	//https://en.wikipedia.org/wiki/ISO_3166-1_alpha-3
	//https://github.com/markmarkoh/datamaps
	
	  var width = document.getElementById('container').offsetWidth;
  var height = document.getElementById('container').offsetHeight;
  
  var url="${pageContext.request.contextPath}/js-templates/county.json";
  
    var map = new Datamap({
		element: document.getElementById('container'),
		scope: 'County',
		geographyConfig: {
      dataUrl: url,
	  popupTemplate: function(geo, data) {
                return ['<div class="hoverinfo"><strong>',
                        geo.properties.COUNTY,
                        '</strong></div>'].join('');
            }
    },
	  setProjection: function(element) {
        var projection = d3.geo.equirectangular()
            .center([36, -1])
            .scale(1000)
            .translate([element.offsetWidth / 2, element.offsetHeight / 2]);
        var path = d3.geo.path()
            .projection(projection);

        return {
            path: path,
            projection: projection
        };
    }

	});
</script>
