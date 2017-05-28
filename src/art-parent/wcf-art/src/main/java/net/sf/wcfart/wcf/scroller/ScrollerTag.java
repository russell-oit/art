package net.sf.wcfart.wcf.scroller;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.TagSupport;

import org.apache.log4j.Logger;

import net.sf.wcfart.wcf.controller.RequestContext;

/**
 * Adds hidden fields to a form in a jsp that ensure that the browser keeps the
 * scroll position when a user is navigating. This tag only works if 
 * the scroller.js javascript file is added to the page.
 * <p> 
 * By default scrolling is disabled. A request handler should enable the 
 * scroller (@link net.sf.wcfart.wcf.scroller.Scroller) if its action 
 * requires keeping the scroll position
 */
public class ScrollerTag extends TagSupport {
	
	private static final long serialVersionUID = 1L;

  private static Logger logger = Logger.getLogger(ScrollerTag.class);
  private Scroller scroller = new Scroller();

  public int doStartTag() throws JspException {
    return SKIP_BODY;
  }

  public int doEndTag() throws JspException {
    try {
      RequestContext context = RequestContext.instance();
      scroller.handleRequest(context, pageContext.getOut());
    } catch (Exception e) {
      logger.error("", e);
      throw new JspException(e);
    }

    return EVAL_PAGE;
  }
}