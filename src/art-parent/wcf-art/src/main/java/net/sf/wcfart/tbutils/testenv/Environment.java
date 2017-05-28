package net.sf.wcfart.tbutils.testenv;

/**
 * If the system properties <code>net.sf.wcf-art.environment</code> is
 * set to <code>test</code> then we are in an test environment and
 * may behave different here and there.
 * 
 * @author av
 */
public class Environment {

  private static boolean test = "test".equals(System.getProperty("net.sf.wcf-art.environment"));

  /**
   * true if this VM runs in a test environment
   * 
   * @return true if this VM runs in a test environment
   */
  public static boolean isTest() {
    return test;
  }
}