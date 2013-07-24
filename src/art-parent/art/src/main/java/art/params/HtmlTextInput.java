/**
 * Copyright 2001-2013 Enrico Liboni <eliboni@users.sourceforge.net>
 *
 * This file is part of ART.
 *
 * ART is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, version 2 of the License.
 *
 * ART is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with ART.  If not, see <http://www.gnu.org/licenses/>.
 */
/**
Text input box

Used in showParams.jsp page to display varchar, integer and number parameters
 */
package art.params;

import java.util.ResourceBundle;

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
    public String getDefaultValue() {
        return defaultValue;
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

	@Override
	public void setMessages(ResourceBundle msgs) {
		//not used
	}
}
