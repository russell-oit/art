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
package art.selfservice;

import art.dbutils.DatabaseUtils;
import art.report.Report;
import art.reportoptions.GeneralReportOptions;
import art.reportoptions.ViewOptions;
import art.runreport.ReportRunner;
import art.user.User;
import art.utils.ArtUtils;
import com.itfsw.query.builder.support.model.JsonRule;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.map.CaseInsensitiveMap;
import org.apache.commons.lang3.StringUtils;

/**
 * Provides methods for assisting with running self service reports
 *
 * @author Timothy Anyona
 */
public class SelfServiceHelper {

	/**
	 * Returns self service columns for use within the self service reports user
	 * interface
	 *
	 * @param report the report
	 * @param user the current user
	 * @return self service columns
	 * @throws SQLException
	 */
	public List<SelfServiceColumn> getSelfServiceColumnsForView(Report report,
			User user) throws SQLException {

		boolean nameAsLabel = true;
		boolean setType = true;
		return getSelfServiceColumns(report, user, nameAsLabel, setType);
	}

	/**
	 * Returns basic self service column information for a report
	 *
	 * @param report the report
	 * @param user the user
	 * @param runId the run id of the report
	 * @return self service columns
	 * @throws SQLException
	 */
	private List<SelfServiceColumn> getSelfServiceColumns(Report report,
			User user, String runId) throws SQLException {

		boolean nameAsLabel = false;
		boolean setType = false;
		return getSelfServiceColumns(report, user, nameAsLabel, setType, runId);
	}

	/**
	 * Returns self service columns based on the report configuration
	 *
	 * @param report the report
	 * @param user the current user
	 * @param nameAsLabel whether name should be used for the label for self
	 * service reports
	 * @param setType whether the type field should be set
	 * @return self service columns
	 * @throws SQLException
	 */
	private List<SelfServiceColumn> getSelfServiceColumns(Report report,
			User user, boolean nameAsLabel, boolean setType) throws SQLException {

		String runId = null;
		return getSelfServiceColumns(report, user, nameAsLabel, setType, runId);
	}

	/**
	 * Returns self service columns based on the report configuration
	 *
	 * @param report the report
	 * @param user the current user
	 * @param nameAsLabel whether name should be used for the label for self
	 * service reports
	 * @param setType whether the type field should be set
	 * @param runId the run id for the report
	 * @return self service columns
	 * @throws SQLException
	 */
	private List<SelfServiceColumn> getSelfServiceColumns(Report report,
			User user, boolean nameAsLabel, boolean setType, String runId)
			throws SQLException {

		Objects.requireNonNull(report, "report must not be null");

		List<SelfServiceColumn> columns = new ArrayList<>();

		GeneralReportOptions generalOptions = report.getGeneralOptions();
		ViewOptions viewOptions = generalOptions.getView();

		List<String> omitColumns = null;
		List<Map<String, String>> columnLabels = null;
		List<Map<String, String>> columnDescriptions = null;

		if (viewOptions != null) {
			omitColumns = viewOptions.getOmitColumns();
			columnLabels = viewOptions.getColumnLabels();
			columnDescriptions = viewOptions.getColumnDescriptions();
		}

		ReportRunner reportRunner = new ReportRunner();
		ResultSet rs = null;
		try {
			reportRunner.setLimit(ReportRunner.RETURN_ZERO_RECORDS);
			reportRunner.setUseViewColumns(true);
			reportRunner.setUser(user);
			reportRunner.setReport(report);
			reportRunner.setRunId(runId);

			rs = reportRunner.executeQuery();

			if (rs == null) {
				throw new RuntimeException("ResultSet is null");
			}

			ResultSetMetaData rsmd = rs.getMetaData();
			int columnCount = rsmd.getColumnCount();

			for (int i = 1; i <= columnCount; i++) {
				SelfServiceColumn column = new SelfServiceColumn();

				column.setName(rsmd.getColumnName(i));

				if (report.isSelfService() && nameAsLabel) {
					column.setLabel(rsmd.getColumnName(i));
				} else {
					column.setLabel(rsmd.getColumnLabel(i));
				}

				if (setType) {
					int sqlType = rsmd.getColumnType(i);

					String type;

					switch (sqlType) {
						case Types.INTEGER:
						case Types.TINYINT:
						case Types.SMALLINT:
						case Types.BIGINT:
							type = "integer";
							break;
						case Types.NUMERIC:
						case Types.DECIMAL:
						case Types.FLOAT:
						case Types.REAL:
						case Types.DOUBLE:
							type = "double";
							break;
						case Types.DATE:
							type = "date";
							break;
						case Types.TIME:
							type = "time";
							break;
						case Types.TIMESTAMP:
							type = "datetime";
							break;
						default:
							type = "string";
					}

					column.setType(type);
				}

				columns.add(column);
			}

			if (omitColumns != null) {
				for (String columnName : omitColumns) {
					//https://stackoverflow.com/questions/10431981/remove-elements-from-collection-while-iterating
					columns.removeIf((SelfServiceColumn column) -> StringUtils.equalsIgnoreCase(columnName, column.getLabel()));
				}
			}

			for (SelfServiceColumn column : columns) {
				String label = column.getLabel();
				String userLabel = null;
				if (columnLabels != null) {
					for (Map<String, String> labelDefinition : columnLabels) {
						Map<String, String> caseInsensitiveMap = new CaseInsensitiveMap<>(labelDefinition);
						userLabel = caseInsensitiveMap.get(label);
						if (userLabel != null) {
							break;
						}
					}
				}

				if (userLabel == null) {
					userLabel = label;
				}
				column.setUserLabel(userLabel);

				String description = null;
				if (columnDescriptions != null) {
					for (Map<String, String> descriptionDefinition : columnDescriptions) {
						Map<String, String> caseInsensitiveMap = new CaseInsensitiveMap<>(descriptionDefinition);
						description = caseInsensitiveMap.get(label);
						if (description != null) {
							break;
						}
					}
				}
				if (description == null) {
					description = "";
				}
				column.setDescription(description);
			}
		} finally {
			DatabaseUtils.close(rs);
			reportRunner.close();
		}
		return columns;
	}

	/**
	 * Verifies the selected self service columns and sets the final columns
	 * string to use
	 *
	 * @param report the report
	 * @param user the current user
	 * @throws IOException
	 * @throws SQLException
	 */
	public void applySelfServiceFields(Report report, User user) throws IOException, SQLException {
		String runId = null;
		applySelfServiceFields(report, user, runId);
	}

	/**
	 * Verifies the selected self service columns and sets the final columns
	 * string to use
	 *
	 * @param report the report
	 * @param user the current user
	 * @param runId the run id for the report
	 * @throws IOException
	 * @throws SQLException
	 */
	public void applySelfServiceFields(Report report, User user, String runId)
			throws IOException, SQLException {

		Objects.requireNonNull(report, "report must not be null");

		if (!report.isViewOrSelfService()) {
			return;
		}

		List<SelfServiceColumn> selfServiceColumns = getSelfServiceColumns(report, user, runId);

		SelfServiceOptions selfServiceOptions;
		String selfServiceOptionsString = report.getSelfServiceOptions();
		if (StringUtils.isBlank(selfServiceOptionsString)) {
			selfServiceOptions = new SelfServiceOptions();
		} else {
			selfServiceOptions = ArtUtils.jsonToObjectIgnoreUnknown(selfServiceOptionsString, SelfServiceOptions.class);
		}

		JsonRule javaRule = selfServiceOptions.getJavaRule();
		if (javaRule != null) {
			for (JsonRule rule : javaRule.getRules()) {
				String field = rule.getField();
				boolean found = false;
				for (SelfServiceColumn selfServiceColumn : selfServiceColumns) {
					if (StringUtils.equalsIgnoreCase(field, selfServiceColumn.getLabel())) {
						found = true;
						break;
					}
				}
				if (!found) {
					throw new RuntimeException("Field not found: " + field);
				}
			}
		}

		List<String> chosenColumns = new ArrayList<>();
		List<String> columns = selfServiceOptions.getColumns();
		if (CollectionUtils.isEmpty(columns)) {
			for (SelfServiceColumn referenceColumn : selfServiceColumns) {
				String columnSpecification = getColumnSpecification(referenceColumn);
				chosenColumns.add(columnSpecification);
			}
		} else {
			for (String column : columns) {
				SelfServiceColumn referenceColumn = selfServiceColumns.stream().filter((SelfServiceColumn c) -> c.getLabel().equals(column)).findAny().orElseThrow(() -> new RuntimeException("Invalid column: " + column));
				String columnSpecification = getColumnSpecification(referenceColumn);
				chosenColumns.add(columnSpecification);
			}
		}

		String columnsString = StringUtils.join(chosenColumns, ", ");

		selfServiceOptions.setColumnsString(columnsString);
		selfServiceOptionsString = ArtUtils.objectToJson(selfServiceOptions);

		report.setSelfServiceOptions(selfServiceOptionsString);
	}

	/**
	 * Returns the column specification to use for a self service column i.e.
	 * column name alone or column name and alias
	 *
	 * @param column the self service column
	 * @return the column specification to use
	 */
	private String getColumnSpecification(SelfServiceColumn column) {
		String columnSpecification;
		if (StringUtils.equals(column.getLabel(), column.getUserLabel())) {
			columnSpecification = column.getLabel();
		} else {
			//https://stackoverflow.com/questions/19657101/what-is-the-difference-between-square-brackets-and-single-quotes-for-aliasing-in
			String cleanUserLabel = StringUtils.remove(column.getUserLabel(), "\"");
			columnSpecification = column.getLabel() + " as \"" + cleanUserLabel + "\"";
		}

		return columnSpecification;
	}

}
