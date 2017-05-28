package net.sf.jpivotart.jpivot.table;


import net.sf.jpivotart.jpivot.core.ModelChangeListener;
import net.sf.jpivotart.jpivot.olap.model.Displayable;
import net.sf.wcfart.wcf.controller.RequestContext;
import net.sf.wcfart.wcf.controller.RequestListener;
import net.sf.jpivotart.jpivot.table.SpanBuilder.SBContext;

/**
 * creates a hyperlink or popup menu on members in the table. When the user
 * clicks on a member, the member will be made available in some way (url parameter or
 * session attribute) and may forward to another jsp.
 * 
 * @author av
 * @since 15.12.2004
 */
public interface ClickableMember extends RequestListener, ModelChangeListener {
  void startRendering(RequestContext context, TableComponent table);
  void decorate(SBContext sbctx, Displayable obj);
  void stopRendering();
}
