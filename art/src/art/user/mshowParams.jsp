<%@ page import="java.util.ResourceBundle, art.servlets.ArtDBCP,art.params.*;" %>

<jsp:useBean id="ue" scope="session" class="art.utils.UserEntity" />
<%   response.setHeader("Cache-control","no-cache"); %>
<%@ include file ="header.jsp" %>

<jsp:useBean id="aq" scope="request" class="art.utils.ArtQuery" />
<!--  query_name may be from query_id... -->
<jsp:setProperty name="aq" property="*" />


<%
   aq.setUsername(ue.getUsername());
   aq.create();
   int queryType = aq.getQueryType();
   int queryId=aq.getQueryId();
   String queryName=aq.getName();
   boolean notSupported=false;
%>

<c:if test="${aq.queryType == 110}">
    <b>Dashboards are not supported in Handset Devices </b>
</html>
<% notSupported = true; %>
</c:if>
<c:if test="${aq.queryType == 111}">
    <jsp:forward page="showText.jsp">
        <jsp:param name="queryId" value="<%= queryId %>"/>
    </jsp:forward>
</c:if>

<c:forEach var='parameter' items='${aq.paramList}'>
    <c:if test="${parameter.chained}">
        <b>Chained parameters are not supported in Handset Devices</b>
        <% notSupported = true; %>
    </html>
</c:if>
</c:forEach>

<% if  (notSupported) { return; } %>

<script type="text/javascript" src="<%= request.getContextPath() %>/js/date.js"></script>
<script type="text/javascript" src="<%= request.getContextPath() %>/js/art-checkParam_js.jsp"></script>

<div id="params">

    <fieldset>
        <legend><%=messages.getString("enterParams") %></legend>

        <form name="artparameters" id="paramForm" action="ExecuteQuery"  method="post">
            <input type="hidden" name="_mobile" value="true">

            <table class="art" align="Center" >
                <tr>
                    <td colspan="3" class="title" >

                        <br> <%=queryName%> <br><br>

                    </td>
                </tr>
                <%
                StringBuffer validateJS = new StringBuffer(128);
                %>
                <c:forEach var='parameter' items='${aq.paramList}'>
                    <% art.params.ParamInterface param = (art.params.ParamInterface) pageContext.getAttribute("parameter");
                     if ( param.getParamClass().equals("INTEGER") || param.getParamClass().equals("NUMBER") || param.getParamClass().equals("DATE") || param.getParamClass().equals("DATETIME") )
                        validateJS.append("ValidateValue('"+param.getParamClass()+"', '"+param.getName()+"', document.getElementById('"+param.getId()+"').value ) && ");
                    %>
                    <tr>
                        <td class="data" colspan="2">
                            ${parameter.name}
                            <input type="hidden" id="${parameter.id}_NAME" name="_${parameter.htmlName}_NAME" value="${parameter.name}">
                            <br/>
                            <c:choose>
                                <c:when test="${parameter.paramClass == 'DATE'}">
                                    <!-- skip js code for calendar (ugly...) -->
                                    <%
                                       String box = param.getValueBox();
                                       int i = box.indexOf("<img ");
                                    %>
                                    <%=box.substring(0, i)%>
                                </c:when>
                                <c:otherwise>
                                    ${parameter.valueBox}
                                </c:otherwise>
                            </c:choose>

                            <c:choose>
                                <c:when test="${parameter.paramClass == 'INTEGER'}">
                                    <img src="<%= request.getContextPath() %>/images/123.png" alt="integer">
                                </c:when>
                                <c:when test="${parameter.paramClass == 'NUMBER'}">
                                    <img src="<%= request.getContextPath() %>/images/1dot23.png" alt="number">
                                </c:when>
                                <c:when test="${parameter.paramClass == 'DATE'}">
                                    <img src="<%= request.getContextPath() %>/images/calendar.png" alt="date">
                                </c:when>
                                <c:when test="${parameter.paramClass == 'DATETIME'}">
                                    <img src="<%= request.getContextPath() %>/images/calendar.png" alt="datetime">
                                </c:when>
                                <c:otherwise>
                                    <img src="<%= request.getContextPath() %>/images/abc.png" alt="text">
                                </c:otherwise>
                            </c:choose>
                        </td>
                        <td class="data"> ${parameter.shortDescr}  </td>
                        <!-- <td class="data">
	 <input type="button" class="buttonup" onClick="alert('${parameter.descr}')" onMouseOver="javascript:btndn(this);" onMouseOut="javascript:btnup(this);"  value="...">  
                        </td>
	  -->
                    </tr>
                </c:forEach>
                <tr>
                    <td colspan="3" class="attr">

                        <% /* Ugly code to decode query type... see art_tables.sql for details */
                            switch ( queryType ) {
                               case 0  :   // normal query
                               case 101:  // crosstab
                        %>    <span style="font-size:95%"><i><%=messages.getString("viewMode")%></i></span>
                        <select name="viewMode" size="1">
                            <option value="html"> <%=messages.getString("html")%> </option>
                            <option value="htmlPlain"> <%=messages.getString("htmlPlain")%> </option>
                            <option value="pdf"> <%=messages.getString("pdf")%> </option>
                            <option value="xls"> <%=messages.getString("xls")%> </option>
                            <option value="xlsZip"> <%=messages.getString("xlsZip")%> </option>
                        </select>
                        <br><input type="checkbox" name="_showParams"> <%=messages.getString("showParams")%>

                        <%
                        break;
                        case 103: // normal html
                        case 102: // xtab html
                        %> 
                        <input type="checkbox" name="_showParams"> <%=messages.getString("showParams")%>

                        <%
                  break;
                  
               }
			   
			   
				if (queryType<0) { %>
                        <small>
                            <i><%=messages.getString("graphType")%></i>
                            <%=messages.getString("graph"+queryType)%>
                        </small>
                        <input type="hidden" name="_GRAPH_ID" value="<%=(queryType*(-1))%>">
                        <input type="hidden" name="_GRAPH_SIZE" value="Default">                        
                        <input type="hidden" name="_showLegend" value="true">
                        <%}%>
                    </td>
                </tr>
                <tr>
                    <td colspan="3" class="data">
                        <input type="hidden" name="QUERY_ID" value="<%=queryId%>">
						<INPUT TYPE=hidden name="QUERY_NAME" VALUE="<%=queryName%>">						
						<INPUT TYPE=hidden name="QUERY_TYPE" VALUE="<%=queryType%>">

                        <div align="center" valign="middle">
                            <input type="submit" onClick="javascript:return(<%= validateJS.toString()%> returnTrue() )" class="buttonup"  style="width:100px;" value="<%=messages.getString("executeQueryButton")%>">
                        </div>

                    </td>
                </tr>
            </table>

        </form>

    </fieldset>
    <c:if test="${ue.username != 'public_user' }">
        <a href="<%= request.getContextPath() %>/logOff.jsp?_mobile=true"> <%=messages.getString("logOffLink")%></a>
    </c:if>
</div>

<%@ include file ="footer.jsp" %>

