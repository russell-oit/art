/*
 * ART. A Reporting Tool.
 * Copyright (C) 2017 Enrico Liboni <eliboni@users.sf.net>
 *
 * This program is free software; you can redistribute it and/or modify
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
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package art.utils;

import art.reportrule.ReportRule;
import art.reportrule.ReportRuleService;
import art.ruleValue.RuleValueService;
import art.user.User;
import art.usergroup.UserGroup;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.apache.commons.lang3.StringUtils;

/**
 * Provides helper methods for working with mondrian
 *
 * @author Timothy Anyona
 */
public class MondrianHelper {

	/**
	 * Returns the roles string to use for mondrian roles configuration, given a
	 * certain report and user
	 *
	 * @param reportId the id of the report
	 * @param user the relevant user
	 * @return the roles string to use for mondrian roles configuration
	 * @throws SQLException
	 */
	public String getRolesString(int reportId, User user) throws SQLException {
		//get roles to be applied. use rule values are roles
		List<String> roles = new ArrayList<>();

		ReportRuleService reportRuleService = new ReportRuleService();
		RuleValueService ruleValueService = new RuleValueService();

		List<ReportRule> reportRules = reportRuleService.getEffectiveReportRules(reportId);

		for (ReportRule reportRule : reportRules) {
			int userId = user.getUserId();
			int ruleId = reportRule.getRule().getRuleId();
			List<String> userRuleValues = ruleValueService.getUserRuleValues(userId, ruleId);
			roles.addAll(userRuleValues);

			for (UserGroup userGroup : user.getUserGroups()) {
				List<String> userGroupRuleValues = ruleValueService.getUserGroupRuleValues(userGroup.getUserGroupId(), ruleId);
				roles.addAll(userGroupRuleValues);
			}
		}

		//https://stackoverflow.com/questions/3317691/replace-elements-in-a-list-with-another
		Collections.replaceAll(roles, ",", ",,");

		//remove duplicates
		//https://stackoverflow.com/questions/203984/how-do-i-remove-repeated-elements-from-arraylist
		Set<String> distinctRoles = new LinkedHashSet<>(roles);

		String rolesString = StringUtils.join(distinctRoles, ",");

		return rolesString;
	}

}
