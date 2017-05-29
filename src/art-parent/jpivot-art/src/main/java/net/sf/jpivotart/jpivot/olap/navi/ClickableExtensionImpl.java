package net.sf.jpivotart.jpivot.olap.navi;

import java.util.Collection;
import java.util.Collections;

import net.sf.jpivotart.jpivot.core.ExtensionSupport;

public class ClickableExtensionImpl extends ExtensionSupport implements ClickableExtension {
  Collection<Object> clickables = Collections.emptyList();
  public String getId() {
    return ID;
  }
  public Collection<Object> getClickables() {
    return clickables;
  }

  public void setClickables(Collection<Object> clickables) {
    this.clickables = clickables;
  }

}