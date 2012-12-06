<%@ page contentType="text/html; charset=UTF-8" %>
<%@ page import="java.util.ResourceBundle,java.sql.Connection,art.utils.*,art.servlets.ArtDBCP" %>
<%  request.setCharacterEncoding("UTF-8");  %>

<jsp:useBean id="ue" scope="session" class="art.utils.UserEntity" />

<%
java.util.ResourceBundle messages = java.util.ResourceBundle.getBundle("art.i18n.ArtMessages",request.getLocale());

try{

int queryId=0;
String queryIdString=request.getParameter("queryId");
String objectId=request.getParameter("objectId");
if(queryIdString!=null){
	queryId=Integer.parseInt(queryIdString);
} else if(objectId!=null){
	queryId=Integer.parseInt(objectId);
}
String description=request.getParameter("description");
String text=request.getParameter("text");

Connection conn = ArtDBCP.getConnection();
if ( conn == null || conn.isClosed()) {
    %>
       <jsp:forward page="error.jsp">
		<jsp:param name="MOD" value="Exec Edit Text"/>
		<jsp:param name="ACT" value="Get connection"/>
		<jsp:param name="MSG" value="Database connection not valid. Please log in again"/>
		<jsp:param name="NUM" value="100"/>
       </jsp:forward>
    <%   
 }

ArtQuery aq=new ArtQuery();
boolean queryExists=aq.create(conn,queryId);
if(!queryExists){	
	%>
   <jsp:forward page="error.jsp">
	    <jsp:param name="MOD" value="Exec Edit Text"/>
	    <jsp:param name="ACT" value="Getting text"/>
	    <jsp:param name="MSG" value="Record does not exist"/>
	    <jsp:param name="NUM" value="130"/>
   </jsp:forward>
   <%
}

PreparedQuery pq=new PreparedQuery();
boolean canEdit=pq.canEditTextObject(ue.getUsername(),queryId);
 if (canEdit || ue.getAccessLevel() >= 10 ) {
	aq.setShortDescription(description);
	aq.setText(text);
    aq.update(conn);
	conn.close();    
     %>
    <jsp:forward page="editText.jsp">
       <jsp:param name="queryId" value="<%=queryId%>"/>
       <jsp:param name="justSaved" value="Text Updated"/>
    </jsp:forward>

<% } else { %>  <%=messages.getString("noRightsToEditQuery")%> <% } %>

<%
conn.close();
} catch (Exception e) { 
%> <%=messages.getString("exception")%> <%=e%>
<%
  }
 %>
