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
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.

package org.columba.core.config;

import java.io.File;
import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.CDATASection;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

public class DefaultXmlConfig {
	private Document document;
	private File file;

	public DefaultXmlConfig(File file) {
		this.file = file;
	}
	
	public File getFile()
	{
		return file;
	}

	public void save() {
		try {

			// Use a Transformer for output
			TransformerFactory tFactory = TransformerFactory.newInstance();
			Transformer transformer = tFactory.newTransformer();
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			transformer.setOutputProperty(OutputKeys.METHOD, "xml");

			DOMSource source = new DOMSource(document);
			StreamResult result = new StreamResult(file);
			transformer.transform(source, result);

		} catch (Exception ex) {
			System.out.println(ex.getMessage());

		}
	}

	public void load() {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

		try {
			DocumentBuilder builder = factory.newDocumentBuilder();

			builder.setErrorHandler(new org.xml.sax.ErrorHandler() {

				public void fatalError(SAXParseException exception)
					throws SAXException {
				}

				public void error(SAXParseException e)
					throws SAXParseException {
					throw e;
				}

				public void warning(SAXParseException err)
					throws SAXParseException {
					System.out.println(
						"** Warning"
							+ ", line "
							+ err.getLineNumber()
							+ ", uri "
							+ err.getSystemId());
					System.out.println("   " + err.getMessage());
				}
			});

			document = builder.parse(file);

		} catch (SAXParseException spe) {
			System.out.println(
				"\n** Parsing error"
					+ ", line "
					+ spe.getLineNumber()
					+ ", uri "
					+ spe.getSystemId());
			System.out.println("   " + spe.getMessage());

			Exception x = spe;
			if (spe.getException() != null)
				x = spe.getException();
			x.printStackTrace();

		} catch (SAXException sxe) {
			Exception x = sxe;
			if (sxe.getException() != null)
				x = sxe.getException();
			x.printStackTrace();

		} catch (ParserConfigurationException pce) {
			pce.printStackTrace();
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
	}

	public Document getDocument() {
		return document;
	}

	public void addElement(Element parent, Element child) {
		parent.appendChild(child);
	}

	public void addCDATASection(Element parent, CDATASection child) {
		parent.appendChild(child);
	}

	/*
	  public void setNodeValue( AdapterNode node, String value )
	  {
	  node.appendChild( document.createTextNode( value ) );
	  }
	*/

	public Element createTextElementNode(String key, String value) {
		AdapterNode adpNode = new AdapterNode(document);

		Element newElement = (Element) document.createElement(key);
		newElement.appendChild(document.createTextNode(value));

		return newElement;
	}

	public CDATASection createCDATAElementNode(String key) {
		AdapterNode adpNode = new AdapterNode(document);

		CDATASection newElement =
			(CDATASection) document.createCDATASection(key);

		return newElement;
	}

	public Element createElementNode(String key) {
		AdapterNode adpNode = new AdapterNode(document);

		Element newElement = (Element) document.createElement(key);
		return newElement;
	}
	
	
}
