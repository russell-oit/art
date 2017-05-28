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
package net.sf.wcfart.wcf.toolbar;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.MissingResourceException;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import net.sf.wcfart.tbutils.res.Resources;
import net.sf.wcfart.wcf.bookmarks.Bookmarkable;
import net.sf.wcfart.wcf.component.Component;
import net.sf.wcfart.wcf.component.NestableComponentSupport;
import net.sf.wcfart.wcf.controller.RequestContext;
import net.sf.wcfart.wcf.utils.XoplonNS;
/**
 * @author av
 */
public class ToolBar extends NestableComponentSupport implements Bookmarkable {
  private static Logger logger = Logger.getLogger(ToolBar.class);
  private Resources resb;
  private List<ToolBarComponent> content = new ArrayList<>();
  private Map<String, ToolBarComponentSupport> buttonMap = new HashMap<>();
  private Map<String, List<ToolButton>> radioGroups = new HashMap<>();
  private boolean globalButtonIds = false;

  public ToolBar(String id, Component parent) {
    super(id, parent);
  }

  public void initialize(RequestContext context) throws Exception {
    super.initialize(context);
    for (ToolBarComponent cc : content) {
      cc.initialize(context, this);
    }
  }

  /**
   * renders toolbar content. Avoids consecutive separators
   */
  public Element render(RequestContext context, Document factory) throws Exception {
    Element root = XoplonNS.createElement(factory, "tool-bar");
    ToolBarComponent separator = null;
    boolean foundButton = false;
    for (ToolBarComponent cc : content) {
      // ignore invisible components
      if (!cc.isVisible(context))
        continue;
      // remember seaparator until there is a button to separate
      if (cc.isSeparator()) {
        separator = cc;
        continue;
      }
      // not a separator
      if (foundButton && separator != null)
        separator.render(context, root);
      separator = null;
      foundButton = true;
      cc.render(context, root);
    }
    return root;
  }

  public Map<String, ToolBarComponentSupport> getButtons() {
    return buttonMap;
  }

  public void addButton(ToolButton button) {
    content.add(button);
    List<ToolButton> list = getRadioGroup(button);
    if (list != null)
      list.add(button);
    buttonMap.put(button.getId(), button);
  }

  public List<ToolButton> getRadioGroup(ToolButton button) {
    String group = button.getRadioGroup();
    if (group == null || group.length() == 0)
      return null;
    List<ToolButton> list = radioGroups.get(group);
    if (list == null) {
      list = new ArrayList<>();
      radioGroups.put(group, list);
    }
    return list;
  }

  public void addSeparator(ToolSeparator separator) {
    content.add(separator);
  }

  public void addImgButton(ImgButton button) {
    content.add(button);
    buttonMap.put(button.getId(), button);
  }

  public Object retrieveBookmarkState(int levelOfDetail) {
	  @SuppressWarnings("unchecked")
    Map<String, Boolean> map = (Map<String, Boolean>) super.retrieveBookmarkState(levelOfDetail);
    for (ToolBarComponent cc : content) {
      map.put(cc.getId(), cc.isVisible());
    }
    return map;
  }

  public void setBookmarkState(Object state) {
    super.setBookmarkState(state);
	@SuppressWarnings("unchecked")
    Map<String, Boolean> map = (Map<String, Boolean>)state;
    for (ToolBarComponent cc : content) {
      Boolean b = map.get(cc.getId());
      if (b != null)
        cc.setVisible(b);
    }
  }

  /**
   * returns tooltip from resource bundle
   * 
   * @param tooltip
   *            name of the value in the resource bundle
   * @return value from resource bundle or <code>tooltip</code> if there is
   *         no resource bundle
   */
  public String getTooltip(String tooltip) {
    try {
      if (tooltip == null)
        return "";
      if (resb != null)
        return resb.getString(tooltip);
      return tooltip;
    } catch (MissingResourceException e) {
      logger.error("missing resource for " + tooltip);
      return tooltip;
    }
  }

  /**
   * @param resb
   */
  public void setBundle(Resources resb) {
    this.resb = resb;
  }

  /**
   * should be false.
   */
  public boolean isGlobalButtonIds() {
    return globalButtonIds;
  }

  /**
   * should be false.
   */
  public void setGlobalButtonIds(boolean globalButtonIds) {
    this.globalButtonIds = globalButtonIds;
  }
}
