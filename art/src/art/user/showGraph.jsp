<%@page import="java.util.*,art.servlets.ArtDBCP,art.graph.*,java.text.SimpleDateFormat,art.utils.*" %>
<%@page import="org.jfree.chart.*,org.apache.commons.beanutils.*,org.apache.commons.lang.StringUtils" %>
<%@page import="java.awt.Font" %>
<%@taglib uri='/WEB-INF/cewolf.tld' prefix='cewolf' %>

<jsp:useBean id="ue" scope="session" class="art.utils.UserEntity" />
<jsp:useBean id="lineRenderer" class="de.laures.cewolf.cpp.LineRendererProcessor" />
<jsp:useBean id="labelRotation" class="de.laures.cewolf.cpp.RotatedAxisLabels" />


<%
  request.setCharacterEncoding("UTF-8");
  response.setHeader("Cache-control","no-cache");

  String graphType[] = {"xy","pie3d","horizontalBar3D","verticalBar3D","line"
                        ,"timeseries","timeseries","stackedVerticalBar3d","stackedHorizontalBar3d","meter"};
						  
  //get query type from attribute so that it doesn't have to be specified in direct url 
  Integer graphIdInteger = (Integer)request.getAttribute("queryType");
  int graphId=graphIdInteger.intValue();
  graphId=Math.abs(graphId);
  
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
  Integer from = (Integer) request.getAttribute("_from");
  Integer to = (Integer) request.getAttribute("_to");
    
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
		fullFileName= ArtDBCP.getExportPath() + fileName;
	} else if (outputToFile.equals("png")){
        fileName=baseFileName + ".png";
		fullFileName= ArtDBCP.getExportPath() + fileName;
	}
	
	  // Labels
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
  if(graphId==2){
	labelFormat= pieLabelFormat;
} else {
	labelFormat=LABELS_OFF;
}

//set specific label format if modifiers are provided
  if (request.getAttribute("_nolabels") != null){
	labelFormat = LABELS_OFF;
	} 
	if (request.getAttribute("_showlabels") != null){
		if(graphId==2){
			labelFormat= pieLabelFormat;
		} else {
			labelFormat=CATEGORY_LABEL_FORMAT;
		}
	}
	
	//make target configurable, mainly for drill down queries
	String target="_blank"; //default to showing links in new window
	String openInNewWindow=graph.getOpenDrilldownInNewWindow();
	if(hasDrilldown){
		if("Y".equals(openInNewWindow) || openInNewWindow==null){
			//open in new window
			target="_blank";
		} else {
			//open in same window
			target="";			
		}
	}
	
    
  boolean isFragment = (request.getParameter("_isFragment")!= null?true:false);  // the html code will be rendered as an html fragmnet (without <html> and </html> tags)
  boolean isPlain    = (request.getParameter("_isPlain")!= null?true:false);
  if (!isFragment && !isPlain) {
%>
    <%@ include file ="header.jsp" %>
<%
  } else if (isPlain) { out.println("<html>"); }

/*
 this is to show this page just after the user clicked on the submit button
  - it avoids an issue with IE that stops the spinning icon when the mouse
  is moved out of the button area
 */

out.flush(); 

//reset jfreechat theme to the default theme. jasper reports sets it to the legacy theme and this affects the speedometer chart
//also allow use of custom font to enable display of non-ascii characters
StandardChartTheme chartTheme = (StandardChartTheme) StandardChartTheme.createJFreeTheme(); 
if(ArtDBCP.isUseCustomPdfFont()){
	String pdfFontName = ArtDBCP.getArtSetting("pdf_font_name");
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

				
//enable show parameters for graphs
Map<Integer,ArtQueryParam> displayParams=graph.getDisplayParameters();
if(displayParams!=null && displayParams.size()>0){
    out.println("<div align=\"center\">");
    out.println("<table border=\"0\" width=\"90%\"><tr><td>");
	out.println("<div id=\"param_div\" width=\"90%\" align=\"center\" class=\"qeparams\">");
	// decode the parameters handling multi ones
	Iterator it = displayParams.entrySet().iterator();
	while (it.hasNext()) {
		Map.Entry entry = (Map.Entry) it.next();                
		ArtQueryParam param=(ArtQueryParam)entry.getValue();
		String paramName=param.getName();
		Object pValue = param.getParamValue();
		String outputString;


		if (pValue instanceof String) {
			String paramValue = (String) pValue;
			outputString = paramName + ": " + paramValue + " <br> "; //default to displaying parameter value

			if (param.usesLov()) {
				//for lov parameters, show both parameter value and display string if any
				Map<String, String> lov = param.getLovValues();
				if (lov != null) {
					//get friendly/display string for this value
					String paramDisplayString = lov.get(paramValue);
					if (!StringUtils.equals(paramValue, paramDisplayString)) {
						//parameter value and display string differ. show both
						outputString = paramName + ": " + paramDisplayString + " (" + paramValue + ") <br> ";
					}
				}
			}
			out.println(outputString);
		} else if (pValue instanceof String[]) { // multi
			String[] paramValues = (String[]) pValue;
			outputString = paramName + ": " + StringUtils.join(paramValues, ", ") + " <br> "; //default to showing parameter values only

			if (param.usesLov()) {
				//for lov parameters, show both parameter value and display string if any
				Map<String, String> lov = param.getLovValues();
				if (lov != null) {
					//get friendly/display string for all the parameter values
					String[] paramDisplayStrings = new String[paramValues.length];
					for (int i = 0; i < paramValues.length; i++) {
						String value = paramValues[i];
						String display = lov.get(value);
						if (!StringUtils.equals(display, value)) {
							//parameter value and display string differ. show both
							paramDisplayStrings[i] = display + " (" + value + ")";
						} else {
							paramDisplayStrings[i] = value;
						}
					}
					outputString = paramName + ": " + StringUtils.join(paramDisplayStrings, ", ") + " <br> ";
				}
			}
			out.println(outputString);
		}
	}                      
	out.println("</div>");
	out.println("</td></tr></table>");
    out.println("</div>");        
}

//set values for axis label rotate and remove options. use variables to prevent error with java 1.7
String rotateAt="5";
String removeAt="10000";

%>

<p>
<table class="plain" align="center" border="0" width="60%">
 <tr>
  <td align="center"> 
   
   <cewolf:chart 
       id="<%=graphElementId%>" 
       title="<%=graph.getTitle()%>" 
       type="<%=graphType[graphId-1]%>" 
       showlegend="<%=showLegend%>"
	   plotbackgroundcolor="#FFFFFF"
       xaxislabel="<%=graph.getXlabel()%>" 
       yaxislabel="<%=graph.getYlabel()%>">
       <cewolf:colorpaint color="<%=graph.getBgColor()%>"/>
	   
       <cewolf:data>
           <cewolf:producer id="graph" />
       </cewolf:data>
	   	    	
      <cewolf:chartpostprocessor id="graph">
          <cewolf:param name="from" value="<%= from %>"/>
          <cewolf:param name="to" value="<%= to %>"/>
          <cewolf:param name="labelFormat" value="<%= labelFormat %>" />
		  <cewolf:param name="outputToFile" value="<%= outputToFile %>" />
          <cewolf:param name="fullFileName" value="<%= fullFileName %>" />          		  		  
		  <cewolf:param name="showLegend" value="<%= showLegend %>" />	  		  
		  <cewolf:param name="showPoints" value="<%=showPoints%>" />
      </cewolf:chartpostprocessor>
       
       <cewolf:chartpostprocessor id="labelRotation">
		<cewolf:param name="rotate_at" value="<%=rotateAt%>" />
		<cewolf:param name="remove_at" value="<%=removeAt%>" />
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
     <table border="0" width="90%">
      <tr>
       <td colspan="2" class="data" align="center" >
        <a type="application/octet-stream" href="../export/<%=fileName%>" target="_blank"><%=fileName%></a>
       </td>
      </tr>
     </table>
    </div>
   </p>

  <% }  %>
  
  <%	
	if(graph.isShowGraphData()){
		RowSetDynaClass graphData=graph.getGraphData();
		if(graphData!=null){
			List rows=graphData.getRows();
			DynaProperty[] dynaProperties = null;
			String columnName;
			String columnValue;
			%>
			<p>
			<br>
			<div align="center">
			<table border="0" width="90%">	
			<%
			for(int i=0;i<rows.size();i++){
				DynaBean row=(DynaBean)rows.get(i);
				if (i==0) {
					%>
					<tr>
					<%
					dynaProperties = row.getDynaClass().getDynaProperties();
					for (int j=0;j<dynaProperties.length;j++) {
						columnName=dynaProperties[j].getName();
						%>
						<td class="graphheader"><%=columnName%></td>
					<%
					}
					%>
					</tr>
					<%
				}
				%>
				<tr>
				<%
				for (int k=0;k<dynaProperties.length;k++) {
					columnName=dynaProperties[k].getName();
					columnValue=String.valueOf(row.get(columnName));
					%>
					<td class="graphdata"><%=columnValue%></td>
				<%
				}
				%>
				</tr>
				<%
			}
		}
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
 
 if (!isFragment && ! isPlain) {    
    %><%@ include file ="footer.jsp" %><%
  } else if (isPlain) {
    %></html><%
  } 
%>
