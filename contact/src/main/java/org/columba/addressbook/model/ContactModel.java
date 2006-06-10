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
package org.columba.addressbook.model;

import java.util.Date;
import java.util.Iterator;
import java.util.Vector;

import javax.swing.ImageIcon;

import org.columba.addressbook.facade.IContactItem;
import org.columba.addressbook.parser.ParserUtil;
import org.columba.core.base.UUIDGenerator;
import org.columba.core.logging.Logging;
import org.columba.ristretto.message.Address;
import org.columba.ristretto.parser.ParserException;

/**
 * Contact model POJO.
 * 
 * @author fdietz
 */
public class ContactModel implements IContactModel {

	private String id;

	private String organisation;

	private String department;

	private String office;

	private String title;

	private String profession;

	private String manager;
	
	private String sortString;

	private String familyName;

	private String givenName;

	private String additionalNames;

	private String nickName;

	private String namePrefix;

	private String nameSuffix;

	private String fullName;

	private String formattedName;

	private String homePage;

	private String weblog;

	private String calendar;

	private String freeBusy;

	private Date birthday;

	private ImageIcon photo;

	private String category;

	private Vector emailAddressVector = new Vector();

	private String preferredPhone;

	private Vector phoneVector = new Vector();

	private Vector instantMessagingVector = new Vector();

	private Vector addressVector = new Vector();

	private String note;
	
	public ContactModel(IContactItem contactItem) {
		if (contactItem == null || contactItem.getEmailAddress() == null  || contactItem.getEmailAddress().length() == 0)
			throw new IllegalArgumentException(
					"address == null or empty String");

		Address adr;

		String fn = contactItem.getName() != null ? contactItem.getName() : contactItem.getEmailAddress();
		setFormattedName(fn);
		
		// backwards compatibility
		setSortString(fn);
		addEmail(new EmailModel(contactItem.getEmailAddress(), EmailModel.TYPE_WORK));

		String[] result = ParserUtil.tryBreakName(fn);
		setGivenName(contactItem.getFirstName());
		setFamilyName(contactItem.getLastName());
		
	}

	public ContactModel() {
		this.id = new UUIDGenerator().newUUID();
	}

	public ContactModel(String id) {
		if (id == null)
			throw new IllegalArgumentException("id == null");
		
		this.id = id;
	}

	public String getId() {
		return id;
	}

	/**
	 * @see org.columba.addressbook.model.IContactModel#getAdditionalNames()
	 */
	public String getAdditionalNames() {
		return additionalNames;
	}

	/**
	 * @see org.columba.addressbook.model.IContactModel#getDepartment()
	 */
	public String getDepartment() {
		return department;
	}

	/**
	 * @see org.columba.addressbook.model.IContactModel#getPreferredEmail()
	 */
	public String getPreferredEmail() {
		Iterator it = getEmailIterator();

		// get first item
		IEmailModel model = (IEmailModel) it.next();

		// backwards compatiblity
		// -> its not possible anymore to create a contact model without email
		// address
		if (model == null)
			return null;

		return model.getAddress();
	}

	/**
	 * @see org.columba.addressbook.model.IContactModel#getFamilyName()
	 */
	public String getFamilyName() {
		return familyName;
	}

	/**
	 * @see org.columba.addressbook.model.IContactModel#getGivenName()
	 */
	public String getGivenName() {
		return givenName;
	}

	/**
	 * @see org.columba.addressbook.model.IContactModel#getNickName()
	 */
	public String getNickName() {
		return nickName;
	}

	/**
	 * @see org.columba.addressbook.model.IContactModel#getOrganisation()
	 */
	public String getOrganisation() {
		return organisation;
	}

	/**
	 * @see org.columba.addressbook.model.IContactModel#getProfession()
	 */
	public String getProfession() {
		return profession;
	}

	/**
	 * @see org.columba.addressbook.model.IContactModel#getNamePrefix()
	 */
	public String getNamePrefix() {
		return namePrefix;
	}

	/**
	 * @see org.columba.addressbook.model.IContactModel#getNameSuffix()
	 */
	public String getNameSuffix() {
		return nameSuffix;
	}

	/**
	 * @param familyName
	 *            The familyName to set.
	 */
	public void setFamilyName(String familyName) {
		this.familyName = familyName;
	}

	/**
	 * @param additionalNames
	 *            The additionalNames to set.
	 */
	public void setAdditionalNames(String additionalNames) {
		this.additionalNames = additionalNames;
	}

	/**
	 * @param department
	 *            The department to set.
	 */
	public void setDepartment(String department) {
		this.department = department;
	}

	/**
	 * @param given
	 *            The given to set.
	 */
	public void setGivenName(String given) {
		this.givenName = given;
	}

	/**
	 * @param nickname
	 *            The nickname to set.
	 */
	public void setNickName(String nickname) {
		this.nickName = nickname;
	}

	/**
	 * @param organisation
	 *            The organisation to set.
	 */
	public void setOrganisation(String organisation) {
		this.organisation = organisation;
	}

	/**
	 * @param position
	 *            The position to set.
	 */
	public void setProfession(String position) {
		this.profession = position;
	}

	/**
	 * @param prefix
	 *            The prefix to set.
	 */
	public void setNamePrefix(String prefix) {
		this.namePrefix = prefix;
	}

	/**
	 * @param suffix
	 *            The suffix to set.
	 */
	public void setNameSuffix(String suffix) {
		this.nameSuffix = suffix;
	}

	public String getHomePage() {
		return homePage;
	}

	public String getWeblog() {
		return weblog;
	}

	public String getCalendar() {
		return calendar;
	}

	public String getFreeBusy() {
		return freeBusy;
	}

	/**
	 * @param calendar
	 *            The calendar to set.
	 */
	public void setCalendar(String calendar) {
		this.calendar = calendar;
	}

	/**
	 * @param freeBusy
	 *            The freeBusy to set.
	 */
	public void setFreeBusy(String freeBusy) {
		this.freeBusy = freeBusy;
	}

	/**
	 * @param homePage
	 *            The homePage to set.
	 */
	public void setHomePage(String homePage) {
		this.homePage = homePage;
	}

	/**
	 * @param weblog
	 *            The weblog to set.
	 */
	public void setWeblog(String weblog) {
		this.weblog = weblog;
	}

	/**
	 * @deprecated use getFormattedName() instead
	 * 
	 * @see org.columba.addressbook.model.IContactModel#getFullName()
	 */
	public String getFullName() {
		return fullName;
	}

	/**
	 * @deprecated use setFormattedName() instead
	 * 
	 * @param fullName
	 *            The fullName to set.
	 */
	public void setFullName(String fullName) {
		this.fullName = fullName;
	}

	public String getFormattedName() {
		return formattedName;
	}

	/**
	 * @param formattedName
	 *            The formattedName to set.
	 */
	public void setFormattedName(String formattedName) {
		this.formattedName = formattedName;
	}

	public Iterator getEmailIterator() {
		return emailAddressVector.iterator();
	}

	public void addEmail(IEmailModel emailAddress) {
		if (emailAddress == null)
			throw new IllegalArgumentException("emailModel == null");

		emailAddressVector.add(emailAddress);
	}

	// public String getAgent() {
	// return agent;
	// }

	public Date getBirthday() {
		return birthday;
	}

	public String getSortString() {
		return sortString;
	}

	public ImageIcon getPhoto() {
		return photo;
	}

	/**
	 * @param birthday
	 *            The birthday to set.
	 */
	public void setBirthday(Date birthday) {
		this.birthday = birthday;
	}

	/**
	 * @param photo
	 *            The photo to set.
	 */
	public void setPhoto(ImageIcon photo) {
		this.photo = photo;
	}

	/**
	 * @param sortString
	 *            The sortString to set.
	 */
	public void setSortString(String sortString) {
		this.sortString = sortString;
	}

	public Iterator getPhoneIterator() {
		return phoneVector.iterator();
	}

	public void addPhone(PhoneModel phoneModel) {
		if (phoneModel == null)
			throw new IllegalArgumentException("phoneModel == null");

		phoneVector.add(phoneModel);
	}

	public Iterator getInstantMessagingIterator() {
		return instantMessagingVector.iterator();
	}

	public void addInstantMessaging(InstantMessagingModel instantMessagingModel) {
		if (instantMessagingModel == null)
			throw new IllegalArgumentException("instantMessaging == null");

		instantMessagingVector.add(instantMessagingModel);
	}

	public String getPreferredPhone() {
		return preferredPhone;
	}

	public void addAddress(AddressModel model) {
		if (model == null)
			throw new IllegalArgumentException("model == null");

		addressVector.add(model);
	}

	public Iterator getAddressIterator() {
		return addressVector.iterator();
	}

	public String getTitle() {
		return title;
	}

	/**
	 * @param title
	 *            The title to set.
	 */
	public void setTitle(String title) {
		this.title = title;
	}

	public String getNote() {
		return note;
	}

	/**
	 * @param note
	 *            The note to set.
	 */
	public void setNote(String note) {
		this.note = note;
	}

	public String getCategory() {
		return category;
	}

	/**
	 * @param category
	 *            The category to set.
	 */
	public void setCategory(String category) {
		this.category = category;
	}

	public String getOffice() {
		return office;
	}

	/**
	 * @param office The office to set.
	 */
	public void setOffice(String office) {
		this.office = office;
	}

	public String getPreferredInstantMessaging() {
		Iterator it = getInstantMessagingIterator();
		if ( it.hasNext() ) {
			InstantMessagingModel m = (InstantMessagingModel) it.next();
			return m.getUserId();
		}
		
		return null;
	}

	public String getManager() {
		return manager;
	}

	public void setManager(String manager) {
		this.manager = manager;
	}
}