/**
 * Copyright (C) 2014 Enrico Liboni <eliboni@users.sourceforge.net>
 *
 * This file is part of ART.
 *
 * ART is free software; you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation, version 2 of the License.
 *
 * ART is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * ART. If not, see <http://www.gnu.org/licenses/>.
 */
package art.usergroup;

import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.core.convert.converter.Converter;

/**
 * Spring converter for string (user group id) to user group object
 *
 * @author Timothy Anyona
 */
public class StringToUserGroup implements Converter<String, UserGroup> {

	@Override
	public UserGroup convert(String s) {
		int id = NumberUtils.toInt(s);

		UserGroup group = new UserGroup();
		group.setUserGroupId(id);

		return group;
	}

}
