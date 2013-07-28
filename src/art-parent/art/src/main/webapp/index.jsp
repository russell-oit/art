<%@ page import="art.servlets.ArtDBCP;" %>

<%@ page contentType="text/html; charset=UTF-8" %>
<%@ page pageEncoding="UTF-8" %>

<%	
 java.util.ResourceBundle messages = java.util.ResourceBundle.getBundle("art.i18n.ArtMessages",request.getLocale());

 if (!ArtDBCP.isArtSettingsLoaded()) {
    // settings not defined: 1st Logon -> go to adminConsole.jsp (passing through the AuthFilterAdmin)
    response.sendRedirect(response.encodeRedirectURL(request.getContextPath() +"/admin/adminConsole.jsp"));
    return; 
 } else {
        String toPage = ArtDBCP.getArtSetting("index_page_default");
        if (toPage != null && !toPage.equals("default") ) {
        toPage  = toPage + ".jsp";
%>
<jsp:forward page="<%=toPage%>"/>
<%
return;
}	
}

%>
<html>
    <head>
        <link rel="stylesheet" href="css/art.css">
        <title>ART - Login</title>

        <script type="text/javascript">
            <!-- Begin
            function Start(page) {
                OpenWin = this.open(page, "CtrlWindow", "toolbar=yes,menubar=no,location=no,statusbar=no,scrollbars=yes,resizable=yes,width=800,height=600");
            }
            // End -->
        </script>
    </head>
    <body>
        <hr style="width:100%;height:2px">

        <div>
            <br>
            <table align="center" style="width:400px"  class="art">
                <tr>
                    <td class="supertitle" align="center">
                        <br> <img height="70" src="<%= request.getContextPath() %>/images/art-64px.jpg" alt="ART">
                        <br><br> <span style="font-size:180%"><b>ART</b></span> <br><br>
                    </td>
                </tr>
                <tr>
                    <td class="link">  <a href="login.jsp"> <%=messages.getString("login")%></a>   </td>
                </tr>
                <tr>
                    <td class="link">  <a href="mobile/index.jsp"> <%=messages.getString("login")%> <small><i>(mobile devices, micro-browsers)</i></small></a>  </td>
                </tr>
                <tr>
                    <td class="link">  <a href="http://art.sourceforge.net">Web site</a>	</td>
                </tr>
                <tr>
                    <td class="action">  &nbsp;	</td>
                </tr>
            </table>
        </div>

        <div>
            <br>
            <hr style="width:100%;height:2px">
        </div>
    </body>
</html>
