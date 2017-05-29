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

package net.sf.cewolfart.taglib.util;

import java.util.*;
import java.util.Map.Entry;

/**
 * @author glaures
 */
public class DatasetProductionTimeStore extends HashMap<DatasetProductionTimesKey,Date> {

	static final long serialVersionUID = -9086317504718166157L;

    private static final DatasetProductionTimeStore instance = new DatasetProductionTimeStore();

    public static final DatasetProductionTimeStore getInstance() {
        return instance;
    }

    public boolean containsEntry (String producerId, Map<String, Object> params) {
        return containsKey(new DatasetProductionTimesKey(producerId, params));
    }

    public void addEntry (String producerId, Map<String, Object> params, Date produceTime) {
        put(new DatasetProductionTimesKey(producerId, params), produceTime);
    }

    public void removeEntry (String producerId, Map<String, Object> params) {
        remove(new DatasetProductionTimesKey(producerId, params));
    }

    public Date getProductionTime (String producerId, Map<String, Object> params) {
        return get(new DatasetProductionTimesKey(producerId, params));
    }

    public String paramsToString (Map<String, Object> params){
    	StringBuffer buf = new StringBuffer("[");
		for(Entry<String, Object> entry:params.entrySet()){
    		buf.append(entry.getKey()).append(":").append(entry.getValue());
    	}
    	buf.append("]");
    	return buf.toString();
    }

}
