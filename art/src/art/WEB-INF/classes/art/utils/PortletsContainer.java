/*
* Copyright (C) 2001/2003  Enrico Liboni  - enrico@computer.org
*
*   This program is free software; you can redistribute it and/or modify
*   it under the terms of the GNU General Public License as published by
*   the Free Software Foundation;
*
*   This program is distributed in the hope that it will be useful,
*   but WITHOUT ANY WARRANTY; without even the implied warranty of
*   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
*   GNU General Public License for more details.
*
*   You should have received a copy of the GNU General Public License
*   (version 2) along with this program (see documentation directory); 
*   otherwise, have a look at http://www.gnu.org or write to the Free Software
*   Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
*  
*/

/** 
* This object parses the portlet container XML definitions
* and returns value to showPortlets.jsp page
*
*/

package art.utils;

import art.servlets.ArtDBCP;

import java.util.*;
import java.sql.*;
import javax.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * This object parses the portlet container(dashboard) XML definitions
 * and returns value to showPortlets.jsp page
 * 
 * @author Enrico Liboni
 */
public class PortletsContainer {
    
    final static Logger logger = LoggerFactory.getLogger(PortletsContainer.class);
    
	int columnsCount;  // number of columns in this portlet container
	int objectId;
	List[] portletsV; // portlets[2] contains a list of Xml strings representing the portlets on third column
	String[] columnClass;
	int[] objectsIdsArray;

	String title;  
	String description;  

    /**
     * 
     */
    public PortletsContainer() {
	}

	/** Load information and build data structure
	*  with the XML tags values
     * 
     * @param i
     * @throws ArtException  
     */
	public void setObjectId(int i) throws ArtException{
		objectId = i;
		
		/* Load portlets info */
		Connection conn =null;
		try {
			String portletXml = null;
			conn = ArtDBCP.getConnection();  
						
			ArtQuery aq = new  ArtQuery();
			if (aq.create(conn, objectId)) {
				title = aq.getShortDescription();
				description = aq.getDescription();
				portletXml = aq.getText();
			} else {
				throw new ArtException("Not able to load header or source code for portlet id: " + objectId);
			}

			List<String> columnsV = XmlParser.getXmlElementValues(portletXml, "COLUMN");
			columnsCount = columnsV.size(); // number of cols

			portletsV = new List[columnsCount];
			columnClass = new String[columnsCount];

			for(i=0;i< columnsCount;i++) { // for each column
				String columnXml = columnsV.get(i);
				String size = XmlParser.getXmlElementValue( columnXml, "SIZE");
				columnClass[i] = (size==null?"auto":size);
				portletsV[i]   = XmlParser.getXmlElementValues(columnXml, "PORTLET");
			} 
			
			
		} catch(Exception e) {
            logger.error("Error",e);
			throw new ArtException("Exception while initializing portlet id: " + objectId +" Exception: " + e);
		} finally {
			try {
				if(conn!=null){
					conn.close();
				}
			} catch (SQLException e) {
                logger.error("Error",e);
				throw new ArtException("Not able to close connection properly for portlet id: " + objectId );
			}
		}
	}

    /**
     * 
     * @param portletId
     * @return query ids of all art objects in the dashboard
     * @throws ArtException
     */
    public static int[] getPortletsObjectsId(int portletId) throws ArtException {
		int[] objectsIdArray;
		Connection conn =null;
		try {
			String portletXml = null;
			conn = ArtDBCP.getConnection();  
			ArtQuery aq = new ArtQuery();
			if (aq.create(conn,portletId)) {   
				portletXml = aq.getText();
			} else {
				throw new ArtException("Not able to load source code for portlet id: " + portletId );
			}
			// get list of objects id used by this portlet
			List<String> objectsV = XmlParser.getXmlElementValues(portletXml, "OBJECTID");
			objectsIdArray = new int[objectsV.size()];
			for(int i=0; i<objectsV.size();i++) {
				objectsIdArray[i] = Integer.parseInt(objectsV.get(i));
			}
		} catch(Exception e) {
            logger.error("Error",e);
			throw new ArtException("Exception while getting objects array for portlet id: " + portletId +" Exception: " + e);
		} finally {
			try {
				if(conn!=null){
					conn.close();
				}
			} catch (SQLException e) {
                logger.error("Error",e);
				throw new ArtException("Not able to close connection properly for portlet id: " + portletId);
			}
		}
		return objectsIdArray;
	}


	/** Returns the link to embed in the portlet.
	*  It can be a absolute URL (specified within the <URL> tag)
	*  or the link to execute an Art Object (query, graph, text)
     * 
     * @param col
     * @param row 
     * @param request 
     * @return the link to embed in the portlet
     * @throws ArtException  
     */
	public String getPortletLink(int col, int row, HttpServletRequest request) throws ArtException{
		// Get the portlet xml info
		String portletXml = (String) portletsV[col].get(row);
		String link = XmlParser.getXmlElementValue(portletXml, "OBJECTID");
		if (link == null) { 
			return XmlParser.getXmlElementValue(portletXml, "URL");
		} else {
			// context path as suffix + build url + switch off html header&footer and add parameters
			StringBuilder linkSb = new StringBuilder();
			boolean getDefaultParameters = true;
			java.util.Enumeration names = request.getParameterNames();
			while (names.hasMoreElements()) {
				String name = (String) names.nextElement();
				if (name.startsWith("P_")) {
					linkSb.append("&"+name+"="+request.getParameter(name));
					getDefaultParameters = false; // the URL has parameters, thus we'll not get the defaults
				}
			}
			link = ObjectUrl.getExecuteUrl(Integer.parseInt(link), false, getDefaultParameters) +"&_isFragment=Y";
			return request.getContextPath() + link  + linkSb.toString();
		}
	}

    /**
     * 
     * @param col
     * @param row
     * @return portlet refresh interval
     * @throws ArtException
     */
    public String getPortletRefresh(int col, int row) throws ArtException{
		// Get the portlet xml info
		String portletXml = (String) portletsV[col].get(row);
		String value = XmlParser.getXmlElementValue(portletXml, "REFRESH"); 
		if (value != null && Integer.parseInt(value) < 5 ){
			value = "5";
		}
               		
		return value;
	}

    /**
     * 
     * @param col
     * @param row
     * @return <code>true</code> if portlet to be executed on load
     * @throws ArtException
     */
    public boolean getPortletOnLoad(int col, int row) throws ArtException{
		// Get the portlet xml info
		String portletXml = (String) portletsV[col].get(row);
		String value = XmlParser.getXmlElementValue(portletXml, "ONLOAD"); 
		
		boolean executeOnLoad=true;
		if(value!=null && value.toLowerCase().equals("false")){
			executeOnLoad=false;
		}
		
		return executeOnLoad;
	}

    /**
     * 
     * @param col
     * @param row
     * @return title for a particular portlet
     * @throws ArtException
     */
    public String getPortletTitle(int col, int row) throws ArtException{
		// Get the portlet xml info
		String portletXml = (String) portletsV[col].get(row);
		return XmlParser.getXmlElementValue(portletXml, "TITLE");
	}

	/** Returns the column size, i.e. the css class name
	*  used to render the portlet
	*  Values are: small, medium, large, auto
     * 
     * @param col
     * @return the css class name used to render the portlet
     */
	public String getPortletClass(int col){
		return columnClass[col].toUpperCase(); // common for all portlets of same column
	}

	/* Container attributes */

	/** returns the number of columns in this PortletContainer
     * @return the number of columns in this PortletContainer
     */
	public int getColumnsCount(){
		return columnsCount;
	}

	/** returns the number of portlets in the PortletContainer's column
     * @param col
     * @return the number of portlets in the PortletContainer's column
     */
	public int getPortletsCount(int col){
		return portletsV[col].size();
	}

    /**
     * 
     * @return title
     */
    public String getPortletsContainerTitle(){
		return title;
	}

    /**
     * 
     * @return description
     */
    public String getPortletsContainerDescr(){
		return description;
	}

}
