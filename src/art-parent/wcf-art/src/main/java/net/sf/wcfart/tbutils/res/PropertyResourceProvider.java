package net.sf.wcfart.tbutils.res;

import java.util.Collection;
import java.util.Properties;
import java.util.Set;

/**
 * Adapter for Properties
 */

public class PropertyResourceProvider implements ResourceProvider {
  Properties props;
  String name;

  public PropertyResourceProvider(String name, Properties props) {
    this.props = props;
    this.name = name;
  }

  public String getString(String key) {
    return props.getProperty(key);
  }

  public Set<Object> keySet() {
    return props.keySet();
  }

  public void close() {
  }

  public void dump(Dumper d) {
    d.dump(this);
  }
  public String getName() {
    return name;
  }
}
