<%@ page import="java.util.*,art.utils.*;" %>
<%@ include file ="headerAdmin.jsp" %>

<script language="javascript">
    <!--

    function moveUp() {
        if (document.viewDrilldown.DRILLDOWN_QUERY_POSITION.selectedIndex>=0) {
            document.viewDrilldown.action="execEditDrilldown.jsp";
            document.viewDrilldown.DRILLDOWN_ACTION.value="MOVEUP";
            document.viewDrilldown.submit();
        } else {
            alert("You must select a query!");
        }
    }

    function deleteDrilldown() {

        if (document.viewDrilldown.DRILLDOWN_QUERY_POSITION.selectedIndex>=0) {
            if (window.confirm("Do you really want to delete this drill down query? Only the drill down association will be deleted. The actual query will not be deleted.")) {
                document.viewDrilldown.action="execEditDrilldown.jsp";
                document.viewDrilldown.DRILLDOWN_ACTION.value="DELETE";
                document.viewDrilldown.submit();
            }
        } else {
            alert("You must select a query!");
        }
 
    }

    function updateDrilldown() {
        if (document.viewDrilldown.DRILLDOWN_QUERY_POSITION.selectedIndex>=0) {
            document.viewDrilldown.action="editDrilldown.jsp";
            document.viewDrilldown.DRILLDOWN_ACTION.value="MODIFY";
            document.viewDrilldown.submit();
        } else {
            alert("You must select a query!");
        }
    }

    function createDrilldown() {
        document.viewDrilldown.action="editDrilldown.jsp";
        document.viewDrilldown.DRILLDOWN_ACTION.value="NEW";
        document.viewDrilldown.submit();
    }

    function goBackToEditQuery() {
        document.viewDrilldown.action="manageQuery.jsp";
        document.viewDrilldown.submit();
    }

    //-->
</script>


<form name="viewDrilldown" method="post">
    <input type="hidden" name="QUERY_ID" value="<%= request.getParameter("QUERY_ID")%>">
    <input type="hidden" name="DRILLDOWN_ACTION" >
    <table align="center">

        <tr><td class="title" colspan="2" > Edit Drill Down Queries </td></tr>
        <%/*
           Show list of existing drill down queries
        */%>
        <tr><td colspan="2" class="data">
                <small>Order - Drill Down Query - Query ID</small>
            </td>
        </tr>
        <tr><td colspan="2" class="data">
                <select name="DRILLDOWN_QUERY_POSITION" size="4" >
                    <%
					int queryId = Integer.parseInt(request.getParameter("QUERY_ID"));
					
					ArtQuery aq=new ArtQuery();
					DrilldownQuery dq;
					int queryPosition;
                    int drilldownQueryId;
					
					Map<Integer,DrilldownQuery> queries=aq.getDrilldownQueries(queryId,false);
					Iterator it = queries.entrySet().iterator();	
					while (it.hasNext()) {						
						Map.Entry entry = (Map.Entry)it.next();
						dq=(DrilldownQuery)entry.getValue();
						drilldownQueryId=dq.getDrilldownQueryId();
						queryPosition=dq.getQueryPosition();						
						
						%>
						<option value="<%=queryPosition+"_"+drilldownQueryId%>" selected>
							<%=queryPosition%> - <%=dq.getDrilldownQueryName()%> - <%=drilldownQueryId%>
						</option>
						<%
					}                        
					%>
                     
                </select>
                <input type="button" onclick="moveUp()" value="Move Up" name="ACTION">
            </td>
        </tr>
        <%/*
           Show button : delete, modify, new
        */%>
        <tr><td colspan="2" class="data">  <input type="button" onclick="deleteDrilldown()" value="Delete" name="ACTION">
            </td>
        </tr>
        <tr><td colspan="2" class="data">  <input type="button" onclick="updateDrilldown()" value="Modify" name="ACTION"> </td>
        </tr>
        <tr>
            <td class="data" valign="center"> <input type="button" onclick="createDrilldown()" value="New" name="ACTION"> </td>
        </tr>
        <tr><td colspan="2" class="data" >  <input type="button" onclick="goBackToEditQuery()" value="<< Back" name="backQuery"> </td>
        </tr>
    </table>
</form>


<%@ include file ="/user/footer.jsp" %>
