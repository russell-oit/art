/*
 * Copyright 2007-2012 AjaxTags-Team
 *
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package net.sf.art.ajaxtags.tags;

import static org.apache.commons.lang3.StringUtils.trimToNull;

import javax.servlet.jsp.JspException;

import net.sf.art.ajaxtags.helpers.HTMLAnchorElement;
import net.sf.art.ajaxtags.helpers.HTMLDivElement;

/**
 * Tag handler for the toggle (on/off, true/false) AJAX tag.
 */
public class AjaxToggleTag extends BaseAjaxTag {

    private static final long serialVersionUID = 6877730352175914711L;

    private String ratings;

    private String defaultRating;

    private String state;

    private boolean onOff;

    private String containerClass;

    private String messageClass;

    private String selectedClass;

    private String selectedOverClass;

    private String selectedLessClass;

    private String overClass;

    private String updateFunction;

    public String getUpdateFunction() {
        return updateFunction;
    }

    public void setUpdateFunction(final String updateFunction) {
        this.updateFunction = updateFunction;
    }

    public String getContainerClass() {
        return containerClass;
    }

    public void setContainerClass(final String containerClass) {
        this.containerClass = containerClass;
    }

    public String getDefaultRating() {
        return defaultRating;
    }

    public void setDefaultRating(final String defaultRating) {
        this.defaultRating = trimToNull(defaultRating);
    }

    public String getMessageClass() {
        return messageClass;
    }

    public void setMessageClass(final String messageClass) {
        this.messageClass = messageClass;
    }

    public String getOnOff() {
        return Boolean.toString(onOff);
    }

    public void setOnOff(final String onOff) {
        this.onOff = Boolean.parseBoolean(onOff);
    }

    public String getOverClass() {
        return overClass;
    }

    public void setOverClass(final String overClass) {
        this.overClass = overClass;
    }

    public String getRatings() {
        return ratings;
    }

    public void setRatings(final String ratings) {
        this.ratings = trimToNull(ratings);
    }

    public String getSelectedClass() {
        return selectedClass;
    }

    public void setSelectedClass(final String selectedClass) {
        this.selectedClass = selectedClass;
    }

    public String getSelectedLessClass() {
        return selectedLessClass;
    }

    public void setSelectedLessClass(final String selectedLessClass) {
        this.selectedLessClass = selectedLessClass;
    }

    public String getSelectedOverClass() {
        return selectedOverClass;
    }

    public void setSelectedOverClass(final String selectedOverClass) {
        this.selectedOverClass = selectedOverClass;
    }

    public String getState() {
        return state;
    }

    public void setState(final String state) {
        this.state = state;
    }

    @Override
    protected String getJsClass() {
        return JSCLASS_BASE + "Toggle";
    }

    @Override
    protected OptionsBuilder getOptions() {
        final OptionsBuilder options = getOptionsBuilder();
        options.add("ratings", ratings, true);
        options.add("containerClass", containerClass, true);
        options.add("selectedClass", selectedClass, true);
        options.add("selectedOverClass", selectedOverClass, true);
        options.add("selectedLessClass", selectedLessClass, true);
        options.add("overClass", overClass, true);
        options.add("messageClass", messageClass, true);
        options.add("state", state, true);
        // options.add("onOff", this.onOff, true);
        options.add("defaultRating", defaultRating, true);
        options.add("updateFunction", updateFunction, false);
        return options;
    }

    @Override
    public int doEndTag() throws JspException {
        // final boolean xOnOff = Boolean.parseBoolean(onOff);
        // write opening div
        final HTMLDivElement div = new HTMLDivElement(getSource());
        div.setClassName(getRatingDivClass(getContainerClass()));

        // write links
        final String[] ratingValues = getRatingValues();
        HTMLAnchorElement anchor;
        if (onOff) {
            anchor = new HTMLAnchorElement(AJAX_VOID_URL);
            if (ratingValues.length > 0) {
                // TODO StringUtils.equalsIgnoreCase(defaultRating, ratingValues[0]);
                if (defaultRating != null && defaultRating.equalsIgnoreCase(ratingValues[0])) {
                    anchor.setTitle(ratingValues[0]);
                    anchor.setClassName(selectedClass);
                } else {
                    anchor.setTitle(ratingValues[1]);
                }
            }
            div.append(anchor);
        } else {
            for (String val : ratingValues) {
                anchor = new HTMLAnchorElement(AJAX_VOID_URL);
                anchor.setTitle(val);
                if (val.equalsIgnoreCase(defaultRating)) {
                    anchor.setClassName(selectedClass);
                }
                div.append(anchor);
            }
        }

        // write script
        div.append(buildScript());
        out(div);
        return EVAL_PAGE;
    }

    private String[] getRatingValues() {
        return ratings == null ? new String[0] : ratings.split(",");
    }

    private String getRatingDivClass(final String containerClass) {
        return onOff ? containerClass + " onoff" : containerClass;
    }

    @Override
    public void releaseTag() {
        ratings = null; // NOPMD
        defaultRating = null; // NOPMD
        state = null; // NOPMD
        onOff = false;
        containerClass = null; // NOPMD
        messageClass = null; // NOPMD
        selectedClass = null; // NOPMD
        selectedOverClass = null; // NOPMD
        selectedLessClass = null; // NOPMD
        overClass = null; // NOPMD
    }
}
