/*
 * Created on Jul 4, 2003
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package org.columba.mail.coder;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import junit.framework.TestCase;

/**
 * @author frd
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class Base64DecoderTest extends TestCase {

	/**
	 * Constructor for Base64DecoderTest.
	 * @param arg0
	 */
	public Base64DecoderTest(String arg0) {
		super(arg0);
		CoderRouter router = new CoderRouter();
	}

	/*
	 * Test for String decode(String, String), charset null
	 */
	public void testDecodeStringString1() {
		String testStr = "YWJjZA==";
		Base64Decoder decoder = new Base64Decoder();
		try {
			String decodedStr = decoder.decode(testStr, null);
			sun.misc.BASE64Decoder sunDecoder = new sun.misc.BASE64Decoder();
			String retStr = new String(sunDecoder.decodeBuffer(testStr));
			assertTrue(retStr.equals(decodedStr));
		} catch (UnsupportedEncodingException e) {
			assertFalse(true);
		} catch (IOException e) {
			assertFalse(true);
		}
	}
	
	public void testDecodeStringString2() {
		String testStr = "MTIzNDU2";
		Base64Decoder decoder = new Base64Decoder();
		try {
			String decodedStr = decoder.decode(testStr, null);
			sun.misc.BASE64Decoder sunDecoder = new sun.misc.BASE64Decoder();
			String retStr = new String(sunDecoder.decodeBuffer(testStr));
			assertTrue(retStr.equals(decodedStr));
		} catch (UnsupportedEncodingException e) {
			assertFalse(true);
		} catch (IOException e) {
			assertFalse(true);
		}
	}
	
	public void testDecodeStringString3() {
		String testStr = "Pz0pKC8mJSSnIiE=";
		Base64Decoder decoder = new Base64Decoder();
		try {
			String decodedStr = decoder.decode(testStr, null);
			sun.misc.BASE64Decoder sunDecoder = new sun.misc.BASE64Decoder();
			String retStr = new String(sunDecoder.decodeBuffer(testStr));
			assertTrue(retStr.equals(decodedStr));
		} catch (UnsupportedEncodingException e) {
			assertFalse(true);
		} catch (IOException e) {
			assertFalse(true);
		}
	}
	
	public void testDecodeStringString4() {
		String testStr = "YWJjZGVzZmdoaWprbG1ub3BxcnN0dXZ3eHl6MTIzNDU2Nzg5MCFhYmNkZXNmZ2hpamtsbW5vcHFy\n"+
		"c3R1dnd4eXoxMjM0NTY3ODkwIWFiY2Rlc2ZnaGlqa2xtbm9wcXJzdHV2d3h5ejEyMzQ1Njc4OTAh\n"+
		"YWJjZGVzZmdoaWprbG1ub3BxcnN0dXZ3eHl6MTIzNDU2Nzg5MCFhYmNkZXNmZ2hpamtsbW5vcHFy\n"+
		"c3R1dnd4eXoxMjM0NTY3ODkwIWFiY2Rlc2ZnaGlqa2xtbm9wcXJzdHV2d3h5ejEyMzQ1Njc4OTAh\n"+
		"YWJjZGVzZmdoaWprbG1ub3BxcnN0dXZ3eHl6MTIzNDU2Nzg5MCFhYmNkZXNmZ2hpamtsbW5vcHFy\n"+
		"c3R1dnd4eXoxMjM0NTY3ODkwIWFiY2Rlc2ZnaGlqa2xtbm9wcXJzdHV2d3h5ejEyMzQ1Njc4OTAh";
		Base64Decoder decoder = new Base64Decoder();
		try {
			String decodedStr = decoder.decode(testStr, null);
			sun.misc.BASE64Decoder sunDecoder = new sun.misc.BASE64Decoder();
			String retStr = new String(sunDecoder.decodeBuffer(testStr));
			assertTrue(retStr.equals(decodedStr));
		} catch (UnsupportedEncodingException e) {
			assertFalse(true);
		} catch (IOException e) {
			assertFalse(true);
		}
	}	

	/*
	 * Test for void decode(InputStream, OutputStream)
	 */
	public void testDecodeInputStreamOutputStream() {
	}

}
