/**
TextArea box

Used in showParams.jsp page to display TEXT parameters or multi parameters that don't use LOVs
 */
package art.params;

import java.util.ResourceBundle;

/**
 * Class to display TEXT parameters or multi parameters that don't use LOVs.
 * 
 * @author Enrico Liboni
 */
public class HtmlTextArea implements ParamInterface {

    String paramHtmlId, paramHtmlName, paramName, paramShortDescr, paramDescr, defaultValue;

    /**
     * Constructor
     * 
     * @param paramHtmlId id of html element
     * @param paramHtmlName name of html element
     * @param paramName user friendly name of parameter
     * @param paramShortDescr short description of parameter
     * @param paramDescr description of parameter
     * @param defaultValue default value
     */
    public HtmlTextArea(String paramHtmlId, String paramHtmlName, String paramName, String paramShortDescr, String paramDescr, String defaultValue) {
        this.paramHtmlId = paramHtmlId;
        this.paramHtmlName = paramHtmlName;
        this.paramName = paramName;
        this.paramShortDescr = paramShortDescr;
        this.paramDescr = paramDescr;
        this.defaultValue = defaultValue;

        if (defaultValue == null || defaultValue.equals("null")) {
            this.defaultValue = "";
        }
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
    public String getHtmlName() {
        return paramHtmlName;
    }

    @Override
    public String getName() {
        return paramName;
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
		
        String vBox = "<textarea col=\"50\" rows=\"5\" "
                + " id=\"" + paramHtmlId + "\""
                + " name=\"" + paramHtmlName + "\">"
                + value + "</textarea>";

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
     * @return VARCHAR
     */
    @Override
    public String getParamClass() {
        return "VARCHAR";
    }

    @Override
    public String getShortDescr() {
        return paramShortDescr;
    }

    @Override
    public String getDescr() {
        return paramDescr;
    }

	@Override
	public void setMessages(ResourceBundle msgs) {
		//not used
	}
}
