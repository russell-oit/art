package net.sf.cewolfart.cpp;

import java.awt.Color;
import java.awt.Font;
import java.awt.Paint;
import java.io.Serializable;
import java.util.*;

import net.sf.cewolfart.ChartPostProcessor;

import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.*;
import org.jfree.chart.labels.StandardCategoryItemLabelGenerator;
import org.jfree.chart.renderer.category.*;

/**
* A postprocessor for setting/removing the bar outline (default: false),
* the item margin for 2D and 3D bar charts (default: 0.2%), whether or not
* item labels are visible (default: no), the color to use for item labels (default: black)
* and their font size (default: 12).
* It also has an option to set custom category colors (as opposed to custom series colors,
* which is what the SeriesPaintProcessor provides); if you use this you'll want to set showlegend=false.
* <P>
* Usage:<P>
* &lt;chart:chartpostprocessor id="barRenderer"&gt;<BR>
* &nbsp;&nbsp;&lt;chart:param name="outline" value="true"/&gt;<BR>
* &nbsp;&nbsp;&lt;chart:param name="itemMargin" value="0.1"/&gt;<BR>
* &nbsp;&nbsp;&lt;chart:param name="showItemLabels" value="true"/&gt;<BR>
* &nbsp;&nbsp;&lt;chart:param name="itemLabelColor" value="#336699" /&gt;<BR>
* &nbsp;&nbsp;&lt;chart:param name="itemLabelSize" value="14" /&gt;<BR>
* &nbsp;&nbsp;&lt;chart:param name="categoryColors" value="true"/&gt;<BR>
* &nbsp;&nbsp;&lt;chart:param name="0" value="#FFFFAA" /&gt;<BR>
* &nbsp;&nbsp;&lt;chart:param name="1" value="#AAFFAA" /&gt;<BR>
* &nbsp;&nbsp;&lt;chart:param name="2" value="#FFAAFF" /&gt;<BR>
* &nbsp;&nbsp;&lt;chart:param name="3" value="#FFAAAA" /&gt;<BR>
* &lt;/chart:chartpostprocessor&gt;
*/

public class BarRendererProcessor implements ChartPostProcessor, Serializable
{
	static final long serialVersionUID = 6687503315061004361L;

	@Override
    public void processChart (JFreeChart chart, Map<String,String> params) {
        Plot plot = chart.getPlot();

		if (plot instanceof CategoryPlot) {
			CategoryPlot catPlot = (CategoryPlot) plot;
			CategoryItemRenderer ciRenderer = catPlot.getRenderer();

			if (ciRenderer instanceof BarRenderer) {
				boolean outline = false;
				boolean showItemLabels = false;
				boolean categoryColors = false;
				double itemMargin = BarRenderer.DEFAULT_ITEM_MARGIN;
				Color itemLabelColor = new Color(0, 0, 0);
				String fontName = "SansSerif";
				int fontSize = 12;
				boolean isBold = false;
				boolean isItalic = false;

				String str = params.get("outline");
				if (str != null)
					outline = "true".equals(str);

				str = params.get("showItemLabels");
				if (str != null)
					showItemLabels = "true".equals(str);

				str = params.get("categoryColors");
				if (str != null)
					categoryColors = "true".equals(str);

				str = params.get("itemLabelColor");
				if (str != null && str.trim().length() > 0) {
					try {
						itemLabelColor = Color.decode(str);
					} catch (NumberFormatException nfex) { }
				}

				str = params.get("itemMargin");
				if (str != null && str.trim().length() > 0) {
					try {
						itemMargin = Double.parseDouble(str);
					} catch (NumberFormatException nfex) { }
				}

				str = params.get("fontname");
				if (str != null && str.trim().length() > 0)
					fontName = str.trim();

				str = params.get("bold");
				if (str != null)
					isBold = "true".equals(str.toLowerCase());

				str = params.get("italic");
				if (str != null)
					isItalic = "true".equals(str.toLowerCase());

				str = params.get("itemLabelSize");
				if (str != null && str.trim().length() > 0) {
					try {
						fontSize = Integer.parseInt(str);
						if (fontSize < 4)
							fontSize = 12;
					} catch (NumberFormatException nfex) { }
				}

				if (categoryColors) {
					List<Paint> paints = new ArrayList<Paint>();
					for (int i = 0; ; i++) {
						String colorStr = params.get(String.valueOf(i));
						if (colorStr == null)
							break;
						paints.add(Color.decode(colorStr));
					}

					/* need to do most specific first! */
					if (ciRenderer instanceof BarRenderer3D) {
						catPlot.setRenderer(new CustomBarRenderer3D(paints));
					} else {
						catPlot.setRenderer(new CustomBarRenderer(paints));
					}

					ciRenderer = catPlot.getRenderer();
				}

				BarRenderer renderer = (BarRenderer) ciRenderer;
				renderer.setDrawBarOutline(outline);
				renderer.setItemMargin(itemMargin);

				if (showItemLabels) {
					renderer.setBaseItemLabelGenerator(new StandardCategoryItemLabelGenerator());
					renderer.setBaseItemLabelsVisible(true);
					renderer.setBaseItemLabelPaint(itemLabelColor);
					Font font = new Font(fontName,
										(isBold ? Font.BOLD : 0) + (isItalic ? Font.ITALIC : 0),
										fontSize);
					renderer.setBaseItemLabelFont(font);
				}
				//ItemLabelPosition itemlabelposition
				//	= new ItemLabelPosition(ItemLabelAnchor.CENTER, TextAnchor.CENTER, TextAnchor.CENTER, 0.0);
				//renderer.setBasePositiveItemLabelPosition(itemlabelposition);
			}
		}
	}

    /**
     * A custom renderer that returns a different color for each item in a single series.
     */
    private static class CustomBarRenderer extends BarRenderer {

		static final long serialVersionUID = 2451764538621611708L;

        /** The colors. */
        private List<Paint> colors;

        /**
         * Creates a new renderer.
         *
         * @param colors  the colors.
         */
        public CustomBarRenderer (List<Paint> colors) {
            this.colors = colors;
        }

        /**
         * Returns the paint for an item. Overrides the default behaviour inherited from AbstractSeriesRenderer.
         *
         * @param row  the series.
         * @param column  the category.
         *
         * @return The item color.
         */
		@Override
        public Paint getItemPaint (int row, int column) {
            return colors.get(column % colors.size());
        }

		@Override
		public boolean equals (Object obj) {
			if (obj == this) {
				return true;
			}
			if (!(obj instanceof CustomBarRenderer)) {
				return false;
			}
			CustomBarRenderer that = (CustomBarRenderer) obj;
			if (!super.equals(obj)) {
				return false;
			}
			if (!this.colors.equals(that.colors)) {
				return false;
			}
			return true;
		}

		@Override
		public int hashCode() {
			assert false : "hashCode not designed";
			return 42; // any arbitrary constant will do 
		}
    }

    /**
     * A custom renderer that returns a different color for each item in a single series.
     */
    private static class CustomBarRenderer3D extends BarRenderer3D {

		static final long serialVersionUID = 2674255384600916413L;

        /** The colors. */
        private List<Paint> colors;

        /**
         * Creates a new renderer.
         *
         * @param colors  the colors.
         */
        public CustomBarRenderer3D (List<Paint> colors) {
            this.colors = colors;
        }

        /**
         * Returns the paint for an item. Overrides the default behaviour inherited from AbstractSeriesRenderer.
         *
         * @param row  the series.
         * @param column  the category.
         *
         * @return The item color.
         */
		@Override
        public Paint getItemPaint (int row, int column) {
            return colors.get(column % colors.size());
        }

		@Override
		public boolean equals (Object obj) {
			if (obj == this) {
				return true;
			}
			if (!(obj instanceof CustomBarRenderer3D)) {
				return false;
			}
			CustomBarRenderer3D that = (CustomBarRenderer3D) obj;
			if (!super.equals(obj)) {
				return false;
			}
			if (!this.colors.equals(that.colors)) {
				return false;
			}
			return true;
		}

		@Override
		public int hashCode() {
			assert false : "hashCode not designed";
			return 42; // any arbitrary constant will do 
		}
    }
}

