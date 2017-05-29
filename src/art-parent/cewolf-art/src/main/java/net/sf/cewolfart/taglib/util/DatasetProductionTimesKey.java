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

import java.io.Serializable;
import java.util.Map;

/**
 * @author glaures
 */
class DatasetProductionTimesKey implements Serializable {

	static final long serialVersionUID = -6015635895045896059L;

	private final int key;

	public DatasetProductionTimesKey(String producerId, Map<String, Object> params){
		key = KeyGenerator.generateKey((Serializable) params);
	}

	public int getKey(){
		return key;
	}

	@Override
	public String toString() {
		return String.valueOf(key);
	}

	@Override
	public int hashCode() {
		return key;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o instanceof DatasetProductionTimesKey) {
			return key == ((DatasetProductionTimesKey) o).getKey();
		}
		return false;
	}

}
