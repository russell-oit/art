<%@ page import="java.sql.*,art.utils.*;" %>
<%@ page contentType="text/html; charset=UTF-8" %>
<%  request.setCharacterEncoding("UTF-8"); %>

<%

 boolean MODIFY = request.getParameter("DRILLDOWN_ACTION").equals("MODIFY");
 boolean DELETE = request.getParameter("DRILLDOWN_ACTION").equals("DELETE");
 boolean MOVEUP = request.getParameter("DRILLDOWN_ACTION").equals("MOVEUP");
 
 int queryId = Integer.parseInt(request.getParameter("QUERY_ID"));
 int queryPosition = -1;
 if (request.getParameter("DRILLDOWN_QUERY_POSITION") != null ) {
    String s= request.getParameter("DRILLDOWN_QUERY_POSITION");    
    if(s.indexOf("_")== -1) { // Always apart when deleting  
       queryPosition = Integer.parseInt(s);  
    } else {
       queryPosition = Integer.parseInt(s.substring(0,s.indexOf("_")));
    }
 } 
 
 Connection conn = (Connection) session.getAttribute("SessionConn");
 if ( conn == null || conn.isClosed()) {
    %>
       <jsp:forward page="error.jsp">
		<jsp:param name="MOD" value="Execute Update Drilldown"/>
		<jsp:param name="ACT" value="Get connection from session"/>
		<jsp:param name="MSG" value="Database connection not valid. Please log in again"/>
		<jsp:param name="NUM" value="100"/>
       </jsp:forward>
    <%   
 }
 
 ArtParamValidation qpv = new ArtParamValidation();
 if (!qpv.validate(request)) {
    %>
       <jsp:forward page="error.jsp">
		<jsp:param name="MOD" value="Execute Update Drilldown"/>
		<jsp:param name="ACT" value="Parsing Parameters"/>
		<jsp:param name="MSG" value="At least one parameter contains an invalid character."/>
		<jsp:param name="NUM" value="110"/>
       </jsp:forward>
    <%   
 }
 
 DrilldownQuery drilldown = new DrilldownQuery();
 boolean actionSuccessful = true;

 try {
    if (MODIFY || DELETE || MOVEUP) { // get existing drill down
        actionSuccessful = drilldown.create(conn, queryId, queryPosition);
    }
    if (DELETE) { // delete and forward
        actionSuccessful = drilldown.delete(conn);
	conn.commit();
	%>
	   <jsp:forward page="viewDrilldowns.jsp">
	    <jsp:param name="QUERY_ID" value="<%= queryId %>"/>
	   </jsp:forward>
	<%   
	return;
    }
    if (MOVEUP) { // moveUp and forward
        actionSuccessful = drilldown.moveUp(conn);
	conn.commit();
	%>
	   <jsp:forward page="viewDrilldowns.jsp">
	    <jsp:param name="QUERY_ID" value="<%= queryId %>"/>
	   </jsp:forward>
	<%   
	return;
    }
    
	//finalize modify action
    if (actionSuccessful) {
       //insert new drill down or save modifications
       drilldown.setQueryId(queryId);
		drilldown.setDrilldownQueryId(Integer.parseInt(request.getParameter("DRILLDOWN_QUERY_ID")));
		drilldown.setDrilldownTitle(request.getParameter("DRILLDOWN_TITLE"));
		drilldown.setDrilldownText(request.getParameter("DRILLDOWN_TEXT"));
		drilldown.setOutputFormat(request.getParameter("OUTPUT_FORMAT"));
		drilldown.setOpenInNewWindow(request.getParameter("OPEN_IN_NEW_WINDOW"));

       if (MODIFY) {   
		 // the field position is already in the object from drilldown.create()
          drilldown.update(conn);
       } else {
          // the field position is determined automatically
          drilldown.insert(conn);
       }
       conn.commit();
	%>
	   <jsp:forward page="viewDrilldowns.jsp">
	    <jsp:param name="QUERY_ID" value="<%= queryId %>"/>
	   </jsp:forward>
	<%   
    } else {
     //rollback and go to error page
     conn.rollback();
%>
   <jsp:forward page="error.jsp">
	    <jsp:param name="MOD" value="Execute Update Drilldown"/>
	    <jsp:param name="ACT" value="Modify Query"/>
	    <jsp:param name="MSG" value="Record Does Not Exist"/>
	    <jsp:param name="NUM" value="130"/>
   </jsp:forward>

<%   
    }
 } catch(Exception ex) {
     // revert to page error
      //ex.printStackTrace();
%>
   <jsp:forward page="error.jsp">
	    <jsp:param name="MOD" value="Execute Update Drilldown"/>
	    <jsp:param name="ACT" value="Modify Drilldown Query"/>
	    <jsp:param name="MSG" value="<%=ex%>"/>
	    <jsp:param name="NUM" value="199"/>
   </jsp:forward>
<%   
 }


%>
