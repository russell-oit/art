package net.sf.jpivotart.jpivot.table;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.w3c.dom.Element;

import net.sf.jpivotart.jpivot.olap.model.Alignable;
import net.sf.jpivotart.jpivot.table.SpanBuilder.SBContext;
import net.sf.jpivotart.jpivot.table.span.Span;
import net.sf.wcfart.wcf.popup.MenuItem;
import net.sf.wcfart.wcf.popup.MenuItemSupport;
import net.sf.wcfart.wcf.popup.PopUp;

public class AxisHeaderBuilderSupport implements AxisHeaderBuilder {

  private SpanBuilder spanBuilder;

  public AxisHeaderBuilderSupport(SpanBuilder spanBuilder) {
    this.spanBuilder = spanBuilder;
  }
  
  class MySBContext implements SBContext {
    Element captionElem;
    String captionLabel;
    List<MenuItem> clickables = new ArrayList<>();

    public void setCaption(Element elem, String label) {
      this.captionElem = elem;
      this.captionLabel = label;
    }
    public void addClickable(String href, String label) {
      MenuItemSupport mis = new MenuItemSupport(href);
      mis.setLabel(label);
      clickables.add(mis);
    }

    void render() {
      if (clickables.isEmpty())
        return;
      
      if (clickables.size() == 1) {
        MenuItem mi = clickables.get(0);
        captionElem.setAttribute("href", mi.getHref());
        return;
      }
      
      PopUp pu = new PopUp();
      pu.setLabel(captionLabel);
      for (MenuItem mi : clickables) {
        pu.addItem(mi);
      }
      Element e = pu.render(captionElem.getOwnerDocument());
      captionElem.appendChild(e);
    }
    
  }

  public void build(Element row, Span span, int rowspan, int colspan, boolean even, boolean memberIndent) {
    MySBContext sbctx = new MySBContext();
    Element elem = spanBuilder.build(sbctx, span, even);
    sbctx.render();
    elem.setAttribute("rowspan", Integer.toString(rowspan));
    elem.setAttribute("colspan", Integer.toString(colspan));

    // no special formatting present?
    if (elem.getAttribute("style").length() == 0) {
      // valid styles are { span, even, odd, span-right, even-right, odd-right}
      String style;
      if (colspan > 1 || rowspan > 1)
        style = "span";
      else if (even)
        style = "even";
      else
        style = "odd";
      if (isRightAligned(span))
        style = style + "-right";
      elem.setAttribute("style", style);
    }

    // indent level for hierarchy style view
    if (memberIndent && span.isMember()) {
      elem.setAttribute("indent", Integer.toString(span.getIndent()));
    }

    row.appendChild(elem);
  }

  private boolean isRightAligned(Span span) {
    Object obj = span.getObject();
    if (obj instanceof Alignable)
      return ((Alignable) obj).getAlignment() == Alignable.Alignment.RIGHT;
    return false;
  }

}
