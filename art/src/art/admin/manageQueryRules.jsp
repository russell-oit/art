<%@ page import="art.utils.*,java.util.*" %>
<%@ include file ="headerAdmin.jsp" %>

<script language="javascript">
    <!--

    function deleteRule() {
        if (document.viewRule.QUERY_RULES.selectedIndex>=0) {
            if (window.confirm("Do you really want to delete this rule?")) {
                document.viewRule.action="execUpdRule.jsp";
                document.viewRule.RULEACTION.value="DELETE";
                document.viewRule.submit();
            } 
        } else {
            alert("You must select a rule");
        }
 
    }

    function addRule() {
		if (document.viewRule.RULE_NAME.selectedIndex>=0) {
			document.viewRule.action="updRule.jsp";
			document.viewRule.RULEACTION.value="NEW";
			document.viewRule.submit();
		} else {
            alert("You must select a rule");
        }
    }

    function goBackToEditQuery() {
        document.viewRule.action="editQuery.jsp";
        document.viewRule.submit();
    }

    //-->
</script>

<%
int queryId = Integer.parseInt(request.getParameter("QUERY_ID"));

ArtQuery aq=new ArtQuery();
Rule ar;
Iterator it;

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
                <select name="QUERY_RULES" size="4" multiple>
                    <%                                  
					 Map queryRules=aq.getQueryRules(queryId);
					it = queryRules.entrySet().iterator();					                                             
                      while (it.hasNext() ) {
						Map.Entry entry = (Map.Entry)it.next();
						ar=(Rule)entry.getValue();
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
        <tr><td colspan="2" class="data">  <input type="button" onclick="deleteRule()" value="Delete">
			<br><br>
            </td>
        </tr>
				
        <%/*
           Show List of rules that have not already been added to the query (for possible addition)
        */%>

        <tr><td colspan="2" class="data">
                <select name="RULE_NAME" size="4" >
                    <%
                    Map allRules=aq.getAvailableRules(queryId);
					it = allRules.entrySet().iterator();	
                    while (it.hasNext()) {
                       Map.Entry entry = (Map.Entry)it.next();
						ar=(Rule)entry.getValue();
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

        <tr><td colspan="2" class="data">  <input type="button" onclick="addRule()" value="New">
			<br><br>
            </td>
        </tr>
        <tr><td colspan="2" class="data">  <input type="button" onclick="goBackToEditQuery()" value="<< Back">
            </td>
        </tr>

    </table>
</p>


<%@ include file ="footer.html" %>
