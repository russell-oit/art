/*
 * ART. A Reporting Tool.
 * Copyright (C) 2019 Enrico Liboni <eliboni@users.sf.net>
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package art.usergroup;

import art.reportgroup.ReportGroup;
import com.fasterxml.jackson.annotation.JsonUnwrapped;

/**
 * Enables custom handling of csv export of user groups where @JsonUnwrapped is
 * used for csv export but not for json export
 *
 * @author Timothy Anyona
 */
public abstract class UserGroupCsvExportMixIn extends UserGroup {
	//https://stackoverflow.com/questions/25425419/jackson-conditional-jsonunwrapped
	//https://stackoverflow.com/questions/28857897/how-do-correctly-use-jackson-mixin-annotation-to-instantiate-a-third-party-class
	//https://stackoverflow.com/questions/50589620/mixins-and-jackson-annotations

	@JsonUnwrapped(prefix = "defaultReportGroup_")
	private ReportGroup defaultReportGroup;

}
