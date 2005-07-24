package org.columba.core.io;

import java.io.File;

import junit.framework.TestCase;

public class DefaultMimeTypeTableTest extends TestCase {

	public void test1() {
		assertEquals("text/plain", DefaultMimeTypeTable.lookup("txt"));
		assertEquals("image/jpeg", DefaultMimeTypeTable.lookup("jpg"));
		assertEquals("image/png", DefaultMimeTypeTable.lookup("png"));
		assertEquals("application/msword", DefaultMimeTypeTable.lookup("doc"));
	}
	
	public void test2() {
		assertEquals("text/plain", DefaultMimeTypeTable.lookup(new File("text.bla.txt")));
		assertEquals("application/octet-stream", DefaultMimeTypeTable.lookup(new File("")));
		assertEquals("application/octet-stream", DefaultMimeTypeTable.lookup(new File(".")));
		assertEquals("application/msword", DefaultMimeTypeTable.lookup(new File("blabla.doc")));
	}
}
