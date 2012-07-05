/**
Text input box

Used in showParams.jsp page to display varchar, integer and number parameters
 */
package art.params;

/**
 * Class to display VARCHAR, INTEGER and NUMBER parameters.
 * 
 * @author Enrico Liboni 
 */
public class HtmlTextInput implements ParamInterface {

    String paramHtmlId, paramHtmlName, paramName, paramClass, paramShortDescr, paramDescr, defaultValue;

    /**
     * Constructor.
     * 
     * @param paramHtmlId id of html element
     * @param paramHtmlName name of html element
     * @param paramName user friendly name of parameter
     * @param paramClass parameter class. VARCHAR.
     * @param paramShortDescr short description of parameter
     * @param paramDescr long description of parameter
     * @param defaultValue default value
     */
    public HtmlTextInput(String paramHtmlId, String paramHtmlName, String paramName, String paramClass, String paramShortDescr, String paramDescr, String defaultValue) {
        this.paramHtmlId = paramHtmlId;
        this.paramHtmlName = paramHtmlName;
        this.paramName = paramName;
        this.paramClass = paramClass;
        this.paramShortDescr = paramShortDescr;
        this.paramDescr = paramDescr;
        this.defaultValue = defaultValue;
    }

    @Override
    public String getChainedValueId() {
        return null;
    }

    @Override
    public String getId() {
        return paramHtmlId;
    }

    @Override
    public String getName() {
        return paramName;
    }

    @Override
    public String getHtmlName() {
        return paramHtmlName;
    }

    @Override
    public String getValueBox() {
        return getValueBox(defaultValue);
    }
	
	@Override
	public String getValueBox(String value) {
		if(value==null){
			//no parameter value override. use default value
			value=defaultValue;
		}
		
        String vBox = "<input type=\"text\" size=\"20\" maxlength=\"30\" "
                + " id=\"" + paramHtmlId + "\""
                + " name=\"" + paramHtmlName + "\""
                + " value = \"" + value + "\"> ";

        return vBox;
    }

    @Override
    public boolean isChained() {
        return false;
    }

    @Override
    public String getChainedId() {
        return null;
    }

    /** 
     * Get the parameter class
     * 
     * @return VARCHAR, INTEGER or NUMBER
     */
    @Override
    public String getParamClass() {
        return paramClass;
    }

    @Override
    public String getShortDescr() {
        return paramShortDescr;
    }

    @Override
    public String getDescr() {
        return paramDescr;
    }
}
