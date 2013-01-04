<%@ page import="java.util.ResourceBundle, java.util.List, art.servlets.ArtDBCP,art.params.*" %>
<%@ page import="org.apache.commons.lang.StringUtils" %>

<%@ taglib uri="http://ajaxtags.sourceforge.net/tags/ajaxtags" prefix="ajax"%>
<jsp:useBean id="ue" scope="session" class="art.utils.UserEntity" />
<%@ include file ="header.jsp" %>

<jsp:useBean id="aq" scope="request" class="art.utils.ArtQuery" />
<!--   query_name may be from query_id... -->
<jsp:setProperty name="aq" property="*" />

<%
aq.setUsername(ue.getUsername());
aq.create();
int queryType = aq.getQueryType();
int queryId=aq.getQueryId();
String queryName=aq.getName();
String showParameters=aq.getShowParameters();

String action;
if(queryType==110){
	action="showDashboard.jsp";
} else if(queryType==112 || queryType==113 || queryType==114){
	action="showAnalysis.jsp";
} else {
	action="ExecuteQuery";
}

int accessLevel=ue.getAccessLevel();
boolean hasParams=false;

boolean showResultsInline=true; //can be modified in case show inline behaviour is not desired

%>

<script type="text/javascript" src="<%= request.getContextPath() %>/js/date.js"></script>
<script type="text/javascript" src="<%= request.getContextPath() %>/js/art-checkParam_js.jsp"></script>

<script type="text/javascript" src="<%= request.getContextPath() %>/js/dhtmlgoodies_calendar/dhtmlgoodies_calendar_js.jsp"></script>

<div id="params">

    <fieldset>
        <legend><%=messages.getString("enterParams") %></legend>

        <form name="artparameters" id="paramForm" action="<%=action%>"  method="post">
			<input type="hidden" name="queryId" value="<%=queryId%>">			
			<input type="hidden" name="fromShowParams" value="true">
			
			<%if(showResultsInline){%>
			<input type="hidden" name="_isInline" id="_isInline" VALUE="true">
			<%}%>
			
            <table class="art" align="center">
                <tr>
                    <td colspan="4" class="title" >
                         <b><%=queryName%></b> <br>
                    </td>
                </tr>

                <%
                List<ParamInterface> params = aq.getParamList();
                StringBuffer validateJS = new StringBuffer(128);
                String paramClass;
                String paramName;
                String paramHtmlName;
                String paramChainedId;
                String paramId;
				String paramChainedValueId;
				
                for(ParamInterface param : params) {
                   hasParams=true;
				   param.setMessages(messages); //enable localisation "All" string for lovs
                   paramHtmlName=param.getHtmlName();
                   paramClass=param.getParamClass();
                   paramName=param.getName();
                   paramChainedId=param.getChainedId();
                   paramId=param.getId();
				   paramChainedValueId=param.getChainedValueId();

                   if ( paramClass.equals("INTEGER") || paramClass.equals("NUMBER") || paramClass.equals("DATE") || paramClass.equals("DATETIME") ){
                      validateJS.append("ValidateValue('"+paramClass+"', '"+paramName+"', document.getElementById('"+paramId+"').value ) && ");
					}
                %>
                <tr>
                    <td class="data">
                        <%=paramName%>
                    </td>
                    <td class="data">                        
                        <%
						out.println(param.getValueBox(request.getParameter(param.getHtmlName())));
						
						if (param.isChained()) {
                              String ajaxParams = "";
                              if (paramHtmlName.startsWith("M_")) { // handle ALL_ITEMS in select
                                 ajaxParams = "action=lov,queryId="+paramClass+",isMulti=yes,filter={"+paramChainedValueId+"}";
                              } else {
                                 ajaxParams = "action=lov,queryId="+paramClass+",filter={"+paramChainedValueId+"}";
                              }
                              String dataProviderUrl = request.getContextPath()+"/XmlDataProvider";
                        %>
                        <ajax:select
							baseUrl="<%=dataProviderUrl%>"
							source="<%=paramChainedId%>"
							target="<%=paramId%>"
							parameters="<%=ajaxParams%>"
							preFunction="artAddWork"
							postFunction="artRemoveWork"
							executeOnLoad="true"
							emptyOptionName="..."
							emptyOptionValue=""
                            />

                        <!-- js code to add a "dummy item" on master select -->
                        <script type="text/javascript">
                            addElementToSelectItem("<%=paramChainedId%>");
                        </script>

                        <% } %>

                        <% if (param.isChained()) { %>
                        <img src="<%= request.getContextPath() %>/images/chain.png" alt="chain"><br>
                        <small>
                            <script type="text/javascript"> document.write(document.getElementById("<%=paramChainedId+"_NAME"%>").value) </script>
                        </small>
                        <% } else if (paramClass.equals("INTEGER")) { %>
                        <img src="<%= request.getContextPath() %>/images/123.png" alt="integer">
                        <% } else if (paramClass.equals("NUMBER")) { %>
                        <img src="<%= request.getContextPath() %>/images/1dot23.png" alt="number">
                        <% } else if (paramClass.equals("DATE") || paramClass.equals("DATETIME")) { %>
                        <!-- image included in html box -->
                        <% } else { %>
                        <img src="<%= request.getContextPath() %>/images/abc.png" alt="text">
                        <% } %>
                    </td>
                    <td class="data">
                        <%=param.getShortDescr()%>
                    </td>
                    <td class="data">
                        <input type="button" class="buttonup" onClick="alert('<%=param.getDescr()%>')" onMouseOver="javascript:btndn(this);" onMouseOut="javascript:btnup(this);"  value="...">

                    </td>
                </tr>

                <% } %>
                <tr>
					<%
                        if (queryType == 110 || queryType==112 || queryType==113 || queryType==114) {
							//dashboards and pivot tables don't have view modes
							%>
							<td colspan="2" class="attr">
							<%
							if(queryType==110){ //dashboard
							%>
							<%=messages.getString("dashboardQuery")%>
							<%} else { //pivot table
							%>
								<%=messages.getString("pivotTableQuery")%>
								<%
							}
							%>
						</td>
                        <%
                        } else {
						%>
						<td colspan="2" class="data">
						<%
                           switch ( queryType ) {
                              case 0  :   // normal query
                              case 101:  // crosstab
                        %>    <span style="font-size:95%"><i><%=messages.getString("viewMode")%></i></span>
                        <SELECT name="viewMode" id="viewMode" size="1">
                            <%
                            List<String> viewModes = ArtDBCP.getUserViewModes();
                            for(String viewMode : viewModes){
                            %>
                            <OPTION VALUE="<%=viewMode%>"> <%=messages.getString(viewMode)%> </OPTION>
                            <% } %>

                            <% if (accessLevel>=5 && ArtDBCP.isSchedulingEnabled()) { %>
                            <OPTION VALUE="SCHEDULE"><%=messages.getString("scheduleJob")%></OPTION>
                            <% } %>

                        </SELECT>
                        <br>
                        
                        <%
                        break;
                        
						case 115: //jasper report
                        case 116:
                        %>
                        <span style="font-size:95%"><i><%=messages.getString("viewMode")%></i></span>
                        <SELECT name="viewMode" id="viewMode" size="1">
                            <OPTION VALUE="pdf"><%=messages.getString("pdf")%></OPTION>
                            <OPTION VALUE="xls"><%=messages.getString("xls")%></OPTION>
                            <OPTION VALUE="xlsx"><%=messages.getString("xlsx")%></OPTION>
                            <OPTION VALUE="html"><%=messages.getString("htmlJasper")%></OPTION>
                            <% if (accessLevel>=5 && ArtDBCP.isSchedulingEnabled()) { %>
                            <OPTION VALUE="SCHEDULE"><%=messages.getString("scheduleJob")%></OPTION>
                            <% } %>

                        </SELECT>
							<br>
                        <%
                        break;
					case 117: //jxls spreadsheet
					case 118:
					%>
					<span style="font-size:95%"><i><%=messages.getString("viewMode")%></i></span>
					<SELECT name="viewMode" id="viewMode" size="1">			     		
					<OPTION VALUE="xls"><%=messages.getString("xls")%></OPTION>
					 <% if (ue.getAccessLevel() >=5 && ArtDBCP.isSchedulingEnabled()) { %>
					<OPTION VALUE="SCHEDULE"><%=messages.getString("scheduleJob")%></OPTION>
					 <% } %>
						 </SELECT>
						 <br>
						 <%
						 break;
					  						
					   }//end switch


					   if (queryType<0) {
						   //graph
						 %>
                        <small><i><%=messages.getString("graphType")%></i>
                            <%=messages.getString("graph"+queryType)%>                            
                            <br><i><%=messages.getString("graphSizeWH")%></i>
                            <INPUT TYPE="text" name="_GRAPH_SIZE" VALUE="Default" size="16" maxlength="60">
                            <input type="button" class="buttonup" value="..." onclick='javascript:alert(" <%=messages.getString("graphHelp1")%> <%=messages.getString("graphHelp2")%> <%=messages.getString("graphHelp3")%> <%=messages.getString("graphHelp4")%> ");' onMouseOver="javascript:btndn(this);" onMouseOut="javascript:btnup(this);">
                        </small>

                        <span style="font-size:95%"><i><%=messages.getString("viewMode")%></i></span>
                        <SELECT name="viewMode" id="viewMode" size="1">
                            <OPTION VALUE="GRAPH"><%=messages.getString("htmlPlain")%></OPTION>
                            <OPTION VALUE="PDFGRAPH"><%=messages.getString("pdf")%></OPTION>
                            <OPTION VALUE="PNGGRAPH"><%=messages.getString("png")%></OPTION>
                            <% if (accessLevel>=5 && ArtDBCP.isSchedulingEnabled()) { %>
                            <OPTION VALUE="SCHEDULE"><%=messages.getString("scheduleJob")%></OPTION>
                            <%}%>
                        </SELECT>

                        <br /> <%=messages.getString("showGraphOptions")%>
                        <input type="checkbox" name="_showLegend" <%=(aq.isShowLegend()?"checked":"")%> /><%=messages.getString("legend")%>
                        <input type="checkbox" name="_showLabels" <%=(aq.isShowLabels()?"checked":"")%> /><%=messages.getString("labels")%>
                        <input type="checkbox" name="_showDataPoints" <%=(aq.isShowPoints()?"checked":"")%> /><%=messages.getString("dataPoints")%>
						<input type="checkbox" name="_showGraphData" <%=(aq.isShowGraphData()?"checked":"")%> /><%=messages.getString("graphData")%>

                        <br><br>
                         
							<%
                         }
						   
						   //display show parameters and show sql options. don't show these options for dashboards, pivot tables, jasper template queries, jxls template queries
						   if (!(queryType == 110 || queryType==112 || queryType==113 || queryType==114 || queryType==115 || queryType==117)) {
							   if(hasParams){
                             if("N".equals(showParameters)) { %>
                                <input type="checkbox" name="_showParams"> <%=messages.getString("showParams")%>
                            <%} else if("Y".equals(showParameters)) { %>
                                <input type="checkbox" name="_showParams" checked> <%=messages.getString("showParams")%>
                            <%} else if("A".equals(showParameters)) { %>
                                <input type="hidden" name="_showParams" value="true"> 
                            <%}
                          }  
							   if(accessLevel>=10) {
                        %>
						&nbsp;<input type="checkbox" name="_showSQL"> <%=messages.getString("showSQL")%>
												
                        <%
							}
						}
						} 
                        %>

                    </td>
                    <td colspan="2" class="attr">
                        <div align="center" valign="middle">
                            <input type="submit" name="execute" id="execute" onClick="javascript:return(<%= validateJS.toString()%> returnTrue() )" class="buttonup"  style="width:100px;" value="<%=messages.getString("executeQueryButton")%>">
                        </div>
                    </td>
                </tr>
            </table>
        </form>
    </fieldset>
						
</div>
							
						
<div id="response"></div>


<script type="text/javascript">
	
jQuery(document).ready(function($){
	$("#paramForm").submit(function(e){
		
		var viewMode=document.getElementById("viewMode");
		var selectedViewMode="";

		if(viewMode!=null){
			var selectedViewMode = viewMode.options[viewMode.selectedIndex].value;
		}

		var qt="<%=queryType%>";
		var showInline=<%=showResultsInline%>;

		if(showInline && selectedViewMode!="SCHEDULE" && !(qt==112 || qt==113 || qt==114)){
			//display results inline. don't display inline for scheduling or pivot tables			
			e.preventDefault();

			$form=$(this);
			
			//disable execute button
			$('input[type="submit"]').attr('disabled','disabled');

			$("#response").load("ExecuteQuery",$form.serialize(),function(responseText, statusText, xhr){
				//callback funtion for when jquery load has finished
				
				//check if session expired
				var user=document.getElementById("username");
				if(user!=null){
					//a login page is being displayed. session must have expired. redirect to enable user to login and start again
					window.location="<%= request.getContextPath() %>/user/showGroups.jsp";
					return;
				}

				if(statusText=="success"){
					//make htmlgrid output sortable
					if(selectedViewMode=="htmlGrid"){
						forEach(document.getElementsByTagName('table'), function(table) {
							if (table.className.search(/\bsortable\b/) != -1) {
								sorttable.makeSortable(table);
							}
						});
					}
					
					//ensure tooltips are displayed for charts
					if(qt<0 || qt==110){
						olLoaded=1;
						overlib('');
					}
				} else if(statusText=="error"){
					alert("An error occurred: " + xhr.status + " - " + xhr.statusText);
				}
				
				//enable submit button
				$('input[type="submit"]').removeAttr('disabled');

			});	
			
		}

});  

//display spinner animation when any ajax activity happens
$("#systemWorking").ajaxStart(function(){
    $(this).show();
 }).ajaxStop(function(){
    $(this).hide();
 }).ajaxError(function(){
    $(this).hide();
 });
 
}); 



</script>

<%@ include file ="footer.jsp" %>

