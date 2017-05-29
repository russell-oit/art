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

import java.io.Serializable;

/** 
 * Tag &lt;param&gt; which defines a key/value pair which is set in the parent
 * tag. This must implement the Parameterized interface.
 * @see Parameterized
 * @author  Guido Laures 
 */
public class ParamTag extends CewolfBodyTag {

	static final long serialVersionUID = 3146335006040952717L;

    private String name;
    private String value;
    private Object objValue;

    public int doEndTag() {
		//Parameterized parent = (Parameterized)getParent();
		// support for conditional use of cewolf:param inside of <c:if> tags
		Parameterized parent = (Parameterized)findAncestorWithClass(this, Parameterized.class);
		parent.addParameter(name, value, objValue);
		return doAfterEndTag(EVAL_PAGE);
    }

    protected void reset() {
    }

    public void setName (String name) {
        this.name = name;
    }
/*
    public void setValue (Serializable val) {
        this.value = val;
    }
*/
    public void setValue (String val) {
        value = val;
        objValue = val;
    }

    public void setValue (Object val) {
		//System.out.println(val.getClass().getName()+"="+val);
		if (val instanceof String) {
			value = (String) val;
			objValue = null;
		} else if (val instanceof Number) {
			value = val.toString();
			objValue = null;
		} else {
			objValue = val;
			value = null;
		}
    }
}
