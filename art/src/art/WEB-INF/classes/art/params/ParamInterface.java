package art.params;

/**
 * Interface used by classes that render html for parameters in showParams.jsp. 
 * Used in showParams.jsp page
 * 
 * @author Enrico Liboni
 * @author Timothy Anyona
 */
public interface ParamInterface {

    /** 
     * Get the Html element ID.
     * @return the Html element ID.
     */
    public String getId();

    /** 
     * Get the Html element name.
     * @return the Html element name.
     */
    public String getHtmlName();

    /** 
     * Get the Parameter name (shown to end-user).
     * @return the Parameter name
     */
    public String getName();

    /** 
     * Get the html code required for capturing the parameter.  
     * @return the html code for capturing the parameter
     */
    public String getValueBox();
	
	/** 
     * Get the html code required for capturing the parameter.  
     * @return the html code for capturing the parameter
     */
    public String getValueBox(String value);

    /** 
     * Determine if this is a chained parameter (depends on another parameter).
     * @return <code>true</code> if this is a chained parameter
     */
    public boolean isChained();

    /** 
     * Get the html object id of the parameter that will trigger this one.
     * @return the html object id of the parameter that will trigger this one
     */
    public String getChainedId();

    /** 
     * Get the parameter class (integer, varchar etc).
     * @return the parameter class
     */
    public String getParamClass();

    /** 
     * Get the parameter's Short Description.
     * @return the parameter's Short Description.
     */
    public String getShortDescr();

    /** 
     * Get the parameter's Description.
     * @return the parameter's Description.
     */
    public String getDescr();

    /**
     * For chained parameters, html object id of the object that will drive the
     * parameter's value.
     * May be different from chained id.
     * 
     * @return the html object id of the object that will drive the parameter's value.
     */
    public String getChainedValueId();
}
