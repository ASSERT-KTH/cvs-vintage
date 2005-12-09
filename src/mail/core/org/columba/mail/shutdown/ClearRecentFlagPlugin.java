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
package org.columba.mail.shutdown;

import java.io.ByteArrayInputStream;
import java.io.UnsupportedEncodingException;
import java.util.Enumeration;
import java.util.logging.Logger;

import org.columba.core.filter.Filter;
import org.columba.core.xml.XmlIO;
import org.columba.mail.folder.AbstractMessageFolder;
import org.columba.mail.folder.IMailFolder;
import org.columba.mail.folder.command.MarkMessageCommand;
import org.columba.mail.gui.tree.FolderTreeModel;

public class ClearRecentFlagPlugin implements Runnable {

	private static final String FILTER_XML = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><filter enabled=\"true\"><rules condition=\"matchall\"><criteria criteria=\"is\" type=\"Flags\" pattern=\"Recent\"></criteria></rules></filter>";

	private static Filter RECENT_FILTER; 
	
	static {
		try {
			XmlIO io = new XmlIO();		
			io.load(new ByteArrayInputStream(FILTER_XML.getBytes("UTF-8")));
			
			RECENT_FILTER = new Filter(io.getRoot().getElement(0));
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	/** JDK 1.4+ logging framework logger, used for logging. */
	private static final Logger LOG = Logger
			.getLogger("org.columba.mail.shutdown");

	public void run() {
		IMailFolder rootFolder = (IMailFolder) FolderTreeModel.getInstance()
				.getRoot();
		clearRecent(rootFolder);
	}

	protected void clearRecent(IMailFolder parentFolder) {
		IMailFolder child;

		for (Enumeration e = parentFolder.children(); e.hasMoreElements();) {
			child = (IMailFolder) e.nextElement();
			
			if( child instanceof AbstractMessageFolder ) {
				AbstractMessageFolder folder = (AbstractMessageFolder) child;
				if( folder.getMessageFolderInfo().getRecent() >  0) {
				
					try {
						Object uids[] = folder.searchMessages(RECENT_FILTER);		
						folder.markMessage(uids, MarkMessageCommand.MARK_AS_NOTRECENT);
						
						folder.save();
					} catch (Exception e1) {
					}
				}
			}
			clearRecent(child);
		}
	}

}
