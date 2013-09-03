<%@ page import="java.util.ResourceBundle, art.servlets.ArtConfig;" %>
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ page pageEncoding="UTF-8" %>
<%   response.setHeader("Cache-control","no-cache"); %>
<!DOCTYPE html>
<html>
<head>
 <meta http-equiv="expires" content="0">
 <meta http-equiv="pragma" content="no-cache">
 <meta http-equiv="cache-control" content="no-cache, must-revalidate">
 <link rel="stylesheet" type="text/css" href="<%=request.getContextPath()%>/css/art.css">
 <title>ART</title>
</head>
<body>
<%

 ResourceBundle messages = ResourceBundle.getBundle("i18n.ArtMessages",request.getLocale());
	    
 /* If this page is called by an Admin session, let's
    close the dedicated connection used by the admin who is logging off 
  */
 if (session.getAttribute("SessionConn") != null) {
     java.sql.Connection conn = (java.sql.Connection) session.getAttribute("SessionConn");
     conn.close();     
 }

 /* Invalidate the session */
 session.invalidate();
 
 //display mobile login page if logging off from mobile version
 if(request.getParameter("_mobile")!=null){
	response.sendRedirect(request.getContextPath()+"/mobile/index.jsp");
	return;
}

%>

<table style="width:100%">
 <tr>
  <td align="left" class="attr">
   <a href="<%=request.getContextPath()%>"><%=messages.getString("login")%></a>
  </td>
 </tr>
</table>

<p>
<table class="centerTableAuto">
 <tr>
  <td class="attr" align="center">
              <%=messages.getString("sessionClosed")%>
    <span style="font-size:135%">
             <br><br> <%=messages.getString("thanksForUsing")%>
    </span>
  </td>
 </tr>
</table>
</p>
<%@ include file ="/user/footer.jsp" %>


