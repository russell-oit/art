<%@ page contentType="text/html; charset=UTF-8" %>
<%  request.setCharacterEncoding("UTF-8"); %>

<%	
 java.util.ResourceBundle messages = java.util.ResourceBundle.getBundle("art.i18n.ArtMessages",request.getLocale());
%>

<html>
    <head>
        <meta http-equiv="expires" content="0">
        <meta http-equiv="pragma" content="no-cache">
        <meta http-equiv="cache-control" content="no-cache">
        <title>ART</title>
        <script type="text/javascript" src="<%= request.getContextPath() %>/js/prototype.js"></script>
        <script type="text/javascript" src="<%= request.getContextPath() %>/js/scriptaculous/scriptaculous.js"></script>
        <script type="text/javascript" src="<%= request.getContextPath() %>/js/ajaxtags.js"></script>
        <script type="text/javascript" src="<%= request.getContextPath() %>/js/art.js"></script>
        <script type="text/javascript" src="<%= request.getContextPath() %>/js/jquery.js"></script>
        <script type="text/javascript" src="<%= request.getContextPath() %>/js/jquery.form.js"></script>
	<script> var $jQuery = jQuery.noConflict();</script>

        <link rel="stylesheet" type="text/css" href="<%= request.getContextPath() %>/css/art.css">
        <link rel="stylesheet" type="text/css" href="<%= request.getContextPath() %>/css/ajaxtags-art.css" />
    </head>
    <body>
        <table width="100%"  class="art" cellpadding="0" cellspacing="0">
            <tr>
                <td align="left" class="attr" >
                    <a href="<%= request.getContextPath() %>/admin/adminConsole.jsp">Admin Console</a>

                    <% if (session.getAttribute("username") != null) {%>

                    :: <a href="<%= request.getContextPath() %>/user/showGroups.jsp">Start Page</a>
                    <% } %>

                    :: <a href="<%= request.getContextPath() %>/logOff.jsp">Log Off</a>
                    <img src="<%= request.getContextPath() %>/images/vertical_16px.gif">
                </td>
                <td class="attr" align="right" width="50%">

                    <span id="systemWorking" style="display: none">
                        <img src="<%= request.getContextPath() %>/images/spinner.gif">
                    </span>
                    <img src="<%= request.getContextPath() %>/images/vertical_16px.gif">

                    <% if (session.getAttribute("ue") != null) {
					art.utils.UserEntity ue = ( art.utils.UserEntity) session.getAttribute("ue");%>
                    <%= ue.getUsername()%>
                    :: Logged in at <%=java.text.DateFormat.getTimeInstance(java.text.DateFormat.SHORT,request.getLocale()).format(ue.getLoginDate())%>
                    <% } %>
                </td>
            </tr>
        </table>

        <hr style="width:100%;height:2px">

