/*
 * Copyright (C)   Enrico Liboni  - enrico@computer.org
 *
 *   This program is free software; you can redistribute it and/or modify
 *   it under the terms of the LGPL License as published by
 *   the Free Software Foundation;
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. *  
 */
package art.graph;

import art.utils.DrilldownQuery;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import org.apache.commons.beanutils.RowSetDynaClass;

/**
<b>ArtGraph</b> is the common interface implemented by art
graphs generator objects. <br>

It is intended as an easy-to-use
wrapper around <i>cewolf/jfreechart</i> in servlets/jsps that 
creates graphs.<BR>

Usage example:
<br>
Initialize the object:
<pre>
ArtGraph o = new artTime();
o.setTitle("title");
o.setXlabel("x label");
o.setYlabel("y label");
o.setWidth(400);
o.setHeight(300);
o.setBgColor(bgColor);
// See the API of the each class
// to know how the resultset layout should be.
o.setResultSet(rs); 
// scroll the result set and prepare the dataset
try {
o.prepareDataset(); 
} catch(java.sql.SQLException e) {
}
</pre>
Set the object in the page scope:
<pre>
pageContext.setAttribute("artGraphData", o);
</pre>
Use the cewolf tag to display the graph:
<pre>

&lt;cewolf:chart 
id="artGraph" 
title="&lt;%=o.getTitle()%&gt;" 
type="timeseries" 
xaxislabel="&lt;%=o.getXlabel()%&gt;" 
yaxislabel="&lt;%=o.getYlabel()%&gt;"&gt;
&lt;cewolf:colorpaint color="&lt;%=o.getBgColor()%&gt;"/&gt;
&lt;cewolf:data&gt;
&lt;cewolf:producer id="artGraphData"/&gt;
&lt;/cewolf:data&gt;
&lt;/cewolf:chart&gt;

&lt;cewolf:img chartid="artGraph" 
renderer="cewolf" 
width="&lt;%=o.getWidth()%&gt;" 
height="&lt;%=o.getHeight()%&gt;"&gt;
&lt;/cewolf:img&gt;


</pre>
<br>
<b>Pre-requisites:</b><br>
See the cewolf example tutorial (shortly, put some jars in the lib dir of your
web application; put overlib.js javascript file in the /etc dir of your web application;
add some lines in the web.xml file to register the cewolf servlet). 
 * 
 * 
 * @author Enrico Liboni
 * @author Timothy Anyona

 */
public interface ArtGraph {

    /**
     * Set the graph title.
     * @param title graph title
     */
    public void setTitle(String title);

    /**
     * Get the graph title.
     * @return graph title
     */
    public String getTitle();

    /**
     * Set the x-axis label.
     * @param xlabel x-axis label
     */
    public void setXlabel(String xlabel);

    /**
     * Get the x-axis label.
     * @return x-axis label
     */
    public String getXlabel();

    /**
     * Set the y-axis label
     * @param ylabel y-axis label
     */
    public void setYlabel(String ylabel);

    /**
     * Get the y-axis label
     * @return y-axis label
     */
    public String getYlabel();

    /**
     * Set the series name
     * @param seriesName series name
     */
    public void setSeriesName(String seriesName);

    /**
     * Set the graph width
     * @param width graph width in pixels
     */
    public void setWidth(int width);

    /**
     * Get the graph width
     * @return graph width in pixels
     */
    public int getWidth();

    /**
     * Set the graph height
     * @param height graph height in pixels
     */
    public void setHeight(int height);

    /**
     * Get the graph height
     * @return graph height in pixels
     */
    public int getHeight();

    /**
     * Set the colour of the area around the graph
     * @param bgColor colour of the area around the graph
     */
    public void setBgColor(String bgColor);

    /**
     * Get the colour of the area around the graph
     * @return colour of the area around the graph
     */
    public String getBgColor();

    /**
     * Determine if graph uses hyperlinks
     * @param b <code>true</code> if hyperlinks used
     */
    public void setUseHyperLinks(boolean b);

    /**
     * Determine if graph uses hyperlinks
     * @return <code>true</code> if hyperlinks used
     */
    public boolean getUseHyperLinks();

    /**
     * Process a query and populate appropriate graph data structures.
     * 
     * @param rs query resultset
     * @param drilldownQueries drill down queries for the query
     * @param inlineParams inline parameters for the query
     * @param multiParams multi parameters for the query
     * @throws SQLException
     */
    public void prepareDataset(ResultSet rs, Map<Integer,DrilldownQuery> drilldownQueries, Map<String, String> inlineParams, Map<String, String[]> multiParams) throws SQLException; //added parameter to support drill down queries. //added parameters to enable drill down query to use parent parameters

    /**
     * Process a query and populate appropriate graph data structures.
     * Ignore drill down queries for the query. <br> 
     * No drill down for scheduled graphs (pdf or png only). Used by {@link ExportGraph#createFile(java.sql.ResultSet, int) ExportGraph}
     * 
     * @param rs query resultset
     * @throws SQLException
     */
    public void prepareDataset(ResultSet rs) throws SQLException;

    /**
     * Determine if graph has drill down queries defined
     * @return <code>true</code> if graph has drill down queries
     */
    public boolean getHasDrilldown();

    /**
     * Determine if drill down queries should be opened
     * in a new browser window or in the same window as the main query
     * 
     * @return <i>'Y'</i> or <i>null</i> if drill downs should be opened in a new window
     */
    public String getOpenDrilldownInNewWindow();

    /**
     * Determine if graph can have tooltips. 
     * Not all charts can have tooltips e.g. speedometer
     * 
     * @return <code>true</code> if graph can have tooltips
     */
    public boolean getHasTooltips();
	
	/**
	 * Determine if graph data should be shown below graph
	 */
	public void setShowGraphData(boolean value);
	
	/**
	 * Get show graph data setting
	 * @return <code>true</code> if graph data should be shown below graph
	 */
	public boolean isShowGraphData();
	
	/**
	 * Get graph's data
	 * @return rowset that contains the graph's data
	 */
	public RowSetDynaClass getGraphData();
         
}
