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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.BodyContent;
import javax.servlet.jsp.tagext.BodyTagSupport;

import org.apache.commons.lang3.StringUtils;

import net.sf.art.ajaxtags.helpers.JavaScript;
import net.sf.art.ajaxtags.servlets.AjaxActionHelper.HTMLAjaxHeader;

import static org.apache.commons.lang3.StringUtils.trimToNull;

/**
 * This is a base class that will help to make a development of any tag really easy.
 */
public abstract class BaseAjaxBodyTag extends BodyTagSupport {

    /**
     * The header with the target of the AJAX call. Value of this header should match the target
     * attribute of the tag.
     */
    public static final String TARGET_HEADER = "X-Request-Target";
    /**
     * The header we are searching to detect the AJAX call.
     */
    public static final String HEADER_FLAG = "X-Requested-With";
    public static final String HEADER_FLAG_VALUE = "XMLHttpRequest";
    public static final String AJAX_VOID_URL = "javascript://nop";

    /**
     * Common prefix for all JavaScript class names.
     */
    public static final String JSCLASS_BASE = "AjaxJspTag.";

    private static final long serialVersionUID = 2128368408391947139L;

    /**
     * True if the body should be ignored.
     */
    private boolean skipBody;

    private String styleClass;

    /**
     * ID of the source element.
     */
    private String source;
    /**
     * ID of the target element.
     */
    private String target;
    private String baseUrl;
    private String parser;
    private String parameters;
    private String preFunction;
    private String postFunction;
    private String errorFunction;
    private String var;
    private String attachTo;
    private String sourceClass;
    private String eventType;

    protected HttpServletRequest getHttpServletRequest() {
        return (HttpServletRequest) pageContext.getRequest();
    }

    protected HttpServletResponse getHttpServletResponse() {
        return (HttpServletResponse) pageContext.getResponse();
    }

    protected boolean isHttpRequestHeader(final String headerName, final String headerValue) {
        return headerValue.equalsIgnoreCase(getHttpRequestHeader(headerName));
    }

    protected String getHttpRequestHeader(final String headerName) {
        return getHttpServletRequest().getHeader(headerName);
    }

    /**
     * Detect if the client does an AJAX call or not.
     *
     * @return true only if the client send the header with XMLHttpRequest
     */
    protected boolean isAjaxRequest() {
        return isHttpRequestHeader(HEADER_FLAG, HEADER_FLAG_VALUE);
    }

    protected boolean isRequestTarget(final String target) {
        return isHttpRequestHeader(TARGET_HEADER, target);
    }

    protected void out(final CharSequence csec) throws JspException {
        try {
            pageContext.getOut().append(csec);
        } catch (IOException e) {
            throw new JspException(e);
        }
    }

    /**
     * Ignore the body of the tag.
     */
    protected final void skipBody() {
        skipBody = true;
    }

    @Override
    public void setId(final String id) {
        if (id != null) {
            // TODO check for valid ID
        }
        super.setId(id);
    }

    @Override
    public final int doStartTag() throws JspException {
        initParameters(); // EVAL_BODY need to be flushed if it is nested!
        // we should set the no cache headers!

        // enable the ajaxheaders
        for (HTMLAjaxHeader header : HTMLAjaxHeader.values()) {
            header.enable(getHttpServletResponse());
        }

        return skipBody ? SKIP_BODY : EVAL_BODY_BUFFERED;
    }

    @Override
    public final void release() {
        setId(null);

        target = null; // NOPMD
        baseUrl = null; // NOPMD
        parser = null; // NOPMD

        preFunction = null; // NOPMD
        postFunction = null; // NOPMD
        errorFunction = null; // NOPMD
        parameters = null; // NOPMD

        var = null; // NOPMD
        attachTo = null; // NOPMD

        source = null; // NOPMD
        sourceClass = null; // NOPMD
        eventType = null; // NOPMD

        styleClass = null; // NOPMD

        releaseTag();
    }

    public final String getEventType() {
        return eventType;
    }

    public final void setEventType(final String eventType) {
        this.eventType = trimToNull(eventType);
    }

    public final String getSourceClass() {
        return sourceClass;
    }

    public final void setSourceClass(final String sourceClass) {
        this.sourceClass = trimToNull(sourceClass);
    }

    public final String getSource() {
        return source;
    }

    /**
     * @param source
     *            ID of the source element
     */
    public final void setSource(final String source) {
        this.source = trimToNull(source);
    }

    public final String getVar() {
        return var;
    }

    public final void setVar(final String var) {
        this.var = trimToNull(var);
    }

    public final void setAttachTo(final String attachTo) {
        this.attachTo = trimToNull(attachTo);
    }

    public final String getAttachTo() {
        return attachTo;
    }

    /**
     * @return Returns the styleClass.
     */
    public final String getStyleClass() {
        return styleClass;
    }

    /**
     * @param styleClass
     *            The styleClass to set.
     */
    public final void setStyleClass(final String styleClass) {
        this.styleClass = trimToNull(styleClass);
    }

    /**
     * Build JavaScript assignment string.
     *
     * @return String with left side of assignment to variable "var foo = " or field "object.foo = "
     */
    public final String getJSVariable() {
        if (var == null) { // short-circuit
            return StringUtils.EMPTY;
        }
        // compiler will use StringBuilder
        return attachTo == null ? "var " + var + " = " : attachTo + "." + var + " = ";
    }

    /**
     * Return JavaScript class for JavaScript class corresponding to this tag (e.g.
     * "AjaxJspTag.Submit" for AjaxSubmitTag Java tag).
     *
     * @return String with JavaScript class suffix
     */
    protected String getJsClass() {
        throw new UnsupportedOperationException(
                "You must implement getJsClass() in your tag class to use buildScript().");
    }

    /**
     * Options for JavaScript generation.
     *
     * @return default options
     */
    protected OptionsBuilder getOptions() {
        return getOptionsBuilder();
    }

    /**
     * Generate JavaScript for tag.
     *
     * @return JavaScript
     */
    public JavaScript buildScript() {
        return new JavaScript(getJSVariable() + "new " + getJsClass() + "({" + getOptions() + "});");
    }

    public final String getParameters() {
        return parameters;
    }

    public final void setParameters(final String parameters) {
        this.parameters = trimToNull(parameters);
    }

    public final String getErrorFunction() {
        return errorFunction;
    }

    public final void setErrorFunction(final String errorFunction) {
        this.errorFunction = trimToNull(errorFunction);
    }

    public final String getPostFunction() {
        return postFunction;
    }

    public final void setPostFunction(final String postFunction) {
        this.postFunction = trimToNull(postFunction);
    }

    public final String getPreFunction() {
        return preFunction;
    }

    public final void setPreFunction(final String preFunction) {
        this.preFunction = trimToNull(preFunction);
    }

    public final String getParser() {
        return parser;
    }

    public final void setParser(final String parser) {
        this.parser = trimToNull(parser);
    }

    public final String getBaseUrl() {
        return baseUrl;
    }

    public final void setBaseUrl(final String baseUrl) {
        this.baseUrl = trimToNull(baseUrl);
    }

    /**
     * @return Returns the target.
     */
    public final String getTarget() {
        return target;
    }

    /**
     * @param target
     *            The target to set.
     */
    public final void setTarget(final String target) {
        this.target = trimToNull(target);
    }

    protected void initParameters() throws JspException { // NOPMD
    }

    /**
     * Never call release() from releaseTag() -> ends in loop.
     */
    protected void releaseTag() { // NOPMD
    }

    /**
     * @return the OptionsBuilder with non-empty subset of default options (baseUrl, parser, target,
     *         source, sourceClass, eventType, parameters, onCreate, onComplete, onFailure)
     */
    protected OptionsBuilder getOptionsBuilder() {
        return getOptionsBuilder(false);
    }

    /**
     * @param empty
     *            true to return empty OptionsBuilder (without any options), false to return
     *            OptionsBuilder with non-empty subset of default options (baseUrl, parser, target,
     *            source, sourceClass, eventType, parameters, onCreate, onComplete, onFailure)
     * @return the OptionsBuilder
     */
    protected OptionsBuilder getOptionsBuilder(final boolean empty) {
        final OptionsBuilder builder = OptionsBuilder.getOptionsBuilder();
        if (empty) {
            return builder;
        }
        builder.add("baseUrl", getBaseUrl(), true);
        builder.add("parser", getParser(), false);

        builder.add("target", getTarget(), true);
        builder.add("source", getSource(), true);
        builder.add("sourceClass", getSourceClass(), true);

        builder.add("eventType", getEventType(), true);

        builder.add("parameters", getParameters(), true);

        builder.add("onCreate", preFunction, false);
        builder.add("onComplete", postFunction, false);
        builder.add("onFailure", errorFunction, false);

        return builder;
    }

    /**
     * Helper to define new AJAX updater for onclick attribute.
     *
     * @param target
     *            the target to request
     * @param href
     *            the URL
     * @param opt
     *            options for javascript library
     * @return the javascript code to do AJAX update
     */
    protected final String getOnclickAjax(final String target, final String href,
            final OptionsBuilder opt) {
        final OptionsBuilder options = OptionsBuilder.getOptionsBuilder(opt);
        // copy all options
        options.add("target", target, true);
        options.add("baseUrl", href, true);

        options.add("eventBase", "this", false);
        options.add("requestHeaders", "['" + TARGET_HEADER + "', '" + target + "']", false);

        // TODO with JavaScript class
        final StringBuilder onclick = new StringBuilder("new AjaxJspTag.OnClick({");
        onclick.append(options.toString());
        onclick.append("}); return false;");
        return onclick.toString();
    }

    /**
     * @return String representation of current body content or null
     */
    protected String getBody() {
        final BodyContent body = this.getBodyContent();
        return body == null ? null : body.getString();
    }
}
