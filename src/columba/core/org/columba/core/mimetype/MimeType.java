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
package org.columba.core.mimetype;

import java.io.File;
import java.net.MalformedURLException;

import org.jdesktop.jdic.filetypes.Association;
import org.jdesktop.jdic.filetypes.AssociationService;

/**
 * Retrieve associated mimetype of file.
 * 
 * @author fdietz
 */
public class MimeType {

	private static AssociationService associationService = new AssociationService();

	/**
	 * @see org.columba.core.nativ.mimetype.LookupMimetype#lookup(java.io.File)
	 */
	public static String lookup(File file) {
		String mimetype = "application/octet-stream";
		
		try {
			Association a = associationService.getAssociationByContent( file.toURL());
			if( a != null) {
				return a.getMimeType();
			}
		} catch (MalformedURLException e) {
		}
		
		return mimetype;
	}

	/**
	 * @see org.columba.core.nativ.mimetype.LookupMimetype#lookup(java.io.File)
	 */
	public static String lookupByExtension(String extension) {
		String mimetype = "application/octet-stream";
		
		Association a = associationService.getFileExtensionAssociation( extension );
		if( a != null) {
			return a.getMimeType();
		}
		
		return mimetype;
	}
}