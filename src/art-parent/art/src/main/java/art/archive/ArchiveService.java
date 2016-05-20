/*
 * Copyright (C) 2016 Enrico Liboni <eliboni@users.sourceforge.net>
 *
 * This file is part of ART.
 *
 * ART is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation, version 2 of the License.
 *
 * ART is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with ART. If not, see <http://www.gnu.org/licenses/>.
 */
package art.archive;

import art.dbutils.DbService;
import art.job.Job;
import art.job.JobService;
import art.user.User;
import art.user.UserService;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.dbutils.BasicRowProcessor;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.BeanListHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Provides methods for retrieving archives
 * 
 * @author Timothy Anyona
 */
@Service
public class ArchiveService {

	private static final Logger logger = LoggerFactory.getLogger(ArchiveService.class);

	private final DbService dbService;
	private final JobService jobService;
	private final UserService userService;

	@Autowired
	public ArchiveService(DbService dbService, JobService jobService, UserService userService) {
		this.dbService = dbService;
		this.jobService = jobService;
		this.userService = userService;
	}

	public ArchiveService() {
		dbService = new DbService();
		jobService = new JobService();
		userService = new UserService();
	}

	/**
	 * Maps a resultset to an object
	 */
	private class ArchiveMapper extends BasicRowProcessor {

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
			Archive archive = new Archive();

			archive.setArchiveId(rs.getString("ARCHIVE_ID"));
			archive.setFileName(rs.getString("ARCHIVE_FILE_NAME"));
			archive.setStartDate(rs.getTimestamp("START_DATE"));
			archive.setEndDate(rs.getTimestamp("END_DATE"));
			archive.setJobSharedStatus(rs.getString("JOB_SHARED"));

			Job job = jobService.getJob(rs.getInt("JOB_ID"));
			archive.setJob(job);

			User user = userService.getUser(rs.getInt("USER_ID"));
			archive.setUser(user);

			return type.cast(archive);
		}
	}

	/**
	 * Returns the archives that the given user has access to
	 * 
	 * @param userId the user id for the relevant user
	 * @return archives that the given user has access to
	 * @throws SQLException 
	 */
	public List<Archive> getArchives(int userId) throws SQLException {
		logger.debug("Entering getArchives: userId={}", userId);

		//get job archives that user has access to
		String sql = "SELECT AJA.*"
				+ " FROM ART_JOB_ARCHIVES AJA, ART_JOBS AJ"
				+ " WHERE AJA.JOB_ID=AJ.JOB_ID"
				+ " AND AJA.USER_ID=?" //user owns job or individualized output
				+ " UNION"
				+ " SELECT AJA.*"
				+ " FROM ART_JOB_ARCHIVES AJA, ART_JOBS AJ, ART_USER_JOBS AUJ"
				+ " WHERE AJA.JOB_ID=AJ.JOB_ID AND AJ.JOB_ID=AUJ.JOB_ID"
				+ " AND AJA.USER_ID<>? AND AJA.JOB_SHARED='Y' AND AUJ.USER_ID=?" //job shared with user
				+ " UNION"
				+ " SELECT AJA.*"
				+ " FROM ART_JOB_ARCHIVES AJA, ART_JOBS AJ, ART_USER_GROUP_JOBS AUGJ, ART_USER_GROUP_ASSIGNMENT AUGA"
				+ " WHERE AJA.JOB_ID=AJ.JOB_ID AND AJ.JOB_ID=AUGJ.JOB_ID"
				+ " AND AUGJ.USER_GROUP_ID=AUGA.USER_GROUP_ID AND AUGA.USER_ID=?"
				+ " AND AJA.USER_ID<>? AND AJA.JOB_SHARED='Y'"; //job shared with user group

		ResultSetHandler<List<Archive>> h = new BeanListHandler<>(Archive.class, new ArchiveMapper());
		return dbService.query(sql, h, userId, userId, userId, userId, userId);
	}
}
