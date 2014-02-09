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
package art.utils;

import java.util.Date;
import org.joda.time.DateTime;
import org.joda.time.Days;
import org.joda.time.LocalDate;

/**
 * Class for custom EL functions for use in jsp pages
 *
 * @author Timothy Anyona
 */
public class ELFunctions {
	//see https://stackoverflow.com/questions/13588530/display-date-diff-in-jsp
	//http://digitaljoel.nerd-herders.com/2011/03/17/how-to-create-a-custom-taglib-containing-an-el-function-for-jsp/
	
	//https://stackoverflow.com/questions/3802893/number-of-days-between-two-dates-in-joda-time

	public static int daysBetween(Date before, Date after) {
		if (before == null || after == null) {
			return Integer.MAX_VALUE;
		}

		//consider "civil" days rather than "mathematical" days. so use LocalDate and not DateTime
		return Days.daysBetween(new LocalDate(before.getTime()), new LocalDate(after.getTime())).getDays();
	}

	public static int daysUntilToday(Date date) {
		if (date == null) {
			return Integer.MAX_VALUE;
		}

		return Days.daysBetween(new LocalDate(date.getTime()), new LocalDate()).getDays();
	}
}
