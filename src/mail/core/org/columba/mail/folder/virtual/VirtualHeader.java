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
package org.columba.mail.folder.virtual;

import java.util.Enumeration;

import org.columba.mail.folder.Folder;
import org.columba.mail.message.ColumbaHeader;
import org.columba.mail.message.HeaderInterface;

/**
 * @author freddy
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class VirtualHeader extends ColumbaHeader implements HeaderInterface {

	protected Folder srcFolder;
	protected Object srcUid;
	protected ColumbaHeader srcHeader;

	public VirtualHeader(
		ColumbaHeader header,
		Folder srcFolder,
		Object srcUid) {
		super();
		
		if( header == null )
			System.out.println("test");

		for (Enumeration e = header.getHashtable().keys();
			e.hasMoreElements();
			) {
			Object o = e.nextElement();

			getHashtable().put(
				(String) o,
				header.getHashtable().get((String) o));

		}

		this.srcFolder = srcFolder;
		this.srcUid = srcUid;
	}

	/**
	 * Returns the srcFolder.
	 * @return Folder
	 */
	public Folder getSrcFolder() {
		return srcFolder;
	}

	/**
	 * Returns the srcUid.
	 * @return Object
	 */
	public Object getSrcUid() {
		return srcUid;
	}

}
