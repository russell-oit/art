package art.graph;

import art.utils.ArtQueryParam;
import art.utils.DrilldownQuery;

import org.jfree.data.general.DefaultValueDataset;
import org.jfree.chart.plot.*;
import org.jfree.chart.*;
import org.jfree.data.Range;

import de.laures.cewolf.*;

import java.sql.*;
import java.util.*;
import java.io.*;
import java.awt.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class to generate speedometer chart.
Query should be in the form <p>

<code>select dataValue, minValue, maxValue, unitsDescription, ranges </code> <br><br>
<b>ranges</b> represent optional sections and a range has 3 columns i.e. rangeUpperValue, rangeColour, rangeDescription
 * </p>

<b>Example</b>
 * <pre>
 * <code>
select 45, 0, 100, "Units",
50,"#00FF00","Normal",
80,"#FFFF00","Warning",
100,"#FF0000","Critical"
 * </code>
 * </pre>
 * 
 * @author Timothy Anyona
 */
public class ArtSpeedometer implements ArtGraph, DatasetProducer, ChartPostProcessor, Serializable {

    private static final long serialVersionUID = 1L;
    
    final static Logger logger = LoggerFactory.getLogger(ArtSpeedometer.class);
        
    String title = "Title";
    String xlabel = "Not Used";
    String ylabel = "Not Used";
    String seriesName = "Not Used";
    int height = 300;
    int width = 500;
    String bgColor = "#FFFFFF";
    boolean useHyperLinks = false;
    boolean hasDrilldown = false;
    boolean hasTooltips = false;
    double minValue;
    double maxValue;
    String unitsDescription;
    TreeMap<Integer, Double> rangeValues;
    TreeMap<Integer, String> rangeColors;
    TreeMap<Integer, String> rangeDescriptions;
    TreeMap<Integer, Range> rangeRanges;
    int rangeCount;
    String openDrilldownInNewWindow;
    DefaultValueDataset dataset = new DefaultValueDataset();
    

    /**
     * Constructor
     */
    public ArtSpeedometer() {
    }
   
    @Override
    public String getOpenDrilldownInNewWindow() {
        return openDrilldownInNewWindow;
    }

    @Override
    public boolean getHasTooltips() {
        return hasTooltips;
    }

    @Override
    public boolean getHasDrilldown() {
        return hasDrilldown;
    }

    @Override
    public void setTitle(String title) {
        this.title = title;
    }

    @Override
    public String getTitle() {
        return title;
    }

    @Override
    public void setXlabel(String xlabel) {
        this.xlabel = xlabel;
    }

    @Override
    public String getXlabel() {
        return xlabel;
    }

    @Override
    public void setYlabel(String value) {
        this.ylabel = value;
    }

    @Override
    public String getYlabel() {
        return ylabel;
    }

    @Override
    public void setSeriesName(String value) {
        this.seriesName = value;
    }

    @Override
    public void setWidth(int value) {
        this.width = value;
    }

    @Override
    public int getWidth() {
        return width;
    }

    @Override
    public void setHeight(int value) {
        this.height = value;
    }

    @Override
    public int getHeight() {
        return height;
    }

    @Override
    public void setBgColor(String value) {
        this.bgColor = value;
    }

    @Override
    public String getBgColor() {
        return bgColor;
    }

    @Override
    public void setUseHyperLinks(boolean b) {
        this.useHyperLinks = b;
    }

    @Override
    public boolean getUseHyperLinks() {
        return useHyperLinks;
    }

    //overload used by exportgraph class. no drill down for scheduled charts
    @Override
    public void prepareDataset(ResultSet rs) throws SQLException {
        prepareDataset(rs, null, null, null);
    }

    //prepare graph data structures with query results
    @Override
    public void prepareDataset(ResultSet rs, Map<Integer, DrilldownQuery> drilldownQueries, Map<String, String> inlineParams, Map<String, String[]> multiParams) throws SQLException {

        ResultSetMetaData rsmd = rs.getMetaData();
        int columnCount = rsmd.getColumnCount();

        if (rs.next()) {
            dataset.setValue(rs.getDouble(1));

            minValue = rs.getDouble(2);
            maxValue = rs.getDouble(3);
            unitsDescription = rs.getString(4);

            if (columnCount > 4) {
                //ranges have been specified
                rangeValues = new TreeMap<Integer, Double>();
                rangeColors = new TreeMap<Integer, String>();
                rangeDescriptions = new TreeMap<Integer, String>();
                rangeRanges = new TreeMap<Integer, Range>();
                Integer key;
                rangeCount = 0;
                int i;
                for (i = 5; i < columnCount; i += 3) {
                    rangeCount++;
                    key = new Integer(rangeCount);
                    rangeValues.put(key, rs.getDouble(i));
                    rangeColors.put(key, rs.getString(i + 1));
                    rangeDescriptions.put(key, rs.getString(i + 2));
                }

                //build chart ranges
                double rangeMin;
                double rangeMax;
                for (i = 1; i <= rangeCount; i++) {
                    key = new Integer(i);
                    if (i == 1) {
                        rangeMin = minValue;
                        rangeMax = rangeValues.get(key);
                    } else {
                        rangeMin = rangeValues.get(key - 1);
                        rangeMax = rangeValues.get(key);
                    }
                    Range range = new Range(rangeMin, rangeMax);
                    rangeRanges.put(key, range);
                }
            }
        }
    }

    /**
     * 
     * @param params
     * @return dataset to be used for rendering the chart
     */
    @Override
    public Object produceDataset(Map params) {
        return dataset;
    }

    
    /**
     * 
     * @return identifier for this producer class
     */
    @Override
    public String getProducerId() {
        return "SpeedometerDataProducer";
    }

    /**
     * 
     * @param params
     * @param since
     * @return <code>true</code> if the data for the chart has expired
     */
    @Override
    public boolean hasExpired(Map params, java.util.Date since) {
        return true;
    }

    /**
     * 
     * @param ch
     * @param params
     */
    @Override
    public void processChart(Object ch, Map params) {
        JFreeChart chart = (JFreeChart) ch;
        MeterPlot plot = (MeterPlot) chart.getPlot();

        finalizePlot(plot);

        boolean showLegend = (Boolean) params.get("showLegend");
        if (!showLegend) {
            chart.removeLegend();
        }

        // Output to file if required     	  
        String outputToFile = (String) params.get("outputToFile");
        String fileName = (String) params.get("fullFileName");
        if (outputToFile.equals("pdf")) {
            PdfGraph.createPdf(ch, fileName, title);
        } else if (outputToFile.equals("png")) {
            //save chart as png file									            
            try {
                ChartUtilities.saveChartAsPNG(new File(fileName), chart, width, height);
            } catch (Exception e) {
                logger.error("Error",e);
            }
        }
    }

    //finalize the plot including adding ranges, units description and custom formatting
    //put code in a method so that it can be used from exportgraph
    /**
     * 
     * @param plot
     */
    public void finalizePlot(MeterPlot plot) {
        plot.setRange(new Range(minValue, maxValue));
        plot.setUnits(unitsDescription);

        plot.setNeedlePaint(Color.darkGray);

        //set ranges
        int i;
        String description;
        Color rangeColor;
        for (i = 1; i <= rangeCount; i++) {
            description = rangeDescriptions.get(i);
            rangeColor = Color.decode(rangeColors.get(i));
            MeterInterval interval = new MeterInterval(description, rangeRanges.get(i), rangeColor, new BasicStroke(2.0F), null);
            plot.addInterval(interval);
        }

        //set tick interval. display interval every 10 percent. by default ticks are displayed every 10 units. can be too many with large values
        double tickInterval = (maxValue - minValue) / 10.0;
        plot.setTickSize(tickInterval);
    }
}