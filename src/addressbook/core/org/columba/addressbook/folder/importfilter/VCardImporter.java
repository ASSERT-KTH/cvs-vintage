// The contents of this file are subject to the Mozilla Public License Version
// 1.1
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
//The Initial Developers of the Original Code are Frederik Dietz and Timo
// Stich.
//Portions created by Frederik Dietz and Timo Stich are Copyright (C) 2003.
//
//All Rights Reserved.
package org.columba.addressbook.folder.importfilter;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;

import net.wimpi.pim.Pim;
import net.wimpi.pim.contact.io.ContactUnmarshaller;
import net.wimpi.pim.factory.ContactIOFactory;

import org.columba.addressbook.folder.AbstractFolder;
import org.columba.addressbook.model.Contact;
import org.columba.addressbook.parser.VCardParser;

/**
 * Contact import filter for vCard contact standard.
 * 
 * @author fdietz
 */
public class VCardImporter extends DefaultAddressbookImporter {

	/**
	 *  
	 */
	public VCardImporter() {
		super();

	}

	/**
	 * @param sourceFile
	 * @param destinationFolder
	 */
	public VCardImporter(File sourceFile, AbstractFolder destinationFolder) {
		super(sourceFile, destinationFolder);		
	}

	/**
	 * @see org.columba.addressbook.folder.importfilter.DefaultAddressbookImporter#importAddressbook(java.io.File)
	 */
	public void importAddressbook(File file) throws Exception {

		BufferedInputStream in = new BufferedInputStream(new FileInputStream(
				file));
		
		Contact c = VCardParser.read(in);

		saveContact(c);
	}

	/**
	 * @see org.columba.addressbook.folder.importfilter.DefaultAddressbookImporter#getDescription()
	 */
	public String getDescription() {
		return "vCard import filter";
	}

	
}