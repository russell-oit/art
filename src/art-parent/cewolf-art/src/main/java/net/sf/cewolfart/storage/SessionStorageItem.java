/*
 * Created on Aug 2, 2004
 */
package net.sf.cewolfart.storage;

import java.io.Serializable;
import java.util.Date;

import net.sf.cewolfart.ChartImage;

/**
 * Wrapper for the images stored in SessionStorageGroups.
 *
 * @author brianf
 */
public class SessionStorageItem implements java.io.Serializable 
{
	static final long serialVersionUID = -481087874120532816L;

  String     cid     = null;
  ChartImage chart   = null;
  Date       timeout = null;

  public SessionStorageItem()
  {
    super();
  }

  public SessionStorageItem( ChartImage theChart, String theCid, Date theTimeout )
  {
    chart   = theChart;
    cid     = theCid;
    timeout = theTimeout;
  }
  
  public String toString()
  {
    return ("SSI: id:"+cid+" expires:"+timeout);
  }

  public final boolean isExpired(Date currentTime)
  {
    return currentTime.after(timeout);
  }
  
  /**
   * @return Returns the chart.
   */
  public ChartImage getChart()
  {
    return chart;
  }
  
  /**
   * @param chart The chart to set.
   */
  public void setChart( ChartImage chart )
  {
    this.chart = chart;
  }
  
  /**
   * @return Returns the cid.
   */
  public String getCid()
  {
    return cid;
  }
  
  /**
   * @param cid The cid to set.
   */
  public void setCid( String cid )
  {
    this.cid = cid;
  }

  /**
   * @return Returns the timeout.
   */
  public Date getTimeout()
  {
    return timeout;
  }
  
  /**
   * @param timeout The timeout to set.
   */
  public void setTimeout( Date timeout )
  {
    this.timeout = timeout;
  }
}
