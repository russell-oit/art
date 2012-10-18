<%@ page import="art.utils.*,java.io.*,java.util.*,art.servlets.ArtDBCP" %>

<%@ include file ="headerAdmin.jsp" %>


       <table align="center">
       <tr><td class="title" colspan="2" align="center">
            <br>ART Admin Console<br><img src="../images/settings-64px.jpg"> </td></tr>

   <%
          int adminLevel = ((Integer) session.getAttribute("AdminLevel") ).intValue();
	  if (adminLevel == 100) {
		  //super admin
   %>

	     <tr><td class="artLink" onclick="javascript:parent.location='editSettings.jsp'" onmouseover="javascript:setClass(this,'artLinkHighlight')" onmouseout="javascript:setClass(this,'artLink')">
		 Settings
		 </td>
	     <td class="data"> Define ART settings<br>
		<small> ART Repository, SMTP settings etc </small>
	     </td></tr>
   <%
	  }

	  if (!ArtDBCP.isArtSettingsLoaded()) {
   %>
	      <tr><td class="artLink" colspan="2"><small>You need to define <i>ART Settings</i> - database connection
	                   parameters and other settings - before being able to use ART.</small></td></tr>
   <%

	  } else {
		    /*
		     *   Manage Datasources
		     */
		if (adminLevel >40) {
			//senior admin and above
   %>
		    <tr><td class="artLink" onclick="javascript:parent.location='manageDatasources.jsp'" onmouseover="javascript:setClass(this,'artLinkHighlight')" onmouseout="javascript:setClass(this,'artLink')">
			Datasources
			</td>
		    <td class="data"> Define Datasource connection parameters<br>
		      <small> define datasources and refresh or review connections status </small>
		    </td></tr>
   <%
		}

		    /*
		     *   Manage Object Groups
		     */
		if (adminLevel >40) {
			//senior admin and above
   %>
		    <tr><td class="artLink" onclick="javascript:parent.location='manageObjectGroups.jsp'" onmouseover="javascript:setClass(this,'artLinkHighlight')" onmouseout="javascript:setClass(this,'artLink')">
			Object Groups
			</td>
            <td class="data"> Define Groups to which objects belong</td></tr>
   <%
		}
		    /*
		     *   Manage Queries
		     */
		if (adminLevel >5) {
			//junior admin and above
   %>
		    <tr><td class="artLink" onclick="javascript:parent.location='manageQueries.jsp'" onmouseover="javascript:setClass(this,'artLinkHighlight')" onmouseout="javascript:setClass(this,'artLink')">
			Objects
			</td>
		    <td class="data"> <span style="color:red">Manage Queries, Dashboards, Text objects</span> <br>
			  <small> create, update, delete, copy SQL queries, dashboards or text objects
			   </small>
		   </td></tr>
   <%
		}
		    /*
		     *   Manage User Groups
		     */
		if (adminLevel >30) {
			//standardd admin and above
   %>
		    <tr><td class="artLink" onclick="javascript:parent.location='manageUserGroups.jsp'" onmouseover="javascript:setClass(this,'artLinkHighlight')" onmouseout="javascript:setClass(this,'artLink')">
			User Groups
			</td>
		    <td class="data"> Define Groups to which users belong</td></tr>
   <%
		}
		    /*
		     *   Manage Users
		     */
		if (adminLevel >30) {
			//standardd admin and above
   %>
		    <tr><td class="artLink" onclick="javascript:parent.location='manageUsers.jsp'" onmouseover="javascript:setClass(this,'artLinkHighlight')" onmouseout="javascript:setClass(this,'artLink')">
			Users
			</td>
		    <td class="data"> Define Users</td></tr>
			<%
		}
		    /*
		     *   Manage User-User Group Membership
		     */
		if (adminLevel > 10) {
			//mid admin and above
   %>
		    <tr><td class="artLink" onclick="javascript:parent.location='manageUserGroupAssignment.jsp'" onmouseover="javascript:setClass(this,'artLinkHighlight')" onmouseout="javascript:setClass(this,'artLink')">
			User Group Membership
			</td>
		    <td class="data"> Assign users to user groups<br>
				<small> users can belong to zero, one, or many user groups </small>
		    </td></tr>

   <%
		}
		    /*
		     *   Manage User/User Group Privileges
		     */
		if (adminLevel > 10) {
			//mid admin and above
   %>
		    <tr><td class="artLink" onclick="javascript:parent.location='manageUserPrivileges.jsp'" onmouseover="javascript:setClass(this,'artLinkHighlight')" onmouseout="javascript:setClass(this,'artLink')">
                    User/User Group Privileges &nbsp;&nbsp;
			</td>
		    <td class="data"> Grant/Revoke to users/user groups the right to access objects/object groups</td></tr>
   <%
		}
		    /*
		     *   Manage Admin Privileges
		     */
		if (adminLevel >30) {
			//standard admin and above
   %>
		    <tr><td class="artLink" onclick="javascript:parent.location='manageAdminPrivileges.jsp'" onmouseover="javascript:setClass(this,'artLinkHighlight')" onmouseout="javascript:setClass(this,'artLink')">
			Admin Privileges
			</td>
		    <td class="data"> Grant/Revoke to junior/mid Admins the right to act on groups and datasources </td></tr>
   <%
		}
		    /*
		     *   Manage Rules
		     */
		if (adminLevel >40) {
			//senior admin and above
   %>
		    <tr><td class="artLink" onclick="javascript:parent.location='manageRuleDefinitions.jsp'" onmouseover="javascript:setClass(this,'artLinkHighlight')" onmouseout="javascript:setClass(this,'artLink')">
			Rules
			</td>
		    <td class="data"> Define Rule names</td></tr>

			 <%
		}
		    /*
		     *   Manage Rule values for given users
		     */
		if (adminLevel >30) {
			//standard admin and above
   %>
		    <tr><td class="artLink" onclick="javascript:parent.location='manageUserRules.jsp'" onmouseover="javascript:setClass(this,'artLinkHighlight')" onmouseout="javascript:setClass(this,'artLink')">
			Rule Values
			</td>
		    <td class="data"> Set rule values for users</td></tr>
   <%
		}
		/*
		     *   Manage Shared Jobs
		     */
		if (adminLevel >5) {
			//junior admin and above
   %>
		    <tr><td class="artLink" onclick="javascript:parent.location='manageSharedJobs.jsp'" onmouseover="javascript:setClass(this,'artLinkHighlight')" onmouseout="javascript:setClass(this,'artLink')">
			Shared Jobs
			</td>
		    <td class="data">Grant/Revoke access to shared jobs</td></tr>
   <%
		}

		/*
		     *   Manage Job Schedules
		     */
		if (adminLevel >5) {
			//junior admin and above
   %>
		    <tr><td class="artLink" onclick="javascript:parent.location='manageJobSchedules.jsp'" onmouseover="javascript:setClass(this,'artLinkHighlight')" onmouseout="javascript:setClass(this,'artLink')">
			Schedules
			</td>
		    <td class="data">Define schedules that can be used when creating jobs</td></tr>
   <%
		}
           }
   %>
	   </table>

<%@ include file ="footer.html" %>
