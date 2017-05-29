
package net.sf.cewolfart;

import java.util.Date;
import java.util.Map;

public interface CewolfRendererMBean {

    public Date getStartup();

	public int getNumberChartsRendered();

	public int getNumberChartPostProcessorsUsed();

	public Map<String,Integer> getCppUsageDetails();

	public Map<String,Integer> getChartUsageDetails();

	public boolean getDebug();

	public void setDebug (boolean debug);

	public boolean getRenderingEnabled();

	public void setRenderingEnabled (boolean renderingEnabled);

}

