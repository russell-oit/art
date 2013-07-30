<%@ page import="java.util.ResourceBundle, art.servlets.ArtDBCP;" %>
<jsp:useBean id="ue" scope="session" class="art.utils.UserEntity" />

<%@ include file="header.jsp" %>


<%
String msg;

msg=messages.getString("jpivotError");

        Throwable e = (Throwable) request.getAttribute("javax.servlet.jsp.jspException");
      while (e != null) {	  
		msg=msg + e.toString() + "<br><br>";

        Throwable prev = e;
        e = e.getCause();
        if (e == prev)
          break;
      }
%>

 <table class="centerTableAuto">
  <tr>
    <td colspan="2" class="data" align="center"> <b><span style="color:red"> <%=messages.getString("error")%> </span></b> 
    </td>
  </tr>
  <tr>
    <td class=attr> <%=messages.getString("message")%>
    </td>
    <td class=data> <%=msg%>
    </td>
  </tr>
 </table>
 

<%@ include file ="footer.jsp" %>
