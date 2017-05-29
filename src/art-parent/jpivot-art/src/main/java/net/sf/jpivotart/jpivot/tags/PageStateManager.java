package net.sf.jpivotart.jpivot.tags;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * @author av
 * @since 15.02.2005
 */
public class PageStateManager implements StateManager {
  Map<String, State> map = new HashMap<>();
  State current;
  StateLogger logger = new Log4jStateLogger();

  void showCurrent() throws Exception {
    if (current != null) {
      logger.show(current);
      current.show();
    }
  }
  
  void hideCurrent() throws Exception {
    if (current != null) {
      logger.hide(current);
      current.hide();
    }
  }
  
  public void initializeAndShow(State next) throws Exception {
    hideCurrent();
    
    // remove state with same name from map
    State prev = map.get(next.getName());
    if (prev != null) {
      logger.destroy(prev);
      prev.destroy();
    }

    map.put(next.getName(), next);
    logger.initialize(next);
    next.initialize();
    current = next;
    showCurrent();
  }

  /**
   * makes the named state the visible one
   */
  public void showByName(String name) throws Exception {
    State s = map.get(name);
    if (s == null) {
      logger.error("could not find state for " + name);
      return;
    }
    if (current != s) {
      hideCurrent();
      current = s;
      showCurrent();
    }
  }

  /**
   * removes and destroys all states
   */
  public void destroyAll() throws Exception {
    hideCurrent();
    current = null;
    for (State s : map.values()) {
      logger.destroy(s);
      s.destroy();
    }
    map.clear();
  }

  /**
   * removes and destroys the named state
   */
  public void destroyByName(String name) throws Exception {
    State s = map.get(name);
    if (s == null) {
      logger.error("query " + name + " not found");
      return;
    }
    if (s == current) {
      hideCurrent();
      current = null;
    }
    logger.destroy(s);
    s.destroy();
    map.remove(name);
  }

  public StateLogger getLogger() {
    return logger;
  }

  public void setLogger(StateLogger logger) {
    this.logger = logger;
  }

}
