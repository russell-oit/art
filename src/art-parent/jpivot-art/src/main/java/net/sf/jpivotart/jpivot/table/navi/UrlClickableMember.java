/*
 * Copyright (c) 1971-2003 TONBELLER AG, Bensheim.
 * All rights reserved.
 */
package net.sf.jpivotart.jpivot.table.navi;

import java.text.MessageFormat;

import net.sf.jpivotart.jpivot.core.ModelChangeEvent;
import net.sf.jpivotart.jpivot.olap.model.Displayable;
import net.sf.jpivotart.jpivot.olap.model.Member;
import net.sf.jpivotart.jpivot.table.SpanBuilder.SBContext;
import net.sf.wcfart.wcf.charset.CharsetFilter;
import net.sf.wcfart.wcf.controller.RequestContext;

/**
 * creates a hyperlink in the table with a specified url. The URL may contain
 * the unique name of the member that belongs to the hyperlink.
 * 
 * @author av
 * @since Mar 27, 2006
 */
public class UrlClickableMember extends AbstractClickableMember {
  /** 
   * urlPattern contains {0} which is replaced with the unique name 
   * of the member
   */
  private String urlPattern;
  private String menuLabel;

  /**
   * @param uniqueName name of level, hierarchy, dimension that shall be clickable
   * 
   * @param urlPattern any url. {0} will be replaced with the unique name of the
   * selected member
   */
  
  protected UrlClickableMember(String uniqueName, String menuLabel, String urlPattern) {
    super(uniqueName);
    this.menuLabel = menuLabel;
    this.urlPattern = urlPattern;
  }

  /**
   * unique name in url
   */
  private String getPatternUrl(Member member) {
    String pattern = urlPattern == null ? "?param={0}" : urlPattern;
    String uname = CharsetFilter.urlEncode(parser.unparse(member));
    Object[] args = new Object[] { uname};
    return MessageFormat.format(pattern, args);
  }

  public void decorate(SBContext sbctx, Displayable obj) {
    if (!(obj instanceof Member))
      return;

    Member m = (Member) obj;
    if (match(m)) {
      sbctx.addClickable(getPatternUrl(m), menuLabel);
    }
  }

  /**
   * ignore
   */
  public void request(RequestContext context) throws Exception {
  }

  /**
   * ignore
   */
  public void modelChanged(ModelChangeEvent e) {
  }

  /**
   * ignore
   */
  public void structureChanged(ModelChangeEvent e) {
  }

}
