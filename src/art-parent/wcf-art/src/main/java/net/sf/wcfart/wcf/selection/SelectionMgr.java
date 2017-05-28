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
package net.sf.wcfart.wcf.selection;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.w3c.dom.Element;

import net.sf.wcfart.wcf.component.Form;
import net.sf.wcfart.wcf.component.FormListener;
import net.sf.wcfart.wcf.component.RenderListener;
import net.sf.wcfart.wcf.controller.Dispatcher;
import net.sf.wcfart.wcf.controller.DispatcherSupport;
import net.sf.wcfart.wcf.controller.RequestContext;
import net.sf.wcfart.wcf.controller.RequestListener;
import net.sf.wcfart.wcf.convert.BooleanConverter;
import net.sf.wcfart.wcf.convert.CheckBoxConverter;
import net.sf.wcfart.wcf.convert.RadioButtonConverter;
import net.sf.wcfart.wcf.ui.CheckBox;
import net.sf.wcfart.wcf.ui.Item;
import net.sf.wcfart.wcf.ui.RadioButton;
import net.sf.wcfart.wcf.utils.DomUtils;

/**
 * Selection Manager
 * 
 * @author av
 */
public class SelectionMgr implements FormListener, RenderListener {

  String groupId = DomUtils.randomId();
  BooleanConverter radioConv = new RadioButtonConverter();
  BooleanConverter checkConv = new CheckBoxConverter();
  List<SelectionHandler> selectionHandlers = new ArrayList<>();
  Dispatcher dispatcher = new DispatcherSupport();
  TitleProvider titleProvider = null;
  boolean readOnly;

  private static Logger logger = Logger.getLogger(SelectionMgr.class);

  SelectionModel selectionModel;

  public SelectionMgr(Dispatcher dispatcher, Form form) {
    this(dispatcher, form, new DefaultSelectionModel());
  }

  public SelectionMgr(Dispatcher dispatcher, Form form, SelectionModel selectionModel) {
    this.selectionModel = selectionModel;
    form.addFormListener(this);
    dispatcher.addRequestListener(null, null, this.dispatcher);
  }

  /**
   * must be called once before rendering
	 * @param context context
   */
  public void startRendering(RequestContext context) {
    selectionHandlers.clear();
    dispatcher.clear();
  }

  /**
   * must be called once after rendering
   */
  public void stopRendering() {
  }

  /**
   * if selection is enabled adds a checkbox or radiobutton element to the parent.
	 * @param parent parent
	 * @param obj object
   */
  public void renderButton(Element parent, Object obj) {

    if (!selectionModel.isSelectable(obj)) {
      DomUtils.appendNbsp(parent);
      return;
    }

    int selMode = selectionModel.getMode();

    if (selMode == SelectionModel.SINGLE_SELECTION_HREF || selMode == SelectionModel.MULTIPLE_SELECTION_HREF) {
      if (readOnly) {
        if (selectionModel.contains(obj))
          parent.setAttribute("style", "selected");
      } else {
        String id = DomUtils.randomId();
        parent.setAttribute("hrefId", id);
        if (selMode == SelectionModel.SINGLE_SELECTION_HREF)
          dispatcher.addRequestListener(id, null, new SingleSelectHandler(obj));
        else 
            dispatcher.addRequestListener(id, null, new MultipleSelectHandler(obj));
        if (selectionModel.contains(obj))
          parent.setAttribute("style", "selected");
      }
    }

    else if (selMode == SelectionModel.SINGLE_SELECTION_BUTTON || selMode == SelectionModel.MULTIPLE_SELECTION_BUTTON) {
      if (readOnly) {
        if (selectionModel.contains(obj))
          parent.setAttribute("style", "selected");
      } else {
        String id = DomUtils.randomId();
        parent.setAttribute("buttonId", id);
        if (selMode == SelectionModel.SINGLE_SELECTION_BUTTON)
          dispatcher.addRequestListener(id, null, new SingleSelectHandler(obj));
        else
          dispatcher.addRequestListener(id, null, new MultipleSelectHandler(obj));
        if (selectionModel.contains(obj))
          parent.setAttribute("selected", "true");
      }
    }

    // create button element
    else if (selMode == SelectionModel.SINGLE_SELECTION
        || selMode == SelectionModel.MULTIPLE_SELECTION) {

      Element button;
      String buttonId = DomUtils.randomId();

      if (selectionModel.getMode() == SelectionModel.SINGLE_SELECTION) {
        button = RadioButton.addRadioButton(parent);
        RadioButton.setGroupId(button, groupId);
        RadioButton.setId(button, buttonId);
        RadioButton.setDisabled(button, readOnly);
        selectionHandlers.add(new SelectionHandler(obj, button, radioConv));
      } else {
        button = CheckBox.addCheckBox(parent);
        CheckBox.setId(button, buttonId);
        CheckBox.setDisabled(button, readOnly);
        selectionHandlers.add(new SelectionHandler(obj, button, checkConv));
      }

      Item.setId(button, DomUtils.randomId());
      Item.setSelected(button, selectionModel.contains(obj));
      if (titleProvider != null) {
        String title = titleProvider.getLabel(obj);
        button.setAttribute("title", title);
      }
    }
  }

  /**
   * Returns the model.
   * @return SelectionModel
   */
  public SelectionModel getSelectionModel() {
    return selectionModel;
  }

  /**
   * Sets the model.
   * @param selectionModel The model to set
   */
  public void setSelectionModel(SelectionModel selectionModel) {
    this.selectionModel = selectionModel;
  }

  class SelectionHandler {
    Object obj;
    Element elem;
    BooleanConverter conv;

    public SelectionHandler(Object obj, Element elem, BooleanConverter conv) {
      this.obj = obj;
      this.elem = elem;
      this.conv = conv;
    }

	/**
	 * validate
	 * @param context context
	 */
    public void validate(RequestContext context) {
      Map<String, String[]> params = context.getParameters();
      switch (conv.isSelected(elem, params)) {
      case BooleanConverter.TRUE:
        if (selectionModel.getMode() == SelectionModel.SINGLE_SELECTION) {
          selectionModel.setSingleSelection(obj);
        } else {
          selectionModel.add(obj);
        }
        selectionModel.fireSelectionChanged(context);
        break;
      case BooleanConverter.FALSE:
        selectionModel.remove(obj);
        selectionModel.fireSelectionChanged(context);
        break;
      default: // UNKNOWN, i.e. not rendered
        break;
      }
    }
  }

  public void revert(RequestContext context) {
  }

  public boolean validate(RequestContext context) {
    logger.info("enter");
    for (SelectionHandler sh : selectionHandlers) {
      sh.validate(context);
    }
    return true;
  }

  /** single selection via href hyperlink */
  class SingleSelectHandler implements RequestListener {
    private Object node;

    SingleSelectHandler(Object node) {
      this.node = node;
    }

    public void request(RequestContext context) throws Exception {
      selectionModel.setSingleSelection(node);
      selectionModel.fireSelectionChanged(context);
    }
  }

  /** multiple selection via image buttons */
  class MultipleSelectHandler implements RequestListener {
    private Object node;

    MultipleSelectHandler(Object node) {
      this.node = node;
    }

    public void request(RequestContext context) throws Exception {
      if (selectionModel.contains(node))
        selectionModel.remove(node);
      else
        selectionModel.add(node);
      selectionModel.fireSelectionChanged(context);
    }
  }

  /**
   * if set creates title attribute
	 * @return title attribute
   */
  public TitleProvider getTitleProvider() {
    return titleProvider;
  }

  /**
   * if set creates title attribute
	 * @param provider provider
   */
  public void setTitleProvider(TitleProvider provider) {
    titleProvider = provider;
  }

  /**
   * if is read only
   * @return if is read only
   */
  public boolean isReadOnly() {
    return readOnly;
  }

  /**
   * set read only
   * @param readOnly read only
   */
  public void setReadOnly(boolean readOnly) {
    this.readOnly = readOnly;
  }

}
