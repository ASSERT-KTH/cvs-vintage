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
package org.columba.core.config;

import java.io.File;
import java.net.MalformedURLException;

import org.columba.core.xml.XmlIO;

public class DefaultXmlConfig extends XmlIO{
	/*
	private Document document;
	*/
	
	//private File file;

	public DefaultXmlConfig(File file) {
		/*
		this.file = file;
		*/
		
		try{	
			setURL( file.toURL() );
		}catch(MalformedURLException mue){}
		
		//printNode( getRoot(), "");
	}
	
	/*
	public File getFile()
	{
		return file;
	}
	*/
	/*
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
	
	*/
}
