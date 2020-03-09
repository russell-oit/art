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
package art.smtpserver;

import art.cache.CacheHelper;
import art.dbutils.DatabaseUtils;
import art.dbutils.DbService;
import art.encryption.AesEncryptor;
import art.user.User;
import art.general.ActionResult;
import art.utils.ArtUtils;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import org.apache.commons.dbutils.BasicRowProcessor;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.BeanHandler;
import org.apache.commons.dbutils.handlers.BeanListHandler;
import org.apache.commons.dbutils.handlers.MapListHandler;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.jsoup.Connection.Method;
import org.jsoup.Connection.Response;
import org.jsoup.Jsoup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

/**
 * Provides methods for adding, deleting, retrieving and updating smtp server
 * configurations
 *
 * @author Timothy Anyona
 */
@Service
public class SmtpServerService {

	private static final Logger logger = LoggerFactory.getLogger(SmtpServerService.class);

	@Autowired
	private CacheHelper cacheHelper;

	private final DbService dbService;

	@Autowired
	public SmtpServerService(DbService dbService) {
		this.dbService = dbService;
	}

	public SmtpServerService() {
		dbService = new DbService();
	}

	private final String SQL_SELECT_ALL = "SELECT * FROM ART_SMTP_SERVERS";

	/**
	 * Maps a resultset to an object
	 */
	private class SmtpServerMapper extends BasicRowProcessor {

		@Override
		public <T> List<T> toBeanList(ResultSet rs, Class<T> type) throws SQLException {
			List<T> list = new ArrayList<>();
			while (rs.next()) {
				list.add(toBean(rs, type));
			}
			return list;
		}

		@Override
		public <T> T toBean(ResultSet rs, Class<T> type) throws SQLException {
			SmtpServer smtpServer = new SmtpServer();

			smtpServer.setSmtpServerId(rs.getInt("SMTP_SERVER_ID"));
			smtpServer.setName(rs.getString("NAME"));
			smtpServer.setDescription(rs.getString("DESCRIPTION"));
			smtpServer.setActive(rs.getBoolean("ACTIVE"));
			smtpServer.setServer(rs.getString("SERVER"));
			smtpServer.setPort(rs.getInt("PORT"));
			smtpServer.setUseStartTls(rs.getBoolean("USE_STARTTLS"));
			smtpServer.setUseSmtpAuthentication(rs.getBoolean("USE_SMTP_AUTHENTICATION"));
			smtpServer.setUsername(rs.getString("USERNAME"));
			smtpServer.setPassword(rs.getString("PASSWORD"));
			smtpServer.setFrom(rs.getString("SMTP_FROM"));
			smtpServer.setUseOAuth2(rs.getBoolean("USE_GOOGLE_OAUTH_2"));
			smtpServer.setOauthClientId(rs.getString("OAUTH_CLIENT_ID"));
			smtpServer.setOauthClientSecret(rs.getString("OAUTH_CLIENT_SECRET"));
			smtpServer.setOauthRefreshToken(rs.getString("OAUTH_REFRESH_TOKEN"));
			smtpServer.setOauthAccessToken(rs.getString("OAUTH_ACCESS_TOKEN"));
			smtpServer.setOauthAccessTokenExpiry(rs.getTimestamp("OAUTH_ACCESS_TOKEN_EXPIRY"));
			smtpServer.setCreationDate(rs.getTimestamp("CREATION_DATE"));
			smtpServer.setUpdateDate(rs.getTimestamp("UPDATE_DATE"));
			smtpServer.setCreatedBy(rs.getString("CREATED_BY"));
			smtpServer.setUpdatedBy(rs.getString("UPDATED_BY"));

			try {
				smtpServer.decryptPasswords();
			} catch (Exception ex) {
				logger.error("Error. {}", smtpServer, ex);
			}

			return type.cast(smtpServer);
		}
	}

	/**
	 * Returns all smtp servers
	 *
	 * @return all smtp servers
	 * @throws SQLException
	 */
	@Cacheable("smtpServers")
	public List<SmtpServer> getAllSmtpServers() throws SQLException {
		logger.debug("Entering getAllSmtpServers");

		ResultSetHandler<List<SmtpServer>> h = new BeanListHandler<>(SmtpServer.class, new SmtpServerMapper());
		return dbService.query(SQL_SELECT_ALL, h);
	}

	/**
	 * Returns smtp servers with given ids
	 *
	 * @param ids comma separated string of the smtp server ids to retrieve
	 * @return smtp servers with given ids
	 * @throws SQLException
	 */
	public List<SmtpServer> getSmtpServers(String ids) throws SQLException {
		logger.debug("Entering getSmtpServers: ids='{}'", ids);

		Object[] idsArray = ArtUtils.idsToObjectArray(ids);

		String sql = SQL_SELECT_ALL
				+ " WHERE SMTP_SERVER_ID IN(" + StringUtils.repeat("?", ",", idsArray.length) + ")";

		ResultSetHandler<List<SmtpServer>> h = new BeanListHandler<>(SmtpServer.class, new SmtpServerMapper());
		return dbService.query(sql, h, idsArray);
	}

	/**
	 * Returns the smtp server with the given id
	 *
	 * @param id the smtp server id
	 * @return the smtp server if found, null otherwise
	 * @throws SQLException
	 */
	@Cacheable("smtpServers")
	public SmtpServer getSmtpServer(int id) throws SQLException {
		logger.debug("Entering getSmtpServer: id={}", id);

		String sql = SQL_SELECT_ALL + " WHERE SMTP_SERVER_ID=?";
		ResultSetHandler<SmtpServer> h = new BeanHandler<>(SmtpServer.class, new SmtpServerMapper());
		return dbService.query(sql, h, id);
	}

	/**
	 * Deletes an smtp server
	 *
	 * @param id the smtp server id
	 * @return ActionResult. if not successful, data contains a list of linked
	 * jobs which prevented the smtp server from being deleted
	 * @throws SQLException
	 */
	@CacheEvict(value = "smtpServers", allEntries = true)
	public ActionResult deleteSmtpServer(int id) throws SQLException {
		logger.debug("Entering deleteSmtpServer: id={}", id);

		ActionResult result = new ActionResult();

		//don't delete if important linked records exist
		List<String> linkedJobs = getLinkedJobs(id);
		if (!linkedJobs.isEmpty()) {
			result.setData(linkedJobs);
			return result;
		}

		String sql;

		sql = "DELETE FROM ART_SMTP_SERVERS WHERE SMTP_SERVER_ID=?";
		dbService.update(sql, id);

		result.setSuccess(true);

		return result;
	}

	/**
	 * Deletes multiple smtp servers
	 *
	 * @param ids the ids of smtp servers to delete
	 * @return ActionResult. if not successful, data contains details of smtp
	 * servers which weren't deleted
	 * @throws SQLException
	 */
	@CacheEvict(value = "smtpServers", allEntries = true)
	public ActionResult deleteSmtpServers(Integer[] ids) throws SQLException {
		logger.debug("Entering deleteSmtpServers: ids={}", (Object) ids);

		ActionResult result = new ActionResult();
		List<String> nonDeletedRecords = new ArrayList<>();

		for (Integer id : ids) {
			ActionResult deleteResult = deleteSmtpServer(id);
			if (!deleteResult.isSuccess()) {
				@SuppressWarnings("unchecked")
				List<String> linkedJobs = (List<String>) deleteResult.getData();
				String value = String.valueOf(id) + " - " + StringUtils.join(linkedJobs, ", ");
				nonDeletedRecords.add(value);
			}
		}

		if (nonDeletedRecords.isEmpty()) {
			result.setSuccess(true);
		} else {
			result.setData(nonDeletedRecords);
		}

		return result;
	}

	/**
	 * Adds a new smtp server
	 *
	 * @param smtpServer the smtp server
	 * @param actionUser the user who is performing the action
	 * @return new record id
	 * @throws SQLException
	 */
	@CacheEvict(value = "smtpServers", allEntries = true)
	public synchronized int addSmtpServer(SmtpServer smtpServer, User actionUser) throws SQLException {
		logger.debug("Entering addSmtpServer: smtpServer={}, actionUser={}", smtpServer, actionUser);

		//generate new id
		String sql = "SELECT MAX(SMTP_SERVER_ID) FROM ART_SMTP_SERVERS";
		int newId = dbService.getNewRecordId(sql);

		saveSmtpServer(smtpServer, newId, actionUser);

		return newId;
	}

	/**
	 * Updates an smtp server
	 *
	 * @param smtpServer the updated smtp server
	 * @param actionUser the user who is performing the action
	 * @throws SQLException
	 */
	@CacheEvict(value = {"smtpServers", "jobs"}, allEntries = true)
	public void updateSmtpServer(SmtpServer smtpServer, User actionUser) throws SQLException {
		Connection conn = null;
		updateSmtpServer(smtpServer, actionUser, conn);
	}

	/**
	 * Updates an smtp server
	 *
	 * @param smtpServer the updated smtp server
	 * @param actionUser the user who is performing the action
	 * @param conn the connection to use
	 * @throws SQLException
	 */
	@CacheEvict(value = {"smtpServers", "jobs"}, allEntries = true)
	public void updateSmtpServer(SmtpServer smtpServer, User actionUser,
			Connection conn) throws SQLException {

		logger.debug("Entering updateSmtpServer: smtpServer={}, actionUser={}",
				smtpServer, actionUser);

		Integer newRecordId = null;
		saveSmtpServer(smtpServer, newRecordId, actionUser, conn);
	}

	/**
	 * Imports smtp server records
	 *
	 * @param smtpServers the list of smtp servers to import
	 * @param actionUser the user who is performing the import
	 * @param conn the connection to use
	 * @param overwrite whether to overwrite existing records
	 * @throws SQLException
	 */
	@CacheEvict(value = "smtpServers", allEntries = true)
	public void importSmtpServers(List<SmtpServer> smtpServers, User actionUser,
			Connection conn, boolean overwrite) throws SQLException {

		logger.debug("Entering importSmtpServers: actionUser={}, overwrite={}",
				actionUser, overwrite);

		boolean originalAutoCommit = true;

		try {
			String sql = "SELECT MAX(SMTP_SERVER_ID) FROM ART_SMTP_SERVERS";
			int id = dbService.getMaxRecordId(conn, sql);

			List<SmtpServer> currentSmtpServers = new ArrayList<>();
			if (overwrite) {
				currentSmtpServers = getAllSmtpServers();
			}

			originalAutoCommit = conn.getAutoCommit();
			conn.setAutoCommit(false);

			for (SmtpServer smtpServer : smtpServers) {
				String smtpServerName = smtpServer.getName();
				boolean update = false;
				if (overwrite) {
					SmtpServer existingSmtpServer = currentSmtpServers.stream()
							.filter(d -> StringUtils.equals(smtpServerName, d.getName()))
							.findFirst()
							.orElse(null);
					if (existingSmtpServer != null) {
						update = true;
						smtpServer.setSmtpServerId(existingSmtpServer.getSmtpServerId());
					}
				}

				Integer newRecordId;
				if (update) {
					newRecordId = null;
				} else {
					id++;
					newRecordId = id;
				}
				saveSmtpServer(smtpServer, newRecordId, actionUser, conn);
			}
			conn.commit();
		} catch (SQLException ex) {
			conn.rollback();
			throw ex;
		} finally {
			conn.setAutoCommit(originalAutoCommit);
		}
	}

	/**
	 * Saves an smtp server
	 *
	 * @param smtpServer the smtp server to save
	 * @param newRecordId id of the new record or null if editing an existing
	 * record
	 * @param actionUser the user who is performing the save
	 * @throws SQLException
	 */
	private void saveSmtpServer(SmtpServer smtpServer, Integer newRecordId,
			User actionUser) throws SQLException {

		Connection conn = null;
		saveSmtpServer(smtpServer, newRecordId, actionUser, conn);
	}

	/**
	 * Saves an smtp server
	 *
	 * @param smtpServer the smtp server
	 * @param newRecordId id of the new record or null if editing an existing
	 * record
	 * @param actionUser the user who is performing the action
	 * @param conn the connection to use. if null, the art database will be used
	 * @throws SQLException
	 */
	private void saveSmtpServer(SmtpServer smtpServer, Integer newRecordId,
			User actionUser, Connection conn) throws SQLException {
		logger.debug("Entering saveSmtpServer: smtpServer={}, newRecordId={},"
				+ " actionUser={}", smtpServer, newRecordId, actionUser);

		int affectedRows;

		boolean newRecord = false;
		if (newRecordId != null) {
			newRecord = true;
		}

		if (newRecord) {
			String sql = "INSERT INTO ART_SMTP_SERVERS"
					+ " (SMTP_SERVER_ID, NAME, DESCRIPTION, ACTIVE,"
					+ " SERVER, PORT, USE_STARTTLS, USE_SMTP_AUTHENTICATION,"
					+ " USERNAME, PASSWORD, SMTP_FROM, USE_GOOGLE_OAUTH_2,"
					+ " OAUTH_CLIENT_ID, OAUTH_CLIENT_SECRET, OAUTH_REFRESH_TOKEN,"
					+ " OAUTH_ACCESS_TOKEN, OAUTH_ACCESS_TOKEN_EXPIRY,"
					+ " CREATION_DATE, CREATED_BY)"
					+ " VALUES(" + StringUtils.repeat("?", ",", 19) + ")";

			Object[] values = {
				newRecordId,
				smtpServer.getName(),
				smtpServer.getDescription(),
				BooleanUtils.toInteger(smtpServer.isActive()),
				smtpServer.getServer(),
				smtpServer.getPort(),
				BooleanUtils.toInteger(smtpServer.isUseStartTls()),
				BooleanUtils.toInteger(smtpServer.isUseSmtpAuthentication()),
				smtpServer.getUsername(),
				smtpServer.getPassword(),
				smtpServer.getFrom(),
				BooleanUtils.toInteger(smtpServer.isUseOAuth2()),
				smtpServer.getOauthClientId(),
				smtpServer.getOauthClientSecret(),
				smtpServer.getOauthRefreshToken(),
				smtpServer.getOauthAccessToken(),
				DatabaseUtils.toSqlTimestamp(smtpServer.getOauthAccessTokenExpiry()),
				DatabaseUtils.getCurrentTimeAsSqlTimestamp(),
				actionUser.getUsername()
			};

			if (conn == null) {
				affectedRows = dbService.update(sql, values);
			} else {
				affectedRows = dbService.update(conn, sql, values);
			}
		} else {
			String sql = "UPDATE ART_SMTP_SERVERS SET NAME=?, DESCRIPTION=?,"
					+ " ACTIVE=?, SERVER=?, PORT=?, USE_STARTTLS=?,"
					+ " USE_SMTP_AUTHENTICATION=?, USERNAME=?, PASSWORD=?,"
					+ " SMTP_FROM=?, USE_GOOGLE_OAUTH_2=?, OAUTH_CLIENT_ID=?,"
					+ " OAUTH_CLIENT_SECRET=?, OAUTH_REFRESH_TOKEN=?,"
					+ " OAUTH_ACCESS_TOKEN=?, OAUTH_ACCESS_TOKEN_EXPIRY=?,"
					+ " UPDATE_DATE=?, UPDATED_BY=?"
					+ " WHERE SMTP_SERVER_ID=?";

			Object[] values = {
				smtpServer.getName(),
				smtpServer.getDescription(),
				BooleanUtils.toInteger(smtpServer.isActive()),
				smtpServer.getServer(),
				smtpServer.getPort(),
				BooleanUtils.toInteger(smtpServer.isUseStartTls()),
				BooleanUtils.toInteger(smtpServer.isUseSmtpAuthentication()),
				smtpServer.getUsername(),
				smtpServer.getPassword(),
				smtpServer.getFrom(),
				BooleanUtils.toInteger(smtpServer.isUseOAuth2()),
				smtpServer.getOauthClientId(),
				smtpServer.getOauthClientSecret(),
				smtpServer.getOauthRefreshToken(),
				smtpServer.getOauthAccessToken(),
				DatabaseUtils.toSqlTimestamp(smtpServer.getOauthAccessTokenExpiry()),
				DatabaseUtils.getCurrentTimeAsSqlTimestamp(),
				actionUser.getUsername(),
				smtpServer.getSmtpServerId()
			};

			if (conn == null) {
				affectedRows = dbService.update(sql, values);
			} else {
				affectedRows = dbService.update(conn, sql, values);
			}
		}

		if (newRecordId != null) {
			smtpServer.setSmtpServerId(newRecordId);
		}

		logger.debug("affectedRows={}", affectedRows);

		if (affectedRows != 1) {
			logger.warn("Problem with save. affectedRows={}, newRecord={}, smtpServer={}",
					affectedRows, newRecord, smtpServer);
		}
	}

	/**
	 * Updates multiple smtp servers
	 *
	 * @param multipleSmtpServerEdit the multiple smtp server edit object
	 * @param actionUser the user who is performing the edit
	 * @throws SQLException
	 */
	@CacheEvict(value = {"smtpServers", "jobs"}, allEntries = true)
	public void updateSmtpServers(MultipleSmtpServerEdit multipleSmtpServerEdit, User actionUser)
			throws SQLException {

		logger.debug("Entering updateSmtpServers: multipleSmtpServerEdit={}, actionUser={}",
				multipleSmtpServerEdit, actionUser);

		String sql;

		List<Object> idsList = ArtUtils.idsToObjectList(multipleSmtpServerEdit.getIds());
		if (!multipleSmtpServerEdit.isActiveUnchanged()) {
			sql = "UPDATE ART_SMTP_SERVERS SET ACTIVE=?, UPDATED_BY=?, UPDATE_DATE=?"
					+ " WHERE SMTP_SERVER_ID IN(" + StringUtils.repeat("?", ",", idsList.size()) + ")";

			List<Object> valuesList = new ArrayList<>();
			valuesList.add(BooleanUtils.toInteger(multipleSmtpServerEdit.isActive()));
			valuesList.add(actionUser.getUsername());
			valuesList.add(DatabaseUtils.getCurrentTimeAsSqlTimestamp());
			valuesList.addAll(idsList);

			Object[] valuesArray = valuesList.toArray(new Object[valuesList.size()]);

			dbService.update(sql, valuesArray);
		}
	}

	/**
	 * Returns details of jobs that use a given smtp server
	 *
	 * @param smtpServerId the smtp server id
	 * @return linked job details
	 * @throws SQLException
	 */
	public List<String> getLinkedJobs(int smtpServerId) throws SQLException {
		logger.debug("Entering getLinkedJobs: smtpServerId={}", smtpServerId);

		String sql = "SELECT JOB_ID, JOB_NAME"
				+ " FROM ART_JOBS"
				+ " WHERE SMTP_SERVER_ID=?";

		ResultSetHandler<List<Map<String, Object>>> h = new MapListHandler();
		List<Map<String, Object>> jobDetails = dbService.query(sql, h, smtpServerId);

		List<String> jobs = new ArrayList<>();
		for (Map<String, Object> jobDetail : jobDetails) {
			Number jobId = (Number) jobDetail.get("JOB_ID");
			String jobName = (String) jobDetail.get("JOB_NAME");
			jobs.add(jobName + " (" + String.valueOf(jobId) + ")");
		}

		return jobs;
	}

	/**
	 * Updates the oauth access token if it has expired
	 *
	 * @param smtpServer the smtp server object
	 * @throws Exception
	 */
	public void updateOAuthAccessToken(SmtpServer smtpServer) throws Exception {
		boolean updateDatabase = true;
		updateOAuthAccessToken(smtpServer, updateDatabase);
	}

	/**
	 * Updates the oauth access token if it has expired
	 *
	 * @param smtpServer the smtp server object
	 * @param updateDatabase whether to update the database with any newly
	 * generated access token
	 * @throws Exception
	 */
	public void updateOAuthAccessToken(SmtpServer smtpServer, boolean updateDatabase)
			throws Exception {

		logger.debug("Entering updateOAuthAccessToken: smtpServer={},"
				+ " updateDatabase={}", smtpServer, updateDatabase);

		if (!smtpServer.isUseOAuth2()) {
			return;
		}

		Date expiry = smtpServer.getOauthAccessTokenExpiry();
		if (expiry == null || expiry.before(new Date())) {
			//https://stackoverflow.com/questions/6348633/reading-json-content
			//https://stackoverflow.com/questions/7133118/jsoup-requesting-json-response
			//https://stackoverflow.com/questions/39450438/jsoup-post-request-encoding/39457512
			//https://medium.com/@pablo127/google-api-authentication-with-oauth-2-on-the-example-of-gmail-a103c897fd98
			//https://developers.google.com/gmail/api/auth/scopes
			//https://blog.timekit.io/google-oauth-invalid-grant-nightmare-and-how-to-fix-it-9f4efaf1da35?gi=995710255996
			//https://developers.google.com/identity/protocols/OAuth2?csw=1
			//https://stackoverflow.com/questions/32831174/how-to-send-mail-with-java-mail-by-using-gmail-smtp-with-oauth2-authentication
			//https://www.themarketingtechnologist.co/google-oauth-2-enable-your-application-to-access-data-from-a-google-user/
			//https://stackoverflow.com/questions/45550385/javamail-gmail-and-oauth2
			//https://stackoverflow.com/questions/53836992/using-googles-oauth-2-0-revoke-endpoint-invalidates-all-other-tokens-for-user
			//https://developers.google.com/identity/protocols/OAuth2WebServer
			//https://www.oauth.com/oauth2-servers/client-registration/client-id-secret/
			//https://developers.google.com/gmail/imap/xoauth2-protocol
			//https://chariotsolutions.com/blog/post/sending-mail-via-gmail-javamail/
			//https://github.com/google/gmail-oauth2-tools/wiki/OAuth2DotPyRunThrough
			//https://security.stackexchange.com/questions/66025/what-are-the-dangers-of-allowing-less-secure-apps-to-access-my-google-account
			//https://support.google.com/mail/answer/7126229?p=BadCredentials&visit_id=636955096563329495-1274740859&rd=2#cantsignin
			//https://stackoverflow.com/questions/2965251/javamail-with-gmail-535-5-7-1-username-and-password-not-accepted?rq=1
			String tokenUrl = "https://www.googleapis.com/oauth2/v4/token";
			Response response = Jsoup.connect(tokenUrl)
					.method(Method.POST)
					.data("client_id", smtpServer.getOauthClientId())
					.data("client_secret", smtpServer.getOauthClientSecret())
					.data("refresh_token", smtpServer.getOauthRefreshToken())
					.data("grant_type", "refresh_token")
					.ignoreContentType(true)
					.execute();

			String responseBody = response.body();
			int responseStatusCode = response.statusCode();
			if (responseStatusCode == 200) {
				ObjectMapper mapper = new ObjectMapper();
				Map<String, Object> map = mapper.readValue(responseBody, new TypeReference<Map<String, Object>>() {
				});
				String accessToken = (String) map.get("access_token");
				Integer expiresInSeconds = (Integer) map.get("expires_in");
				final int GAP_MINUTES = 2; //set expiry to x minutes before actual expiry
				int finalExpiresInSeconds = expiresInSeconds - (int) TimeUnit.MINUTES.toSeconds(GAP_MINUTES);
				Date newExpiry = DateUtils.addSeconds(new Date(), finalExpiresInSeconds);
				smtpServer.setOauthAccessToken(accessToken);
				smtpServer.setOauthAccessTokenExpiry(newExpiry);
				if (updateDatabase) {
					updateDatabaseOAuthAccessToken(smtpServer);
				}
			} else {
				StringBuilder sb = new StringBuilder();

				sb.append("Refresh google oauth token failed. ")
						.append(smtpServer)
						.append("Response: Status Code=")
						.append(responseStatusCode)
						.append(", Content=")
						.append(responseBody);

				String message = sb.toString();

				throw new RuntimeException(message);
			}
		}
	}

	/**
	 * Updates the oath access token field in the database
	 *
	 * @param smtpServer the smtp server object with the updated access token
	 * @throws Exception
	 */
	private void updateDatabaseOAuthAccessToken(SmtpServer smtpServer) throws Exception {
		logger.debug("Entering updateDatabaseOAuthAccessToken: smtpServer={}", smtpServer);

		String sql = "UPDATE ART_SMTP_SERVERS"
				+ " SET OAUTH_ACCESS_TOKEN=?, OAUTH_ACCESS_TOKEN_EXPIRY=?"
				+ " WHERE SMTP_SERVER_ID=?";

		String encryptedAccessToken = AesEncryptor.encrypt(smtpServer.getOauthAccessToken());

		Object[] values = {
			encryptedAccessToken,
			DatabaseUtils.toSqlTimestamp(smtpServer.getOauthAccessTokenExpiry()),
			smtpServer.getSmtpServerId()
		};

		dbService.update(sql, values);

		//note that cacheHelper will be null if SmtpServerService instantiated with new rather than @Autowired
		cacheHelper.clearSmtpServers();
	}

}
