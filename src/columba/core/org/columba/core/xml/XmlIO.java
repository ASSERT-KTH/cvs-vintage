//The contents of this file are subject to the Mozilla Public License Version 1.1
//(the "License"); you may not use this file except in compliance with the 
//License. You may obtain a copy of the License at http://www.mozilla.org/MPL/
//
//Software distributed under the License is distributed on an "AS IS" basis,
//WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License 
//for the specific language governing rights and
//limitations under the License.
//
//The Original Code is "The Columba Project"
//
//The Initial Developers of the Original Code are Frederik Dietz and Timo Stich.
//Portions created by Frederik Dietz and Timo Stich are Copyright (C) 2003. 
//
//All Rights Reserved.

package org.columba.core.xml;

import java.io.CharArrayWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.URL;
import java.util.Enumeration;
import java.util.Vector;

import javax.swing.JOptionPane;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.columba.core.logging.ColumbaLogger;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

public class XmlIO extends DefaultHandler {
	// List of sub-elements
	Vector elements;
	// Top level element (Used to hold everything else)
	XmlElement rootElement;
	// The current element you are working on
	XmlElement currentElement;

	// For writing out the data
	// Indent for each level
	int writeIndent = 2;
	// Maximum data to put on a "one liner"
	int maxOneLineData = 20;

	// The SAX 2 parser...
	private XMLReader xr;

	// Buffer for collecting data from
	// the "characters" SAX event.
	private CharArrayWriter contents = new CharArrayWriter();

	protected URL url = null;

	/*
	// Default constructor
	public XmlIO() {
	}
	*/

	/*
	// setup and load constructor
	public XmlIO(String FilePath) {
		currentElement = null;


	}
	*/

	public XmlIO(URL url) {
		super();

		this.url = url;
	}

	// setup and load constructor
	public XmlIO() {
		currentElement = null;

	}

	public void setURL(URL url) {
		this.url = url;
	}

	public boolean load() {
		//this.file = F;

		return load(url);
	}

	// Load a file. This is what starts things off.
	public boolean load(URL inputURL) {
		elements = new Vector();
		rootElement = new XmlElement("__COLUMBA_XML_TREE_TOP__");
		currentElement = rootElement;

		try {
			// Create the XML reader...
			//      xr = XMLReaderFactory.createXMLReader();
			SAXParserFactory factory = SAXParserFactory.newInstance();
			// Set the ContentHandler...
			//      xr.setContentHandler( this );

			SAXParser saxParser = factory.newSAXParser();

			saxParser.parse(inputURL.openStream(), this);

		} catch (javax.xml.parsers.ParserConfigurationException ex) {
			ColumbaLogger.log.error(
				"XML config error while attempting to read XML file \n'"
                +inputURL+"'");
			ColumbaLogger.log.error(ex.toString());
			ex.printStackTrace();
			return (false);
		} catch (SAXException ex) {
			// Error
			ColumbaLogger.log.error(
				"XML parse error while attempting to read XML file \n'"
                +inputURL+"'");
			ColumbaLogger.log.error(ex.toString());
			ex.printStackTrace();
			return (false);
		} catch (IOException ex) {
			ColumbaLogger.log.error(
				"I/O error while attempting to read XML file \n'"
                +inputURL+"'");
			ColumbaLogger.log.error(ex.toString());
			ex.printStackTrace();
			return (false);
		}

		//XmlElement.printNode( getRoot(), "");

		return (true);
	}

	// Implement the content hander methods that
	// will delegate SAX events to the tag tracker network.

	public void startElement(
		String namespaceURI,
		String localName,
		String qName,
		Attributes attrs)
		throws SAXException {

		// Resetting contents buffer.
		// Assuming that tags either tag content or children, not both.
		// This is usually the case with XML that is representing
		// data strucutures in a programming language independant way.
		// This assumption is not typically valid where XML is being
		// used in the classical text mark up style where tagging
		// is used to style content and several styles may overlap
		// at once.
		try {
			contents.reset();
			String Name = localName; // element name
			if (Name.equals(""))
				Name = qName; // namespaceAware = false

			XmlElement P = currentElement;

			currentElement = currentElement.addSubElement(Name);
			currentElement.setParent(P);

			if (attrs != null) {
				for (int i = 0; i < attrs.getLength(); i++) {
					String aName = attrs.getLocalName(i); // Attr name
					if (aName.equals(""))
						aName = attrs.getQName(i);

					currentElement.addAttribute(aName, attrs.getValue(i));
				}
			}
		} catch (java.lang.NullPointerException ex) {
			ColumbaLogger.log.error("Null!!!");
			ColumbaLogger.log.error(ex.toString());
			ex.printStackTrace();
		}
	}

	public void endElement(String namespaceURI, String localName, String qName)
		throws SAXException {

		currentElement.setData(contents.toString().trim());
		contents.reset();

		currentElement = currentElement.getParent();
	}

	public void characters(char[] ch, int start, int length)
		throws SAXException {
		// accumulate the contents into a buffer.
		contents.write(ch, start, length);
	}

	public XmlElement getRoot() {
		return (rootElement);
	}

	public void errorDialog(String Msg) {
		JOptionPane.showMessageDialog(null, "Error: " + Msg);
	}
	public void warningDialog(String Msg) {
		JOptionPane.showMessageDialog(null, "Warning: " + Msg);
	}
	public void infoDialog(String Msg) {
		JOptionPane.showMessageDialog(null, "Info: " + Msg);
	}

	public void save() throws Exception {
		write(new FileOutputStream(url.getPath()));
	}

	//
	// Writer interface
	//
	public void write(OutputStream out) throws IOException {
		PrintWriter PW = new PrintWriter(out);
		PW.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
		if (rootElement.subElements.size() > 0) {
			for (int i = 0; i < rootElement.subElements.size(); i++) {
				_writeSubNode(
					PW,
					(XmlElement) rootElement.subElements.get(i),
					0);
			}
		}
		PW.flush();
	}

	private String _escapeText(String txt){
		StringBuffer buffer = new StringBuffer(txt);
		_stringReplaceAll(buffer, '<', "&lt;");
		_stringReplaceAll(buffer, '>', "&tg;");
		_stringReplaceAll(buffer, '&', "&amp;");
		return buffer.toString();
	}
	
	private StringBuffer _stringReplaceAll(StringBuffer orig, char token, String replacement){
		for(int i=0;i<orig.length();i++){
			if(orig.charAt(i) == token)
				orig = orig.replace(i, ++i, replacement);
		}
		return orig;
	}

	private void _writeSubNode(PrintWriter out, XmlElement element, int indent)
		throws IOException {
		_writeSpace(out, indent);
		out.print("<" + element.getName());
		for (Enumeration e = element.getAttributeNames();
			e.hasMoreElements();
			) {
			String K = (String) e.nextElement();
			out.print(" " + K + "=\"" + _escapeText(element.getAttribute(K))
                      + "\"");
		}

		out.print(">");

		String data = element.getData();

		if (data != null && !data.equals("")) {
			if (data.length() > maxOneLineData) {
				out.println("");
				_writeSpace(out, indent + writeIndent);
			}
			out.print(_escapeText(data));
		}
		Vector subElements = element.getElements();

		if (subElements.size() > 0) {
			out.println("");
			for (int i = 0; i < subElements.size(); i++) {
				_writeSubNode(
					out,
					(XmlElement) subElements.get(i),
					indent + writeIndent);
			}
			_writeSpace(out, indent);
		}
		if (data.length() > maxOneLineData) {
			out.println("");
			_writeSpace(out, indent);
		}
		out.println("</" + _escapeText(element.getName()) + ">");
	}

	private void _writeSpace(PrintWriter out, int numSpaces)
		throws IOException {
		for (int i = 0; i < numSpaces; i++)
			out.print(" ");
	}

} // End class XmlIO
