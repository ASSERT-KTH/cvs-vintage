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
package org.columba.mail.folder;

import java.util.Vector;

/**
 * @author freddy
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class CapabilityList extends Vector {

	public static final int RENAME_FOLDER_ACTION = 0x0001;
	public static final int REMOVE_FOLDER_ACTION = 0x0002;
	public static final int FOLDER_SHOW_HEADERLIST_ACTION = 0x0003;

	public static final int MESSAGE_REPLY_ACTION = 0x1001;
	public static final int MESSAGE_REPLY_TO_ALL_ACTION = 0x1002;
	public static final int MESSAGE_REPLY_TO_LIST_ACTION = 0x1003;
	public static final int MESSAGE_REPLY_AS_ATTACHMENT_ACTION = 0x1004;

	public static final int MESSAGE_FORWARD_ACTION = 0x1005;
	public static final int MESSAGE_FORWARD_INLINE_ACTION = 0x1006;

	public static final int MESSAGE_BOUNCE_ACTION = 0x1007;

	public static final int MESSAGE_OPEN_ACTION = 0x1008;
	public static final int MESSAGE_SAVE_AS_ACTION = 0x1009;
	public static final int MESSAGE_PRINT_ACTION = 0x1010;

	public static final int MESSAGE_COPY_ACTION = 0x1011;
	public static final int MESSAGE_MOVE_ACTION = 0x1012;
	public static final int MESSAGE_DELETE_ACTION = 0x1013;

	public static final int MESSAGE_ADD_SENDER_ACTION = 0x1014;
	public static final int MESSAGE_ADD_ALL_SENDER__ACTION = 0x1015;

	public static final int MESSAGE_VIEW_SOURCE_ACTION = 0x1016;

	public static final int MESSAGE_MARK_AS_READ_ACTION = 0x1017;
	public static final int MESSAGE_MARK_AS_FLAGGED_ACTION = 0x1018;
	public static final int MESSAGE_MARK_AS_EXPUNGED_ACTION = 0x1019;

	/**
	 * Constructor for CapabilityList.
	 */
	public CapabilityList() {
		super();
	}

	public void add(int capabilityCode) {
		super.add(new Integer(capabilityCode));
	}

	public boolean contains(int capabilityCode) {
		Integer i = new Integer(capabilityCode);

		return super.contains(i);
	}

	public static CapabilityList getDefaultFolderCapabilities() {
		CapabilityList c = new CapabilityList();
		c.add(MESSAGE_REPLY_ACTION);
		c.add(MESSAGE_REPLY_TO_ALL_ACTION);
		c.add(MESSAGE_REPLY_TO_LIST_ACTION);
		c.add(MESSAGE_REPLY_AS_ATTACHMENT_ACTION);
		c.add(MESSAGE_FORWARD_ACTION);
		c.add(MESSAGE_FORWARD_INLINE_ACTION);
		c.add(MESSAGE_BOUNCE_ACTION);
		c.add(MESSAGE_OPEN_ACTION);
		c.add(MESSAGE_SAVE_AS_ACTION);
		c.add(MESSAGE_PRINT_ACTION);
		c.add(MESSAGE_COPY_ACTION);
		c.add(MESSAGE_MOVE_ACTION);
		c.add(MESSAGE_DELETE_ACTION);
		c.add(MESSAGE_ADD_SENDER_ACTION);
		c.add(MESSAGE_ADD_ALL_SENDER__ACTION);
		c.add(MESSAGE_VIEW_SOURCE_ACTION);
		c.add(MESSAGE_MARK_AS_READ_ACTION);
		c.add(MESSAGE_MARK_AS_FLAGGED_ACTION);
		c.add(MESSAGE_MARK_AS_EXPUNGED_ACTION);
		
		return c;
	}

}
