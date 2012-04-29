<%@ include file ="headerAdmin2.jsp" %>

<table align="center" width="90%">

    <tr><td class="title">ART Error codes
        </td></tr>
    <tr><td>
            <b>100 Connection null or closed</b>
            <br><i>Reason:</i>
            <br><small> The database connection is unavailable or, most likely, the session has expired
                and the connection was closed.
            </small>
            <br><i>Solution:</i>
            <br><small> Log in again to restore the connection. If it does not work, check
                the ART Database connection parameters in the art.props file and make sure
                the ART Database is up and running.
            </small>
        </td></tr>

    <tr><td>
            <b>110 Invalid characters</b>
            <br><i>Reason:</i>
            <br><small>You have attempted to insert invalid characters in some text fields.
            </small>
            <br><i>Solution:</i>
            <br><small>Go back and make sure not to use characters like ' " &lt; and &gt;
            </small>
        </td></tr>

    <tr><td>
            <b>120 Cannot create a Query Header or SQL from a given query id</b>
            <br><i>Reason:</i>
            <br><small>Probably the query was deleted by another admin while you were
                working on it.
            </small>
            <br><i>Solution:</i>
            <br><small>!
            </small>
        </td></tr>

    <tr><td>
            <b>130 Record Does Not Exists</b>
            <br><i>Reason:</i>
            <br><small>You are attempting to modify or delete a record that does not exist
            </small>
            <br><i>Solution:</i>
            <br><small>Make sure the record still exists and that no one has deleted it.
            </small>
        </td></tr>


    <tr><td>
            <b>140 Error storing ART properties</b>
            <br><i>Reason:</i>
            <br><small>You are attempting to modify or insert ART properties but ART is not
                able to save the properties correcly.
            </small>
            <br><i>Solution:</i>
            <br><small>Make sure the user that executes the servlet engine is able to write
                to the art.props file in the WEB-INF directory
            </small>
        </td></tr>

    <tr><td>
            <b>145 Authentication failure when modifying ART properties</b>
            <br><i>Reason:</i>
            <br><small>You must specify previous art database username and
                password in order to replace existing properties
            </small>
            <br><i>Solution:</i>
            <%
              ServletContext ctx   = getServletConfig().getServletContext();
              String baseDir = ctx.getRealPath("");
              String sep = java.io.File.separator;
              String propsFile = baseDir+sep+"WEB-INF"+sep+"art.props";
            %>
            <br><small>If you do not know previous password, edit the file art.props
                in the WEB-INF directory of ART web application (<i><%=propsFile%></i>).
            </small>
        </td></tr>

    <tr><td>
            <b>150 Error in ART properties</b>
            <br><i>Reason:</i>
            <br><small>The ART database connection parameters are not correct or
                the JDBC driver has not been loaded by the servlet engine
            </small>
            <br><i>Solution:</i>
            <br><small>Double-check the values and make sure the JDBC driver
                is available to the servlet engine.
            </small>
        </td></tr>

    <tr><td>
            <b>160 Error while creating/updating query</b>
            <br><i>Reason:</i>
            <br><small>ART was not able to update or create a query
            </small>
            <br><i>Solution:</i>
            <br><small>Check the error message. Likely one of the values you specified
                is not correct (e.g. you specified a query name that already exists etc.)
            </small>
        </td></tr>


    <tr><td>
            <b>199 Unhandled Generic Exception</b>
            <br><i>Reason:</i>
            <br><small>An exception raised during the execution of a server side jsp or servlet.
            </small>
            <br><i>Solution:</i>
            <br><small>That's probably a bug. Please try to reproduce it and contact the ART administrator.
            </small>
        </td></tr>

    <tr><td>
            <b>200  Generic Exception</b>
            <br><i>Reason:</i>
            <br><small>An exception raised during the execution of a server side jsp or servlet.
            </small>
            <br><i>Solution:</i>
            <br><small>Read the error message and fix the error
            </small>
        </td></tr>

    <!-- template
    <tr><td>
    <b>x</b>
    <br><i>Reason:</i>
    <br><code></code>
    <br><i>Solution:</i><br>
    <br><code></code>
    </td></tr>
    -->

</table>
<%@ include file ="footer.html" %>
