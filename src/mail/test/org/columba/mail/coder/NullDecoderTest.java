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
