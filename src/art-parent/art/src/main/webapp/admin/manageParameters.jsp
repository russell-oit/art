<%@ page import="java.sql.*,java.util.*,art.utils.*" %>
<%@ page import="org.apache.commons.lang.StringUtils" %>
<%@ include file ="headerAdmin.jsp" %>

<script language="javascript">
    <!--

    function moveUp() {
		if (document.viewParam.FIELD_POSITION.selectedIndex>=0) {
			document.viewParam.action="execEditParameter.jsp";
			document.viewParam.PARAMACTION.value="MOVEUP";
			document.viewParam.submit();
		} else {
            alert("You must select a parameter");
        }
    }

    function deleteParam() {
        if (document.viewParam.FIELD_POSITION.selectedIndex>=0) {
            if (window.confirm("Do you really want to delete this parameter?\nNote: You need to modify the query's SQL to remove the parameter label.")) {
                document.viewParam.action="execEditParameter.jsp";
                document.viewParam.PARAMACTION.value="DELETE";
                document.viewParam.submit();
            }
        } else {
            alert("You must select a parameter");
        }

    }

    function updateParam() {
		if (document.viewParam.FIELD_POSITION.selectedIndex>=0) {
			document.viewParam.action="editParameter.jsp";
			document.viewParam.PARAMACTION.value="MODIFY";
			document.viewParam.submit();
		} else {
            alert("You must select a parameter");
        }
    }

    function createParam() {
        document.viewParam.action="editParameter.jsp";
        document.viewParam.PARAMACTION.value="NEW";
        document.viewParam.submit();
    }

    function goBackToEditQuery() {
        document.viewParam.action="manageQuery.jsp";
        document.viewParam.submit();
    }

    //-->
</script>

<%
int queryId = Integer.parseInt(request.getParameter("QUERY_ID"));

Connection conn = (Connection) session.getAttribute("SessionConn");
if ( conn == null || conn.isClosed()) {
%>
<jsp:forward page="error.jsp">
    <jsp:param name="MOD" value="View Parameters"/>
    <jsp:param name="ACT" value="Get connection from session"/>
    <jsp:param name="MSG" value="Database connection not valid. Please log in again"/>
    <jsp:param name="NUM" value="100"/>
</jsp:forward>

<%
}

ArtQuery aq=new ArtQuery();

%>

<form name="viewParam" method="post">
    <input type="hidden" name="QUERY_ID" value="<%= request.getParameter("QUERY_ID")%>">
    <input type="hidden" name="PARAMACTION" >
    <table align="center">
        <%/*
           Print title
        */%>
        <tr><td class="title" colspan="2" > Edit Parameters </td></tr>
        <%/*
           Show List of existing parameters
        */%>
        <tr><td colspan="2" class="data">
                <small>Order - Type - Name - Label</small>
            </td>
        </tr>
        <tr><td colspan="2" class="data">
                <select name="FIELD_POSITION" size="4" >
                    <%
					ArtQueryParam param;
					int fieldPosition;
                    String pType;
					
					Map<Integer, ArtQueryParam> queries=aq.getQueryParams(queryId);
					for (Map.Entry<Integer, ArtQueryParam> entry : queries.entrySet()) {
						param=entry.getValue();
						fieldPosition=param.getFieldPosition();
						pType=param.getParamType();
						
						String paramType="";
						if (StringUtils.equals(pType,"M")) {
							paramType="MULTI";
						} else if (StringUtils.equals(pType,"I")) {
							paramType="INLINE";
						} 
																
						%>
						<option value="<%=fieldPosition%>">
							<%=fieldPosition%>-<%=paramType%>-<%=param.getName()%>-<%=param.getParamLabel()%>
						</option>
						<%
					}
					%>

                </select>
                <input type="button" onclick="moveUp()" value="Move Up" name="ACTION">
            </td>
        </tr>

        <%/*
           Show buttons : delete, modify, new 
        */%>
        <tr><td colspan="2" class="data">
                <input type="button" onclick="createParam()" value="New" name="ACTION">
                &nbsp;<input type="button" onclick="updateParam()" value="Modify" name="ACTION">				
				&nbsp;<input type="button" onclick="deleteParam()" value="Delete" name="ACTION">
            </td>
        </tr>
              
        <tr>
            <td>&nbsp;</td>
        </tr>
        <tr>
			<td colspan="2">
				<input type="button" onclick="goBackToEditQuery()" value="<< Back" name="backQuery">
			</td>
        </tr>
    </table>
</form>

<%@ include file ="/user/footer.jsp" %>
