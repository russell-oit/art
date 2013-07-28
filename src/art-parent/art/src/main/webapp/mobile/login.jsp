<%@ page pageEncoding="UTF-8" %>
<%@ page import=" art.servlets.ArtDBCP;" %>
<%@ page contentType="text/html; charset=UTF-8" %>
<%
  /* Invalidate the current session to allow a new login */
  session.invalidate();
  java.util.ResourceBundle messages = java.util.ResourceBundle.getBundle("art.i18n.ArtMessages",request.getLocale());
%>
<html>
    <head>
        <title>ART - Login</title>
        <meta http-equiv="pragma" content="no-cache">
        <meta http-equiv="cache-control" content="no-cache, must-revalidate">
        <link rel="stylesheet" type="text/css" href="<%= request.getContextPath() %>/css/art.css">
    </head>
    <body onload="javascript:document.getElementById('username').focus();">
        <table cellSpacing="1" cellPadding="5" style="width:300px; height:300px" border="0" class="art" valign="middle" align="center">
            <tr vAlign="center" align="middle">
                <td class="title" colspan="2" align="left"><img height="70" src="<%= request.getContextPath() %>/images/art-64px.jpg"></td>
            </tr>
            <tr vAlign="center" align="middle">
                <td colSpan="2"> <img height="64" src="<%= request.getContextPath() %>/images/users-64px.jpg" width="64" align="absMiddle" border="0"> &nbsp;
                    <span style="font-size:180%"><b>ART</b><i>mobile</i></span>
                </td>
            </tr>
            <form name="login" method="post" action="<%= request.getContextPath() %>/user/mshowGroups.jsp">
                <input type="hidden" name="_mobile" value="true">
                <% String msg = (String) request.getAttribute("message");
                   if (msg != null) {
                %>
                <tr>
                    <td colspan="2" align="center">
                        <span style="color:red">
                            <%=msg%>
                        </span>
                    </td>
                </tr>
                <% } %>
                <tr>
                    <td vAlign="center" align="right" width="50%"><%=messages.getString("username")%></td>
                    <td vAlign="center" align="left" width="50%">
                        <input id="username" maxLength="15" size="16" name="username">
                    </td>
                </tr>
                <tr>
                    <td vAlign="center" align="right" width="50%"><%=messages.getString("password")%></td>
                    <td vAlign="center" align="left" width="50%">
                        <input id="password" type="password" maxLength="40" size="16" name="password">
                    </td>
                </tr>
                <tr>
                    <td vAlign="center" align="middle" colspan="2">
                        <input type="submit" class="buttonup" style="width:100px;" value="<%=messages.getString("login")%>"> </td>
                </tr>
            </form>
        </table>
    </body>
</html>
