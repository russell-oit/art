package net.sf.jpivotart.jpivot.olap.navi;

import java.util.Collection;

import net.sf.jpivotart.jpivot.core.Extension;
import net.sf.jpivotart.jpivot.table.ClickableMember;

public interface ClickableExtension extends Extension {
  public static final String ID = "clickable";
  /**
   * List of ClickableMember
   * @see ClickableMember
   */
  Collection<Object> getClickables();
  /**
   * List of ClickableMember
   * @see ClickableMember
   */
  void setClickables(Collection<Object> clickables);
}
