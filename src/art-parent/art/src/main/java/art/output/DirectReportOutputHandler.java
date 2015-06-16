/*
 * Copyright 2001-2013 Enrico Liboni <eliboni@users.sourceforge.net>
 *
 * This file is part of ART.
 *
 * ART is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation, version 2 of the License.
 *
 * ART is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * ART. If not, see <http://www.gnu.org/licenses/>.
 */
package art.output;

import art.enums.DisplayNull;
import art.servlets.Config;
import art.utils.ActionResult;
import art.utils.ArtQueryParam;
import art.utils.DrilldownQuery;
import java.io.PrintWriter;
import java.net.URLEncoder;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;
import java.util.*;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Generate report output by scrolling the resultset and feeding the
 * ReportOutputInterface object
 *
 * @author Enrico Liboni
 * @author Timothy Anyona
 */
public class DirectReportOutputHandler {

	private static final Logger logger = LoggerFactory.getLogger(DirectReportOutputHandler.class);

	/**
	 * Flush the output as it is (row by row). For output that can't have drill
	 * down queries and needs to show parameters in output
	 *
	 * @param o output object
	 * @param rs query resultset
	 * @return ActionResult. if successful, data contains the number of rows in
	 * the resultset. if not, message contains the i18n message indicating the
	 * problem
	 * @throws SQLException
	 */
	public static ActionResult flushOutput(ReportOutputInterface o, ResultSet rs) throws SQLException {

		return flushOutput(o, rs, null, null, null, null);
	}

	/**
	 * Output query results
	 *
	 * @param o output object
	 * @param rs query resultset
	 * @param drilldownQueries drill down queries
	 * @param baseUrl url to art application
	 * @param inlineParams inline parameters
	 * @param multiParams multi parameters
	 * @return ActionResult. if successful, data contains the number of rows in
	 * the resultset. if not, message contains the i18n message indicating the
	 * problem
	 * @throws SQLException
	 */
	public static ActionResult flushOutput(ReportOutputInterface o, ResultSet rs,
			Map<Integer, DrilldownQuery> drilldownQueries, String baseUrl,
			Map<String, String> inlineParams, Map<String, String[]> multiParams) throws SQLException {

		ActionResult result = new ActionResult();

		ResultSetMetaData rsmd = rs.getMetaData();

		int columnCount = rsmd.getColumnCount();
		int i;
		int counter = 0;

		int columnTypes[] = new int[columnCount]; // 0 = string ; 1 = numeric/double ; 2 = int ; 3 = date 
		String tmpstr;

		//include columns for drill down queries
		int drilldownCount = 0;
		if (drilldownQueries != null) {
			drilldownCount = drilldownQueries.size();
		}

		//include columns for drill down queries		
		o.setColumnsNumber(columnCount + drilldownCount);

		o.beginHeader();
		for (i = 0; i < columnCount; i++) {
			tmpstr = rsmd.getColumnLabel(i + 1);
			setTypesArray(rsmd.getColumnType(i + 1), columnTypes, i);
			o.addHeaderCell(tmpstr);
		}

		//include columns for drill down queries
		String drilldownTitle;

		if (drilldownQueries != null) {
			for (Map.Entry<Integer, DrilldownQuery> entry : drilldownQueries.entrySet()) {
				DrilldownQuery drilldown = entry.getValue();
				drilldownTitle = drilldown.getDrilldownTitle();
				if (drilldownTitle == null || drilldownTitle.trim().length() == 0) {
					drilldownTitle = drilldown.getDrilldownQueryName();
				}
				o.addHeaderCell(drilldownTitle);
			}
		}

		o.endHeader();

		o.beginLines();

		String drilldownUrl;
		String drilldownText;
		String outputFormat;
		int drilldownQueryId;
		List<ArtQueryParam> drilldownParams;
		String openInNewWindow;

		//store parameter names so that parent parameters with the same name as in the drilldown query are omitted
		HashMap<String, String> params = new HashMap<String, String>();

		//checking to see if Display Null Value optional setting is set to "No"
//		if (Config.getSettings().getDisplayNull() != DisplayNull.Yes) {
//			o = new HideNullOutput(o);
//		}

		while (rs.next()) {
			if (!o.newLine()) {
				//couldn't create new line. row limit exceeded
				//for xlsx, it's also possible that an error occurred.
				//just show one message. if error occurred, it will be logged
				result.setMessage("tooManyRows");
				return result;
			}

			//save column values for use in drill down columns.
			//for the jdbc-odbc bridge, you can only read
			// column values ONCE and in the ORDER they appear in the select
			List<String> columnValues = new ArrayList<String>();
			for (i = 0; i < columnCount; i++) {
				switch (columnTypes[i]) {
					case 0:
						String stringValue = rs.getString(i + 1);
						columnValues.add(stringValue);
						o.addCellString(stringValue);
						break;
					case 1:
						//allow null int or float fields to be indicated as null instead of 0
						Double doubleValue = Double.valueOf(rs.getDouble(i + 1));
						columnValues.add(String.valueOf(doubleValue));
						if (rs.wasNull()) {
							doubleValue = null;
						}
						o.addCellDouble(doubleValue);
						break;
					case 2:
						//allow null int or float fields to be indicated as null instead of 0
						Long longValue = Long.valueOf(rs.getLong(i + 1));
						columnValues.add(String.valueOf(longValue));
						if (rs.wasNull()) {
							longValue = null;
						}
						o.addCellLong(longValue);
						break;
					case 3:
						java.util.Date dateValue = rs.getTimestamp(i + 1);
						columnValues.add(String.valueOf(dateValue));
						o.addCellDate(dateValue); // try always to get a timestamp, the jdbc driver should properly convert to a date...
						break;
					case 4:
						// convert CLOB to string
						java.sql.Clob clob = rs.getClob(i + 1);

						String clobValue = null;
						if (clob == null) {
							o.addCellString(null);
						} else {
							try {
								clobValue = clob.getSubString(1, (int) clob.length());
								o.addCellString(clobValue);
							} catch (SQLException e) {
								logger.error("Error", e);
								clobValue = "Exception getting CLOB: " + e;
								o.addCellString(clobValue);
							}
						}
						columnValues.add(clobValue);
						break;
					default:
						String defaultValue = rs.getString(i + 1);
						columnValues.add(defaultValue);
						o.addCellString(defaultValue);
				}
			}

			//display columns for drill down queries			
			if (drilldownQueries != null) {
				for (Map.Entry<Integer, DrilldownQuery> entry : drilldownQueries.entrySet()) {
					DrilldownQuery drilldown = entry.getValue();
					drilldownText = drilldown.getDrilldownText();
					if (drilldownText == null || drilldownText.trim().length() == 0) {
						drilldownText = "Drill Down";
					}

					drilldownQueryId = drilldown.getDrilldownQueryId();
					outputFormat = drilldown.getOutputFormat();

					StringBuilder sb = new StringBuilder(200);

					if (outputFormat == null || outputFormat.equalsIgnoreCase("ALL")) {
						sb.append(baseUrl).append("/user/showParams.jsp?queryId=").append(drilldownQueryId);
					} else {
						sb.append(baseUrl).append("/user/ExecuteQuery?queryId=").append(drilldownQueryId)
								.append("&viewMode=").append(outputFormat);
					}

					String paramLabel;
					String paramValue;
					drilldownParams = drilldown.getDrilldownParams();
					if (drilldownParams != null) {
						for (ArtQueryParam param : drilldownParams) {
							paramLabel = param.getParamLabel();
							paramValue = columnValues.get(param.getDrilldownColumn() - 1);
							if (paramValue != null) {
								try {
									paramValue = URLEncoder.encode(paramValue, "UTF-8");
								} catch (Exception e) {
									logger.warn("Error while encoding. Parameter={}, Value={}", new Object[]{paramLabel, paramValue, e});
								}
							}
							sb.append("&P_").append(paramLabel).append("=").append(paramValue);
							params.put(paramLabel, paramValue);
						}
					}

					//add parameters from parent query										
					if (inlineParams != null) {
						for (Map.Entry<String, String> entryInline : inlineParams.entrySet()) {
							paramLabel = entryInline.getKey();
							paramValue = entryInline.getValue();
							//add parameter only if one with a similar name doesn't already exist in the drill down parameters
							if (!params.containsKey(paramLabel)) {
								if (paramValue != null) {
									try {
										paramValue = URLEncoder.encode(paramValue, "UTF-8");
									} catch (Exception e) {
										logger.warn("Error while encoding. Parameter={}, Value={}", new Object[]{paramLabel, paramValue, e});
									}
								}
								sb.append("&P_").append(paramLabel).append("=").append(paramValue);
							}
						}
					}

					if (multiParams != null) {
						String[] paramValues;
						for (Map.Entry<String, String[]> entryMulti : multiParams.entrySet()) {
							paramLabel = entryMulti.getKey();
							paramValues = entryMulti.getValue();
							for (String param : paramValues) {
								if (param != null) {
									try {
										param = URLEncoder.encode(param, "UTF-8");
									} catch (Exception e) {
										logger.warn("Error while encoding. Parameter={}, Value={}", new Object[]{paramLabel, param, e});
									}
								}
								sb.append("&M_").append(paramLabel).append("=").append(param);
							}
						}
					}

					drilldownUrl = sb.toString();
					openInNewWindow = drilldown.getOpenInNewWindow();
					if (StringUtils.equals(openInNewWindow, "Y") || openInNewWindow == null) {
						//open drill down in new window
						o.addCellString("<a href=\"" + drilldownUrl + "\" target=\"_blank\">" + drilldownText + " </a>");
					} else {
						//open in same window
						o.addCellString("<a href=\"" + drilldownUrl + "\">" + drilldownText + " </a>");
					}
				}
			}

			counter++;
		}
		o.endLines();

		result.setSuccess(true);
		result.setData(counter);
		return result;
	}

	/**
	 * Generate crosstab output.
	 *
	 * @param o output object
	 * @param rs query resultset
	 * @return ActionResult. if successful, data contains the number of rows in
	 * the resultset. if not, message contains the i18n message indicating the
	 * problem
	 * @throws SQLException
	 */
	public static ActionResult flushXOutput(ReportOutputInterface o, ResultSet rs)
			throws SQLException {

		/*
		 * input
		 */ 		     	 /*
		 * input
		 */
		// A Jan 14			     	  A 1 Jan 1 14
		// A Feb 24			     	  A 1 Feb 2 24
		// A Mar 34			     	  A 1 Mar 3 34
		// B Jan 14			     	  B 2 Jan 1 14
		// B Feb 24			     	  B 2 Feb 2 24
		// C Jan 04			     	  C 3 Jan 1 04
		// C Mar 44			     	  C 3 Mar 3 44
		//				     	    ^-----^------Used to sort the x/y axis

		/*
		 * output
		 */		     	 /*
		 * output
		 */
		//         y-axis		     	 	  y-axis	      
		//           |		     	 	    |				 
		//  x-axis - _   Feb Jan Mar     	   x-axis - _  Jan Feb Mar
		//           A    24  14  34      	 	    A	14  24  34   
		//           B    24  14  -      	 	    B	14  24   -   
		//           C    -   04  44      	 	    C	04   -  44   
		//                   ^--- Jan comes after Feb!			     	 

		ActionResult result = new ActionResult();

		ResultSetMetaData rsmd = rs.getMetaData();
		int colCount = rsmd.getColumnCount();
		if (colCount != 3 && colCount != 5) {
			result.setMessage("reports.message.invalidCrosstab");
			return result;
		}

		//checking to see if Display Null Value optional setting is set to "No"
//		if (Config.getSettings().getDisplayNull() != DisplayNull.Yes) {
//			o = new HideNullOutput(o);
//		}

		// Check the data type of the value (last column)
		int columnType;
		int[] columnTypes = new int[1];
		setTypesArray(rsmd.getColumnType(colCount), columnTypes, 0);
		columnType = columnTypes[0];
		int counter = 0;

		boolean alternateSort = (colCount > 3 ? true : false);

		HashMap<String, Object> values = new HashMap<String, Object>();
		Object[] xa;
		Object[] ya;
		if (alternateSort) { // name1, altSort1, name2, altSort2, value
			TreeMap<Object, Object> x = new TreeMap<Object, Object>(); // allows a sorted toArray (or Iterator())
			TreeMap<Object, Object> y = new TreeMap<Object, Object>();

			// Scroll resultset and feed data structures
			// to read it as a crosstab (pivot)
			while (rs.next()) {
				Object DyVal = rs.getObject(1);
				Object Dy = rs.getObject(2);
				Object DxVal = rs.getObject(3);
				Object Dx = rs.getObject(4);
				x.put(Dx, DxVal);
				y.put(Dy, DyVal);
				addValue(Dy.toString() + "-" + Dx.toString(), values, rs, 5, columnType);
			}

			xa = x.keySet().toArray();
			ya = y.keySet().toArray();

			o.setColumnsNumber(xa.length + 1);
			o.beginHeader();
			o.addHeaderCell(rsmd.getColumnLabel(5) + " (" + rsmd.getColumnLabel(1) + " / " + rsmd.getColumnLabel(3) + ")");
			int i, j;
			for (i = 0; i < xa.length; i++) {
				o.addHeaderCell(x.get(xa[i]).toString());
			}
			o.endHeader();
			o.beginLines();

			//  _ Jan Feb Mar
			for (j = 0; j < ya.length; j++) {
				if (!o.newLine()) {
					result.setMessage("reports.message.tooManyRows");
					return result;
				}
				Object Dy = ya[j];
				//o.addHeaderCell(y.get(Dy).toString()); //column 1 data displayed as a header
				o.addHeaderCellLeft(y.get(Dy).toString()); //column 1 data displayed as a header
				for (i = 0; i < xa.length; i++) {
					Object value = values.get(Dy.toString() + "-" + xa[i].toString());
					addCell(o, value, columnType);
				}
				counter++;
			}

		} else {
			TreeSet<Object> x = new TreeSet<Object>(); // allows a sorted toArray (or Iterator())
			TreeSet<Object> y = new TreeSet<Object>();

			// Scroll resultset and feed data structures
			// to read it as a crosstab (pivot)
			while (rs.next()) {
				Object Dy = rs.getObject(1);
				Object Dx = rs.getObject(2);
				x.add(Dx);
				y.add(Dy);
				addValue(Dy.toString() + "-" + Dx.toString(), values, rs, 3, columnType);
			}

			xa = x.toArray();
			ya = y.toArray();

			o.setColumnsNumber(xa.length + 1);
			o.beginHeader();
			o.addHeaderCell(rsmd.getColumnLabel(3) + " (" + rsmd.getColumnLabel(1) + " / " + rsmd.getColumnLabel(2) + ")");
			int i, j;
			for (i = 0; i < xa.length; i++) {
				o.addHeaderCell(xa[i].toString());
			}

			o.endHeader();
			o.beginLines();

			//  _ Jan Feb Mar
			for (j = 0; j < ya.length; j++) {
				if (!o.newLine()) {
					result.setMessage("reports.message.tooManyRows");
					return result;
				}
				Object Dy = ya[j];
				//o.addHeaderCell(Dy.toString()); //column 1 data displayed as a header
				o.addHeaderCellLeft(Dy.toString()); //column 1 data displayed as a header
				for (i = 0; i < xa.length; i++) {
					Object value = values.get(Dy.toString() + "-" + xa[i].toString());
					addCell(o, value, columnType);
				}
				counter++;
			}
		}

		o.endLines();

		result.setSuccess(true);
		result.setData(counter);
		return result;
	}

	/**
	 * Used to call the right method on ReportOutputInterface when flushing
	 * values (flushXOutput)
	 */
	private static void addCell(ReportOutputInterface o, Object value, int columnType) {

		if (columnType == 0) {
			o.addCellString((String) value);
		} else if (columnType == 1) {
			o.addCellDouble((Double) value);
		} else if (columnType == 2) {
			o.addCellLong((Long) value);
		} else if (columnType == 3) {
			o.addCellDate((java.util.Date) value);
		} else if (columnType == 4) {
			if (value == null) {
				o.addCellString(null);
			} else {
				java.sql.Clob clob = (java.sql.Clob) value;
				try {
					o.addCellString(clob.getSubString(1, (int) clob.length()));
				} catch (SQLException e) {
					logger.error("Error", e);
					o.addCellString("Error getting CLOB: " + e);
				}
			}
		} else {
			o.addCellString((String) value);
		}
	}

	/**
	 * Used to cast and store the right object type in the Hashmap used by
	 * flushXOutput to cache sorted values
	 */
	private static void addValue(String key, Map<String, Object> values, ResultSet rs,
			int index, int columnType) throws SQLException {
		//values.put((String) Dy +"-"+ (String) Dx , new Double(rs.getDouble(3)) );
		if (columnType == 0) {
			values.put(key, rs.getString(index));
		} else if (columnType == 1) {
			values.put(key, Double.valueOf(rs.getDouble(index)));
		} else if (columnType == 2) {
			values.put(key, Long.valueOf(rs.getLong(index)));
		} else if (columnType == 3) {
			values.put(key, rs.getTimestamp(index));
		} else if (columnType == 4) {
			values.put(key, rs.getClob(index));
		} else {
			values.put(key, rs.getString(index));
		}
	}

	private static void setTypesArray(int sqlType, int[] ta, int i) {
		switch (sqlType) {
			case Types.CHAR:
			case Types.VARCHAR:
			case Types.LONGVARCHAR:
				ta[i] = 0;
				break;
			case Types.NUMERIC:  // driver should convert this properly on getDouble
			case Types.DECIMAL:  // driver should convert this properly on getDouble
			case Types.FLOAT:
			case Types.REAL:
			case Types.DOUBLE:
				ta[i] = 1;
				break;
			case Types.INTEGER:
			case Types.TINYINT:
			case Types.SMALLINT:
			case Types.BIGINT:
				ta[i] = 2;
				break;
			case Types.DATE:
			case Types.TIMESTAMP:
				ta[i] = 3;
				break;
			case Types.CLOB:
				ta[i] = 4;
				break;
			default:
				ta[i] = 0;
		}
	}

//	public static void displayParameters(PrintWriter out, Map<Integer, ArtQueryParam> displayParams) {
//		displayParameters(out, displayParams, null);
//	}

	/**
	 * Display parameters for html view modes
	 *
	 * @param out
	 * @param displayParams
	 * @param allParametersText string to be displayed for multi parameter where
	 * "All" was selected
	 */
	public static void displayParameters(PrintWriter out,
			Map<Integer, ArtQueryParam> displayParams, String allParametersText) {

		// display parameters if they are available
		if (displayParams != null && !displayParams.isEmpty() && out != null) {
			out.println("<div align=\"center\">");
			out.println("<table border=\"0\" width=\"90%\"><tr><td>");
			out.println("<div id=\"param_div\" width=\"90%\" align=\"center\" class=\"qeparams\">");

			for (Map.Entry<Integer, ArtQueryParam> entry : displayParams.entrySet()) {
				ArtQueryParam param = entry.getValue();
				String paramName = param.getName();
				Object pValue = param.getParamValue();
				String outputString;

				if (pValue == null) {
					//multi parameter with all selected
					outputString = paramName + ": " + allParametersText + " <br> ";
					out.println(outputString);
				} else if (pValue instanceof String) {
					String paramValue = (String) pValue;
					outputString = paramName + ": " + paramValue + " <br> "; //default to displaying parameter value

					if (param.usesLov()) {
						//for lov parameters, show both parameter value and display string if any
						Map<String, String> lov = param.getLovValues();
						if (lov != null) {
							//get friendly/display string for this value
							String paramDisplayString = lov.get(paramValue);
							if (!StringUtils.equals(paramValue, paramDisplayString)) {
								//parameter value and display string differ. show both
								outputString = paramName + ": " + paramDisplayString + " (" + paramValue + ") <br> ";
							}
						}
					}
					out.println(outputString);
				} else if (pValue instanceof String[]) { // multi
					String[] paramValues = (String[]) pValue;
					outputString = paramName + ": " + StringUtils.join(paramValues, ", ") + " <br> "; //default to showing parameter values only

					if (param.usesLov()) {
						//for lov parameters, show both parameter value and display string if any
						Map<String, String> lov = param.getLovValues();
						if (lov != null) {
							//get friendly/display string for all the parameter values
							String[] paramDisplayStrings = new String[paramValues.length];
							for (int i = 0; i < paramValues.length; i++) {
								String value = paramValues[i];
								String display = lov.get(value);
								if (!StringUtils.equals(display, value)) {
									//parameter value and display string differ. show both
									paramDisplayStrings[i] = display + " (" + value + ")";
								} else {
									paramDisplayStrings[i] = value;
								}
							}
							outputString = paramName + ": " + StringUtils.join(paramDisplayStrings, ", ") + " <br> ";
						}
					}
					out.println(outputString);
				}
			}
			out.println("</div>");
			out.println("</td></tr></table>");
			out.println("</div>");
		}
	}

		/**
	 * Generate a group report
	 *
	 * @param out
	 * @param rs needs to be a scrollable resultset
	 * @param splitCol
	 * @return number of rows output
	 * @throws SQLException
	 */
	public static int generateGroupReport(PrintWriter out, ResultSet rs, int splitCol) throws SQLException {
		
		ResultSetMetaData rsmd = rs.getMetaData();
		
		int colCount = rsmd.getColumnCount();
		int i;
		int counter = 0;
		GroupHtmlOutput o = new GroupHtmlOutput(out);
		String tmpstr;
		StringBuffer cmpStr; // temporary string used to compare values
		StringBuffer tmpCmpStr; // temporary string used to compare values

		// Report, is intended to be something like that:
		/*
		 * ------------------------------------- | Attr1 | Attr2 | Attr3 | //
		 * Main header ------------------------------------- | Value1 | Value2 |
		 * Value3 | // Main Data -------------------------------------
		 *
		 * -----------------------------... | SubAttr1 | Subattr2 |... // Sub
		 * Header -----------------------------... | SubValue1.1 | SubValue1.2
		 * |... // Sub Data -----------------------------... | SubValue2.1 |
		 * SubValue2.2 |... -----------------------------...
		 * ................................ ................................
		 * ................................
		 *
		 * etc...
		 */
		// Build main header HTML
		for (i = 0; i < (splitCol); i++) {
			tmpstr = rsmd.getColumnLabel(i + 1);
			o.addCellToMainHeader(tmpstr);
		}
		// Now the header is completed

		// Build the Sub Header
		for (; i < colCount; i++) {
			tmpstr = rsmd.getColumnLabel(i + 1);
			o.addCellToSubHeader(tmpstr);
		}

		int maxRows = Config.getMaxRows("htmlreport");

		while (rs.next() && counter < maxRows) {
			// Separators
			out.println("<br><hr style=\"width:90%;height:1px\"><br>");

			// Output Main Header and Main Data
			o.header(90);
			o.printMainHeader();
			o.beginLines();
			cmpStr = new StringBuffer();

			// Output Main Data (only one row, obviously)
			for (i = 0; i < splitCol; i++) {
				o.addCellToLine(rs.getString(i + 1));
				cmpStr.append(rs.getString(i + 1));
			}
			o.endLines();
			o.footer();

			// Output Sub Header and Sub Data
			o.header(80);
			o.printSubHeader();
			o.beginLines();

			// Output Sub Data (first line)
			for (; i < colCount; i++) {
				o.addCellToLine(rs.getString(i + 1));
			}

			boolean currentMain = true;
			while (currentMain && counter < maxRows) {  // next line
				// Get Main Data in order to compare it
				if (rs.next()) {
					counter++;
					tmpCmpStr = new StringBuffer();

					for (i = 0; i < splitCol; i++) {
						tmpCmpStr.append(rs.getString(i + 1));
					}

					if (tmpCmpStr.toString().equals(cmpStr.toString()) == true) { // same Main
						o.newLine();
						// Add data lines
						for (; i < colCount; i++) {
							o.addCellToLine(rs.getString(i + 1));
						}
					} else {
						o.endLines();
						o.footer();
						currentMain = false;
						rs.previous();
					}
				} else {
					currentMain = false;
					// The outer and inner while will exit
				}
			}
		}

		if (!(counter < maxRows)) {
			o.newLine();
			o.addCellToLine("<blink>Too many rows (>" + maxRows
					+ "). Data not completed. Please narrow your search.</blink>", "qeattr", "left", colCount);
		}

		o.endLines();
		o.footer();

		return counter + 1; // number of rows
	}
}
