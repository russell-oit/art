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

/**
 *
 * @author Timothy Anyona
 */
public class ArtRuntimeException extends RuntimeException {
	private static final long serialVersionUID = 1L;

    public ArtRuntimeException() {
        super();
    }

    public ArtRuntimeException(String message) {
        super(message);
    }

    public ArtRuntimeException(String message, Throwable cause) {
        super(message, cause);
    }

    public ArtRuntimeException(Throwable cause) {
        super(cause);
    }
}
