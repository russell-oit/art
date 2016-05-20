/**
 * Copyright (C) 2016 Enrico Liboni <eliboni@users.sourceforge.net>
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

package art.reportgroup;

import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.core.convert.converter.Converter;

/**
 * Spring converter for string (report group id) to report group object
 * 
 * @author Timothy Anyona
 */
public class StringToReportGroup implements Converter<String, ReportGroup> {

	@Override
	public ReportGroup convert(String s) {
		int id = NumberUtils.toInt(s);
		
		//get value from database instead of new object with only id populated? not necessary?
		ReportGroup group=new ReportGroup();
		group.setReportGroupId(id);
		
		return group;
	}
	
}
