package art.output;

import art.utils.ArtQueryParam;
import java.io.PrintWriter;
import java.util.Map;

/**
<i>ArtOutputInterface</i> is implemented by the objects
which present the queries output (view modes). <br>

ART retrieves the SQL code and runs it in the target database.
The resultset is used to feed the objects  who
render the output (presentation layer).
These objects need to implement this interface.<br>

Briefly, the methods below are the ones used by the QueryExecute
servlet (or the scheduler) to pass the data to the object who is in charge to "display" the
output (in html, xml , xls,  etc).

<br>
To add a new <i>output type (View Mode)</i>, you need to:
<ol>
<li>Create a class that implements this interface, the class name must be
<code><i>xyz</i>Output</code>, where <i>xyz</i> is a free string.<br>
Copy the <code>xyzOutput.class</code> file in the same directory where
<code>ArtOutputInterface.class</code> is stored.
</li>
<li>Update, in  web.xml, the two context parameters:
<code>viewModeCodeList</code> and <code>viewModeDescriptionList</code> 
so that the new mode will show up in the list ov available view modes. 
</li>
<li> <i>(optional)</i> Add a context parameter <code><i>xyz</i>OutputMaxRows</code>
on the web.xml file if you want to limit the maximum number of rows.
</li>
</ol>
<br>
Technically, the QueryExecute servlet (or the scheduler job) receives the <i>output type</i> 
name (<i>xyz</i>) and tries to instatiate a class named <code>xyzOutput</code>, that is assumed 
to implement the   <i>ArtOutputInterface</i>.<br>
<br>
Have a look on the existing classes for a real word examples
(see xmlOutput.java for a simple example).<br>
 * 
 * @author Enrico Liboni
 */
public interface ArtOutputInterface {

    /**     
    Returns the name of this class as it should appear to users in the
    QueryParameters part.
    <br>Keep it short.<br>
    <i>unused (for future reference only, it will be used by QueryParameter sevlet in order
    to dynamically display the available output modes rather that loading the names
    from the web.xml file)</i>
     * 
     * @return he name of this class as it should appear to users
     */
    public String getName();   

    /**     
    Returns output mime Type (use <code>text/html</code> for html)
    produced by this object. Should use text/html;charset=utf-8<br>
    <br> Since ART 1.3.1     
     * 
     * @return output mime Type
     * @since 1.3.1
     */
    public String getContentType();   

    /**     
    Set the output stream.
    <br>Use it to print something: <br>
    <code>o.println("Hello Word!"); </code><br>
    will print <i>Hello Word!</i> to the user browser   
     * 
     * @param o 
     */
    public void setWriter(PrintWriter o); // output

    /**     
    Method invoked  with the query name as argument.
    <br>Used, for example, to show the query name in the output header or to create
    the file name.
     * 
     * @param s query name
     */
    public void setQueryName(String s);

    /**     
    Method invoked  with the username of the person executing the query or the job id for the job
    <br>Used to show the name in the username or job id in the output file name.
     * 
     * @param s username of the person executing the query or the job id for the job
     */
    public void setFileUserName(String s);

    /**     
    This method is invoked to set the maximum number of rows allowed to be output,
     * as stated in the web.xml file.
     * 
     * @param i maximum number of rows to be output
     */
    public void setMaxRows(int i);

    /**     
    Method invoked  with the number of columns of the result set.
    <br>Used, for example, to prepare a tabular output when the number of columns
    is required to be know from the beginning.
     * 
     * @param i number of columns of the result set
     */
    public void setColumnsNumber(int i);

    
    /**     
    Method invoked to set the default path to the export directory.
    <br>This method is called just before beginHeader(). Files should be created here.
     * 
     * @param s path to the export directory
     */
    public void setExportPath(String s);

    /**     
    Set parameters to be displayed in the report output
     * 
     * @param params parameters to be displayed in the report output
     */
    public void setDisplayParameters(Map<Integer, ArtQueryParam> params);

    /**     
    This method is invoked to state that the header begins. 
	* Initialization code should run here
     */
    public void beginHeader();

    /**     
    This method is invoked to set a column header name (from the result set meta data).
    <br>QueryExecute will call this method as many time as the number of columns in the result set.
     * 
     * @param s column header name
     */
    public void addHeaderCell(String s);

    /**     
    Method invoked to state that the header finishes.
     */
    public void endHeader();

    /**     
    Method invoked to state that the result set lines begins.
     */
    public void beginLines();

    /**     
    Method invoked to add a String value in the current line.
     * 
     * @param s String value to output
     */
    public void addCellString(String s);

    /**     
    Method invoked to add a Numeric value in the current line.
    <br>Used for NUMERIC, FLOAT, DOUBLE data types.
     * 
     * 
     * @param d Numeric value to output
     */
    public void addCellDouble(Double d);

    /**     
    Method invoked to add an integer value in the current line.<br>
    <br>Used for INTEGER, TINYINT, SMALLINT, BIGINT data types.
     * 
     * 
     * @param i Integer value to output
     */
    public void addCellLong(Long i);       // used for INTEGER, TINYINT, SMALLINT, BIGINT. //changed from primitive long to Long class to allow nulls

    /**     
    Method invoked to add a Date value in the current line.<br>
    <br>Used for DATE and TIMESTAMP.
     * 
     * @param d Date value to output
     */
    public void addCellDate(java.util.Date d);   // used for DATE and TIMESTAMP

    /** 
     * Method invoked to close the current line and open a new one.
    <br>This method should return true if the new line is allocatable, false if it is not possible to proceed 
    (for example MaxRows reached or an exception raises).
     * If false is returned, the QueryExecute servlet will stop to feed the object, it will call endLines() and close the result set.
     * 
     * @return <code>true</code> if can proceed to next record
     */
    public boolean newLine();

    /**     
    This method is invoked when the last row has been flushed.
    <br>     Usually, here the total number of rows are printed and open streams (files)
    are closed. 
     */
    public void endLines();

    /**     
    Returns the file name if a file is generated or null
    if no file is generated. <br>
    The file name must have the complete path.
    This is used by the scheduler to decide what to publish/e-mail: if
    the value is null the scheduler will publish/e-mail the object output stream, 
    otherwise the file is published/e-mailed.
     * 
     * @return the file name used if a file is generated or null if no file is generated
     */
    public String getFileName();

    /**     
    States if the standard html header and footer have to be printed to the output.
    <br>	If this method returns true, the QueryExecute servlet will print
    the standard html header (with query name and status box) and footer (total
    time elapsed).
    <br>	If it returns false, QueryExecute will not add anything around what it is printed
    by this object. 
    <br>
    For example, this should return false  if you are implementing a class that outputs non-html (like
    xml or pdf) or completely custom html<br>
    
    If the value returned is "true", the following javascript function is available
    to display a feedback on the page while the output is flushing:
    <code>o.println("&lt;script&gt;writeStatus(\"Message \");&lt;/script&gt;");</code>
    
    <br> Since ART 1.3.1     
     * 
     * @return <code>true</code> if standard query header and footer to be displayed
     */
    public boolean isShowQueryHeaderAndFooter();
}
