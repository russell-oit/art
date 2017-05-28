package net.sf.wcfart.wcf.test;

import net.sf.wcfart.wcf.controller.RequestContext;
import net.sf.wcfart.wcf.form.ActionReferenceException;
import net.sf.wcfart.wcf.form.FormBean;
import net.sf.wcfart.wcf.form.FormComponent;

/**
 * @author av
 */
public class WizardTestBean implements FormBean {

  String stringValue = "XX";
  int intValue;
  boolean booleanValue;
  FormComponent form;
  
  public void onNext(RequestContext context) throws Exception {
    if (!"AB".equals(stringValue)) {
      form.setError("errorElementID", "Please enter AB");
      throw new ActionReferenceException();
    }
    form.setError("errorElementID", null);
  }
  
  public void setFormComponent(RequestContext context, FormComponent form) {
    // this bean is called with multiple forms. 
    // We remember the first one only to display the error message
    if (this.form == null)
      this.form = form;
  }

  public boolean isBooleanValue() {
    return booleanValue;
  }
  public void setBooleanValue(boolean booleanValue) {
    this.booleanValue = booleanValue;
  }
  public int getIntValue() {
    return intValue;
  }
  public void setIntValue(int intValue) {
    this.intValue = intValue;
  }
  public String getStringValue() {
    return stringValue;
  }
  public void setStringValue(String stringValue) {
    this.stringValue = stringValue;
  }
}
