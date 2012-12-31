<%@ page import="java.sql.*,art.utils.*" %>
<%@ page import="org.apache.commons.lang.StringUtils" %>

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
 
 boolean MODIFY = request.getParameter("PARAMACTION").equals("MODIFY");
 boolean DELETE = request.getParameter("PARAMACTION").equals("DELETE");
 boolean MOVEUP = request.getParameter("PARAMACTION").equals("MOVEUP");
 
 int queryId = Integer.parseInt(request.getParameter("QUERY_ID"));
 int fieldPosition = Integer.parseInt(request.getParameter("FIELD_POSITION"));
 
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
 boolean paramExists = true;

 try {
    if (MODIFY || DELETE || MOVEUP) { // get existent
        paramExists = qp.create(conn, queryId, fieldPosition);
    }
    if (DELETE) { // delete and go to manage parameters page
		qp.delete(conn);
		conn.commit();
	%>
	   <jsp:forward page="manageParameters.jsp">
	    <jsp:param name="QUERY_ID" value="<%= queryId %>"/>
	   </jsp:forward>
	<%   
	return;
    }
    if (MOVEUP) { // moveUp and go to manage parameters page
        qp.moveUp(conn);
		conn.commit();
	%>
	   <jsp:forward page="manageParameters.jsp">
	    <jsp:param name="QUERY_ID" value="<%= queryId %>"/>
	   </jsp:forward>
	<%   
	return;
    }
    
	//if we are here, either it's a new parameter creation or a parameter modification (and not move up)  
    if (paramExists) {    
		String paramType=request.getParameter("PARAM_TYPE");
		
       qp.setQueryId(Integer.parseInt(request.getParameter("QUERY_ID")));                                        
       qp.setParamType(paramType);       
       qp.setName(request.getParameter("NAME"));
       qp.setShortDescription(request.getParameter("SHORT_DESCRIPTION"));
       qp.setDescription(request.getParameter("DESCRIPTION"));
       qp.setParamDataType(request.getParameter("PARAM_DATA_TYPE"));
       qp.setDefaultValue(request.getParameter("DEFAULT_VALUE"));
	   qp.setParamLabel(request.getParameter("PARAM_LABEL"));
	   
	   String useLov=request.getParameter("USE_LOV");
	   String lovQueryId=request.getParameter("LOV_QUERY_ID");	   
       qp.setUseLov(useLov);
	   if(StringUtils.equals(useLov,"Y") && lovQueryId!=null){		
		qp.setLovQueryId(Integer.parseInt(lovQueryId));		
		} else {
			qp.setLovQueryId(0);
		}
	   
       qp.setApplyRulesToLov(request.getParameter("APPLY_RULES_TO_LOV"));
       qp.setChainedPosition(Integer.parseInt(request.getParameter("CHAINED_POSITION")));
	   
	   //save drill down column for inline parameter. multi parameter doesn't have drill down column
	   if(StringUtils.equals(paramType,"I")){
		qp.setDrilldownColumn(Integer.parseInt(request.getParameter("DRILLDOWN_COLUMN"))); //save new drill down column property
		}
		qp.setChainedValuePosition(Integer.parseInt(request.getParameter("CHAINED_VALUE_POSITION")));
		
		//save drill down column for inline parameter. not relevant for multi parameter
	   if(StringUtils.equals(paramType,"I")){
		   qp.setDirectSubstitution(request.getParameter("DIRECT_SUBSTITUTION"));
	   }

       if (MODIFY) {
          // the field position is already in the object as we created it: qp.create	  
          qp.update(conn);
       } else {
          //new parameter definition. the field position is determined automatically
          qp.insert(conn);
       }
       conn.commit();
	%>
	   <jsp:forward page="manageParameters.jsp">
	    <jsp:param name="QUERY_ID" value="<%= queryId %>"/>
	   </jsp:forward>
	<%   
    } else {
		//problem while creating or retrieving parameter definition     
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
