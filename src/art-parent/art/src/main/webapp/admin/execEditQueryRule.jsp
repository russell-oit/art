<%@ page import="java.sql.*,art.utils.*" %>
<%@ page import="org.apache.commons.lang.StringUtils" %>

<%@ page contentType="text/html; charset=UTF-8" %>
<%  request.setCharacterEncoding("UTF-8"); %>

<%
String action = request.getParameter("RULEACTION");
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

    if (StringUtils.equals(action, "DELETE")) {
        String[] values = request.getParameterValues("QUERY_RULES");

	    for(int i=0; i<  values.length; i++) {
			actionSuccessful=ar.deleteQueryRule(conn,queryId,values[i]);
	    }

    } else {
		//MODIFY or NEW
		ar.setQueryId(queryId);
		ar.setFieldName(request.getParameter("FIELD_NAME"));
		ar.setRuleName(request.getParameter("RULE_NAME"));
		ar.setFieldDataType(request.getParameter("FIELD_DATA_TYPE"));
		
		if (StringUtils.equals(action, "NEW")){
			actionSuccessful=ar.insertQueryRule(conn);
		} else {
			actionSuccessful=ar.modifyQueryRuleColumn(conn);
		}
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
			<jsp:param name="MSG" value="Action unsuccessful"/>
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
