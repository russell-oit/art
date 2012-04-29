/**
Date box

Used in showParams.jsp page to display date and datetime parameter fields

20080128		added code in constructor to allow an offset
(from current date) to be specified in the default value
 */
package art.params;

import art.utils.PreparedQuery;

import java.util.*;
import java.text.SimpleDateFormat;

/**
 * Class to display DATE and DATETIME parameters.
 * 
 * @author Enrico Liboni
 * @author Timothy Anyona
 */
public class HtmlDateInput implements ParamInterface {

    String paramHtmlId, paramHtmlName, paramName, paramShortDescr, paramDescr, defaultValue;
    boolean datetime;
    String paramClass; //can be DATE or DATETIME
    String calendarFormat; //dhtmlgoodies calendar format
    String inputSize; //size of html input element where date is displayed
    String displayTime; //whether to display time in dhtmlgoodies calendar
    String maxLength; //max length of the html box

    /**
     * Constructor.
     * 
     * @param paramHtmlId id of html element
     * @param paramHtmlName name of html element
     * @param paramName user friendly name of parameter
     * @param paramShortDescr short description of parameter
     * @param paramDescr long description of parameter
     * @param defaultValue default value
     * @param pClass parameter class. Either DATE or DATETIME.
     */
    public HtmlDateInput(String paramHtmlId, String paramHtmlName, String paramName, String paramShortDescr, String paramDescr, String defaultValue, String pClass) {
        this.paramHtmlId = paramHtmlId;
        this.paramHtmlName = paramHtmlName;
        this.paramName = paramName;
        this.paramShortDescr = paramShortDescr;
        this.paramDescr = paramDescr;

        paramClass = pClass;

        String defaultDateFormat; //date format for use with simpledateformat object
        if ("DATETIME".equals(paramClass)) {
            datetime = true;
            calendarFormat = "yyyy-mm-dd hh:ii";
            inputSize = "19";
            displayTime = "true";
            maxLength = "19";

            defaultDateFormat = "yyyy-MM-dd HH:mm:ss";
        } else {
            datetime = false;
            calendarFormat = "yyyy-mm-dd";
            inputSize = "11";
            displayTime = "false";
            maxLength = "20";

            defaultDateFormat = "yyyy-MM-dd";
        }

        //convert default value to a valid date string
        Date defaultDate = PreparedQuery.setDefaultValueDate(defaultValue);
        SimpleDateFormat dateFormatter = new SimpleDateFormat(defaultDateFormat);
        this.defaultValue = dateFormatter.format(defaultDate);
    }

    @Override
    public String getChainedValueId() {
        return null;
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
        String vBox = "<input type=\"text\" size=\"" + inputSize + "\" maxlength=\"" + maxLength + "\" "
                + " id=\"" + paramHtmlId + "\""
                + " name=\"" + paramHtmlName + "\""
                + " value = \"" + defaultValue + "\" > "
                + "<img onclick=\"displayCalendar(document.artparameters."
                + paramHtmlName + ",'" + calendarFormat + "',this," + displayTime + ")\" src=\"../images/calendar.png\">";

        return vBox;
    }

    @Override
    public boolean isChained() {
        return false;
    }

    @Override
    public String getChainedId() {
        return null;
    }

    /** 
     * Get the parameter class
     * 
     * @return DATE or DATETIME
     */
    @Override
    public String getParamClass() {
        return paramClass;
    }

    @Override
    public String getShortDescr() {
        return paramShortDescr;
    }

    @Override
    public String getDescr() {
        return paramDescr;
    }
}
