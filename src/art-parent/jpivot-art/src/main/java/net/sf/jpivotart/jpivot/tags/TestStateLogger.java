/*
 * Copyright (c) 1971-2003 TONBELLER AG, Bensheim.
 * All rights reserved.
 */
package net.sf.jpivotart.jpivot.tags;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import net.sf.jpivotart.jpivot.tags.StateManager.State;

public class TestStateLogger extends Log4jStateLogger implements StateLogger {

  Set<String> active = new TreeSet<>();
  List<String> errors = new ArrayList<>();
  
  public void initialize(State s) {
    String name = s.getName();
    if (active.contains(name)) {
      String msg = "trying to initialize state " + name + " which is already initialized";
      errors.add(msg);
      super.error(msg);
    }
    active.add(name);
    super.initialize(s);
  }
  
  public void destroy(State s) {
    String name = s.getName();
    if (!active.contains(name)) {
      String msg = "trying to destroy state " + name + " which is not initialized";
      errors.add(msg);
      super.error(msg);
    }
    active.remove(name);
    super.destroy(s);
  }
  
  public Set getActive() {
    return active;
  }

  public List getErrors() {
    return errors;
  }
}
