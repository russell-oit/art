package art.utils;

import art.servlets.ArtDBCP;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.Collator;
import java.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class to represent rules and utility methods related to rules
 *
 */
public class Rule {

	final static Logger logger = LoggerFactory.getLogger(Rule.class);
	
	String ruleName = "";
	String description = ""; //for query definition
	int queryId; //for query rules
	String fieldName =""; //for query rules
	private String fieldDataType=""; //for query rules
	String username; //for user rules
	String ruleValue; //for user rules
	String ruleType; //for user rules

	/**
	 *
	 */
	public Rule() {
	}

	/**
	 * @return the fieldDataType
	 */
	public String getFieldDataType() {
		return fieldDataType;
	}

	/**
	 * @param fieldDataType the fieldDataType to set
	 */
	public void setFieldDataType(String fieldDataType) {
		this.fieldDataType = fieldDataType;
	}

	/**
	 *
	 * @return rule user
	 */
	public String getUsername() {
		return username;
	}

	/**
	 *
	 * @param value
	 */
	public void setUsername(String value) {
		username = value;
	}

	/**
	 *
	 * @return rule value
	 */
	public String getRuleValue() {
		return ruleValue;
	}

	/**
	 *
	 * @param value
	 */
	public void setRuleValue(String value) {
		ruleValue = value;
	}

	/**
	 *
	 * @return rule type
	 */
	public String getRuleType() {
		return ruleType;
	}

	/**
	 *
	 * @param value
	 */
	public void setRuleType(String value) {
		ruleType = value;
	}

	/**
	 *
	 * @return query id for the rule
	 */
	public int getQueryId() {
		return queryId;
	}

	/**
	 *
	 * @param value
	 */
	public void setQueryId(int value) {
		queryId = value;
	}

	/**
	 *
	 * @return rule name
	 */
	public String getRuleName() {
		return ruleName;
	}

	/**
	 *
	 * @param value
	 */
	public void setRuleName(String value) {
		ruleName = value;
	}

	/**
	 *
	 * @return field on which rule will apply
	 */
	public String getFieldName() {
		return fieldName;
	}

	/**
	 *
	 * @param value
	 */
	public void setFieldName(String value) {
		fieldName = value;
	}

	/**
	 *
	 * @return description
	 */
	public String getDescription() {
		return description;
	}

	/**
	 *
	 * @param value
	 */
	public void setDescription(String value) {
		description = value;
	}

	/**
	 * Delete a query-rule association
	 *
	 * @param conn
	 * @param qId
	 * @param rName
	 * @return
	 * <code>true</code> if successful
	 */
	public boolean deleteQueryRule(Connection conn, int qId, String rName) {
		boolean success = false;

		try {
			String sql = "DELETE FROM ART_QUERY_RULES WHERE QUERY_ID = ? AND RULE_NAME = ?";
			PreparedStatement ps = conn.prepareStatement(sql);

			ps.setInt(1, qId);
			ps.setString(2, rName);

			ps.executeUpdate();
			ps.close();
			success = true;
		} catch (Exception e) {
			logger.error("Error", e);
		}

		return success;
	}

	/**
	 * Insert a query-rule association
	 *
	 * @param conn
	 * @return
	 * <code>true</code> if successful
	 */
	public boolean insertQueryRule(Connection conn) {
		boolean success = false;

		try {
			String sql = "INSERT INTO ART_QUERY_RULES (QUERY_ID, FIELD_NAME, RULE_NAME, FIELD_DATA_TYPE )VALUES (?, ?, ?, ?) ";
			PreparedStatement ps = conn.prepareStatement(sql);

			ps.setInt(1, queryId);
			ps.setString(2, fieldName);
			ps.setString(3, ruleName);
			ps.setString(4,fieldDataType);

			ps.executeUpdate();
			ps.close();
			success = true;
		} catch (Exception e) {
			logger.error("Error", e);
		}

		return success;
	}

	/**
	 * Modify the database column attached to a query rule
	 *
	 * @param conn
	 * @return
	 * <code>true</code> if successful
	 */
	public boolean modifyQueryRuleColumn(Connection conn) {
		boolean success = false;

		try {
			String sql = "UPDATE ART_QUERY_RULES "
					+ " SET FIELD_NAME=?, FIELD_DATA_TYPE=? "
					+ " WHERE QUERY_ID=? AND RULE_NAME=?";
			PreparedStatement ps = conn.prepareStatement(sql);

			ps.setString(1, fieldName);
			ps.setString(2,fieldDataType);
			ps.setInt(3, queryId);
			ps.setString(4, ruleName);

			ps.executeUpdate();
			ps.close();
			success = true;
		} catch (Exception e) {
			logger.error("Error", e);
		}

		return success;
	}

	/**
	 * Populate the rule object with the database column for the given query
	 * rule
	 *
	 * @param name
	 * @return
	 * <code>true</code> if successful
	 */
	public boolean loadQueryRuleColumn(int qId, String rName) {
		boolean success = false;

		Connection conn = null;

		queryId = qId;
		ruleName = rName;

		try {
			conn = ArtDBCP.getConnection();

			String sql = "SELECT FIELD_NAME, FIELD_DATA_TYPE "
					+ " FROM ART_QUERY_RULES "
					+ " WHERE QUERY_ID=? AND RULE_NAME=?";
			PreparedStatement ps = conn.prepareStatement(sql);

			ps.setInt(1, queryId);
			ps.setString(2, ruleName);

			ResultSet rs = ps.executeQuery();
			if (rs.next()) {
				fieldName = rs.getString("FIELD_NAME");
				fieldDataType=rs.getString("FIELD_DATA_TYPE");
			}
			rs.close();
			ps.close();
			success = true;
		} catch (Exception e) {
			logger.error("Error", e);
		} finally {
			try {
				if (conn != null) {
					conn.close();
				}
			} catch (Exception e) {
				logger.error("Error", e);
			}
		}

		return success;
	}

	/**
	 * Insert new rule definition
	 *
	 * @return
	 * <code>true</code> if successful
	 */
	public boolean insertDefinition() {
		boolean success = false;

		Connection conn = null;

		try {
			conn = ArtDBCP.getConnection();

			String sql = "INSERT INTO ART_RULES (RULE_NAME, SHORT_DESCRIPTION) "
					+ " VALUES (?,?)";

			PreparedStatement ps = conn.prepareStatement(sql);
			ps.setString(1, ruleName);
			ps.setString(2, description);
			ps.executeUpdate();
			ps.close();
			success = true;
		} catch (Exception e) {
			logger.error("Error", e);
		} finally {
			try {
				if (conn != null) {
					conn.close();
				}
			} catch (Exception e) {
				logger.error("Error", e);
			}
		}

		return success;
	}

	/**
	 * Delete a rule definition
	 *
	 * @param name
	 * @return
	 * <code>true</code> if successful
	 */
	public boolean deleteDefinition(String name) {
		boolean success = false;

		Connection conn = null;

		try {
			conn = ArtDBCP.getConnection();

			String sql = "DELETE FROM ART_RULES "
					+ " WHERE RULE_NAME = ?";

			PreparedStatement ps = conn.prepareStatement(sql);
			ps.setString(1, name);
			ps.executeUpdate();
			ps.close();
			success = true;
		} catch (Exception e) {
			logger.error("Error", e);
		} finally {
			try {
				if (conn != null) {
					conn.close();
				}
			} catch (Exception e) {
				logger.error("Error", e);
			}
		}

		return success;
	}

	/**
	 * Populate the rule object with rule definition of the given rule name
	 *
	 * @param name
	 * @return
	 * <code>true</code> if successful
	 */
	public boolean loadDefinition(String name) {
		boolean success = false;

		Connection conn = null;

		try {
			conn = ArtDBCP.getConnection();

			String sql = "SELECT RULE_NAME, SHORT_DESCRIPTION "
					+ " FROM ART_RULES "
					+ " WHERE RULE_NAME = ?";
			PreparedStatement ps = conn.prepareStatement(sql);

			ps.setString(1, name);

			ResultSet rs = ps.executeQuery();
			if (rs.next()) {
				ruleName = rs.getString("RULE_NAME");
				description = rs.getString("SHORT_DESCRIPTION");
			}
			rs.close();
			ps.close();
			success = true;
		} catch (Exception e) {
			logger.error("Error", e);
		} finally {
			try {
				if (conn != null) {
					conn.close();
				}
			} catch (Exception e) {
				logger.error("Error", e);
			}
		}

		return success;
	}

	/**
	 * Update rule definition
	 *
	 * @return
	 * <code>true</code> if successful
	 */
	public boolean updateDefinition() {
		boolean success = false;

		Connection conn = null;

		try {
			conn = ArtDBCP.getConnection();

			String sql = "UPDATE ART_RULES SET SHORT_DESCRIPTION = ? "
					+ " WHERE RULE_NAME = ? ";

			PreparedStatement ps = conn.prepareStatement(sql);
			ps.setString(1, description);
			ps.setString(2, ruleName);

			ps.executeUpdate();
			ps.close();

			success = true;
		} catch (Exception e) {
			logger.error("Error", e);
		} finally {
			try {
				if (conn != null) {
					conn.close();
				}
			} catch (Exception e) {
				logger.error("Error", e);
			}
		}

		return success;
	}

	/**
	 * Insert a rule value for a given user
	 *
	 * @return
	 * <code>true</code> if successful
	 */
	public boolean insertUserRuleValue() {
		boolean success = false;

		Connection conn = null;

		try {
			conn = ArtDBCP.getConnection();

			String sql = "INSERT INTO ART_USER_RULES (RULE_NAME, USERNAME, RULE_TYPE, RULE_VALUE) "
					+ " VALUES (?, ?, ?, ?)";

			PreparedStatement ps = conn.prepareStatement(sql);
			ps.setString(1, ruleName);
			ps.setString(2, username);
			ps.setString(3, ruleType);
			ps.setString(4, ruleValue);

			ps.executeUpdate();
			ps.close();
			success = true;
		} catch (Exception e) {
			logger.error("Error", e);
		} finally {
			try {
				if (conn != null) {
					conn.close();
				}
			} catch (Exception e) {
				logger.error("Error", e);
			}
		}

		return success;
	}

	/**
	 * Update a rule value for a given user
	 *
	 * @param oldType
	 * @param oldValue
	 * @return
	 * <code>true</code> if successful
	 */
	public boolean updateUserRuleValue(String oldType, String oldValue) {
		boolean success = false;

		Connection conn = null;

		try {
			conn = ArtDBCP.getConnection();

			String sql = "UPDATE ART_USER_RULES SET RULE_TYPE = ? , RULE_VALUE = ?  "
					+ " WHERE RULE_NAME = ? AND USERNAME = ? AND RULE_TYPE = ? AND RULE_VALUE = ?";

			PreparedStatement ps = conn.prepareStatement(sql);
			ps.setString(1, ruleType);
			ps.setString(2, ruleValue);
			ps.setString(3, ruleName);
			ps.setString(4, username);
			ps.setString(5, oldType);
			ps.setString(6, oldValue);

			ps.executeUpdate();
			ps.close();
			success = true;
		} catch (Exception e) {
			logger.error("Error", e);
		} finally {
			try {
				if (conn != null) {
					conn.close();
				}
			} catch (Exception e) {
				logger.error("Error", e);
			}
		}

		return success;
	}

	/**
	 * Delete a rule value for a given user
	 *
	 * @param user
	 * @param rName
	 * @param rType
	 * @param rValue
	 * @return
	 * <code>true</code> if successful
	 */
	public boolean deleteUserRuleValue(String user, String rName, String rType, String rValue) {
		boolean success = false;

		Connection conn = null;

		try {
			conn = ArtDBCP.getConnection();

			String sql = "DELETE FROM ART_USER_RULES "
					+ " WHERE RULE_NAME = ?  "
					+ " AND USERNAME = ? "
					+ " AND RULE_TYPE = ? "
					+ " AND RULE_VALUE = ? ";

			PreparedStatement ps = conn.prepareStatement(sql);

			ps.setString(1, rName);
			ps.setString(2, user);
			ps.setString(3, rType);
			ps.setString(4, rValue);

			ps.executeUpdate();
			ps.close();
			success = true;
		} catch (Exception e) {
			logger.error("Error", e);
		} finally {
			try {
				if (conn != null) {
					conn.close();
				}
			} catch (Exception e) {
				logger.error("Error", e);
			}
		}

		return success;
	}

	/**
	 * Get all rule names
	 *
	 * @return all rule names
	 */
	public List<String> getAllRuleNames() {
		List<String> names = new ArrayList<String>();

		Connection conn = null;

		try {
			conn = ArtDBCP.getConnection();

			String sql = "SELECT RULE_NAME FROM ART_RULES";

			PreparedStatement ps = conn.prepareStatement(sql);
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				names.add(rs.getString("RULE_NAME"));
			}
			ps.close();
			rs.close();
		} catch (Exception e) {
			logger.error("Error", e);
		} finally {
			try {
				if (conn != null) {
					conn.close();
				}
			} catch (Exception e) {
				logger.error("Error", e);
			}
		}

		//sort names
		Collator stringCollator = Collator.getInstance();
		stringCollator.setStrength(Collator.TERTIARY); //order by case
		Collections.sort(names, stringCollator);

		return names;
	}

	/**
	 * Get all queries linked/associated with a rule
	 *
	 * @param rName
	 * @return all queries linked/associated with a rule
	 */
	public Map getLinkedQueries(String rName) {
		TreeMap<Integer, ArtQuery> map = new TreeMap<Integer, ArtQuery>();

		Connection conn = null;

		try {
			conn = ArtDBCP.getConnection();

			String sql;
			PreparedStatement ps;
			ResultSet rs;
			int qId;

			sql = "SELECT AQ.QUERY_GROUP_ID, AQ.QUERY_ID, AQ.NAME FROM ART_QUERY_RULES AQR, ART_QUERIES AQ"
					+ " WHERE AQ.QUERY_ID = AQR.QUERY_ID "
					+ " AND AQR.RULE_NAME = ?  ";

			ps = conn.prepareStatement(sql);

			ps.setString(1, rName);
			rs = ps.executeQuery();
			while (rs.next()) {
				ArtQuery aq = new ArtQuery();
				qId = rs.getInt("QUERY_ID");
				aq.setQueryId(qId);
				aq.setGroupId(rs.getInt("QUERY_GROUP_ID"));
				aq.setName(rs.getString("NAME"));

				map.put(qId, aq);
			}
			rs.close();
			ps.close();

		} catch (Exception e) {
			logger.error("Error", e);
		} finally {
			try {
				if (conn != null) {
					conn.close();
				}
			} catch (SQLException e) {
				logger.error("Error", e);
			}
		}

		return map;
	}

	/**
	 * Get an indicator of which users have values defined for which rules
	 *
	 * @return an indicator of which users have values defined for which rules
	 */
	public Map getUserRuleAssignment() {
		TreeMap<Integer, String> map = new TreeMap<Integer, String>();

		Connection conn = null;

		try {
			conn = ArtDBCP.getConnection();

			String sql;
			PreparedStatement ps;
			ResultSet rs;
			String tmp;
			Integer count = 0;

			sql = "SELECT DISTINCT USERNAME, RULE_NAME "
					+ " FROM ART_USER_RULES";

			ps = conn.prepareStatement(sql);
			rs = ps.executeQuery();
			while (rs.next()) {
				count++;
				tmp = rs.getString("USERNAME") + " - " + rs.getString("RULE_NAME");
				map.put(count, tmp);
			}
			rs.close();
			ps.close();
		} catch (Exception e) {
			logger.error("Error", e);
		} finally {
			try {
				if (conn != null) {
					conn.close();
				}
			} catch (SQLException e) {
				logger.error("Error", e);
			}
		}

		return map;
	}

	/**
	 * Delete all rule values for a given user
	 *
	 * @param user
	 * @param rName
	 * @return
	 * <code>true</code> if successful
	 */
	public boolean deleteUserRule(String user, String rName) {
		boolean success = false;

		Connection conn = null;

		try {
			conn = ArtDBCP.getConnection();

			String sql = "DELETE FROM ART_USER_RULES "
					+ " WHERE RULE_NAME = ? AND USERNAME = ? ";

			PreparedStatement ps = conn.prepareStatement(sql);

			ps.setString(1, rName);
			ps.setString(2, user);

			ps.executeUpdate();
			ps.close();
			success = true;
		} catch (Exception e) {
			logger.error("Error", e);
		} finally {
			try {
				if (conn != null) {
					conn.close();
				}
			} catch (Exception e) {
				logger.error("Error", e);
			}
		}

		return success;
	}
}