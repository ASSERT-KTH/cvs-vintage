/*
 * Copyright (C) 2002-2003, Simon Nieuviarts
 */
package org.objectweb.carol.cmi.compiler;

import java.io.StringWriter;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

public class XMLTree extends DefaultHandler {
    /** Default parser name. */
    private static final String DEFAULT_PARSER_NAME =
        "org.apache.xerces.parsers.SAXParser";
    private XMLElement root = new XMLElement(null, null, null);
    private XMLElement cur = root;
    private boolean chars = false;
    private StringWriter str = new StringWriter();

    //
    // DocumentHandler methods
    //

    private void flushChars() {
        if (!chars)
            return;
        cur.add(str.toString());
        str.getBuffer().setLength(0);
        chars = false;
    }

    public void startDocument() {
        flushChars();
    }

    public void endDocument() throws SAXException {
        flushChars();
        if (cur != root)
            throw new SAXException("Unexpected end of document");
    }

    public void startElement(
        String uri,
        String name,
        String qName,
        Attributes attrs)
        throws SAXException {
        flushChars();
        XMLElement e = new XMLElement(cur, name, attrs);
        cur.add(e);
        cur = e;
    }

    public void endElement(String uri, String name, String qName)
        throws SAXException {
        flushChars();
        if (!name.equals(cur.name))
            throw new SAXException("endElement name not corresponding with the name of startElement");
        cur = cur.parent;
        if (cur == null)
            throw new SAXException("endElement with no corresponding startElement");
    }

    public void characters(char ch[], int start, int length)
        throws SAXException {
        str.write(ch, start, length);
        chars = true;
    }

    public void ignorableWhitespace(char ch[], int start, int length) {
        flushChars();
    }

    //
    // ErrorHandler methods
    //

    public void warning(SAXParseException ex) {
        System.err.println(
            "[Warning] " + getLocationString(ex) + ": " + ex.getMessage());
    }

    public void error(SAXParseException ex) {
        System.err.println(
            "[Error] " + getLocationString(ex) + ": " + ex.getMessage());
    }

    public void fatalError(SAXParseException ex) throws SAXException {
        System.err.println(
            "[Fatal Error] " + getLocationString(ex) + ": " + ex.getMessage());
        System.exit(1);
    }

    private String getLocationString(SAXParseException ex) {
        StringBuffer str = new StringBuffer();
        String systemId = ex.getSystemId();
        if (systemId != null) {
            int index = systemId.lastIndexOf('/');
            if (index != -1)
                systemId = systemId.substring(index + 1);
            str.append(systemId);
        }
        str.append(':');
        str.append(ex.getLineNumber());
        str.append(':');
        str.append(ex.getColumnNumber());

        return str.toString();
    }

    public static XMLElement read(String uri) throws Exception {
        XMLTree t = new XMLTree();
        XMLReader parser =
            (XMLReader) Class.forName(DEFAULT_PARSER_NAME).newInstance();
        parser.setContentHandler(t);
        parser.setErrorHandler(t);
        parser.setFeature("http://xml.org/sax/features/validation", true);
        parser.parse(uri);
        return t.root;
    }
}
