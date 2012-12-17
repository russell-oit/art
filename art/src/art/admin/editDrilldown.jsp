<%@ page import="art.utils.*,java.util.*,art.servlets.ArtDBCP" %>
<%@ include file ="headerAdmin.jsp" %>

<%
 
boolean MODIFY = request.getParameter("DRILLDOWN_ACTION").equals("MODIFY");
boolean NEW = request.getParameter("DRILLDOWN_ACTION").equals("NEW");

int queryId;
int queryPosition = -1;
int drilldownQueryId = -1;

queryId = Integer.parseInt(request.getParameter("QUERY_ID"));

if (MODIFY && request.getParameter("DRILLDOWN_QUERY_POSITION") != null) {
	String s= request.getParameter("DRILLDOWN_QUERY_POSITION");    
	queryPosition = Integer.parseInt(s.substring(0,s.indexOf("_")));
	drilldownQueryId = Integer.parseInt(s.substring(s.indexOf("_")+1));
} 
    
String value;

DrilldownQuery dq=new DrilldownQuery();
dq.create(queryId,queryPosition);
  
%>

<form action="execEditDrilldown.jsp" method="post">
    <input type="hidden" name="QUERY_ID" value="<%=queryId%>">
    <input type="hidden" name="DRILLDOWN_ACTION" value="<%= request.getParameter("DRILLDOWN_ACTION")%>">

    <%
    if (MODIFY) {	
    %>
    <input type="hidden" name="DRILLDOWN_QUERY_POSITION" value="<%=queryPosition%>">
    <%
    }
    %>

    <table align="center">
        <tr><td class="title" colspan="2" > Select Drill Down Query </td></tr>
        <tr><td class="data"> Drill Down Query </td>
            <td class="data">
                <select name="DRILLDOWN_QUERY_ID" size="1">
                    <%
                    ArtQuery aq=new ArtQuery();
                    Map<String, Integer> drilldowns=aq.getAllDrilldownQueries();
                    Integer candidateId; //query id of candidate drill down query
                    String candidateName; //query name of candidate drill down query
                    for (Map.Entry<String, Integer> entry : drilldowns.entrySet()) {
                         candidateId=entry.getValue();
                         candidateName=entry.getKey();
                    %>
                    <option value="<%=candidateId%>" <%=(drilldownQueryId == candidateId)?"SELECTED":"" %> >
                        <%=candidateName %> - <%=candidateId%>
                    </option>
                    <%
                   }
                    %>
                </select>
                <input type="button" class="buttonup" onclick="javascript:alert('A drill down query should have at least one inline parameter where drill down column > 0.')" value="?" onMouseOver="javascript:btndn(this);" onMouseOut="javascript:btnup(this);">
            </td>
        </tr>

        <tr><td class="data"> Drill Down Title </td>
            <td class="data">
                <%
					value=(dq.getDrilldownTitle()==null?"":dq.getDrilldownTitle());                
                %>
                <input type="text" name="DRILLDOWN_TITLE" size="30" maxlength="30" value="<%=value%>">                
                <input type="button" class="buttonup" onclick="javascript:alert('The header text of the drill down query column. If blank, the drill down query\'s name will be used.')" value="?" onMouseOver="javascript:btndn(this);" onMouseOut="javascript:btnup(this);">
            </td>
        </tr>

        <tr><td class="data"> Drill Down Text </td>
            <td class="data">
				<%
					value=(dq.getDrilldownText()==null?"":dq.getDrilldownText());                
                %>              
                <input type="text" name="DRILLDOWN_TEXT" size="30" maxlength="30" value="<%=value%>">                
                <input type="button" class="buttonup" onclick="javascript:alert('The hyperlink text for the drill down query.')" value="?" onMouseOver="javascript:btndn(this);" onMouseOut="javascript:btnup(this);">
            </td>
        </tr>

        <tr><td class="data"> View Mode </td>
            <td class="data">
                <select name="OUTPUT_FORMAT" size="1">
					<%
						value=dq.getOutputFormat();                
					%>
                    
                    <option value="ALL" <%="ALL".equals(value)?"selected":""%> > All </option>
					<option value="default" <%="default".equals(value)?"selected":""%> > Default </option>
                    <%
                       List<String> viewModes = ArtDBCP.getUserViewModes();
                       for(String viewMode : viewModes){
						%>
						<option value="<%=viewMode%>" <%=viewMode.equals(value)?"selected":""%> > <%=messages.getString(viewMode)%> </option>
						<% } %>

                    <option value="graph" <%="graph".equals(value)?"selected":""%> > <%=messages.getString("htmlGraph")%> </option>
                    <option value="pdfgraph" <%="pdfgraph".equals(value)?"selected":""%> > <%=messages.getString("pdfGraph")%> </option>
                    <option value="pnggraph" <%="pnggraph".equals(value)?"selected":""%> > <%=messages.getString("pngGraph")%> </option>                                        
                </select>
                <input type="button" class="buttonup" onclick="javascript:alert('The output format of the drill down query. If All is selected, the user will be able to select an output format before the drill down query runs')" value="?" onMouseOver="javascript:btndn(this);" onMouseOut="javascript:btnup(this);">
            </td>
        </tr>

        <tr><td class="data"> Open In New Window </td>
            <td class="data">
                <select name="OPEN_IN_NEW_WINDOW" size="1">
					<%
						value=(dq.getOpenInNewWindow()==null?"Y":dq.getOpenInNewWindow());                
					%>                    
                    <option value="N" <%="N".equals(value)?"selected":""%> > No </option>
                    <option value="Y" <%="Y".equals(value)?"selected":""%> > Yes </option>                    
                </select>
                <input type="button" class="buttonup" onclick="javascript:alert('If set to Yes, the drill down query will open in a new browser window. If set to No, the drill down query will open in the same window as the main query.')" value="?" onMouseOver="javascript:btndn(this);" onMouseOut="javascript:btnup(this);">
            </td>
        </tr>

        <tr>
            <td><input type="submit" value="Submit"></td>
            <td></td>
        </tr>

    </table>
</form> 


<%@ include file ="/user/footer.jsp" %>

