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

package org.columba.mail.message;

import java.io.File;

/**
 * @author timo
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class ComposerAttachment extends MimePart {
	
	public static final int		FILE = 0;

	private File fileAttachment;
	private int type;

	public ComposerAttachment(MimeHeader header, File file) {
		super();
		mimeHeader = header;
		
		setFileAttachment( file );
	}

	

	/**
	 * Returns the fileAttachment.
	 * @return File
	 */
	public File getFileAttachment() {
		return fileAttachment;
	}

	/**
	 * Returns the type.
	 * @return int
	 */
	public int getType() {
		return type;
	}

	/**
	 * Sets the fileAttachment.
	 * @param fileAttachment The fileAttachment to set
	 */
	public void setFileAttachment(File file) {
		this.fileAttachment = file;

		if( mimeHeader.contentType.equals( "text" ) ) {
			mimeHeader.contentTransferEncoding = "quoted-printable";			
		} else {			
			mimeHeader.contentTransferEncoding = "base64";
		}			
		
		
		mimeHeader.putContentParameter("name",file.getName());		
		mimeHeader.putDispositionParameter("filename",file.getName());		
		mimeHeader.contentDisposition = "attachment";
		
		type = 0;
	}

	/**
	 * Sets the type.
	 * @param type The type to set
	 */
	public void setType(int type) {
		this.type = type;
	}

}
