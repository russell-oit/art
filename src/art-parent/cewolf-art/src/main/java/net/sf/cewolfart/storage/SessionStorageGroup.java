
package net.sf.cewolfart.storage;

import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * A container for any images for an user session. This is needed to ensure
 * that one user can not access other users' images.
 * @author brianf
 * @author zluspai
 */
public class SessionStorageGroup {

  // map contains chartId->SessionStorageItem mappings
  private Map<String, SessionStorageItem> map = new HashMap<String, SessionStorageItem>();
  
  /**
   * Constructor registers this storage group for the 
   *
   */
  public SessionStorageGroup() {
	  // register this storage group in the cleanup thread
	  StorageCleaner.getInstance().addStorageGroup(this);
  }

  /**
   * Get the storage for a chart
   * @param chartId The id of a chart
   * @return The storage
   */
  public synchronized SessionStorageItem get(Object chartId)
  {
    return map.get(chartId);
  }

  /**
   * Add a chart to the storage
   * @param chartId The id
   * @param item The storage item
   */
  public synchronized void put( String chartId, SessionStorageItem item)
  {
    map.put(chartId, item);
  }
  
  /**
   * Remove one chart item.
   * @param chartId
   */
  public synchronized void remove(String chartId) {
	  map.remove(chartId);
  }

	/**
	 * Clean up (remove) all expired charts from this storage group.
	 */
	protected synchronized void cleanup() {
		Date now = new Date();

		Iterator<String> iter = map.keySet().iterator();
		while (iter.hasNext()) {
			// System.out.println("Get Next");
			String cid = iter.next();
			SessionStorageItem ssi = get(cid);
			if (ssi.isExpired(now)) {
				// System.out.println("Removing " + ssi);
				iter.remove();
			}
		}
	}

	/**
	 * If the storage group is empty
	 * @return If empty
	 */
	public boolean isEmpty() {
		return map.isEmpty();
	}
  
}

/**
 * Single (singleton) thread (well,...almost) to clean up many storage groups
 * @author zluspai
 *
 */
class StorageCleaner implements Runnable {

	  // storage groups are stored in weak references, so they go away when the session goes away
	  private WeakHashMap<SessionStorageGroup,Object> storageGroups = new WeakHashMap<SessionStorageGroup,Object>();
	  
	  // the runner thread
	  private Thread runner;

	  private static StorageCleaner INSTANCE = new StorageCleaner();

	  // singleton
	  private StorageCleaner() {
	  }

	  /**
	   * Get sole instance
	   * @return The singleton.
	   */
	  public static StorageCleaner getInstance() {
		  return INSTANCE;
	  }
	  
	  /**
	   * Register a storage group to be automatically cleaned up.
	   * @param group The group
	   */
	  public synchronized void addStorageGroup(SessionStorageGroup group) {
		  //make method synchronized to prevent errors when starting
		  //https://stackoverflow.com/questions/7315941/java-lang-illegalthreadstateexception
		  this.storageGroups.put(group, null);
		  // start the thread if needed
		  start();
	  }

	  /**
	   * Start the runner thread for this cleanup class, if not running yet.
	   */
	  private void start()
	  {
		// start a new thread if our thread is not running
	    if (!isRunning())
	    {
	      runner = new Thread(this);
	      runner.setDaemon(true);
	      runner.setName("Cewolf-SessionCleanup");
	      runner.setPriority(Thread.MIN_PRIORITY);
	      runner.start();
	    }
	  }
	  
	 /**
	  * If the cleanup thread is currently running.
	  * @return True if running
	  */
	 boolean isRunning() {
		 return ( runner != null && runner.isAlive() );
	 }

	/**
	 * Clean up the known storage groups
	 */
	public void run() {		
        try {
			// note my thread will stop if there are no more groups to clean up
			while (! storageGroups.keySet().isEmpty()) {
				// clean up all session groups
				for (SessionStorageGroup ssg : storageGroups.keySet()) {
					// delegate the task to the storage group to clean up itself
					ssg.cleanup();
				}
		        // wait a bit
				synchronized (this) {
					wait(1000);
				}
			}
		} catch (InterruptedException e) {
			// Thread interrupted, exit...
		}
	}
}
