package art.job;

import art.servlets.ArtConfig;
import art.utils.DbUtils;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class to provide data access methods for jobs
 *
 * @author Timothy Anyona
 */
public class JobDao {

	final static Logger logger = LoggerFactory.getLogger(JobDao.class);

	/**
	 * Get the shared jobs a user has access to
	 *
	 * @return the shared jobs a user has access to
	 */
	public static List<SharedJob> getSharedJobs(String username) {
		List<SharedJob> jobs = new ArrayList<SharedJob>();

		Connection conn = null;

		try {
			conn = ArtConfig.getConnection();
			String sql;
			PreparedStatement ps = null;
			ResultSet rs = null;

			//get shared jobs user has access to via group membership. 
			//non-split jobs. no entries for them in the art_user_jobs table
			try {
				sql = "SELECT aq.NAME AS QUERY_NAME, aj.JOB_NAME, aj.JOB_ID, aj.JOB_TYPE,"
						+ " aq.USES_RULES, aj.ALLOW_SPLITTING "
						+ " , aj.LAST_START_DATE , aj.LAST_FILE_NAME , aj.NEXT_RUN_DATE,"
						+ " aj.CACHED_TABLE_NAME,aj.LAST_END_DATE, aj.OUTPUT_FORMAT, "
						+ " aj.MAIL_TOS, aj.SUBJECT, aj.MESSAGE,aj.JOB_MINUTE,  "
						+ " aj.JOB_HOUR, aj.JOB_DAY, aj.JOB_WEEKDAY, aj.JOB_MONTH "
						+ " FROM ART_JOBS aj, ART_QUERIES aq, ART_USER_GROUP_JOBS AUGJ "
						+ " WHERE aq.QUERY_ID = aj.QUERY_ID AND aj.JOB_ID = AUGJ.JOB_ID "
						+ " AND aj.USERNAME <> ? AND EXISTS "
						+ " (SELECT * FROM ART_USER_GROUP_ASSIGNMENT AUGA WHERE AUGA.USERNAME = ? "
						+ " AND AUGA.USER_GROUP_ID=AUGJ.USER_GROUP_ID)";

				ps = conn.prepareStatement(sql);
				ps.setString(1, username);
				ps.setString(2, username);
				rs = ps.executeQuery();

				while (rs.next()) {
					SharedJob job = new SharedJob();
					job.setQueryName(rs.getString("QUERY_NAME"));
					job.setJobName(rs.getString("JOB_NAME"));
					job.setJobId(rs.getInt("JOB_ID"));
					job.setJobType(rs.getInt("JOB_TYPE"));
					job.setUsesRules(rs.getString("USES_RULES"));
					job.setAllowSplitting(rs.getString("ALLOW_SPLITTING"));
					job.setLastStartDate(rs.getTimestamp("LAST_START_DATE"));
					job.setLastFileName(rs.getString("LAST_FILE_NAME"));
					job.setNextRunDate(rs.getTimestamp("NEXT_RUN_DATE"));
//					job.setCachedTableName(rs.getString("CACHED_TABLE_NAME"));
					job.setLastEndDate(rs.getTimestamp("LAST_END_DATE"));
//					job.setOutputFormat(rs.getString("OUTPUT_FORMAT"));
//					job.setTos(rs.getString("MAIL_TOS"));
//					job.setSubject(rs.getString("SUBJECT"));
//					job.setMessage(rs.getString("MESSAGE"));
//					job.setMinute(rs.getString("JOB_MINUTE"));
//					job.setHour(rs.getString("JOB_HOUR"));
//					job.setDay(rs.getString("JOB_DAY"));
//					job.setWeekday(rs.getString("JOB_WEEKDAY"));
//					job.setMonth(rs.getString("JOB_MONTH"));

					jobs.add(job);
				}
			} finally {
				DbUtils.close(rs, ps);
			}

			//get jobs user has direct access to. both split and non-split jobs
			try {
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

				ps = conn.prepareStatement(sql);
				ps.setString(1, username);
				ps.setString(2, username);
				rs = ps.executeQuery();

				while (rs.next()) {
					SharedJob job = new SharedJob();
					job.setQueryName(rs.getString("QUERY_NAME"));
					job.setJobName(rs.getString("JOB_NAME"));
					job.setJobId(rs.getInt("JOB_ID"));
					job.setJobType(rs.getInt("JOB_TYPE"));
					job.setUsesRules(rs.getString("USES_RULES"));
					job.setAllowSplitting(rs.getString("ALLOW_SPLITTING"));
					job.setLastStartDate(rs.getTimestamp("LAST_START_DATE"));
					job.setLastFileName(rs.getString("LAST_FILE_NAME"));
					job.setNextRunDate(rs.getTimestamp("NEXT_RUN_DATE"));
//					job.setCachedTableName(rs.getString("CACHED_TABLE_NAME"));
					job.setSharedLastFileName(rs.getString("SHARED_FILE_NAME"));
					job.setSharedLastStartDate(rs.getTimestamp("SHARED_START_DATE"));
					job.setLastEndDate(rs.getTimestamp("LAST_END_DATE"));
					job.setSharedLastEndDate(rs.getTimestamp("SHARED_END_DATE"));
//					job.setOutputFormat(rs.getString("OUTPUT_FORMAT"));
//					job.setTos(rs.getString("MAIL_TOS"));
//					job.setSubject(rs.getString("SUBJECT"));
//					job.setMessage(rs.getString("MESSAGE"));
//					job.setMinute(rs.getString("JOB_MINUTE"));
//					job.setHour(rs.getString("JOB_HOUR"));
//					job.setDay(rs.getString("JOB_DAY"));
//					job.setWeekday(rs.getString("JOB_WEEKDAY"));
//					job.setMonth(rs.getString("JOB_MONTH"));
//
//					job.buildParametersDisplayString();

					jobs.add(job);
				}
			} finally {
				DbUtils.close(rs, ps);
			}
		} catch (Exception e) {
			logger.error("Error", e);
		} finally {
			DbUtils.closeConnection(conn);
		}

		return jobs;
	}
}
