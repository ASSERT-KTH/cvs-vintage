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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.StringTokenizer;

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

import org.columba.addressbook.model.Contact;
import org.columba.addressbook.model.VCARD;

/**
 * Contact data parser for a vCard-standard compliant text/plain file.
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
	public static void write(Contact c, OutputStream out) {
		ContactIOFactory ciof = Pim.getContactIOFactory();
		ContactMarshaller marshaller = ciof.createContactMarshaller();
		marshaller.setEncoding("UTF-8");

		// create jpim contact instance
		net.wimpi.pim.contact.model.Contact exportContact = new ContactImpl();

		PersonalIdentity identity = new PersonalIdentityImpl();
		exportContact.setPersonalIdentity(identity);

		// set sort-string/displayname
		if (c.get(VCARD.DISPLAYNAME) != null)
			identity.setSortString(c.get(VCARD.DISPLAYNAME));

		// set first name
		identity.setFirstname(c.get(VCARD.N_GIVEN));
		// set formatted name
		identity.setFormattedName(c.formatGet(VCARD.FN));
		// set last name
		identity.setLastname(c.get(VCARD.N_FAMILY));

		// add all additional names (middle names)
		String[] s = getType(c.get(VCARD.N_ADDITIONALNAMES));
		for (int i = 0; i < s.length; i++) {
			identity.addAdditionalName(s[i]);
		}

		// add all nicknames
		s = getType(c.get(VCARD.NICKNAME));
		for (int i = 0; i < s.length; i++) {
			identity.addNickname(s[i]);
		}

		// add all prefixes
		s = getType(c.get(VCARD.N_PREFIX));
		for (int i = 0; i < s.length; i++) {
			identity.addPrefix(s[i]);
		}

		// add all suffixes
		s = getType(c.get(VCARD.N_SUFFIX));
		for (int i = 0; i < s.length; i++) {
			identity.addSuffix(s[i]);
		}

		// set website/homepage
		exportContact.setURL(c.get(VCARD.URL));

		Communications communications = new CommunicationsImpl();
		exportContact.setCommunications(communications);

		// add email addresses
		EmailAddress adr = new EmailAddressImpl();
		adr.setType(EmailAddress.TYPE_INTERNET);
		adr.setAddress(c.get(VCARD.EMAIL, VCARD.EMAIL_TYPE_INTERNET));
		communications.addEmailAddress(adr);
		adr.setType(EmailAddress.TYPE_X400);
		adr.setAddress(c.get(VCARD.EMAIL, VCARD.EMAIL_TYPE_X400));
		communications.addEmailAddress(adr);
		
		adr.setAddress(c.get(VCARD.EMAIL, VCARD.EMAIL_TYPE_PREF));
		communications.setPreferredEmailAddress(adr);

		OrganizationalIdentity organizationalIdentity = new OrganizationalIdentityImpl();
		exportContact.setOrganizationalIdentity(organizationalIdentity);
		organizationalIdentity.setOrganization(new OrganizationImpl());

		// set name of organization
		organizationalIdentity.getOrganization().setName(c.get(VCARD.ORG));

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
	public static Contact read(InputStream in) {
		ContactIOFactory ciof = Pim.getContactIOFactory();
		ContactUnmarshaller unmarshaller = ciof.createContactUnmarshaller();
		unmarshaller.setEncoding("UTF-8");

		net.wimpi.pim.contact.model.Contact importContact = unmarshaller
				.unmarshallContact(in);

		Contact c = new Contact();

		OrganizationalIdentity organisationalIdentity = importContact
				.getOrganizationalIdentity();

		// name of organisation
		c.set(VCARD.ORG, organisationalIdentity.getOrganization().getName());

		/*
		 * not supported in ui anyway!
		 * 
		 * c.set(VCARD.ROLE, organisationalIdentity.getRole());
		 * c.set(VCARD.TITLE, organisationalIdentity.getTitle());
		 *  
		 */

		if (importContact.hasPersonalIdentity()) {
			PersonalIdentity identity = importContact.getPersonalIdentity();

			// displayname (Columba-specific additional attribute)
			c.set(VCARD.DISPLAYNAME, identity.getSortString());

			// sort-string
			c.set(VCARD.SORTSTRING, identity.getSortString());

			// list of nick names
			if (identity.getNicknameCount() > 0)
				c.set(VCARD.NICKNAME, getString(identity.listNicknames()));

			// list of prefixes
			if (identity.listPrefixes().length > 0)
				c.set(VCARD.N_PREFIX, getString(identity.listPrefixes()));

			c.set(VCARD.N_FAMILY, identity.getLastname());
			c.set(VCARD.N_GIVEN, identity.getFirstname());

			// list of additional names (middle names)
			if (identity.listAdditionalNames().length > 0)
				c.set(VCARD.N_ADDITIONALNAMES, getString(identity
						.listAdditionalNames()));

			// list of suffices
			if (identity.listSuffixes().length > 0)
				c.set(VCARD.N_SUFFIX, getString(identity.listSuffixes()));

			// formatted name
			c.formatSet(VCARD.FN, identity.getFormattedName());
		}

		// url to website/homepage
		c.set(VCARD.URL, importContact.getURL());

		// email addresses
		if (importContact.hasCommunications()) {
			Communications communications = importContact.getCommunications();

			Iterator it = communications.getEmailAddresses();
			while (it.hasNext()) {
				EmailAddress adr = (EmailAddress) it.next();
				String type = adr.getType();
				if (type.equals(EmailAddress.TYPE_INTERNET))
					c.set(VCARD.EMAIL, VCARD.EMAIL_TYPE_INTERNET, adr
							.getAddress());
				else if (type.equals(EmailAddress.TYPE_X400))
					c.set(VCARD.EMAIL, VCARD.EMAIL_TYPE_X400, adr.getAddress());
			}
		}

		/*
		 * 
		 * not supported in ui anyway
		 * 
		 * 
		 * if (importContact.getAddressCount() > 0) {
		 * 
		 * Iterator it = importContact.getAddresses(); Address address =
		 * (Address) it.next();
		 * 
		 * StringBuffer buf = new StringBuffer(); if ( address.isDomestic() )
		 * buf.append(VCARD.ADR_TYPE_DOM+","); if ( address.isHome() )
		 * buf.append(VCARD.ADR_TYPE_HOME+","); if ( address.isInternational() )
		 * buf.append(VCARD.ADR_TYPE_INTL+","); if ( address.isParcel() )
		 * buf.append(VCARD.ADR_TYPE_PARCEL+","); if ( address.isPostal() )
		 * buf.append(VCARD.ADR_TYPE_POSTAL+","); if ( address.isWork() )
		 * buf.append(VCARD.ADR_TYPE_WORK+","); // remove last "," character
		 * buf.substring(0, buf.length()-1); // country c.set(VCARD.ADR_COUNTRY,
		 * address.getCountry());
		 * 
		 * c.set(VCARD.ADR_POSTOFFICEBOX, address.getPostBox());
		 * c.set(VCARD.ADR_EXTENDEDADDRESS, address.getExtended());
		 * c.set(VCARD.ADR_STREETADDRESS, address.getStreet());
		 * c.set(VCARD.ADR_REGION, address.getRegion());
		 * c.set(VCARD.ADR_POSTALCODE, address.getPostalCode()); // address
		 * label c.set(VCARD.LABEL_TYPE_DOM, address.getLabel());
		 * c.set(VCARD.LABEL_TYPE_INTL, address.getLabel());
		 * c.set(VCARD.LABEL_TYPE_POSTAL, address.getLabel());
		 * c.set(VCARD.LABEL_TYPE_PARCEL, address.getLabel());
		 * c.set(VCARD.LABEL_TYPE_HOME, address.getLabel());
		 * c.set(VCARD.LABEL_TYPE_WORK, address.getLabel());
		 * c.set(VCARD.LABEL_TYPE_PREF, address.getLabel()); }
		 */

		return c;
	}

	/**
	 * Create array from comma-separated string.
	 * 
	 * @param s
	 *            comma-separated string
	 * @return string array
	 */
	static String[] getType(String s) {
		ArrayList list = new ArrayList();

		StringTokenizer tok = new StringTokenizer(s, ",");
		while (tok.hasMoreTokens()) {
			String t = tok.nextToken();
			list.add(t);
		}

		return (String[]) list.toArray(new String[] { "" });

	}

	/**
	 * Create comma-separated string from string array.
	 * 
	 * @param s
	 *            string array
	 * @return comma separated string
	 */
	static String getString(String[] s) {
		StringBuffer buf = new StringBuffer();
		for (int i = 0; i < s.length; i++) {
			buf.append(s[i]);
			if (i < s.length - 1)
				buf.append(",");
		}

		return buf.toString();
	}

}