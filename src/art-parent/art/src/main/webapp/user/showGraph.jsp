<%@ page import="java.util.*,art.servlets.ArtConfig,art.graph.*,art.utils.*" %>
<%@ page import="java.text.SimpleDateFormat" %>
<%@ page import="org.jfree.chart.*,org.apache.commons.beanutils.*,java.awt.Font" %>
<%@ page import="org.apache.commons.lang.StringUtils" %>
<%@ page import="art.output.ArtOutHandler,java.io.PrintWriter" %>
<%@ page import="org.jfree.chart.renderer.category.*" %>

<%@ taglib uri="http://cewolf.sourceforge.net/taglib/cewolf.tld" prefix="cewolf" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<jsp:useBean id="ue" scope="session" class="art.utils.UserEntity" />
<jsp:useBean id="lineRenderer" class="de.laures.cewolf.cpp.LineRendererProcessor" />
<jsp:useBean id="labelRotation" class="de.laures.cewolf.cpp.RotatedAxisLabels" />
<jsp:useBean id="heatmapPP" class="de.laures.cewolf.cpp.HeatmapEnhancer" />


<%
  request.setCharacterEncoding("UTF-8");
  response.setHeader("Cache-control","no-cache");

  String graphType[] = {"xy","pie3D","horizontalBar3D","verticalBar3D","line"
                        ,"timeseries","timeseries","stackedVerticalBar3D","stackedHorizontalBar3D"
						,"meter","bubble","heatmap","pie","verticalBar","stackedVerticalBar"
						,"horizontalBar","stackedHorizontalBar"};
						  
  //get query type from attribute so that it doesn't have to be specified in direct url 
  Integer queryTypeInteger = (Integer)request.getAttribute("queryType");
  int queryType=queryTypeInteger.intValue();
  int graphId=Math.abs(queryType);
  
  Integer queryId = (Integer) request.getAttribute("queryId");

  String graphElementId = "artGraph_" + queryId + "_" + graphId;

  if ( request.getAttribute("artGraph") == null ) {
    %><html><body>Error: Graphic object has not been created. Check permissions, parameters, database connection or size string.<br>
                  Try to run the query with tabular output to debug it better.</body></html>
    <%
    return;
  }
  
  ArtGraph graph = (ArtGraph) request.getAttribute("artGraph");
  pageContext.setAttribute("graph", graph);
  
  boolean usesHyperlinks=graph.getUseHyperLinks();
  boolean hasTooltips=graph.getHasTooltips();
  
  //add support for drill down queries
  boolean hasDrilldown=graph.getHasDrilldown();
		  
   // y axis data range
  String from = (String) request.getAttribute("_from");
  String to = (String) request.getAttribute("_to");
  
  //x-axis label handling
  String rotateAt = (String) request.getAttribute("_rotate_at"); //cewolf expects a string object
  String removeAt = (String) request.getAttribute("_remove_at"); //cewolf expects a string object
    
  // legend
  boolean showLegend = true;
  if (request.getAttribute("_nolegend") != null){
	showLegend = false;
	}
  
  // data points
   boolean showPoints = false;
  if (request.getAttribute("_showpoints") != null){
	showPoints = true;
	}
   
    //show parameters
   boolean showParams=false;
   if (request.getAttribute("showParams") != null){
	showParams = true;
	}
   
   //show sql
   String finalSQL=(String) request.getAttribute("finalSQL");
   boolean showSQL=false;
   if (request.getAttribute("showSQL") != null){
	showSQL = true;
	}
      
  //determine if chart is to be output to file
  String outputToFile = "nofile";  
  if (request.getAttribute("outputToFile") != null){
	outputToFile=(String) request.getAttribute("outputToFile");
	}
  
	//build output file name   
  String fileName = "nofile";  
    String fullFileName="nofile";
    String baseFileName=(String) request.getAttribute("baseFileName");
	
	if (outputToFile.equals("pdf")){
        fileName=baseFileName + ".pdf";
		fullFileName= ArtConfig.getExportPath() + fileName;
	} else if (outputToFile.equals("png")){
        fileName=baseFileName + ".png";
		fullFileName= ArtConfig.getExportPath() + fileName;
	}
	
// Labels
	boolean showLabels=false;
  String labelFormat;
  final String LABELS_OFF="off";
  final String CATEGORY_LABEL_FORMAT="{2}";
  
  //display pie chart data value in label for png/pdf output
  String pieLabelFormat;  
  if(!StringUtils.equals(outputToFile,"nofile")){
	  //either png or pdf output
	  pieLabelFormat="{0} = {1} ({2})";
  } else {
	  //browser output
	  pieLabelFormat="{0} ({2})";
  }
  
  //set default values. by default labels are shown for pie charts but not for category dataset charts (line and bar)
  if(graphId==2 || graphId==13){
	labelFormat= pieLabelFormat;
} else {
	labelFormat=LABELS_OFF;
}

//set specific label format if modifiers are provided
  if (request.getAttribute("_nolabels") != null){
	labelFormat = LABELS_OFF;
	} 
  
	if (request.getAttribute("_showlabels") != null){
		if(graphId==2 || graphId==13){
			labelFormat= pieLabelFormat;
		} else {
			labelFormat=CATEGORY_LABEL_FORMAT;
		}
		showLabels=true;
	}
	
	//make target configurable, mainly for drill down queries
	String target="_blank"; //default to showing links in new window
	String openInNewWindow=graph.getOpenDrilldownInNewWindow();
	if(hasDrilldown){
		if(StringUtils.equals(openInNewWindow,"Y") || openInNewWindow==null){
			//open in new window
			target="_blank";
		} else {
			//open in same window
			target="";			
		}
	}

    
  boolean isFragment = (request.getParameter("_isFragment")!= null?true:false);  // the html code will be rendered as an html fragmnet (without <html> and </html> tags)
  boolean isPlain    = (request.getParameter("_isPlain")!= null?true:false);
  boolean isInline    = (request.getParameter("_isInline")!= null?true:false);
  if (!isFragment && !isPlain && !isInline) {
%>
    <%@ include file ="header.jsp" %>
<%
  } else if (isPlain) {
	  out.println("<html>");
  }

/*
 this is to show this page just after the user clicked on the submit button
  - it avoids an issue with IE that stops the spinning icon when the mouse
  is moved out of the button area
 */

out.flush(); 

//reset jfreechat theme to the default theme ("jfree"). jasper reports sets it to the legacy theme and this affects the speedometer chart
StandardChartTheme chartTheme = (StandardChartTheme) StandardChartTheme.createJFreeTheme(); 
chartTheme.setBarPainter(new StandardBarPainter()); //remove white line/glossy effect on 2D bar graphs with the jfree theme

//also allow use of custom font to enable display of non-ascii characters
if(ArtConfig.isUseCustomPdfFont()){
	String pdfFontName = ArtConfig.getArtSetting("pdf_font_name");
	Font oldExtraLargeFont = chartTheme.getExtraLargeFont();
	Font oldLargeFont = chartTheme.getLargeFont();
	Font oldRegularFont = chartTheme.getRegularFont();

	Font extraLargeFont = new Font(pdfFontName, oldExtraLargeFont.getStyle(), oldExtraLargeFont.getSize());
	Font largeFont = new Font(pdfFontName, oldLargeFont.getStyle(), oldLargeFont.getSize());
	Font regularFont = new Font(pdfFontName, oldRegularFont.getStyle(), oldRegularFont.getSize());

	chartTheme.setExtraLargeFont(extraLargeFont);
	chartTheme.setLargeFont(largeFont);
	chartTheme.setRegularFont(regularFont);
}
ChartFactory.setChartTheme(chartTheme);
				
//display parameters
PrintWriter htmlout=response.getWriter();

if(showParams){
	ResourceBundle msgs = ResourceBundle.getBundle("i18n.ArtMessages", request.getLocale());
	ArtOutHandler.displayParameters(htmlout, graph.getDisplayParameters(), msgs);
}

//display final sql
if (showSQL) {
	ArtOutHandler.displayFinalSQL(htmlout, finalSQL);
}

%>

<p>
<table class="plain centerTable" style="width: 60%">
 <tr>
  <td align="center"> 
   
   <cewolf:chart 
       id="<%=graphElementId%>" 
       title="<%=graph.getTitle()%>" 
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
	   
	   <cewolf:chartpostprocessor id="lineRenderer">
			<cewolf:param name="shapes" value="<%= String.valueOf(showPoints)%>" />
		</cewolf:chartpostprocessor> 
	   
	   <% if(queryType==-12){
		   ArtXYZChart heatmap=(ArtXYZChart)graph;
		   Map<String,String> options=heatmap.getHeatmapOptions();
		   %>
	   <cewolf:chartpostprocessor id="heatmapPP">
        <cewolf:param name="xLabel" value="<%=graph.getXAxisLabel()%>"/>
        <cewolf:param name="yLabel" value="<%=graph.getYAxisLabel()%>"/>
		<cewolf:param name="showItemLabels" value="<%= String.valueOf(showLabels)%>"/>
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
		  <cewolf:param name="showLegend" value="<%= String.valueOf(showLegend) %>" />	  		  
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
 

  <% if (outputToFile.equals("pdf") || outputToFile.equals("png")) {  %>
   <p>
    <div align="center">
     <table style="width:90%">
      <tr>
       <td colspan="2" class="data" align="center" >
        <a type="application/octet-stream" href="../export/<%=fileName%>" target="_blank"><%=fileName%></a>
       </td>
      </tr>
     </table>
    </div>
   </p>

  <% }  %>
  
<p>
<br>
<div align="center">
<table style="width:90%">
  
  <%	
	if(graph.isShowGraphData()){
		RowSetDynaClass graphData=graph.getGraphData();
		request.setAttribute("graphData", graphData);
		%>
		<c:forEach var="row" items="${graphData.rows}" varStatus="status">
			<% DynaBean tempRow = (DynaBean) pageContext.getAttribute("row"); %>
			<c:if test="${status.first}">
				<%
				DynaProperty[] graphDataColumns = tempRow.getDynaClass().getDynaProperties();
				pageContext.setAttribute("graphDataColumns", graphDataColumns);
				%>
				<tr>
					<c:forEach var="col" items="${graphDataColumns}">
						<td class="graphheader">${col.name}</td>
					</c:forEach>
				</tr>
			</c:if>
				
			<tr>
				<c:forEach var="col" items="${graphDataColumns}">
					<%
					DynaProperty c = (DynaProperty) pageContext.getAttribute("col");
					String columnName = c.getName();
					%>
					<td class="graphdata"><%= String.valueOf(tempRow.get(columnName)) %></td>
				</c:forEach>
			</tr>
		</c:forEach>
		
	<%
	}
	%>
	
	</table>
	</div>
</p>


  </td>
  </tr>
</table>
</p>


<!-- Test Memory Leak: removing object from request. -->
<% // Remove object from request 
 request.removeAttribute("artGraph");
 graph = null;
 
 if (!isFragment && !isPlain && !isInline) {    
    %><%@ include file ="footer.jsp" %><%
  } else if (isPlain) {
    %></html><%
  } 
%>
