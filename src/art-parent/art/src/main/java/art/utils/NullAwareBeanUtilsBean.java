/*
 * Copyright (C) 2014 Enrico Liboni <eliboni@users.sourceforge.net>
 *
 * This file is part of ART.
 *
 * ART is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation, version 2 of the License.
 *
 * ART is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with ART. If not, see <http://www.gnu.org/licenses/>.
 */
package art.utils;

import java.lang.reflect.InvocationTargetException;
import org.apache.commons.beanutils.BeanUtilsBean;

/**
 *
 * @author Timothy Anyona
 */
public class NullAwareBeanUtilsBean extends BeanUtilsBean {

	//https://stackoverflow.com/questions/1301697/helper-in-order-to-copy-non-null-properties-from-object-to-another-java
	
	 @Override
	public void copyProperty(Object dest, String name, Object value)
			throws IllegalAccessException, InvocationTargetException {
		if (value == null) {
			return;
		}
		
		super.copyProperty(dest, name, value);
	}
}
