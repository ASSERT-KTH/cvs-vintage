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


/**
 * @author fdietz
 *
 */
public class GroupItem extends HeaderItem {

	private String description;
	
	private int folderUid;
	
	/**
	 * 
	 */
	public GroupItem() {
		super();
		
		setContact(false);
	}

	/**
	 * @param group
	 */
	public GroupItem(Group group) {
		super();
		
		setDisplayName(group.getName());
		setDescription(group.getDescription());
		setFolderUid(group.getFolderUid());
		setContact(false);
	}
	
	/**
	 * @return Returns the description.
	 */
	public String getDescription() {
		return description;
	}
	/**
	 * @param description The description to set.
	 */
	public void setDescription(String description) {
		this.description = description;
	}
	
	/**
	 * @see java.lang.Object#clone()
	 */
	public Object clone() {
		GroupItem item = new GroupItem();
		item.setDisplayName(getDisplayName());
		item.setHeader(getHeader());
		item.setDescription(getDescription());
		item.setFolderUid(getFolderUid());
		
		
		return item;
	}

	/**
	 * @return Returns the folderUid.
	 */
	public int getFolderUid() {
		return folderUid;
	}
	/**
	 * @param folderUid The folderUid to set.
	 */
	public void setFolderUid(int folderUid) {
		this.folderUid = folderUid;
	}

}
