<%@ page import="java.util.ResourceBundle,art.servlets.ArtDBCP" %>

<jsp:useBean id="ue" scope="session" class="art.utils.UserEntity" />

<%@ include file ="header.jsp" %>


<c:if test="${param.action == 'execute'}">
    <jsp:forward page="ExecuteQuery"/>
</c:if>


<div id="params">

    <form name="viewmodeForm" method="post">
        <input type="hidden" name="action" value="execute">

        <table class="art" align="center">
            <tr>
                <td colspan="2" class="title" >
                    <br> <b><%=messages.getString("selectViewMode")%></b> <br><br>
                </td>
            </tr>

            <tr>
                <td class="attr">
                    <span style="font-size:95%"><i><%=messages.getString("viewMode")%></i></span>
                    <select name="viewMode" size="1">
                        <%
                        java.util.Iterator itVm = ArtDBCP.getUserViewModes().iterator();
                        while(itVm.hasNext()) {
                           String viewMode = (String) itVm.next();
                        %>
                        <option value="<%=viewMode%>"> <%=messages.getString(viewMode)%> </option>
                        <% } %>

                    </select>
                </td>

                <td class="data">
                    <div align="center" valign="middle">
                        <input type="submit" class="buttonup"  style="width:100px;" value="<%=messages.getString("executeQueryButton")%>">
                    </div>
                </td>
            </tr>
        </table>
    </form>
</div>

<%@ include file ="footer.jsp" %>