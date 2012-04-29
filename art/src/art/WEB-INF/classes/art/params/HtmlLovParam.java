/**
Lov Box

Used in showParams.jsp page to display lov parameters and chained parameter fields

When     Who  What
20070918 john added line to copmare with default value (line ~109)
 */
package art.params;

import art.utils.PreparedQuery;

import java.util.Iterator;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Class to display either inline or multi parameters that use LOVs, or chained parameters.
 * 
 * @author Enrico Liboni
 * @author John
 * @author Timothy Anyona
 */
public class HtmlLovParam implements ParamInterface {
    
    final static Logger logger = LoggerFactory.getLogger(HtmlLovParam.class);
    
    String username, paramHtmlId, paramHtmlName, paramName, paramShortDescr, paramDescr, defaultValue, chainedParamId;
    boolean useSmartRules;
    boolean isMulti = false;
    int lovQueryId;
    String chainedValueId;

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
     * @param chainedParamId html object id of the parameter that will trigger this one
     * @param useSmartRules determine if rules should be applied to the lov query
     * @param username user that is running the query
     * @param chainedValueId html object id of the parameter whose value this one is based on
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
        this.useSmartRules = useSmartRules;
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
        String vBox;

        if (chainedParamId != null) {
            //ajaxed
            vBox = "\n<select id=\"" + paramHtmlId + "\" name=\"" + paramHtmlName + "\" " + (isMulti ? "size=\"5\" multiple" : "") + "><option value=\"\">...</option></select>";
        } else {
            vBox = getValues();
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
     * @return the html code required for capturing a non-chained parameter.
     */
    private String getValues() {
        String values = "";

        PreparedQuery pq = null;

        try {
            pq = new PreparedQuery();
            pq.setUsername(username);
            pq.setQueryId(lovQueryId);
            pq.isUseSmartRules(useSmartRules);
                        
            String selected;
            String value;
            String viewColumnValue;

            StringBuilder sb = new StringBuilder(1024);
            sb.append("\n<select "
                    + " id=\"" + paramHtmlId + "\""
                    + " name=\"" + paramHtmlName + "\" " + (isMulti ? "size=\"5\" multiple" : "") + ">");
            if (isMulti) {
                if (defaultValue.equals("ALL_ITEMS") || defaultValue.equals("All")) {
                    selected = "selected";
                } else {
                    selected = "";
                }
                sb.append("<option value=\"ALL_ITEMS\" " + selected + ">All</option>");
            }

            Map<String,String> lov = pq.executeLovQuery();
            Iterator it=lov.entrySet().iterator();
            while (it.hasNext()) {
                // build html option list
                Map.Entry entry=(Map.Entry) it.next();                
                value = (String) entry.getKey();
                viewColumnValue = (String) entry.getValue();
                
                if (defaultValue.equals(viewColumnValue) || defaultValue.equals(value)) {
                    selected = "selected";
                } else {
                    selected = "";
                }
                sb.append("<option value=\"");
                sb.append(value);
                sb.append("\" ");
                sb.append(selected);
                sb.append(">");
                sb.append(viewColumnValue);
                sb.append("</option>");
            }
            
            sb.append("\n</select>\n");
            values = sb.toString();

        } catch (Exception e) {
            values = "Error: " + e;
            logger.error("Error",e);
        } finally {
            pq.close();
        }

        return values;
    }
}
