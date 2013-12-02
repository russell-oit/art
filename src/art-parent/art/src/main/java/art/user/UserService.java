package art.user;

import art.reportgroup.ReportGroup;
import java.sql.SQLException;
import java.util.List;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

/**
 * Class to provide methods related to users
 *
 * @author Timothy Anyona
 */
@Service
public class UserService {

	final static Logger logger = LoggerFactory.getLogger(UserService.class);
	private final UserRepository userRepository;

	@Autowired
	public UserService(UserRepository userRepository) {
		this.userRepository = userRepository;
	}

	public UserService() {
		this.userRepository=new UserRepository();
	}

	/**
	 * Get a user object for the given username
	 *
	 * @param username
	 * @return populated user object if username exists, otherwise null
	 */
	public User getUser(String username) {
		return userRepository.getUser(username);
	}
	
	/**
	 * Get all users
	 * 
	 * @return all users
	 */
	public List<User> getAllUsers() {
		return userRepository.getAllUsers();
	}
	
	/**
	 * Get report groups that are available for selection for a given user
	 * 
	 * @param username
	 * @return
	 * @throws SQLException 
	 */
	@Cacheable("art")
	public Set<ReportGroup> getAvailableReportGroups(String username) throws SQLException {
		return userRepository.getAvailableReportGroups(username);
	}
}
