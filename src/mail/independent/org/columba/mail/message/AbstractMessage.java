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

/**
 * @author freddy
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public abstract class AbstractMessage {

	protected String source=null;
	protected MimePartTree mimePartCollection;
	
	
	protected MimePart bodyPart;
	
	public AbstractMessage()
	{}
	
	public abstract HeaderInterface getHeader();
	public abstract void setHeader( HeaderInterface h );
	
	
	public String getSource()
	{
		return source;
	}
	
	public void setSource( String s )
	{
		source = s;
	}
	
	public void setBodyPart(MimePart body) {
		bodyPart = body;
	}

	public MimePart getBodyPart() {
		return bodyPart;
	}
	
	public int getSize() {
		Integer sizeInt = (Integer) getHeader().get("columba.size");

		return sizeInt.intValue();
	}

	public Flags getFlags() {
		return ((ColumbaHeader)getHeader()).getFlags();
	}

	public void setUID(Object o) {
		if (o != null)
			getHeader().set("columba.uid", o);
		else
			getHeader().set("columba.uid", new String(""));

		//uid = o;
	}

	public Object getUID() {
		return getHeader().get("columba.uid");
	}

	public MimePart getMimePart(int number) {
		return mimePartCollection.get(number);

	}

	public int getMimePartCount() {
		if( mimePartCollection != null )
		return mimePartCollection.count();
		else return 0;
	}

	public MimePartTree getMimePartTree() {
		return mimePartCollection;
	}

	public void setMimePartTree(MimePartTree ac) {
		mimePartCollection = ac;
	}
	
	public void freeMemory()
	{
		source=null;
	}
}
