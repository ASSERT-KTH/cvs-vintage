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

import java.lang.reflect.Array;

public class MimePart extends MimeTreeNode {
	protected Object content;

	protected MimeHeader mimeHeader;

	public MimePart() {
		super();
		mimeHeader = new MimeHeader();
	}

	public MimePart(MimeHeader header) {
		super();
		mimeHeader = header;
	}

	public MimePart(MimeHeader header, Object content ) {
		super();
		mimeHeader = header;
		
		this.content = content;
	}


	public MimeHeader getHeader() {
		return mimeHeader;
	}

	/**
	 * Method setHeader.
	 * @param h
	 */
	public void setHeader(MimeHeader h) {
		mimeHeader = h;
	}

	/**
	 * Gets the body.
	 * @return Returns a String
	 */
	public String getBody() {
		return (String) content;
	}

	/**
	 * Sets the body.
	 * @param body The body to set
	 */
	public void setBody(String body) {
		content = body;
	}
	

	// Own count() method inherited from MimeTreeNode

	public int count() {
		// If this is a Multipart/Alternative then return also only 1
		if (mimeHeader.contentSubtype.equals("alternative"))
			return 1;
		
		return super.count();	
	}

	

	/**
	 * Gets the content.
	 * @return Returns a Object
	 */
	public Object getContent() {
		return content;
	}

	/**
	 * Sets the content.
	 * @param content The content to set
	 */
	public void setContent(Object content) {
		this.content = content;
	}

	public boolean equals(Object obj) {
		MimePart other = (MimePart) obj;
		if( other == null ) return false;

		Object[] thisAttributes = {
			content,
			mimeHeader };

		Object[] otherAttributes = {
			other.getContent(),
			other.getHeader()	
		};

		for( int i=0; i<Array.getLength( thisAttributes ); i++ ) {
			if( thisAttributes[i] == otherAttributes[i])
				continue;
			
			if( thisAttributes[i] == null )
				return false;
				
			if( otherAttributes[i] == null )
				return false;
				
			if( !thisAttributes[i].equals( otherAttributes[i] ) )
				return false;
		}
				
		return true;
	}

}
