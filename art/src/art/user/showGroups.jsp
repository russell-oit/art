<%@ page import="java.util.*, art.servlets.ArtDBCP;" %>
<%@ taglib uri="http://ajaxtags.sourceforge.net/tags/ajaxtags" prefix="ajax"%>
<jsp:useBean id="ue" scope="session" class="art.utils.UserEntity" />

<%@ include file ="header.jsp" %>

<c:remove var="job" scope="session" />

<%
boolean canChangePassword=ue.isCanChangePassword();
%>

<script language="javascript">
<!--

function setAction() {
	val = document.queryForm.typeId.value ;
	if (val == 111){
		document.queryForm.action = "showText.jsp";
	} else {
		document.queryForm.action = "showParams.jsp";
	}
	return true;
}

-->

</script>

      <form id="queryForm" name="queryForm" method="get" action="showParams.jsp">

  <fieldset>
    <legend><%=messages.getString("welcomeMessage") %></legend>
   <p>
    <table width="90%" align="center" class="art">
     <tr>
      <td  align="center" class="title" width="40%">

       <label for="groupId"><%=messages.getString("availableGroups")%></label><br>
       <% // this allows to focus public_user in one group only
         Map<String,Integer> groups;
	 if (request.getParameter("groupId") == null)  {
             groups = ue.getAvailableQueryGroups();
         } else  {
             groups = ue.getAvailableQueryGroup(Integer.parseInt(request.getParameter("groupId")));
         }

       %>

       <select id="groupId" name="groupId" size="5">
        <%
    Iterator it2 = groups.entrySet().iterator();
	Integer groupId;
	int defaultGroup=ue.getDefaultQueryGroup();	
	while(it2.hasNext()) {
		Map.Entry entry = (Map.Entry)it2.next();
		groupId=(Integer)entry.getValue();
   %>
   <option value="<%=groupId%>" <%=(groupId==defaultGroup)?"selected":""%> ><%=entry.getKey()%></option>
	<%
   }
   %>
       </select>

       <br><br>
       <label for="queryId"><%=messages.getString("availableItems")%></label><br>
	  <select id="queryId" name="queryId" disabled="disabled" size="10">
	    <option value="">...</option>
	  </select>
	  <br>&nbsp;
      </td>
      <td align="center" class="title">
         <div id="queryDescription" align="left" class="queryDescr">
	   <fieldset>
	    <b>
	     &nbsp;<br>
	     &nbsp;<br>
	     &nbsp;<br>
	     &nbsp;<br>
	     &nbsp;<br>
	     &nbsp;<br>
	     &nbsp;<br>
	    </fieldset>
          </div>
      </td>
     </tr>
    </table>
  <p align="center">
<input type="submit" id="submitButton" class="buttonup" onClick="javascript:setAction()" onMouseOver="javascript:btndn(this);" onMouseOut="javascript:btnup(this);" style="width:100px;" disabled value="<%=messages.getString("nextButton")%>">
  </p>
  </fieldset>


      </form >


<script type="text/javascript">

function activateSubmitButton() {
  document.getElementById("submitButton").disabled = false;
artRemoveWork();
}

function deactivateSubmitButton() {
  document.getElementById("submitButton").disabled = true;
artRemoveWork();
}
</script>

<%
  String dataProviderUrl = request.getContextPath()+"/XmlDataProvider";
 %>

<ajax:select
  baseUrl="<%=dataProviderUrl%>"
  source="groupId"
  target="queryId"
  parameters="action=queries,groupId={groupId}"
  postFunction="deactivateSubmitButton"
  emptyOptionName="........"
  emptyOptionValue=""
  preFunction="artAddWork"
	executeOnLoad="true"
  />

<ajax:htmlContent
  baseUrl="<%=dataProviderUrl%>"
  source="queryId"
  target="queryDescription"
  parameters="action=querydescr,queryId={queryId}"
  eventType="change"
  postFunction="activateSubmitButton"
preFunction="artAddWork"
  />


 <% if (ue.isInternalAuth() && !ue.getUsername().equals("public_user") && canChangePassword) { %>
   <!-- Change password for internal user -->
   <p><div align="center">
   <a href="<%= request.getContextPath() %>/user/changePassword.jsp"><small><%=messages.getString("changePassword")%></small></a>

   </div></p>
 <%}%>
<%@ include file ="footer.jsp" %>

