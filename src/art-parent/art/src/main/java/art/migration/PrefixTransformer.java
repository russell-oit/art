/*
 * ART. A Reporting Tool.
 * Copyright (C) 2018 Enrico Liboni <eliboni@users.sf.net>
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
package art.migration;

import com.univocity.parsers.annotations.HeaderTransformer;
import java.lang.reflect.Field;

/**
 * Adds a prefix to a field when exporting/importing an object using
 * univocity-parsers
 *
 * @author Timothy Anyona
 */
public class PrefixTransformer extends HeaderTransformer {
	//https://stackoverflow.com/questions/30527654/how-to-write-an-object-with-list-into-csv-file-using-univocity-writer

	private final String prefix;

	public PrefixTransformer(String... args) {
		prefix = args[0];
	}

	@Override
	public String transformName(Field field, String name) {
		return prefix + "_" + name;
	}
}
