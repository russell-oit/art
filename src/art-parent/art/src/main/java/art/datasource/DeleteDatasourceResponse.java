/**
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

package art.datasource;

import art.report.AvailableReport;
import art.utils.AjaxResponse;
import java.util.List;

/**
 * Class for ajax reponse for delete datasource action
 * 
 * @author Timothy Anyona
 */
public class DeleteDatasourceResponse extends AjaxResponse {
	private List<AvailableReport> linkedReports;

	/**
	 * @return the linkedReports
	 */
	public List<AvailableReport> getLinkedReports() {
		return linkedReports;
	}

	/**
	 * @param linkedReports the linkedReports to set
	 */
	public void setLinkedReports(List<AvailableReport> linkedReports) {
		this.linkedReports = linkedReports;
	}
	
}
