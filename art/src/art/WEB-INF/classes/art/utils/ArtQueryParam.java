/*
 * Copyright (C) 2001/2003  Enrico Liboni  - enrico@computer.org
 *
 *   This program is free software; you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation;
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   (version 2) along with this program (see documentation directory);
 *   otherwise, have a look at http://www.gnu.org or write to the Free Software
 *   Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 */
package art.utils;

import art.servlets.ArtDBCP;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Map;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class to represent query parameters
 *
 */
public class ArtQueryParam implements Serializable {

	private static final long serialVersionUID = 1L; //need to implement serializable to be used as a field in artgraph classes
	
	final static Logger logger = LoggerFactory.getLogger(ArtQueryParam.class);
	
	String name = "";
	String shortDescription = "";
	String description = "";
	String paramDataType = "";
	String defaultValue = "";
	String useLov = "";
	String paramType = "";
	String paramLabel = "";
	String applyRulesToLov = "";
	int queryId;
	int fieldPosition = -1;
	int lovQueryId;	
	int chainedPosition;
	int drilldownColumn;
	int chainedValuePosition;
	Object paramValue = null; //store parameter values. either String for inline parameters or String[] for multi parameters
	private Map<String, String> lovValues = null; //store value/friendly display string for lov parameters
	private String directSubstitution="N";

	/**
	 *
	 */
	public ArtQueryParam() {
	}

	/**
	 * @return the directSubstitution
	 */
	public String getDirectSubstitution() {
		return directSubstitution;
	}

	/**
	 * @param directSubstitution the directSubstitution to set
	 */
	public void setDirectSubstitution(String directSubstitution) {
		this.directSubstitution = directSubstitution;
	}
	
	/**
	 * Utility method to determine if the parameter uses a direct substitution
	 * values
	 *
	 * @return
	 * <code>true</code> if direct_substitution column has 'Y'.
	 * <code>false</code> otherwise
	 */
	public boolean usesDirectSubstitution() {
		boolean useDirectSubstitution;
		if (StringUtils.equals(directSubstitution, "Y")) {
			useDirectSubstitution = true;
		} else {
			useDirectSubstitution = false;
		}
 
		return useDirectSubstitution;
	}

	/**
	 * Get value and friendly display string for lov parameters
	 *
	 * @return the lovValues
	 */
	public Map<String, String> getLovValues() {
		return lovValues;
	}

	/**
	 * Store value and friendly display string for lov parameters
	 *
	 * @param lovValues the lovValues to set
	 */
	public void setLovValues(Map<String, String> lovValues) {
		this.lovValues = lovValues;
	}

	/**
	 * Utility method to determine if the parameter uses an lov query for its
	 * values
	 *
	 * @return
	 * <code>false</code> if use_lov column has 'N'.
	 * <code>true</code> otherwise
	 */
	public boolean usesLov() {
		boolean usesLov;
		if (StringUtils.equals(useLov, "N")) {
			usesLov = false;
		} else {
			usesLov = true;
		}
 
		return usesLov;
	}

	/**
	 * Utility method to determine, if the parameter is chained (it's value
	 * depends on another one), the position of the parameter which determines
	 * this parameter's value
	 *
	 * @return the position of the parameter which determines this parameter's
	 * value
	 */
	public int getFilterPosition() {
		int position;

		if (chainedPosition > 0 && chainedValuePosition > 0) {
			//parameter is chained, but value doesn't come from the master parameter
			position = chainedValuePosition;
		} else {
			position = chainedPosition;
		}

		return position;
	}

	/**
	 * Utility method to determine if the parameter is chained (it's value
	 * depends on another parameter)
	 *
	 * @return
	 * <code>true</code> if parameter is chained
	 */
	public boolean isChained() {
		boolean chained;

		if (chainedPosition > 0) {
			chained = true;
		} else {
			chained = false;
		}

		return chained;
	}

	/**
	 *
	 * @return parameter value
	 */
	public Object getParamValue() {
		return paramValue;
	}

	/**
	 *
	 * @param value
	 */
	public void setParamValue(Object value) {
		paramValue = value;
	}

	/**
	 *
	 * @param value
	 */
	public void setChainedValuePosition(int value) {
		chainedValuePosition = value;
	}

	/**
	 *
	 * @return chained value position
	 */
	public int getChainedValuePosition() {
		return chainedValuePosition;
	}

	/**
	 *
	 * @param i
	 */
	public void setDrilldownColumn(int i) {
		drilldownColumn = i;
	}

	/**
	 *
	 * @return drill down column
	 */
	public int getDrilldownColumn() {
		return drilldownColumn;
	}

	/*
	 * Sets
	 */
	/**
	 *
	 * @param i
	 */
	public void setLovQueryId(int i) {
		lovQueryId = i;
	}

	private void setFieldPosition(int i) { //Private!
		fieldPosition = i;
	}

	/**
	 *
	 * @param i
	 */
	public void setQueryId(int i) {
		queryId = i;
	}

	
	/**
	 *
	 * @param s
	 */
	public void setName(String s) {
		name = s;
	}

	/**
	 *
	 * @param s
	 */
	public void setShortDescription(String s) {
		shortDescription = s;
	}

	/**
	 *
	 * @param s
	 */
	public void setDescription(String s) {
		description = s;
	}

	/**
	 *
	 * @param s
	 */
	public void setApplyRulesToLov(String s) {
		s = s.substring(0, 1);
		applyRulesToLov = s;
	}

	/**
	 *
	 * @param s
	 */
	public void setParamDataType(String s) {
		paramDataType = s;
	}

	/**
	 *
	 * @param s
	 */
	public void setDefaultValue(String s) {
		defaultValue = s;
	}

	/**
	 *
	 * @param s
	 */
	public void setUseLov(String s) {
		useLov = s;
	}

	/**
	 *
	 * @param s
	 */
	public void setParamType(String s) {
		paramType = s;
	}

	/**
	 *
	 * @param s
	 */
	public void setParamLabel(String s) {
		paramLabel = s;
	}

	/**
	 *
	 * @param i
	 */
	public void setChainedPosition(int i) {
		chainedPosition = i;
	}

	/*
	 * Gets
	 */
	/**
	 *
	 * @return query id for lov query
	 */
	public int getLovQueryId() {
		return lovQueryId;
	}

	/**
	 *
	 * @return parameter position
	 */
	public int getFieldPosition() {
		return fieldPosition;
	}

	/**
	 *
	 * @return parent query id
	 */
	public int getQueryId() {
		return queryId;
	}

	
	/**
	 *
	 * @return display name
	 */
	public String getName() {
		return name;
	}

	/**
	 *
	 * @return short description
	 */
	public String getShortDescription() {
		return shortDescription;
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
	 * @return whether to use rules on the parameter's lov query
	 */
	public String getApplyRulesToLov() {
		return applyRulesToLov;
	}

	/**
	 *
	 * @return data type of parameter
	 */
	public String getParamDataType() {
		return paramDataType;
	}

	/**
	 *
	 * @return default value
	 */
	public String getDefaultValue() {
		return defaultValue;
	}

	/**
	 *
	 * @return whether the parameter uses an lov
	 */
	public String getUseLov() {
		return useLov;
	}

	/**
	 *
	 * @return parameter type
	 */
	public String getParamType() {
		return paramType;
	}

	/**
	 *
	 * @return parameter label
	 */
	public String getParamLabel() {
		return paramLabel;
	}

	/**
	 *
	 * @return chained param position
	 */
	public int getChainedPosition() {
		return chainedPosition;
	}

	/**
	 * Create the object from an existing query param (qId, fId)
	 *
	 *
	 * @param conn
	 * @param qId
	 * @param fId
	 * @return
	 * <code>true</code> if object populated successfully
	 */
	public boolean create(Connection conn, int qId, int fId) {
		boolean success = false;

		try {
			String SQL = "SELECT QUERY_ID, FIELD_POSITION, NAME, SHORT_DESCRIPTION, DESCRIPTION "
					+ " ,PARAM_DATA_TYPE, DEFAULT_VALUE, USE_LOV, PARAM_TYPE, PARAM_LABEL, APPLY_RULES_TO_LOV "
					+ " ,LOV_QUERY_ID, CHAINED_PARAM_POSITION, DRILLDOWN_COLUMN, CHAINED_VALUE_POSITION "
					+ " ,DIRECT_SUBSTITUTION "
					+ " FROM ART_QUERY_FIELDS "
					+ " WHERE QUERY_ID = ? AND FIELD_POSITION = ?";

			PreparedStatement ps = conn.prepareStatement(SQL);
			ps.setInt(1, qId);
			ps.setInt(2, fId);
			ResultSet rs = ps.executeQuery();
			if (rs.next()) {
				setQueryId(rs.getInt("QUERY_ID"));
				setFieldPosition(rs.getInt("FIELD_POSITION"));
				setName(rs.getString("NAME"));
				setShortDescription(rs.getString("SHORT_DESCRIPTION"));
				setDescription(rs.getString("DESCRIPTION"));
				setParamDataType(rs.getString("PARAM_DATA_TYPE"));
				setDefaultValue(rs.getString("DEFAULT_VALUE"));
				setUseLov(rs.getString("USE_LOV"));
				setParamType(rs.getString("PARAM_TYPE"));
				setParamLabel(rs.getString("PARAM_LABEL"));
				setApplyRulesToLov(rs.getString("APPLY_RULES_TO_LOV"));
				setLovQueryId(rs.getInt("LOV_QUERY_ID"));
				setChainedPosition(rs.getInt("CHAINED_PARAM_POSITION"));
				setDrilldownColumn(rs.getInt("DRILLDOWN_COLUMN"));
				setChainedValuePosition(rs.getInt("CHAINED_VALUE_POSITION"));
				directSubstitution=rs.getString("DIRECT_SUBSTITUTION");

				rs.close();
				ps.close();

				success = true;
			} else {
				logger.warn("The query id {} does not exist", queryId);
			}
		} catch (SQLException e) {
			logger.error("Error. Query id {}", queryId, e);
		}

		return success;
	}

	/**
	 * Update the database
	 *
	 *
	 * @param conn
	 * @return
	 * <code>true</code> if successful
	 */
	public boolean update(Connection conn) {
		// Delete
		// and Insert

		boolean success = false;

		try {
			String SQL;
			PreparedStatement ps;

			java.util.Date utilDate = new java.util.Date();
			java.sql.Date sysdate = new java.sql.Date(utilDate.getTime());

			SQL = ("DELETE FROM ART_QUERY_FIELDS WHERE QUERY_ID = ? AND FIELD_POSITION = ? ");
			ps = conn.prepareStatement(SQL);
			ps.setInt(1, queryId);
			ps.setInt(2, fieldPosition);
			ps.executeUpdate();
			ps.close();

			SQL = "INSERT INTO ART_QUERY_FIELDS "
					+ " (QUERY_ID, FIELD_POSITION, NAME, SHORT_DESCRIPTION, DESCRIPTION "
					+ " ,PARAM_DATA_TYPE, DEFAULT_VALUE, USE_LOV, PARAM_TYPE, PARAM_LABEL, APPLY_RULES_TO_LOV "
					+ " ,LOV_QUERY_ID, UPDATE_DATE, CHAINED_PARAM_POSITION, DRILLDOWN_COLUMN "
					+ " ,CHAINED_VALUE_POSITION, DIRECT_SUBSTITUTION) "
					+ " VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

			ps = conn.prepareStatement(SQL);

			ps.setInt(1, getQueryId());
			ps.setInt(2, getFieldPosition());
			ps.setString(3, getName());
			ps.setString(4, getShortDescription());
			ps.setString(5, getDescription());
			ps.setString(6, getParamDataType());
			ps.setString(7, getDefaultValue());
			ps.setString(8, getUseLov());
			ps.setString(9, getParamType());
			ps.setString(10, getParamLabel());
			ps.setString(11, getApplyRulesToLov());
			ps.setInt(12, getLovQueryId());
			ps.setDate(13, sysdate);
			ps.setInt(14, getChainedPosition());
			ps.setInt(15, getDrilldownColumn());
			ps.setInt(16, getChainedValuePosition());
			ps.setString(17,directSubstitution);

			ps.executeUpdate();
			ps.close();

			success = true;
		} catch (SQLException e) {
			logger.error("Error. Query id {}", queryId, e);
		}

		return success;
	}

	/**
	 * Insert
	 *
	 *
	 * @param conn
	 * @return
	 * <code>true</code> if successful
	 */
	public boolean insert(Connection conn) {
		// Get new Position
		try {
			String SQL;
			PreparedStatement ps;
			ResultSet rs;

			SQL = ("SELECT MAX(FIELD_POSITION) FROM ART_QUERY_FIELDS WHERE QUERY_ID = ? ");
			ps = conn.prepareStatement(SQL);
			ps.setInt(1, queryId);
			rs = ps.executeQuery();
			if (rs.next()) {
				setFieldPosition(1 + rs.getInt(1));
			}
			rs.close();
			ps.close();
		} catch (Exception e) {
			logger.error("Error. Query id {}", queryId, e);
			return false;
		}
		return update(conn);
	}

	/**
	 * Delete an existing param from the database
	 *
	 *
	 * @param conn
	 * @return
	 * <code>true</code> if successful
	 */
	public boolean delete(Connection conn) {
		boolean success = false;

		try {
			String SQL;
			PreparedStatement ps;

			// Delete Parameter
			SQL = ("DELETE FROM ART_QUERY_FIELDS WHERE QUERY_ID = ? AND FIELD_POSITION = ? ");
			ps = conn.prepareStatement(SQL);
			ps.setInt(1, queryId);
			ps.setInt(2, fieldPosition);
			ps.executeUpdate();
			ps.close();
			
			// Fix the field position for subsequent parameters
			SQL = ("UPDATE ART_QUERY_FIELDS SET FIELD_POSITION = (FIELD_POSITION-1) WHERE FIELD_POSITION > ? AND QUERY_ID = ?");
			ps = conn.prepareStatement(SQL);
			ps.setInt(1, fieldPosition);
			ps.setInt(2, queryId);
			ps.executeUpdate();
			ps.close();

			success = true;
		} catch (SQLException e) {
			logger.error("Error. Query id {}", queryId, e);
		}

		return success;
	}

	/**
	 * Delete an existing parameter from the database
	 *
	 *
	 * @param conn
	 * @return
	 * <code>true</code> if successful
	 */
	public boolean moveUp(Connection conn) {
		// Move Up
		if (fieldPosition <= 1) {
			return true; // it is already on top
		}

		boolean success = false;

		try {
			String SQL;
			PreparedStatement ps;

			SQL = ("UPDATE ART_QUERY_FIELDS SET FIELD_POSITION = ? WHERE FIELD_POSITION = ? AND QUERY_ID = ?");
			ps = conn.prepareStatement(SQL);

			// Assign to *this* parameter the "swap" position 0
			ps.setInt(1, 0);
			ps.setInt(2, fieldPosition);
			ps.setInt(3, queryId);
			ps.addBatch();

			// Move down the previous one
			ps.setInt(1, fieldPosition);
			ps.setInt(2, (fieldPosition - 1));
			ps.setInt(3, queryId);
			ps.addBatch();

			// Assign the right position to *this* parameter
			ps.setInt(1, (fieldPosition - 1));
			ps.setInt(2, 0);
			ps.setInt(3, queryId);
			ps.addBatch();

			ps.executeBatch();

			ps.close();

			success = true;
		} catch (SQLException e) {
			logger.error("Error. Query id {}", queryId, e);
		}

		return success;
	}

	/**
	 * Get a parameter's display name
	 *
	 * @param qId
	 * @param htmlName
	 * @return display name
	 */
	public String getDisplayName(int qId, String htmlName) {
		String displayName = "";

		Connection conn = null;

		try {
			conn = ArtDBCP.getConnection();

			String sql;
			PreparedStatement ps;
			ResultSet rs;

			String label = htmlName.substring(2);
			NumberFormat f = new DecimalFormat("00");

			if (htmlName.substring(0, 2).equals("P_") || !NumberUtils.isNumber(label)) {
				//inline parameter or multi parameter that uses a label

				sql = "SELECT FIELD_POSITION, NAME FROM ART_QUERY_FIELDS "
						+ " WHERE QUERY_ID = ? AND PARAM_LABEL=?";

				ps = conn.prepareStatement(sql);
				ps.setInt(1, qId);
				ps.setString(2, label);

				rs = ps.executeQuery();
				if (rs.next()) {
					displayName = f.format(rs.getInt("FIELD_POSITION")) + ". " + rs.getString("NAME");
				}
				ps.close();
				rs.close();
			} else {
				//multi parameter that uses the field position
				sql = "SELECT FIELD_POSITION, NAME FROM ART_QUERY_FIELDS "
						+ " WHERE QUERY_ID = ? AND FIELD_POSITION=?";

				ps = conn.prepareStatement(sql);
				ps.setInt(1, qId);
				ps.setInt(2, Integer.parseInt(label));

				rs = ps.executeQuery();
				if (rs.next()) {
					displayName = f.format(rs.getInt("FIELD_POSITION")) + ". " + rs.getString("NAME");
				}
				ps.close();
				rs.close();
			}

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

		if (displayName == null || displayName.equals("")) {
			displayName = htmlName;
		}

		return displayName;
	}

	/**
	 * Get a parameter's html name
	 *
	 * @param qId
	 * @param fieldPos
	 * @return a parameter's html name
	 */
	public String getHtmlName(int qId, int fieldPos) {
		String htmlName = "";

		Connection conn = null;

		try {
			conn = ArtDBCP.getConnection();

			String sql;
			PreparedStatement ps;
			ResultSet rs;

			sql = "SELECT NAME, PARAM_LABEL, PARAM_TYPE FROM ART_QUERY_FIELDS "
					+ " WHERE QUERY_ID = ? AND FIELD_POSITION=?";

			ps = conn.prepareStatement(sql);
			ps.setInt(1, qId);
			ps.setInt(2, fieldPos);

			rs = ps.executeQuery();
			if (rs.next()) {
				String label = rs.getString("PARAM_LABEL");
				String type = rs.getString("PARAM_TYPE");

				if (type.equals("I")) {
					//inline param                    
					htmlName = "P_" + label;
				} else if (type.equals("M")) {
					//multi param. can be either labelled (M_label) or non-labelled (M_1)
					//only return M_label
					htmlName = "M_" + label;
				}
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
			} catch (SQLException e) {
				logger.error("Error", e);
			}

		}

		return htmlName;
	}
	
	
}
