/*
 * $Header: /tmp/cvs-vintage/tomcat/src/j2ee/org/apache/tomcat/util/XMLParser.java,v 1.3 2000/05/31 21:15:34 costin Exp $
 * $Revision: 1.3 $
 * $Date: 2000/05/31 21:15:34 $
 *
 * ====================================================================
 *
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 1999 The Apache Software Foundation.  All rights 
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer. 
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution, if
 *    any, must include the following acknowlegement:  
 *       "This product includes software developed by the 
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowlegement may appear in the software itself,
 *    if and wherever such third-party acknowlegements normally appear.
 *
 * 4. The names "The Jakarta Project", "Tomcat", and "Apache Software
 *    Foundation" must not be used to endorse or promote products derived
 *    from this software without prior written permission. For written 
 *    permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache"
 *    nor may "Apache" appear in their names without prior written
 *    permission of the Apache Group.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 *
 * [Additional notices, if required by prior licensing conditions]
 *
 */ 


package org.apache.tomcat.util;

import com.sun.xml.tree.XmlDocument;
import com.sun.xml.tree.ElementNode;
import com.sun.xml.tree.XmlDocumentBuilder;
import com.sun.xml.parser.Resolver;
import com.sun.xml.parser.ValidatingParser;
import com.sun.xml.parser.Parser;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.w3c.dom.Element;
import org.w3c.dom.Text;
import org.w3c.dom.CharacterData;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.w3c.dom.NamedNodeMap;
import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.Enumeration;
import java.util.Vector;
import java.io.IOException;
import java.lang.NullPointerException;

/**
 *
 * @author James Todd [gonzo@eng.sun.com]
 */

public class XMLParser {
    public static final String WEB_XML_PublicId =
	"-//Sun Microsystems, Inc.//DTD Web Application 2.2//EN";
    public static final String WEB_XML_Resource =
	"/org/apache/tomcat/deployment/web.dtd";

    private XmlDocument doc = null;
    private static final boolean DefaultCheckType = false;
    private static final String DefaultContentType =
        "text/html;charset=utf-8";
    private static final String DefaultScheme = "file";
    private static final boolean DefaultXMLValidate = true;
    private static final int PCDataType = 3;

    public XMLParser() {
    }

    public void clear() {
        doc = null;
    }

    public XMLTree process(InputStream is)
    throws IOException, SAXParseException, SAXException {
        return process(is, DefaultCheckType, DefaultContentType,
	    DefaultScheme);
    }

    public XMLTree process(InputStream is, boolean checkType)
    throws IOException, SAXParseException, SAXException {
        return process(is, checkType, DefaultContentType, DefaultScheme);
    }

    public XMLTree process(InputStream is, boolean checkType,
        String contentType)
    throws IOException, SAXParseException, SAXException {
        return process(is, checkType, contentType, DefaultScheme);
    }

    public XMLTree process(InputStream is, boolean checkType,
        String contentType, String scheme)
    throws IOException, SAXParseException, SAXException {
        return parse(Resolver.createInputSource(contentType, is,
            checkType, scheme), checkType);
    }

    public XMLTree process(URL uri)
    throws IOException, SAXParseException, SAXException {
        return process(uri, DefaultCheckType);
    }

    public XMLTree process(URL uri, boolean checkType)
    throws IOException, SAXParseException, SAXException {
        return parse(new InputSource(uri.toString()), checkType);
    }

    public XmlDocument getXmlDocument() {
        return doc;
    }

    public void write()
    throws IOException {
        write(null);
    }

    public void write(OutputStream os)
    throws IOException {
        if (os == null) {
            os = System.out;
        }

	doc.getDocumentElement().normalize();
        doc.write(os);
    }

    private XMLTree parse(InputSource is)
    throws IOException, SAXParseException, SAXException {
        return parse(is, DefaultXMLValidate);
    }

    private XMLTree parse(InputSource is, boolean validate)
    throws IOException, SAXParseException, SAXException {
        Parser parser = (validate) ?
	    new ValidatingParser(true) : new Parser();
	Resolver resolver = new Resolver();
	XmlDocumentBuilder builder = new XmlDocumentBuilder();
	// Server.xml is not validated and has no DTD ( and will change for a while)
	// 	URL serverURL = this.getClass().getResource(
	//             Constants.DTD.Server.Resource);
	URL webApplicationURL = this.getClass().getResource(
            WEB_XML_Resource);

// 	resolver.registerCatalogEntry(Constants.DTD.Server.PublicId,
//             serverURL.toString());
	resolver.registerCatalogEntry(
	    WEB_XML_PublicId,
	    webApplicationURL.toString());

	try {
	    // parser.setFastStandalone(true);
	    parser.setEntityResolver(resolver);
	    builder.setDisableNamespaces(true);
	    builder.setParser(parser);
	    parser.parse(is);

	    doc = builder.getDocument();
	} catch (Exception e) {
	    String systemId = "";
	    int lineNumber = -1;
	    String message = "";
	    String publicId = "";
	    int columnNumber = -1;

	    if (e instanceof SAXParseException) {
	        systemId = ((SAXParseException)e).getSystemId();
		lineNumber = ((SAXParseException)e).getLineNumber();
		message = ((SAXParseException)e).getMessage();
		publicId = ((SAXParseException)e).getPublicId();
		columnNumber = ((SAXParseException)e).getColumnNumber();
	    }

	    String msg = "SAXParseException: " + systemId +
	        " : " + lineNumber + "\n  msg : " + message;

	    throw new SAXParseException(msg, publicId, systemId,
	        lineNumber, columnNumber);
	}

	XMLTree xmlElement = new XMLTree();
        Element element = doc.getDocumentElement();

	xmlElement.setName(element.getTagName());
	addAttributes(xmlElement, element);
	addElements(xmlElement, element);

        return xmlElement;
    }

    private void addAttributes(XMLTree xmlElement, Node node) {
        NamedNodeMap attributes = node.getAttributes();

        if (attributes != null) {
	    for (int i = 0; i < attributes.getLength(); i++) {
	        Node attribute = attributes.item(i);

	        xmlElement.addAttribute(attribute.getNodeName(),
                    attribute.getNodeValue());
            }
	}
    }

    private void addElements(XMLTree xmlElement, Node node) {
        NodeList childNodes = node.getChildNodes();

	for (int i = 0; i < childNodes.getLength(); i++) {
	    XMLTree childXMLTree = new XMLTree();
	    Node childNode = childNodes.item(i);

	    childXMLTree.setName(childNode.getNodeName());
	    addAttributes(childXMLTree, childNode);

            if (childNode.getNodeType() != PCDataType) {
	        addElements(childXMLTree, childNode);
	        xmlElement.addElement(childXMLTree);
            } else {
                xmlElement.setValue(childNode.getNodeValue());
            }
	}
    }
}
