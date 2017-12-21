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
package art.destination;

import art.destinationoptions.NetworkShareOptions;
import art.encryption.AesEncryptor;
import art.enums.DestinationType;
import art.job.JobService;
import art.user.User;
import art.utils.ActionResult;
import art.utils.AjaxResponse;
import art.utils.ArtUtils;
import com.hierynomus.smbj.SMBClient;
import com.hierynomus.smbj.SmbConfig;
import com.hierynomus.smbj.auth.AuthenticationContext;
import com.hierynomus.smbj.share.DiskShare;
import com.jcraft.jsch.Channel;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.concurrent.TimeUnit;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Controller for destination configuration
 *
 * @author Timothy Anyona
 */
@Controller
public class DestinationController {

	private static final Logger logger = LoggerFactory.getLogger(DestinationController.class);

	@Autowired
	private DestinationService destinationService;

	@Autowired
	private JobService jobService;

	@Autowired
	private MessageSource messageSource;

	@RequestMapping(value = "/destinations", method = RequestMethod.GET)
	public String showDestinations(Model model) {
		logger.debug("Entering showDestinations");

		try {
			model.addAttribute("destinations", destinationService.getAllDestinations());
		} catch (SQLException | RuntimeException ex) {
			logger.error("Error", ex);
			model.addAttribute("error", ex);
		}

		return "destinations";
	}

	@RequestMapping(value = "/deleteDestination", method = RequestMethod.POST)
	public @ResponseBody
	AjaxResponse deleteDestination(@RequestParam("id") Integer id) {
		logger.debug("Entering deleteDestination: id={}", id);

		AjaxResponse response = new AjaxResponse();

		try {
			ActionResult deleteResult = destinationService.deleteDestination(id);

			logger.debug("deleteResult.isSuccess() = {}", deleteResult.isSuccess());
			if (deleteResult.isSuccess()) {
				response.setSuccess(true);
			} else {
				//destination not deleted because of linked jobs
				List<String> cleanedData = deleteResult.cleanData();
				response.setData(cleanedData);
			}
		} catch (SQLException | RuntimeException ex) {
			logger.error("Error", ex);
			response.setErrorMessage(ex.toString());
		}

		return response;
	}

	@RequestMapping(value = "/deleteDestinations", method = RequestMethod.POST)
	public @ResponseBody
	AjaxResponse deleteDestinations(@RequestParam("ids[]") Integer[] ids) {
		logger.debug("Entering deleteDestinations: ids={}", (Object) ids);

		AjaxResponse response = new AjaxResponse();

		try {
			ActionResult deleteResult = destinationService.deleteDestinations(ids);

			logger.debug("deleteResult.isSuccess() = {}", deleteResult.isSuccess());
			if (deleteResult.isSuccess()) {
				response.setSuccess(true);
			} else {
				List<String> cleanedData = deleteResult.cleanData();
				response.setData(cleanedData);
			}
		} catch (SQLException | RuntimeException ex) {
			logger.error("Error", ex);
			response.setErrorMessage(ex.toString());
		}

		return response;
	}

	@RequestMapping(value = "/addDestination", method = RequestMethod.GET)
	public String addDestination(Model model) {
		logger.debug("Entering addDestination");

		model.addAttribute("destination", new Destination());

		return showEditDestination("add", model);
	}

	@RequestMapping(value = "/editDestination", method = RequestMethod.GET)
	public String editDestination(@RequestParam("id") Integer id, Model model) {
		logger.debug("Entering editDestination: id={}", id);

		try {
			model.addAttribute("destination", destinationService.getDestination(id));
		} catch (SQLException | RuntimeException ex) {
			logger.error("Error", ex);
			model.addAttribute("error", ex);
		}

		return showEditDestination("edit", model);
	}

	@RequestMapping(value = "/editDestinations", method = RequestMethod.GET)
	public String editDestinations(@RequestParam("ids") String ids, Model model,
			HttpSession session) {

		logger.debug("Entering editDestinations: ids={}", ids);

		MultipleDestinationEdit multipleDestinationEdit = new MultipleDestinationEdit();
		multipleDestinationEdit.setIds(ids);

		model.addAttribute("multipleDestinationEdit", multipleDestinationEdit);

		return "editDestinations";
	}

	@RequestMapping(value = "/copyDestination", method = RequestMethod.GET)
	public String copyDestination(@RequestParam("id") Integer id, Model model) {
		logger.debug("Entering copyDestination: id={}", id);

		try {
			model.addAttribute("destination", destinationService.getDestination(id));
		} catch (SQLException | RuntimeException ex) {
			logger.error("Error", ex);
			model.addAttribute("error", ex);
		}

		return showEditDestination("copy", model);
	}

	@RequestMapping(value = "/saveDestination", method = RequestMethod.POST)
	public String saveDestination(@ModelAttribute("destination") @Valid Destination destination,
			@RequestParam("action") String action,
			BindingResult result, Model model, RedirectAttributes redirectAttributes,
			HttpSession session) {

		logger.debug("Entering saveDestination: destination={}, action='{}'", destination, action);

		logger.debug("result.hasErrors()={}", result.hasErrors());
		if (result.hasErrors()) {
			model.addAttribute("formErrors", "");
			return showEditDestination(action, model);
		}

		try {
			//set password as appropriate
			String setPasswordMessage = setPassword(destination, action);
			logger.debug("setPasswordMessage='{}'", setPasswordMessage);
			if (setPasswordMessage != null) {
				model.addAttribute("message", setPasswordMessage);
				return showEditDestination(action, model);
			}

			User sessionUser = (User) session.getAttribute("sessionUser");
			if (StringUtils.equals(action, "add") || StringUtils.equals(action, "copy")) {
				destinationService.addDestination(destination, sessionUser);
				redirectAttributes.addFlashAttribute("recordSavedMessage", "page.message.recordAdded");
			} else if (StringUtils.equals(action, "edit")) {
				destinationService.updateDestination(destination, sessionUser);
				redirectAttributes.addFlashAttribute("recordSavedMessage", "page.message.recordUpdated");
			}

			String recordName = destination.getName() + " (" + destination.getDestinationId() + ")";
			redirectAttributes.addFlashAttribute("recordName", recordName);

			return "redirect:/destinations";
		} catch (SQLException | RuntimeException ex) {
			logger.error("Error", ex);
			model.addAttribute("error", ex);
		}

		return showEditDestination(action, model);
	}

	@RequestMapping(value = "/saveDestinations", method = RequestMethod.POST)
	public String saveDestinations(@ModelAttribute("multipleDestinationEdit") @Valid MultipleDestinationEdit multipleDestinationEdit,
			BindingResult result, Model model, RedirectAttributes redirectAttributes,
			HttpSession session) {

		logger.debug("Entering saveDestinations: multipleDestinationEdit={}", multipleDestinationEdit);

		logger.debug("result.hasErrors()={}", result.hasErrors());
		if (result.hasErrors()) {
			model.addAttribute("formErrors", "");
			return showEditDestinations();
		}

		try {
			User sessionUser = (User) session.getAttribute("sessionUser");
			destinationService.updateDestinations(multipleDestinationEdit, sessionUser);
			redirectAttributes.addFlashAttribute("recordSavedMessage", "page.message.recordsUpdated");
			redirectAttributes.addFlashAttribute("recordName", multipleDestinationEdit.getIds());
			return "redirect:/destinations";
		} catch (SQLException | RuntimeException ex) {
			logger.error("Error", ex);
			model.addAttribute("error", ex);
		}

		return showEditDestinations();
	}

	/**
	 * Prepares model data and returns the jsp file to display
	 *
	 * @param action the action to use
	 * @param model the model to use
	 * @return the jsp file to display
	 */
	private String showEditDestination(String action, Model model) {
		logger.debug("Entering showDestination: action='{}'", action);

		model.addAttribute("destinationTypes", DestinationType.list());
		model.addAttribute("action", action);

		return "editDestination";
	}

	/**
	 * Prepares model data and returns jsp file to display
	 *
	 * @param action
	 * @param model
	 * @return
	 */
	private String showEditDestinations() {
		logger.debug("Entering showEditDestinations");

		return "editDestinations";
	}

	/**
	 * Sets the password field of the destination
	 *
	 * @param destination the destination object to set
	 * @param action "add or "edit"
	 * @return i18n message to display in the user interface if there was a
	 * problem, null otherwise
	 * @throws SQLException
	 */
	private String setPassword(Destination destination, String action) throws SQLException {
		logger.debug("Entering setPassword: destination={}, action='{}'", destination, action);

		//encrypt password
		boolean useCurrentPassword = false;
		String newPassword = destination.getPassword();

		if (destination.isUseBlankPassword()) {
			newPassword = "";
		} else {
			if (StringUtils.isEmpty(newPassword) && StringUtils.equals(action, "edit")) {
				//password field blank. use current password
				useCurrentPassword = true;
			}
		}

		if (useCurrentPassword) {
			//password field blank. use current password
			Destination currentDestination = destinationService.getDestination(destination.getDestinationId());
			if (currentDestination == null) {
				return "page.message.cannotUseCurrentPassword";
			} else {
				newPassword = currentDestination.getPassword();
			}
		}

		//encrypt new password
		String encryptedPassword = AesEncryptor.encrypt(newPassword);
		destination.setPassword(encryptedPassword);

		return null;
	}

	@RequestMapping(value = "/jobsWithDestination", method = RequestMethod.GET)
	public String showJobsWithDestination(@RequestParam("destinationId") Integer destinationId, Model model) {
		logger.debug("Entering showJobsWithDestination: destinationId={}", destinationId);

		try {
			model.addAttribute("jobs", jobService.getJobsWithDestination(destinationId));
			model.addAttribute("destination", destinationService.getDestination(destinationId));
		} catch (SQLException | RuntimeException ex) {
			logger.error("Error", ex);
			model.addAttribute("error", ex);
		}

		return "jobsWithDestination";
	}

	@RequestMapping(value = "/testDestination", method = RequestMethod.POST)
	public @ResponseBody
	AjaxResponse testDestination(@RequestParam("id") Integer id,
			@RequestParam("destinationType") DestinationType destinationType,
			@RequestParam("server") String server, @RequestParam("port") Integer port,
			@RequestParam("user") String user, @RequestParam("password") String password,
			@RequestParam("useBlankPassword") Boolean useBlankPassword,
			@RequestParam("domain") String domain, @RequestParam("path") String path,
			@RequestParam("options") String options,
			@RequestParam("action") String action,
			Locale locale) {

		logger.debug("Entering testDestination: id={}, server='{}', port={},"
				+ " user='{}', useBlankPassword={}, domain='{}', path='{}',"
				+ " options='{}', action='{}'", id, server, port, user,
				useBlankPassword, domain, path, options, action);

		AjaxResponse response = new AjaxResponse();

		try {
			//set password as appropriate
			boolean useCurrentPassword = false;
			if (useBlankPassword) {
				password = "";
			} else {
				if (StringUtils.isEmpty(password)) {
					//password field blank. use current password
					useCurrentPassword = true;
				}
			}

			if ((StringUtils.equalsAnyIgnoreCase(action, "edit", "copy"))
					&& useCurrentPassword) {
				//password field blank. use current password
				Destination currentDestination = destinationService.getDestination(id);
				logger.debug("currentDestination={}", currentDestination);
				if (currentDestination == null) {
					response.setErrorMessage(messageSource.getMessage("page.message.cannotUseCurrentPassword", null, locale));
					return response;
				} else {
					password = currentDestination.getPassword();
				}
			}

			testConnection(destinationType, server, port, user, password, domain, path, options);

			//if we are here, connection was successful
			response.setSuccess(true);
		} catch (SQLException | IOException | JSchException | RuntimeException ex) {
			logger.error("Error", ex);
			response.setErrorMessage(ex.toString());
		}

		return response;
	}

	/**
	 * Tests a destination connection. Throws an error if connection was not
	 * successful. Otherwise, connection was successful.
	 *
	 * @param destinationType the destination type
	 * @param server the ip or host name of the destination
	 * @param port the port of the destination
	 * @param user the user to connect to the destination
	 * @param password the password to connect to the destination
	 * @param domain for network share destinations, an optional user domain
	 * @param path for network share destinations, the share name
	 * @param options destination options
	 * @throws IOException
	 * @throws JSchException
	 */
	private void testConnection(DestinationType destinationType,
			String server, Integer port, String user, String password,
			String domain, String path, String options) throws IOException, JSchException {

		switch (destinationType) {
			case FTP:
				testFtp(server, port, user, password);
				break;
			case SFTP:
				testSftp(server, port, user, password);
				break;
			case NetworkShare:
				testNetworkShare(server, port, user, password, domain, path, options);
				break;
			default:
			//do nothing
		}

	}

	/**
	 * Tests an ftp connection. Throws an error if connection was not
	 * successful. Otherwise, connection was successful.
	 *
	 * @param server the ip or host name of the server
	 * @param port the port of the server
	 * @param user the user to connect to the server
	 * @param password the password to connect to the server
	 * @throws IOException
	 */
	private void testFtp(String server, Integer port, String user, String password)
			throws IOException {

		if (port <= 0) {
			final int DEFAULT_FTP_PORT = 21;
			port = DEFAULT_FTP_PORT;
		}

		FTPClient ftpClient = new FTPClient();

		try {
			final long CONNECT_TIMEOUT_SECONDS = 10;
			int connectTimeoutMillis = (int) TimeUnit.SECONDS.toMillis(CONNECT_TIMEOUT_SECONDS);
			ftpClient.setConnectTimeout(connectTimeoutMillis);

			ftpClient.connect(server, port);

			// After connection attempt, you should check the reply code to verify
			// success.
			int reply = ftpClient.getReplyCode();

			if (!FTPReply.isPositiveCompletion(reply)) {
				ftpClient.disconnect();
				throw new IOException("FTP server refused connection");
			}

			if (!ftpClient.login(user, password)) {
				throw new IOException("FTP login failed");
			}

			ftpClient.logout();
		} finally {
			try {
				if (ftpClient.isConnected()) {
					ftpClient.disconnect();
				}
			} catch (IOException ex) {
				logger.error("Error", ex);
			}
		}

	}

	/**
	 * Tests an sftp connection. Throws an error if connection was not
	 * successful. Otherwise, connection was successful.
	 *
	 * @param server the ip or host name of the server
	 * @param port the port of the server
	 * @param user the user to connect to the server
	 * @param password the password to connect to the server
	 * @throws JSchException
	 */
	private void testSftp(String server, Integer port, String user, String password)
			throws JSchException {

		if (port <= 0) {
			final int DEFAULT_SFTP_PORT = 22;
			port = DEFAULT_SFTP_PORT;
		}

		Session session = null;
		Channel channel = null;

		try {
			JSch jsch = new JSch();
			session = jsch.getSession(user, server, port);
			session.setPassword(password);

			Properties config = new Properties();
			config.put("StrictHostKeyChecking", "no");
			session.setConfig(config);

			final long CONNECT_TIMEOUT_SECONDS = 10;
			int connectTimeoutMillis = (int) TimeUnit.SECONDS.toMillis(CONNECT_TIMEOUT_SECONDS);
			session.setTimeout(connectTimeoutMillis);

			session.connect();
			channel = session.openChannel("sftp");
			channel.connect(connectTimeoutMillis);
		} finally {
			if (channel != null) {
				channel.disconnect();
			}

			if (session != null) {
				session.disconnect();
			}
		}
	}

	/**
	 * Tests a network share connection. Throws an error if connection was not
	 * successful. Otherwise, connection was successful.
	 *
	 * @param server the ip or host name of the server
	 * @param port an optional port of the server
	 * @param user the user to connect to the server
	 * @param password the password to connect to the server
	 * @param domain an optional user domain
	 * @param path the share name
	 * @param options destination options
	 * @throws IOException
	 */
	private void testNetworkShare(String server, Integer port, String user,
			String password, String domain, String path, String options)
			throws IOException {

		com.hierynomus.smbj.connection.Connection connection = null;

		try {
			NetworkShareOptions networkShareOptions;
			if (StringUtils.isBlank(options)) {
				networkShareOptions = new NetworkShareOptions();
			} else {
				networkShareOptions = ArtUtils.jsonToObject(options, NetworkShareOptions.class);
			}

			SmbConfig.Builder configBuilder = SmbConfig.builder();
			if (networkShareOptions.getTimeoutSeconds() != null) {
				configBuilder = configBuilder.withTimeout(networkShareOptions.getTimeoutSeconds(), TimeUnit.SECONDS);
			}
			if (networkShareOptions.getSocketTimeoutSeconds() != null) {
				configBuilder = configBuilder.withSoTimeout(networkShareOptions.getSocketTimeoutSeconds(), TimeUnit.SECONDS);
			}
			if (networkShareOptions.getMultiProtocolNegotiate() != null) {
				configBuilder = configBuilder.withMultiProtocolNegotiate(networkShareOptions.getMultiProtocolNegotiate());
			}
			if (networkShareOptions.getDfsEnabled() != null) {
				configBuilder = configBuilder.withDfsEnabled(networkShareOptions.getDfsEnabled());
			}
			if (networkShareOptions.getSigningRequired() != null) {
				configBuilder = configBuilder.withSigningRequired(networkShareOptions.getSigningRequired());
			}
			if (networkShareOptions.getBufferSize() != null) {
				configBuilder = configBuilder.withBufferSize(networkShareOptions.getBufferSize());
			}

			SmbConfig config = configBuilder.build();

			SMBClient client = new SMBClient(config);

			if (port > 0) {
				connection = client.connect(server, port);
			} else {
				connection = client.connect(server);
			}

			String username = user;
			if (username == null) {
				username = "";
			}

			if (password == null) {
				password = "";
			}

			AuthenticationContext ac;
			if (networkShareOptions.isAnonymousUser()) {
				ac = AuthenticationContext.anonymous();
			} else if (networkShareOptions.isGuestUser()) {
				ac = AuthenticationContext.guest();
			} else {
				ac = new AuthenticationContext(username, password.toCharArray(), domain);
			}

			com.hierynomus.smbj.session.Session session = connection.authenticate(ac);

			// Connect to Share
			DiskShare share = (DiskShare) session.connectShare(path);
			share.close();
		} finally {
			if (connection != null) {
				try {
					connection.close();
				} catch (Exception ex) {
					logger.error("Error", ex);
				}
			}
		}

	}
}
