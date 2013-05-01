<%@ page contentType="text/html; charset=UTF-8" %>
<%@ page import="java.util.ResourceBundle,java.sql.Connection,art.utils.*,art.servlets.ArtDBCP" %>
<%  request.setCharacterEncoding("UTF-8");  %>

<jsp:useBean id="ue" scope="session" class="art.utils.UserEntity" />

<%
java.util.ResourceBundle messages = java.util.ResourceBundle.getBundle("art.i18n.ArtMessages",request.getLocale());

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

ArtQuery aq=new ArtQuery();
boolean queryExists=aq.create(queryId,false);
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
boolean canEdit=pq.canEditTextQuery(ue.getUsername(),queryId);
 if (canEdit || ue.getAccessLevel() >= 30 ) {
	aq.setShortDescription(description);
	aq.setText(text);
    aq.update();
     %>
    <jsp:forward page="editText.jsp">
       <jsp:param name="queryId" value="<%=queryId%>"/>
       <jsp:param name="justSaved" value="Text Updated"/>
    </jsp:forward>

<% } else { %>
	<%=messages.getString("noRightsToEditQuery")%> 
<% } %>


