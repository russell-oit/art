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
import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPathExpressionException;

import net.sf.art.ajaxtags.helpers.XMLUtils;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import static org.apache.commons.lang3.StringUtils.trimToNull;

/**
 * Rewrites HTML anchor tags (&lt;A&gt;), replacing the href attribute with an onclick event so that
 * retrieved content is loaded inside a region on the page.
 */
public class AjaxAnchorsTag extends BaseAjaxBodyTag {

    private static final long serialVersionUID = -1732745741282114289L;

    /** Warp dirty hack to use internal HTML parser. */
    private static final String WARP0 = "<div>";
    private static final String WARP1 = "</div>";

    /**
     * Rewrite the body and make use of AJAX. Rewrite all &lt;a&gt; links to use javascript calls to
     * Prototype.
     *
     * @return EVAL_PAGE
     * @throws JspException
     *             if an error occurred while processing this tag
     */
    @Override
    public int doEndTag() throws JspException {
        out(processBody());
        return EVAL_PAGE;
    }

    /**
     * Rewrite anchors in body content of the tag.
     *
     * @return rewritten and reformatted XHTML content
     * @throws JspException
     *             on errors
     */
    public String processBody() throws JspException {
        final String content = getBody();
        try {
            return processContent(content);
        } catch (XPathExpressionException e) {
            throw new JspException(getClass().getSimpleName()
                    + ": rewrite links failed (wrong XPath expression)\n" + content, e);
        } catch (TransformerException e) {
            throw new JspException(getClass().getSimpleName()
                    + ": rewrite links failed (cannot transform XHTML to text)\n" + content, e);
        } catch (SAXException e) {
            throw new JspException(getClass().getSimpleName()
                    + ": rewrite links failed (invalid XHTML content)\n" + content, e);
        }
    }

    protected String processContent(final String content) throws XPathExpressionException,
            TransformerException, SAXException {
        return rewriteAnchors(content, getTarget(), getSourceClass());
    }

    /**
     * Rewrite anchors.
     *
     * @param html
     *            XHTML source
     * @param target
     *            target of request
     * @param className
     *            CSS class name of anchor elements
     * @return rewritten and reformatted XHTML text
     * @throws SAXException
     *             if any parse errors occur
     * @throws TransformerException
     *             if it is not possible to transform document to string
     * @throws XPathExpressionException
     *             if XPath expression cannot be evaluated (wrong XPath expression)
     */
    protected String rewriteAnchors(final String html, final String target, final String className)
            throws XPathExpressionException, TransformerException, SAXException {
        final Document document = getDocument(html);
        final NodeList links = XMLUtils
                .evaluateXPathExpression(getAnchorXPath(className), document);
        rewriteLinks(links, target);
        return XMLUtils.toString(document);
    }

    private static String getAnchorXPath(final String className) {
        return className == null ? "//a" : "//a[@class=\"" + className + "\"]";
    }

    protected void rewriteLinks(final NodeList links, final String target) {
        for (int i = 0, len = links.getLength(); i < len; i++) {
            rewriteLink(links.item(i), target);
        }
    }

    /**
     * Rewrite link. Change (or create) "onclick" attribute, set "href" attribute to
     * "javascript://nop/".
     *
     * @param link
     *            node of document with link
     * @param target
     *            target of request
     */
    protected final void rewriteLink(final Node link, final String target) {
        final NamedNodeMap map = link.getAttributes();
        final Attr href = (Attr) map.getNamedItem("href");
        if (href != null) {
            Attr onclick = (Attr) map.getNamedItem("onclick");
            if (onclick == null) {
                onclick = link.getOwnerDocument().createAttribute("onclick");
                map.setNamedItem(onclick);
            }
            onclick.setValue(getOnclickAjax(target, href.getValue(), getOptionsBuilder()));
            href.setValue("javascript://nop/");
        }
    }

    /**
     * Parse XHTML document from given string.
     *
     * @param html
     *            string with XHTML content
     * @return parsed document (wrapped into DIV) or null
     * @throws SAXException
     *             if any parse errors occur
     */
    protected static final Document getDocument(final String html) throws SAXException {
        final String xhtml = trimToNull(html); // .replaceAll("<br(.*?)>", "<br$1/>");
        return xhtml == null ? null : XMLUtils.getXMLDocument(WARP0 + xhtml + WARP1);
    }
}
