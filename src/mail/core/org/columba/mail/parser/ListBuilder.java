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
package org.columba.mail.parser;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import org.columba.addressbook.facade.IContactFacade;
import org.columba.addressbook.facade.IContactItem;
import org.columba.addressbook.facade.IFolder;
import org.columba.addressbook.facade.IFolderFacade;
import org.columba.addressbook.facade.IGroupItem;
import org.columba.addressbook.facade.IHeaderItem;
import org.columba.api.exception.ServiceNotFoundException;
import org.columba.mail.connector.ServiceConnector;

/**
 * Provides methods for creating new lists from other list formats.
 * 
 * @author fdietz
 */
public class ListBuilder {

	private static IContactItem retrieveContactItem(String name) {

		try {
			IContactFacade facade = ServiceConnector.getContactFacade();
			IFolderFacade folderFacade = ServiceConnector.getFolderFacade();
			List<IFolder> list = folderFacade.getAllFolders();
			Iterator<IFolder> it = list.iterator();
			while (it.hasNext()) {
				IFolder folder = it.next();
				String id = facade.findByName(folder.getId(), name);
				if (id != null) {
					IContactItem contactItem = facade.getContactItem(folder
							.getId(), id);
					return contactItem;
				}
			}
		} catch (ServiceNotFoundException e) {
			e.printStackTrace();
		}

		return null;
	}

	private static IGroupItem retrieveGroupItem(String name) {

		try {
			IContactFacade facade = ServiceConnector.getContactFacade();
			IFolderFacade folderFacade = ServiceConnector.getFolderFacade();
			List<IFolder> list = folderFacade.getAllFolders();
			Iterator<IFolder> it = list.iterator();
			while (it.hasNext()) {
				IFolder folder = it.next();
				List<IGroupItem> groupList = facade
						.getAllGroups(folder.getId());
				Iterator<IGroupItem> groupIt = groupList.iterator();
				while (groupIt.hasNext()) {
					IGroupItem groupItem = groupIt.next();
					if (name.equals(groupItem.getName()))
						return groupItem;
				}
			}
		} catch (ServiceNotFoundException e) {
			e.printStackTrace();
		}

		return null;
	}

	/**
	 * Flatten mixed list containing contacts and groups to a new list
	 * containing only contacts.
	 * 
	 * @param list
	 *            mixed list
	 * @return list containing only contacts. Never <code>null</code>
	 */
	public static List<String> createFlatList(List<String> list) {
		if ( list == null ) throw new IllegalArgumentException("list == null");
		
		List<String> result = new Vector<String>();

		Iterator<String> it = list.iterator();
		while (it.hasNext()) {
			String str = it.next();
			
			// remove leading or trailing whitespaces
			str = str.trim();
			
			IContactItem contactItem = retrieveContactItem(str);
			if (contactItem != null) {
				// found contact item in contact component
				result.add(contactItem.getEmailAddress());
			} else {
				// check if its a group item

				IGroupItem groupItem = retrieveGroupItem(str);
				if (groupItem != null) {

					List<IContactItem> contactItemList = groupItem
							.getContacts();

					Iterator<IContactItem> it2 = contactItemList.iterator();
					while (it2.hasNext()) {
						IContactItem i = it2.next();
						String address = i.getEmailAddress();

						if (address == null) {
							continue;
						}

						result.add(address);
					}
				} else
				{
					result.add(str);
				}
			}

		}

		return result;
	}

	/**
	 * Create list containing only strings from a HeaderItemList containing
	 * HeaderItem objects.
	 * 
	 * @param list
	 *            HeaderItemList containing HeaderItem objects
	 * @return list containing only strings
	 */
	public static List<String> createStringListFromItemList(
			List<IHeaderItem> list) {
		List<String> result = new Vector<String>();

		for (Iterator it = list.iterator(); it.hasNext();) {
			IHeaderItem item = (IHeaderItem) it.next();
			result.add(item.getName());
		}

		return result;
	}

}