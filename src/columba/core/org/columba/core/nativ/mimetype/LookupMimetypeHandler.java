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
package org.columba.core.nativ.mimetype;

import java.io.File;

import org.columba.core.util.OSInfo;

/**
 * Retrieve associated mimetype of file.
 * 
 * @author fdietz
 */
public class LookupMimetypeHandler implements LookupMimetype {

	public LookupMimetypeHandler() {	
	}
	
	/**
	 * @see org.columba.core.nativ.mimetype.LookupMimetype#lookup(java.io.File)
	 */
	public String lookup(File file) {
		String mimetype = null;
		
		if (OSInfo.isWin32Platform()) {
			// win32 lookup using windows registry
			mimetype = new Win32LookupMimetype().lookup(file);
		} else {
			// using JDK's built-in mimetype lookup facility
			mimetype = new GenericLookupMimetype().lookup(file);
		}
		
		// if windows failed, fall-back to generic
		if ( mimetype == null) {
			mimetype = new GenericLookupMimetype().lookup(file);
		}
		
		return mimetype;
	}

}