package net.sf.wcfart.tbutils.res;

import java.util.Collection;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.Set;

/**
 * Adapter fuer ResourceBundle
 */
public class BundleResourceProvider implements ResourceProvider {
  private ResourceBundle resb;
  private String name;

  public BundleResourceProvider(String name, ResourceBundle resb) {
    this.name = name;
    this.resb = resb;
  }

  public String getString(String key) {
    try {
      return resb.getString(key);
    } catch (MissingResourceException e) {
      return null;
    }
  }

  public Set<Object> keySet() {
    Set<Object> set = new HashSet<>();
    for (Enumeration<String> en = resb.getKeys(); en.hasMoreElements();)
      set.add(en.nextElement());
    return set;
  }

  public void close() {
  }

  public String getName() {
    return "BundleResourceProvider " + name;
  }

  public void dump(Dumper d) {
    d.dump(this);
  }

}