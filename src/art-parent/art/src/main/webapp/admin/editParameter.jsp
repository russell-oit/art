<%@ page import="java.sql.*,java.util.*,art.utils.*" %>
<%@ page import="org.apache.commons.lang.StringUtils" %>
<%@ include file ="headerAdmin.jsp" %>

<%
 Connection conn = (Connection) session.getAttribute("SessionConn");
 if ( conn == null || conn.isClosed()) {
%>
<jsp:forward page="error.jsp">
    <jsp:param name="MOD" value="Update Parameter"/>
    <jsp:param name="ACT" value="Get connection from session"/>
    <jsp:param name="MSG" value="Database connection not valid. Please log in again"/>
    <jsp:param name="NUM" value="100"/>
</jsp:forward>
<%
}
 
boolean MODIFY = request.getParameter("PARAMACTION").equals("MODIFY");
 
int queryId = Integer.parseInt(request.getParameter("QUERY_ID"));
int fieldPosition = -1;

ArtQueryParam qp = new ArtQueryParam();
if (MODIFY) {
	fieldPosition = Integer.parseInt(request.getParameter("FIELD_POSITION")); //request parameter not availabe if new 
	qp.create(conn, queryId, fieldPosition);
}

String help;

%>

<script language="javascript" type="text/javascript">
	<!-- Begin
	function onTypeSelection() {
		var paramType = document.getElementById("paramType").value;
		
		// reset use lov drop down
		var useLov = document.getElementById("useLov");
		useLov.options.length = 0; // reset the select
		//
		//reset data type drop down
		var dataType = document.getElementById("paramDataType");
		dataType.options.length = 0; // reset the select
		   
		if(paramType == 'M'){
			//hide fields not relevant for multi parameters
			document.getElementById("drilldownColumn").className="collapse";
			document.getElementById("directSubstitution").className="collapse";
			
			//default use lov to yes
			var i=0;
			useLov.options[i++] = new Option("Yes","Y");
			useLov.options[i++] = new Option("No","N");
			
			//set available data types
			i=0;
			dataType.options[i++] = new Option("VARCHAR","VARCHAR");
			dataType.options[i++] = new Option("NUMBER","NUMBER");
		} else if(paramType == 'I') {
			//inline parameter. unhide fields that may have been hidden for multi parameter
			document.getElementById("drilldownColumn").className="expand";
			document.getElementById("directSubstitution").className="expand";
			
			//default use lov to no
			var i=0;
			useLov.options[i++] = new Option("No","N");
			useLov.options[i++] = new Option("Yes","Y");	
			
			//set available data types
			i=0;
			dataType.options[i++] = new Option("VARCHAR","VARCHAR");
			dataType.options[i++] = new Option("TEXT","TEXT");			
			dataType.options[i++] = new Option("INTEGER","INTEGER");
			dataType.options[i++] = new Option("NUMBER","NUMBER");
			dataType.options[i++] = new Option("DATE","DATE");
			dataType.options[i++] = new Option("DATETIME","DATETIME");
			dataType.options[i++] = new Option("DATASOURCE","DATASOURCE");
		}
		
		//set use lov to saved value
		var useLovValue="<%=qp.getUseLov()%>";			
		for (var i = 0; i < useLov.options.length; i++) {				
			if (useLov.options[i].value === useLovValue) {
				useLov.selectedIndex = i;
				break;
			}
		}
		
		//set data type to saved value
		var dataTypeValue="<%=qp.getParamDataType()%>";			
		for (var i = 0; i < dataType.options.length; i++) {				
			if (dataType.options[i].value === dataTypeValue) {
				dataType.selectedIndex = i;
				break;
			}
		}
	}
	
	//-->
</script>
	

<form action="execEditParameter.jsp" method="post">
    <input type="hidden" name="QUERY_ID" value="<%= request.getParameter("QUERY_ID")%>">
    <input type="hidden" name="PARAMACTION" value="<%= request.getParameter("PARAMACTION")%>">
	<input type="hidden" name="FIELD_POSITION" value="<%= fieldPosition%>">
   
    <table align="center">
        <tr><td class="title" colspan="2" > Define Parameter </td></tr>
       		
		<tr><td class="data"> Parameter Type </td><td class="data">
                <select name="PARAM_TYPE" id="paramType" size="1" onChange="javascript:onTypeSelection();">
                    <option value="I" <%=("I".equals(qp.getParamType())?"selected":"")%>>Inline</option>                    
                    <option value="M"  <%=("M".equals(qp.getParamType())?"selected":"") %>>Multi</option>                    
                </select>
            </td>
        </tr>
		<tr><td class="data"> Parameter Label <br><small>(without #, case sensitive)</small></td>
            <td class="data"> <input type="text" name="PARAM_LABEL" size="40" maxlength="55" value="<%=qp.getParamLabel()%>"></td>
        </tr>
        <tr><td class="data"> Name (User Viewable) </td>
            <td class="data"> <input type="text" name="NAME" size="25" maxlength="25" value="<%=qp.getName()%>"> </td>
        </tr>
        <tr><td class="data"> Short Description </td>
            <td class="data"> <input type="text" name="SHORT_DESCRIPTION" size="40" maxlength="40" value="<%=qp.getShortDescription()%>"> </td>
        </tr>

        <tr><td class="data"> Help Description </td>
            <td class="data"> <input type="text" name="DESCRIPTION" size="40" maxlength="120" value="<%=qp.getDescription()%>"> </td>
        </tr>
       
        <tr><td class="data"> Data Type </td><td class="data">
                <select name="PARAM_DATA_TYPE" id="paramDataType" size="1">
					<!-- populated dynamically. inline and multi parameters have different options -->
                   </select>
            </td>
        </tr>
		
        <tr><td class="data"> Default Value
                </td>
				<%
				String defaultValue=qp.getDefaultValue();
				if(defaultValue==null){
					defaultValue="";
				}
				%>
            <td class="data"> <input type="text" name="DEFAULT_VALUE" size="25" maxlength="80" value="<%=defaultValue%>">
				
			<input type="button" class="buttonup" onclick="javascript:alert('Default DATE values\n\n1. For DATE parameters, use YYYY-MM-DD format (e.g. 2012-01-01) to set a specific default date.\n\n2. For DATETIME parameters, use YYYY-MM-DD HH:mm or YYYY-MM-DD HH:mm:ss  (e.g. 2012-01-30 23:15 or 2012-01-30 23:15:45).\n\n3. Leave blank to set the default date to the current date - i.e. when the query is executed.\n\n4. Use the following syntax to set an offset from current date:\n ADD DAYS|MONTHS|YEARS <number> \n e.g. ADD DAYS -5 -> set the date to 5 days in the past')" value="?" onMouseOver="javascript:btndn(this);" onMouseOut="javascript:btnup(this);">				
            </td>
        </tr>

        <tr><td class="data"> Use List Of Values (LOV) <br>
                <small>(Set to Yes if the parameter <br> should be picked from a list)</small>
            </td>
            <td class="data">
                <select name="USE_LOV" id="useLov" size="1">
					<!-- populated dynamically. inline and multi parameters have different order of the options -->
                </select> </td>
        </tr>

        <tr><td class="data"> LOV Query <br>
                <small>(The query that will produce the list of values)</small>
            </td>
            <td class="data">
                <select name="LOV_QUERY_ID" size="1">
					<option value="0">None</option>
                    <%
					ArtQuery aq=new ArtQuery();
					int id;
					String name;

					Map<Integer, String> lovs=aq.getLovQueries();
					for (Map.Entry<Integer, String> entry : lovs.entrySet()) {
						Integer integerId=entry.getKey();
						id=integerId.intValue();
						name=entry.getValue();
						%>
						<option value="<%=id%>" <%=(qp.getLovQueryId() == id )?"selected":"" %> >
							<%=id%> - <%=name%>
						</option>
						<%
					   }
                    %>
                </select>
            </td>
        </tr>


        <tr><td class="data"> Apply Rules on LOV<br>
                <small> (Filter the list of values <br> if a rule exists for the LOV query) </small>
            </td>
            <td class="data">
                <select name="APPLY_RULES_TO_LOV" size="1">
                    <option value="N" <%=("N".equals(qp.getApplyRulesToLov())? "selected" : "")%>  >No</option>
					<option value="Y" <%=("Y".equals(qp.getApplyRulesToLov())? "selected" : "")%>  >Yes</option>
                </select> </td>
        </tr>

        <tr><td class="data">Chained Parameter Sequence<br>
                <small> (This parameter - slave - depends on <br> values of another parameter - master) </small>
            </td>
            <td class="data">
                <select name="CHAINED_POSITION">
                    <option value="0" <%= (qp.getChainedPosition() ==  0 ? "SELECTED" : "")%>>Not Chained</option>
                    <%
                    for(int i=1; i<=20; i++) {
                    %>
                    <option value="<%=i%>" <%= (qp.getChainedPosition() == i ? "SELECTED" : "")%> >Chain on param <%=i%> </option>
                    <%}%>
                </select>

                <input type="button" class="buttonup" onclick="javascript:alert('If this parameter is chained, the value the user selects in the master parameter is used to filter the values displayed for this one.\nThe LOV query needs to have an inline parameter labelled #filter# \n\nSee admin manual for more details on Chained Parameters.')" value="?" onMouseOver="javascript:btndn(this);" onMouseOut="javascript:btnup(this);">

            </td>
        </tr>

        <tr><td class="data">Chained Value Position</td>
            <td class="data">
                <select name="CHAINED_VALUE_POSITION">
                    <option VALUE="0" <%= (qp.getChainedValuePosition() == 0 ? "SELECTED" : "")%>>Same as sequence</option>
                    <%
                    for(int i=1; i<=20; i++) {
                    %>
                    <option value="<%=i%>" <%= (qp.getChainedValuePosition() == i ? "SELECTED" : "")%> >Param <%=i%> </option>
                    <%}%>
                </select>

                <input type="button" class="buttonup" onclick="javascript:alert('The master parameter which will drive the value of this parameter, if not the same as the chained parameter sequence. \n\nSee admin manual for more details on Chained Parameters.')" value="?" onMouseOver="javascript:btndn(this);" onMouseOut="javascript:btnup(this);">

            </td>
        </tr>
        
        <tr><td class="data"> Drill Down Column </td>
            <td class="data"> <input type="text" name="DRILLDOWN_COLUMN" id="drilldownColumn" size="3" maxlength="2" value="<%=qp.getDrilldownColumn()%>">

                <% help="If this query is used as a drill down query, enter the column number of the parent query from which this parameter will get its value. Column numbering starts from 1." +
               "\\n\\nIf the parent query is a graph, the parameter will get the following values.\\nDrill down column 1 = data value\\nDrill down column 2 = category name" +
               "\\nDrill down column 3 = series name";
                %>

                <input type="button" class="buttonup" onclick="javascript:alert('<%=help%>')" value="?" onMouseOver="javascript:btndn(this);" onMouseOut="javascript:btnup(this);" />
            </td>
        </tr>
		
		<tr><td class="data"> Direct Substitution</td>
            <td class="data">
                <select name="DIRECT_SUBSTITUTION" id="directSubstitution" size="1">
                    <option value="N" <%=("N".equals(qp.getDirectSubstitution())? "selected" : "")%>  >No</option>
					<option value="Y" <%=("Y".equals(qp.getDirectSubstitution())? "selected" : "")%>  >Yes</option>
                </select>
				
				<% help="** WARNING ** \\n\\nUsing direct substitution offers no sql escaping for parameter values, " +
               "and therefore increases the risk of sql injection attacks on the target database.";
                %>

                <input type="button" class="buttonup" onclick="javascript:alert('<%=help%>')" value="?" onMouseOver="javascript:btndn(this);" onMouseOut="javascript:btnup(this);" />
			</td>
        </tr>
       
        <tr>
            <td><input type="submit" value="Submit"></td>
            <td></td>
        </tr>

    </table>
</form>
			
<script language="javascript" type="text/javascript">
    onTypeSelection();
</script>

<%@ include file ="/user/footer.jsp" %>

