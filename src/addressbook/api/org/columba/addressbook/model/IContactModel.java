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

import javax.swing.ImageIcon;

/**
 * Contact model.
 * 
 * @author fdietz
 */
public interface IContactModel {

	public abstract String getId();
	
	public abstract Iterator getAddressIterator();
	
	public abstract Iterator getEmailIterator();
	public abstract String getPreferredEmail();
	
	
	public abstract Iterator getPhoneIterator();
	public abstract String getPreferredPhone();
	
	
	public abstract Iterator getInstantMessagingIterator();
	public abstract String getPreferredInstantMessaging();
	
	public abstract String getProfession();
	public abstract String getTitle();
	public abstract String getManager();
	public abstract String getOrganisation();
	public abstract String getDepartment();
	public abstract String getOffice();
	
	
	public abstract String getNickName();
	public abstract String getFamilyName();
	public abstract String getGivenName();
	public abstract String getAdditionalNames();
	public abstract String getNamePrefix();
	public abstract String getNameSuffix();
	
	public abstract String getFormattedName();
	
	public abstract Date getBirthday();
	
	public abstract String getSortString();
	
	public abstract ImageIcon getPhoto();
	
	public abstract String getHomePage();
	public abstract String getWeblog();
	public abstract String getCalendar();
	public abstract String getFreeBusy();
	
	public abstract String getCategory();
	
	public abstract String getNote();
}
