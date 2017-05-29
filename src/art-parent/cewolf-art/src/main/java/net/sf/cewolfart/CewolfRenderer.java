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

package net.sf.cewolfart;

import net.sf.cewolfart.util.RenderingHelper;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import javax.imageio.ImageIO;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import javax.management.MBeanServer;
import javax.management.ObjectName;
import java.lang.management.ManagementFactory;

/**
 * The rendering servlet of Cewolf. It is resposible for writing an entire chart
 * img into the response stream of the client. Everything needed for this is
 * prepared already by the ChartImgTag resp. LegendTag. The ID of a chart image
 * is passed to this servlet as a request parameter. After that the image object
 * is retrieved from the server side session based image cache. This servlet
 * must be configured in web.xml of the web application in order to use Cewolf
 * services. The servlet's URL relative to the web apps root is used as the
 * renderer attribute of the ChartImgTag resp. LegendTag in the JSP page.
 * 
 * @see net.sf.cewolfart.taglib.tags.ChartImgTag
 * @see net.sf.cewolfart.taglib.tags.LegendTag
 * @author Guido Laures
 */

public class CewolfRenderer
	extends HttpServlet
	implements CewolfRendererMBean
{
	static final long serialVersionUID = 6604197744166761599L;

	private static Date startup = new Date();

	public static final String INIT_CONFIG = "CewolfRenderer_Init_Config";
	public static final String WEB_ROOT_DIR = "Web_Root_Dir";
	private static final String STATE = "state";
	private static boolean debugged = false;
	private static boolean renderingEnabled = true;
	private static AtomicInteger requestCount = new AtomicInteger(0);
	private static AtomicInteger cppCount = new AtomicInteger(0);
	private static Map<String,Integer> chartUsageDetails = new HashMap<String,Integer>();
	private static Map<String,Integer> cppUsageDetails = new HashMap<String,Integer>();
	private Configuration config = null;
	private String path;

	@Override
	public void init (ServletConfig servletCfg) throws ServletException {
		super.init(servletCfg);

		//Store init config params for processing by the Configuration
		ServletContext context = servletCfg.getServletContext();
		context.setAttribute(INIT_CONFIG, servletCfg);
		context.setAttribute(WEB_ROOT_DIR, context.getRealPath("/"));
		config = Configuration.getInstance(servletCfg.getServletContext());

		if (config != null)
			debugged = config.isDebugged();
		else
			debugged = false;

		// don't use disk cache - we may not have the permissions
		ImageIO.setUseCache(false);

		path = context.getContextPath();
		if (path.equals(""))
			path = "/";

		try {
			MBeanServer server = ManagementFactory.getPlatformMBeanServer();
			ObjectName name = new ObjectName("Cewolf:name=Renderer,path="+path);
			server.registerMBean(this, name);
			log("registered MBean: "+name);
		} catch (Exception ex) {
			log("Bad JMX object name: "+ex.getMessage());
		}
	}

	@Override
    public void destroy() {
		try {
			log("unregistering MBean");
			MBeanServer server = ManagementFactory.getPlatformMBeanServer();
            server.unregisterMBean(new ObjectName("Cewolf:name=Renderer,path="+path));
        } catch (Exception ex) {
            log("problem unregistering MBean: "+ex.getMessage());
        }
    }

	public void printParameters (HttpServletRequest request) {
		Enumeration<String> enumeration = request.getParameterNames();
		while (enumeration.hasMoreElements()) {
			String cur = enumeration.nextElement();
			Object obj = request.getParameter(cur);

			if (debugged)
				log("Request Parameter -> " + cur + " Value -> " + obj.toString());
		}
	}

  /**
   * Processes HTTP <code>GET</code> request. Renders the chart or the lengend into the client's response stream.
   * 
   * @param request servlet request
   * @throws ServletException when the production of data could not be handled by the configured DatasetProcuder
   */
	@Override
  protected void doGet (HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
  {
    if (debugged)
      logRequest(request);

    addHeaders(response);
    if (request.getParameter(STATE) != null || !request.getParameterNames().hasMoreElements())
    {
      requestState(response);
      return;
    }

    int width = 400;
    int height = 400;
    boolean removeAfterRendering = false;
    if (request.getParameter(WebConstants.REMOVE_AFTER_RENDERING) != null)
    {
    	removeAfterRendering = true;
    }
    if (request.getParameter(WebConstants.WIDTH_PARAM) != null)
    {
		width = Integer.parseInt(request.getParameter(WebConstants.WIDTH_PARAM));
    }
    if (request.getParameter(WebConstants.HEIGHT_PARAM) != null)
    {
		height = Integer.parseInt(request.getParameter(WebConstants.HEIGHT_PARAM));
    }

	// check whether rendering is curently disabled
	if (!renderingEnabled)
	{
		renderNotEnabled(response, 400, 50);
		return;
	}

	// make sure max image size is not exceeded
	if (width > config.getMaxImageWidth() || height > config.getMaxImageHeight())
	{
		renderImageTooLarge(response, 400, 50);
		return;
	}

    // determine the cache key
    String imgKey = request.getParameter(WebConstants.IMG_PARAM);
    if (imgKey == null)
    {
		logAndRenderException(new ServletException("no '" + WebConstants.IMG_PARAM + "' parameter provided for Cewolf servlet."), response, width, height);
		return;
    }
    Storage storage = config.getStorage();
    ChartImage chartImage = storage.getChartImage(imgKey, request);
    if (chartImage == null)
    {
		// use fixed width and height if image doesn't exist or has expired
		renderImageExpiry(response, 400, 50);
		return;
    }

	requestCount.incrementAndGet();

    // send the img
    try {
      long start = System.currentTimeMillis();
      final int size = chartImage.getSize();
      response.setContentType(chartImage.getMimeType());
      response.setContentLength(size);
	  // TODO: is the next line necessary?
      response.setBufferSize(size);
      response.setStatus(HttpServletResponse.SC_OK);
      response.getOutputStream().write(chartImage.getBytes());
      long last = System.currentTimeMillis() - start;
      if (debugged)
        log("creation time for chart " + imgKey + ": " + last + "ms.");
    } catch (Throwable t) {
		logAndRenderException(t, response, width, height);
    } finally {
    	if (removeAfterRendering) {
    		try {
				storage.removeChartImage(imgKey , request);
			} catch (CewolfException e) {
				log("Removal of image failed", e);
			}
    	}
    }
  }

	private void addHeaders (HttpServletResponse response) {
		response.setDateHeader("Expires", System.currentTimeMillis());
	}

	private void requestState (HttpServletResponse response) throws IOException
	{
		Writer writer = response.getWriter();
		writer.write("<HTML><BODY>");
		/*
		 * StateDescriptor sd = (StateDescriptor) ChartImageCacheFactory.getChartImageBase(getServletContext());
		 * writer.write(HTMLStateTable.getStateTable(sd));
		 */
		writer.write("<b>Cewolf servlet up and running.</b><br>");
		writer.write("Requests served so far: " + requestCount.get());
		writer.write("</HTML></BODY>");
		writer.close();
	}

	private void logAndRenderException (Throwable ex, HttpServletResponse response, int width, int height) throws IOException
	{
		log(ex.getMessage(), ex);
		response.setContentType("image/jpg");
		OutputStream out = response.getOutputStream();
		RenderingHelper.renderException(ex, width, height, out);
		out.close();
	}

	private void renderImageExpiry (HttpServletResponse response, int width, int height) throws IOException
	{
		response.setContentType("image/jpg");
		OutputStream out = response.getOutputStream();
		RenderingHelper.renderMessage("This chart has expired. Please reload.", width, height, out);
		out.close();
	}

	private void renderImageTooLarge (HttpServletResponse response, int width, int height) throws IOException
	{
		response.setContentType("image/jpg");
		OutputStream out = response.getOutputStream();
		RenderingHelper.renderMessage("Maximum image size exceeded.", width, height, out);
		out.close();
	}

	private void renderNotEnabled (HttpServletResponse response, int width, int height) throws IOException
	{
		response.setContentType("image/jpg");
		OutputStream out = response.getOutputStream();
		RenderingHelper.renderMessage("Charts are currently not available.", width, height, out);
		out.close();
	}

  private void logRequest (HttpServletRequest request) throws IOException
  {
    log("Cewolf request:");
    log("Actual Request values:");
    printParameters(request);
    Enumeration<String> headerNames = request.getHeaderNames();
    while (headerNames.hasMoreElements())
    {
      String name = headerNames.nextElement();
      Enumeration<String> values = request.getHeaders(name);
      StringBuffer value = new StringBuffer();
      while (values.hasMoreElements())
      {
        value.append(values.nextElement()).append(",");
      }
      // cut last comma
      if (value.length() > 0)
        value.setLength(value.length() - 1);
      log(name + ": " + value);
    }
  //  InputStream body = request.getInputStream();
 //   byte[] bodyData = new byte[body.available()];
 //   body.read(bodyData);
 //   body.close();
 //   log(new String(bodyData));
  }

	public static synchronized void cppUsed (ChartPostProcessor cpp) {
		cppCount.incrementAndGet();
		String name = cpp.getClass().getName();
		Integer count = cppUsageDetails.get(name);
		if (count == null) {
			cppUsageDetails.put(name, 1);
		} else {
			cppUsageDetails.put(name, count+1);
		}
	}

	public static synchronized void chartUsed (String type) {
		Integer count = chartUsageDetails.get(type);
		if (count == null) {
			chartUsageDetails.put(type, 1);
		} else {
			chartUsageDetails.put(type, count+1);
		}
	}

	// the following methods are just for JMX

    public Date getStartup() { return startup; }

	public int getNumberChartsRendered() {
		return requestCount.get();
	}

	public int getNumberChartPostProcessorsUsed() {
		return cppCount.get();
	}

	public Map<String,Integer> getCppUsageDetails() {
		return cppUsageDetails;
	}

	public Map<String,Integer> getChartUsageDetails() {
		return chartUsageDetails;
	}


	public boolean getDebug() {
		return debugged;
	}

	public void setDebug (boolean pDebugged) {
		debugged = pDebugged;
	}

	public boolean getRenderingEnabled() {
		return renderingEnabled;
	}

	public void setRenderingEnabled (boolean pRenderingEnabled) {
		renderingEnabled = pRenderingEnabled;
	}
}
