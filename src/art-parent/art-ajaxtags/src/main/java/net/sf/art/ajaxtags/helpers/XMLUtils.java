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

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;

import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * Some helper functions for XML.
 */
public final class XMLUtils {

    /** Value for transformer output properties. */
    private static final String TRANSFORMER_YES = "yes";

    /* THREAD HELPER IMPLEMENTATIONS */
    /** TransformerFactory. */
    private static final ThreadLocal<TransformerFactory> TRANSFORMER_FACTORY = new ThreadLocal<TransformerFactory>() {
        @Override
        protected TransformerFactory initialValue() {
            return TransformerFactory.newInstance();
        }
    };

    /** DocumentBuilderFactory. */
    private static final ThreadLocal<DocumentBuilderFactory> DOC_FACTORY = new ThreadLocal<DocumentBuilderFactory>() {
        @Override
        protected DocumentBuilderFactory initialValue() {
            final DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            dbf.setValidating(false);
            dbf.setIgnoringElementContentWhitespace(true);
            return dbf;
        }
    };

    /** XPathFactory. */
    private static final ThreadLocal<XPathFactory> XPATH_FACTORY = new ThreadLocal<XPathFactory>() {
        @Override
        protected XPathFactory initialValue() {
            return XPathFactory.newInstance();
        }
    };

    /* END THREAD HELPER IMPLEMENTATIONS */

    /**
     * Never instance this class.
     */
    private XMLUtils() {
    }

    /**
     * Evaluate XPath expression and return list of nodes.
     *
     * @param expression
     *            XPath expression
     * @param node
     *            DOM node
     * @return list of DOM nodes
     * @throws XPathExpressionException
     *             if expression cannot be evaluated
     */
    public static NodeList evaluateXPathExpression(final String expression, final Node node)
            throws XPathExpressionException {
        return (NodeList) evaluateXPathExpression(expression, node, XPathConstants.NODESET);
    }

    /**
     * Evaluate XPath expression.
     *
     * @param expression
     *            XPath expression
     * @param node
     *            DOM node
     * @param returnValue
     *            the desired return type
     * @return result of evaluating an XPath expression as an Object of returnType
     * @throws XPathExpressionException
     *             if expression cannot be evaluated
     */
    public static Object evaluateXPathExpression(final String expression, final Node node,
            final QName returnValue) throws XPathExpressionException {
        return getNewXPath().evaluate(expression, node,
                returnValue == null ? XPathConstants.NODE : returnValue);
    }

    /**
     * Create and return a new {@link XPath} object from {@link ThreadLocal}.
     *
     * @return a new {@link XPath} object.
     */
    public static XPath getNewXPath() {
        return XPATH_FACTORY.get().newXPath();
    }

    /**
     * @return DocumentBuilder
     * @throws ParserConfigurationException
     *             if a DocumentBuilder cannot be created which satisfies the configuration
     *             requested
     */
    private static DocumentBuilder getNewDocumentBuilder() throws ParserConfigurationException {
        return DOC_FACTORY.get().newDocumentBuilder();
    }

    /**
     * Parse string with XML content to {@link org.w3c.dom.Document}.
     *
     * @param xml
     *            string with XML content
     * @return Document
     * @throws SAXException
     *             if any parse errors occur
     */
    public static Document getXMLDocument(final String xml) throws SAXException {
        try {
            return getNewDocumentBuilder().parse(new InputSource(new StringReader(xml)));
        } catch (IOException e) {
            throw new SAXException(e);
        } catch (ParserConfigurationException e) {
            throw new SAXException(e);
        }
    }

    /**
     * Create a new {@link org.w3c.dom.Document}.
     *
     * @return an empty document
     * @throws ParserConfigurationException
     *             if a DocumentBuilder cannot be created
     */
    public static Document createDocument() throws ParserConfigurationException {
        return getNewDocumentBuilder().newDocument();
    }

    /**
     * Parse string as XML document and return string with reformatted document.
     *
     * @param xml
     *            string with XML content
     * @return reformatted content
     * @throws TransformerException
     *             if it is not possible to transform document to string
     * @throws SAXException
     *             if any parse errors occur
     */
    public static String format(final String xml) throws TransformerException, SAXException {
        return toString(getXMLDocument(xml));
    }

    /**
     * Transform document to string representation.
     *
     * @param document
     *            XHTML document
     * @return string representation of document
     * @throws TransformerException
     *             if it is not possible to create a Transformer instance or to transform document
     */
    public static String toString(final Document document) throws TransformerException {
        final StringWriter stringWriter = new StringWriter();
        final StreamResult streamResult = new StreamResult(stringWriter);
        final Transformer transformer = TRANSFORMER_FACTORY.get().newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, TRANSFORMER_YES);
        // set indent for XML
        transformer.setOutputProperty("{http://xml.apache.org/xalan}indent-amount", "2");
        // not all JavaSE have the same implementation
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
        // transformer.setOutputProperty(OutputKeys.METHOD, "html");
        // html method transforms <br/> into <br>, which cannot be re-parsed
        // transformer.setOutputProperty(OutputKeys.METHOD, "xhtml");
        // xhtml method does not work for my xalan transformer
        transformer.setOutputProperty(OutputKeys.METHOD, "xml");
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, TRANSFORMER_YES);
        transformer.transform(new DOMSource(document.getDocumentElement()), streamResult);
        return stringWriter.toString();
    }

    protected static boolean isCharInRange(final int c, final int begin, final int end) {
        return c >= begin && c <= end;
    }

    /**
     * http://www.w3.org/TR/html4/types.html#type-name
     *
     * ID and NAME tokens must begin with a letter ([A-Za-z]).
     *
     * @param c
     *            char
     * @return true if HTML Name can start with that char
     */
    protected static boolean isValidHtmlNameStartChar(final int c) {
        return isCharInRange(c, 'A', 'Z') || isCharInRange(c, 'a', 'z');
    }

    /**
     * http://www.w3.org/TR/html4/types.html#type-name
     *
     * ID and NAME tokens must begin with a letter ([A-Za-z]) and may be followed by any number of
     * letters, digits ([0-9]), hyphens ("-"), underscores ("_"), colons (":"), and periods (".").
     *
     * @param c
     *            char
     * @return true if HTML Name can continue with that char
     */
    protected static boolean isValidHtmlNameChar(final int c) {
        return isValidXmlNameStartChar(c) || isCharInRange(c, '0', '9') || c == '-' || c == '_'
                || c == ':' || c == '.';
    }

    /**
     * http://www.w3.org/TR/REC-xml/#NT-Name
     *
     * NameStartChar ::= ":" | [A-Z] | "_" | [a-z] | [#xC0-#xD6] | [#xD8-#xF6] | [#xF8-#x2FF] |
     * [#x370-#x37D] | [#x37F-#x1FFF] | [#x200C-#x200D] | [#x2070-#x218F] | [#x2C00-#x2FEF] |
     * [#x3001-#xD7FF] | [#xF900-#xFDCF] | [#xFDF0-#xFFFD] | [#x10000-#xEFFFF]
     *
     * @param c
     *            char
     * @return true if XML Name can start with that char
     */
    protected static boolean isValidXmlNameStartChar(final int c) {
        return c == ':' || isCharInRange(c, 'A', 'Z') || c == '_' || isCharInRange(c, 'a', 'z')
                || isCharInRange(c, 0xC0, 0xD6) || isCharInRange(c, 0xD8, 0xF6)
                || isCharInRange(c, 0xF8, 0x2FF) || isCharInRange(c, 0x370, 0x37D)
                || isCharInRange(c, 0x37F, 0x1FFF) || isCharInRange(c, 0x200C, 0x200D)
                || isCharInRange(c, 0x2070, 0x218F) || isCharInRange(c, 0x2C00, 0x2FEF)
                || isCharInRange(c, 0x3001, 0xD7FF) || isCharInRange(c, 0xF900, 0xFDCF)
                || isCharInRange(c, 0xFDF0, 0xFFFD) || isCharInRange(c, 0x10000, 0xEFFFF);
    }

    /**
     * http://www.w3.org/TR/REC-xml/#NT-Name
     *
     * NameChar ::= NameStartChar | "-" | "." | [0-9] | #xB7 | [#x0300-#x036F] | [#x203F-#x2040]
     *
     * @param c
     *            char
     * @return true if XML Name can continue with that char
     */
    protected static boolean isValidXmlNameChar(final int c) {
        return isValidXmlNameStartChar(c) || c == '-' || c == '.' || isCharInRange(c, '0', '9')
                || c == 0xB7 || isCharInRange(c, 0x0300, 0x036F)
                || isCharInRange(c, 0x203F, 0x2040);
    }

    public static boolean isValidHtmlName(final String name) {
        if (name == null) {
            return true;
        }
        if (!isValidHtmlNameStartChar(name.codePointAt(0))) {
            return false;
        }
        for (int i = 1; i < name.length(); i++) {
            if (!isValidHtmlNameChar(name.codePointAt(i))) {
                return false;
            }
        }
        return true;
    }

    /**
     * Name ::= NameStartChar (NameChar)*
     */
    public static boolean isValidXmlName(final String name) {
        if (name == null) {
            return true;
        }
        if (!isValidXmlNameStartChar(name.codePointAt(0))) {
            return false;
        }
        for (int i = 1; i < name.length(); i++) {
            if (!isValidXmlNameChar(name.codePointAt(i))) {
                return false;
            }
        }
        return true;
    }

}
