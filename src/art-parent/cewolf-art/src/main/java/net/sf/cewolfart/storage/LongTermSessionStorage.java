/* ================================================================
 * Cewolf : Chart enabling Web Objects Framework
 * ================================================================
 *
 * Project Info:  http://cewolf.sourceforge.net
 * Project Lead:  Guido Laures (guido@laures.de);
 *
 * (C) Copyright 2002, by Guido Laures
 *
 * This library is free software; you can redistribute it and/or modify it under the terms
 * of the GNU Lesser General Public License as published by the Free Software Foundation;
 * either version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this
 * library; if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330,
 * Boston, MA 02111-1307, USA.
 */
package net.sf.cewolfart.storage;

import java.io.Serializable;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.servlet.jsp.PageContext;

import net.sf.cewolfart.CewolfException;
import net.sf.cewolfart.ChartImage;
import net.sf.cewolfart.Storage;
import net.sf.cewolfart.taglib.util.KeyGenerator;

/**
 * Storage stores images in session, but expires them after a certain time. 
 * This expiration time defaults to 300 seconds, and can be changed by adding 
 * the timeout="xxx" parameter to <cewolf:img> and <cewolf:legend> tags.
 * 
 * @author brianf
 */
public class LongTermSessionStorage implements Storage
{
	static final long serialVersionUID = 6810872505939693581L;

  public final String getKey( ChartImage cid )
  {
    return String.valueOf(KeyGenerator.generateKey((Serializable) cid));
  }

  /*
   * (non-Javadoc)
   * 
   * @see net.sf.cewolfart.Storage#storeChartImage(net.sf.cewolfart.ChartImage, javax.servlet.jsp.PageContext)
   */
	@Override
  public String storeChartImage( ChartImage chartImage, PageContext pageContext ) throws CewolfException
  {
    HttpSession session = pageContext.getSession();
    SessionStorageGroup ssg = (SessionStorageGroup) session.getAttribute("CewolfCharts");
    if ( ssg == null )
    {
      ssg = new SessionStorageGroup();
      session.setAttribute("CewolfCharts", ssg);
    }
    String cid = getKey(chartImage);
    SessionStorageItem ssi = new SessionStorageItem(chartImage, cid, chartImage.getTimeoutTime());
    ssg.put(cid, ssi);

    return cid;
  }

  /*
   * (non-Javadoc)
   * 
   * @see net.sf.cewolfart.Storage#getChartImage(java.lang.String, javax.servlet.http.HttpServletRequest)
   */
	@Override
  public ChartImage getChartImage( String id, HttpServletRequest request )
  {
    HttpSession session = request.getSession();
    ChartImage chart = null;
    SessionStorageGroup ssg = (SessionStorageGroup) session.getAttribute("CewolfCharts");
    if ( ssg != null )
    {
      SessionStorageItem ssi = ssg.get(id);
      if ( ssi != null )
      {
        chart = ssi.getChart();
      }
    }

    return chart;
  }
  /*
   * (non-Javadoc)
   * 
   * @see net.sf.cewolfart.Storage#init(javax.servlet.ServletContext)
   */
	@Override
  public void init( ServletContext servletContext ) throws CewolfException {
  }

  /**
   * @see net.sf.cewolfart.Storage#removeChartImage(java.lang.String, javax.servlet.http.HttpServletRequest)
   */
	@Override
  public String removeChartImage(String cid, HttpServletRequest request) throws CewolfException {
	  HttpSession session = request.getSession();
	  // No session exit
	  if (session == null)
	  {
		  return cid;
	  }
	  SessionStorageGroup ssg = (SessionStorageGroup) session.getAttribute("CewolfCharts");
	  if ( ssg == null )
	  {
		  // No group exit
		  return cid;
	  }
	  ssg.remove(cid);
	  return cid;  
  }
}
