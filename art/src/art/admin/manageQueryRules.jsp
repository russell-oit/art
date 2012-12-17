<%@ page import="art.utils.*,java.util.*" %>
<%@ include file ="headerAdmin.jsp" %>

<script language="javascript">
    <!--
	
	function countSelected(list){
        var count=0;
        for(var i=0; i<list.options.length; i++ ){
            if(list.options[i].selected){
                count++;
            }
        }
        
        return count;
    }

    function deleteRule() {
        if (document.viewRule.QUERY_RULES.selectedIndex>=0) {
            if (window.confirm("Do you really want to delete this rule?")) {
                document.viewRule.action="execEditQueryRule.jsp";
                document.viewRule.RULEACTION.value="DELETE";
                document.viewRule.submit();
            } 
        } else {
            alert("Please select a rule");
        }
 
    }

    function addRule() {
		if (document.viewRule.RULE_NAME.selectedIndex>=0) {
			document.viewRule.action="editQueryRule.jsp";
			document.viewRule.RULEACTION.value="NEW";
			document.viewRule.submit();
		} else {
            alert("Please select a rule");
        }
    }
	
	function modifyRule() {
		if(countSelected(document.getElementById("queryRules"))>1){
            alert("Please select a single rule");
		} else if (document.viewRule.QUERY_RULES.selectedIndex>=0) {
			document.viewRule.action="editQueryRule.jsp";
			document.viewRule.RULEACTION.value="MODIFY";
			document.viewRule.submit();
		} else {
            alert("Please select a rule");
        }
    }

    function goBackToEditQuery() {
        document.viewRule.action="manageQuery.jsp";
        document.viewRule.submit();
    }

    //-->
</script>

<%
int queryId = Integer.parseInt(request.getParameter("QUERY_ID"));

ArtQuery aq=new ArtQuery();
Rule ar;

String ruleName;
String fieldName;
String description;
%>

<form name="viewRule" method="post">
    <input type="hidden" name="QUERY_ID" value="<%= request.getParameter("QUERY_ID")%>">
    <input type="hidden" name="RULEACTION" >
	
    <table align="center">        
        <tr><td class="title" colspan="2" > Edit Rule </td></tr>
		
        <%/*
           Show List of existing rules for the current query (for possible deletion)
        */%>
		
        <tr><td colspan="2" class="data">
                <small>Rule Name-Column Name-Rule Description</small>
            </td>
        </tr>
        <tr><td colspan="2" class="data">
                <select name="QUERY_RULES" id="queryRules" size="5" multiple>
                    <%                                  
					 Map<String, Rule> queryRules=aq.getQueryRules(queryId);
                      for (Map.Entry<String, Rule> entry : queryRules.entrySet()) {
						ar=entry.getValue();
                         ruleName=ar.getRuleName();
						 fieldName=ar.getFieldName();
						 description=ar.getDescription();
						%>
						<option value="<%=ruleName%>">
							<%=ruleName +"-" + fieldName +"-"+ description %>
						</option>
						<%						
						}                 
						%>
                </select>

        </tr>
        <tr><td colspan="2" class="data"> 								
				<input type="button" onclick="modifyRule()" value="Modify" name="ACTION">
				&nbsp;<input type="button" onclick="deleteRule()" value="Delete" name="ACTION">
			<br><br><br>
            </td>
        </tr>
				
        <%/*
           Show List of rules that have not already been added to the query (for possible addition)
        */%>
		
        <tr><td colspan="2" class="data">
                <select name="RULE_NAME" size="5" >
                    <%
                    Map<String, Rule> allRules=aq.getAvailableRules(queryId);
                    for (Map.Entry<String, Rule> entry : allRules.entrySet()) {
						ar=entry.getValue();
                         ruleName=ar.getRuleName();						 
						 description=ar.getDescription();
						%>
						<option value="<%=ruleName%>" >
							<%=ruleName +"-"+ description %>
						</option>
						<%                    
					 }                
                    %>
                </select>
            </td>
        </tr>

        <tr><td colspan="2" class="data">  <input type="button" onclick="addRule()" value="New" name="ACTION">
			<br><br>
            </td>
        </tr>
        <tr><td colspan="2" class="data">  <input type="button" onclick="goBackToEditQuery()" value="<< Back">
            </td>
        </tr>

    </table>
</p>


<%@ include file ="/user/footer.jsp" %>
