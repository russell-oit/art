<%@ page contentType="text/html; charset=UTF-8" %>
<% request.setCharacterEncoding("UTF-8"); %>

<%@ include file ="headerAdminPlain.jsp" %>

<%
 String mod = request.getParameter("MOD");
 String act = request.getParameter("ACT");
 String msg = request.getParameter("MSG");
 String num = request.getParameter("NUM");
%>

<table align="center">
    <tr>
        <td colspan="2" class="data" align="center"> <span style="color:red"> <b>Error! </b></span>
        </td>
    </tr>
    <tr>
        <td class="attr"> Message:
        </td>
        <td class="data"> <%=msg%>
        </td>
    </tr>
    <tr>
        <td class="attr"> Error Code:
        </td>
        <td class="data"> <%=num%> &nbsp; <a href="errorCodes.jsp">See Error Codes</a>
        </td>
    </tr>
    <tr>
        <td class="attr"> Module:
        </td>
        <td class="data"> <%=mod%>
        </td>
    </tr>
    <tr>
        <td class="attr"> Action:
        </td>
        <td class="data"> <%=act%>
        </td>
    </tr>
</table>
<p>
<table align="center">
    <tr><td>
            <%
             if ( num.equals("100") ) {
              session.invalidate();
            %>
            <a href="<%= request.getContextPath() %>"><small>&nbsp;Login Page&nbsp;</small></a>
            <%
            } else {
            %>
            <button type="button" name="back"  onclick="javascript:history.back()"> Back </button>
            <%
            }
            %>
        </td></tr>
</table>
</p>
<%@ include file ="/user/footer.jsp" %>

