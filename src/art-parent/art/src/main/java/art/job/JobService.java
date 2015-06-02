package art.job;

import art.dbutils.DbService;
import art.dbutils.DatabaseUtils;
import art.enums.JobType;
import art.report.Report;
import art.servlets.Config;
import art.user.User;
import art.utils.CachedResult;
import art.utils.SchedulerUtils;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.dbutils.BasicRowProcessor;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.BeanHandler;
import org.apache.commons.dbutils.handlers.BeanListHandler;
import org.apache.commons.dbutils.handlers.ScalarHandler;
import org.apache.commons.lang3.StringUtils;
import static org.quartz.JobKey.jobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

/**
 * Class to provide methods related to jobs
 *
 * @author Timothy
 */
@Service
public class JobService {

	private static final Logger logger = LoggerFactory.getLogger(JobService.class);

	private final DbService dbService;

	@Autowired
	public JobService(DbService dbService) {
		this.dbService = dbService;
	}

	public JobService() {
		dbService = new DbService();
	}

	private final String SQL_SELECT_ALL = "SELECT AJ.*,"
			+ " AQ.NAME AS REPORT_NAME, AU.USERNAME"
			+ " FROM ART_JOBS AJ"
			+ " LEFT JOIN ART_QUERIES AQ"
			+ " ON AJ.QUERY_ID=AQ.QUERY_ID"
			+ " LEFT JOIN ART_USERS AU"
			+ " ON AJ.USER_ID=AU.USER_ID";

	/**
	 * Class to map resultset to an object
	 */
	private class JobMapper extends BasicRowProcessor {

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
			Job job = new Job();

			job.setJobId(rs.getInt("JOB_ID"));
			job.setName(rs.getString("JOB_NAME"));

			job.setCreationDate(rs.getTimestamp("CREATION_DATE"));
			job.setUpdateDate(rs.getTimestamp("UPDATE_DATE"));
			job.setCreatedBy(rs.getString("CREATED_BY"));
			job.setUpdatedBy(rs.getString("UPDATED_BY"));

			Report report = new Report();
			report.setReportId(rs.getInt("QUERY_ID"));
			report.setName(rs.getString("REPORT_NAME"));
			job.setReport(report);

			User user = new User();
			user.setUserId(rs.getInt("USER_ID"));
			user.setUsername(rs.getString("USERNAME"));
			job.setUser(user);

			return type.cast(job);
		}
	}

	/**
	 * Get all jobs
	 *
	 * @return list of all jobs, empty list otherwise
	 * @throws SQLException
	 */
	@Cacheable("jobs")
	public List<Job> getAllJobs() throws SQLException {
		logger.debug("Entering getAllJobs");

		ResultSetHandler<List<Job>> h = new BeanListHandler<>(Job.class, new JobMapper());
		return dbService.query(SQL_SELECT_ALL, h);
	}

	/**
	 * Get a job
	 *
	 * @param id
	 * @return populated object if found, null otherwise
	 * @throws SQLException
	 */
	@Cacheable("jobs")
	public Job getJob(int id) throws SQLException {
		logger.debug("Entering getJob: id={}", id);

		return getFreshJob(id);
	}

	/**
	 * Get a job. Data always retrieved from the database and not the cache
	 *
	 * @param id
	 * @return populated object if found, null otherwise
	 * @throws SQLException
	 */
	public Job getFreshJob(int id) throws SQLException {
		logger.debug("Entering getFreshJob: id={}", id);

		String sql = SQL_SELECT_ALL + " WHERE JOB_ID = ? ";
		ResultSetHandler<Job> h = new BeanHandler<>(Job.class, new JobMapper());
		return dbService.query(sql, h, id);
	}

	/**
	 * Delete a job
	 *
	 * @param id
	 * @throws SQLException
	 * @throws org.quartz.SchedulerException
	 */
	@CacheEvict(value = "jobs", allEntries = true)
	public void deleteJob(int id) throws SQLException, SchedulerException {
		logger.debug("Entering deleteJob: id={}", id);

		//get job object. need job details in order to delete cached table for cached result jobs
		Job job = getJob(id);
		if (job == null) {
			logger.warn("Cannot delete job: {}. Job not available.", id);
			return;
		}

		//delete records in quartz tables
		Scheduler scheduler = SchedulerUtils.getScheduler();
		if (scheduler == null) {
			logger.warn("Cannot delete job: {}. Scheduler not available.", id);
			return;
		}
		scheduler.deleteJob(jobKey(String.valueOf(id)));

		// Delete the Cached table if this job is a cache result one
		JobType jobType = job.getJobType();
		if (jobType == JobType.CacheAppend || jobType == JobType.CacheInsert) {
			// Delete
			int targetDatabaseId = Integer.parseInt(job.getOutputFormat());
			Connection connCache = Config.getConnection(targetDatabaseId);
			try {
				String cachedTableName = job.getCachedTableName();
				if (StringUtils.isBlank(cachedTableName)) {
					cachedTableName = job.getReport().getName() + "_J" + job.getJobId();
				}
				CachedResult cr = new CachedResult();
				cr.setTargetConnection(connCache);
				cr.setCachedTableName(cachedTableName);
				cr.drop(); //TODO potential sql injection. drop hardcoded table names only
			} finally {
				DatabaseUtils.close(connCache);
			}
		}

		String sql;

		//delete foreign key records
		sql = "DELETE FROM ART_JOBS_PARAMETERS WHERE JOB_ID=?";
		dbService.update(sql, id);

		sql = "DELETE FROM ART_USER_JOBS WHERE JOB_ID=?";
		dbService.update(sql, id);

		sql = "DELETE FROM ART_USER_GROUP_JOBS WHERE JOB_ID=?";
		dbService.update(sql, id);

		sql = "DELETE FROM ART_JOB_ARCHIVES WHERE JOB_ID=?";
		dbService.update(sql, id);

		//finally delete job
		sql = "DELETE FROM ART_JOBS WHERE JOB_ID=?";
		dbService.update(sql, id);
	}

	/**
	 * Add a new job to the database
	 *
	 * @param job
	 * @return new record id
	 * @throws SQLException
	 */
	@CacheEvict(value = "jobs", allEntries = true)
	public synchronized int addJob(Job job) throws SQLException {
		logger.debug("Entering addJob: job={}", job);

		//generate new id
		String sql = "SELECT MAX(JOB_ID) FROM ART_JOBS";
		ResultSetHandler<Integer> h = new ScalarHandler<>();
		Integer maxId = dbService.query(sql, h);
		logger.debug("maxId={}", maxId);

		int newId;
		if (maxId == null || maxId < 0) {
			//no records in the table, or only hardcoded records
			newId = 1;
		} else {
			newId = maxId + 1;
		}
		logger.debug("newId={}", newId);

		sql = "INSERT INTO ART_JOBS"
				+ " (JOB_ID, JOB_NAME, QUERY_ID, USER_ID, USERNAME, OUTPUT_FORMAT,"
				+ " JOB_TYPE, JOB_MINUTE, JOB_HOUR, JOB_DAY, JOB_WEEKDAY, JOB_MONTH,"
				+ " MAIL_TOS, MAIL_FROM, MAIL_CC, MAIL_BCC, SUBJECT, MESSAGE,"
				+ " CACHED_TABLE_NAME, START_DATE, END_DATE, NEXT_RUN_DATE,"
				+ " CREATION_DATE)"
				+ " VALUES(" + StringUtils.repeat("?", ",", 6) + ")";

		Object[] values = {
			newId,
			job.getName(),
			DatabaseUtils.getCurrentTimeAsSqlTimestamp()
		};

		dbService.update(sql, values);

		return newId;
	}

	/**
	 * Update an existing job
	 *
	 * @param job
	 * @throws SQLException
	 */
	@CacheEvict(value = "jobs", allEntries = true)
	public void updateJob(Job job) throws SQLException {
		logger.debug("Entering updateJob: job={}", job);

		String sql = "UPDATE ART_JOBS SET NAME=?, DESCRIPTION=?,"
				+ " DEFAULT_QUERY_GROUP=?, START_QUERY=?, UPDATE_DATE=?"
				+ " WHERE JOB_ID=?";

		Object[] values = {
			job.getName(),
			DatabaseUtils.getCurrentTimeAsSqlTimestamp(),
			job.getJobId()
		};

		dbService.update(sql, values);
	}

	/**
	 * Get all the jobs a user has access to. Both the jobs the user owns and
	 * jobs shared with him
	 *
	 * @param userId
	 * @return all the jobs a user has access to
	 * @throws java.sql.SQLException
	 */
	@Cacheable("jobs")
	public List<Job> getJobs(int userId) throws SQLException {
		List<Job> jobs = new ArrayList<>();

		jobs.addAll(getOwnedJobs(userId));
		jobs.addAll(getSharedJobs(userId));

		return jobs;
	}

	/**
	 * Get the jobs a user owns
	 *
	 * @param userId
	 * @return list of jobs that the user owns, empty list if none
	 * @throws java.sql.SQLException
	 */
	public List<Job> getOwnedJobs(int userId) throws SQLException {
		logger.debug("Entering getOwnedJobs: userId={}", userId);

		String sql = SQL_SELECT_ALL + " WHERE AJ.USER_ID=?";
		ResultSetHandler<List<Job>> h = new BeanListHandler<>(Job.class, new JobMapper());
		return dbService.query(sql, h);
	}

	/**
	 * Get the shared jobs a user has access to
	 *
	 * @param userId
	 * @return the shared jobs a user has access to
	 * @throws java.sql.SQLException
	 */
	public List<SharedJob> getSharedJobs(int userId) throws SQLException {
		logger.debug("Entering getSharedJobs: userId={}", userId);

		List<SharedJob> jobs = new ArrayList<>();
		String sql;

		//get shared jobs user has access to via job membership. 
		//non-split jobs. no entries for them in the art_user_jobs table
		sql = SQL_SELECT_ALL + " INNER JOIN ART_USER_GROUP_JOBS AUGJ"
				+ " ON AJ.JOB_ID=AUGJ.JOB_ID"
				+ " WHERE AJ.USER_ID <> ? AND EXISTS "
				+ " (SELECT * FROM ART_USER_GROUP_ASSIGNMENT AUGA WHERE AUGA.USER_ID = ? "
				+ " AND AUGA.JOB_ID=AUGJ.JOB_ID)";
		ResultSetHandler<List<SharedJob>> h = new BeanListHandler<>(SharedJob.class, new JobMapper());
		jobs.addAll(dbService.query(sql, h, userId, userId));

		//get shared jobs user has direct access to, but doesn't own. both split and non-split jobs
		//stored in the art_user_jobs table
		sql=SQL_SELECT_ALL + " INNER JOIN ART_USER_JOBS AUJ"
				+ " ON AJ.JOB_ID=AUJ.JOB_ID"
				+ " WHERE AJ.USER_ID<>? AND AUJ.USER_ID=?";
		jobs.addAll(dbService.query(sql, h, userId));
		
		sql = "SELECT aq.NAME AS QUERY_NAME, aj.JOB_NAME, aj.JOB_ID, aj.JOB_TYPE,"
						+ " aq.USES_RULES, aj.ALLOW_SPLITTING "
						+ " , aj.LAST_START_DATE , aj.LAST_FILE_NAME , aj.NEXT_RUN_DATE,"
						+ " aj.CACHED_TABLE_NAME,auj.LAST_FILE_NAME AS SHARED_FILE_NAME, "
						+ " auj.LAST_START_DATE AS SHARED_START_DATE,aj.LAST_END_DATE, "
						+ " aj.OUTPUT_FORMAT, aj.MAIL_TOS, aj.SUBJECT, aj.MESSAGE "
						+ " ,aj.JOB_MINUTE, aj.JOB_HOUR, aj.JOB_DAY, aj.JOB_WEEKDAY,"
						+ " aj.JOB_MONTH, auj.LAST_END_DATE AS SHARED_END_DATE "
						+ " FROM ART_JOBS aj, ART_QUERIES aq, ART_USER_JOBS auj "
						+ " WHERE aq.QUERY_ID = aj.QUERY_ID AND aj.JOB_ID=auj.JOB_ID "
						+ " AND auj.USERNAME = ? AND aj.USERNAME <> ?";
		
		return jobs;
	}
	
	/**
	 * Get jobs that have not been migrated to the quartz scheduling system
	 *
	 * @return jobs that have not been migrated to the quartz scheduling system
	 * @throws java.sql.SQLException
	 */
	public List<Job> getNonQuartzJobs() throws SQLException {
		logger.debug("Entering getNonQuartzJobs.");

		String sql = SQL_SELECT_ALL + " WHERE AJ.MIGRATED_TO_QUARTZ='N'";
		ResultSetHandler<List<Job>> h = new BeanListHandler<>(Job.class, new JobMapper());
		return dbService.query(sql, h);
	}

}
