<%@ page import="java.sql.*,java.util.*,art.utils.*,art.servlets.ArtDBCP" %>
<%@ include file="headerAdmin.jsp" %>


<%

boolean MODIFY = request.getParameter("QUERYACTION").equals("MODIFY");
int queryId = -1;
if (MODIFY && request.getParameter("QUERY_ID") != null) {
	queryId = Integer.parseInt(request.getParameter("QUERY_ID"));
}

String currentStringValue ="";
int    currentIntValue = -1;

Connection conn = (Connection) session.getAttribute("SessionConn");
if ( conn == null || conn.isClosed()) {
%>
<jsp:forward page="error.jsp">
    <jsp:param name="MOD" value="Update Query"/>
    <jsp:param name="ACT" value="Get Ccnnection from session"/>
    <jsp:param name="MSG" value="Database connection not valid. Please log in again"/>
    <jsp:param name="NUM" value="100"/>
</jsp:forward>
<%
}


ArtQuery aq = new ArtQuery();

if (MODIFY) {
	aq.create(conn, queryId);
}

String helpText;

int accessLevel = ((Integer) session.getAttribute("AdminLevel")).intValue();
String username=(String) session.getAttribute("AdminUsername");

%>

<script language="javascript" type="text/javascript" src="<%= request.getContextPath() %>/js/tiny_mce/tiny_mce.js"></script>
<script language="javascript" type="text/javascript">
tinyMCE.init({
	   mode : "exact",
	   elements : "textSource",
	   theme : "advanced",
		   theme_advanced_buttons1 : "bold,italic,underline,separator,justifyleft,justifycenter,justifyright,justifyfull,separator,bullist,numlist,undo,redo,fontselect,fontsizeselect,forecolor,link,hr,code",
	   theme_advanced_buttons2 : "",
	   theme_advanced_buttons3 : "",
	   theme_advanced_toolbar_location : "top" });
</script>

<script language="Javascript">
	<!-- Begin
	function onTypeSelection() {
		i = document.getElementById("typeId").value;
		if (i == 110 || i == 111) {
            //dashboard or text query
			document.getElementById("querydiv").className="collapse";
			document.getElementById("portletdiv").className="expand";

			document.getElementById("sqlcode").className="collapse";
			document.getElementById("mdxcode").className="collapse";

			if(i==110){
                //dashboard
				document.getElementById("xmlcode").className="expand";
				document.getElementById("textcode").className="collapse";
			} else {
                //text query
				document.getElementById("xmlcode").className="collapse";
				document.getElementById("textcode").className="expand";
			}

			// disable some fields
			document.getElementById("usesRules").className="collapse";
			document.getElementById("dbId").className="collapse";
            
			document.getElementById("div_template1").className="collapse";
			document.getElementById("div_template2").className="collapse";
			document.getElementById("div_xmla_url1").className="collapse";
			document.getElementById("div_xmla_url2").className="collapse";
			document.getElementById("div_xmla_datasource1").className="collapse";
			document.getElementById("div_xmla_datasource2").className="collapse";
			document.getElementById("div_xmla_catalog1").className="collapse";
			document.getElementById("div_xmla_catalog2").className="collapse";
			document.getElementById("div_xmla_username1").className="collapse";
			document.getElementById("div_xmla_username2").className="collapse";
			document.getElementById("div_xmla_password1").className="collapse";
			document.getElementById("div_xmla_password2").className="collapse";

			document.getElementById("div_xaxis1").className="collapse";
			document.getElementById("div_xaxis2").className="collapse";
			document.getElementById("div_yaxis1").className="collapse";
			document.getElementById("div_yaxis2").className="collapse";
			document.getElementById("div_graph_options1").className="collapse";
			document.getElementById("div_graph_options2").className="collapse";
            
            document.getElementById("showParameters").className="collapse";
			document.getElementById("displayResultset").className="collapse";

		} else if (i==112) {
			//olap - mondrian
			document.getElementById("portletdiv").className="collapse";
			document.getElementById("querydiv").className="expand";

			document.getElementById("sqlcode").className="collapse";
			document.getElementById("xmlcode").className="collapse";
			document.getElementById("textcode").className="collapse";
			document.getElementById("mdxcode").className="expand";

			document.getElementById("usesRules").className="expand";
			document.getElementById("dbId").className="expand";

			document.getElementById("div_template1").className="expand";
			document.getElementById("div_template2").className="expand";
			document.getElementById("div_xmla_url1").className="collapse";
			document.getElementById("div_xmla_url2").className="collapse";
			document.getElementById("div_xmla_datasource1").className="collapse";
			document.getElementById("div_xmla_datasource2").className="collapse";
			document.getElementById("div_xmla_catalog1").className="collapse";
			document.getElementById("div_xmla_catalog2").className="collapse";
			document.getElementById("div_xmla_username1").className="collapse";
			document.getElementById("div_xmla_username2").className="collapse";
			document.getElementById("div_xmla_password1").className="collapse";
			document.getElementById("div_xmla_password2").className="collapse";

			document.getElementById("div_xaxis1").className="collapse";
			document.getElementById("div_xaxis2").className="collapse";
			document.getElementById("div_yaxis1").className="collapse";
			document.getElementById("div_yaxis2").className="collapse";
			document.getElementById("div_graph_options1").className="collapse";
			document.getElementById("div_graph_options2").className="collapse";
            
            document.getElementById("showParameters").className="collapse";
			document.getElementById("displayResultset").className="collapse";

		} else if (i==113 || i==114) {
			//olap - mondrian via xmla or ssas via xmla
			document.getElementById("portletdiv").className="collapse";
			document.getElementById("querydiv").className="expand";

			document.getElementById("sqlcode").className="collapse";
			document.getElementById("xmlcode").className="collapse";
			document.getElementById("textcode").className="collapse";
			document.getElementById("mdxcode").className="expand";

			document.getElementById("usesRules").className="expand";
			document.getElementById("dbId").className="collapse";

			document.getElementById("div_template1").className="collapse";
			document.getElementById("div_template2").className="collapse";
			document.getElementById("div_xmla_url1").className="expand";
			document.getElementById("div_xmla_url2").className="expand";

			//datasource name only configurable for mondrian xmla. for ssas, it's hardcoded as provider=msolap
			if(i==113){
                //mondrian via xmla
				document.getElementById("div_xmla_datasource1").className="expand";
				document.getElementById("div_xmla_datasource2").className="expand";
			} else {
                //sql server analysis services via xmla
				document.getElementById("div_xmla_datasource1").className="collapse";
				document.getElementById("div_xmla_datasource2").className="collapse";
			}

			document.getElementById("div_xmla_catalog1").className="expand";
			document.getElementById("div_xmla_catalog2").className="expand";
			document.getElementById("div_xmla_username1").className="expand";
			document.getElementById("div_xmla_username2").className="expand";
			document.getElementById("div_xmla_password1").className="expand";
			document.getElementById("div_xmla_password2").className="expand";

			document.getElementById("div_xaxis1").className="collapse";
			document.getElementById("div_xaxis2").className="collapse";
			document.getElementById("div_yaxis1").className="collapse";
			document.getElementById("div_yaxis2").className="collapse";
			document.getElementById("div_graph_options1").className="collapse";
			document.getElementById("div_graph_options2").className="collapse";
            
            document.getElementById("showParameters").className="collapse";
			document.getElementById("displayResultset").className="collapse";

		} else if (i==115 || i==116 || i==117 || i==118) {
			//jasper report or jxls template
			document.getElementById("portletdiv").className="collapse";
			document.getElementById("querydiv").className="expand";

			document.getElementById("sqlcode").className="expand";
			document.getElementById("xmlcode").className="collapse";
			document.getElementById("textcode").className="collapse";
			document.getElementById("mdxcode").className="collapse";

			if(i==115 || i==117){
				//template queries can't use rules
				document.getElementById("usesRules").className="collapse";
				document.getElementById("displayResultset").className="collapse";
			} else {
				document.getElementById("usesRules").className="expand";
				document.getElementById("displayResultset").className="expand";
			}
			document.getElementById("dbId").className="expand";

			document.getElementById("div_template1").className="expand";
			document.getElementById("div_template2").className="expand";
			document.getElementById("div_xmla_url1").className="collapse";
			document.getElementById("div_xmla_url2").className="collapse";
			document.getElementById("div_xmla_datasource1").className="collapse";
			document.getElementById("div_xmla_datasource2").className="collapse";
			document.getElementById("div_xmla_catalog1").className="collapse";
			document.getElementById("div_xmla_catalog2").className="collapse";
			document.getElementById("div_xmla_username1").className="collapse";
			document.getElementById("div_xmla_username2").className="collapse";
			document.getElementById("div_xmla_password1").className="collapse";
			document.getElementById("div_xmla_password2").className="collapse";

			document.getElementById("div_xaxis1").className="collapse";
			document.getElementById("div_xaxis2").className="collapse";
			document.getElementById("div_yaxis1").className="collapse";
			document.getElementById("div_yaxis2").className="collapse";
			document.getElementById("div_graph_options1").className="collapse";
			document.getElementById("div_graph_options2").className="collapse";
            
            document.getElementById("showParameters").className="collapse";
              
		} else {
			document.getElementById("portletdiv").className="collapse";
			document.getElementById("querydiv").className="expand";

			document.getElementById("sqlcode").className="expand";
			document.getElementById("xmlcode").className="collapse";
			document.getElementById("textcode").className="collapse";
			document.getElementById("mdxcode").className="collapse";

			// enable target db and rules
			document.getElementById("usesRules").className="expand";
			document.getElementById("dbId").className="expand";

			document.getElementById("div_template1").className="collapse";
			document.getElementById("div_template2").className="collapse";
			document.getElementById("div_xmla_url1").className="collapse";
			document.getElementById("div_xmla_url2").className="collapse";
			document.getElementById("div_xmla_datasource1").className="collapse";
			document.getElementById("div_xmla_datasource2").className="collapse";
			document.getElementById("div_xmla_catalog1").className="collapse";
			document.getElementById("div_xmla_catalog2").className="collapse";
			document.getElementById("div_xmla_username1").className="collapse";
			document.getElementById("div_xmla_username2").className="collapse";
			document.getElementById("div_xmla_password1").className="collapse";
			document.getElementById("div_xmla_password2").className="collapse";
			
			document.getElementById("displayResultset").className="expand";

			if(i<0){
				if(i!=-2 && i!=-10 && i!=-13){
					document.getElementById("div_xaxis1").className="expand";
					document.getElementById("div_xaxis2").className="expand";
					document.getElementById("div_yaxis1").className="expand";
					document.getElementById("div_yaxis2").className="expand";
				} else {
					document.getElementById("div_xaxis1").className="collapse";
					document.getElementById("div_xaxis2").className="collapse";
					document.getElementById("div_yaxis1").className="collapse";
					document.getElementById("div_yaxis2").className="collapse";
				}

				document.getElementById("div_graph_options1").className="expand";
				document.getElementById("div_graph_options2").className="expand";
			} else {
				document.getElementById("div_xaxis1").className="collapse";
				document.getElementById("div_xaxis2").className="collapse";
				document.getElementById("div_yaxis1").className="collapse";
				document.getElementById("div_yaxis2").className="collapse";
				document.getElementById("div_graph_options1").className="collapse";
				document.getElementById("div_graph_options2").className="collapse";
			}
            
            if(i==0 || i==101 || i==102 || i==103 || i<0 || (i>=1 && i<=99)){
                document.getElementById("showParameters").className="expand";                
            } else {
                //show parameters option not valid for other query types
                document.getElementById("showParameters").className="collapse";
            }
            
            if(i==120){
                //static lov
                document.getElementById("usesRules").className="collapse";
                document.getElementById("dbId").className="collapse";
                document.getElementById("showParameters").className="collapse";
				document.getElementById("displayResultset").className="collapse";
            }
			
			if(i==121){
                //dynamic job recepients query
                document.getElementById("usesRules").className="collapse";
            }
		}

		//use different textarea for text querys
		if (i == 111) {
			// disable the area to avoid it to be submitted
			document.getElementById("sourceTextArea").disabled=true;
			// hide the area
			document.getElementById("sourceTextAreadiv").className="collapse";

			//enable area for text input
			document.getElementById("textSource").disabled=false;
			document.getElementById("textSourceDiv").className="expand";
		} else {
			//re-enable the main area
			document.getElementById("sourceTextArea").disabled=false;
			document.getElementById("sourceTextAreadiv").className="expand";

			//disable area for text input
			document.getElementById("textSource").disabled=true;
			document.getElementById("textSourceDiv").className="collapse";
		}
	}
	// End -->
</script>

<form method="post" name="updQuery" action="execEditQuery.jsp" enctype="multipart/form-data">
    <input type="hidden" name="QUERYACTION" value="<%=request.getParameter("QUERYACTION")%>" >
    <%
     if (MODIFY) {
    %>
    <input type="hidden" name="QUERY_ID" value="<%=request.getParameter("QUERY_ID")%>" >
    <%
     }
    %>
    <table align="center">
		<tr><td class="title" colspan="2" >Define Query</td></tr>
        <tr><td class="title" colspan="2" > Header</td></tr>
        <tr><td class="data"> ID </td>
			<%
			if(queryId==-1){
				currentStringValue="Auto";
			} else {
				currentStringValue=String.valueOf(queryId);
			}
			%>
            <td class="data"> <%=currentStringValue%> </td>
        </tr>
        <tr><td class="data"> Group </td>
            <td class="data">
                <select name="GROUP_ID" size="1">
                    <%
                    int currentGroupId = aq.getGroupId();
                    if(currentGroupId==-1){
                        //this is a new query. default to selecting group id 2 i.e the first custom group
                        currentGroupId=2;
                    }

					int counterGroups=0;
                    boolean isOnlyListGroupAvailable = true;
					int gId;
					String groupName;
					QueryGroup qg;
					Map<String, QueryGroup> groups=aq.getAdminQueryGroups(accessLevel,username);
					for (Map.Entry<String, QueryGroup> entry : groups.entrySet()) {
						counterGroups++;
						qg=entry.getValue();
						gId=qg.getGroupId();
						groupName=qg.getName();

						if (gId != 0){
							isOnlyListGroupAvailable = false;
						}

						%>
						<option value="<%=gId%>" <%=(gId == currentGroupId?"selected":"")%> >
							<%=groupName%>
						</option>
						<%
					}
					%>
                </select>
				<%=(isOnlyListGroupAvailable?"<small>With the LOV group you can only create List of Values for query parameters,<br> you need another group to create real queries</small>":"")%>
            </td>
        </tr>
        <tr><td class="data"> Name </td>
            <td class="data"> <input type="text" name="NAME" size="50" maxlength="50" value="<%=aq.getName()%>"> </td>
        </tr>

        <tr><td class="data"> Status </td>
            <td class="data">
                <%
                 currentStringValue = aq.getStatus();
                %>
                <select name="STATUS" size="1">
                    <option value="A" <%=("A".equals(currentStringValue)?"selected":"")%>>Active</option>
                    <option value="D" <%=("D".equals(currentStringValue)?"selected":"")%>>Disabled</option>
                    <option value="H" <%=("H".equals(currentStringValue)?"selected":"")%>>Hidden</option>
                </select>
                <input type="button" class="buttonup" onclick="javascript:alert('If a query is disabled, it will not run, either interactively or as a scheduled job.')" value="?" onMouseOver="javascript:btndn(this);" onMouseOut="javascript:btnup(this);">
            </td></tr>
        </tr>


        <tr><td class="data"> Short Description <br><small>(also: Title on Graphs/Dashboards)</small> </td>
            <td class="data"> <input type="text" name="SHORT_DESCRIPTION" size="50" maxlength="254" value="<%=aq.getShortDescription()%>"> </td>
        </tr>

        <tr><td class="data"> Description</td>
            <td class="data">
                <textarea name="DESCRIPTION" rows="2" cols="40"><%=aq.getDescription()%></textarea>
            </td>
        </tr>

        <tr><td class="data"> Contact Person </td>
            <td class="data"> <input type="text" name="CONTACT_PERSON" size="30" maxlength="20" value="<%=aq.getContactPerson()%>"> </td>
        </tr>
        
        <tr><td class="data"> Type</td>
            <td class="data">
                <select name="QUERY_TYPE" id="typeId" size="1" onChange="javascript:onTypeSelection();">
					<%
						currentStringValue = "" + aq.getQueryType();
					%>
                    
                    <option value="119" <%=(currentStringValue.equals("119")?"SELECTED":"")%>>LOV: Dynamic</option>
                    <option value="120" <%=(currentStringValue.equals("120")?"SELECTED":"")%>>LOV: Static</option>
					<option value="121" <%=(currentStringValue.equals("121")?"SELECTED":"")%>>Dynamic Job Recipients</option>

                    <option value="110" <%=(currentStringValue.equals("110")?"SELECTED":"")%>>Dashboard</option>
                    <option value="111" <%=(currentStringValue.equals("111")?"SELECTED":"")%>>Text</option>
                    <option value="100" <%=(currentStringValue.equals("100")?"SELECTED":"")%>>Update Statement</option>
                    <option value="101" <%=(currentStringValue.equals("101")?"SELECTED":"")%>>Crosstab </option>
                    <option value="102" <%=(currentStringValue.equals("102")?"SELECTED":"")%>>Crosstab (html only)</option>
					<option value="0" <%=(currentStringValue.equals("0")?"SELECTED":"")%>>Tabular </option>
                    <option value="103" <%=(currentStringValue.equals("103")?"SELECTED":"")%>>Tabular (html only)</option>
                    <option value="-1" <%=(currentStringValue.equals("-1")?"SELECTED":"")%>>Graph: XY Chart</option>
					<option value="-13" <%=(currentStringValue.equals("-13")?"SELECTED":"")%>>Graph: Pie 2D</option>
                    <option value="-2" <%=(currentStringValue.equals("-2")?"SELECTED":"")%>>Graph: Pie 3D</option>
					<option value="-14" <%=(currentStringValue.equals("-14")?"SELECTED":"")%>>Graph: Vertical Bar 2D</option>
                    <option value="-4" <%=(currentStringValue.equals("-4")?"SELECTED":"")%>>Graph: Vertical Bar 3D</option>
					<option value="-15" <%=(currentStringValue.equals("-15")?"SELECTED":"")%>>Graph: Stacked Vertical Bar 2D</option>
                    <option value="-8" <%=(currentStringValue.equals("-8")?"SELECTED":"")%>>Graph: Stacked Vertical Bar 3D</option>
					<option value="-16" <%=(currentStringValue.equals("-16")?"SELECTED":"")%>>Graph: Horizontal Bar 2D</option>
                    <option value="-3" <%=(currentStringValue.equals("-3")?"SELECTED":"")%>>Graph: Horizontal Bar 3D</option>
					<option value="-17" <%=(currentStringValue.equals("-17")?"SELECTED":"")%>>Graph: Stacked Horizontal Bar 2D</option>
                    <option value="-9" <%=(currentStringValue.equals("-9")?"SELECTED":"")%>>Graph: Stacked Horizontal Bar 3D</option>
                    <option value="-5" <%=(currentStringValue.equals("-5")?"SELECTED":"")%>>Graph: Line</option>
                    <option value="-6" <%=(currentStringValue.equals("-6")?"SELECTED":"")%>>Graph: Time Series</option>
                    <option value="-7" <%=(currentStringValue.equals("-7")?"SELECTED":"")%>>Graph: Date Series</option>
                    <option value="-10" <%=(currentStringValue.equals("-10")?"SELECTED":"")%>>Graph: Speedometer</option>
					<option value="-11" <%=(currentStringValue.equals("-11")?"SELECTED":"")%>>Graph: Bubble Chart</option>
					<option value="-12" <%=(currentStringValue.equals("-12")?"SELECTED":"")%>>Graph: Heat Map</option>

					<%
					if(ArtDBCP.isArtFullVersion()){
					%>
                    <option value="112" <%=(currentStringValue.equals("112")?"SELECTED":"")%>>Pivot Table: Mondrian</option>
                    <option value="113" <%=(currentStringValue.equals("113")?"SELECTED":"")%>>Pivot Table: Mondrian XMLA</option>
                    <option value="114" <%=(currentStringValue.equals("114")?"SELECTED":"")%>>Pivot Table: Microsoft XMLA</option>
                    <option value="115" <%=(currentStringValue.equals("115")?"SELECTED":"")%>>Jasper Report: Template Query</option>
                    <option value="116" <%=(currentStringValue.equals("116")?"SELECTED":"")%>>Jasper Report: ART Query</option>
					<option VALUE="117" <%=(currentStringValue.equals("117")?"SELECTED":"")%>>jXLS Spreadsheet: Template Query</option>
					<option VALUE="118" <%=(currentStringValue.equals("118")?"SELECTED":"")%>>jXLS Spreadsheet: ART Query</option>
					<%
					}
					%>

                    <option value="1" <%=(currentStringValue.equals("1")?"SELECTED":"")%>>Report on Column: 1</option>
                    <option value="2" <%=(currentStringValue.equals("2")?"SELECTED":"")%>>Report on Column: 2</option>
                    <option value="3" <%=(currentStringValue.equals("3")?"SELECTED":"")%>>Report on Column: 3</option>
                    <option value="4" <%=(currentStringValue.equals("4")?"SELECTED":"")%>>Report on Column: 4</option>
                    <option value="5" <%=(currentStringValue.equals("5")?"SELECTED":"")%>>Report on Column: 5</option>

                </select>
					<%
				helpText="See the Notes at the bottom of this page for the syntax to use for some query types e.g. graphs, crosstabs, dashboards";
				%>
				<input type="button" class="buttonup" onclick="javascript:alert('<%=helpText%>')" value="?" onMouseOver="javascript:btndn(this);" onMouseOut="javascript:btnup(this);">
			</td>
        </tr>

        <tr><td class="data"> Datasource </td>
            <td class="data">
                <select name="DATABASE_ID" id="dbId" size="1" >
                    <%
					currentIntValue = aq.getDatabaseId();

					int counterDatasources=0;
					int dbId;
					String dbName;
					Map<String, Integer> dbs=aq.getAdminDatasources(accessLevel,username);
					for (Map.Entry<String, Integer> entry : dbs.entrySet()) {
						counterDatasources++;
						Integer integerId=entry.getValue();
						dbId=integerId.intValue();
						dbName=entry.getKey();

						%>
						<option value="<%=dbId%>" <%=(dbId == currentIntValue?"selected":"")%> >
							<%=dbName%>
						</option>
						<%
					}
					%>

                </select>
            </td>
        </tr>
        <%
         if (counterGroups<1 || counterDatasources <1) {
        %>
        <tr>
            <td class="data" colspan="2" >
                <span style="color:red"><b>Error:</b></span> <br>
                At least one Datasource and one Query Group need to be available in order to manage queries.
            </td>
        </tr>
    </table>
    <%
       return;
    }

    %>
    
    <tr><td class="data"> Uses Rules</td>
        <td class="data">
            <%
             currentStringValue = aq.getUsesRules();
            %>
            <select name="USES_RULES" id="usesRules" size="1">
                <option value="N" <%=("N".equals(currentStringValue)?"selected":"")%>>No</option>
                <option value="Y" <%=("Y".equals(currentStringValue)?"selected":"")%>>Yes</option>
            </select> </td></tr>
    </tr>
        
    <tr><td class="data"> Show Parameters In Output</td>
        <td class="data">
            <%
             currentStringValue = aq.getShowParameters();
            %>
            <select name="showParameters" id="showParameters" size="1">
                <option value="N" <%=("N".equals(currentStringValue)?"selected":"")%>>No</option>
                <option value="Y" <%=("Y".equals(currentStringValue)?"selected":"")%>>Yes</option>
                <option value="A" <%=("A".equals(currentStringValue)?"selected":"")%>>Always</option>
            </select> </td></tr>
    </tr>
	
	<tr><td class="data"> Display Resultset </td>
            <td class="data">
				<input type="text" name="displayResultset" id="displayResultset" size="3" maxlength="2" value="<%=aq.getDisplayResultset()%>">
				<%
				helpText="The resultset to display if the sql source contains multiple sql statements e.g. "
						+ "some DDL/DML statements before the main select."
						+ "\\nLeave as 0 if the sql source doesn\\'t have multiple statements."
						+ "\\nSet to 1 to use the first statement, 2 to use the second, etc."
						+ "\\nSet to -1 to use the select statement, regardless of how many statements exist."
						+ "\\nSet to -2 to use the last statement, regardless of how many statements exist."
						+ "\\nYour RDBMS may not support multiple statements in a query or may require some configuration for it to work.";
				%>
				<input type="button" class="buttonup" onclick="javascript:alert('<%=helpText%>')" value="?" onMouseOver="javascript:btndn(this);" onMouseOut="javascript:btnup(this);">
			</td>
        </tr>

	<tr><td class="data">
			<div id="div_xaxis1">
			X Axis Label
			</div>
			</td>
			<td class="data">
				<div id="div_xaxis2">
					<input type="text" name="xaxisLabel" size="30" maxlength="50" value="<%=aq.getXaxisLabel()%>">
				</div>
			</td>
		</tr>

		<tr><td class="data">
			<div id="div_yaxis1">
			Y Axis Label
			</div>
			</td>
			<td class="data">
				<div id="div_yaxis2">
					<input type="text" name="yaxisLabel" size="30" maxlength="50" value="<%=aq.getYaxisLabel()%>">
				</div>
			</td>
		</tr>

		<tr><td class="data">
			<div id="div_graph_options1">
			Graph Options
			</div>
			</td>
			<td class="data">
				<div id="div_graph_options2">
					<input type="checkbox" name="showLegend" <%=(aq.isShowLegend()?"checked":"")%> /><%=messages.getString("legend")%>
					<input type="checkbox" name="showLabels" <%=(aq.isShowLabels()?"checked":"")%> /><%=messages.getString("labels")%>
					<input type="checkbox" name="showDataPoints" <%=(aq.isShowPoints()?"checked":"")%> /><%=messages.getString("dataPoints")%>
					<input type="checkbox" name="showGraphData" <%=(aq.isShowGraphData()?"checked":"")%> /><%=messages.getString("graphData")%>

					<br><br>
					Width
					<input type="text" name="graph_width" value=<%=aq.getGraphWidth()%> size="4" maxlength="4">
					Height
					<input type="text" name="graph_height" value=<%=aq.getGraphHeight()%> size="4" maxlength="4">
					&nbsp;&nbsp;Back Color
					<input type="text" name="graph_bgcolor" value=<%=aq.getGraphBgColor()%> size="7" maxlength="7">

					<br><br>
					y-axis Min
					<input type="text" name="graph_ymin" value=<%=aq.getGraphYMin()%> size="5" maxlength="15">
					y-axis Max
					<input type="text" name="graph_ymax" value=<%=aq.getGraphYMax()%> size="5" maxlength="15">
					<input type="button" class="buttonup" onclick="javascript:alert('Leave y-axis min and max values as 0 to use the full data range of the result set')" value="?" onMouseOver="javascript:btndn(this);" onMouseOut="javascript:btnup(this);">
					
					<br><br>
					Rotate x-axis labels at
					<input type="text" name="graph_rotate_at" value=<%=aq.getGraphRotateAt()%> size="4" maxlength="4">
					<%
					helpText="Display x-axis labels vertically when the graph contains this number of categories."
							+ "\\nSet to 1 to always display x-axis labels vertically";
					%>
					<input type="button" class="buttonup" onclick="javascript:alert('<%=helpText%>')" value="?" onMouseOver="javascript:btndn(this);" onMouseOut="javascript:btnup(this);">
					Remove x-axis labels at
					<input type="text" name="graph_remove_at" value=<%=aq.getGraphRemoveAt()%> size="4" maxlength="4">
					<%
					helpText="Omit x-axis labels when the graph contains this number of categories."
							+ "\\nSet to 1 to always omit x-axis labels";
					%>
					<input type="button" class="buttonup" onclick="javascript:alert('<%=helpText%>')" value="?" onMouseOver="javascript:btndn(this);" onMouseOut="javascript:btnup(this);">
				</div>
			</td>
		</tr>

    <tr><td class="data">
		<div id="div_template1">
		Template
		</div>
		</td>
        <td class="data">
			<div id="div_template2">
				<input type="text" name="template_filename" size="30" maxlength="100" value="<%=aq.getTemplate()%>"> <br>
				<input type="file" name="template" id="template" size="40">
				<%
				helpText="Select the path of the mondrian schema xml file, jasper reports file or jXLS template file to use. "
						+"Alternatively, type the file name in the textbox above to reuse an already uploaded file.";
				%>
				<input type="button" class="buttonup" onclick="javascript:alert('<%=helpText%>')" value="?" onMouseOver="javascript:btndn(this);" onMouseOut="javascript:btnup(this);">
				<br><br>
				Subreport <br>
				<input type="file" name="subreport" id="subreport" size="40">
				<%
				helpText="For jasper reports, if your main report uses a subreport, "
						+"you can use this field to upload the subreport file.";
				%>
				<input type="button" class="buttonup" onclick="javascript:alert('<%=helpText%>')" value="?" onMouseOver="javascript:btndn(this);" onMouseOut="javascript:btnup(this);">
			</div>
        </td>
    </tr>

    <tr><td class="data">
		<div id="div_xmla_url1">
		XMLA URL
		</div>
		</td>
        <td class="data">
			<div id="div_xmla_url2">
				<input type="text" name="xmla_url" id="xmla_url" size="50" maxlength="300" value="<%=aq.getXmlaUrl()%>">
			</div>
		</td>
    </tr>

    <tr><td class="data">
		<div id="div_xmla_datasource1">
		XMLA Datasource
		</div>
		</td>
        <td class="data">
			<div id="div_xmla_datasource2">
				<input type="text" name="xmla_datasource" id="xmla_datasource" size="50" maxlength="50" value="<%=aq.getXmlaDatasource()%>">
			</div>
		</td>
    </tr>

    <tr><td class="data">
		<div id="div_xmla_catalog1">
		XMLA Catalog
		</div>
		</td>
        <td class="data">
			<div id="div_xmla_catalog2">
				<input type="text" name="xmla_catalog" id="xmla_catalog" size="30" maxlength="50" value="<%=aq.getXmlaCatalog()%>">
				<%
				helpText="For Microsoft XMLA, this is the SSAS database name";
				%>
				<input type="button" class="buttonup" onclick="javascript:alert('<%=helpText%>')" value="?" onMouseOver="javascript:btndn(this);" onMouseOut="javascript:btnup(this);">
			</div>
		</td>
    </tr>

	<tr><td class="data">
		<div id="div_xmla_username1">
		XMLA Username
		</div>
		</td>
        <td class="data">
			<div id="div_xmla_username2">
				<%
				currentStringValue=aq.getXmlaUsername();
				if(currentStringValue==null){
					currentStringValue="";
				}
				%>
				<input type="text" name="xmla_username" id="xmla_username" size="30" maxlength="50" value="<%=currentStringValue%>">
				<%
				helpText="Optional. May be required if using Microsoft XMLA and the server requires basic authentication.";
				%>
				<input type="button" class="buttonup" onclick="javascript:alert('<%=helpText%>')" value="?" onMouseOver="javascript:btndn(this);" onMouseOut="javascript:btnup(this);">
			</div>
		</td>
    </tr>

	<tr><td class="data">
		<div id="div_xmla_password1">
		XMLA Password
		</div>
		</td>
        <td class="data">
			<div id="div_xmla_password2">
				<%
				String password=aq.getXmlaPassword();
				if(password==null){
					password="";
				} else {
					password=Encrypter.decrypt(password);
				}
				%>				
				<input type="password" name="xmla_password" id="xmla_password" size="30" maxlength="50" value="<%=password%>">
			</div>
		</td>
    </tr>

    <tr>
        <td class="title" colspan="2" >
            <div id="sourceTextAreaHeaderdiv">
                <span id="sqlcode">Source (SQL)</span>
                <span id="xmlcode">Source (xml)</span>
				<span id="textcode">Source (text/html)</span>
                <span id="mdxcode">Source (MDX)</span>
                <input type="submit" value="Save Changes">
            </div>
        </td>
    </tr>
    <tr><td colspan="2" >
            <div id="sourceTextAreadiv">
                <textarea name="SQL" id="sourceTextArea" cols="70" rows="20" wrap="off"><%= aq.getText()%></textarea>
            </div>
        </td>
    </tr>
	<tr><td colspan="2" >
            <div id="textSourceDiv">
                <textarea name="textSource" id="textSource" cols="70" rows="10" wrap="off"><%= aq.getText()%></textarea>
            </div>
        </td>
    </tr>

    <tr><td class="data" colspan="2" align="center"> <input type="submit" value="Save Changes"> </td>
    </tr>
</table>
</form>

<div id="querydiv">
    <table align="center" class="art">

        <tr><td type="attr">

                <b>Notes:</b> <br>
                <ul>                    
                    <li>You can use the special tag  <i>:USERNAME</i> to create dynamic security filters
                        (other than the ones you can create using <i>rules</i>).
                        The string will be automatically substituted with the username of the person who is running the
                        query prior to sending the query to the database.
                    </li>
                    <li>Other available tags are: <i>:DATE</i> (format is YYYY-MM-DD)
                        and <i>:TIME</i> (format is YYYY-MM-DD HH:MI:SS)
                    </li>
                    <li>
                        You can use the following xml-like syntax to create dynamic SQL:
                        <i>
                            <br>&lt;IF&gt;
                            <br>&lt;EXP1&gt;value1&lt;/EXP1&gt; &lt;OP&gt;operator&lt;/OP&gt; &lt;EXP2&gt;value2&lt;/EXP2&gt;
                            <br>&lt;TEXT&gt;...&lt;/TEXT&gt;
                            <br>&lt;ELSETEXT&gt;...&lt;/ELSETEXT&gt;
                            <br>&lt;/IF&gt;
                        </i><br>
                        <i>EXP1</i> and <i>EXP2</i> values can be inline labels, tags or static strings;
                        <i>operator</i> can be one of the following:
                        <select style="font-size: 8pt">
                            <option>eq or equals</option>
							<option>neq or not equals</option>
                            <option>la - less than (alpha)</option>
                            <option>ga - great than (alpha)</option>
                            <option>ln - less than (number)</option>
                            <option>gn - great than (number)</option>
                            <option>is blank</option>
                            <option>is not blank</option>
                            <option>starts with</option>
                            <option>ends with</option>
                            <option>contains</option>
                        </select>
                    </li>                   
                </ul>
                <b>Type:</b>
                <br>
                <ul>
                    <li>
                        <i>Crosstab</i>: Create a pivot over two columns of the
	resultset. The resultset is expected to be:<br>
                        <i> SELECT Col1 "Label1", [AltSort1,] Col2 "Label2", [AltSort2,] Value FROM ... </i> (data type: string, [any, ] string, [any, ] any )
                        <br>The AltSort columns are optional.
                    </li>
                    <li>
                        <i>Report on Column</i>: Split the retrieved rows
	in order to group values. If the value is set to <i>n</i>, the query
	must be ordered by the the first <i>n-1</i> columns.
                    </li>
                    <li>

                        <i>Graph</i>: Chart the result set. See below for the query layout.<br>

                        <br><b>XY</b>: <br><i> SELECT Value1, Value2 "Series Name" FROM ...</i> (data type: number, number )
                        
						<br><br><b>Pie</b>: <br><i> SELECT Category, Value FROM ...</i> (data type: string, number )
                        
						<br><br><b>Bars/Line</b>:
                        <br>Static Series
                        <br><i> SELECT Item, Value1 "Series1 Name" [, Value2, ...]  FROM ...</i> (data type: string, number, [, number, ...] )
                        <br>Dynamic Series
                        <br><i> SELECT Item, SeriesName, Value  FROM ...</i> (data type: string, string, number)
                        
						<br><br><b>Time/Date Series</b>:
                        <br>Static Series
                        <br><i> SELECT Timestamp|Date, Value1 "Series1 Name" [, Value2, ...]  FROM ...</i> (data type: timestamp|date, number, [, number, ...] ). Date must be unique.
                        <br>Dynamic Series
                        <br><i> SELECT Timestamp|Date, SeriesName, Value  FROM ...</i> (data type: timestamp|date, string, number)

                        <br><br><b>Speedometer</b>: <br>
                        <i>SELECT DataValue, MinValue, MaxValue, UnitsDescription [, Range1, Range2, ...] FROM ...</i> (data type: number, number, number, string)
                        <br />Ranges represent optional columns and each range has 3 values separated by :  i.e. RangeUpperValue:RangeColour:RangeDescription (data type: number, string, string)
						RangeUpperValue can be a percentage.
                        <br />Example:
                        <br />SELECT reading, 0, 100, "degrees",
                        <br />"50:#00FF00:Normal",
                        <br />"80%:#FFFF00:Warning",
                        <br />"100:#FF0000:Critical"
						<br />FROM temperature_reading
						
						<br><br><b>Bubble</b>: <br><i> SELECT Value1, Value2 "Series Name", Value3 [, normalisedValue3] FROM ...</i> (data type: number, number, number [,number] )
						
						<br><br><b>Heat Map</b>: <br>
                        <i>SELECT Value1, Value2, Value3 [, Option1, Option2, ...] FROM ...</i> (data type: number, number, number [,string, string, ...)
                        <br>The option columns are used to configure the chart and are in the form &lt;option&gt;=&lt;value&gt;. See the Admin Manual for possible options.
                        <br />Example:
                        <br />SELECT x, y, z, "upperBound=100",
						<br />FROM myvalues

                    </li>
                </ul>

            </td></tr>

    </table>
</div>

<div id="portletdiv">
    <table align="center" class="art">

        <tr>
            <td type="attr">

                    <b>Notes:</b> <br>
                    <ul>
                        <li><b>Dashboard</b><br>
                            Any ART query, graph or text query can be considered a portlet, i.e.
	a small embeddable frame within a web page.
	A Dashboard is an object that allows you to group portlets in a
	single page.<br>
                            You can create small portal-like web pages to display queries,
	graphs, text fragments (see below) or static pages.<br>
	Before executing a dashboard, you can specify
	the parameters for all the queries. Parameters with identical labels
	are displayed once.<br>
	Use the following xml-like syntax to build your dashboard:
                            <b> tags are case sensitive (all in uppercase) </b><br>
                            <textarea readonly cols="80" rows="10"><DASHBOARD>
<COLUMN>
<!-- column size: auto|small|medium|large -->
<SIZE>medium</SIZE>
<!-- create a new portlet within this column
     to embed an ART query (tabular, graph, text) -->
<PORTLET>
<TITLE>Portlet title</TITLE>
 <!-- (optional, default is true) load content when page appears -->
 <ONLOAD>true</ONLOAD>
 <!-- (optional, default is never) refresh content every 30 seconds-->
 <REFRESH>30</REFRESH>
 <QUERYID>2</QUERYID>
</PORTLET>
<!-- create a new portlet within this column
     to embed an external html fragment -->
<PORTLET>
<TITLE>Portlet title</TITLE>
 <URL>Url</URL>
</PORTLET>

<!-- .. you can add as many portlets as you want -->
</COLUMN>
<COLUMN>
<!-- you can add as many columns as you want -->
</COLUMN>
</DASHBOARD></textarea>

                        </li>
                        <li><b>Text</b><br>
                            A text query is just an html fragment (without the &lt;html&gt; tags) that
	can be rendered stand-alone or in a dashboard. <br>
	A text query can be viewed by any user and can be edited by users that have
	been granted access to it. With the latter, an edit link appears at the bottom right of the page
	when the user views it. This allows one to edit the content using a 
	WYSIWYG editor.
                        </li>
                    </ul>
            </td>
        </tr>
    </table>
</div>

<script language="Javascript">
    onTypeSelection();
</script>

<%@ include file ="/user/footer.jsp" %>
