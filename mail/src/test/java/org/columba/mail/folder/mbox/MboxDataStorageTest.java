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

package org.columba.mail.folder.mbox;

import java.io.ByteArrayInputStream;

import junit.framework.TestCase;

import org.columba.mail.folder.MHFolderFactory;
import org.columba.ristretto.io.Source;

public class MboxDataStorageTest extends TestCase {

	private static final String mail1 = "From: alice1@columba-mail.org\r\nTo: bob@columba-mail.org\r\n\r\ntest 1 mail";
	private static final String mail2 = "From: alice2@columba-mail.org\r\nTo: bob@columba-mail.org\r\n\r\ntest   2  mail";
	private static final String mail3 = "From: alice3@columba-mail.org\r\nTo: bob@columba-mail.org\r\n\r\ntest    3    mail";
	
	public void testSaveMessage() throws Exception {
		MboxDataStorage storage = new MboxDataStorage(new MHFolderFactory().createFolder(100));
		
		storage.saveMessage(new Integer(1), new ByteArrayInputStream(mail1.getBytes("US-ASCII")));
		
		Source source = storage.getMessageSource(new Integer(1));
		
		assertEquals(mail1, source.toString());
		
		storage.removeMessage(new Integer(1));
		
		assertFalse(storage.exists(new Integer(1)));
		assertEquals(storage.getMessageCount(), 0);
	}
	
	public void testSaveMultipleMessages() throws Exception {
		MboxDataStorage storage = new MboxDataStorage(new MHFolderFactory().createFolder(100));
		
		storage.saveMessage(new Integer(1), new ByteArrayInputStream(mail1.getBytes("US-ASCII")));
		storage.saveMessage(new Integer(2), new ByteArrayInputStream(mail2.getBytes("US-ASCII")));
		
		Source source = storage.getMessageSource(new Integer(1));
		assertEquals(mail1, source.toString());
		source = null;
		
		storage.saveMessage(new Integer(3), new ByteArrayInputStream(mail3.getBytes("US-ASCII")));

		storage.removeMessage(new Integer(1));
		storage.removeMessage(new Integer(3));

		source = storage.getMessageSource(new Integer(2));
		assertEquals(mail2, source.toString());
	}
	
	public void testLoadSave() throws Exception {
		MboxDataStorage storage = new MboxDataStorage(new MHFolderFactory().createFolder(100));
		
		storage.saveMessage(new Integer(1), new ByteArrayInputStream(mail1.getBytes("US-ASCII")));
		storage.saveMessage(new Integer(2), new ByteArrayInputStream(mail2.getBytes("US-ASCII")));
		
		storage.save();
		
		storage.load();
		Source source = storage.getMessageSource(new Integer(1));
		assertEquals(mail1, source.toString());
		
	}


}
