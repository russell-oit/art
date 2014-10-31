<%-- 
    Document   : showChart
    Created on : 30-Oct-2014, 15:58:59
    Author     : Timothy Anyona

Display a chart report
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page trimDirectiveWhitespaces="true" %>

<%@taglib tagdir="/WEB-INF/tags" prefix="t" %>
<%@taglib uri="http://cewolf.sourceforge.net/taglib/cewolf.tld" prefix="cewolf" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@taglib uri="https://www.owasp.org/index.php/OWASP_Java_Encoder_Project" prefix="encode" %>

<p>
<table class="plain" align="center" border="0" width="60%">
 <tr>
  <td align="center"> 
   
   <cewolf:chart 
       id="${htmlElementId}" 
       title="${chart.title}" 
       type="<%=graphType[graphId-1]%>" 
       showlegend="<%=showLegend%>"
	   plotbackgroundcolor="#FFFFFF"
       xaxislabel="<%=graph.getXAxisLabel()%>" 
       yaxislabel="<%=graph.getYAxisLabel()%>">
       <cewolf:colorpaint color="<%=graph.getBgColor()%>"/>
	   
       <cewolf:data>
           <cewolf:producer id="graph" />
       </cewolf:data>
	   
		<cewolf:chartpostprocessor id="labelRotation">
			<cewolf:param name="rotate_at" value="<%=rotateAt%>" />
			<cewolf:param name="remove_at" value="<%=removeAt%>" />
		</cewolf:chartpostprocessor> 
	   
	   <% if(queryType==-12){
		   ArtXYZChart heatmap=(ArtXYZChart)graph;
		   Map<String,String> options=heatmap.getHeatmapOptions();
		   %>
	   <cewolf:chartpostprocessor id="heatmapPP">
        <cewolf:param name="xLabel" value="<%=graph.getXAxisLabel()%>"/>
        <cewolf:param name="yLabel" value="<%=graph.getYAxisLabel()%>"/>
		<cewolf:param name="showItemLabels" value="<%=showLabels%>"/>
		%>
		<%
		for(Map.Entry<String,String> entry: options.entrySet()){
			String option=entry.getKey();
			String value=entry.getValue();
			%>
			<cewolf:param name="<%=option%>" value="<%=value%>"/>
			<%
		}
        %>
    </cewolf:chartpostprocessor>
	   
	   <%} %>
	   	    	
	   
      <cewolf:chartpostprocessor id="graph">
          <cewolf:param name="from" value="<%= from %>"/>
          <cewolf:param name="to" value="<%= to %>"/>
          <cewolf:param name="labelFormat" value="<%= labelFormat %>" />
		  <cewolf:param name="outputToFile" value="<%= outputToFile %>" />
          <cewolf:param name="fullFileName" value="<%= fullFileName %>" />          		  		  
		  <cewolf:param name="showLegend" value="<%= showLegend %>" />	  		  
		  <cewolf:param name="showPoints" value="<%=showPoints%>" />
      </cewolf:chartpostprocessor>
	   
   </cewolf:chart>
   
   <cewolf:img chartid="<%=graphElementId%>" 
               renderer="/cewolf" 
	       width="<%=graph.getWidth()%>" 
	       height="<%=graph.getHeight()%>"
			removeAfterRender="true"
		   >
	   	
	<% if(usesHyperlinks || hasDrilldown) {  %>
         <cewolf:map linkgeneratorid="graph" target="<%=target%>" tooltipgeneratorid="graph"/> 
      <%} else if(hasTooltips) {%>
         <cewolf:map tooltipgeneratorid="graph"/> 
      <%} %>
   </cewolf:img>
