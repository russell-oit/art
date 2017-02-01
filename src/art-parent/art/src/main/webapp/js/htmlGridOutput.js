/* 
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

//javascript to support htmlGrid report format

function highLight(obj, cssName) {
	if (obj.className !== "slct" && obj.className !== "slct2" ) {
		obj.className = cssName;
	}
}

function selectRow(obj) {
	if (obj.className !== 'slct') {
		obj.className = 'slct';
	} else {
		obj.className = 'hiliterows';
	}
}

function selectRow2(obj) {
	if (obj.className !== 'slct2') {
		obj.className = 'slct2';
	} else {
		obj.className = 'hiliterows';
	}
}
