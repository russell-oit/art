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

package art.runreport;

import java.io.Serializable;

/**
 * Represents report options used when running a report
 * 
 * @author Timothy Anyona
 */
public class ReportOptions implements Serializable {
	private static final long serialVersionUID = 1L;
	private boolean showParameters;

	/**
	 * @return the showParameters
	 */
	public boolean isShowParameters() {
		return showParameters;
	}

	/**
	 * @param showParameters the showParameters to set
	 */
	public void setShowParameters(boolean showParameters) {
		this.showParameters = showParameters;
	}
}
