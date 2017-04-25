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
package art.utils;

import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

/**
 * Spring converter for string to integer. To override the default converter
 * which throws an exception with an empty string. This converter converts an
 * empty string to 0.
 *
 * @author Timothy Anyona
 */
@Component
public class StringToInteger implements Converter<String, Integer> {

	//for default converter, see http://docs.spring.io/spring/docs/3.0.0.RC2/reference/html/ch05s05.html
	@Override
	public Integer convert(String s) {
		return NumberUtils.toInt(s);
	}
}
