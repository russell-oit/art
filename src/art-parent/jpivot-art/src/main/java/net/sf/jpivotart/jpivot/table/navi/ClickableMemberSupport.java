/*
 * ====================================================================
 * This software is subject to the terms of the Common Public License
 * Agreement, available at the following URL:
 *   http://www.opensource.org/licenses/cpl.html .
 * Copyright (C) 2003-2004 TONBELLER AG.
 * All Rights Reserved.
 * You must accept the terms of that agreement to use this software.
 * ====================================================================
 *
 * 
 */
package net.sf.jpivotart.jpivot.table.navi;

import org.apache.log4j.Logger;

import net.sf.jpivotart.jpivot.core.ModelChangeEvent;
import net.sf.jpivotart.jpivot.olap.model.Displayable;
import net.sf.jpivotart.jpivot.olap.model.Member;
import net.sf.jpivotart.jpivot.olap.model.OlapModel;
import net.sf.jpivotart.jpivot.table.TableComponent;
import net.sf.jpivotart.jpivot.table.SpanBuilder.SBContext;
import net.sf.wcfart.wcf.controller.Dispatcher;
import net.sf.wcfart.wcf.controller.DispatcherSupport;
import net.sf.wcfart.wcf.controller.RequestContext;
import net.sf.wcfart.wcf.controller.RequestListener;
import net.sf.wcfart.wcf.utils.DomUtils;

/**
 * clickable that invokes a RequestListener instead of following an URL
 * @see net.sf.jpivotart.jpivot.table.navi.UrlClickableMember
 * 
 * @author av
 */
public abstract class ClickableMemberSupport extends AbstractClickableMember {

  private static final Logger logger = Logger.getLogger(ClickableMemberSupport.class);

  protected Dispatcher dispatcher = new DispatcherSupport();
  protected OlapModel model;
  private String urlPattern;

  /**
   * label to show in popup menu
   */
  protected abstract String getMenuLabel();

  /**
   * specifies what should happen when the user clicks on the member.
   */
  protected abstract RequestListener createRequestListener(OlapModel model, Member m);

  /**
   * @param uniqueName name of level, hierarchy or dimension that shall be clickable.
   * If null, all dimensions except Measures will be clickable.
   */
  public ClickableMemberSupport(String uniqueName) {
    super(uniqueName);
  }

  public void startRendering(RequestContext context, TableComponent table) {
    this.model = table.getOlapModel();
    dispatcher.clear();
    super.startRendering(context, table);
  }

  public void stopRendering() {
    super.stopRendering();
    model = null;
  }

  private String handlerUrl(String id) {
    String pattern = urlPattern == null ? "" : urlPattern;
    char sep = '?';
    if (pattern.indexOf('?') > 0)
      sep = '&';
    return pattern + sep + id + "=x";
  }

  public void decorate(SBContext sbctx, Displayable obj) {
    if (!(obj instanceof Member))
      return;
    Member m = (Member) obj;
    if (match(m)) {
      RequestListener r = createRequestListener(model, m);
      String id = DomUtils.randomId();
      dispatcher.addRequestListener(id, null, r);
      sbctx.addClickable(handlerUrl(id), getMenuLabel());
    }
  }

  public void modelChanged(ModelChangeEvent e) {
  }

  public void structureChanged(ModelChangeEvent e) {
    dispatcher.clear();
  }

  public void request(RequestContext context) throws Exception {
    dispatcher.request(context);
  }

  /**
   * the url to generate into the hyprelink, in most cases the default null is ok
   */
  public void setUrlPattern(String urlPattern) {
    this.urlPattern = urlPattern;
  }

  public String getUrlPattern() {
    return urlPattern;
  }
}