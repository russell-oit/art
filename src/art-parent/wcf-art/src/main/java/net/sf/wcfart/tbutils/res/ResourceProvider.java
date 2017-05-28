package net.sf.wcfart.tbutils.res;

import java.util.Collection;
import java.util.Set;

/**
 * Hides various ways of accessing name/value pairs.
 */
public interface ResourceProvider {
  /** returns null if key does not exist */
  String getString(String key);

  Set<Object> keySet();
  
  /** frees resources */
  void close();
  
  /** for debugging / logging */
  void dump(Dumper d);
  /** for debugging / logging */
  String getName();
}
