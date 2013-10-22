package art.job;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Class to provide methods related to jobs
 *
 * @author Timothy
 */
@Service
public class JobService {

	final static Logger logger = LoggerFactory.getLogger(JobService.class);
	private final JobRepository jobDao;

	@Autowired
	public JobService(JobRepository jobDao) {
		this.jobDao = jobDao;
	}

	/**
	 * Get all the jobs a user has access to. Both the jobs the user owns and
	 * jobs shared with him
	 *
	 * @param username
	 * @return all the jobs a user has access to
	 */
	public List<Job> getAllJobs(String username) {
		return jobDao.getAllJobs(username);
	}
}
