<%@ page import="java.util.ResourceBundle, art.servlets.ArtDBCP,art.utils.*,java.sql.Connection" %>

<jsp:useBean id="ue" scope="session" class="art.utils.UserEntity" />

<% response.setHeader("Cache-control","no-cache"); %>
<%@ include file="header.jsp" %>

<%
try {

String username=ue.getUsername();
int queryId=0;
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
		<jsp:param name="MOD" value="Show Text"/>
		<jsp:param name="ACT" value="Get connection"/>
		<jsp:param name="MSG" value="Database connection not valid. Please log in again"/>
		<jsp:param name="NUM" value="100"/>
       </jsp:forward>
    <%   
 }

ArtQuery aq=new ArtQuery();
boolean queryExists=aq.create(conn,queryId);
conn.close();
if(!queryExists){	
	%>
   <jsp:forward page="error.jsp">
	    <jsp:param name="MOD" value="Show Text"/>
	    <jsp:param name="ACT" value="Getting text"/>
	    <jsp:param name="MSG" value="Record does not exist"/>
	    <jsp:param name="NUM" value="130"/>
   </jsp:forward>
   <%
}

ArtDBCP.log(username, "query", request.getRemoteAddr(), queryId, 0, 0, "text");

String queryText;
int queryType=aq.getQueryType();
if(queryType==111){
	queryText=aq.getText();
} else {
	//not a text query query
	queryText="Query ID " + queryId + " is not a text query";
}
%>

<%=queryText%>

<%
PreparedQuery pq=new PreparedQuery();
boolean canEdit=pq.canEditTextObject(username,queryId);
if (canEdit && request.getParameter("_mobile") == null ) { %>
	  <hr>
  <div align="right">
   <span style="font-size:75%">
	<a href="javascript:startInNewWindow('<%= request.getContextPath() %>/user/editText.jsp?queryId=<%=queryId%>')">
	   <img src="<%= request.getContextPath() %>/images/edit-10px.png" alt="Edit" title="Edit Content" border="0">
	</a>  
   </span>
  </div>
<% }

} catch (Exception e) { %>
 <%=messages.getString("exception")%> <%=e%>
<%}%>

<%@ include file="footer.jsp" %>
