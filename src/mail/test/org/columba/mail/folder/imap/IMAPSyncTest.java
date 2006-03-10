package org.columba.mail.folder.imap;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import junit.framework.TestCase;

import org.columba.core.command.CommandCancelledException;
import org.columba.mail.imap.TestHeaderList;
import org.columba.mail.imap.TestServer;
import org.columba.mail.message.ColumbaHeader;
import org.columba.mail.message.IPersistantHeaderList;
import org.columba.ristretto.imap.IMAPException;
import org.columba.ristretto.imap.MailboxStatus;

public class IMAPSyncTest extends TestCase {

	public void testSimpleNewMails() throws IOException, CommandCancelledException, IMAPException, Exception {
		IPersistantHeaderList testList = new TestHeaderList();
		MailboxStatus status = new MailboxStatus();
		status.setMessages(100);
		status.setUidNext(101);
		TestServer testServer = new TestServer(status);
		
		IMAPFolder folder = new IMAPFolder(testList, testServer);
		
		// Add some messages
		for( int i=1; i<=100; i++) {
			ColumbaHeader header = new ColumbaHeader();
			header.set("columba.uid", i);
			
			testList.add(header, i);
			testServer.addHeader(header,i);
		}
		
		
		// Check for the new messages
		for( int i=1; i<=10; i++) {
			ColumbaHeader header = new ColumbaHeader();
			header.set("columba.uid", i+100);
			testServer.addHeader(header,i+100);
		}
		status.setMessages(110);
		status.setUidNext(111);

		assertEquals(100, testList.count());		
		folder.synchronizeHeaderlist();
		
		// Check for success
		assertEquals(110, testList.count());
		List keys = Arrays.asList(testList.getUids());
		Collections.sort(keys);
		int testUid = 1;
		for( Object uid: keys) {
			assertEquals(testUid++, uid);
		}
		
		assertEquals(111, testUid);
	}
	
	public void testSimpleRemovedMails() throws IOException, CommandCancelledException, IMAPException, Exception {
		IPersistantHeaderList testList = new TestHeaderList();
		MailboxStatus status = new MailboxStatus();
		status.setMessages(100);
		status.setUidNext(101);
		TestServer testServer = new TestServer(status);
		
		IMAPFolder folder = new IMAPFolder(testList, testServer);
		
		// Add some messages
		for( int i=1; i<=100; i++) {
			ColumbaHeader header = new ColumbaHeader();
			header.set("columba.uid", i);
			
			testList.add(header, i);
			testServer.addHeader(header,i);
		}
		
		
		// Check for the new messages
		for( int i=1; i<=10; i++) {
			testServer.removeHeader(100 - 3 * i);
		}
		status.setMessages(90);

		assertEquals(100, testList.count());		
		folder.synchronizeHeaderlist();
		
		// Check for success
		assertEquals(90, testList.count());
		List keys = Arrays.asList(testList.getUids());
		for( Object uid: keys) {
			if(testServer.getHeaderList().exists(uid) == false) {
				System.out.println("UID is removed but not from headerlist: " + uid);
				assertTrue(false);
			}
		}
	}

	public void testNewAndRemovedMails() throws IOException, CommandCancelledException, IMAPException, Exception {
		IPersistantHeaderList testList = new TestHeaderList();
		MailboxStatus status = new MailboxStatus();
		status.setMessages(100);
		status.setUidNext(101);
		TestServer testServer = new TestServer(status);
		
		IMAPFolder folder = new IMAPFolder(testList, testServer);
		
		// Add some messages
		for( int i=1; i<=100; i++) {
			ColumbaHeader header = new ColumbaHeader();
			header.set("columba.uid", i);
			
			testList.add(header, i);
			testServer.addHeader(header,i);

		}
		
		
		// Add some new messages
		// and remove the same number of old messages
		for( int i=1; i<=10; i++) {
			ColumbaHeader header = new ColumbaHeader();
			header.set("columba.uid", i+100);
			testServer.addHeader(header,i+100);
			testServer.removeHeader(100 - 3 * i);
			
		}
		status.setMessages(100);
		status.setUidNext(111);

		assertEquals(100, testList.count());		
		folder.synchronizeHeaderlist();
		
		// Check for success
		assertEquals(100, testList.count());
		List keys = Arrays.asList(testList.getUids());
		for( Object uid: keys) {
			if(testServer.getHeaderList().exists(uid) == false) {
				System.out.println("UID is removed but not from headerlist: " + uid);
				assertTrue(false);
			}
		}
	}

}
