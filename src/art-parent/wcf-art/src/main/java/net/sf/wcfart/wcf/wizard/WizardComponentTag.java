package net.sf.wcfart.wcf.wizard;

import net.sf.wcfart.wcf.component.Component;
import net.sf.wcfart.wcf.component.ComponentTag;
import net.sf.wcfart.wcf.controller.RequestContext;

/**
 * @author av
 */
public class WizardComponentTag extends ComponentTag {
	
	private static final long serialVersionUID = 1L;

  protected Component createComponent(RequestContext context) throws Exception {
    return new WizardComponent(id, null);
  }

}
