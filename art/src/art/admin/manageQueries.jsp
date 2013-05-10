<%@ page import="java.util.*,art.utils.*" %>
<%@ taglib uri="http://ajaxtags.sourceforge.net/tags/ajaxtags" prefix="ajax"%>
<%@ include file ="headerAdmin.jsp" %>

<script language="javascript">
    
    function countSelected(list){
        var count=0;
        for(var i=0; i<list.options.length; i++ ){
            if(list.options[i].selected){
                count++;
            }
        }
        
        return count;
    }
    
    function deleteQuery() {
        if ( document.getElementById("queryId").value > 0 ) {
            if (window.confirm("Do you really want to delete the selected queries?")) {
                document.manageQueries.action="execManageQueries.jsp";
                document.manageQueries.QUERYACTION.value="DELETE";
                document.manageQueries.submit();
            }
        } else {
            alert("Please select at leaset one query");
        }
    }
	
	function moveQuery() {
        if ( document.getElementById("queryId").value > 0 && document.getElementById("newGroupId").value > 0 ) {
			document.manageQueries.action="execManageQueries.jsp";
			document.manageQueries.QUERYACTION.value="MOVE";
			document.manageQueries.submit();
        } else {
            alert("Please select at leaset one query and specify the new query group");
        }
    }

    function copyQuery() {
        if(countSelected(document.getElementById("queryId"))>1){
            alert("Please select a single query");
        } else if ( document.getElementById("queryId").value > 0 && document.getElementById("copyName").value != "" && document.getElementById("copyName").value != "New Name") {
            document.manageQueries.action="execManageQueries.jsp";
            document.manageQueries.QUERYACTION.value="COPY";
            document.manageQueries.submit();
        } else {
            alert("Please select a query and specify a new name for it");
        }
    }
	
	function renameQuery() {
        if(countSelected(document.getElementById("queryId"))>1){
            alert("Please select a single query");
        } else if ( document.getElementById("queryId").value > 0 && document.getElementById("newName").value != "" && document.getElementById("newName").value != "New Name") {
            document.manageQueries.action="execManageQueries.jsp";
            document.manageQueries.QUERYACTION.value="RENAME";
            document.manageQueries.submit();
        } else {
            alert("Please select a query and specify a new name for it");
        }
    }

    function newQuery() {
        document.manageQueries.action="<%= request.getContextPath() %>/admin/editQuery.jsp";
        document.manageQueries.QUERYACTION.value="NEW";
        document.manageQueries.submit();
    }

    function updateQuery() {
        if(countSelected(document.getElementById("queryId"))>1){
            alert("Please select a single query");
        } else if ( document.getElementById("queryId").value > 0 ) {
            document.manageQueries.action="<%= request.getContextPath() %>/admin/manageQuery.jsp";
            document.manageQueries.submit();
        } else {
            alert("Please select a query");
        }
    }

    function goBackToartConsole() {
        document.manageQueries.action="<%= request.getContextPath() %>/admin/adminConsole.jsp";
        document.manageQueries.submit();
    }

    function voidGroupSelection() {
        for (var i=0; i<document.getElementById("groupId").length; i++) {
            document.getElementById("groupId").options[i].selected = false
            document.getElementById("groupId").selectedIndex = 0;
        }
		for (var i=0; i<document.getElementById("newGroupId").length; i++) {
            document.getElementById("newGroupId").options[i].selected = false
            document.getElementById("newGroupId").selectedIndex = 0;
        }
    }
	
    //void the group selection when page has finished to load
    // this is to avoid the back browser issue with ajax chained values
    // i.e. the goup would appear as selected but item list empty
    window.onload = voidGroupSelection;   

</script>

<%
int accessLevel = ((Integer) session.getAttribute("AdminLevel")).intValue();
String username=(String) session.getAttribute("AdminUsername");

ArtQuery aq=new ArtQuery();
Map<String, QueryGroup> groups=aq.getAdminQueryGroups(accessLevel,username);
int queryGroupId;
%>					


<form name="manageQueries" method="post">
    <input type="hidden" name="QUERYACTION">
	
    <table align="center" class="art">       
        <tr>
			<td class="title" colspan="2">
				<br>Query Management Console <br> <img src="<%=request.getContextPath()%>/images/queries-64px.jpg">
			</td>
		</tr>

        <tr>
            <td class="attr" colspan="2" align="center">
                <input type="button" onclick="newQuery()" value="Create New Query">
            </td>
        </tr>
       
        <tr>
            <td  class="attr" colspan="2" >
                Select query to modify/delete/copy
            </td>
        </tr>
        <tr>
            <td style="vertical-align: middle;" align="center" colspan="2">
                <select id="groupId" name="GROUP_ID">
                    <option value="-1">Select Group</option>
                    <%
					for (Map.Entry<String, QueryGroup> entry : groups.entrySet()) {
						QueryGroup qg=entry.getValue();
                        queryGroupId=qg.getGroupId();						
						%>
						<option value="<%=queryGroupId%>">
							<%=qg.getName() + " - " + qg.getDescription() +" (" + queryGroupId + ")" %>
						</option>
						<%
					 }                 
                    %>
                </select>

                <br>
                <select id="queryId" name="QUERY_ID" size="10" multiple="multiple" >
                    <option value="">...</option>
                </select>
            </td>
        </tr>
        <tr>
            <td align="right" width="80%">
				&nbsp;
			</td>
            <td align="left">
				<input type="button" onclick="updateQuery()" value="Modify">
			</td>
        </tr>
        <tr>
			<td align="right" width="80%">
				&nbsp;
			</td>
            <td align="left">
				<input type="button" onclick="deleteQuery()" value="Delete">
			</td>
        </tr>
        <tr>
            <td align="right" width="80%">
				<input type="text" size="40"  maxlength="50" value="New Name" name="COPY_QUERY_NAME" id="copyName">
			</td>
            <td align="left">
				<input type="button" onclick="copyQuery()" value="Copy">
			</td>
        </tr>
		<tr>
            <td align="right" width="80%">
				<input type="text" size="40"  maxlength="50" value="New Name" name="NEW_QUERY_NAME" id="newName">
			</td>
            <td align="left">
				<input type="button" onclick="renameQuery()" value="Rename">
			</td>
        </tr>
		<tr>
            <td align="right" width="80%">
				<select id="newGroupId" name="NEW_GROUP_ID">
                    <option value="-1">New Group</option>
                    <%
					for (Map.Entry<String, QueryGroup> entry : groups.entrySet()) {
						QueryGroup qg=entry.getValue();
                        queryGroupId=qg.getGroupId();						
						%>
						<option value="<%=queryGroupId%>">
							<%=qg.getName()%>
						</option>
						<%
					 }                 
                    %>
                </select>
			</td>
            <td align="left">
				<input type="button" onclick="moveQuery()" value="Move">
			</td>
        </tr>

    </table>
</form>


<%
 String dataProviderUrl = request.getContextPath()+"/XmlDataProvider";
%>

<ajax:select
baseUrl="<%=dataProviderUrl%>"
    source="groupId"
    target="queryId"
    parameters="action=queriesadmin,groupId={groupId}"
    emptyOptionName="Empty"
    emptyOptionValue=""
    preFunction="artAddWork"
    postFunction="artRemoveWork"
    />


<div align="center" valign="center">
    <table>
        <tr><td>
                <form name="backToAdminConsole" method="post" action="<%= request.getContextPath() %>/admin/adminConsole.jsp">
                    <input type="submit"  value=" << " name="backQuery"><small>&nbsp;Back to Admin Console</small>
                </form>
            </td>
		</tr>
    </table>
</div>


<%@ include file ="/user/footer.jsp" %>
