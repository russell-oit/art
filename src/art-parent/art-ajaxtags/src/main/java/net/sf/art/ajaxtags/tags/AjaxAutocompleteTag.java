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

/**
 * Tag handler for the autocomplete AJAX tag.
 */
public class AjaxAutocompleteTag extends BaseAjaxTag {

    private static final long serialVersionUID = -6332721548834673822L;

    private String minimumCharacters;

    private String appendSeparator;

    /**
     * CSS class name to apply to the popup autocomplete dropdown. Default: "autocomplete".
     */
    private String className = "autocomplete";

    private String indicator;

    /**
     * Function to execute after user has selected some option and source and target fields were
     * updated with selected values.
     */
    private String afterUpdate;

    /**
     * True to automatically select entry and hide autocompleter if there is no choice (only one
     * entry to display). Default: false.
     */
    private boolean autoSelect;

    private String paramName;

    public String getAppendSeparator() {
        return appendSeparator;
    }

    public void setAppendSeparator(final String appendSeparator) {
        this.appendSeparator = appendSeparator;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(final String className) {
        this.className = trimToNull(className);
    }

    public String getIndicator() {
        return indicator;
    }

    public void setIndicator(final String indicator) {
        this.indicator = indicator;
    }

    public String getMinimumCharacters() {
        return minimumCharacters;
    }

    public void setMinimumCharacters(final String minimumCharacters) {
        this.minimumCharacters = minimumCharacters;
    }

    public String getAfterUpdate() {
        return afterUpdate;
    }

    public void setAfterUpdate(final String afterUpdate) {
        this.afterUpdate = trimToNull(afterUpdate);
    }

    public boolean isAutoSelect() {
        return autoSelect;
    }

    public void setAutoSelect(boolean autoSelect) {
        this.autoSelect = autoSelect;
    }

    public String getParamName() {
        return paramName;
    }

    public void setParamName(final String paramName) {
        this.paramName = trimToNull(paramName);
    }

    @Override
    protected String getJsClass() {
        return JSCLASS_BASE + "Autocomplete";
    }

    @Override
    protected OptionsBuilder getOptions() {
        final OptionsBuilder options = getOptionsBuilder();
        options.add("className", className, true);
        options.add("indicator", indicator, true);
        options.add("minChars", minimumCharacters, true);
        options.add("appendSeparator", appendSeparator, true);
        options.add("afterUpdate", afterUpdate, false);
        options.add("autoSelect", autoSelect);
        options.add("paramName", paramName, true);
        return options;
    }

    @Override
    public int doEndTag() throws JspException {
        out(buildScript());
        return EVAL_PAGE;
    }

    @Override
    public void releaseTag() {
        className = null; // NOPMD
        minimumCharacters = null; // NOPMD
        appendSeparator = null; // NOPMD
        indicator = null; // NOPMD
        afterUpdate = null; // NOPMD
    }
}
