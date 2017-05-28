package net.sf.wcfart.tbutils.res;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;

/**
 * Dummy that does not know any key/values
 */
public class NullResourceProvider implements ResourceProvider {

  public String getString(String key) {
    return null;
  }

  public Set<Object> keySet() {
    return Collections.emptySet();
  }

  public void close() {
  }

  public void dump(Dumper d) {
    d.dump(this);
  }
  public String getName() {
    return "Empty Provider";
  }
}
