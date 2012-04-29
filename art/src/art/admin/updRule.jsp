<%@ page import="java.sql.*,art.utils.*;" %>
<%@ include file ="headerAdmin.jsp" %>
<%
 
 boolean NEW = request.getParameter("RULEACTION").equals("NEW");
 int queryId = -1;
 if (request.getParameter("QUERY_ID") != null) {
    queryId = Integer.parseInt(request.getParameter("QUERY_ID"));
 }
 
%>
<form action="execUpdRule.jsp" method="post">
    <input type="hidden" name="QUERY_ID" value="<%= request.getParameter("QUERY_ID")%>">
    <input type="hidden" name="RULEACTION" value="<%= request.getParameter("RULEACTION")%>">
    <table align="center">
        <%
        if (NEW) {
        %>
        <input type="hidden" name="RULE_NAME" value="<%=request.getParameter("RULE_NAME")%>">
        <tr><td class="title" colspan="2" ><i>Add Rule</i></td></tr>

        <tr><td class="data"> Rule Name </td>
            <td class="data"> <%=request.getParameter("RULE_NAME")%>
            </td>
        </tr>
        <tr><td class="data"> Column Name </td>
            <td class="data"> <input type="text" name="FIELD_NAME" size="40" maxlength="40">
            </td>
        </tr>

        <tr>
            <td><input type="submit" value="Submit"></td>
            <td></td>
        </tr>

    </table>
</form> 
<%
 } else {
%> 
error
<%
 }
%> 

<%@ include file ="footer.html" %>

