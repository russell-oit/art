package net.sf.wcfart.wcf.wizard;

import net.sf.wcfart.wcf.controller.RequestContext;

/**
 * a WizardPage tells its wizard that its finished by calling one of these methods.
 * 
 * @author av
 */
public interface PageListener {
  void onNext(RequestContext context) throws Exception;
  void onBack(RequestContext context) throws Exception;
  void onFinish(RequestContext context) throws Exception;
  void onCancel(RequestContext context) throws Exception;
}
