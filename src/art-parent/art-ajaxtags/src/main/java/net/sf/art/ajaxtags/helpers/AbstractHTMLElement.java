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

import java.util.Locale;
import java.util.Map.Entry;
import java.util.SortedMap;
import java.util.TreeMap;

import org.apache.commons.lang3.StringUtils;

import static org.apache.commons.lang3.StringUtils.trimToNull;

/**
 * This class should help to write HTML tags. We will have a simple append operation and a easy use
 * in connection with strings.
 *
 * TODO HTMLAnchorElement (A), HTMLLIElement (LI), HTMLLinkElement (LINK)
 */
public abstract class AbstractHTMLElement implements CharSequence, Appendable {

    /**
     * A simple enumeration holding some HTML-Attributes we are using.
     */
    public static enum HTMLAttribute {
        /** HTML class attribute. */
        CLASS,
        /** HTML id attribute. */
        ID,
        /** HTML href attribute. */
        HREF,
        /** HTML title attribute. */
        TITLE;
    }

    /**
     * Name of element. For start and end of tag.
     */
    private String name;

    /**
     * Body.
     */
    private CharSequence body;

    /**
     * Store the attributes of HTML element.
     */
    private SortedMap<Object, String> attributes;

    /**
     * Create a HTML element.
     *
     * @param name
     *            name of start and end tag
     * @param id
     *            id of element
     * @param body
     *            content
     */
    protected AbstractHTMLElement(final String name, final String id, final String body) {
        this.name = name;
        this.body = body;
        // if value is null then exception is thrown
        attributes = new TreeMap<Object, String>();
        if (id != null) {
            setId(id);
        }
    }

    /**
     * Create a HTML element.
     *
     * @param name
     *            the tag name
     */
    protected AbstractHTMLElement(final String name) {
        this(name, null);
    }

    /**
     * Create a HTML element.
     *
     * @param name
     *            the tag name
     * @param id
     *            the id attribute
     */
    protected AbstractHTMLElement(final String name, final String id) {
        this(name, id, null);
    }

    /**
     * @return the map of attributes
     */
    protected final SortedMap<Object, String> getAttributes() {
        return attributes;
    }

    /**
     * @return the tag name
     */
    protected final String getName() {
        return name;
    }

    /**
     * @return the body content or empty string if body is null
     */
    public final String getBody() {
        return body == null ? StringUtils.EMPTY : body.toString();
    }

    /**
     * Set the body content.
     *
     * @param body
     *            the body content
     */
    public final void setBody(final CharSequence body) {
        this.body = body;
    }

    /**
     * @return the class name attribute
     */
    public final String getClassName() {
        return attributes.get(HTMLAttribute.CLASS);
    }

    /**
     * Set the class name attribute.
     *
     * @param className
     *            the class attribute value
     */
    public final void setClassName(final String className) {
        attributes.put(HTMLAttribute.CLASS, className);
    }

    /**
     * @return the id attribute
     */
    public final String getId() {
        return attributes.get(HTMLAttribute.ID);
    }

    /**
     * Set the id attribute.
     *
     * @param id
     *            the id attribute value
     */
    public final void setId(final String id) {
        attributes.put(HTMLAttribute.ID, id);
    }

    /**
     * @return the length
     */
    public final int length() {
        return toString().length();
    }

    /**
     * @see CharSequence#charAt(int)
     * @param index
     *            the index
     * @return the char at index
     */
    public final char charAt(final int index) {
        return toString().charAt(index);
    }

    /**
     * @see CharSequence#subSequence(int, int)
     * @param start
     *            the start index
     * @param end
     *            the end index
     * @return the substring from start to end
     */
    public final CharSequence subSequence(final int start, final int end) {
        return toString().subSequence(start, end);
    }

    /**
     * Cleaning the attributes. This method does nothing by default.
     */
    protected void cleanAttributes() {
        // empty
    }

    /**
     * @return the string representation of this HTML element
     */
    @Override
    public final String toString() {
        final StringBuilder s = new StringBuilder("<");
        s.append(getName());
        cleanAttributes();
        for (Entry<Object, String> e : getAttributes().entrySet()) {
            final String value = trimToNull(e.getValue());
            if (value != null) {
                // if we do have a ENUM Object we try to get it in a lower case
                // type. toString should ensure the value is valid as key in HTML
                s.append(' ').append(e.getKey().toString().toLowerCase(Locale.getDefault()))
                        .append("=\"");
                s.append(value.replaceAll("\"", "\\\"")).append('"');
            }
        }
        s.append('>');
        s.append(getBody());
        s.append("</").append(getName()).append('>');
        return s.toString();
    }

    /**
     * Append {@link CharSequence} to this object.
     *
     * @param string
     *            the data to append
     * @return self
     */
    public final AbstractHTMLElement append(final CharSequence string) {
        setBody(getBody() + string);
        return this;
    }

    /**
     * Append a char.
     *
     * @param c
     *            the char to append
     * @return self
     */
    public final AbstractHTMLElement append(final char c) {
        return append(String.valueOf(c));
    }

    /**
     * @param csq
     *            the char sequence
     * @param start
     *            the start index
     * @param end
     *            the end index
     * @return self
     */
    public final AbstractHTMLElement append(final CharSequence csq, final int start, final int end) {
        return append(csq.subSequence(start, end));
    }
}
