<%@ page import="java.sql.*,art.utils.*;" %>
<%@ page contentType="text/html; charset=UTF-8" %>
<%  request.setCharacterEncoding("UTF-8"); %>

<%
/* CHANGELOG
 *
 DATE      WHO         What
 20030701  Enrico      Added missed commit() when deleting a param
 20050212  Enrico      Added logic to handle inline params
 *
 */
 
 boolean MODIFY = request.getParameter("PARAMACTION").equals("MODIFY");
 boolean DELETE = request.getParameter("PARAMACTION").equals("DELETE");
 boolean MOVEUP = request.getParameter("PARAMACTION").equals("MOVEUP");
 
 int queryId = Integer.parseInt(request.getParameter("QUERY_ID"));
 int fieldPosition = -1;
 if (request.getParameter("FIELD_POSITION") != null ) {
    String s= request.getParameter("FIELD_POSITION");    
    if(s.indexOf("_")== -1) { // Always apart when deleting  
       fieldPosition = Integer.parseInt(s);  
    } else {
       fieldPosition = Integer.parseInt(s.substring(0,s.indexOf("_")));
    }
 } 
 
 Connection conn = (Connection) session.getAttribute("SessionConn");
 if ( conn == null || conn.isClosed()) {
    %>
       <jsp:forward page="error.jsp">
		<jsp:param name="MOD" value="Execute Update Parameter"/>
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
		<jsp:param name="MOD" value="Execute Update Parameter"/>
		<jsp:param name="ACT" value="Parsing Parameters"/>
		<jsp:param name="MSG" value="At least one parameter contains an invalid char."/>
		<jsp:param name="NUM" value="110"/>
       </jsp:forward>
    <%   
 }
 ArtQueryParam qp = new ArtQueryParam();
 boolean bp = true;

 try {
    if (MODIFY || DELETE || MOVEUP) { // get existent
        bp = qp.create(conn, queryId, fieldPosition);
    }
    if (DELETE) { // delete and forward
        bp = qp.delete(conn);
	conn.commit();
	%>
	   <jsp:forward page="manageParameters.jsp">
	    <jsp:param name="QUERY_ID" value="<%= queryId %>"/>
	   </jsp:forward>
	<%   
	return;
    }
    if (MOVEUP) { // moveUp
        bp = qp.moveUp(conn);
	conn.commit();
	%>
	   <jsp:forward page="manageParameters.jsp">
	    <jsp:param name="QUERY_ID" value="<%= queryId %>"/>
	   </jsp:forward>
	<%   
	return;
    }
    boolean bind = request.getParameter("PARAM_TYPE").equals("N");
    boolean inline = request.getParameter("PARAM_TYPE").equals("I");
    
    if (inline) { // check if the inline label exists in the SQL code
        ArtQuery aq = new ArtQuery();
		aq.create(conn, queryId);
		if (aq.getText().indexOf("#"+request.getParameter("PARAM_LABEL")+"#") == -1) {
		  // label does not exists! forward to error page
		  String msg = "The inline parameter <b>#"+request.getParameter("PARAM_LABEL")+"#</b> "+
					   " does not exist in the SQL source.<br> The inline parameter label is " + 
				   " case sensitive and it must match exactly the label in the SQL source";
		  
			  %>
			 <jsp:forward page="error.jsp">
				  <jsp:param name="MOD" value="Execute Update Parameter"/>
				  <jsp:param name="ACT" value="Check inline paramaeter"/>
				  <jsp:param name="MSG" value="<%=msg%>"/>
				  <jsp:param name="NUM" value="200"/>
			 </jsp:forward>
			  <%   
		}
    }
    
    if (bp) {       
       qp.setQueryId(Integer.parseInt(request.getParameter("QUERY_ID")));       
              
       if (bind) {
		   // BIND
		  if (request.getParameter("BIND_POSITION") == null){
			  %>
			 <jsp:forward page="error.jsp">
				  <jsp:param name="MOD" value="Execute Update Parameter"/>
				  <jsp:param name="ACT" value="Parsing values for bind parameter"/>
				  <jsp:param name="MSG" value="You must select one parameter from the list"/>
				  <jsp:param name="NUM" value="200"/>
			 </jsp:forward>
			  <%   

		   }
			  qp.setBindPosition(Integer.parseInt(request.getParameter("BIND_POSITION")));
			  qp.setParamType("N");
		   // ----- end BIND
       } else {
       // MULTI or INLINE
          qp.setParamLabel(request.getParameter("PARAM_LABEL"));       
       }
              
       qp.setParamType(request.getParameter("PARAM_TYPE"));       
       qp.setName(request.getParameter("NAME"));
       qp.setShortDescription(request.getParameter("SHORT_DESCRIPTION"));
       qp.setDescription(request.getParameter("DESCRIPTION"));
       qp.setFieldClass(request.getParameter("FIELD_CLASS"));
       qp.setDefaultValue(request.getParameter("DEFAULT_VALUE"));
	   
	   String useLov=request.getParameter("USE_LOV");
	   String lovQueryId=request.getParameter("LOV_QUERY_ID");	   
       qp.setUseLov(useLov);
	   if("Y".equals(useLov) && lovQueryId!=null){		
		qp.setLovQueryId(Integer.parseInt(lovQueryId));		
		} else {
			qp.setLovQueryId(0);
		}
	   
       qp.setApplyRulesToLov(request.getParameter("APPLY_RULES_TO_LOV"));
       qp.setChainedPosition(Integer.parseInt(request.getParameter("CHAINED_POSITION")));
	   
	   //save drill down column. multi parameter doesn't have drill down column
	   if(request.getParameter("DRILLDOWN_COLUMN")!=null){
		qp.setDrilldownColumn(Integer.parseInt(request.getParameter("DRILLDOWN_COLUMN"))); //save new drill down column property
		}
		qp.setChainedValuePosition(Integer.parseInt(request.getParameter("CHAINED_VALUE_POSITION")));

       if (MODIFY) {
          // the field position is already in the object
	  // as we created it: qp.create
          qp.update(conn);
       } else {
          // the field position is determined automatically
          qp.insert(conn);
       }
       conn.commit();
	%>
	   <jsp:forward page="manageParameters.jsp">
	    <jsp:param name="QUERY_ID" value="<%= queryId %>"/>
	   </jsp:forward>
	<%   
    } else {
     // revert to page error
     conn.rollback();
%>
   <jsp:forward page="error.jsp">
	    <jsp:param name="MOD" value="Execute Update Parameter"/>
	    <jsp:param name="ACT" value="Modify Query Param and SQL"/>
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
	    <jsp:param name="MOD" value="Execute Update Parameter"/>
	    <jsp:param name="ACT" value="Modify Query Param and SQL"/>
	    <jsp:param name="MSG" value="<%=ex%>"/>
	    <jsp:param name="NUM" value="199"/>
   </jsp:forward>
<%   
 }


%>
