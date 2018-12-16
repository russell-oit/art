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
package art.parameter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Represents options for date range parameters
 *
 * @author Timothy Anyona
 */
public class DateRangeOptions implements Serializable {

	private static final long serialVersionUID = 1L;
	private DateRangeDestination fromParameter;
	private DateRangeDestination toParameter;
	private String format = "MMMM dd, yyyy";
	private String separator = " - ";
	private String direction = "ltr";
	private String startDate;
	private String endDate;
	private boolean autoApply;
	private boolean showDropdowns;
	private boolean showWeekNumbers;
	private boolean showISOWeekNumbers;
	private boolean showCustomRangeLabel = true;
	private boolean timePicker;
	private boolean timePicker24Hour;
	private int timePickerIncrement = 1;
	private boolean timePickerSeconds;
	private boolean linkedCalendars = true;
	private boolean autoUpdateInput = false;
	private boolean alwaysShowCalendars;
	private String opens = "right";
	private String drops = "down";
	private String buttonClasses = "btn btn-sm";
	private String applyClass = "btn-success";
	private String cancelClass = "btn-default";
	//https://stackoverflow.com/questions/21696784/how-to-declare-an-arraylist-with-values/21696869#21696869
	private List<String> ranges = new ArrayList<>(Arrays.asList("default"));

	/**
	 * @return the fromParameter
	 */
	public DateRangeDestination getFromParameter() {
		return fromParameter;
	}

	/**
	 * @param fromParameter the fromParameter to set
	 */
	public void setFromParameter(DateRangeDestination fromParameter) {
		this.fromParameter = fromParameter;
	}

	/**
	 * @return the toParameter
	 */
	public DateRangeDestination getToParameter() {
		return toParameter;
	}

	/**
	 * @param toParameter the toParameter to set
	 */
	public void setToParameter(DateRangeDestination toParameter) {
		this.toParameter = toParameter;
	}

	/**
	 * @return the format
	 */
	public String getFormat() {
		return format;
	}

	/**
	 * @param format the format to set
	 */
	public void setFormat(String format) {
		this.format = format;
	}

	/**
	 * @return the startDate
	 */
	public String getStartDate() {
		return startDate;
	}

	/**
	 * @param startDate the startDate to set
	 */
	public void setStartDate(String startDate) {
		this.startDate = startDate;
	}

	/**
	 * @return the endDate
	 */
	public String getEndDate() {
		return endDate;
	}

	/**
	 * @param endDate the endDate to set
	 */
	public void setEndDate(String endDate) {
		this.endDate = endDate;
	}

	/**
	 * @return the autoApply
	 */
	public boolean isAutoApply() {
		return autoApply;
	}

	/**
	 * @param autoApply the autoApply to set
	 */
	public void setAutoApply(boolean autoApply) {
		this.autoApply = autoApply;
	}

	/**
	 * @return the linkedCalendars
	 */
	public boolean isLinkedCalendars() {
		return linkedCalendars;
	}

	/**
	 * @param linkedCalendars the linkedCalendars to set
	 */
	public void setLinkedCalendars(boolean linkedCalendars) {
		this.linkedCalendars = linkedCalendars;
	}

	/**
	 * @return the autoUpdateInput
	 */
	public boolean isAutoUpdateInput() {
		return autoUpdateInput;
	}

	/**
	 * @param autoUpdateInput the autoUpdateInput to set
	 */
	public void setAutoUpdateInput(boolean autoUpdateInput) {
		this.autoUpdateInput = autoUpdateInput;
	}

	/**
	 * @return the timePicker
	 */
	public boolean isTimePicker() {
		return timePicker;
	}

	/**
	 * @param timePicker the timePicker to set
	 */
	public void setTimePicker(boolean timePicker) {
		this.timePicker = timePicker;
	}

	/**
	 * @return the timePicker24Hour
	 */
	public boolean isTimePicker24Hour() {
		return timePicker24Hour;
	}

	/**
	 * @param timePicker24Hour the timePicker24Hour to set
	 */
	public void setTimePicker24Hour(boolean timePicker24Hour) {
		this.timePicker24Hour = timePicker24Hour;
	}

	/**
	 * @return the timePickerIncrement
	 */
	public int getTimePickerIncrement() {
		return timePickerIncrement;
	}

	/**
	 * @param timePickerIncrement the timePickerIncrement to set
	 */
	public void setTimePickerIncrement(int timePickerIncrement) {
		this.timePickerIncrement = timePickerIncrement;
	}

	/**
	 * @return the timePickerSeconds
	 */
	public boolean isTimePickerSeconds() {
		return timePickerSeconds;
	}

	/**
	 * @param timePickerSeconds the timePickerSeconds to set
	 */
	public void setTimePickerSeconds(boolean timePickerSeconds) {
		this.timePickerSeconds = timePickerSeconds;
	}

	/**
	 * @return the separator
	 */
	public String getSeparator() {
		return separator;
	}

	/**
	 * @param separator the separator to set
	 */
	public void setSeparator(String separator) {
		this.separator = separator;
	}

	/**
	 * @return the direction
	 */
	public String getDirection() {
		return direction;
	}

	/**
	 * @param direction the direction to set
	 */
	public void setDirection(String direction) {
		this.direction = direction;
	}

	/**
	 * @return the showDropdowns
	 */
	public boolean isShowDropdowns() {
		return showDropdowns;
	}

	/**
	 * @param showDropdowns the showDropdowns to set
	 */
	public void setShowDropdowns(boolean showDropdowns) {
		this.showDropdowns = showDropdowns;
	}

	/**
	 * @return the showWeekNumbers
	 */
	public boolean isShowWeekNumbers() {
		return showWeekNumbers;
	}

	/**
	 * @param showWeekNumbers the showWeekNumbers to set
	 */
	public void setShowWeekNumbers(boolean showWeekNumbers) {
		this.showWeekNumbers = showWeekNumbers;
	}

	/**
	 * @return the showISOWeekNumbers
	 */
	public boolean isShowISOWeekNumbers() {
		return showISOWeekNumbers;
	}

	/**
	 * @param showISOWeekNumbers the showISOWeekNumbers to set
	 */
	public void setShowISOWeekNumbers(boolean showISOWeekNumbers) {
		this.showISOWeekNumbers = showISOWeekNumbers;
	}

	/**
	 * @return the showCustomRangeLabel
	 */
	public boolean isShowCustomRangeLabel() {
		return showCustomRangeLabel;
	}

	/**
	 * @param showCustomRangeLabel the showCustomRangeLabel to set
	 */
	public void setShowCustomRangeLabel(boolean showCustomRangeLabel) {
		this.showCustomRangeLabel = showCustomRangeLabel;
	}

	/**
	 * @return the alwaysShowCalendars
	 */
	public boolean isAlwaysShowCalendars() {
		return alwaysShowCalendars;
	}

	/**
	 * @param alwaysShowCalendars the alwaysShowCalendars to set
	 */
	public void setAlwaysShowCalendars(boolean alwaysShowCalendars) {
		this.alwaysShowCalendars = alwaysShowCalendars;
	}

	/**
	 * @return the opens
	 */
	public String getOpens() {
		return opens;
	}

	/**
	 * @param opens the opens to set
	 */
	public void setOpens(String opens) {
		this.opens = opens;
	}

	/**
	 * @return the drops
	 */
	public String getDrops() {
		return drops;
	}

	/**
	 * @param drops the drops to set
	 */
	public void setDrops(String drops) {
		this.drops = drops;
	}

	/**
	 * @return the buttonClasses
	 */
	public String getButtonClasses() {
		return buttonClasses;
	}

	/**
	 * @param buttonClasses the buttonClasses to set
	 */
	public void setButtonClasses(String buttonClasses) {
		this.buttonClasses = buttonClasses;
	}

	/**
	 * @return the applyClass
	 */
	public String getApplyClass() {
		return applyClass;
	}

	/**
	 * @param applyClass the applyClass to set
	 */
	public void setApplyClass(String applyClass) {
		this.applyClass = applyClass;
	}

	/**
	 * @return the cancelClass
	 */
	public String getCancelClass() {
		return cancelClass;
	}

	/**
	 * @param cancelClass the cancelClass to set
	 */
	public void setCancelClass(String cancelClass) {
		this.cancelClass = cancelClass;
	}

	/**
	 * @return the ranges
	 */
	public List<String> getRanges() {
		return ranges;
	}

	/**
	 * @param ranges the ranges to set
	 */
	public void setRanges(List<String> ranges) {
		this.ranges = ranges;
	}

}
