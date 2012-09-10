/*
 * Copyright (C) 2001/2004  Enrico Liboni  - enrico@computer.org
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
/** ArtOutHandler.java
 *
 * Caller:	QueryExecute and ArtJob
 * Purpose:	Scroll the resultset and feed properly
the ArtOutputInterface 
 *
 *
 */
package art.output;

import art.utils.*;

import java.sql.*;
import java.util.*;
import java.net.URLEncoder;
import java.io.UnsupportedEncodingException;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Generate query output by scrolling the resultset and feeding the ArtOutputInterface object
 * 
 * @author Enrico Liboni
 * @author Timothy Anyona
 */
public class ArtOutHandler {
    
    final static Logger logger = LoggerFactory.getLogger(ArtOutHandler.class);
    
    
    /**
     * Flush the output as it is (row by row).
     * For output that can't have drill down queries and needs to show parameters in output
     * 
     * @param messages resourcebundle for error message translation
     * @param o output object
     * @param rs query resultset
     * @param rsmd resultset metadata     
     * @return number of rows in the resultset
     * @throws SQLException 
     * @throws ArtException 
     */
    public static int flushOutput(ResourceBundle messages, ArtOutputInterface o, ResultSet rs, ResultSetMetaData rsmd) throws SQLException, ArtException {

        return flushOutput(messages, o, rs, rsmd, null, null, null, null);
    }

    /**
     * Output query results
     * 
     * @param messages resourcebundle for error message translation
     * @param o output object
     * @param rs query resultset
     * @param rsmd resultset metadata     
     * @param drilldownQueries drill down queries
     * @param baseUrl url to art application
     * @param inlineParams inline parameters
     * @param multiParams multi parameters
     * @return number of rows in the resultset
     * @throws SQLException
     * @throws ArtException if max rows reached
     */
    public static int flushOutput(ResourceBundle messages, ArtOutputInterface o, ResultSet rs, ResultSetMetaData rsmd, Map<Integer,DrilldownQuery> drilldownQueries, String baseUrl, Map<String, String> inlineParams, Map<String, String[]> multiParams) throws SQLException, ArtException {

       
        int col_count = rsmd.getColumnCount();
        int i;
        int counter = 0;

        int columnTypes[] = new int[col_count]; // 0 = string ; 1 = numeric/double ; 2 = int ; 3 = date 
        String tmpstr;

        //include columns for drill down queries
        int drilldownCount = 0;
        if (drilldownQueries != null) {
            drilldownCount = drilldownQueries.size();
        }

        //include columns for drill down queries		
        o.setColumnsNumber(col_count + drilldownCount);

        o.beginHeader();
        for (i = 0; i < (col_count); i++) {
            tmpstr = rsmd.getColumnLabel(i + 1);
            setTypesArray(rsmd.getColumnType(i + 1), columnTypes, i);
            o.addHeaderCell(tmpstr);
        }

        //include columns for drill down queries
        String drilldownTitle;

        if (drilldownCount > 0) {
            Iterator it = drilldownQueries.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry entry = (Map.Entry) it.next();
                DrilldownQuery drilldown = (DrilldownQuery) entry.getValue();
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
        String paramString;
        String openInNewWindow;

        //allow null int or float fields to be indicated as null instead of 0
        Long longField;
        Double doubleField;

        //store parameter names so that parent parameters with the same name as in the drilldown query are omitted
        HashMap<String, String> params = new HashMap<String, String>();

        while (rs.next()) {
            if (!o.newLine()) {
                if (o instanceof xlsxOutput) {
                    throw new ArtException(messages.getString("tooManyRowsOrError"));
                } else {
                    throw new ArtException(messages.getString("tooManyRows"));
                }
            }
            for (i = 0; i < (col_count); i++) {

                switch (columnTypes[i]) {
                    case 0:
                        o.addCellString(rs.getString(i + 1));
                        break;
                    case 1:
                        doubleField = new Double(rs.getDouble(i + 1));
                        if (rs.wasNull()) {
                            doubleField = null;
                        }
                        o.addCellDouble(doubleField);
                        break;
                    case 2:
                        longField = new Long(rs.getLong(i + 1));
                        if (rs.wasNull()) {
                            longField = null;
                        }
                        o.addCellLong(longField);
                        break;
                    case 3:
                        o.addCellDate(rs.getTimestamp(i + 1)); // try always to get a timestamp, the jdbc driver should properly convert to a date...
                        break;
                    case 4:
                        // convert CLOB to string
                        java.sql.Clob clob = rs.getClob(i + 1);
                        try {
                            o.addCellString(clob.getSubString(1, (int) clob.length()));
                        } catch (SQLException e) {
                            logger.error("Error",e);
                            o.addCellString("Exception getting CLOB: " + e);
                        }
                        break;
                    default:
                        o.addCellString(rs.getString(i + 1));
                }
            }

            //display columns for drill down queries			
            if (drilldownCount > 0) {
                Iterator it = drilldownQueries.entrySet().iterator();
                while (it.hasNext()) {
                    Map.Entry entry = (Map.Entry) it.next();
                    DrilldownQuery drilldown = (DrilldownQuery) entry.getValue();
                    drilldownText = drilldown.getDrilldownText();
                    if (drilldownText == null || drilldownText.trim().length() == 0) {
                        drilldownText = "Drill Down";
                    }

                    drilldownQueryId = drilldown.getDrilldownQueryId();
                    outputFormat = drilldown.getOutputFormat();
                    if (outputFormat == null || outputFormat.toUpperCase().equals("ALL")) {
                        drilldownUrl = baseUrl + "/user/showParams.jsp?queryId=" + drilldownQueryId;
                    } else {
                        drilldownUrl = baseUrl + "/user/ExecuteQuery?queryId=" + drilldownQueryId + "&viewMode=" + outputFormat;
                    }

                    String paramLabel;
                    String paramValue;
                    drilldownParams = drilldown.getDrilldownParams();
                    if (drilldownParams != null) {
                        Iterator it2 = drilldownParams.iterator();
                        while (it2.hasNext()) {
                            ArtQueryParam param = (ArtQueryParam) it2.next();
                            paramLabel = param.getParamLabel();
                            paramValue = rs.getString(param.getDrilldownColumn());
                            try {
                                paramValue = URLEncoder.encode(paramValue, "UTF-8");
                            } catch (UnsupportedEncodingException e) {
                                logger.warn("UTF-8 encoding not supported",e);
                            }
                            paramString = "&P_" + paramLabel + "=" + paramValue;
                            drilldownUrl = drilldownUrl + paramString;
                            params.put(paramLabel, paramValue);
                        }
                    }

                    //add parameters from parent query										
                    if (inlineParams != null) {
                        Iterator itInline=inlineParams.entrySet().iterator();
                        while (itInline.hasNext()) {
                            Map.Entry entryInline = (Map.Entry) itInline.next();
                            paramLabel = (String) entryInline.getKey();
                            paramValue = (String) entryInline.getValue();
                            //add parameter only if one with a similar name doesn't already exist in the drill down parameters
                            if (!params.containsKey(paramLabel)) {                                
                                try {
                                    paramValue = URLEncoder.encode(paramValue, "UTF-8");
                                } catch (UnsupportedEncodingException e) {
                                    logger.warn("UTF-8 encoding not supported",e);
                                }
                                paramString = "&P_" + paramLabel + "=" + paramValue;
                                drilldownUrl = drilldownUrl + paramString;
                            }
                        }
                    }

                    if (multiParams != null) {
                        String[] paramValues;
                        Iterator itMulti=multiParams.entrySet().iterator();
                        while (itMulti.hasNext()) {
                            Map.Entry entryMulti = (Map.Entry) itMulti.next();
                            paramLabel = (String) entryMulti.getKey();
                            paramValues = (String[]) entryMulti.getValue();
                            for (String param : paramValues) {
                                try {
                                    param = URLEncoder.encode(param, "UTF-8");
                                } catch (UnsupportedEncodingException e) {
                                    logger.warn("UTF-8 encoding not supported",e);
                                }
                                paramString = "&M_" + paramLabel + "=" + param;
                                drilldownUrl = drilldownUrl + paramString;
                            }
                        }
                    }

                    openInNewWindow = drilldown.getOpenInNewWindow();
                    if (StringUtils.equals(openInNewWindow,"Y") || openInNewWindow == null) {
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

        return counter;
    }
   
    /**
     * Generate crosstab output.
     * 
     * @param messages resourcebundle for error message translation
     * @param o output object
     * @param rs query resultset
     * @param rsmd resultset metadata
     * @param displayParams parameters to be displayed in output
     * @return number of rows in resultset
     * @throws SQLException
     * @throws ArtException if resulset not in format for a crosstab or max rows exceeded
     */
    public static int flushXOutput(ResourceBundle messages, ArtOutputInterface o, ResultSet rs, ResultSetMetaData rsmd) throws SQLException, ArtException {

        /* input */ 		     	 /* input */
        // A Jan 14			     	  A 1 Jan 1 14
        // A Feb 24			     	  A 1 Feb 2 24
        // A Mar 34			     	  A 1 Mar 3 34
        // B Jan 14			     	  B 2 Jan 1 14
        // B Feb 24			     	  B 2 Feb 2 24
        // C Jan 04			     	  C 3 Jan 1 04
        // C Mar 44			     	  C 3 Mar 3 44
        //				     	    ^-----^------Used to sort the x/y axis

        /* output */		     	 /* output */
        //         y-axis		     	 	  y-axis	      
        //           |		     	 	    |				 
        //  x-axis - _   Feb Jan Mar     	   x-axis - _  Jan Feb Mar
        //           A    24  14  34      	 	    A	14  24  34   
        //           B    24  14  -      	 	    B	14  24   -   
        //           C    -   04  44      	 	    C	04   -  44   
        //                   ^--- Jan comes after Feb!			     	 


        
        int colCount = rsmd.getColumnCount();
        if (colCount != 3 && colCount != 5) {
            throw new ArtException(messages.getString("notACrosstab"));
        }

        // Check the data type of the value (last column)
        int columnType;
        int[] columnTypes = new int[1];
        setTypesArray(rsmd.getColumnType(colCount), columnTypes, 0);
        columnType = columnTypes[0];
        int counter = 0;

        boolean alternateSort = (colCount > 3 ? true : false);

        HashMap<String,Object> values = new HashMap<String,Object>();
        Object[] xa;
        Object[] ya;
        if (alternateSort) { // name1, altSort1, name2, altSort2, value
            TreeMap<Object,Object> x = new TreeMap<Object,Object>(); // allows a sorted toArray (or Iterator())
            TreeMap<Object,Object> y = new TreeMap<Object,Object>();

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
                    throw new ArtException(messages.getString("tooManyRows"));
                }
                Object Dy = ya[j];
                o.addHeaderCell(y.get(Dy).toString());
                for (i = 0; i < xa.length; i++) {
                    Object value = values.get(Dy.toString() + "-" + xa[i].toString());
                    if (value != null) {
                        addCell(o, value, columnType);
                    } else {
                        addCell(o, "", 0);

                    }
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
                    throw new ArtException(messages.getString("tooManyRows"));
                }
                Object Dy = ya[j];
                o.addHeaderCell(Dy.toString());
                for (i = 0; i < xa.length; i++) {
                    Object value = values.get(Dy.toString() + "-" + xa[i].toString());
                    if (value != null) {
                        addCell(o, value, columnType);
                    } else {
                        addCell(o, "", 0);
                    }
                }
                counter++;
            }
        }

        o.endLines();

        return counter;
    }

    /**
     * Used to call the right method on ArtOutputInterface
     * when flushing values (flushXOutput)
     */
    private static void addCell(ArtOutputInterface o, Object value, int columnType) {

        if (columnType == 0) {
            o.addCellString((String) value);
        } else if (columnType == 1) {
            o.addCellDouble((Double) value);
        } else if (columnType == 2) {
            o.addCellLong((Long) value);
        } else if (columnType == 3) {
            o.addCellDate((java.util.Date) value);
        } else if (columnType == 4) {
            java.sql.Clob clob = (java.sql.Clob) value;
            try {
                o.addCellString(clob.getSubString(1, (int) clob.length()));
            } catch (SQLException e) {
                logger.error("Error",e);
                o.addCellString("Exception getting CLOB: " + e);
            }
        } else {
            o.addCellString((String) value);
        }
    }

    /**
     * Used to cast and store the right object type in the Hashmap 
     * used by flushXOutput to cache sorted values
     */
    private static void addValue(String key, Map<String,Object> values, ResultSet rs,
            int index, int columnType) throws SQLException {
        //values.put((String) Dy +"-"+ (String) Dx , new Double(rs.getDouble(3)) );
        if (columnType == 0) {
            values.put(key, rs.getString(index));
        } else if (columnType == 1) {
            values.put(key, new Double(rs.getDouble(index)));
        } else if (columnType == 2) {
            values.put(key, new Long(rs.getLong(index)));
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
}
