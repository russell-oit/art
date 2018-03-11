/*
 * ART. A Reporting Tool.
 * Copyright (C) 2018 Enrico Liboni <eliboni@users.sf.net>
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
package art.report;

import art.accessright.AccessRightService;
import art.accessright.UserGroupReportRight;
import art.accessright.UserReportRight;
import art.artdatabase.ArtDatabase;
import art.connectionpool.DbConnections;
import art.datasource.Datasource;
import art.datasource.DatasourceService;
import art.dbutils.DbService;
import art.drilldown.Drilldown;
import art.drilldown.DrilldownService;
import art.encryptor.Encryptor;
import art.encryptor.EncryptorService;
import art.parameter.Parameter;
import art.parameter.ParameterService;
import art.reportgroup.ReportGroup;
import art.reportgroup.ReportGroupService;
import art.reportgroupmembership.ReportGroupMembershipService2;
import art.reportparameter.ReportParameterService;
import art.reportrule.ReportRule;
import art.reportrule.ReportRuleService;
import art.rule.Rule;
import art.rule.RuleService;
import art.ruleValue.RuleValueService;
import art.ruleValue.UserGroupRuleValue;
import art.ruleValue.UserRuleValue;
import art.servlets.Config;
import art.user.User;
import art.user.UserService;
import art.usergroup.UserGroup;
import art.usergroup.UserGroupService;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides method to import report records
 *
 * @author Timothy Anyona
 */
public class ReportServiceHelper {
	//use separate class to avoid circular with ParameterService and ReportParameterService

	private static final Logger logger = LoggerFactory.getLogger(ReportServiceHelper.class);

	/**
	 * Imports report records
	 *
	 * @param reports the list of reports to import
	 * @param actionUser the user who is performing the import
	 * @param conn the connection to use
	 * @param local whether the import is to the local/current art instance
	 * @throws SQLException
	 */
	public void importReports(List<Report> reports, User actionUser,
			Connection conn, boolean local) throws SQLException {

		boolean commit = true;
		importReports(reports, actionUser, conn, local, commit);
	}

	/**
	 * Imports report records
	 *
	 * @param reports the list of reports to import
	 * @param actionUser the user who is performing the import
	 * @param conn the connection to use
	 * @param local whether the import is to the local/current art instance
	 * @param commit whether to perform a commit after a successful import
	 * @throws SQLException
	 */
	public void importReports(List<Report> reports, User actionUser,
			Connection conn, boolean local, boolean commit) throws SQLException {

		logger.debug("Entering importReports: actionUser={}, local={},"
				+ " commit={}", actionUser, local, commit);

		boolean originalAutoCommit = true;

		try {
			originalAutoCommit = conn.getAutoCommit();
			conn.setAutoCommit(false);

			DbService dbService = new DbService();
			ReportService reportService = new ReportService();
			ReportParameterService reportParameterService = new ReportParameterService();
			DatasourceService datasourceService = new DatasourceService();
			EncryptorService encryptorService = new EncryptorService();
			ReportGroupService reportGroupService = new ReportGroupService();
			ReportGroupMembershipService2 reportGroupMembershipService2 = new ReportGroupMembershipService2();
			ParameterService parameterService = new ParameterService();
			RuleValueService ruleValueService = new RuleValueService();
			RuleService ruleService = new RuleService();
			UserService userService = new UserService();
			UserGroupService userGroupService = new UserGroupService();
			ReportRuleService reportRuleService = new ReportRuleService();
			AccessRightService accessRightService = new AccessRightService();
			DrilldownService drilldownService = new DrilldownService();

			String sql = "SELECT MAX(QUERY_ID) FROM ART_QUERIES";
			int reportId = dbService.getMaxRecordId(conn, sql);

			sql = "SELECT MAX(DATABASE_ID) FROM ART_DATABASES";
			int datasourceId = dbService.getMaxRecordId(conn, sql);

			sql = "SELECT MAX(ENCRYPTOR_ID) FROM ART_ENCRYPTORS";
			int encryptorId = dbService.getMaxRecordId(conn, sql);

			sql = "SELECT MAX(QUERY_GROUP_ID) FROM ART_QUERY_GROUPS";
			int reportGroupId = dbService.getMaxRecordId(conn, sql);

			Map<String, Datasource> addedDatasources = new HashMap<>();
			Map<String, Encryptor> addedEncryptors = new HashMap<>();
			Map<String, ReportGroup> addedReportGroups = new HashMap<>();
			Map<String, Report> addedReports = new HashMap<>();

			List<Report> parameterReports = new ArrayList<>();
			for (Report report : reports) {
				List<Parameter> parameters = parameterService.getReportParameters(report.getReportId());
				for (Parameter parameter : parameters) {
					Report defaultValueReport = parameter.getDefaultValueReport();
					if (defaultValueReport != null) {
						parameterReports.add(defaultValueReport);
					}
					Report lovReport = parameter.getLovReport();
					if (lovReport != null) {
						parameterReports.add(lovReport);
					}
				}
			}
			reports.addAll(parameterReports);

			List<Report> drilldownReports = new ArrayList<>();
			for (Report report : reports) {
				List<Drilldown> drilldowns = report.getDrilldowns();
				if (CollectionUtils.isNotEmpty(drilldowns)) {
					for (Drilldown drilldown : drilldowns) {
						Report drilldownReport = drilldown.getDrilldownReport();
						drilldownReports.add(drilldownReport);
					}
				}
			}
			reports.addAll(drilldownReports);

			for (Report report : reports) {
				String reportName = report.getName();
				if (StringUtils.isNotBlank(reportName)) {
					Report existingReport = reportService.getReport(reportName);
					if (existingReport == null) {
						Report addedReport = addedReports.get(reportName);
						if (addedReport == null) {
							reportId++;

							Datasource datasource = report.getDatasource();
							if (datasource != null) {
								String datasourceName = datasource.getName();
								if (StringUtils.isBlank(datasourceName)) {
									report.setDatasource(null);
								} else {
									Datasource existingDatasource = datasourceService.getDatasource(datasourceName);
									if (existingDatasource == null) {
										Datasource addedDatasource = addedDatasources.get(datasourceName);
										if (addedDatasource == null) {
											datasourceId++;
											datasourceService.saveDatasource(datasource, datasourceId, actionUser, conn);
											addedDatasources.put(datasourceName, datasource);
										} else {
											report.setDatasource(addedDatasource);
										}
									} else {
										report.setDatasource(existingDatasource);
									}
								}
							}

							Encryptor encryptor = report.getEncryptor();
							if (encryptor != null) {
								String encryptorName = encryptor.getName();
								if (StringUtils.isBlank(encryptorName)) {
									report.setEncryptor(null);
								} else {
									Encryptor existingEncryptor = encryptorService.getEncryptor(encryptorName);
									if (existingEncryptor == null) {
										Encryptor addedEncryptor = addedEncryptors.get(encryptorName);
										if (addedEncryptor == null) {
											encryptorId++;
											encryptorService.saveEncryptor(encryptor, encryptorId, actionUser, conn);
											addedEncryptors.put(encryptorName, encryptor);
										} else {
											report.setEncryptor(addedEncryptor);
										}
									} else {
										report.setEncryptor(existingEncryptor);
									}
								}
							}

							List<ReportGroup> reportGroups = report.getReportGroups();
							if (CollectionUtils.isNotEmpty(reportGroups)) {
								List<ReportGroup> newReportGroups = new ArrayList<>();
								for (ReportGroup reportGroup : reportGroups) {
									String reportGroupName = reportGroup.getName();
									ReportGroup existingReportGroup = reportGroupService.getReportGroup(reportGroupName);
									if (existingReportGroup == null) {
										ReportGroup addedReportGroup = addedReportGroups.get(reportGroupName);
										if (addedReportGroup == null) {
											reportGroupId++;
											reportGroupService.saveReportGroup(reportGroup, reportGroupId, actionUser, conn);
											addedReportGroups.put(reportGroupName, reportGroup);
											newReportGroups.add(reportGroup);
										} else {
											newReportGroups.add(addedReportGroup);
										}
									} else {
										newReportGroups.add(existingReportGroup);
									}
								}
								report.setReportGroups(newReportGroups);
							}

							reportService.saveReport(report, reportId, actionUser, conn);
							addedReports.put(reportName, report);
							reportGroupMembershipService2.recreateReportGroupMemberships(report);
						} else {
							report.setReportId(addedReport.getReportId());
						}
					} else {
						report.setReportId(existingReport.getReportId());
					}
				}
			}

			if (local) {
				ArtDatabase artDbConfig = Config.getArtDbConfig();
				for (Datasource datasource : addedDatasources.values()) {
					if (datasource.isActive()) {
						datasource.decryptPassword();
						DbConnections.createConnectionPool(datasource, artDbConfig.getMaxPoolConnections(), artDbConfig.getConnectionPoolLibrary());
					}
				}
			}

			reportParameterService.importReportParameters(reports, actionUser, conn);

			List<Drilldown> allDrilldowns = new ArrayList<>();
			for (Report report : reports) {
				int tempReportId = report.getReportId();
				List<Drilldown> drilldowns = report.getDrilldowns();
				if (CollectionUtils.isNotEmpty(drilldowns)) {
					for (Drilldown drilldown : drilldowns) {
						drilldown.setParentReportId(tempReportId);
					}
					allDrilldowns.addAll(drilldowns);
				}
			}
			drilldownService.importDrilldowns(allDrilldowns, conn);

			sql = "SELECT MAX(RULE_ID) FROM ART_RULES";
			int ruleId = dbService.getMaxRecordId(conn, sql);

			sql = "SELECT MAX(USER_ID) FROM ART_USERS";
			int userId = dbService.getMaxRecordId(conn, sql);

			sql = "SELECT MAX(USER_GROUP_ID) FROM ART_USER_GROUPS";
			int userGroupId = dbService.getMaxRecordId(sql);

			sql = "SELECT MAX(QUERY_RULE_ID) FROM ART_QUERY_RULES";
			int reportRuleId = dbService.getMaxRecordId(sql);

			List<UserRuleValue> allUserRuleValues = new ArrayList<>();
			List<UserGroupRuleValue> allUserGroupRuleValues = new ArrayList<>();
			List<ReportRule> allReportRules = new ArrayList<>();
			List<UserReportRight> allUserReportRights = new ArrayList<>();
			List<UserGroupReportRight> allUserGroupReportRights = new ArrayList<>();
			for (Report report : reports) {
				List<UserRuleValue> userRuleValues = report.getUserRuleValues();
				if (CollectionUtils.isNotEmpty(userRuleValues)) {
					allUserRuleValues.addAll(userRuleValues);
				}

				List<UserGroupRuleValue> userGroupRuleValues = report.getUserGroupRuleValues();
				if (CollectionUtils.isNotEmpty(userGroupRuleValues)) {
					allUserGroupRuleValues.addAll(userGroupRuleValues);
				}

				List<ReportRule> reportRules = report.getReportRules();
				if (CollectionUtils.isNotEmpty(reportRules)) {
					for (ReportRule reportRule : reportRules) {
						reportRule.setReportId(report.getReportId());
						allReportRules.add(reportRule);
					}
				}

				List<UserReportRight> userReportRights = report.getUserReportRights();
				if (CollectionUtils.isNotEmpty(userReportRights)) {
					for (UserReportRight userReportRight : userReportRights) {
						userReportRight.setReport(report);
						allUserReportRights.add(userReportRight);
					}
				}

				List<UserGroupReportRight> userGroupReportRights = report.getUserGroupReportRights();
				if (CollectionUtils.isNotEmpty(userGroupReportRights)) {
					for (UserGroupReportRight userGroupReportRight : userGroupReportRights) {
						userGroupReportRight.setReport(report);
						allUserGroupReportRights.add(userGroupReportRight);
					}
				}
			}

			List<Rule> allRules = new ArrayList<>();
			for (UserRuleValue userRuleValue : allUserRuleValues) {
				Rule rule = userRuleValue.getRule();
				allRules.add(rule);
			}

			for (UserGroupRuleValue userGroupRuleValue : allUserGroupRuleValues) {
				Rule rule = userGroupRuleValue.getRule();
				allRules.add(rule);
			}

			for (ReportRule reportRule : allReportRules) {
				Rule rule = reportRule.getRule();
				allRules.add(rule);
			}

			Map<String, Rule> addedRules = new HashMap<>();
			for (Rule rule : allRules) {
				String ruleName = rule.getName();
				Rule existingRule = ruleService.getRule(ruleName);
				if (existingRule == null) {
					Rule addedRule = addedRules.get(ruleName);
					if (addedRule == null) {
						ruleId++;
						ruleService.saveRule(rule, ruleId, actionUser, conn);
						addedRules.put(ruleName, rule);
					} else {
						rule.setRuleId(addedRule.getRuleId());
					}
				} else {
					rule.setRuleId(existingRule.getRuleId());
				}
			}

			List<User> allUsers = new ArrayList<>();
			for (UserRuleValue userRuleValue : allUserRuleValues) {
				User user = userRuleValue.getUser();
				allUsers.add(user);
			}

			for (UserReportRight userReportRight : allUserReportRights) {
				User user = userReportRight.getUser();
				allUsers.add(user);
			}

			Map<String, User> addedUsers = new HashMap<>();
			for (User user : allUsers) {
				String username = user.getUsername();
				User existingUser = userService.getUser(username);
				if (existingUser == null) {
					User addedUser = addedUsers.get(username);
					if (addedUser == null) {
						userId++;
						userService.saveUser(user, userId, actionUser, conn);
						addedUsers.put(username, user);
					} else {
						user.setUserId(addedUser.getUserId());
					}
				} else {
					user.setUserId(existingUser.getUserId());
				}
			}

			List<UserGroup> allUserGroups = new ArrayList<>();
			for (UserGroupRuleValue userGroupRuleValue : allUserGroupRuleValues) {
				UserGroup userGroup = userGroupRuleValue.getUserGroup();
				allUserGroups.add(userGroup);
			}

			for (UserGroupReportRight userGroupReportRight : allUserGroupReportRights) {
				UserGroup userGroup = userGroupReportRight.getUserGroup();
				allUserGroups.add(userGroup);
			}

			Map<String, UserGroup> addedUserGroups = new HashMap<>();
			for (UserGroup userGroup : allUserGroups) {
				String userGroupName = userGroup.getName();
				UserGroup existingUserGroup = userGroupService.getUserGroup(userGroupName);
				if (existingUserGroup == null) {
					UserGroup addedUserGroup = addedUserGroups.get(userGroupName);
					if (addedUserGroup == null) {
						userGroupId++;
						userGroupService.saveUserGroup(userGroup, userGroupId, actionUser, conn);
						addedUserGroups.put(userGroupName, userGroup);
					} else {
						userGroup.setUserGroupId(addedUserGroup.getUserGroupId());
					}
				} else {
					userGroup.setUserGroupId(existingUserGroup.getUserGroupId());
				}
			}

			Map<String, ReportRule> addedReportRules = new HashMap<>();
			for (ReportRule reportRule : allReportRules) {
				int tempReportId = reportRule.getReportId();
				int tempRuleId = reportRule.getRule().getRuleId();
				String reportRuleKey = tempReportId + "-" + tempRuleId;
				ReportRule existingReportRule = reportRuleService.getReportRule(tempReportId, tempRuleId);
				if (existingReportRule == null) {
					ReportRule addedReportRule = addedReportRules.get(reportRuleKey);
					if (addedReportRule == null) {
						reportRuleId++;
						reportRuleService.saveReportRule(reportRule, reportRuleId, conn);
						addedReportRules.put(reportRuleKey, reportRule);
					} else {
						reportRule.setReportRuleId(addedReportRule.getReportRuleId());
					}
				} else {
					reportRule.setReportRuleId(existingReportRule.getReportRuleId());
				}
			}

			for (UserRuleValue userRuleValue : allUserRuleValues) {
				User user = userRuleValue.getUser();
				String userKey = user.getUserId() + "-" + user.getUsername();
				Rule rule = userRuleValue.getRule();
				String ruleKey = rule.getRuleId() + "-" + rule.getName();
				String ruleValue = userRuleValue.getRuleValue();
				String users[] = {userKey};
				Integer[] userGroups = null;
				ruleValueService.addRuleValue(users, userGroups, ruleKey, ruleValue, conn);
			}

			for (UserGroupRuleValue userGroupRuleValue : allUserGroupRuleValues) {
				UserGroup userGroup = userGroupRuleValue.getUserGroup();
				Integer tempUserGroupId = userGroup.getUserGroupId();
				Rule rule = userGroupRuleValue.getRule();
				String ruleKey = rule.getRuleId() + "-" + rule.getName();
				String ruleValue = userGroupRuleValue.getRuleValue();
				String users[] = null;
				Integer[] userGroups = {tempUserGroupId};
				ruleValueService.addRuleValue(users, userGroups, ruleKey, ruleValue, conn);
			}

			for (UserReportRight userReportRight : allUserReportRights) {
				User user = userReportRight.getUser();
				String userKey = user.getUserId() + "-" + user.getUsername();
				Report report = userReportRight.getReport();
				int tempReportId = report.getReportId();
				String action = "grant";
				String users[] = {userKey};
				Integer[] userGroups = null;
				Integer[] tempReports = {tempReportId};
				Integer[] reportGroups = null;
				Integer[] jobs = null;
				accessRightService.updateAccessRights(action, users, userGroups, tempReports, reportGroups, jobs);
			}

			for (UserGroupReportRight userGroupReportRight : allUserGroupReportRights) {
				UserGroup userGroup = userGroupReportRight.getUserGroup();
				Integer tempUserGroupId = userGroup.getUserGroupId();
				Report report = userGroupReportRight.getReport();
				int tempReportId = report.getReportId();
				String action = "grant";
				String users[] = null;
				Integer[] userGroups = {tempUserGroupId};
				Integer[] tempReports = {tempReportId};
				Integer[] reportGroups = null;
				Integer[] jobs = null;
				accessRightService.updateAccessRights(action, users, userGroups, tempReports, reportGroups, jobs);
			}

			if (commit) {
				conn.commit();
			}
		} catch (SQLException ex) {
			conn.rollback();
			throw ex;
		} finally {
			conn.setAutoCommit(originalAutoCommit);
		}
	}
}
