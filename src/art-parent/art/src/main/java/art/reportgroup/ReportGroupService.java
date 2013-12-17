package art.reportgroup;

import java.sql.SQLException;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

/**
 * Class to provide methods related to report groups
 *
 * @author Timothy Anyona
 */
@Service
public class ReportGroupService {

	final static Logger logger = LoggerFactory.getLogger(ReportGroupService.class);
	private final ReportGroupRepository reportGroupRepository;

	@Autowired
	public ReportGroupService(ReportGroupRepository reportGroupRepository) {
		this.reportGroupRepository = reportGroupRepository;
	}

	public ReportGroupService() {
		this.reportGroupRepository = new ReportGroupRepository();
	}

	/**
	 * Get report groups that are available for selection for a given user
	 *
	 * @param username
	 * @return
	 * @throws SQLException
	 */
	@Cacheable(value = "general")
	public List<ReportGroup> getAvailableReportGroups(String username) throws SQLException {
		//TODO remove
		logger.info("cache miss");
		
		//TODO test cacheable
		return reportGroupRepository.getAvailableReportGroups(username);
	}
}
