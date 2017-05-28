package net.sf.wcfart.wcf.param;

import javax.servlet.jsp.JspException;

import org.apache.log4j.Logger;

/**
 * creates or replaces a session parameter
 * 
 * @author av
 * @since 18.02.2005
 */
public class SetParamTag extends SetParamTagBase {
  
  public static final Logger logger = Logger.getLogger(SetParamTag.class);
  
  private static final long serialVersionUID = 1L;

  public int doStartTag() throws JspException {
    super.doStartTag();
    oldParam = newParam = null;
    return SKIP_BODY;
  }
  
}
