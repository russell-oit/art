<%@ page import="java.sql.*,art.utils.*;" %>
<%@ page contentType="text/html; charset=UTF-8" %>
<%  request.setCharacterEncoding("UTF-8"); %>

<%
 boolean DELETE = request.getParameter("RULEACTION").equals("DELETE");
 int queryId = Integer.parseInt(request.getParameter("QUERY_ID"));
 
 Connection conn = (Connection) session.getAttribute("SessionConn");
 if ( conn == null || conn.isClosed()) {
    %>
       <jsp:forward page="error.jsp">
		<jsp:param name="MOD" value="Execute Update Rule"/>
		<jsp:param name="ACT" value="Get connection from session"/>
		<jsp:param name="MSG" value="Database connection not valid. Please log in again"/>
		<jsp:param name="NUM" value="100"/>
       </jsp:forward>
    <%
 }


 try {
	boolean actionSuccessful=true;
	Rule ar=new Rule();

    if (DELETE) {
        String[] values = request.getParameterValues("QUERY_RULES");

	    for(int i=0; i<  values.length; i++) {
			actionSuccessful=ar.deleteQueryRule(conn,queryId,values[i]);
	    }

    } else { //NEW
		ar.setQueryId(queryId);
		ar.setFieldName(request.getParameter("FIELD_NAME"));
		ar.setRuleName(request.getParameter("RULE_NAME"));
        actionSuccessful=ar.insertQueryRule(conn);
    }

	if(actionSuccessful){
		conn.commit();
		%>
		   <jsp:forward page="manageQueryRules.jsp">
			<jsp:param name="QUERY_ID" value="<%= queryId %>"/>
		   </jsp:forward>
		<%
	} else {
		//rollback and go to error page
		conn.rollback();
		%>
	   <jsp:forward page="error.jsp">
			<jsp:param name="MOD" value="Execute Update Rule"/>
			<jsp:param name="ACT" value="Modify Query Rules"/>
			<jsp:param name="MSG" value="Unsuccessful NEW or DELETE"/>
			<jsp:param name="NUM" value="200"/>
	   </jsp:forward>
		<%
	}
 } catch(Exception e) {
    conn.rollback();
     // revert to page error
    String act = "Generic Exception "+ e.toString();
%>
   <jsp:forward page="error.jsp">
	    <jsp:param name="MOD" value="Execute Update Rule"/>
	    <jsp:param name="ACT" value="Modify Query Rule"/>
	    <jsp:param name="MSG" value="<%= act %>"/>
	    <jsp:param name="NUM" value="199"/>
   </jsp:forward>
<%
 }
%>
