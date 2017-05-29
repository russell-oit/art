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
package net.sf.cewolfart.util;

import java.util.List;

/**
 * Renders the state of a StateDescriptor as an HTML table.
 *
 * @see net.sf.cewolfart.util.StateDescriptor
 * @author glaures
 */
public class HTMLStateTable {

	public static String getStateTable(StateDescriptor sd) {
		StringBuffer res = new StringBuffer("<TABLE BORDER='1'>");
		res.append("<TR><TH colspan='2'>" + sd.getID() + "</TH></TR>");
		List<Object> params = sd.getStateParameters();
		for (Object param : params) {
			Object val = sd.getState(param);
			res.append("<TR><TD>" + param + "</TD><TD>" + val + "</TD></TR>");
		}
		res.append("</TABLE>");
		return res.toString();
	}

}
