// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU Library General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.

package org.columba.mail.message;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Vector;

import org.columba.mail.coder.CoderRouter;
import org.columba.mail.coder.Encoder;

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

		mimeHeader.contentTransferEncoding = "base64";			
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
