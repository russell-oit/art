<%@ page import="art.utils.*,java.util.*" %>
<%@ include file ="headerAdmin.jsp" %>


<script type="text/javascript">

    function validateForm()  {
		//ensure minimum number of items are selected
		if (document.manageUserPrivileges.USERS.selectedIndex<0 && document.manageUserPrivileges.USER_GROUPS.selectedIndex<0) {
			alert("Please select at least one user or user group");
			return false;
		} else if (document.manageUserPrivileges.OBJECTS.selectedIndex<0 && document.manageUserPrivileges.OBJECT_GROUPS.selectedIndex<0) {
			alert("Please select at least one object or object group");
			return false;
		}
    }
    function successForm() {
		alert("Update Successful");
        
        //clear selected items to enable change of selections
        document.manageUserPrivileges.USERS.selectedIndex=-1;
        document.manageUserPrivileges.USER_GROUPS.selectedIndex=-1;
        document.manageUserPrivileges.OBJECTS.selectedIndex=-1;
        document.manageUserPrivileges.OBJECT_GROUPS.selectedIndex=-1;
    }


$jQuery(document).ready(function() {
    // bind form using ajaxForm
    $jQuery('#artForm').ajaxForm( { beforeSubmit: validateForm, success: successForm  });
});
</script>


<%
UserEntity ue=new UserEntity();
Iterator it;
int adminLevel = ((Integer) session.getAttribute("AdminLevel")).intValue();
String username=(String) session.getAttribute("AdminUsername");
%>

<form id="artForm" name="manageUserPrivileges" method="post" action="execManageUserPrivileges.jsp">
    <table align="center">
        <tr>
			<td class="title" colspan="2"> Manage User/User Group Privileges </td>
        </tr>
        <tr>
            <td class="data" colspan="2"> Select users/user groups and the objects/object groups to grant/revoke access </td>
        </tr>

		<tr>
            <td class="data"> Users </td>
            <td class="data">
                <select name="USERS" size="5" multiple>
                    <%
					List<String> usernames=ue.getAllUsernames();
					it=usernames.iterator();
					String name;
					while(it.hasNext()) {
						name=(String)it.next();
						%>
						<option value="<%=name%>" ><%=name%></option>
						<%
					}
					%>
                </select>
            </td>
        </tr>

		<tr>
            <td class="data"> User Groups </td>
            <td class="data">
                <select name="USER_GROUPS" size="5" multiple>
                    <%
					UserGroup ug=new UserGroup();
					Map userGroups=ug.getAllUserGroupNames();
					it = userGroups.entrySet().iterator();
					while(it.hasNext()) {
						Map.Entry entry = (Map.Entry)it.next();
						%>
						<option value="<%=entry.getValue()%>" ><%=entry.getKey()%></option>
						<%
					}
					%>
                </select>
            </td>
        </tr>

		<tr><td colspan="2">&nbsp;</td></tr>

		<tr>
            <td class="data"> Object Groups </td>
            <td class="data">
                <select name="OBJECT_GROUPS" size="5" multiple>
                    <%
					ArtQuery aq=new ArtQuery();
					Map objectGroups=aq.getAdminObjectGroupsList(adminLevel,username);
					it = objectGroups.entrySet().iterator();
					while(it.hasNext()) {
						Map.Entry entry = (Map.Entry)it.next();
						%>
						<option value="<%=entry.getValue()%>" ><%=entry.getKey()%></option>
						<%
					}
					%>
                </select>
            </td>
        </tr>

		 <tr>
            <td class="data"> Objects <br><small>[Object Group Name] object name (id) </small> </td>
            <td class="data">
                <select name="OBJECTS" size="10" multiple>
                    <%
					Map objects=ue.getAdminQueries(adminLevel,username);
					it = objects.entrySet().iterator();
					int queryId;

					while (it.hasNext()) {
						Map.Entry entry = (Map.Entry)it.next();
						ArtQuery query=(ArtQuery)entry.getValue();
                        queryId=query.getQueryId();
						%>
						<option value="<%=queryId%>">
							<%="[" + query.getGroupName() + "] " + query.getName() +" (" + queryId + ")" %>
						</option>
						<%
					}
					%>
                </select>
            </td>
        </tr>

        <tr><td colspan="2" class="data"> Action:
			<select name="ACTION">
				<option VALUE="GRANT">GRANT</option>
				<option VALUE="REVOKE">REVOKE</option>
				</select>
			</td>
		</tr>

		<tr>
            <td class="data">
	     <input type="submit" value="Submit">
	    </td>
	    <td class="data">
	     <a href="./showUserPrivileges.jsp">Show Current</a>
	    </td>        </tr>

    </table>
</form>



<%@ include file ="footer.html" %>