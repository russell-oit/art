<%@ page import="java.util.ResourceBundle, art.servlets.ArtDBCP,art.utils.*,java.sql.Connection" %>
<%@ page contentType="text/html; charset=UTF-8" %>

<%	
 request.setCharacterEncoding("UTF-8"); 
 java.util.ResourceBundle messages = java.util.ResourceBundle.getBundle("art.i18n.ArtMessages",request.getLocale());
%>

<jsp:useBean id="ue" scope="session" class="art.utils.UserEntity" />

<html>
    <head>
        <title>ART</title>
        <link rel="stylesheet" type="text/css" href="<%= request.getContextPath() %>/css/art.css" />
        <link rel="stylesheet" type="text/css" href="<%= request.getContextPath() %>/css/ajaxtags-art.css" />
    </head>
    <body>
        <table  class="art" width="100%">
            <tr>
                <td class="attr" align="left" width="50%">
                    <%=messages.getString("textEditor")%>
                </td>
                <td class="attr" align="right" width="50%">
                    <%= ue.getUsername()%>
                    :: <%=messages.getString("loggedAt")%> <%=java.text.DateFormat.getTimeInstance(java.text.DateFormat.SHORT,request.getLocale()).format(ue.getLoginDate())%>
                </td>
            </tr>
        </table>

        <hr style="width:100%;height:2px">


<%
String queryText=""; 
int queryId=0;
ArtQuery aq=new ArtQuery();

  try {
  
  String msg = request.getParameter("message");  
String queryIdString=request.getParameter("queryId");
String objectId=request.getParameter("objectId");
if(queryIdString!=null){
	queryId=Integer.parseInt(queryIdString);
} else if(objectId!=null){
	queryId=Integer.parseInt(objectId);
}

Connection conn = ArtDBCP.getConnection();
if ( conn == null || conn.isClosed()) {
    %>
       <jsp:forward page="error.jsp">
		<jsp:param name="MOD" value="Edit Text"/>
		<jsp:param name="ACT" value="Get connection"/>
		<jsp:param name="MSG" value="Database connection not valid. Please log in again"/>
		<jsp:param name="NUM" value="100"/>
       </jsp:forward>
    <%   
 }

boolean queryExists=aq.create(conn,queryId);
conn.close();
if(!queryExists){
	%>
   <jsp:forward page="error.jsp">
	    <jsp:param name="MOD" value="Edit Text"/>
	    <jsp:param name="ACT" value="Getting text"/>
	    <jsp:param name="MSG" value="Record does not exist"/>
	    <jsp:param name="NUM" value="130"/>
   </jsp:forward>
   <%
}

int queryType=aq.getQueryType();
if(queryType==111){
	queryText=aq.getText();
} else {
	//not a text query query
	queryText="Query ID " + queryId + " is not a text query";
}
 
  } catch (Exception e) { 
%> Exception <%=e%> <%
  }
%>  

<script language="javascript" type="text/javascript" src="<%= request.getContextPath() %>/js/tiny_mce/tiny_mce.js"></script>

<script language="javascript" type="text/javascript">
    tinyMCE.init({
        mode : "exact",
        elements : "mceedit",
        theme : "advanced",
        theme_advanced_buttons1 : "bold,italic,underline,separator,justifyleft,justifycenter,justifyright, justifyfull,bullist,numlist,undo,redo,link,forecolor,image,hr,code",
        theme_advanced_buttons2 : "fontselect,fontsizeselect",
        theme_advanced_buttons3 : "",
        theme_advanced_toolbar_location : "top"	});
</script>

<div align="center">
    <form method="post" action="execEditText.jsp">
        <%=messages.getString("description")%> <input type="text" name="description" value="<%=aq.getShortDescription()%>" size="40" maxlength="254">
        <p>
            <textarea name="text" id="mceedit" cols="80" rows="25"><%=queryText%></textarea>
        </p>
        <input type="hidden" name="queryId" value="<%=queryId%>">
        <div align="center">
            <% if (request.getParameter("justSaved") != null) { %>
            <small>
                <%=messages.getString("savedAt")%> 
                <%= java.text.DateFormat.getDateTimeInstance(java.text.DateFormat.MEDIUM,java.text.DateFormat.MEDIUM,request.getLocale()).format(new java.util.Date())%>
            </small>
            <br>
            <% } %>
            <button type="submit" value="OK"> <%=messages.getString("save")%> </button>
            <button type="button" onClick="javascript:window.close()" value="Close"> <%=messages.getString("closeWin")%> </button>

        </div>
    </form>
</div>

<%@ include file="footer.jsp" %>
