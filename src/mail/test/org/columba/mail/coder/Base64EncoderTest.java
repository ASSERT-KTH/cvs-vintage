/*
 * Created on Jul 4, 2003
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package org.columba.mail.coder;

import java.io.UnsupportedEncodingException;

import junit.framework.TestCase;

/**
 * @author frd
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class Base64EncoderTest extends TestCase {

	/**
	 * Constructor for Base64EncoderTest.
	 * @param arg0
	 */
	public Base64EncoderTest(String arg0) {
		super(arg0);
		CoderRouter router = new CoderRouter();
	}

	/*
	 * Test for String encode(String, String) with charset null. testing four alpha characters
	 */
	public void testEncodeStringString1() {
		String testStr = "abcd";
		Base64Encoder columbaEncoder = new Base64Encoder();
		try {
			String codedStr = columbaEncoder.encode(testStr, null);
			// should be 'YWJjZA=='
			sun.misc.BASE64Encoder sunEncoder = new sun.misc.BASE64Encoder();
			assertTrue(sunEncoder.encode(testStr.getBytes()).equals(codedStr));
		} catch (UnsupportedEncodingException e) {
			assertFalse(true);
		}
	}
	/**
	 * Test encoding (String,String) with charset null. Only numbers are tested on a 6 sign basis
	 */
	public void testEncodeStringString2() {
		String testStr = "123456";
		Base64Encoder columbaEncoder = new Base64Encoder();
		try {
			String codedStr = columbaEncoder.encode(testStr, null);
			// should be 'MTIzNDU2'
			sun.misc.BASE64Encoder sunEncoder = new sun.misc.BASE64Encoder();
			assertTrue(sunEncoder.encode(testStr.getBytes()).equals(codedStr));
		} catch (UnsupportedEncodingException e) {
			assertFalse(true);
		}
	}
	/**
	 * Test encoding (String,String) with charset null. Test only extra characters
	 */
	public void testEncodeStringString3() {
		String testStr = "?=)(/&%$§\"!";
		Base64Encoder columbaEncoder = new Base64Encoder();
		try {
			String codedStr = columbaEncoder.encode(testStr, null);
			sun.misc.BASE64Encoder sunEncoder = new sun.misc.BASE64Encoder();
			assertTrue(sunEncoder.encode(testStr.getBytes()).equals(codedStr));
		} catch (UnsupportedEncodingException e) {
			assertFalse(true);
		}
	}
	/**
	 * Test encoding (String, String) with charset null. Test a long string
	 */
	public void testEncodeStringString4() {
		String testStr ="abcdesfghijklmnopqrstuvwxyz1234567890!abcdesfghijklmnopqrstuvwxyz1234567890!abcdesfghijklmnopqrstuvwxyz1234567890!" +
		"abcdesfghijklmnopqrstuvwxyz1234567890!abcdesfghijklmnopqrstuvwxyz1234567890!abcdesfghijklmnopqrstuvwxyz1234567890!"+
		"abcdesfghijklmnopqrstuvwxyz1234567890!abcdesfghijklmnopqrstuvwxyz1234567890!abcdesfghijklmnopqrstuvwxyz1234567890!";
		Base64Encoder columbaEncoder = new Base64Encoder();
		try {
			String codedStr = columbaEncoder.encode(testStr, null);
			sun.misc.BASE64Encoder sunEncoder = new sun.misc.BASE64Encoder();
			assertTrue(sunEncoder.encode(testStr.getBytes()).equals(codedStr));
		} catch (UnsupportedEncodingException e) {
			assertFalse(true);
		}
	}

	/*
	 * Test for void encode(InputStream, OutputStream, WorkerStatusController)
	 */
	public void testEncodeInputStreamOutputStreamWorkerStatusController() {
	}

}
