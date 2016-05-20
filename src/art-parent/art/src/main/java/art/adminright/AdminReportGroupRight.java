/**
 * Copyright (C) 2016 Enrico Liboni <eliboni@users.sourceforge.net>
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
package art.adminright;

import art.reportgroup.ReportGroup;
import art.user.User;
import java.io.Serializable;

/**
 * Represents an admin-report group right
 * 
 * @author Timothy Anyona
 */
public class AdminReportGroupRight implements Serializable {

	private static final long serialVersionUID = 1L;
	private User admin;
	private ReportGroup reportGroup;

	/**
	 * @return the admin
	 */
	public User getAdmin() {
		return admin;
	}

	/**
	 * @param admin the admin to set
	 */
	public void setAdmin(User admin) {
		this.admin = admin;
	}

	/**
	 * @return the reportGroup
	 */
	public ReportGroup getReportGroup() {
		return reportGroup;
	}

	/**
	 * @param reportGroup the reportGroup to set
	 */
	public void setReportGroup(ReportGroup reportGroup) {
		this.reportGroup = reportGroup;
	}
}
