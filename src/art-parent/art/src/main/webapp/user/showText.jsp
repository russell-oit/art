<%@ page import="java.util.ResourceBundle, art.servlets.ArtDBCP,art.utils.*,java.sql.Connection" %>

<jsp:useBean id="ue" scope="session" class="art.utils.UserEntity" />

<% response.setHeader("Cache-control","no-cache"); %>

<%
boolean isInline=false;
if(request.getParameter("_isInline")!=null){
	isInline=true;
}

if(!isInline){ 	%>	
	<%@ include file ="header.jsp" %>
<% }
%>

<%

String username=ue.getUsername();
int queryId=0;
String queryIdString=request.getParameter("queryId");
String objectId=request.getParameter("objectId");
if(queryIdString!=null){
	queryId=Integer.parseInt(queryIdString);
} else if(objectId!=null){
	queryId=Integer.parseInt(objectId);
}

ArtQuery aq=new ArtQuery();
boolean queryExists=aq.create(queryId,false);
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
	//not a text query
	queryText="Query ID " + queryId + " is not a text query";
}
%>

<div align="left">
<%=queryText%>

<%
PreparedQuery pq=new PreparedQuery();
boolean canEdit=pq.canEditTextQuery(username,queryId);
if (canEdit && request.getParameter("_mobile") == null ) { %>
	  <hr>
  <div align="right">
   <span style="font-size:75%">
	<a href="javascript:startInNewWindow('<%= request.getContextPath() %>/user/editText.jsp?queryId=<%=queryId%>')">
	   <img src="<%= request.getContextPath() %>/images/edit-10px.png" alt="Edit" title="Edit Content" border="0">
	</a>  
   </span>
  </div>
<% } %>

</div>

<%
if(!isInline){ 	%>	
	<%@ include file ="footer.jsp" %>
<% }
%>
