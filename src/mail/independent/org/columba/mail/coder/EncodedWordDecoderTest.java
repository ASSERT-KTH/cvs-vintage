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

import junit.framework.TestCase;

/**
 * @author Timo Stich (tstich@users.sourceforge.net)
 * 
 */
public class EncodedWordDecoderTest extends TestCase {

	private String testString[] = { "Do nothing String",
		"[Columba-devel] Re: [Columba-devel] =?ISO-8859-15?Q?Re:_[Columba-devel]_Minor_GUI_improvement_in_mail_tree?= bla bla",
		"[Columba-devel] Re: [Columba-devel] =?ISO-8859-15?Q?Re:_[Columba-devel]_Minor_GUI_improvement_in_mail_tree? =",
		"=?bad_encoded_word?=" };

	private String resultString[] = { "Do nothing String","[Columba-devel] Re: [Columba-devel] Re: [Columba-devel] Minor GUI improvement in mail tree bla bla",
		"[Columba-devel] Re: [Columba-devel] =?ISO-8859-15?Q?Re:_[Columba-devel]_Minor_GUI_improvement_in_mail_tree? =",
		 "=?bad_encoded_word?=" };

	/**
	 * Constructor for EncodedWordDecoderTest.
	 * @param arg0
	 */
	public EncodedWordDecoderTest(String arg0) {
		super(arg0);
	}
	
	

	public void testDecode() {
		EncodedWordDecoder decoder = new EncodedWordDecoder();
		
		String result;
		
		for( int i=0; i<testString.length; i++ ) {
			result = decoder.decode(testString[i]);
			assertTrue( result.equals( resultString[i] ));
		}				
	}

	/**
	 * @see junit.framework.TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		new CoderRouter();
	}

}
