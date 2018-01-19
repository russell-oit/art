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
package art.encryptor;

import art.dbutils.DatabaseUtils;
import art.dbutils.DbService;
import art.encryption.AesEncryptor;
import art.enums.EncryptorType;
import art.user.User;
import art.utils.ActionResult;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.apache.commons.dbutils.BasicRowProcessor;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.BeanHandler;
import org.apache.commons.dbutils.handlers.BeanListHandler;
import org.apache.commons.dbutils.handlers.ColumnListHandler;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

/**
 * Provides methods for adding, deleting, retrieving and updating encryptor
 * configurations
 *
 * @author Timothy Anyona
 */
@Service
public class EncryptorService {

	private static final Logger logger = LoggerFactory.getLogger(EncryptorService.class);

	private final DbService dbService;

	@Autowired
	public EncryptorService(DbService dbService) {
		this.dbService = dbService;
	}

	public EncryptorService() {
		dbService = new DbService();
	}

	private final String SQL_SELECT_ALL = "SELECT * FROM ART_ENCRYPTORS";

	/**
	 * Maps a resultset to an object
	 */
	private class EncryptorMapper extends BasicRowProcessor {

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
			Encryptor encryptor = new Encryptor();

			encryptor.setEncryptorId(rs.getInt("ENCRYPTOR_ID"));
			encryptor.setName(rs.getString("NAME"));
			encryptor.setDescription(rs.getString("DESCRIPTION"));
			encryptor.setActive(rs.getBoolean("ACTIVE"));
			encryptor.setEncryptorType(EncryptorType.toEnum(rs.getString("ENCRYPTOR_TYPE")));
			encryptor.setAesCryptPassword(rs.getString("AESCRYPT_PASSWORD"));
			encryptor.setOpenPgpPublicKeyFile(rs.getString("OPENPGP_PUBLIC_KEY_FILE"));
			encryptor.setOpenPgpPublicKeyString(rs.getString("OPENPGP_PUBLIC_KEY_STRING"));
			encryptor.setOpenPgpSigningKeyFile(rs.getString("OPENPGP_SIGNING_KEY_FILE"));
			encryptor.setOpenPgpSigningKeyPassphrase(rs.getString("OPENPGP_SIGNING_KEY_PASSPHRASE"));
			encryptor.setOpenPassword(rs.getString("OPEN_PASSWORD"));
			encryptor.setModifyPassword(rs.getString("MODIFY_PASSWORD"));
			encryptor.setCreationDate(rs.getTimestamp("CREATION_DATE"));
			encryptor.setUpdateDate(rs.getTimestamp("UPDATE_DATE"));
			encryptor.setCreatedBy(rs.getString("CREATED_BY"));
			encryptor.setUpdatedBy(rs.getString("UPDATED_BY"));

			//decrypt passwords
			String aesCryptPassword = encryptor.getAesCryptPassword();
			aesCryptPassword = AesEncryptor.decrypt(aesCryptPassword);
			encryptor.setAesCryptPassword(aesCryptPassword);

			String openPgpSigningKeyPassphrase = encryptor.getOpenPgpSigningKeyPassphrase();
			openPgpSigningKeyPassphrase = AesEncryptor.decrypt(openPgpSigningKeyPassphrase);
			encryptor.setOpenPgpSigningKeyPassphrase(openPgpSigningKeyPassphrase);

			String openPassword = encryptor.getOpenPassword();
			openPassword = AesEncryptor.decrypt(openPassword);
			encryptor.setOpenPassword(openPassword);

			String modifyPassword = encryptor.getModifyPassword();
			modifyPassword = AesEncryptor.decrypt(modifyPassword);
			encryptor.setModifyPassword(modifyPassword);

			return type.cast(encryptor);
		}
	}

	/**
	 * Returns all encryptors
	 *
	 * @return all encryptors
	 * @throws SQLException
	 */
	@Cacheable("encryptors")
	public List<Encryptor> getAllEncryptors() throws SQLException {
		logger.debug("Entering getAllEncryptors");

		ResultSetHandler<List<Encryptor>> h = new BeanListHandler<>(Encryptor.class, new EncryptorMapper());
		return dbService.query(SQL_SELECT_ALL, h);
	}

	/**
	 * Returns the encryptor with the given id
	 *
	 * @param id the encryptor id
	 * @return the encryptor if found, null otherwise
	 * @throws SQLException
	 */
	@Cacheable("encryptors")
	public Encryptor getEncryptor(int id) throws SQLException {
		logger.debug("Entering getEncryptor: id={}", id);

		String sql = SQL_SELECT_ALL + " WHERE ENCRYPTOR_ID=?";
		ResultSetHandler<Encryptor> h = new BeanHandler<>(Encryptor.class, new EncryptorMapper());
		return dbService.query(sql, h, id);
	}

	/**
	 * Deletes the encryptor with the given id
	 *
	 * @param id the encryptor id
	 * @return ActionResult. if not successful, data contains a list of linked
	 * reports which prevented the encryptor from being deleted
	 * @throws SQLException
	 */
	@CacheEvict(value = {"encryptors", "reports"}, allEntries = true)
	public ActionResult deleteEncryptor(int id) throws SQLException {
		logger.debug("Entering deleteEncryptor: id={}", id);

		ActionResult result = new ActionResult();

		//don't delete if important linked records exist
		List<String> linkedJobs = getLinkedReports(id);
		if (!linkedJobs.isEmpty()) {
			result.setData(linkedJobs);
			return result;
		}

		String sql;

		sql = "DELETE FROM ART_ENCRYPTORS WHERE ENCRYPTOR_ID=?";
		dbService.update(sql, id);

		result.setSuccess(true);

		return result;
	}

	/**
	 * Deletes the encryptors with the given ids
	 *
	 * @param ids the encryptor ids
	 * @return ActionResult. if not successful, data contains details of
	 * encrptors which weren't deleted
	 * @throws SQLException
	 */
	@CacheEvict(value = {"encryptors", "reports"}, allEntries = true)
	public ActionResult deleteEncryptors(Integer[] ids) throws SQLException {
		logger.debug("Entering deleteEncryptors: ids={}", (Object) ids);

		ActionResult result = new ActionResult();
		List<String> nonDeletedRecords = new ArrayList<>();

		for (Integer id : ids) {
			ActionResult deleteResult = deleteEncryptor(id);
			if (!deleteResult.isSuccess()) {
				@SuppressWarnings("unchecked")
				List<String> linkedReports = (List<String>) deleteResult.getData();
				String value = String.valueOf(id) + " - " + StringUtils.join(linkedReports, ", ");
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
	 * Adds a new encryptor
	 *
	 * @param encryptor the encryptor
	 * @param actionUser the user who is performing the action
	 * @return new record id
	 * @throws SQLException
	 */
	@CacheEvict(value = "encryptors", allEntries = true)
	public synchronized int addEncryptor(Encryptor encryptor, User actionUser) throws SQLException {
		logger.debug("Entering addEncryptor: encryptor={}, actionUser={}", encryptor, actionUser);

		//generate new id
		String sql = "SELECT MAX(ENCRYPTOR_ID) FROM ART_ENCRYPTORS";
		int newId = dbService.getNewRecordId(sql);

		saveEncryptor(encryptor, newId, actionUser);

		return newId;
	}

	/**
	 * Updates an encryptor
	 *
	 * @param encryptor the updated encryptor
	 * @param actionUser the user who is performing the action
	 * @throws SQLException
	 */
	@CacheEvict(value = {"encryptors", "reports"}, allEntries = true)
	public void updateEncryptor(Encryptor encryptor, User actionUser) throws SQLException {
		logger.debug("Entering updateEncryptor: encryptor={}, actionUser={}", encryptor, actionUser);

		Integer newRecordId = null;
		saveEncryptor(encryptor, newRecordId, actionUser);
	}

	/**
	 * Saves an encryptor
	 *
	 * @param encryptor the encryptor
	 * @param newRecordId id of the new record or null if editing an existing
	 * record
	 * @param actionUser the user who is performing the action
	 * @throws SQLException
	 */
	private void saveEncryptor(Encryptor encryptor, Integer newRecordId, User actionUser) throws SQLException {
		logger.debug("Entering saveEncryptor: encryptor={}, newRecordId={}, actionUser={}",
				encryptor, newRecordId, actionUser);

		int affectedRows;

		boolean newRecord = false;
		if (newRecordId != null) {
			newRecord = true;
		}

		if (newRecord) {
			String sql = "INSERT INTO ART_ENCRYPTORS"
					+ " (ENCRYPTOR_ID, NAME, DESCRIPTION, ACTIVE, ENCRYPTOR_TYPE,"
					+ " AESCRYPT_PASSWORD, OPENPGP_PUBLIC_KEY_FILE,"
					+ " OPENPGP_PUBLIC_KEY_STRING, OPENPGP_SIGNING_KEY_FILE,"
					+ " OPENPGP_SIGNING_KEY_PASSPHRASE, OPEN_PASSWORD,"
					+ " MODIFY_PASSWORD,"
					+ " CREATION_DATE, CREATED_BY)"
					+ " VALUES(" + StringUtils.repeat("?", ",", 14) + ")";

			Object[] values = {
				newRecordId,
				encryptor.getName(),
				encryptor.getDescription(),
				BooleanUtils.toInteger(encryptor.isActive()),
				encryptor.getEncryptorType().getValue(),
				encryptor.getAesCryptPassword(),
				encryptor.getOpenPgpPublicKeyFile(),
				encryptor.getOpenPgpPublicKeyString(),
				encryptor.getOpenPgpSigningKeyFile(),
				encryptor.getOpenPgpSigningKeyPassphrase(),
				encryptor.getOpenPassword(),
				encryptor.getModifyPassword(),
				DatabaseUtils.getCurrentTimeAsSqlTimestamp(),
				actionUser.getUsername()
			};

			affectedRows = dbService.update(sql, values);
		} else {
			String sql = "UPDATE ART_ENCRYPTORS SET NAME=?, DESCRIPTION=?,"
					+ " ACTIVE=?, ENCRYPTOR_TYPE=?, AESCRYPT_PASSWORD=?,"
					+ " OPENPGP_PUBLIC_KEY_FILE=?, OPENPGP_PUBLIC_KEY_STRING=?,"
					+ " OPENPGP_SIGNING_KEY_FILE=?, OPENPGP_SIGNING_KEY_PASSPHRASE=?,"
					+ " OPEN_PASSWORD=?, MODIFY_PASSWORD=?,"
					+ " UPDATE_DATE=?, UPDATED_BY=?"
					+ " WHERE ENCRYPTOR_ID=?";

			Object[] values = {
				encryptor.getName(),
				encryptor.getDescription(),
				BooleanUtils.toInteger(encryptor.isActive()),
				encryptor.getEncryptorType().getValue(),
				encryptor.getAesCryptPassword(),
				encryptor.getOpenPgpPublicKeyFile(),
				encryptor.getOpenPgpPublicKeyString(),
				encryptor.getOpenPgpSigningKeyFile(),
				encryptor.getOpenPgpSigningKeyPassphrase(),
				encryptor.getOpenPassword(),
				encryptor.getModifyPassword(),
				DatabaseUtils.getCurrentTimeAsSqlTimestamp(),
				actionUser.getUsername(),
				encryptor.getEncryptorId()
			};

			affectedRows = dbService.update(sql, values);
		}

		if (newRecordId != null) {
			encryptor.setEncryptorId(newRecordId);
		}

		logger.debug("affectedRows={}", affectedRows);

		if (affectedRows != 1) {
			logger.warn("Problem with save. affectedRows={}, newRecord={}, encryptor={}",
					affectedRows, newRecord, encryptor);
		}
	}

	/**
	 * Updates multiple encryptors
	 *
	 * @param multipleEncryptorEdit the multiple encryptor edit object
	 * @param actionUser the user who is performing the edit
	 * @throws SQLException
	 */
	@CacheEvict(value = {"encryptors", "reports"}, allEntries = true)
	public void updateEncryptors(MultipleEncryptorEdit multipleEncryptorEdit, User actionUser)
			throws SQLException {

		logger.debug("Entering updateEncryptors: multipleEncryptorEdit={}, actionUser={}",
				multipleEncryptorEdit, actionUser);

		String sql;

		String[] ids = StringUtils.split(multipleEncryptorEdit.getIds(), ",");
		if (!multipleEncryptorEdit.isActiveUnchanged()) {
			sql = "UPDATE ART_ENCRYPTORS SET ACTIVE=?, UPDATED_BY=?, UPDATE_DATE=?"
					+ " WHERE ENCRYPTOR_ID IN(" + StringUtils.repeat("?", ",", ids.length) + ")";

			List<Object> valuesList = new ArrayList<>();
			valuesList.add(BooleanUtils.toInteger(multipleEncryptorEdit.isActive()));
			valuesList.add(actionUser.getUsername());
			valuesList.add(DatabaseUtils.getCurrentTimeAsSqlTimestamp());
			valuesList.addAll(Arrays.asList(ids));

			Object[] valuesArray = valuesList.toArray(new Object[valuesList.size()]);

			dbService.update(sql, valuesArray);
		}
	}

	/**
	 * Returns reports that use a given encryptor
	 *
	 * @param encryptorId the encryptor id
	 * @return linked report names
	 * @throws SQLException
	 */
	public List<String> getLinkedReports(int encryptorId) throws SQLException {
		logger.debug("Entering getLinkedReports: encryptorId={}", encryptorId);

		String sql = "SELECT NAME"
				+ " FROM ART_QUERIES"
				+ " WHERE ENCRYPTOR_ID=?";

		ResultSetHandler<List<String>> h = new ColumnListHandler<>(1);
		return dbService.query(sql, h, encryptorId);
	}
}
