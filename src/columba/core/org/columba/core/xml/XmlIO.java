// this is -*- java -*- code
/////////////////////////////////////////////////////////////////////////////
// FILE:         XmlIO.java
// SUMMARY:
// USAGE:
//
// AUTHOR:       Tony Parent
// ORG:          AMCC
// ORIG-DATE:     8-Oct-02 at 12:24:05
// LAST-MOD:      6-Jan-03 at 12:00:21 by Tony Parent
// DESCRIPTION:
// DESCRIP-END.
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU Library General Public License for more details.
//
// You should have received a copy of the GNU General Public License along
// with this program; if not, write to the Free Software Foundation, Inc.,
// 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
//
//
// $Log: XmlIO.java,v $
// Revision 1.2  2003/01/06 19:18:26  tonyparent
// [intern] Escape '<', '>', and '&' when writing data and attributes.
//
// Revision 1.1  2002/12/29 17:44:37  fdietz
// [intern]source tree cleanup
//
/////////////////////////////////////////////////////////////////////////////

package org.columba.core.xml;

import java.io.CharArrayWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
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
	Vector Elements;
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

	private File file;

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

	public XmlIO(File f) {
		super();

		this.file = f;
	}

	// setup and load constructor
	public XmlIO() {
		currentElement = null;

	}

	public void setFile(File file) {
		this.file = file;
	}

	public boolean load() {
		//this.file = F;

		return load(file.getPath());
	}

	// Load a file. This is what starts things off.
	public boolean load(String inputFile) {
		Elements = new Vector();
		rootElement = new XmlElement("__CULUMBA_XML_TREE_TOP__");
		currentElement = rootElement;

		try {
			// Use an instance of ourselves as the SAX event handler
			DefaultHandler handler = this;
			// Create the XML reader...
			//      xr = XMLReaderFactory.createXMLReader();
			SAXParserFactory factory = SAXParserFactory.newInstance();
			// Set the ContentHandler...
			//      xr.setContentHandler( this );

			SAXParser saxParser = factory.newSAXParser();



			saxParser.parse(inputFile, this);

		} catch (javax.xml.parsers.ParserConfigurationException ex) {
			ColumbaLogger.log.error(
				"XML config error while attempting to read XML file \n'"
                +inputFile+"'");
			ColumbaLogger.log.error(ex.toString());
			ex.printStackTrace();
			return (false);
		} catch (org.xml.sax.SAXException ex) {
			// Error
			ColumbaLogger.log.error(
				"XML parse error while attempting to read XML file \n'"
                +inputFile+"'");
			ColumbaLogger.log.error(ex.toString());
			ex.printStackTrace();
			return (false);
		} catch (java.io.IOException ex) {
			ColumbaLogger.log.error(
				"File read error while attempting to read XML file \n'"
                +inputFile+"'");
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

		write(new FileOutputStream(file));
	}

	//
	// Writer interface
	//
	public void write(OutputStream out) throws IOException {
		PrintWriter PW = new PrintWriter(out);
		PW.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
		if (rootElement.SubElements.size() > 0) {
			for (int i = 0; i < rootElement.SubElements.size(); i++) {
				_writeSubNode(
					PW,
					(XmlElement) rootElement.SubElements.get(i),
					0);
			}
		}
		PW.flush();
	}

    private String _escapeText(String txt){
      txt = txt.replaceAll("<","&lt;");
      txt = txt.replaceAll(">","&gt;");
      txt = txt.replaceAll("&","&amp;");
      return(txt);
    }

	private void _writeSubNode(PrintWriter out, XmlElement Element, int indent)
		throws IOException {
		_writeSpace(out, indent);
		out.print("<" + Element.getName());
		for (Enumeration e = Element.getAttributeNames();
			e.hasMoreElements();
			) {
			String K = (String) e.nextElement();
			out.print(" " + K + "=\"" + _escapeText(Element.getAttribute(K))
                      + "\"");
		}

		out.print(">");

		String Data = Element.getData();

		if (Data != null && !Data.equals("")) {
			if (Data.length() > maxOneLineData) {
				out.println("");
				_writeSpace(out, indent + writeIndent);
			}
			out.print(_escapeText(Data));
		}
		Vector SubElements = Element.getElements();

		if (SubElements.size() > 0) {
			out.println("");
			for (int i = 0; i < SubElements.size(); i++) {
				_writeSubNode(
					out,
					(XmlElement) SubElements.get(i),
					indent + writeIndent);
			}
			_writeSpace(out, indent);
		}
		if (Data.length() > maxOneLineData) {
			out.println("");
			_writeSpace(out, indent);
		}
		out.println("</" + _escapeText(Element.getName()) + ">");
	}

	private void _writeSpace(PrintWriter out, int numSpaces)
		throws IOException {
		for (int i = 0; i < numSpaces; i++)
			out.print(" ");
	}

} // End class XmlIO
