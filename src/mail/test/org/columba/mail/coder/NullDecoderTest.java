package org.columba.mail.coder;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;

import junit.framework.TestCase;

/**
 * @author Timo Stich (tstich@users.sourceforge.net)
 * 
 */
public class NullDecoderTest extends TestCase {

	Decoder decoder;

	/**
	 * Constructor for NullDecoderTest.
	 * @param arg0
	 */
	public NullDecoderTest(String arg0) {
		super(arg0);
	}

	/*
	 * Test for String decode(String, String)
	 */
	public void testDecodeStringString() {
		byte[] test = { -1, -30 , 100 };
		byte[] resultArray;
		String testString = null;
		String result = null;
		decoder = new NullDecoder();

		try {
			testString = new String( test, "iso-8859-1" );
			result = decoder.decode(testString, "iso-8859-1");
			resultArray = result.getBytes("iso-8859-1");

			assertTrue( Arrays.equals( test, resultArray) );
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		

	}

	/*
	 * Test for void decode(InputStream, OutputStream)
	 */
	public void testDecodeInputStreamOutputStream() {
	}

	/**
	 * @see junit.framework.TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();
		
		new CoderRouter();
		
	}

}
