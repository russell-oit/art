/*
 * ART. A Reporting Tool.
 * Copyright (C) 2017 Enrico Liboni <eliboni@users.sf.net>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package art.holiday;

import java.sql.SQLException;
import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

/**
 * Spring converter for string (holiday id) to holiday object
 *
 * @author Timothy Anyona
 */
@Component
public class StringToHoliday implements Converter<String, Holiday> {

	private static final Logger logger = LoggerFactory.getLogger(StringToHoliday.class);

	@Autowired
	private HolidayService holidayService;

	@Override
	public Holiday convert(String s) {
		int id = NumberUtils.toInt(s);

		Holiday holiday = null;
		try {
			holiday = holidayService.getHoliday(id);
		} catch (SQLException ex) {
			logger.error("Error", ex);
		}

		return holiday;
	}

}
