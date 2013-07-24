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
	
	/**
	 * Set resource bundle with localised string.
	 * To enable display of localised "All" string
	 * @param msgs 
	 */
	public void setMessages(java.util.ResourceBundle msgs);
	
	/** 
     * Get the Html element default value
     * @return the Html element default value
     */
    public String getDefaultValue();
}
