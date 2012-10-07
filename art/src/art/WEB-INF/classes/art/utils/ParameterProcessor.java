/*
 * Copyright (C) 2001/2004  Enrico Liboni  - enrico@computer.org
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
 * ParameterProcessor.java
 *
 * Caller:	QueryExecute, editJob.jsp Purpose:	parse the request parameter and
 * feed the hash tables Note:	Parameters that begin with _ are ignored as well
 * as the ones in the first IF below
 */
package art.utils;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import javax.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Takes query parameters in a http request and populates maps with their values
 *
 * @author Enrico Liboni
 * @author Timothy Anyona
 */
public class ParameterProcessor {

	final static Logger logger = LoggerFactory.getLogger(ParameterProcessor.class);

	/**
	 *
	 */
	public ParameterProcessor() {
	}

	/**
	 * Process the http request and fill objects with parameter values.
	 *
	 * @param request http request
	 * @param inlineParams new hashmap to store inline parameters
	 * @param multiParams new hashmap to store multi parameters
	 * @param queryId query id
	 * @return parameters to be shown in report if show parameters option is
	 * selected
	 */
	public static Map<Integer, ArtQueryParam> processParameters(HttpServletRequest request, Map<String, String> inlineParams, Map<String, String[]> multiParams, int queryId) {
		return processParameters(request, inlineParams, multiParams, queryId, null);
	}

	/**
	 * Process the http request and fill objects with parameter values.
	 *
	 * @param request http request
	 * @param inlineParams new hashmap to store inline parameters
	 * @param multiParams new hashmap to store multi parameters
	 * @param queryId query id
	 * @param htmlParams map that contains all the query's defined parameters
	 * @return parameters to be shown in report if show parameters option is
	 * selected
	 */
	public static Map<Integer, ArtQueryParam> processParameters(HttpServletRequest request, Map<String, String> inlineParams, Map<String, String[]> multiParams, int queryId, Map<String, ArtQueryParam> htmlParams) {

		Enumeration names = request.getParameterNames();
		String htmlName; //html element htmlName
		String label; //parameter label        
		Map<Integer, ArtQueryParam> displayParams = null; //display parameters. contains param position and list with display name and value                


		boolean showParams;
		ArtQuery aq = new ArtQuery();

		if (aq.alwaysShowParameters(queryId)) {
			//always show params, regardless of whether _showParams html parameter exists. especially for drill down queries
			showParams = true;
		} else {
			//consider the user's choice
			if (request.getParameter("_showParams") != null) {
				showParams = true;
			} else {
				showParams = false;
			}
		}

		if (showParams) {
			displayParams = new TreeMap<Integer, ArtQueryParam>(); //use treemap so that params can be displayed in field position order
		}

		if (htmlParams == null) {
			//get list of all valid parameters. used with display params and counters sql injection from direct url execution            
			htmlParams = aq.getHtmlParams(queryId);
		}

		while (names.hasMoreElements()) {
			// Get the htmlName of the parameter
			htmlName = (String) names.nextElement();

			//populate inline and multi parameters
			if (htmlName.startsWith("P_")) { //use startswith instead of substring(0,2) because chrome passes a parameter "-" which causes StringIndexOutOfBoundsException. reported by yidong123
				label = htmlName.substring(2);
				String paramValue = request.getParameter(htmlName);
				//only add parameters that actually exist. with direct url, someone can type non-existent parameters
				ArtQueryParam param = htmlParams.get(htmlName);
				if (param != null) {
					logger.debug("Adding inline parameter: {}", htmlName);

					inlineParams.put(label, paramValue);
					param.setParamValue(paramValue);
					
					if (showParams) {
						//populate display params map
					
						//for lov parameters, show both parameter value and friendly value
						if (param.usesLov()) {
							//get all possible lov values.							
							try {
								PreparedQuery pq = new PreparedQuery();
								pq.setQueryId(param.getLovQueryId());
								//for chained parameters, handle #filter# parameter
								int filterPosition = param.getFilterPosition();
								if (filterPosition > 0) {
									//parameter chained on another parameter. get filter parameter html name
									String valueParamHtmlName = param.getHtmlName(queryId, filterPosition);
									//get filter value. use request because inline and multi param maps haven't been populated with all parameter values yet
									String filter = request.getParameter(valueParamHtmlName); 									
									Map<String, String> filterParam = new HashMap<String, String>();
									filterParam.put("filter", filter);
									pq.setInlineParams(filterParam);
								}
								Map<String, String> lov = pq.executeLovQuery(false); //don't apply rules
								param.setLovValues(lov);
							} catch (Exception e) {
								logger.error("Error", e);
							}
						}

						displayParams.put(param.getFieldPosition(), param);
					}
				}
			} else if (htmlName.startsWith("M_")) { // it is a multiple param
				label = htmlName.substring(2);

				String[] paramValues = request.getParameterValues(htmlName); // get values string array for the parameter
				String firstValue = paramValues[0];

				//only add parameters that actually exist. with direct url, someone can type non-existent parameter
				ArtQueryParam param = htmlParams.get(htmlName);
				if (param != null) {
					if (!firstValue.equals("ALL_ITEMS")) { // if it is not ALL_ITEMS, add to the list
						logger.debug("Adding multi parameter: {}", htmlName);

						//check if this is a multi parameter that doesn't use an LOV
						String lines[] = firstValue.split("\\r?\\n");
						if (lines.length > 1) {
							//multi param that doesn't use LOV contains values separated by newlines
							paramValues = lines;
						}
						multiParams.put(label, paramValues);
						param.setParamValue(paramValues);
						
						if (showParams) {
							//populate display params map                                                                                       
							
							//for lov parameters, show both parameter value and friendly value
							if (param.usesLov()) {
								//get all possible lov values.								
								try {
									PreparedQuery pq = new PreparedQuery();
									pq.setQueryId(param.getLovQueryId());
									//for chained parameters, handle #filter# parameter
									int filterPosition = param.getFilterPosition();
									if (filterPosition > 0) {
										//parameter chained on another parameter. get filter value
										String valueParamHtmlName = param.getHtmlName(queryId, filterPosition);
										String filter = request.getParameter(valueParamHtmlName);										
										Map<String, String> filterParam = new HashMap<String, String>();
										filterParam.put("filter", filter);
										pq.setInlineParams(filterParam);
									}
									Map<String, String> lov = pq.executeLovQuery(false); //don't apply rules
									param.setLovValues(lov);
								} catch (Exception e) {
									logger.error("Error", e);
								}
							}

							displayParams.put(param.getFieldPosition(), param);
						}
					}
				}
			}

		}

		return displayParams;
	}
}
