<%@ page import="java.util.ResourceBundle, art.servlets.ArtDBCP;" %>

<jsp:useBean id="ue" scope="session" class="art.utils.UserEntity" />
<%   response.setHeader("Cache-control","no-cache"); %>
<%@ include file ="header.jsp" %>

  <fieldset>
    <legend><%=messages.getString("welcomeMessage") %></legend>
    <table style="width: 90%" class="centerTable art">
     <tr>
      <td  align="center" class="title" width="40%">

   <% if (request.getParameter("groupId") == null) { %>
      <label for="groupId"><%=messages.getString("availableGroups")%></label><br>
      <form name="queryForm" method="get" action="mshowGroups.jsp">
       <select name="groupId" >
        <c:forEach var='item' items='<%=ue.getAvailableQueryGroups()%>'>
	    <option value="${item.value}">${item.key}</option>
        </c:forEach>
   <% } else {
      int groupId = Integer.parseInt(request.getParameter("groupId"));
      %>
      <label for="groupId"><%=messages.getString("availableItems")%></label><br>
      <form name="queryForm" method="get" action="mshowParams.jsp">
          <select name="queryId" >
          <c:forEach var='item' items='<%=ue.getAvailableQueries(groupId)%>'>
	      <option value="${item.value}">${item.key}</option>
          </c:forEach>
   <% } %>

          </select>
       <input type="hidden" name="_mobile" value="true">
       <br><input type="submit" class="buttonup"  style="width:100px;" value="<%=messages.getString("nextButton")%>">
      </form>

      </td>
     </tr>
    </table>
  </fieldset>
<a href="<%= request.getContextPath() %>/logOff.jsp?_mobile=true"> <%=messages.getString("logOffLink")%></a>

<%@ include file ="footer.jsp" %>

