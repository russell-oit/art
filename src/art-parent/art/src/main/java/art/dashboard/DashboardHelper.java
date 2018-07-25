/*
 * ART. A Reporting Tool.
 * Copyright (C) 2018 Enrico Liboni <eliboni@users.sf.net>
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package art.dashboard;

import art.report.Report;
import art.reportoptions.GridstackItemOptions;
import art.reportparameter.ReportParameter;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import javax.servlet.http.HttpServletRequest;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.owasp.encoder.Encode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

/**
 * Provides methods for building dashboard objects
 *
 * @author Timothy Anyona
 */
public class DashboardHelper {
	
	private static final Logger logger = LoggerFactory.getLogger(DashboardHelper.class);
	
	private XPath xPath;
	private Element rootNode;

	/**
	 * Returns the gridstack dashboard object to be used to display the
	 * gridstack dashboard
	 *
	 * @param report the gridstack dashboard report
	 * @return the gridstack dashboard object to be used to display the
	 * gridstack dashboard
	 * @throws Exception
	 */
	public GridstackDashboard buildBasicGridstackDashboardObject(Report report)
			throws Exception {

		logger.debug("Entering buildBasicGridstackDashboardObject: Report={}", report);
		
		Objects.requireNonNull(report, "report must not be null");

		String dashboardXml = report.getReportSource();
		logger.debug("dashboardXml='{}'", dashboardXml);

		if (StringUtils.isBlank(dashboardXml)) {
			throw new IllegalArgumentException("No dashboard content");
		}

		GridstackDashboard dashboard = new GridstackDashboard();

		List<GridstackItem> items = new ArrayList<>();

		List<GridstackItemOptions> itemOptions;
		String savedOptions = report.getGridstackSavedOptions();
		if (StringUtils.isBlank(savedOptions)) {
			itemOptions = new ArrayList<>();
		} else {
			//https://stackoverflow.com/questions/11664894/jackson-deserialize-using-generic-class
			//https://stackoverflow.com/questions/8263008/how-to-deserialize-json-file-starting-with-an-array-in-jackson
			ObjectMapper mapper = new ObjectMapper();
			itemOptions = mapper.readValue(savedOptions, new TypeReference<List<GridstackItemOptions>>() {
			});
		}

		//https://stackoverflow.com/questions/773012/getting-xml-node-text-value-with-java-dom
		//https://stackoverflow.com/questions/4076910/how-to-retrieve-element-value-of-xml-using-java
		//http://www.w3schools.com/xml/xpath_intro.asp
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();
		Document document = builder.parse(new InputSource(new StringReader(dashboardXml)));
		rootNode = document.getDocumentElement();

		xPath = XPathFactory.newInstance().newXPath();

		Map<Integer, DashboardItem> itemsMap = new HashMap<>();

		NodeList itemNodes = (NodeList) xPath.evaluate("ITEM", rootNode, XPathConstants.NODESET);
		int itemIndex = 0;
		for (int i = 0; i < itemNodes.getLength(); i++) {
			itemIndex++;

			Node itemNode = itemNodes.item(i);

			GridstackItem item = new GridstackItem();
			item.setIndex(itemIndex);

			setGridstackItemProperties(item, itemNode);

			for (GridstackItemOptions itemOption : itemOptions) {
				int index = itemOption.getIndex();
				if (index == itemIndex) {
					item.setxPosition(itemOption.getX());
					item.setyPosition(itemOption.getY());
					item.setWidth(itemOption.getWidth());
					item.setHeight(itemOption.getHeight());
					break;
				}
			}

			items.add(item);

			itemsMap.put(item.getIndex(), item);
		}

		dashboard.setItems(items);

		return dashboard;
	}
	
		/**
	 * Sets the properties of a gridstack item
	 *
	 * @param item the gridstack item
	 * @param itemNode the item's node
	 * @param request the http request
	 * @param locale the locale being used
	 * @throws UnsupportedEncodingException
	 * @throws javax.xml.xpath.XPathExpressionException
	 */
	private void setGridstackItemProperties(GridstackItem item, Node itemNode)
			throws UnsupportedEncodingException, XPathExpressionException,
			JsonProcessingException {

		logger.debug("Entering setGridstackItemProperties");


		String reportIdString = xPath.evaluate("OBJECTID", itemNode);

		//allow use of QUERYID tag (legacy)
		if (StringUtils.isBlank(reportIdString)) {
			reportIdString = xPath.evaluate("QUERYID", itemNode);
		}

		//allow use of REPORTID tag (3.0+)
		if (StringUtils.isBlank(reportIdString)) {
			reportIdString = xPath.evaluate("REPORTID", itemNode);
		}
		
		if (StringUtils.isNotBlank(reportIdString)) {
			int reportId = Integer.parseInt(StringUtils.substringBefore(reportIdString, "&"));
			item.setReportId(reportId);
		}

		String title = xPath.evaluate("TITLE", itemNode);
		title = StringUtils.trimToEmpty(title);
		title = Encode.forHtmlContent(title);

		item.setTitle(title);

		int xPosition;
		String xPositionString = xPath.evaluate("XPOSITION", itemNode);
		if (StringUtils.isBlank(xPositionString)) {
			final int DEFAULT_X_POSITION = 0;
			xPosition = DEFAULT_X_POSITION;
		} else {
			xPosition = Integer.parseInt(xPositionString);
		}
		item.setxPosition(xPosition);

		int yPosition;
		String yPositionString = xPath.evaluate("YPOSITION", itemNode);
		if (StringUtils.isBlank(yPositionString)) {
			final int DEFAULT_Y_POSITION = 0;
			yPosition = DEFAULT_Y_POSITION;
		} else {
			yPosition = Integer.parseInt(yPositionString);
		}
		item.setyPosition(yPosition);

		int width;
		String widthString = xPath.evaluate("WIDTH", itemNode);
		if (StringUtils.isBlank(widthString)) {
			final int DEFAULT_WIDTH = 2;
			width = DEFAULT_WIDTH;
		} else {
			width = Integer.parseInt(widthString);
		}
		item.setWidth(width);

		int height;
		String heightString = xPath.evaluate("HEIGHT", itemNode);
		if (StringUtils.isBlank(heightString)) {
			final int DEFAULT_HEIGHT = 2;
			height = DEFAULT_HEIGHT;
		} else {
			height = Integer.parseInt(heightString);
		}
		item.setHeight(height);

		String autoheightString = xPath.evaluate("AUTOHEIGHT", itemNode);
		boolean autoheight = BooleanUtils.toBoolean(autoheightString);
		item.setAutoheight(autoheight);

		String autowidthString = xPath.evaluate("AUTOWIDTH", itemNode);
		boolean autowidth = BooleanUtils.toBoolean(autowidthString);
		item.setAutowidth(autowidth);

		String noResizeString = xPath.evaluate("NORESIZE", itemNode);
		boolean noResize = BooleanUtils.toBoolean(noResizeString);
		item.setNoResize(noResize);

		String noMoveString = xPath.evaluate("NOMOVE", itemNode);
		boolean noMove = BooleanUtils.toBoolean(noMoveString);
		item.setNoMove(noMove);

		String autopositionString = xPath.evaluate("AUTOPOSITION", itemNode);
		boolean autoposition = BooleanUtils.toBoolean(autopositionString);
		item.setAutoposition(autoposition);

		String lockedString = xPath.evaluate("LOCKED", itemNode);
		boolean locked = BooleanUtils.toBoolean(lockedString);
		item.setLocked(locked);

		int minWidth;
		String minWidthString = xPath.evaluate("MINWIDTH", itemNode);
		if (StringUtils.isBlank(minWidthString)) {
			final int DEFAULT_MIN_WIDTH = 0; //also used in showGridstackDashboardInline.jsp
			minWidth = DEFAULT_MIN_WIDTH;
		} else {
			minWidth = Integer.parseInt(minWidthString);
		}
		item.setMinWidth(minWidth);

		int minHeight;
		String minHeightString = xPath.evaluate("MINHEIGHT", itemNode);
		if (StringUtils.isBlank(minHeightString)) {
			final int DEFAULT_MIN_HEIGHT = 0; //also used in showGridstackDashboardInline.jsp
			minHeight = DEFAULT_MIN_HEIGHT;
		} else {
			minHeight = Integer.parseInt(minHeightString);
		}
		item.setMinHeight(minHeight);

		int maxWidth;
		String maxWidthString = xPath.evaluate("MAXWIDTH", itemNode);
		if (StringUtils.isBlank(maxWidthString)) {
			final int DEFAULT_MAX_WIDTH = 0; //also used in showGridstackDashboardInline.jsp
			maxWidth = DEFAULT_MAX_WIDTH;
		} else {
			maxWidth = Integer.parseInt(maxWidthString);
		}
		item.setMaxWidth(maxWidth);

		int maxHeight;
		String maxHeightString = xPath.evaluate("MAXHEIGHT", itemNode);
		if (StringUtils.isBlank(maxHeightString)) {
			final int DEFAULT_MAX_HEIGHT = 0; //also used in showGridstackDashboardInline.jsp
			maxHeight = DEFAULT_MAX_HEIGHT;
		} else {
			maxHeight = Integer.parseInt(maxHeightString);
		}
		item.setMaxHeight(maxHeight);
	}

}
