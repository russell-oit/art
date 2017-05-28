package net.sf.wcfart.tbutils.res;

import java.util.Collection;
import java.util.Set;

/**
 * looksup keys via System.getProperty()
 * 
 * @author av
 */
public class SystemResourceProvider implements ResourceProvider {

  public SystemResourceProvider() {
    super();
  }
  public String getString(String key) {
    return System.getProperty(key);
  }

  public Set<Object> keySet() {
    return System.getProperties().keySet();
  }
  public void close() {
  }
  
  public void dump(Dumper d) {
    d.dump(this);
  }
  public String getName() {
    return "System Properties";
  }

}
