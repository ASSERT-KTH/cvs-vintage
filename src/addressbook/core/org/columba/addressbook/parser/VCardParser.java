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
package org.columba.addressbook.parser;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Iterator;

import net.wimpi.pim.Pim;
import net.wimpi.pim.contact.basicimpl.CommunicationsImpl;
import net.wimpi.pim.contact.basicimpl.ContactImpl;
import net.wimpi.pim.contact.basicimpl.EmailAddressImpl;
import net.wimpi.pim.contact.basicimpl.OrganizationImpl;
import net.wimpi.pim.contact.basicimpl.OrganizationalIdentityImpl;
import net.wimpi.pim.contact.basicimpl.PersonalIdentityImpl;
import net.wimpi.pim.contact.io.ContactMarshaller;
import net.wimpi.pim.contact.io.ContactUnmarshaller;
import net.wimpi.pim.contact.model.Communications;
import net.wimpi.pim.contact.model.EmailAddress;
import net.wimpi.pim.contact.model.OrganizationalIdentity;
import net.wimpi.pim.contact.model.PersonalIdentity;
import net.wimpi.pim.factory.ContactIOFactory;

import org.columba.addressbook.model.ContactModel;
import org.columba.addressbook.model.EmailModel;
import org.columba.addressbook.model.IContactModel;
import org.columba.addressbook.model.IEmailModel;

/**
 * Contact data parser for a vCard-standard compliant text/plain file.
 * <p>
 * It makes use of the jpim library. Its not really a wrapper. It only
 * creates a mapping between the jpim data model and our data model.
 * 
 * @author fdietz
 */
public class VCardParser {

	/**
	 * Write vcard contact to outpustream.
	 * 
	 * @param c
	 *            contact data
	 * @param out
	 *            outputstream
	 */
	public static void write(IContactModel c, OutputStream out) {
		ContactIOFactory ciof = Pim.getContactIOFactory();
		ContactMarshaller marshaller = ciof.createContactMarshaller();
		marshaller.setEncoding("UTF-8");

		// create jpim contact instance
		net.wimpi.pim.contact.model.Contact exportContact = new ContactImpl();

		PersonalIdentity identity = new PersonalIdentityImpl();
		exportContact.setPersonalIdentity(identity);

		// set sort-string/displayname

		identity.setSortString(c.getSortString());

		// set first name
		identity.setFirstname(c.getGivenName());
		// set formatted name
		identity.setFormattedName(c.getFormattedName());
		// set last name

		identity.setLastname(c.getFamilyName());

		// add all additional names (middle names)
		String[] s = ParserUtil.getArrayOfString(c.getAdditionalNames(), ",");
		for (int i = 0; i < s.length; i++) {
			identity.addAdditionalName(s[i]);
		}

		// add all nicknames
		s = ParserUtil.getArrayOfString(c.getNickName(), ",");
		for (int i = 0; i < s.length; i++) {
			identity.addNickname(s[i]);
		}

		// add all prefixes
		s = ParserUtil.getArrayOfString(c.getNamePrefix(), ",");
		for (int i = 0; i < s.length; i++) {
			identity.addPrefix(s[i]);
		}

		// add all suffixes
		s = ParserUtil.getArrayOfString(c.getNameSuffix(), ",");
		for (int i = 0; i < s.length; i++) {
			identity.addSuffix(s[i]);
		}

		// set website/homepage
		exportContact.setURL(c.getHomePage());

		Communications communications = new CommunicationsImpl();
		exportContact.setCommunications(communications);

		// add email addresses

		Iterator it = c.getEmailIterator();
		while (it.hasNext()) {
			IEmailModel model = (IEmailModel) it.next();
			EmailAddress adr = new EmailAddressImpl();
			adr.setType(EmailAddress.TYPE_INTERNET);
			adr.setAddress(model.getAddress());
			communications.addEmailAddress(adr);
		}

		OrganizationalIdentity organizationalIdentity = new OrganizationalIdentityImpl();
		exportContact.setOrganizationalIdentity(organizationalIdentity);
		organizationalIdentity.setOrganization(new OrganizationImpl());

		// set name of organization
		organizationalIdentity.getOrganization().setName(c.getOrganisation());

		// save contact to outputstream
		marshaller.marshallContact(out, exportContact);
	}

	/**
	 * Parse vCard contact data from inputstream.
	 * 
	 * @param in
	 *            inputstream to vCard data
	 * @return contact
	 */
	public static IContactModel read(InputStream in) {
		ContactIOFactory ciof = Pim.getContactIOFactory();
		ContactUnmarshaller unmarshaller = ciof.createContactUnmarshaller();
		unmarshaller.setEncoding("UTF-8");

		net.wimpi.pim.contact.model.Contact importContact = unmarshaller
				.unmarshallContact(in);

		ContactModel c = new ContactModel();

		OrganizationalIdentity organisationalIdentity = importContact
				.getOrganizationalIdentity();

		// name of organisation
		c.setOrganisation(organisationalIdentity.getOrganization().getName());

		/*
		 * not supported in ui anyway!
		 * 
		 * c.set(VCARD.ROLE, organisationalIdentity.getRole());
		 * c.set(VCARD.TITLE, organisationalIdentity.getTitle());
		 * 
		 */

		if (importContact.hasPersonalIdentity()) {
			PersonalIdentity identity = importContact.getPersonalIdentity();
			
			// sort-string
			c.setSortString(identity.getSortString());

			// list of nick names
			if (identity.getNicknameCount() > 0)
				c.setNickName(ParserUtil.getStringOfArray(identity
						.listNicknames(), ","));

			// list of prefixes
			if (identity.listPrefixes().length > 0)
				c.setNamePrefix(ParserUtil.getStringOfArray(identity
						.listPrefixes(), ","));

			c.setFamilyName(identity.getLastname());
			c.setGivenName(identity.getFirstname());

			// list of additional names (middle names)
			if (identity.listAdditionalNames().length > 0)
				c.setAdditionalNames(ParserUtil.getStringOfArray(identity
						.listAdditionalNames(), ","));

			// list of suffices
			if (identity.listSuffixes().length > 0)
				c.setNameSuffix(ParserUtil.getStringOfArray(identity
						.listSuffixes(), ","));

			// formatted name
			c.setFormattedName(identity.getFormattedName());
		}

		// url to website/homepage
		c.setHomePage(importContact.getURL());

		// email addresses
		if (importContact.hasCommunications()) {
			Communications communications = importContact.getCommunications();

			Iterator it = communications.getEmailAddresses();
			while (it.hasNext()) {
				EmailAddress adr = (EmailAddress) it.next();
				String type = adr.getType();
				c.addEmail(new EmailModel(adr.getAddress(),
						EmailModel.TYPE_WORK));
			}
		}

		return c;
	}

}