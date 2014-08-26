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

package art.dbutils;

import art.dbcp.DataSource;
import java.util.LinkedHashMap;

/**
 *
 * @author Timothy Anyona
 */
public class DbConnections {
	private static LinkedHashMap<Integer, DataSource> connectionPools; //use a LinkedHashMap that should store items sorted as per the order the items are inserted in the map...
	
}
