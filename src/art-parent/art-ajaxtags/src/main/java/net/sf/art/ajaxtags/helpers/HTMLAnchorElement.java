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
package net.sf.art.ajaxtags.helpers;

/**
 * HTML &lt;a&gt; element.
 */
public class HTMLAnchorElement extends AbstractHTMLElement {

    public HTMLAnchorElement(String href) {
        super("a");
        setHref(href);
    }

    public String getHref() {
        return getAttributes().get(HTMLAttribute.HREF);
    }

    public void setHref(String href) {
        getAttributes().put(HTMLAttribute.HREF, href);
    }

    public String getTitle() {
        return getAttributes().get(HTMLAttribute.TITLE);
    }

    public void setTitle(String title) {
        getAttributes().put(HTMLAttribute.TITLE, title);
    }

}
