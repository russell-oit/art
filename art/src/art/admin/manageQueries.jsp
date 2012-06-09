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
            if (window.confirm("Do you really want to delete the selected object(s)?")) {
                document.manageQueries.action="execManageQueries.jsp";
                document.manageQueries.QUERYACTION.value="DELETE";
                document.manageQueries.submit();
            }
        } else {
            alert("You need to select an object in order to delete it!");
        }
 
    }

    function copyQuery() {
        if(countSelected(document.getElementById("queryId"))>1){
            alert("Please select a single object");
        } else if ( document.getElementById("queryId").value > 0 && document.getElementById("newName").value != "" && document.getElementById("newName").value != "New Name") {
            document.manageQueries.action="execManageQueries.jsp";
            document.manageQueries.QUERYACTION.value="COPY";
            document.manageQueries.submit();
        } else {
            alert("You need to select an object and specify a new name in order to copy it!");
        }
    }

    function newQuery() {
        document.manageQueries.action="<%= request.getContextPath() %>/admin/editQuery.jsp";
        document.manageQueries.QUERYACTION.value="NEW";
        document.manageQueries.submit();
    }

    function updateQuery() {
        if(countSelected(document.getElementById("queryId"))>1){
            alert("Please select a single object");
        } else if ( document.getElementById("queryId").value > 0 ) {
            document.manageQueries.action="<%= request.getContextPath() %>/admin/manageQuery.jsp";
            document.manageQueries.submit();
        } else {
            alert("You need to select an object in order to modify it!");
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
    }
	
    //void the group selection when page has finished to load
    // this is to avoid the back browser issue with ajax chained values
    // i.e. the goup would appear as selected but item list empty
    window.onload = voidGroupSelection;   

</script>


<form name="manageQueries" method="post">
    <input type="hidden" name="QUERYACTION">
	
    <table align="center" class="art">       
        <tr>
			<td class="title" colspan="2">
				<br>Object Management Console <br> <img src="<%=request.getContextPath()%>/images/queries-64px.jpg">
			</td>
		</tr>

        <tr>
            <td class="attr" colspan="2" align="center">
                <input type="button" onclick="newQuery()" value="Create New Object">
            </td>
        </tr>
       
        <tr>
            <td  class="attr" colspan="2" >
                Select object to modify/delete/copy
            </td>
        </tr>
        <tr>
            <td style="vertical-align: middle;" align="center" colspan="2">
                <select id="groupId" name="GROUP_ID">
                    <option value="-1">Select Group</option>
                    <%
                    int adminLevel = ((Integer) session.getAttribute("AdminLevel")).intValue();
					String username=(String) session.getAttribute("AdminUsername");
																									
					ArtQuery aq=new ArtQuery();
					Map groups=aq.getAdminObjectGroups(adminLevel,username);
					Iterator it = groups.entrySet().iterator();
					int objectGroupId; 
					
					while (it.hasNext()) {
						Map.Entry entry = (Map.Entry)it.next();
						ObjectGroup og=(ObjectGroup)entry.getValue();
                        objectGroupId=og.getGroupId();						
						%>
						<option value="<%=objectGroupId%>">
							<%=og.getName() + " - " + og.getDescription() +" (" + objectGroupId + ")" %>
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
            <td align="right" width="50%">Modify selected</td>
            <td align="left" width="50%">  <input type="button" onclick="updateQuery()" value="Modify"> </td>
        </tr>
        <tr>
            <td align="right">Delete selected</td>
            <td align="left">  <input type="button" onclick="deleteQuery()" value="Delete"> </td>
        </tr>
        <tr>
            <td align="right"><input type="text" size="20"  maxlength="25" value="New Name" name="NEW_QUERY_NAME" id="newName"> </td>
            <td align="left">  <input type="button" onclick="copyQuery()" value="Copy"> </td>
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


<%@ include file ="footer.html" %>
