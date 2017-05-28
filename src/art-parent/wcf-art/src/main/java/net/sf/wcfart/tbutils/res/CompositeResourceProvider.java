package net.sf.wcfart.tbutils.res;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Composite ResourceProvider.
 * 
 * Contains an ordered list of {@link ResourceProvider}'s. For a given key
 * it iterates the list and returns the value of the first ResourceProvider
 * that knows about the key.
 * <br>
 */

public class CompositeResourceProvider implements ResourceProvider {
  List<ResourceProvider> list = new ArrayList<>();

  public String getString(String key) {
    for (ResourceProvider r : list) {
      String s = r.getString(key);
      if (s != null)
        return s;
    }
    return null;
  }

  public void add(ResourceProvider r) {
    list.add(r);
  }

  public void add(int index, ResourceProvider r) {
    list.add(index, r);
  }

  public List<ResourceProvider> getProviders() {
    return list;
  }

  public Set<Object> keySet() {
    Set<Object> set = new HashSet<>();
    for (ResourceProvider r : list) {
      set.addAll(r.keySet());
    }
    return set;
  }

  public void close() {
    for (ResourceProvider r : list) {
      r.close();
    }
  }

  public void dump(Dumper d) {
    for (ResourceProvider r : list) {
      r.dump(d);
    }
  }

  public String getName() {
    return "CompositeResourceProvider";
  }
}