<%@ page import="java.util.*,art.utils.*" %>
<%@ taglib uri="http://ajaxtags.sourceforge.net/tags/ajaxtags" prefix="ajax"%>
<%@ include file ="headerAdmin.jsp" %>

<script type="text/javascript">
    
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
            alert("Please select at least one query");
        }
    }
	
	function moveQuery() {
        if ( document.getElementById("queryId").value > 0
			&& document.getElementById("newGroupId").value > -1 ) {
			
			document.manageQueries.action="execManageQueries.jsp";
			document.manageQueries.QUERYACTION.value="MOVE";
			document.manageQueries.submit();
        } else {
            alert("Please select at least one query and specify the new query group");
        }
    }

    function copyQuery() {
        if(countSelected(document.getElementById("queryId"))>1){
            alert("Please select a single query");
        } else if ( document.getElementById("queryId").value > 0
			&& document.getElementById("copyName").value != "") {
			
            document.manageQueries.action="execManageQueries.jsp";
            document.manageQueries.QUERYACTION.value="COPY";
            document.manageQueries.submit();
        } else {
            alert("Please select a query and specify a new name for it");
			document.getElementById('copyName').focus();
        }
    }
	
	function renameQuery() {
        if(countSelected(document.getElementById("queryId"))>1){
            alert("Please select a single query");
        } else if ( document.getElementById("queryId").value > 0
			&& document.getElementById("newName").value != "") {
			
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
	
	function updateQuerySource() {
        if(countSelected(document.getElementById("queryId"))>1){
            alert("Please select a single query");
        } else if ( document.getElementById("queryId").value > 0 ) {
            document.manageQueries.action="<%= request.getContextPath() %>/admin/editQuery.jsp";
			document.manageQueries.QUERYACTION.value="MODIFY";
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
            document.getElementById("groupId").options[i].selected = false;
            document.getElementById("groupId").selectedIndex = 0;
        }
		for (var i=0; i<document.getElementById("newGroupId").length; i++) {
            document.getElementById("newGroupId").options[i].selected = false;
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


<form name="manageQueries" method="post" >
    <input type="hidden" name="QUERYACTION">
	
    <table class="centerTableAuto art">       
        <tr>
			<td class="title" colspan="2">
				<br>Query Management Console <br> <img src="<%=request.getContextPath()%>/images/queries-64px.jpg">
			</td>
		</tr>

        <tr>
            <td class="attr"  align="center" colspan="2">
                <input type="button" onclick="newQuery()" value="Create New Query">
            </td>
        </tr>
       
        <tr>
            <td  class="attr" colspan="2">
                Select query to modify/delete/copy
            </td>
        </tr>
        <tr>
            <td colspan="2" style="padding-top: 10px; padding-bottom: 10px">
				Query Groups <br>
                <select id="groupId" name="GROUP_ID" size="5">
					<option value="-1">--</option>
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
			</td>
		</tr>
		<tr><td colspan="2" style="padding-top: 10px; padding-bottom: 10px">
				 Queries <br>
                <select id="queryId" name="QUERY_ID" size="10" multiple>
                    <option value="">...</option>
                </select>
            </td>
        </tr>
		<tr>
		<td colspan="2" style="padding-top: 10px; padding-bottom: 10px">
				<input type="button" onclick="updateQuery()" value="Modify">
				&nbsp;
				<input type="button" onclick="updateQuerySource()" value="Modify Source">
				&nbsp;
				<input type="button" onclick="deleteQuery()" value="Delete">
			</td>
		</tr>
<tr>
			<td>
				<input type="button" onclick="copyQuery()" value="Copy">
			</td>
			<td>
				<input type="text" size="40"  maxlength="50" value=""
					   name="COPY_QUERY_NAME" id="copyName" placeholder="new name">
			</td>
			</tr>
			<tr>
			<td>
				<input type="button" onclick="renameQuery()" value="Rename">
			</td>
			<td>
				<input type="text" size="40"  maxlength="50" value=""
					   name="NEW_QUERY_NAME" id="newName" placeholder="new name">
			</td>
			</tr>
			<tr>
			<td>
				<input type="button" onclick="moveQuery()" value="Move">
			</td>
			<td>
				<select id="newGroupId" name="NEW_GROUP_ID">
                    <option value="-1">Select Group</option>
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
        
</tr>
    </table>
</form>
				
				<p>
    <table class="centerTable">
        <tr><td>
                <form name="backToAdminConsole" method="post" action="<%= request.getContextPath() %>/admin/adminConsole.jsp">
                    <input type="submit"  value=" << " name="backQuery"><small>&nbsp;Back to Admin Console</small>
                </form>
            </td>
		</tr>
    </table>
					</p>

<%@ include file ="/user/footer.jsp" %>


<%
 String dataProviderUrl = request.getContextPath()+"/AjaxTagsDataProvider";
%>

<ajax:select
baseUrl="<%=dataProviderUrl%>"
    source="groupId"
    target="queryId"
    parameters="action=queriesadmin,groupId={groupId}"
    emptyOptionName="..."
    emptyOptionValue=""
    preFunction="artAddWork"
    postFunction="artRemoveWork"
	executeOnLoad="true"
    />



