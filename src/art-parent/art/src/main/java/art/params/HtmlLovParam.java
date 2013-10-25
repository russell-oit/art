/**
 * Copyright 2001-2013 Enrico Liboni <eliboni@users.sourceforge.net>
 *
 * This file is part of ART.
 *
 * ART is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, version 2 of the License.
 *
 * ART is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with ART.  If not, see <http://www.gnu.org/licenses/>.
 */
/**
 * Lov Box
 *
 * Used in showParams.jsp page to display lov parameters and chained parameter
 * fields
 *
 * When Who What 20070918 john added line to compare with default value (line
 * ~109)
 */
package art.params;

import art.utils.PreparedQuery;
import java.util.Map;
import java.util.ResourceBundle;
import org.apache.commons.lang3.StringUtils;
import org.owasp.encoder.Encode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class to display either inline or multi parameters that use LOVs, or chained
 * parameters.
 *
 * @author Enrico Liboni
 * @author John
 * @author Timothy Anyona
 */
public class HtmlLovParam implements ParamInterface {

	final static Logger logger = LoggerFactory.getLogger(HtmlLovParam.class);
	String username, paramHtmlId, paramHtmlName, paramName, paramShortDescr, paramDescr, defaultValue, chainedParamId;
	boolean useRules;
	boolean isMulti = false;
	int lovQueryId;
	String chainedValueId;
	ResourceBundle messages;

	/**
	 * Constructor.
	 *
	 * @param paramHtmlId id of html element
	 * @param paramHtmlName name of html element
	 * @param paramName user friendly name of parameter
	 * @param paramShortDescr short description of parameter
	 * @param paramDescr description of parameter
	 * @param defaultValue default value
	 * @param lovQueryId query id for the lov
	 * @param chainedParamId html object id of the parameter that will trigger
	 * this one
	 * @param useSmartRules determine if rules should be applied to the lov
	 * query
	 * @param username user that is running the query
	 * @param chainedValueId html object id of the parameter whose value this
	 * one is based on
	 */
	public HtmlLovParam(String paramHtmlId, String paramHtmlName, String paramName, String paramShortDescr, String paramDescr, String defaultValue, int lovQueryId, String chainedParamId, boolean useSmartRules, String username, String chainedValueId) {

		this.paramHtmlId = paramHtmlId;
		this.paramHtmlName = paramHtmlName;
		this.paramName = paramName;
		this.paramShortDescr = paramShortDescr;
		this.paramDescr = paramDescr;
		this.defaultValue = defaultValue;
		this.lovQueryId = lovQueryId;
		this.chainedParamId = chainedParamId;
		this.useRules = useSmartRules;
		this.username = username;
		this.chainedValueId = chainedValueId;

		if (defaultValue == null || defaultValue.equals("null")) {
			this.defaultValue = "";
		}

		if (paramHtmlName.startsWith("M_")) {
			isMulti = true;
		}
	}
	
	@Override
    public String getDefaultValue() {
        return defaultValue;
    }

	@Override
	public String getChainedValueId() {
		return chainedValueId;
	}

	@Override
	public String getId() {
		return paramHtmlId;
	}

	@Override
	public String getHtmlName() {
		return paramHtmlName;
	}

	@Override
	public String getName() {
		return paramName;
	}

	@Override
	public String getValueBox() {
		return getValueBox(defaultValue);
	}

	@Override
	public String getValueBox(String value) {
		String vBox;

		if (chainedParamId != null) {
			//ajaxed
			vBox = "\n<select id=\"" + paramHtmlId + "\" name=\"" + paramHtmlName + "\" " + (isMulti ? "size=\"5\" multiple" : "") + "><option value=\"\">...</option></select>";
		} else {
			vBox = getValues(value);
		}

		return vBox;
	}

	@Override
	public boolean isChained() {
		if (chainedParamId != null) {
			return true;
		} else {
			return false;
		}
	}

	@Override
	public String getChainedId() {
		return chainedParamId;
	}

	/**
	 * Returns the lov query id for ajax.
	 *
	 * @return the lov query id
	 */
	@Override
	public String getParamClass() {
		return "" + lovQueryId;
	}

	@Override
	public String getShortDescr() {
		return paramShortDescr;
	}

	@Override
	public String getDescr() {
		return paramDescr;
	}

	/**
	 * Get the html code required for capturing a non-chained parameter.
	 *
	 * @return the html code required for capturing a non-chained parameter.
	 */
	private String getValues(String initialValue) {
		String values = "";

		if (initialValue == null) {
			//no parameter value override. use default value
			initialValue = defaultValue;
		}

		PreparedQuery pq = null;

		try {
			pq = new PreparedQuery();
			pq.setUsername(username); //in case lov should use rules
			pq.setQueryId(lovQueryId);

			String selected;
			String value;
			String viewColumnValue;

			StringBuilder sb = new StringBuilder(1024);
			sb.append("\n<select " + " id=\"").append(paramHtmlId).append("\"" + " name=\"");
			sb.append(paramHtmlName).append("\" ").append(isMulti ? "size=\"5\" multiple" : "").append(">");
			if (isMulti) {
				if (StringUtils.equals(initialValue, "ALL_ITEMS") || StringUtils.equals(initialValue, "All")) {
					selected = "selected";
				} else {
					selected = "";
				}
				String allString = "All";
				if (messages != null) {
					allString = messages.getString("allItems");
				}
				sb.append("<option value=\"ALL_ITEMS\" ").append(selected).append(">").append(allString).append("</option>");
			}

			Map<String, String> lov = pq.executeLovQuery(useRules); //override lov use rules setting with setting defined in the parameter definition
			for (Map.Entry<String, String> entry : lov.entrySet()) {
				// build html option list
				value = entry.getKey();
				viewColumnValue = entry.getValue();

				if (StringUtils.equals(initialValue, viewColumnValue) || StringUtils.equals(initialValue, value)) {
					selected = "selected";
				} else {
					selected = "";
				}
				sb.append("<option value=\"");
				sb.append(Encode.forHtmlAttribute(value));
				sb.append("\" ");
				sb.append(selected);
				sb.append(">");
				sb.append(Encode.forHtmlContent(viewColumnValue));
				sb.append("</option>");
			}

			sb.append("\n</select>\n");
			values = sb.toString();

		} catch (Exception e) {
			values = "Error: " + e;
			logger.error("Error", e);
		} finally {
			if (pq != null) {
				pq.close();
			}
		}

		return values;
	}

	@Override
	public void setMessages(ResourceBundle msgs) {
		messages = msgs;
	}
}
