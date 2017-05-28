package net.sf.wcfart.wcf.param;

/**
 * indicates that a parameter was expected in the paramPool but was not found.
 * 
 * @author av
 * @since 22.04.2005
 */
public class MissingParameterException extends RuntimeException {
	
	private static final long serialVersionUID = 1L;

  public MissingParameterException(String message) {
    super(message);
  }

}
