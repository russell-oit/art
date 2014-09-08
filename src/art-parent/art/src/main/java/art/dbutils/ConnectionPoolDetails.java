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

import java.io.Serializable;

/**
 * Represents a connection pool's details, providing access to the pool's name, size
 * and so on. This class is used for displaying connection pool details in the
 * user interface.
 *
 * @author Timothy Anyona
 */
public class ConnectionPoolDetails implements Serializable {
	// Constants ---------------------------------------------------------------

	private static final long serialVersionUID = 1L;

	// Properties --------------------------------------------------------------
	int id;
	String name;
	int size;
}
