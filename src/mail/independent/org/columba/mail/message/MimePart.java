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
