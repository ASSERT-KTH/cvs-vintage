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

package org.columba.addressbook.folder;

import org.columba.addressbook.gui.table.util.*;
import org.columba.core.config.HeaderTableItem;
import org.columba.addressbook.config.FolderItem;
import org.columba.addressbook.main.AddressbookInterface;
import org.columba.core.util.SwingWorker;
import org.columba.addressbook.parser.*;

import org.columba.core.config.DefaultXmlConfig;

import java.io.*;

import java.util.*;

import org.apache.crimson.tree.*;

import javax.xml.transform.*;
import javax.xml.transform.dom.*;
import javax.xml.transform.stream.*;
import javax.xml.parsers.*;
import org.xml.sax.*;
import org.w3c.dom.*;

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