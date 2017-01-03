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

import javax.servlet.jsp.JspException;

/**
 * Tag handler for the form field AJAX tag.
 */
public class AjaxFormFieldTag extends BaseAjaxTag {

    private static final long serialVersionUID = -7774526024294932262L;

    private boolean valueUpdateByName;

    @Override
    protected String getJsClass() {
        return JSCLASS_BASE + "UpdateField";
    }

    @Override
    protected OptionsBuilder getOptions() {
        final OptionsBuilder options = getOptionsBuilder();
        options.add("valueUpdateByName", String.valueOf(valueUpdateByName), false);
        return options;
    }

    @Override
    public int doEndTag() throws JspException {
        out(buildScript());
        return EVAL_PAGE;
    }

    @Override
    public void releaseTag() {
        valueUpdateByName = false;
    }

    public boolean getValueUpdateByName() {
        return valueUpdateByName;
    }

    public void setValueUpdateByName(final boolean valueUpdateByName) {
        this.valueUpdateByName = valueUpdateByName;
    }
}
