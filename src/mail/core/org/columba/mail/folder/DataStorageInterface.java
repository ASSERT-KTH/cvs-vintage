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

import java.io.InputStream;

import org.columba.ristretto.message.io.FileSource;


/**
 * Interface for local folders. This is a complete separation from the
 * headercache and the datastorage method.
 * <p>
 * This makes it very easy to add new mailbox formats.
 * 
 * @see org.columba.mail.folder.LocalFolder
 *
 * @author fdietz
 */
public interface DataStorageInterface {
	public void saveMessage( String source, Object uid ) throws Exception;
	
	
	public String loadMessage( Object uid ) throws Exception ;
	public void removeMessage( Object uid );
	
	public FileSource getFileSource( Object uid ) throws Exception;
	public void saveInputStream( Object uid, InputStream source) throws Exception;
	
	public int getMessageCount();
	
	public boolean exists( Object uid ) throws Exception ;
	
	public Object[] getMessageUids();
	
}
