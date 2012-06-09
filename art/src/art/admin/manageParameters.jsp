<%@ page import="java.sql.*,java.util.*,art.utils.*;" %>
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
            if (window.confirm("Do you really want to delete this parameter?\nNote: For INLINE parameters you need to modify the query's SQL to remove the parameter label.")) {
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
           Show List of existent parameters
        */%>
        <tr><td colspan="2" class="data">
                <small>Order - Param Type - Param Name - Label</small>
            </td>
        </tr>
        <tr><td colspan="2" class="data">
                <select name="FIELD_POSITION" size="4" >
                    <%
					ArtQueryParam param;
					int fieldPosition;
                    String pType;
					
					Map queries=aq.getQueryParams(queryId);
					Iterator it = queries.entrySet().iterator();
					while (it.hasNext()) {
						Map.Entry entry = (Map.Entry)it.next();
						param=(ArtQueryParam)entry.getValue();
						fieldPosition=param.getFieldPosition();
						pType=param.getParamType();
						
						String paramType;
						if (pType.equals("M")) {
							paramType="MULTI";
						} else if (pType.equals("I")) {
							paramType="INLINE";
						} else {
							paramType="BIND";
						}
						String multiFieldOrBindPos; // stores column name (MULTI) or bind position (BIND) or inline label (INLINE)
						if (paramType.equals("BIND")) {
							multiFieldOrBindPos = String.valueOf(param.getChainedPosition());
						} else {
							multiFieldOrBindPos = param.getParamLabel();
						}
						%>
						<option value="<%=fieldPosition+"_"+paramType%>">
							<%=fieldPosition%>-<%=paramType%>-<%=param.getName()%>-<%=multiFieldOrBindPos%>
						</option>
						<%
					}
					%>

                </select>
                <input type="button" onclick="moveUp()" value="Move Up" name="ACTION">
            </td>
        </tr>

        <%/*
           Show button  : delete, modify, new Bind Param, new Multi Param
        */%>
        <tr><td colspan="2" class="data">
                <input type="button" onclick="deleteParam()" value="Delete" name="ACTION">
                &nbsp;<input type="button" onclick="updateParam()" value="Modify" name="ACTION">
            </td>
        </tr>
       
        <tr><td colspan="2" class="data">                
                <input type="radio"  name="NEWTYPE"  value="INLINE" checked >Inline
                <input type="radio"  name="NEWTYPE"  value="MULTI" >Multi                
                &nbsp;
                <input type="button" onclick="createParam()" value="New" name="ACTION"> </td>
            </td>
            
        </tr>
        <tr>
            <td>&nbsp;</td>
        </tr>
        <tr><td colspan="2">  <input type="button" onclick="goBackToEditQuery()" value="<< Back" name="backQuery"> </td>
        </tr>
    </table>
</form>

<%@ include file ="footer.html" %>
