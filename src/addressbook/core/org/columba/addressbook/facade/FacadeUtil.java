/**
 * Copyright 2005, 2006 ToolCafe, Inc. All rights reserved.
 */
package org.columba.addressbook.facade;

import org.columba.core.util.NameParser;
import org.columba.core.util.NameParser.Name;

/**
 * Utilities to assist with using the addressbook facade
 * @author Rick Horowitz
 *
 */
public class FacadeUtil {
	
	private static FacadeUtil _instance;
	public static FacadeUtil getInstance() {
		if (_instance == null)
			_instance = new FacadeUtil();
		return _instance;
	}
	
	private FacadeUtil() {
	}

	/**
	 * Initialize the IContactItem with the specified name and emailAddr
	 * @param contactItem The IContactItem to initialize
	 * @param displayName The displayName to 
	 * @param emailAddr
	 */
	public void initContactItem(IContactItem contactItem, String displayName, String emailAddr) {
		
		NameParser.Name name = NameParser.getInstance().parseDisplayName(displayName);
		contactItem.setAddress(emailAddr);
		contactItem.setFirstname(name.getFirstName());
		contactItem.setLastname(name.getLastName());
		contactItem.setName(name.toString());
	}
}
