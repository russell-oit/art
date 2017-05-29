/* ================================================================
 * Cewolf : Chart enabling Web Objects Framework
 * ================================================================
 *
 * Project Info:  http://cewolf.sourceforge.net
 * Project Lead:  Guido Laures (guido@laures.de);
 *
 * (C) Copyright 2002, by Guido Laures
 *
 * This library is free software; you can redistribute it and/or modify it under the terms
 * of the GNU Lesser General Public License as published by the Free Software Foundation;
 * either version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this
 * library; if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330,
 * Boston, MA 02111-1307, USA.
 */

package net.sf.cewolfart.taglib.tags;

import java.util.HashMap;
import java.util.Map;

/** 
 * Abstract base class for all tags referencing a scripting variable by its ID
 * which they want to parameterize further.
 * @see Parameterized
 * @author  Guido Laures 
 */
public abstract class AbstractParameterizedObjectTag extends AbstractObjectTag implements Parameterized {
	
	private static final long serialVersionUID = 1L;
    
    private Map<String,String> params = new HashMap<String,String>();
    private Map<String,Object> objParams = new HashMap<String,Object>();

    protected void reset(){
		// params.clear() does NOT work; references to params are kept elsewhere
		params = new HashMap<String,String>();
		objParams = new HashMap<String,Object>();
    }

    protected Map<String,Object> getParameters() {
		return objParams;
    }

    protected Map<String,String> getStringParameters() {
		return params;
    }

    public void addParameter (String name, String value, Object objValue) {
		if (value != null) {
			params.put(name, value);
			objParams.put(name, value);
		}
		if (objValue != null) {
			objParams.put(name, objValue);
		}
    }
}
