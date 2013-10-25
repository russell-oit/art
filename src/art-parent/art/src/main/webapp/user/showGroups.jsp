<%@ page import="java.util.*,art.servlets.ArtConfig,art.utils.ArtQuery" %>
<%@ page import="org.apache.commons.lang3.StringUtils" %>
<%@ page import="org.apache.commons.lang3.math.NumberUtils" %>
<%@ taglib uri="http://ajaxtags.sourceforge.net/tags/ajaxtags" prefix="ajax"%>
<jsp:useBean id="ue" scope="session" class="art.utils.UserEntity" />

<%@ include file ="header.jsp" %>

<c:remove var="job" scope="session" />

<%
boolean canChangePassword=ue.isCanChangePassword();
String startQueryId;
int startQueryType=0;
String startQuery=ue.getStartQuery();
if(StringUtils.isNotBlank(startQuery)){
	ArtQuery aq=new ArtQuery();
	if(NumberUtils.isNumber(startQuery)){
		startQueryId=startQuery;
	} else {
		startQueryId=StringUtils.substringBefore(startQuery, "&");
	}
	startQuery="ExecuteQuery?queryId=" + startQuery + "&_isInline=true";
	startQueryType=aq.getQueryType(Integer.parseInt(startQueryId));
} else {
	startQuery="";
}
%>

<script type="text/javascript">
function setAction() {
	val = document.queryForm.typeId.value ;
	if (val == 111){
		document.queryForm.action = "showText.jsp";
	} else {
		document.queryForm.action = "showParams.jsp";
	}
	return true;
}
</script>

      <form id="queryForm" name="queryForm" method="get" action="showParams.jsp">
		  
  <fieldset>
    <legend><%=messages.getString("welcomeMessage") %></legend>
   <p>
    <table class="art centerTable" style="width: 90%">
     <tr>
      <td align="center" class="titletop" width="40%">

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
	Integer groupId;
	int defaultGroup=ue.getDefaultQueryGroup();	
	for (Map.Entry<String, Integer> entry : groups.entrySet()) {
		groupId=entry.getValue();
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
	
jQuery(document).ready(function($){

var qt=<%=startQueryType%>;
var sq="<%=startQuery%>";
if(sq!="" && !(qt==112 || qt==113 || qt==114)){
	$("#queryDescription").load(sq,function(responseText, statusText, xhr){
		//callback funtion for when jquery load has finished
		if(statusText=="success"){
			//make htmlgrid output sortable
			forEach(document.getElementsByTagName('table'), function(table) {
					if (table.className.search(/\bsortable\b/) != -1) {
						sorttable.makeSortable(table);
					}
			});
			
			//ensure tooltips are displayed for charts
			if(qt<0 || qt==110){
				olLoaded=1;
				overlib('');
			}
		} else if(statusText=="error"){
			alert("An error occurred: " + xhr.status + " - " + xhr.statusText);
		}

	});	
}	

//display spinner animation when any ajax activity happens
$("#systemWorking").ajaxStart(function(){
    $(this).show();
 }).ajaxStop(function(){
    $(this).hide();
 }).ajaxError(function(){
    $(this).hide();
 });
 
}); 

</script>


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
  String dataProviderUrl = request.getContextPath()+"/AjaxTagsDataProvider";
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


 <% if (!ue.getUsername().equals("public_user") && canChangePassword) { %>
   <!-- Change password for internal user -->
   <p><div align="center">
   <a href="<%= request.getContextPath() %>/user/changePassword.jsp"><small><%=messages.getString("header.link.changePassword")%></small></a>

   </div></p>
 <%}%>
<%@ include file ="footer.jsp" %>

