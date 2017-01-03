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

import java.io.IOException;

import javax.servlet.jsp.JspException;
import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPathExpressionException;

import org.xml.sax.SAXException;

import net.sf.art.ajaxtags.helpers.HTMLDivElement;

/**
 * Wraps any area on the page (with a DIV element) so that actions within that area refresh/load
 * inside the defined DIV region rather than inside the whole browser window.
 */
public class AjaxAreaTag extends AjaxAnchorsTag {

    private static final long serialVersionUID = -7940387487602588115L;

    /**
     * Flag if we should rewrite &lt;a&gt; tags to use ajaxlinks.
     */
    private boolean ajaxAnchors;

    /**
     * @return Returns true if we answering to AJAX request: request has proper "X-Requested-With"
     *         and "X-Request-Target" headers.
     */
    @Override
    public final boolean isAjaxRequest() {
        return super.isAjaxRequest() && isRequestTarget(getId());
    }

    /**
     * @return Returns the ajaxAnchors.
     */
    public final boolean isAjaxAnchors() {
        return ajaxAnchors;
    }

    /**
     * @param ajaxAnchors
     *            The ajaxAnchors to set.
     */
    public final void setAjaxAnchors(final boolean ajaxAnchors) {
        this.ajaxAnchors = ajaxAnchors;
    }

    /**
     * Clear page content before start of tag if we are processing AJAX request.
     *
     * @throws JspException
     *             when HTTP response cannot be reset (has already had its status code and headers
     *             written)
     */
    @Override
    public void initParameters() throws JspException {
        if (isAjaxRequest()) {
            try {
                pageContext.getOut().clearBuffer();
            } catch (IOException e) {
                throw new JspException(e);
            }
        }
    }

    /**
     * Write body. Skip the rest of the page if we are processing AJAX request.
     *
     * @return SKIP_PAGE for AJAX request, EVAL_PAGE for usual request
     * @throws JspException
     *             on errors
     */
    @Override
    public int doEndTag() throws JspException {
        final HTMLDivElement div = new HTMLDivElement(getId());
        div.append(processBody());
        if (getStyleClass() != null) {
            div.setClassName(getStyleClass());
        }
        if (isAjaxRequest()) {
            out(div.getBody());
            return SKIP_PAGE;
        } else {
            out(div);
            return EVAL_PAGE;
        }
    }

    /**
     * @see javax.servlet.jsp.tagext.Tag#release()
     */
    @Override
    public void releaseTag() {
        ajaxAnchors = false;
    }

    /**
     * Process content. Rewrite anchors to AJAX links if needed.
     *
     * @param content
     *            XHTML source as string
     * @return either content with rewritten anchors or unchanged content depending on flag
     * @throws SAXException
     *             if any parse errors occur
     * @throws TransformerException
     *             if it is not possible to transform document to string
     * @throws XPathExpressionException
     *             if XPath expression cannot be evaluated (wrong XPath expression)
     */
    @Override
    protected String processContent(final String content) throws XPathExpressionException,
            TransformerException, SAXException {
        return isAjaxAnchors() ? rewriteAnchors(content, getId(), getSourceClass()) : content;
    }

}
