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
