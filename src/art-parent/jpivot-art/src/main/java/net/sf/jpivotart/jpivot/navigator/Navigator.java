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
package net.sf.jpivotart.jpivot.navigator;

import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpSession;

import org.w3c.dom.Document;

import net.sf.jpivotart.jpivot.core.ModelChangeEvent;
import net.sf.jpivotart.jpivot.core.ModelChangeListener;
import net.sf.jpivotart.jpivot.navigator.hierarchy.HierarchyItem;
import net.sf.jpivotart.jpivot.navigator.hierarchy.HierarchyItemClickHandler;
import net.sf.jpivotart.jpivot.navigator.hierarchy.HierarchyNavigator;
import net.sf.jpivotart.jpivot.navigator.member.MemberNavigator;
import net.sf.jpivotart.jpivot.navigator.member.MemberSelectionModel;
import net.sf.jpivotart.jpivot.olap.model.Hierarchy;
import net.sf.jpivotart.jpivot.olap.model.OlapModel;
import net.sf.jpivotart.jpivot.ui.Available;
import net.sf.wcfart.wcf.component.Component;
import net.sf.wcfart.wcf.component.ComponentSupport;
import net.sf.wcfart.wcf.controller.RequestContext;
import net.sf.wcfart.wcf.controller.RequestListener;
import net.sf.wcfart.wcf.tree.NodeSorter;

/**
 * Root Component, contains MemberNavigator and HierarchyNavigator.
 * Coordinates the activities between MemberNavigator and HierarchyNavigator (GOF Director pattern).
 *
 * @author av
 */

public class Navigator extends ComponentSupport implements ModelChangeListener, Available {
  OlapModel olapModel;
  MemberNavigator memberNav;
  HierarchyNavigator hierarchyNav;
  HierarchyItem currentItem;
  Component current;

  public Navigator(String id, Component parent, OlapModel olapModel) {
    super(id, parent);
    this.olapModel = olapModel;
    olapModel.addModelChangeListener(this);

    RequestListener acceptHandler = createMemberNavAcceptHandler();
    RequestListener cancelHandler = createMemberNavCancelHandler();
    memberNav = new MemberNavigator(id + ".membernav", this, olapModel, acceptHandler, cancelHandler);
    hierarchyNav = new HierarchyNavigator(id + ".hiernav", this, olapModel);
    if (memberNav.isAvailable())
      hierarchyNav.setHierarchyItemClickHandler(createHierarchyItemClickHandler());
    current = hierarchyNav;
  }

  /** 
   * lifecycle
   */
  public void initialize(RequestContext context) throws Exception {
    super.initialize(context);
    memberNav.initialize(context);
    hierarchyNav.initialize(context);
  }

  /** 
   * lifecycle
   */
  public void destroy(HttpSession session) throws Exception {
    memberNav.destroy(session);
    hierarchyNav.destroy(session);
    super.destroy(session);
  }

  public Document render(RequestContext context) throws Exception {
    return current.render(context);
  }

  /**
   * sets component to visible
   */
  public void show(Component component) {
    if (component == null)
      this.current = hierarchyNav;
    else
      this.current = component;
  }

  /**
   * invoked when the user clicks on a Hierarchy in the HierarchyNavigator
   */
  private class HierarchyItemClickAdapter implements HierarchyItemClickHandler {
    public void itemClicked(
      RequestContext context,
      HierarchyItem item,
      MemberSelectionModel selection,
      boolean allowChangeOrder) {
      
      currentItem = item;
      Hierarchy[] hiers = item.getDimension().getHierarchies();
      memberNav.setHierarchies(hiers, allowChangeOrder, selection, item.getDeleted());
      
      show(memberNav);
    }
  }

  private class MemberNavAcceptHandler implements RequestListener {
    public void request(RequestContext context) throws Exception {
      // read user selection (checkboxes)
      boolean valid = memberNav.validate(context);

      // the selection is an unsorted set. 
      // Order the selected items into tree order.
      Set<Object> set = memberNav.getSelectionModel().getSelection();
      List<Object> list = NodeSorter.preOrder(set, memberNav.getModel());
      String errmesg = currentItem.validateSelection(list);
      if (errmesg != null)
        memberNav.setError(errmesg);
      else if (valid) {
        currentItem.setSelection(list);
        currentItem.setDeleted(memberNav.getDeleteNodeModel().getDeleted());
        show(hierarchyNav);
      }
    }
  }

  private class MemberNavCancelHandler implements RequestListener {
    public void request(RequestContext context) throws Exception {
      memberNav.revert(context);
      show(hierarchyNav);
    }
  }

  /**
   * factory method that allows derived classes to install their own handlers
   */
  protected RequestListener createMemberNavAcceptHandler() {
    return new MemberNavAcceptHandler();
  }

  /**
   * factory method that allows derived classes to install their own handlers
   */
  protected HierarchyItemClickHandler createHierarchyItemClickHandler() {
    return new HierarchyItemClickAdapter();
  }

  /**
   * factory method that allows derived classes to install their own handlers
   */
  protected RequestListener createMemberNavCancelHandler() {
    return new MemberNavCancelHandler();
  }

  /**
   * Returns the visible.
   * @return boolean
   */
  public boolean isVisible() {
    return hierarchyNav.isVisible();
  }

  /**
   * Sets the visible.
   * @param visible The visible to set
   */
  public void setVisible(boolean visible) {
    hierarchyNav.setVisible(visible);
  }

  public void modelChanged(ModelChangeEvent e) {
    show(hierarchyNav);
  }

  public void structureChanged(ModelChangeEvent e) {
    if (memberNav.isAvailable())
      hierarchyNav.setHierarchyItemClickHandler(createHierarchyItemClickHandler());
    else
      hierarchyNav.setHierarchyItemClickHandler(null);
    show(hierarchyNav);
  }

  public HierarchyNavigator getHierarchyNav() {
    return hierarchyNav;
  }

  public MemberNavigator getMemberNav() {
    return memberNav;
  }
  
  public boolean isAvailable() {
    return hierarchyNav.isAvailable();
  }

}
