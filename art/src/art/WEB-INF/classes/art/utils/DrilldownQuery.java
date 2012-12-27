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
import java.sql.*;
import java.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * class that represents a drill down query and related methods
 * 
 */
public class DrilldownQuery  {
	
	final static Logger logger = LoggerFactory.getLogger(DrilldownQuery.class);

	int queryId;
	int drilldownQueryId;
	int queryPosition=-1;
	String drilldownQueryName;
	String drilldownTitle;
	String drilldownText;
	String outputFormat;
	String openInNewWindow;
	
	List<ArtQueryParam> drilldownParams; //to store drill down parameters

    /**
     * 
     */
    public DrilldownQuery() {		
		drilldownParams=new ArrayList<ArtQueryParam>(); 
	}
	
    /**
     * 
     * @return whether to open drill down query in a new window
     */
    public String getOpenInNewWindow(){
		return openInNewWindow;
	}
	
    /**
     * 
     * @param value
     */
    public void setOpenInNewWindow(String value){
		openInNewWindow=value;
	}
	
    /**
     * 
     * @return list of drill down parameters
     */
    public List<ArtQueryParam> getDrilldownParams(){
		return drilldownParams;
	}
		
    /**
     * 
     * @return drill down query name
     */
    public String getDrilldownQueryName(){
		return drilldownQueryName;
	}
	
    /**
     * 
     * @param value
     */
    public void setDrilldownQueryName(String value){
		drilldownQueryName=value;
	}
	
    /**
     * 
     * @return drill down column title
     */
    public String getDrilldownTitle(){
		return drilldownTitle;
	}
	
    /**
     * 
     * @param value
     */
    public void setDrilldownTitle(String value){
		drilldownTitle=value;
	}
	
    /**
     * 
     * @return drill down text
     */
    public String getDrilldownText(){
		return drilldownText;
	}
	
    /**
     * 
     * @param value
     */
    public void setDrilldownText(String value){
		drilldownText=value;
	}
	
    /**
     * 
     * @return output format
     */
    public String getOutputFormat(){
		return outputFormat;
	}
	
    /**
     * 
     * @param value
     */
    public void setOutputFormat(String value){
		outputFormat=value;
	}
	
    /**
     * 
     * @param value
     */
    public void setQueryId(int value){
		queryId=value;
	}
	
    /**
     * 
     * @return parent query id
     */
    public int getQueryId(){
		return queryId;
	}
	
    /**
     * 
     * @param value
     */
    public void setDrilldownQueryId(int value){
		drilldownQueryId=value;
	}
	
    /**
     * 
     * @return drill down query id
     */
    public int getDrilldownQueryId(){
		return drilldownQueryId;
	}
	
    /**
     * 
     * @param value
     */
    public void setQueryPosition(int value){
		queryPosition=value;
	}
	
    /**
     * 
     * @return query position
     */
    public int getQueryPosition(){
		return queryPosition;
	}
	
		
    /**
     * Load object with values for a given drill down query
     * 
     * @param qId
     * @param position
     * @return <code>true</code> if successful
     */
    public boolean create(int qId,int position) {
		boolean success=false;
		
		Connection conn=null;
		
		try{
			conn = ArtDBCP.getConnection();
			success=create(conn,qId,position);		
		} catch (Exception e) {
			logger.error("Error",e);
		} finally {
			try{
				if(conn!=null){
					conn.close();
				}
			} catch(SQLException e) {
				logger.error("Error",e);
			}
		}
		
		return success;
	}
	 
	
    /**
     * Create the object from an existing drill down query	
     * 
     * @param conn
     * @param qId
     * @param position
     * @return <code>true</code> if successful
     */
    public boolean create(Connection conn, int qId, int position) {
		boolean success=false;
		
		try {
			String SQL = "SELECT ADQ.QUERY_ID, ADQ.DRILLDOWN_QUERY_ID, ADQ.DRILLDOWN_QUERY_POSITION " +			
			" , ADQ.DRILLDOWN_TITLE, ADQ.DRILLDOWN_TEXT, ADQ.OUTPUT_FORMAT, AQ.NAME, ADQ.OPEN_IN_NEW_WINDOW " +
			"  FROM ART_DRILLDOWN_QUERIES ADQ, ART_QUERIES AQ "+
			" WHERE ADQ.DRILLDOWN_QUERY_ID = AQ.QUERY_ID " +
			" AND ADQ.QUERY_ID = ? AND ADQ.DRILLDOWN_QUERY_POSITION = ?";
			
			PreparedStatement ps = conn.prepareStatement(SQL);
			ps.setInt(1,qId);
			ps.setInt(2,position);
			ResultSet rs = ps.executeQuery();
			if(rs.next()) {
				queryId=rs.getInt("QUERY_ID");
				drilldownQueryId=rs.getInt("DRILLDOWN_QUERY_ID");	
				queryPosition=rs.getInt("DRILLDOWN_QUERY_POSITION");
				drilldownTitle=rs.getString("DRILLDOWN_TITLE");
				drilldownText=rs.getString("DRILLDOWN_TEXT");
				outputFormat=rs.getString("OUTPUT_FORMAT");
				drilldownQueryName=rs.getString("NAME");
				openInNewWindow=rs.getString("OPEN_IN_NEW_WINDOW");
				
				rs.close();
				ps.close();
				
				success=true;
			} else {
				logger.warn("The query id {} does not exist",queryId);				
			}	   
		} catch(SQLException e) {
			logger.error("Error. Query id {}",queryId,e);
		}
		
		return success;
	}
	
    /**
     * Update drill down query
     * 
     * @param conn
     * @return <code>true</code> if successful
     */
    public boolean update(Connection conn){
		// Delete the record if it exists and then insert a new one. because method also used by insert
		
		boolean success=false;

		try {
			String SQL;
			PreparedStatement ps;
			
			SQL = "DELETE FROM ART_DRILLDOWN_QUERIES WHERE QUERY_ID = ? AND DRILLDOWN_QUERY_POSITION = ?";
			ps = conn.prepareStatement(SQL);
			ps.setInt(1,queryId);
			ps.setInt(2,queryPosition);
			ps.executeUpdate();
			ps.close();

			SQL = "INSERT INTO ART_DRILLDOWN_QUERIES (DRILLDOWN_QUERY_ID, QUERY_ID, DRILLDOWN_QUERY_POSITION, DRILLDOWN_TITLE, DRILLDOWN_TEXT, OUTPUT_FORMAT, OPEN_IN_NEW_WINDOW) " +						
			" VALUES (?, ?, ?, ?, ?, ?, ?)";
			ps = conn.prepareStatement(SQL);
			
			ps.setInt(1,drilldownQueryId);		  
			ps.setInt(2,queryId);		  
			ps.setInt(3,queryPosition);	  
			ps.setString(4,drilldownTitle);
			ps.setString(5,drilldownText);
			ps.setString(6,outputFormat);
			ps.setString(7,openInNewWindow);
						
			ps.executeUpdate();
			ps.close();
			
			success=true;
		} catch(SQLException e) {
			logger.error("Error. Query id {}",queryId,e);
		}
		
		return success;
	}
	
    /**
     * Create new drill down query 
     * 
     * @param conn
     * @return <code>true</code> if successful
     */
    public boolean insert(Connection conn){
		// Get new Position and call update to create the record
		try {
			String SQL;
			PreparedStatement ps;
			ResultSet rs;
			
			SQL = "SELECT MAX(DRILLDOWN_QUERY_POSITION) FROM ART_DRILLDOWN_QUERIES WHERE QUERY_ID = ?";
			ps = conn.prepareStatement(SQL);
			ps.setInt(1,queryId);
			rs = ps.executeQuery();
			if(rs.next()){
				queryPosition=1+rs.getInt(1);
			}
			rs.close();
			ps.close();
		} catch (Exception e) {
			logger.error("Error. Query id {}",queryId,e);
			return false;
		}
		return update(conn);   
	}
	
    /**
     * Delete the drill down query association
     * 
     * @param conn
     * @return <code>true</code> if successful
     */
    public boolean delete(Connection conn){	
		boolean success=false;
		
		try {
			String SQL;
			PreparedStatement ps;
			
			// Delete drill down record
			SQL = "DELETE FROM ART_DRILLDOWN_QUERIES WHERE QUERY_ID = ? AND DRILLDOWN_QUERY_POSITION = ?";
			ps = conn.prepareStatement(SQL);
			ps.setInt(1,queryId);
			ps.setInt(2,queryPosition);
			ps.executeUpdate();
			ps.close();
						
			//update the position for subsequent drill down queries
			SQL = "UPDATE ART_DRILLDOWN_QUERIES SET DRILLDOWN_QUERY_POSITION = (DRILLDOWN_QUERY_POSITION-1) " +
			" WHERE DRILLDOWN_QUERY_POSITION > ? AND QUERY_ID = ?";
			
			ps = conn.prepareStatement(SQL);
			ps.setInt(1,queryPosition);
			ps.setInt(2,queryId);
			ps.executeUpdate();
			ps.close();
			
			success=true;
		} catch(SQLException e) {
			logger.error("Error. Query id {}",queryId,e);
		}
		
		return success;
	}
	
    /**
     * Move the drill down query up one position  
     * 
     * @param conn
     * @return <code>true</code> if successful
     */
    public boolean moveUp(Connection conn){		
		if (queryPosition <= 1) {
			// it is already on top
			return true; 
		}
		
		boolean success=false;

		try {
			String SQL;
			PreparedStatement ps;
			
			SQL = "UPDATE ART_DRILLDOWN_QUERIES SET DRILLDOWN_QUERY_POSITION = ? WHERE DRILLDOWN_QUERY_POSITION = ? AND QUERY_ID = ?";
			ps = conn.prepareStatement(SQL);

			// Assign to *this* query the "swap" position 0
			ps.setInt(1,0);
			ps.setInt(2,queryPosition);
			ps.setInt(3,queryId);
			ps.addBatch();

			// Move down the previous one
			ps.setInt(1,queryPosition );
			ps.setInt(2,(queryPosition-1) );
			ps.setInt(3,queryId);
			ps.addBatch();

			// Assign the right position to *this* query
			ps.setInt(1,(queryPosition-1));
			ps.setInt(2,0);
			ps.setInt(3,queryId);
			ps.addBatch();
			
			ps.executeBatch();
			ps.close();

			success=true;
		} catch(SQLException e) {
			logger.error("Error. Query id {}",queryId,e);
		}
		
		return success;
	}
		
	
    /**
     * Build and store drill down parameters list
     */
    public void buildDrilldownParams(){
		Connection conn = null;
		
		try{
			conn = ArtDBCP.getConnection();
			
			String sql;
			PreparedStatement ps;
			ResultSet rs;
			
			sql = "SELECT FIELD_POSITION " +			
			" FROM ART_QUERY_FIELDS " +
			" WHERE QUERY_ID = ? AND DRILLDOWN_COLUMN > 0 AND PARAM_TYPE = 'I'";
			
			ps=conn.prepareStatement(sql);
			ps.setInt(1,drilldownQueryId);
			rs=ps.executeQuery();
			while(rs.next()){
				ArtQueryParam param=new ArtQueryParam();
				param.create(conn,drilldownQueryId,rs.getInt("FIELD_POSITION"));
				drilldownParams.add(param);
			}
			rs.close();
			ps.close();
		
		} catch (Exception e) {
			logger.error("Error",e);
		} finally {
			try{
				if(conn!=null){
					conn.close();
				}
			} catch(SQLException e) {
				logger.error("Error",e);
			}
		}		
	}
}   
