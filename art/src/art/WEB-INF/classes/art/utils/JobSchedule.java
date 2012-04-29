package art.utils;

import art.servlets.ArtDBCP;

import java.sql.*;
import java.util.*;
import java.text.Collator; //for ordering of strings
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class to use to save job schedule details for later reuse
 * 
 * @author Timothy Anyona
 */
public class JobSchedule{

	final static Logger logger = LoggerFactory.getLogger(JobSchedule.class);
	
	String minute="0";
	String hour="3";
	String day="1";
	String month="*";
	String weekday="?";	
	String scheduleName="";
	
	
    /**
     * 
     */
    public JobSchedule(){
	}
	
    /**
     * 
     * @param value
     */
    public void setScheduleName(String value){
		scheduleName=value;
	}
	
    /**
     * 
     * @return schedule name
     */
    public String getScheduleName(){
		return scheduleName;
	}
	
    /**
     * 
     * @param value
     */
    public void setMinute(String value) {
		minute=value;
	}

    /**
     * 
     * @param value
     */
    public void setHour(String value) {
		hour=value;
	}

    /**
     * 
     * @param value
     */
    public void setDay(String value) {
		day=value;
	}

    /**
     * 
     * @param value
     */
    public void setWeekday(String value) {
		weekday=value;
	}
	
    /**
     * 
     * @param value
     */
    public void setMonth(String value) {
		month=value;
	}
	
    /**
     * 
     * @return minute that job should run
     */
    public String getMinute() {
		return minute ;
	}

    /**
     * 
     * @return hour that job should run
     */
    public String getHour() {
		return hour ;
	}

    /**
     * 
     * @return day of the month that job should run
     */
    public String getDay() {
		return day ;
	}

    /**
     * 
     * @return weekday that job should run
     */
    public String getWeekday() {
		return weekday ;
	}
	
    /**
     * 
     * @return month that job should run
     */
    public String getMonth() {
		return month ;
	}
	
	
    /**
     * Check if a schedule exists
     * 
     * @param name
     * @return <code>true</code> if schedule exists
     */
    public boolean exists(String name){
		boolean scheduleExists=false;
		Connection conn=null;
		String sqlQuery;
		
		try{
			conn = ArtDBCP.getConnection();
			sqlQuery = "SELECT AJS.JOB_MINUTE "+
			" FROM ART_JOB_SCHEDULES AJS " +
			" WHERE AJS.SCHEDULE_NAME = ?";      
			
			PreparedStatement ps = conn.prepareStatement(sqlQuery);
			ps.setString(1,name);
			
			ResultSet rs = ps.executeQuery();
			if(rs.next()){
				scheduleExists=true;
			}
			ps.close();
			rs.close();
		} catch(Exception e){
			logger.error("Error",e);
		} finally {
			try {
				if (conn != null){
					conn.close();
				}
			} catch(Exception e) {
				logger.error("Error",e);
			}
		}
		
		return scheduleExists;
	}
	
	
    /**
     * Insert new schedule
     * 
     * @return <code>true</code> if successful
     */
    public boolean insert(){
		boolean success=false;
		Connection conn=null;
		String sqlQuery;
		
		try{
			conn = ArtDBCP.getConnection();
			sqlQuery = "INSERT INTO ART_JOB_SCHEDULES (SCHEDULE_NAME, JOB_MINUTE, JOB_HOUR, JOB_DAY, JOB_MONTH, JOB_WEEKDAY) VALUES (?, ?, ?, ?, ?, ?)";     
			
			PreparedStatement ps = conn.prepareStatement(sqlQuery);
			ps.setString(1,scheduleName);
			ps.setString(2,minute);
			ps.setString(3,hour);
			ps.setString(4,day);
			ps.setString(5,month);
			ps.setString(6,weekday);
			
			ps.executeUpdate();			
			ps.close();
			
			success=true;
		} catch(Exception e){
			logger.error("Error",e);
		} finally {
			try {
				if (conn != null){
					conn.close();
				}
			} catch(Exception e) {
				logger.error("Error",e);
			}
		}
		
		return success;
	}
	
	
    /**
     * Update schedule
     * 
     * @return <code>true</code> if successful
     */
    public boolean update(){
		boolean success=false;
		Connection conn=null;
				
		try{
			conn = ArtDBCP.getConnection();
			String sql = "UPDATE ART_JOB_SCHEDULES SET JOB_MINUTE = ? , JOB_HOUR = ? , JOB_DAY = ? , JOB_MONTH = ? , JOB_WEEKDAY = ? " +		  
			" WHERE SCHEDULE_NAME = ? ";
			
			PreparedStatement ps = conn.prepareStatement(sql);
			ps.setString(1,minute);
			ps.setString(2,hour);
			ps.setString(3,day);
			ps.setString(4,month);
			ps.setString(5,weekday);	    
			ps.setString(6,scheduleName);
			
			ps.executeUpdate();
			ps.close();
			
			success=true;
		} catch(Exception e){
			logger.error("Error",e);
		} finally {
			try {
				if (conn != null){
					conn.close();
				}
			} catch(Exception e) {
				logger.error("Error",e);
			}
		}
		
		return success;
	}
	
	
    /**
     * Get all schedule names
     * 
     * @return all schedule names
     */
    public List<String> getAllScheduleNames(){
		List<String> names = new ArrayList<String>();
		
		Connection conn=null;
				
		try{
			conn = ArtDBCP.getConnection();
			
			String sql = "SELECT AJS.SCHEDULE_NAME "+
			" FROM ART_JOB_SCHEDULES AJS";
			
			PreparedStatement ps = conn.prepareStatement(sql);						
			ResultSet rs = ps.executeQuery();
			while(rs.next()){
				names.add(rs.getString("SCHEDULE_NAME"));
			}
			ps.close();
			rs.close();
		} catch(Exception e){
			logger.error("Error",e);
		} finally {
			try {
				if (conn != null){
					conn.close();
				}
			} catch(Exception e) {
				logger.error("Error",e);
			}
		}
		
		//sort names
		Collator stringCollator = Collator.getInstance();
		stringCollator.setStrength(Collator.TERTIARY); //order by case
		Collections.sort(names,stringCollator);
		
		return names;
	}
		
    /**
     * Delete a schedule
     * 
     * @param name
     * @return <code>true</code> if successful
     */
    public boolean delete(String name){
		boolean deleted=false;
		
		Connection conn=null;
				
		try{
			conn = ArtDBCP.getConnection();
			
			String sql = "DELETE FROM ART_JOB_SCHEDULES " +
			" WHERE SCHEDULE_NAME = ?";				
			PreparedStatement ps = conn.prepareStatement(sql);	     	 

			ps.setString(1,name);
			ps.executeUpdate();
			ps.close();			
			deleted=true;
		} catch(Exception e){
			logger.error("Error",e);
		} finally {
			try {
				if (conn != null){
					conn.close();
				}
			} catch(Exception e) {
				logger.error("Error",e);
			}
		}
		
		return deleted;
	}
	
	
    /**
     * Populate the schedule object with details of the given schedule name
     * 
     * @param name
     * @return <code>true</code> if successful
     */
    public boolean load(String name){
		boolean success=false;
		
		Connection conn=null;
				
		try{
			conn = ArtDBCP.getConnection();
			
			String sql = "SELECT SCHEDULE_NAME, JOB_MINUTE, JOB_HOUR, JOB_DAY, JOB_MONTH, JOB_WEEKDAY " + 
			" FROM ART_JOB_SCHEDULES " +
			" WHERE SCHEDULE_NAME = ?";				
			PreparedStatement ps = conn.prepareStatement(sql);	     	 

			ps.setString(1,name);
			ResultSet rs=ps.executeQuery();
			if(rs.next()){
				scheduleName=rs.getString("SCHEDULE_NAME");
				minute=rs.getString("JOB_MINUTE");
				hour=rs.getString("JOB_HOUR");
				day=rs.getString("JOB_DAY");
				month=rs.getString("JOB_MONTH");
				weekday=rs.getString("JOB_WEEKDAY");
			}
			rs.close();
			ps.close();			
			success=true;
		} catch(Exception e){
			logger.error("Error",e);
		} finally {
			try {
				if (conn != null){
					conn.close();
				}
			} catch(Exception e) {
				logger.error("Error",e);
			}
		}
		
		return success;
	}
	
}
