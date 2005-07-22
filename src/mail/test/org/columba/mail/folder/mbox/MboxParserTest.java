package org.columba.mail.folder.mbox;

import java.io.IOException;

import junit.framework.TestCase;

import org.columba.ristretto.io.CharSequenceSource;

public class MboxParserTest extends TestCase {

	public void test1() throws IOException {
		String testMbox = "From god@heaven.af.mil Sat Jan  3 01:05:34 1996\n" +
				"1\n";
          
		MboxMessage[] messages = MboxParser.parseMbox(new CharSequenceSource(testMbox));
		
		assertEquals(1,messages.length);
		
		String message = testMbox.substring((int)messages[0].getStart(), (int)(messages[0].getStart() + messages[0].getLength()));
		
		assertEquals("1\n", message);
		
	}
	
	
	public void test2() throws IOException {
		String testMbox = "From god@heaven.af.mil Sat Jan  3 01:05:34 1996\n" +
				"1\n" +
				"From god@heaven.af.mil Sat Jan  3 01:05:34 1996\n" +
				"2\n";
          
		MboxMessage[] messages = MboxParser.parseMbox(new CharSequenceSource(testMbox));
		
		assertEquals(2,messages.length);
		
		String message = testMbox.substring((int)messages[0].getStart(), (int)(messages[0].getStart() + messages[0].getLength()));
		
		assertEquals("1\n", message);
		
		message = testMbox.substring((int)messages[1].getStart(), (int)(messages[1].getStart() + messages[1].getLength()));
		assertEquals("2\n", message);
		
	}	

	public void test3() throws IOException {
		String testMbox = "From god@heaven.af.mil Sat Jan  3 01:05:34 1996\n" +
				"From 1\n" +
				"From god@heaven.af.mil Sat Jan  3 01:05:34 1996\n" +
				"2\n";
          
		MboxMessage[] messages = MboxParser.parseMbox(new CharSequenceSource(testMbox));
		
		assertEquals(2,messages.length);
		
		String message = testMbox.substring((int)messages[0].getStart(), (int)(messages[0].getStart() + messages[0].getLength()));
		
		assertEquals("From 1\n", message);
		
		message = testMbox.substring((int)messages[1].getStart(), (int)(messages[1].getStart() + messages[1].getLength()));
		assertEquals("2\n", message);
		
	}	

}
