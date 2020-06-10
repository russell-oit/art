/*
 * ART. A Reporting Tool.
 * Copyright (C) 2020 Enrico Liboni <eliboni@users.sf.net>
 *
 * This program is free software: you can redistribute it and/or modify
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
package art.pipeline;

import art.dbutils.DatabaseUtils;
import art.dbutils.DbService;
import art.user.User;
import art.utils.ArtUtils;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.dbutils.BasicRowProcessor;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.BeanHandler;
import org.apache.commons.dbutils.handlers.BeanListHandler;
import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

/**
 * Provides methods for retrieving, adding, updating and deleting pipelines
 *
 * @author Timothy Anyona
 */
@Service
public class PipelineService {

	private static final Logger logger = LoggerFactory.getLogger(PipelineService.class);

	private final DbService dbService;

	@Autowired
	public PipelineService(DbService dbService) {
		this.dbService = dbService;
	}

	public PipelineService() {
		dbService = new DbService();
	}

	private final String SQL_SELECT_ALL = "SELECT * FROM ART_PIPELINES";

	/**
	 * Maps a resultset to an object
	 */
	private class PipelineMapper extends BasicRowProcessor {

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
			Pipeline pipeline = new Pipeline();

			pipeline.setPipelineId(rs.getInt("PIPELINE_ID"));
			pipeline.setName(rs.getString("NAME"));
			pipeline.setDescription(rs.getString("DESCRIPTION"));
			pipeline.setSerial(rs.getString("SERIAL"));
			pipeline.setContinueOnError(rs.getBoolean("CONTINUE_ON_ERROR"));
			pipeline.setCreationDate(rs.getTimestamp("CREATION_DATE"));
			pipeline.setCreatedBy(rs.getString("CREATED_BY"));
			pipeline.setUpdateDate(rs.getTimestamp("UPDATE_DATE"));
			pipeline.setUpdatedBy(rs.getString("UPDATED_BY"));

			return type.cast(pipeline);
		}
	}

	/**
	 * Returns all pipelines
	 *
	 * @return all pipelines
	 * @throws SQLException
	 */
	@Cacheable("pipelines")
	public List<Pipeline> getAllPipelines() throws SQLException {
		logger.debug("Entering getAllPipelines");

		ResultSetHandler<List<Pipeline>> h = new BeanListHandler<>(Pipeline.class, new PipelineMapper());
		return dbService.query(SQL_SELECT_ALL, h);
	}

	/**
	 * Returns pipelines with given ids
	 *
	 * @param ids comma separated string of the pipeline ids to retrieve
	 * @return pipelines with given ids
	 * @throws SQLException
	 */
	public List<Pipeline> getPipelines(String ids) throws SQLException {
		logger.debug("Entering getPipelines: ids='{}'", ids);

		Object[] idsArray = ArtUtils.idsToObjectArray(ids);

		if (idsArray.length == 0) {
			return new ArrayList<>();
		}

		String sql = SQL_SELECT_ALL
				+ " WHERE PIPELINE_ID IN(" + StringUtils.repeat("?", ",", idsArray.length) + ")";

		ResultSetHandler<List<Pipeline>> h = new BeanListHandler<>(Pipeline.class, new PipelineMapper());
		return dbService.query(sql, h, idsArray);
	}

	/**
	 * Returns a pipeline with the given id
	 *
	 * @param id the pipeline id
	 * @return pipeline if found, null otherwise
	 * @throws SQLException
	 */
	@Cacheable("pipelines")
	public Pipeline getPipeline(int id) throws SQLException {
		logger.debug("Entering getPipeline: id={}", id);

		String sql = SQL_SELECT_ALL + " WHERE PIPELINE_ID=?";
		ResultSetHandler<Pipeline> h = new BeanHandler<>(Pipeline.class, new PipelineMapper());
		return dbService.query(sql, h, id);
	}

	/**
	 * Returns a pipeline with the given name
	 *
	 * @param name the pipeline name
	 * @return pipeline if found, null otherwise
	 * @throws SQLException
	 */
	@Cacheable("pipelines")
	public Pipeline getPipeline(String name) throws SQLException {
		logger.debug("Entering getPipeline: name='{}'", name);

		String sql = SQL_SELECT_ALL + " WHERE NAME=?";
		ResultSetHandler<Pipeline> h = new BeanHandler<>(Pipeline.class, new PipelineMapper());
		return dbService.query(sql, h, name);
	}

	/**
	 * Deletes a pipeline
	 *
	 * @param id the pipeline id
	 * @throws SQLException
	 */
	@CacheEvict(value = "pipelines", allEntries = true)
	public void deletePipeline(int id) throws SQLException {
		logger.debug("Entering deletePipeline: id={}", id);

		String sql = "DELETE FROM ART_PIPELINES WHERE PIPELINE_ID=?";
		dbService.update(sql, id);
	}

	/**
	 * Deletes multiple pipelines
	 *
	 * @param ids the ids of the pipelines to delete
	 * @throws SQLException
	 */
	@CacheEvict(value = "pipelines", allEntries = true)
	public void deletePipelines(Integer[] ids) throws SQLException {
		logger.debug("Entering deletePipelines: ids={}", (Object) ids);

		for (Integer id : ids) {
			deletePipeline(id);
		}
	}

	/**
	 * Adds a new pipeline
	 *
	 * @param pipeline the pipeline to add
	 * @param actionUser the user who is performing the action
	 * @return new record id
	 * @throws SQLException
	 */
	@CacheEvict(value = "pipelines", allEntries = true)
	public synchronized int addPipeline(Pipeline pipeline, User actionUser) throws SQLException {
		logger.debug("Entering addPipeline: pipeline={}, actionUser={}", pipeline, actionUser);

		//generate new id
		String sql = "SELECT MAX(PIPELINE_ID) FROM ART_PIPELINES";
		int newId = dbService.getNewRecordId(sql);

		savePipeline(pipeline, newId, actionUser);

		return newId;
	}

	/**
	 * Updates an existing pipeline
	 *
	 * @param pipeline the updated pipeline
	 * @param actionUser the user who is performing the action
	 * @throws SQLException
	 */
	@CacheEvict(value = "pipelines", allEntries = true)
	public void updatePipeline(Pipeline pipeline, User actionUser) throws SQLException {
		Connection conn = null;
		updatePipeline(pipeline, actionUser, conn);
	}

	/**
	 * Updates an existing pipeline
	 *
	 * @param pipeline the updated pipeline
	 * @param actionUser the user who is performing the action
	 * @param conn the connection to use
	 * @throws SQLException
	 */
	@CacheEvict(value = "pipelines", allEntries = true)
	public void updatePipeline(Pipeline pipeline, User actionUser, Connection conn) throws SQLException {
		logger.debug("Entering updatePipeline: pipeline={}, actionUser={}", pipeline, actionUser);

		Integer newRecordId = null;
		savePipeline(pipeline, newRecordId, actionUser, conn);
	}

	/**
	 * Imports pipeline records
	 *
	 * @param pipelines the list of pipelines to import
	 * @param actionUser the user who is performing the import
	 * @param conn the connection to use
	 * @param overwrite whether to overwrite existing records
	 * @throws SQLException
	 */
	@CacheEvict(value = "pipelines", allEntries = true)
	public void importPipelines(List<Pipeline> pipelines, User actionUser,
			Connection conn, boolean overwrite) throws SQLException {

		logger.debug("Entering importPipelines: actionUser={}, overwrite={}",
				actionUser, overwrite);

		boolean originalAutoCommit = true;

		try {
			String sql = "SELECT MAX(PIPELINE_ID) FROM ART_PIPELINES";
			int id = dbService.getMaxRecordId(conn, sql);

			List<Pipeline> currentPipelines = new ArrayList<>();
			if (overwrite) {
				currentPipelines = getAllPipelines();
			}

			originalAutoCommit = conn.getAutoCommit();
			conn.setAutoCommit(false);

			for (Pipeline pipeline : pipelines) {
				String pipelineName = pipeline.getName();
				boolean update = false;
				if (overwrite) {
					Pipeline existingPipeline = currentPipelines.stream()
							.filter(d -> StringUtils.equals(pipelineName, d.getName()))
							.findFirst()
							.orElse(null);
					if (existingPipeline != null) {
						update = true;
						pipeline.setPipelineId(existingPipeline.getPipelineId());
					}
				}

				Integer newRecordId;
				if (update) {
					newRecordId = null;
				} else {
					id++;
					newRecordId = id;
				}
				savePipeline(pipeline, newRecordId, actionUser, conn);
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
	 * Saves a pipeline
	 *
	 * @param pipeline the pipeline to save
	 * @param newRecordId id of the new record or null if editing an existing
	 * record
	 * @param actionUser the user who is performing the save
	 * @throws SQLException
	 */
	private void savePipeline(Pipeline pipeline, Integer newRecordId,
			User actionUser) throws SQLException {

		Connection conn = null;
		savePipeline(pipeline, newRecordId, actionUser, conn);
	}

	/**
	 * Saves a pipeline
	 *
	 * @param pipeline the pipeline to save
	 * @param newRecordId id of the new record or null if editing an existing
	 * record
	 * @param actionUser the user who is performing the action
	 * @param conn the connection to use. if null, the art database will be used
	 * @throws SQLException
	 */
	@CacheEvict(value = "pipelines", allEntries = true)
	public void savePipeline(Pipeline pipeline, Integer newRecordId,
			User actionUser, Connection conn) throws SQLException {

		logger.debug("Entering savePipeline: pipeline={}, newRecordId={},actionUser={}",
				pipeline, newRecordId, actionUser);

		int affectedRows;

		boolean newRecord = false;
		if (newRecordId != null) {
			newRecord = true;
		}

		if (newRecord) {
			String sql = "INSERT INTO ART_PIPELINES"
					+ " (PIPELINE_ID, NAME, DESCRIPTION, SERIAL,"
					+ " CONTINUE_ON_ERROR,"
					+ " CREATION_DATE, CREATED_BY)"
					+ " VALUES(" + StringUtils.repeat("?", ",", 7) + ")";

			Object[] values = {
				newRecordId,
				pipeline.getName(),
				pipeline.getDescription(),
				pipeline.getSerial(),
				BooleanUtils.toInteger(pipeline.isContinueOnError()),
				DatabaseUtils.getCurrentTimeAsSqlTimestamp(),
				actionUser.getUsername()
			};

			affectedRows = dbService.update(conn, sql, values);
		} else {
			String sql = "UPDATE ART_PIPELINES SET NAME=?, DESCRIPTION=?,"
					+ "	SERIAL=?, CONTINUE_ON_ERROR=?,"
					+ " UPDATE_DATE=?, UPDATED_BY=?"
					+ " WHERE PIPELINE_ID=?";

			Object[] values = {
				pipeline.getName(),
				pipeline.getDescription(),
				pipeline.getSerial(),
				BooleanUtils.toInteger(pipeline.isContinueOnError()),
				DatabaseUtils.getCurrentTimeAsSqlTimestamp(),
				actionUser.getUsername(),
				pipeline.getPipelineId()
			};

			affectedRows = dbService.update(conn, sql, values);
		}

		if (newRecordId != null) {
			pipeline.setPipelineId(newRecordId);
		}

		logger.debug("affectedRows={}", affectedRows);

		if (affectedRows != 1) {
			logger.warn("Problem with save. affectedRows={}, newRecord={}, pipeline={}",
					affectedRows, newRecord, pipeline);
		}
	}

}
