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

package org.columba.addressbook.folder;

import java.io.File;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.columba.addressbook.parser.DefaultCardLoader;
import org.w3c.dom.Document;

/**
 * 
 * VCardXmlDataStorage stores VCARD directory information
 * in an xml-file.
 * 
 * It uses one xml-file for every VCARD. This is very similar to
 * the MH-mailfolder approach.
 * 
 * 
 */
public class VCardXmlDataStorage implements DataStorage
{
	Folder folder;

	public VCardXmlDataStorage(Folder folder)
	{

		this.folder = folder;
	}

	/*
	public void saveContactCard(ContactCard card, Object uid)
	{
		card.setUid(uid);
		Document document = card.getDocument();

		try
		{

			File file =
				new File(folder.directoryFile.toString() + "/" + ((Integer) uid) + ".xml");

			// Use a Transformer for output
			TransformerFactory tFactory = TransformerFactory.newInstance();
			Transformer transformer = tFactory.newTransformer();
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			transformer.setOutputProperty(OutputKeys.METHOD, "xml");

			DOMSource source = new DOMSource(document);
			StreamResult result = new StreamResult(file);
			transformer.transform(source, result);

		}
		catch (Exception ex)
		{
			System.out.println(ex.getMessage());

		}
	}
	*/
	

	/*
	public void removeContactCard(Object uid)
	{
		File file =
			new File(folder.directoryFile.toString() + "/" + ((Integer) uid) + ".xml");
		file.delete();
	}
	*/
	
	public DefaultCard loadDefaultCard( Object uid )
	{
		File file =
			new File(folder.directoryFile.toString() + "/" + (uid.toString()) + ".xml");
			
		DefaultCardLoader parser = new DefaultCardLoader(file);
				parser.load();

		DefaultCard card = null;
				if ( parser.isContact() == true )
				{
					card = parser.createContactCard();

					
				}
				else
				{
					card = parser.createGroupListCard();
					
					
				}
				
		return card;
	}


	/*
	public GroupListCard loadGroupListCard(Object uid)
	{
		File file =
			new File(folder.directoryFile.toString() + "/" + ((Integer) uid) + ".xml");

		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		Document document = null;
		try
		{
			DocumentBuilder builder = factory.newDocumentBuilder();

			builder.setErrorHandler(new org.xml.sax.ErrorHandler()
			{

				public void fatalError(SAXParseException exception) throws SAXException
				{
				}

				public void error(SAXParseException e) throws SAXParseException
				{
					throw e;
				}

				public void warning(SAXParseException err) throws SAXParseException
				{
					System.out.println(
						"** Warning" + ", line " + err.getLineNumber() + ", uri " + err.getSystemId());
					System.out.println("   " + err.getMessage());
				}
			});

			document = builder.parse(file);

		}

		catch (SAXParseException spe)
		{
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

		}
		catch (SAXException sxe)
		{
			Exception x = sxe;
			if (sxe.getException() != null)
				x = sxe.getException();
			x.printStackTrace();

		}
		catch (ParserConfigurationException pce)
		{
			pce.printStackTrace();
		}
		catch (IOException ioe)
		{
			ioe.printStackTrace();
		}

		//ContactCardParser parser = new ContactCardParser(file);
		//parser.load();

		//ContactCard card = parser.createContactCard();
		GroupListCard card = new GroupListCard( document, null);

		return card;
	}
	*/
	
	
	
	public void saveDefaultCard(DefaultCard card, Object uid)
	{
		
		
		card.setUid(uid);
		Document document = card.getDocument();

		try
		{

			File file =
				new File(folder.directoryFile.toString() + "/" + ((Integer) uid) + ".xml");

			// Use a Transformer for output
			TransformerFactory tFactory = TransformerFactory.newInstance();
			Transformer transformer = tFactory.newTransformer();
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			transformer.setOutputProperty(OutputKeys.METHOD, "xml");

			DOMSource source = new DOMSource(document);
			StreamResult result = new StreamResult(file);
			transformer.transform(source, result);

		}
		catch (Exception ex)
		{
			System.out.println(ex.getMessage());

		}
	}
	
	public void modifyCard( DefaultCard card, Object uid )
	{
		saveDefaultCard( card, uid );
		/*
		if ( card instanceof ContactCard )
		{
			saveContactCard( (ContactCard) card, uid );
		}
		else
		{
			saveGroupListCard( (GroupListCard) card, uid );
		}
		*/
		
	}
	
	public void removeCard( Object uid )
	{
		File file =
			new File(folder.directoryFile.toString() + "/" + ((Integer) uid) + ".xml");
		file.delete();
	}
	
	/*
	public void removeGroupListCard(Object uid)
	{
		File file =
			new File(folder.directoryFile.toString() + "/" + ((Integer) uid) + ".xml");
		file.delete();
	}
	*/


	/*
	public ContactCard loadContactCard(Object uid)
	{
		File file =
			new File(folder.directoryFile.toString() + "/" + ((Integer) uid) + ".xml");

		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		Document document = null;
		try
		{
			DocumentBuilder builder = factory.newDocumentBuilder();

			builder.setErrorHandler(new org.xml.sax.ErrorHandler()
			{

				public void fatalError(SAXParseException exception) throws SAXException
				{
				}

				public void error(SAXParseException e) throws SAXParseException
				{
					throw e;
				}

				public void warning(SAXParseException err) throws SAXParseException
				{
					System.out.println(
						"** Warning" + ", line " + err.getLineNumber() + ", uri " + err.getSystemId());
					System.out.println("   " + err.getMessage());
				}
			});

			document = builder.parse(file);

		}

		catch (SAXParseException spe)
		{
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

		}
		catch (SAXException sxe)
		{
			Exception x = sxe;
			if (sxe.getException() != null)
				x = sxe.getException();
			x.printStackTrace();

		}
		catch (ParserConfigurationException pce)
		{
			pce.printStackTrace();
		}
		catch (IOException ioe)
		{
			ioe.printStackTrace();
		}

		//ContactCardParser parser = new ContactCardParser(file);
		//parser.load();

		//ContactCard card = parser.createContactCard();
		ContactCard card = new ContactCard( document, null);

		return card;
	}
	*/
}