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

import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPathExpressionException;

import net.sf.art.ajaxtags.helpers.XMLUtils;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * Wraps a DisplayTag (http://displaytag.org) table, enabling AJAX capabilities. In the process,
 * anchors in the navigation are rewritten on the fly so that the DisplayTag table refreshes within
 * the same region on the page without a full-page reload.
 */
public class AjaxDisplayTag extends AjaxAreaTag {

    private static final long serialVersionUID = -5945152631578965550L;

    private String pagelinksClass;

    private String columnClass;

    /**
     * Default constructor.
     */
    public AjaxDisplayTag() {
        super();
        init();
    }

    /**
     * Initialize properties to default values. Used in {@link #AjaxDisplayTag()} and in
     * {@link #releaseTag()}.
     */
    private void init() {
        pagelinksClass = "pagelinks";
        columnClass = "sortable";
    }

    /**
     * @return Returns the pagelinksClass.
     */
    public String getPagelinksClass() {
        return pagelinksClass;
    }

    /**
     * @param pagelinksClass
     *            The pagelinksClass to set. Null-safe.
     */
    public void setPagelinksClass(final String pagelinksClass) {
        this.pagelinksClass = trimToNull(pagelinksClass);
    }

    /**
     * @return Returns the columnClass.
     */
    public String getColumnClass() {
        return columnClass;
    }

    /**
     * @param columnClass
     *            The columnClass to set. Null-safe.
     */
    public void setColumnClass(final String columnClass) {
        this.columnClass = trimToNull(columnClass);
    }

    /**
     * @see javax.servlet.jsp.tagext.Tag#release()
     */
    @Override
    public void releaseTag() {
        super.releaseTag();
        init();
    }

    /**
     * Rewrite anchors in content.
     *
     * Parse content to XHTML {@link org.w3c.dom.Document}, rewrite DisplayTag anchor elements and
     * return string representation of document.
     *
     * @param content
     *            XHTML source as string
     * @return content with rewritten anchors
     * @throws SAXException
     *             if any parse errors occur
     * @throws TransformerException
     *             if it is not possible to transform document to string
     * @throws XPathExpressionException
     *             if XPath expression cannot be evaluated (wrong XPath expression)
     * @see net.sourceforge.ajaxtags.tags.AjaxAreaTag#processContent(java.lang.String)
     */
    @Override
    protected String processContent(final String content) throws TransformerException,
            SAXException, XPathExpressionException {
        return rewriteAnchors(content, getId());
    }

    protected String rewriteAnchors(final String html, final String target)
            throws TransformerException, SAXException, XPathExpressionException {
        final Document document = getDocument(html);

        final NodeList pagelinkAnchors = findRewritableLinksFor(document, "span",
                getPagelinksClass());
        rewriteLinks(pagelinkAnchors, target);

        final NodeList columnAnchors = findRewritableLinksFor(document, "th", getColumnClass());
        rewriteLinks(columnAnchors, target);

        return XMLUtils.toString(document);
    }

    private static NodeList findRewritableLinksFor(final Document document, final String tagName,
            final String className) throws XPathExpressionException {
        return XMLUtils.evaluateXPathExpression(getAnchorXPath(tagName, className), document);
    }

    private static String getAnchorXPath(final String tagName, final String className) {
        // contains(concat(' ', @class, ' '), ' class ')
        // contains(concat(' ', normalize-space(@class), ' '), ' class ')
        // contains(tokenize(@class, '\s'), "class")
        return className == null ? "//" + tagName + "/a" : "//" + tagName + "[@class=\""
                + className + "\" or contains(concat(' ',@class,' '),' " + className + " ')]/a";
    }

    @Override
    protected String rewriteAnchors(String html, String target, String className) {
        throw new UnsupportedOperationException(
                "This method should never be called. Use rewriteAnchors(String, String) instead.");
    }

}
