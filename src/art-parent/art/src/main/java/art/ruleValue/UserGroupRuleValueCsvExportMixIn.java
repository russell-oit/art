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
package art.ruleValue;

import art.rule.Rule;
import art.usergroup.UserGroup;
import com.fasterxml.jackson.annotation.JsonUnwrapped;

/**
 * Enables custom handling of csv export of user group rule values where
 * JsonUnwrapped is used for csv export but not for json export
 *
 * @author Timothy Anyona
 */
public abstract class UserGroupRuleValueCsvExportMixIn extends UserGroupRuleValue {
	
	private static final long serialVersionUID = 1L;
	
	@JsonUnwrapped(prefix = "rule_")
	private Rule rule;
	@JsonUnwrapped(prefix = "userGroup_")
	private UserGroup userGroup;

}
