<%@ page import="java.util.ResourceBundle, art.servlets.ArtDBCP;" %>
<jsp:useBean id="ue" scope="session" class="art.utils.UserEntity" />

<%@ include file="header.jsp" %>


<%
String msg;

msg="JPivot had an error...<br><br>";

        Throwable e = (Throwable) request.getAttribute("javax.servlet.jsp.jspException");
      while (e != null) {	  
		msg=msg + e.toString() + "<br><br>";

        Throwable prev = e;
        e = e.getCause();
        if (e == prev)
          break;
      }
%>

 <table align=center>
  <tr>
    <td colspan=2 class=data align=center> <b><span style="color:red">Error! </span></b> 
    </td>
  </tr>
  <tr>
    <td class=attr> Message:
    </td>
    <td class=data> <%=msg%>
    </td>
  </tr>
 </table>
 

<%@ include file ="footer.jsp" %>
