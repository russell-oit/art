/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package art.schedule;

/**
 * Interface for providing data access methods. Use an interface in case other
 * implementations other than jdbc are used e.g. hibernate or a specific
 * database implementation e.g. a MySql Dao
 *
 * @author Timothy Anyona
 */
public interface ScheduleDao {

	void saveSchedule(Schedule schedule);

	void updateSchedule(Schedule schedule);

	void deleteSchedule(String scheduleName);

	Schedule getSchedule(String scheduleName);
}
