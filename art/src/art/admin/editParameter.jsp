<%@ page import="java.sql.*,java.util.*,art.utils.*;" %>
<%@ include file ="headerAdmin.jsp" %>

<%
 boolean MODIFY = request.getParameter("PARAMACTION").equals("MODIFY");
 boolean NEW = request.getParameter("PARAMACTION").equals("NEW");

 int queryId = -1;
 int fieldPosition = -1;
 String type="";

 if (request.getParameter("QUERY_ID") != null) {
    queryId = Integer.parseInt(request.getParameter("QUERY_ID"));
 }
 if (MODIFY && request.getParameter("FIELD_POSITION") != null) {
    String s= request.getParameter("FIELD_POSITION");
    type = s.substring(s.indexOf("_")+1); // can be BIND, MULTI or INLINE
    fieldPosition = Integer.parseInt(s.substring(0,s.indexOf("_")));
 } else if (NEW) {
    type = request.getParameter("NEWTYPE");
 }

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

ArtQueryParam qp = new ArtQueryParam();
if (MODIFY) {
	qp.create(conn, queryId, fieldPosition);
}

String help;

%>

<form action="execEditParameter.jsp" method="post">
    <input type="hidden" name="QUERY_ID" value="<%= request.getParameter("QUERY_ID")%>">
    <input type="hidden" name="PARAMACTION" value="<%= request.getParameter("PARAMACTION")%>">
    <%
    if (MODIFY) {
    %>
    <input type="hidden" name="FIELD_POSITION" value="<%= fieldPosition%>">
    <%
    }
    %>
    <table align="center">
        <tr><td class="title" colspan="2" > Define Parameter </td></tr>

        <%
        if (type.equals("BIND")){
        %>
        <input type="hidden" name="PARAM_TYPE" value="N">
        <tr><td class="title" colspan="2" ><i>Bind  Parameter</i><br>
            </td>
        <tr><td class="attr" colspan="2" >
                <small><span style="color:red"><b>Note:</b></span><br>
                    <i><b>Bind</b> parameters are not supported.
                        Please use <b>Inline</b> parameters instead.</i></small>
            </td></tr>

        <tr><td class="data" >Available bind parameters</td>
            <td class="data">
                <select name="BIND_POSITION" size="4" >
                    <%
					qp.prepareBindParams(conn,queryId);
					int i = 0;
					int numOfBind = qp.getNumberOfBindsInSQL();
                    int actualBindPosition = qp.getBindPosition();
                    for(i=1; i<=numOfBind;i++) {
                      if (qp.isBindPositionFree(i) || (actualBindPosition==i) ){
                    %>
                    <option value="<%=i%>" <%=((actualBindPosition==i)?"SELECTED":"")%>>
                        <%=qp.getStringAroundBind(i).replace('<','-').replace('>','-')%>
                    </option>
                    <%
                       }
                    }
                    %>
                </select>
            </td>
        </tr>
        <%
        } else if (type.equals("MULTI")) {
        %>
        <input type="hidden" name="PARAM_TYPE" value="M">
        <tr><td class="title" colspan="2" ><i>Multi Parameter</i></td></tr>
        <tr><td class="data">Parameter Label</td>
            <td class="data"> <input type="text" name="PARAM_LABEL" size="40" maxlength="55" value="<%=qp.getParamLabel()%>"></td>
        </tr>
        <%
        } else { // INLINE
        %>
        <input type="hidden" name="PARAM_TYPE" value="I">
        <tr><td class="title" colspan="2" ><i>Inline Parameter</i></td></tr>
        <tr><td class="data"> Parameter Label <br><small>(without #, case sensitive)</small></td>
            <td class="data"> <input type="text" name="PARAM_LABEL" size="40" maxlength="55" value="<%=qp.getParamLabel()%>"></td>
        </tr>
        <%
        }
        %>

        <tr><td class="data"> Name (User Viewable) </td>
            <td class="data"> <input type="text" name="NAME" size="25" maxlength="25" value="<%=qp.getName()%>"> </td>
        </tr>
        <tr><td class="data"> Short Description </td>
            <td class="data"> <input type="text" name="SHORT_DESCRIPTION" size="40" maxlength="40" value="<%=qp.getShortDescription()%>"> </td>
        </tr>

        <tr><td class="data"> Help Description </td>
            <td class="data"> <input type="text" name="DESCRIPTION" size="40" maxlength="120" value="<%=qp.getDescription()%>"> </td>
        </tr>
        <%
        if (type.equals("INLINE")){           
        %>

        <tr><td class="data"> Data Type </td><td class="data">
                <select name="FIELD_CLASS" size="1">
                    <option value="VARCHAR" <%=("VARCHAR".equals(qp.getFieldClass())?"selected":"")%>>VARCHAR</option>
                    <option value="TEXT"    <%=("TEXT".equals(qp.getFieldClass())?"selected":"") %>>TEXT</option>
                    <option value="INTEGER" <%=("INTEGER".equals(qp.getFieldClass())?"selected":"")%>>INTEGER</option>
                    <option value="NUMBER"  <%=("NUMBER".equals(qp.getFieldClass())?"selected":"") %>>NUMBER</option>
                    <option value="DATE"    <%=("DATE".equals(qp.getFieldClass())?"selected":"") %>>DATE</option>
                    <option value="DATETIME" <%=("DATETIME".equals(qp.getFieldClass())?"selected":"") %>>DATETIME</option>
                </select>
            </td>
        </tr>
		<%
        } else if(type.equals("MULTI")) { //multi parameters are treated as either VARCHAR or NUMBER
		%>
		<tr><td class="data"> Data Type </td><td class="data">
                <select name="FIELD_CLASS" size="1">
                    <option value="VARCHAR" <%=("VARCHAR".equals(qp.getFieldClass())?"selected":"")%>>VARCHAR</option>                    
                    <option value="NUMBER"  <%=("NUMBER".equals(qp.getFieldClass())?"selected":"") %>>NUMBER</option>                    
                </select>
            </td>
        </tr>
		<%
		}
        %>

        <tr><td class="data"> Default Value
                </td>
				<%
				String defaultValue=qp.getDefaultValue();
				if(defaultValue==null){
					defaultValue="";
				}
				%>
            <td class="data"> <input type="text" name="DEFAULT_VALUE" size="25" maxlength="80" value="<%=defaultValue%>">
				<%
				if(type.equals("INLINE")){
					%>
					<input type="button" class="buttonup" onclick="javascript:alert('Default DATE values\n\n1. For DATE parameters, use YYYY-MM-DD format (e.g. 2012-01-01) to set a specific default date.\n\n2. For DATETIME parameters, use YYYY-MM-DD HH:mm or YYYY-MM-DD HH:mm:ss  (e.g. 2012-01-30 23:15 or 2012-01-30 23:15:45).\n\n3. Leave blank to set the default date to the current date - i.e. when the query is executed.\n\n4. Use the following syntax to set an offset from current date:\n ADD DAYS|MONTHS|YEARS <number> \n e.g. ADD DAYS -5 -> set the date to 5 days in the past')" value="?" onMouseOver="javascript:btndn(this);" onMouseOut="javascript:btnup(this);">
				<%
					}
				%>

            </td>
        </tr>

        <tr><td class="data"> Use List Of Values (LOV) <br>
                <small>(Set to Yes if the parameter <br> should be picked from a list)</small>
            </td>
            <td class="data">
                <select name="USE_LOV" size="1">
					<%
					if (!type.equals("MULTI")){ //default to No
						%>
                    <option value="N" <%=("N".equals(qp.getUseLov())?"selected":"")%>>No</option>
					<option value="Y" <%=("Y".equals(qp.getUseLov())?"selected":"")%>>Yes</option>
					<% } else { //multi parameter. default to Yes
					%>
					<option value="Y" <%=("Y".equals(qp.getUseLov())?"selected":"")%>>Yes</option>
					<option value="N" <%=("N".equals(qp.getUseLov())?"selected":"")%>>No</option>

					<%
					}
					%>
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

					Map lovs=aq.getLovQueries();
					Iterator it = lovs.entrySet().iterator();
					while(it.hasNext()){
						Map.Entry entry = (Map.Entry)it.next();
						Integer integerId=(Integer)entry.getKey();
						id=integerId.intValue();
						name=(String)entry.getValue();
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

        <%
        if (type.equals("INLINE")){
             //display drilldown column option
        %>
        <tr><td class="data"> Drill Down Column </td>
            <td class="data"> <input type="text" name="DRILLDOWN_COLUMN" size="3" maxlength="2" value="<%=qp.getDrilldownColumn()%>">

                <% help="If this query is used as a drill down query, enter the column number of the parent query from which this parameter will get its value. Column numbering starts from 1." +
               "\\n\\nIf the parent query is a graph, the parameter will get the following values.\\nDrill down column 1 = data value\\nDrill down column 2 = category name" +
               "\\nDrill down column 3 = series name";
                %>

                <input type="button" class="buttonup" onclick="javascript:alert('<%=help%>')" value="?" onMouseOver="javascript:btndn(this);" onMouseOut="javascript:btnup(this);" />
            </td>
        </tr>
        <%}%>

        <tr>
            <td><input type="submit" value="Submit"></td>
            <td></td>
        </tr>

    </table>
</form>

<%@ include file ="footer.html" %>

