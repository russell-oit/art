/*
 * ART. A Reporting Tool.
 * Copyright (C) 2017 Enrico Liboni <eliboni@users.sf.net>
 *
 * This program is free software: you can redistribute it and/or modify
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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package art.springconfig;

import art.enums.ArtAuthenticationMethod;
import art.login.LoginHelper;
import art.login.LoginResult;
import art.servlets.Config;
import art.user.User;
import art.user.UserService;
import art.utils.ArtUtils;
import java.net.URI;
import java.net.URISyntaxException;
import java.sql.SQLException;
import java.util.Locale;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

/**
 * Ensures only certain users are allowed to access certain urls
 *
 * @author Timothy Anyona
 */
public class AuthorizationInterceptor extends HandlerInterceptorAdapter {
	//https://stackoverflow.com/questions/23349180/java-config-for-spring-interceptor-where-interceptor-is-using-autowired-spring-b
	//https://stackoverflow.com/questions/20243130/mvc-java-config-handlerinterceptor-not-excluding-paths
	//https://stackoverflow.com/questions/18218386/cannot-autowire-service-in-handlerinterceptoradapter
	//https://stackoverflow.com/questions/14071272/spring-mvc-authorization-in-rest-resources
	//http://www.journaldev.com/2676/spring-mvc-interceptor-example-handlerinterceptor-handlerinterceptoradapter
	//https://examples.javacodegeeks.com/enterprise-java/spring/mvc/spring-mvc-interceptor-tutorial/
	//http://docs.spring.io/spring/docs/current/javadoc-api/org/springframework/web/servlet/handler/HandlerInterceptorAdapter.html
	//http://www.concretepage.com/spring/spring-mvc/spring-handlerinterceptor-annotation-example-webmvcconfigureradapter

	private static final Logger logger = LoggerFactory.getLogger(AuthorizationInterceptor.class);

	@Autowired
	private MessageSource messageSource;

	@Autowired
	private LocaleResolver localeResolver;

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response,
			Object handler) throws Exception {

		HttpSession session = request.getSession();

		//https://stackoverflow.com/questions/4931323/whats-the-difference-between-getrequesturi-and-getpathinfo-methods-in-httpservl
		String requestUri = request.getRequestURI();
		String page = "";
		String pathMinusContext = "";
		//https://stackoverflow.com/questions/4050087/how-to-obtain-the-last-path-segment-of-an-uri
		//https://docs.oracle.com/javase/7/docs/api/java/net/URI.html
		//https://stackoverflow.com/questions/4931323/whats-the-difference-between-getrequesturi-and-getpathinfo-methods-in-httpservl
		try {
			URI uri = new URI(requestUri);
			String path = uri.getPath();
			String contextPath = request.getContextPath();
			pathMinusContext = StringUtils.substringAfter(path, contextPath);
			page = path.substring(path.lastIndexOf('/') + 1);
			page = StringUtils.substringBefore(page, ";"); //;jsessionid may be included at the end of the url. may also be in caps? i.e. ;JSESSIONID ?
		} catch (URISyntaxException ex) {
			logger.error("Error", ex);
		}

		String message = null;

		User user = (User) session.getAttribute("sessionUser");

		String urlUsername = request.getParameter("username");
		String urlPassword = request.getParameter("password");

		//reset session user if different username passed in url
		if (user != null && processUrlCredentials(page, urlUsername, urlPassword)
				&& !StringUtils.equalsIgnoreCase(user.getUsername(), urlUsername)) {
			session.removeAttribute("sessionUser");
			user = null;
		}

		if (user == null) {
			//custom authentication, public user session, credentials in url or session expired
			ArtAuthenticationMethod loginMethod = null;

			//test custom authentication
			String username = (String) session.getAttribute("username");

			if (username == null) {
				//not using custom authentication
				//check if this is a public session
				if (Boolean.parseBoolean(request.getParameter("public"))) {
					username = ArtUtils.PUBLIC_USER;
					loginMethod = ArtAuthenticationMethod.Public;
				} else {
					//check if credentials passed in url
					String windowsDomain = request.getParameter("windowsDomain");
					String authenticationMethodString = request.getParameter("authenticationMethod");
					//some pages e.g. saveUser, saveDatasource send the fields "username" and "password"
					if (processUrlCredentials(page, urlUsername, urlPassword)) {
						LoginHelper loginHelper = new LoginHelper();
						ArtAuthenticationMethod authenticationMethod = ArtAuthenticationMethod.toEnum(authenticationMethodString);
						LoginResult result = loginHelper.authenticate(authenticationMethod, urlUsername, urlPassword, windowsDomain);
						message = result.getMessage();
						if (result.isAuthenticated()) {
							username = urlUsername;
							loginMethod = authenticationMethod;
						}
					}
				}
			} else {
				loginMethod = ArtAuthenticationMethod.Custom;
			}

			if (loginMethod == null) {
				//session expired or unauthorized access attempt
				//don't show any message
			} else {
				//either custom authentication or public user session or credentials in url
				//ensure user exists
				UserService userService = new UserService();
				try {
					user = userService.getUser(username);
				} catch (SQLException ex) {
					logger.error("Error", ex);
				}

				LoginHelper loginHelper = new LoginHelper();
				String ip = request.getRemoteAddr();

				if (user == null) {
					//user doesn't exist
					message = "login.message.artUserInvalid";
					//log failure
					loginHelper.logFailure(loginMethod, username, ip, ArtUtils.ART_USER_INVALID);
				} else if (!user.isActive()) {
					//user disabled
					message = "login.message.artUserDisabled";
					//log failure
					loginHelper.logFailure(loginMethod, username, ip, ArtUtils.ART_USER_DISABLED);
				} else {
					//valid access
					session.setAttribute("sessionUser", user);
					session.setAttribute("authenticationMethod", loginMethod.getValue());
					session.setAttribute("administratorEmail", Config.getSettings().getAdministratorEmail());
					session.setAttribute("casLogoutUrl", Config.getSettings().getCasLogoutUrl());
					//log success
					loginHelper.logSuccess(loginMethod, username, ip);
				}
			}
		}

		//https://stackoverflow.com/questions/29902872/spring-how-to-get-the-actual-current-locale-like-it-works-for-messages
		//https://stackoverflow.com/questions/10792551/how-to-obtain-a-current-user-locale-from-spring-without-passing-it-as-a-paramete
		//https://stackoverflow.com/questions/24612568/localcontexthandler-usage
		Locale locale = localeResolver.resolveLocale(request);

		if (user == null || !user.isActive()) {
			//forward to login page. 
			//use forward instead of redirect so that an indication of the
			//page that was being accessed remains in the browser
			//remember the page the user tried to access in order to forward after the authentication
			//use relative path (without context path).
			//that's what redirect in login controller needs
			String nextPageAfterLogin = StringUtils.substringAfter(request.getRequestURI(), request.getContextPath());
			if (request.getQueryString() != null) {
				nextPageAfterLogin = nextPageAfterLogin + "?" + request.getQueryString();
			}

			if (StringUtils.startsWith(pathMinusContext, "/saiku2")) {
				//for saiku rest calls, don't return/display html page in response. just return error status code and message
				//https://stackoverflow.com/questions/21417256/spring-4-api-request-authentication
				//https://stackoverflow.com/questions/377644/jquery-ajax-error-handling-show-custom-exception-messages
				response.setStatus(HttpStatus.UNAUTHORIZED.value()); //HttpServletResponse.SC_UNAUTHORIZED (401)
				String localizedMessage = messageSource.getMessage(message, null, locale);
				response.setContentType(MediaType.TEXT_PLAIN_VALUE);
				response.getWriter().write(localizedMessage);
				return false;
			} else {
				session.setAttribute("nextPageAfterLogin", nextPageAfterLogin);
				request.setAttribute("message", message);
				request.getRequestDispatcher("/login").forward(request, response);
				return false;
			}
		}

		if (StringUtils.startsWith(pathMinusContext, "/saiku2")
				|| canAccessPage(page, user, session, pathMinusContext)) {
			if (!Config.isArtDatabaseConfigured()) {
				//if art database not configured, only allow access to artDatabase and language
				if (StringUtils.equals(page, "artDatabase")
						|| StringUtils.equals(page, "language")
						|| StringUtils.equals(page, "success")) {
					return true;
				} else {
					message = "page.message.artDatabaseNotConfigured";
					if (StringUtils.startsWith(pathMinusContext, "/saiku2")) {
						response.setStatus(HttpStatus.UNAUTHORIZED.value());
						String localizedMessage = messageSource.getMessage(message, null, locale);
						response.setContentType(MediaType.TEXT_PLAIN_VALUE);
						response.getWriter().write(localizedMessage);
						return false;
					} else {
						request.setAttribute("message", message);
						request.getRequestDispatcher("/accessDenied").forward(request, response);
						return false;
					}
				}
			}
			//authorisation is ok. display page
			return true;
		} else {
			if (StringUtils.startsWith(pathMinusContext, "/saiku2")) {
				message = "page.message.accessDenied";
				response.setStatus(HttpStatus.UNAUTHORIZED.value());
				String localizedMessage = messageSource.getMessage(message, null, locale);
				response.setContentType(MediaType.TEXT_PLAIN_VALUE);
				response.getWriter().write(localizedMessage);
				return false;
			} else {
				//show access denied page. 
				//use forward instead of redirect so that the intended url remains in the browser
				request.getRequestDispatcher("/accessDenied").forward(request, response);
				return false;
			}
		}
	}

	/**
	 * Returns <code>true</code> if credentials in the url should be considered
	 *
	 * @param page the page being processed
	 * @param urlUsername the "username" url parameter value
	 * @param urlPassword the "password" url parameter value
	 * @return <code>true</code> if credentials in the url should be considered
	 */
	private boolean processUrlCredentials(String page, String urlUsername, String urlPassword) {
		//some pages e.g. saveUser, saveDatasource, testDatasource send the fields "username" and "password"
		//and so should not be considered. mainly considering running reports via url, page = runReport
		//login doesn't pass through authorizationInterceptor
//		if (!StringUtils.startsWith(page, "save") && !StringUtils.equals(page, "testDatasource")
//				&& !StringUtils.equals(page, "artDatabase")
//				&& urlUsername != null && urlPassword != null) {
//			return true;
//		} else {
//			return false;
//		}

		if ((StringUtils.equals(page, "runReport") || StringUtils.equals(page, "reports"))
				&& urlUsername != null && urlPassword != null) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Returns <code>true</code> if the logged in user can access the given page
	 *
	 * @param page the page
	 * @param user the user
	 * @param session the http session
	 * @param pathMinusContext the url path after (minus) the application
	 * context
	 * @return <code>true</code> if the logged in user can access the given page
	 */
	private boolean canAccessPage(String page, User user, HttpSession session,
			String pathMinusContext) {

		boolean authorised = false;

		//"" = home page "/"
		if (StringUtils.equalsAny(page, "", "reports", "selectReportParameters",
				"showDashboard", "getLovValues", "runReport", "emailReport",
				"showJPivot", "jpivotBusy", "jpivotError", "saiku3",
				"saveParameterSelection", "clearSavedParameterSelection")) {
			if (user.hasPermission("view_reports")) {
				authorised = true;
			}
		} else if (StringUtils.equalsAny(page, "saveJPivot",
				"savePivotTableJs", "deletePivotTableJs")) {
			if (user.hasPermission("save_reports")) {
				authorised = true;
			}
		} else if (StringUtils.equals(page, "getSchedule")) {
			if (user.hasPermission("schedule_jobs")) {
				authorised = true;
			}
		} else if (StringUtils.equalsAny(page, "jobs", "archives")) {
			if (user.hasPermission("view_jobs")) {
				authorised = true;
			}
		} else if (StringUtils.equalsAny(page, "saveGridstack", "deleteGridstack")) {
			if (user.hasAnyPermission("save_reports", "self_service_dashboards")) {
				authorised = true;
			}
		} else if (StringUtils.equalsAny(page, "selfServiceDashboards", "getDashboardCandidateReports")) {
			if (user.hasPermission("self_service_dashboards")) {
				authorised = true;
			}
		} else if (StringUtils.startsWith(pathMinusContext, "/export/")) {
			//all can access
			authorised = true;
		} else if (StringUtils.endsWith(page, "Job")) {
			if (user.hasPermission("schedule_jobs")) {
				authorised = true;
			}
		} else if (StringUtils.endsWith(page, "Jobs") || StringUtils.equals(page, "jobsConfig")) {
			if (user.hasPermission("configure_jobs")) {
				authorised = true;
			}
		} else if (StringUtils.equals(page, "logs")) {
			if (user.hasPermission("view_logs")) {
				authorised = true;
			}
		} else if (StringUtils.equals(page, "users")
				|| StringUtils.endsWithAny(page, "User", "Users")) {
			if (user.hasPermission("configure_users")) {
				authorised = true;
			}
		} else if (StringUtils.equals(page, "artDatabase")) {
			if (user.hasPermission("configure_art_database")) {
				authorised = true;
			}
		} else if (StringUtils.equals(page, "settings")) {
			if (user.hasPermission("configure_settings")) {
				authorised = true;
			}
		} else if (StringUtils.equals(page, "language")) {
			//all can access
			authorised = true;
		} else if (StringUtils.equals(page, "password")) {
			//everyone, if enabled to change password and is using internal authentication
			String authenticationMethod = (String) session.getAttribute("authenticationMethod");
			if (user.isCanChangePassword()
					&& StringUtils.equals(authenticationMethod, ArtAuthenticationMethod.Internal.getValue())) {
				authorised = true;
			}
		} else if (StringUtils.equalsAny(page, "success", "accessDenied", "reportError")) {
			//all can access
			authorised = true;
		} else if (StringUtils.equals(page, "userGroups")
				|| StringUtils.endsWithAny(page, "UserGroup", "UserGroups")) {
			if (user.hasPermission("configure_user_groups")) {
				authorised = true;
			}
		} else if (StringUtils.equalsAny(page, "datasources", "testDatasource")
				|| StringUtils.endsWithAny(page, "Datasource", "Datasources")) {
			if (user.hasPermission("configure_datasources")) {
				authorised = true;
			}
		} else if (StringUtils.equalsAny(page, "reportsConfig", "uploadResources", "reportConfig")
				|| StringUtils.endsWithAny(page, "Report", "Reports")) {
			if (user.hasPermission("configure_reports")) {
				authorised = true;
			}
		} else if (StringUtils.equalsAny(page, "caches", "clearAllCaches")
				|| StringUtils.endsWith(page, "Cache")) {
			if (user.hasPermission("configure_caches")) {
				authorised = true;
			}
		} else if (StringUtils.equalsAny(page, "connections", "refreshConnectionPool")) {
			if (user.hasPermission("configure_connections")) {
				authorised = true;
			}
		} else if (StringUtils.equals(page, "loggers") || StringUtils.endsWith(page, "Logger")) {
			if (user.hasPermission("configure_loggers")) {
				authorised = true;
			}
		} else if (StringUtils.equals(page, "reportGroups")
				|| StringUtils.endsWithAny(page, "ReportGroup", "ReportGroups")) {
			if (user.hasPermission("configure_report_groups")) {
				authorised = true;
			}
		} else if (StringUtils.equals(page, "schedules")
				|| StringUtils.endsWithAny(page, "Schedule", "Schedules")) {
			if (user.hasPermission("configure_schedules")) {
				authorised = true;
			}
		} else if (StringUtils.equals(page, "holidays")
				|| StringUtils.endsWithAny(page, "Holiday", "Holidays")) {
			if (user.hasPermission("configure_holidays")) {
				authorised = true;
			}
		} else if (StringUtils.equals(page, "destinations")
				|| StringUtils.endsWithAny(page, "Destination", "Destinations")) {
			if (user.hasPermission("configure_destinations")) {
				authorised = true;
			}
		} else if (StringUtils.equals(page, "drilldowns")
				|| StringUtils.endsWithAny(page, "Drilldown", "Drilldowns")) {
			if (user.hasPermission("configure_reports")) {
				authorised = true;
			}
		} else if (StringUtils.equalsAny(page, "adminRights", "adminRightsConfig")
				|| StringUtils.endsWith(page, "AdminRight")) {
			if (user.hasPermission("configure_admin_rights")) {
				authorised = true;
			}
		} else if (StringUtils.equalsAny(page, "accessRights", "accessRightsConfig")
				|| StringUtils.endsWithAny(page, "AccessRight", "AccessRights")) {
			if (user.hasPermission("configure_access_rights")) {
				authorised = true;
			}
		} else if (StringUtils.equalsAny(page, "userGroupMembership", "userGroupMembershipConfig")
				|| StringUtils.endsWith(page, "UserGroupMembership")) {
			if (user.hasPermission("configure_user_group_membership")) {
				authorised = true;
			}
		} else if (StringUtils.equalsAny(page, "reportGroupMembership", "reportGroupMembershipConfig")
				|| StringUtils.endsWith(page, "ReportGroupMembership")) {
			if (user.hasPermission("configure_report_group_membership")) {
				authorised = true;
			}
		} else if (StringUtils.equalsAny(page, "rules", "reportRules")
				|| StringUtils.endsWithAny(page, "Rule", "Rules")) {
			if (user.hasPermission("configure_reports")) {
				authorised = true;
			}
		} else if (StringUtils.equalsAny(page, "parameters", "reportParameterConfig")
				|| StringUtils.endsWithAny(page, "Parameter", "Parameters")) {
			if (user.hasPermission("configure_reports")) {
				authorised = true;
			}
		} else if (StringUtils.equalsAny(page, "ruleValues", "ruleValuesConfig")
				|| StringUtils.endsWithAny(page, "RuleValue", "RuleValues")) {
			if (user.hasPermission("configure_reports")) {
				authorised = true;
			}
		} else if (StringUtils.equalsAny(page, "paramDefaults", "paramDefaultsConfig")
				|| StringUtils.endsWithAny(page, "ParamDefault", "ParamDefaults")) {
			if (user.hasPermission("configure_reports")) {
				authorised = true;
			}
		} else if (StringUtils.equalsAny(page, "fixedParamValues", "fixedParamValuesConfig")
				|| StringUtils.endsWithAny(page, "FixedParamValue", "FixedParamValues")) {
			if (user.hasPermission("configure_reports")) {
				authorised = true;
			}
		} else if (StringUtils.equals(page, "smtpServers")
				|| StringUtils.endsWithAny(page, "SmtpServer", "SmtpServers")) {
			if (user.hasPermission("configure_smtp_servers")) {
				authorised = true;
			}
		} else if (StringUtils.equals(page, "encryptors")
				|| StringUtils.endsWithAny(page, "Encryptor", "Encryptors")) {
			if (user.hasPermission("configure_encryptors")) {
				authorised = true;
			}
		} else if (StringUtils.endsWith(page, "Records")) {
			if (user.hasPermission("migrate_records")) {
				authorised = true;
			}
		} else if (StringUtils.equalsAny(page, "roles", "rolePermissionsConfig",
				"updateRolePermissions")
				|| StringUtils.endsWithAny(page, "Role", "Roles")) {
			if (user.hasPermission("configure_roles")) {
				authorised = true;
			}
		} else if (StringUtils.equals(page, "permissionsConfig")
				|| StringUtils.endsWithAny(page, "Permission", "Permissions")) {
			if (user.hasPermission("configure_permissions")) {
				authorised = true;
			}
		}

		return authorised;
	}

}
