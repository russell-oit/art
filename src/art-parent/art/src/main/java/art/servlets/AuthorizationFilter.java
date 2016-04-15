package art.servlets;

import art.enums.AccessLevel;
import art.enums.ArtAuthenticationMethod;
import art.login.LoginHelper;
import art.user.User;
import art.user.UserService;
import art.utils.ArtUtils;
import java.io.IOException;
import java.sql.SQLException;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

/**
 * Filter to ensure user has access to the requested page
 *
 * @author Timothy Anyona
 */
public class AuthorizationFilter implements Filter {

	private static final Logger logger = LoggerFactory.getLogger(AuthorizationFilter.class);

	/**
	 *
	 */
	@Override
	public void destroy() {
	}

	/**
	 *
	 * @param filterConfig
	 * @throws ServletException
	 */
	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
	}

	/**
	 * Ensure user has access to the requested page
	 *
	 * @param srequest
	 * @param sresponse
	 * @param chain
	 * @throws IOException
	 * @throws ServletException
	 */
	@Override
	public void doFilter(ServletRequest srequest, ServletResponse sresponse,
			FilterChain chain) throws IOException, ServletException {

		if (srequest instanceof HttpServletRequest && sresponse instanceof HttpServletResponse) {
			HttpServletRequest request = (HttpServletRequest) srequest;
			HttpServletResponse response = (HttpServletResponse) sresponse;

			HttpSession session = request.getSession();

			String message = null;

			User user = (User) session.getAttribute("sessionUser");
			if (user == null) {
				//custom authentication, public user session or session expired
				ArtAuthenticationMethod loginMethod = null;

				//test custom authentication
				String username = (String) session.getAttribute("username");

				if (username == null) {
					//not using custom authentication. check if this is a public session
					if (Boolean.valueOf(request.getParameter("publicUser"))) {
						username = ArtUtils.PUBLIC_USER;
						loginMethod = ArtAuthenticationMethod.Public;
					}
				} else {
					loginMethod = ArtAuthenticationMethod.Custom;
				}

				if (loginMethod == null) {
					//session expired or just unauthorized access attempt
					message = "login.message.sessionExpired";
				} else {
					//either custom authentication or public user session
					//ensure user exists
					LoginHelper loginHelper = new LoginHelper();
					String ip = request.getRemoteAddr();

					UserService userService = new UserService();
					try {
						user = userService.getUser(username);
					} catch (SQLException ex) {
						logger.error("Error", ex);
					}

					if (user == null) {
						//user doesn't exist
						//always display invalidAccount message in login page. log actual cause
						message = "login.message.invalidCredentials";
						//log failure
						loginHelper.logFailure(loginMethod, username, ip, ArtUtils.ART_USER_INVALID);
					} else if (!user.isActive()) {
						//user disabled
						//always display invalidAccount message in login page. log actual cause
						message = "login.message.invalidCredentials";
						//log failure
						loginHelper.logFailure(loginMethod, username, ip, ArtUtils.ART_USER_DISABLED);
					} else {
						//valid access
						session.setAttribute("sessionUser", user);
						session.setAttribute("authenticationMethod", loginMethod.getValue());
						session.setAttribute("administratorEmail", Config.getSettings().getAdministratorEmail());
						//log success
						loginHelper.logSuccess(loginMethod, username, ip);
					}
				}
			}

			if (user == null) {
				//no valid user available

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
				session.setAttribute("nextPageAfterLogin", nextPageAfterLogin);
				request.setAttribute("message", message);
				request.getRequestDispatcher("/login.do").forward(request, response);
				return;
			}

			//if we are here, user is authenticated
			//ensure they have access to the specific page. if not show access denied page
			String requestUri = request.getRequestURI();
			String path = request.getContextPath() + "/app/";
			String page = StringUtils.substringBetween(requestUri, path, ".do");

			if (canAccessPage(page, user, session)) {
				if (!Config.isArtDatabaseConfigured()) {
					//if art database not configured, only allow access to artDatabase.do
					if (!StringUtils.equals(page, "artDatabase")) {
						request.setAttribute("message", "page.message.artDatabaseNotConfigured");
						request.getRequestDispatcher("/app/accessDenied.do").forward(request, response);
						return;
					}
				}
				//authorisation is ok. display page

				//enable display of custom attributes in logs
				addMdcAttributes(request, user);
				try {
					chain.doFilter(srequest, sresponse);
				} finally {
					removeMdcAttributes();
				}
			} else {
				//show access denied page. 
				//use forward instead of redirect so that the intended url remains in the browser
				request.getRequestDispatcher("/app/accessDenied.do").forward(request, response);
			}
		}
	}

	/**
	 * Add mdc attributes
	 *
	 * @param request
	 * @param user
	 */
	private void addMdcAttributes(HttpServletRequest request, User user) {
		//http://logback.qos.ch/manual/mdc.html
		//http://logback.qos.ch/xref/ch/qos/logback/classic/helpers/MDCInsertingServletFilter.html
		MDC.put("user", user.getUsername());
		MDC.put("remoteAddr", request.getRemoteAddr());
		MDC.put("requestURI", request.getRequestURI());
		MDC.put("xForwardedFor", request.getHeader("X-Forwarded-For"));
	}

	/**
	 * Clear mdc attributes.
	 */
	private void removeMdcAttributes() {
		//everything added to mdc should be removed here
		MDC.remove("user");
		MDC.remove("remoteAddr");
		MDC.remove("requestURI");
		MDC.remove("xForwardedFor");
	}

	private boolean canAccessPage(String page, User user, HttpSession session) {
		if (user.getAccessLevel() == null) {
			return false;
		}

		boolean authorised = false;

		int accessLevel = user.getAccessLevel().getValue();

		if (StringUtils.equals(page, "reports")
				|| StringUtils.equals(page, "selectReportParameters")
				|| StringUtils.equals(page, "showDashboard")
				|| StringUtils.equals(page, "showAnalysis")
				|| StringUtils.equals(page, "jpivotError")
				|| StringUtils.equals(page, "jpivotBusy")
				|| StringUtils.equals(page, "getSchedule")
				|| StringUtils.equals(page, "runReport")
				|| StringUtils.equals(page, "archives")
				|| StringUtils.equals(page, "getLovValues")) {
			//everyone can access
			//NOTE: "everyone" doesn't include when accessing as the art repository user
			if (accessLevel >= AccessLevel.NormalUser.getValue()) {
				authorised = true;
			}
		} else if (StringUtils.equals(page, "jobs")) {
			//everyone
			if (accessLevel >= AccessLevel.NormalUser.getValue()) {
				authorised = true;
			}
		} else if (StringUtils.equals(page, "logs")) {
			//senior admins and above
			if (accessLevel >= AccessLevel.SeniorAdmin.getValue()) {
				authorised = true;
			}
		} else if (StringUtils.equals(page, "users") || StringUtils.endsWith(page, "User")
				|| StringUtils.endsWith(page, "Users")) {
			//standard admins and above, and repository user
			if (accessLevel >= AccessLevel.StandardAdmin.getValue()
					|| accessLevel == AccessLevel.RepositoryUser.getValue()) {
				authorised = true;
			}
		} else if (StringUtils.equals(page, "artDatabase")) {
			//super admins, and repository user
			if (accessLevel == AccessLevel.SuperAdmin.getValue()
					|| accessLevel == AccessLevel.RepositoryUser.getValue()) {
				authorised = true;
			}
		} else if (StringUtils.equals(page, "settings")) {
			//super admins
			if (accessLevel == AccessLevel.SuperAdmin.getValue()) {
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
		} else if (StringUtils.equals(page, "success")
				|| StringUtils.equals(page, "accessDenied")
				|| StringUtils.equals(page, "reportError")) {
			//all can access
			authorised = true;
		} else if (StringUtils.equals(page, "userGroups") || StringUtils.endsWith(page, "UserGroup")
				|| StringUtils.endsWith(page, "UserGroups")) {
			//standard admins and above
			if (accessLevel >= AccessLevel.StandardAdmin.getValue()) {
				authorised = true;
			}
		} else if (StringUtils.equals(page, "datasources") || StringUtils.endsWith(page, "Datasource")
				|| StringUtils.equals(page, "testDatasource")
				|| StringUtils.endsWith(page, "Datasources")) {
			//senior admins and above
			if (accessLevel >= AccessLevel.SeniorAdmin.getValue()) {
				authorised = true;
			}
		} else if (StringUtils.equals(page, "reportsConfig") || StringUtils.endsWith(page, "Report")
				||  StringUtils.endsWith(page, "Reports")) {
			//junior admins and above
			if (accessLevel >= AccessLevel.JuniorAdmin.getValue()) {
				authorised = true;
			}
		} else if (StringUtils.equals(page, "caches") || StringUtils.endsWith(page, "Cache")
				|| StringUtils.equals(page, "clearAllCaches")) {
			//senior admins and above
			if (accessLevel >= AccessLevel.SeniorAdmin.getValue()) {
				authorised = true;
			}
		} else if (StringUtils.equals(page, "connections")
				|| StringUtils.equals(page, "refreshConnectionPool")) {
			//senior admins and above
			if (accessLevel >= AccessLevel.SeniorAdmin.getValue()) {
				authorised = true;
			}
		} else if (StringUtils.equals(page, "loggers") || StringUtils.endsWith(page, "Logger")) {
			//senior admins and above
			if (accessLevel >= AccessLevel.SeniorAdmin.getValue()) {
				authorised = true;
			}
		} else if (StringUtils.equals(page, "reportGroups") || StringUtils.endsWith(page, "ReportGroup")
				|| StringUtils.endsWith(page, "ReportGroups")) {
			//senior admins and above
			if (accessLevel >= AccessLevel.SeniorAdmin.getValue()) {
				authorised = true;
			}
		} else if (StringUtils.equals(page, "schedules") || StringUtils.endsWith(page, "Schedule")
				|| StringUtils.endsWith(page, "Schedules") ) {
			//senior admins and above
			if (accessLevel >= AccessLevel.SeniorAdmin.getValue()) {
				authorised = true;
			}
		} else if (StringUtils.equals(page, "jobsConfig") || StringUtils.endsWith(page, "Job")
				|| StringUtils.endsWith(page, "Jobs")) {
			//standard admins and above
			if (accessLevel >= AccessLevel.StandardAdmin.getValue()) {
				authorised = true;
			}
		} else if (StringUtils.equals(page, "drilldowns")
				|| StringUtils.endsWith(page, "Drilldown")
				|| StringUtils.endsWith(page, "Drilldowns")) {
			//junior admins and above
			if (accessLevel >= AccessLevel.JuniorAdmin.getValue()) {
				authorised = true;
			}
		} else if (StringUtils.equals(page, "adminRights") || StringUtils.endsWith(page, "AdminRight")
				|| StringUtils.equals(page, "adminRightsConfig")) {
			//standard admins and above
			if (accessLevel >= AccessLevel.StandardAdmin.getValue()) {
				authorised = true;
			}
		} else if (StringUtils.equals(page, "accessRights") || StringUtils.endsWith(page, "AccessRight")
				|| StringUtils.equals(page, "accessRightsConfig")) {
			//mid admins and above
			if (accessLevel >= AccessLevel.MidAdmin.getValue()) {
				authorised = true;
			}
		} else if (StringUtils.equals(page, "userGroupMembership") || StringUtils.endsWith(page, "UserGroupMembership")
				|| StringUtils.equals(page, "userGroupMembershipConfig")) {
			//standard admins and above
			if (accessLevel >= AccessLevel.StandardAdmin.getValue()) {
				authorised = true;
			}
		} else if (StringUtils.equals(page, "filters") || StringUtils.endsWith(page, "Filter")) {
			//senior admins and above
			if (accessLevel >= AccessLevel.SeniorAdmin.getValue()) {
				authorised = true;
			}
		} else if (StringUtils.equals(page, "rules") || StringUtils.endsWith(page, "Rule")
				|| StringUtils.endsWith(page, "Rules")) {
			//senior admins and above
			if (accessLevel >= AccessLevel.SeniorAdmin.getValue()) {
				authorised = true;
			}
		} else if (StringUtils.equals(page, "reportFilters") || StringUtils.endsWith(page, "ReportFilter")) {
			//junior admins and above
			if (accessLevel >= AccessLevel.JuniorAdmin.getValue()) {
				authorised = true;
			}
		} else if (StringUtils.equals(page, "reportRules") || StringUtils.endsWith(page, "ReportRule")) {
			//junior admins and above
			if (accessLevel >= AccessLevel.JuniorAdmin.getValue()) {
				authorised = true;
			}
		} else if (StringUtils.equals(page, "parameters") || StringUtils.endsWith(page, "Parameter")
				|| StringUtils.endsWith(page, "Parameters")) {
			//junior admins and above
			if (accessLevel >= AccessLevel.JuniorAdmin.getValue()) {
				authorised = true;
			}
		} else if (StringUtils.equals(page, "filterValues") || StringUtils.endsWith(page, "FilterValue")
				|| StringUtils.equals(page, "filterValuesConfig") || StringUtils.endsWith(page, "FilterValues")) {
			//senior admins and above
			if (accessLevel >= AccessLevel.SeniorAdmin.getValue()) {
				authorised = true;
			}
		} else if (StringUtils.equals(page, "ruleValues") || StringUtils.endsWith(page, "RuleValue")
				|| StringUtils.equals(page, "ruleValuesConfig") || StringUtils.endsWith(page, "RuleValues")) {
			//senior admins and above
			if (accessLevel >= AccessLevel.SeniorAdmin.getValue()) {
				authorised = true;
			}
		} else if (StringUtils.equals(page, "reportParameterConfig")
				|| StringUtils.endsWith(page, "ReportParameter")
				|| StringUtils.endsWith(page, "ReportParameters")) {
			//junior admins and above
			if (accessLevel >= AccessLevel.JuniorAdmin.getValue()) {
				authorised = true;
			}
		}

		return authorised;
	}
}
