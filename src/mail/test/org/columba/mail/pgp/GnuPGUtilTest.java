package org.columba.mail.pgp;

import junit.framework.TestCase;

public class GnuPGUtilTest extends TestCase {

	private String toParseStr = "";
  private String expected = "";
  private GnuPGUtil gpgUtil;

	public GnuPGUtilTest(String arg0) {
		super(arg0);
	}

	protected void setUp() throws Exception {
		super.setUp();
    this.gpgUtil = new GnuPGUtil();
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}

	public void testParse1() {
    this.toParseStr = "gpg:eine Zeile\neine Zeile ohne gpg\ngpg:noch eine Zeile "+
     "mit gpg\nund eine mit gpg: inside";
    this.expected = "eine Zeile\neine Zeile ohne gpg\nnoch eine Zeile mit gpg\nund eine mit gpg: inside";
    assertEquals(gpgUtil.parse(this.toParseStr),expected);
    this.toParseStr = "";
    this.expected = "";
    assertEquals(gpgUtil.parse(this.toParseStr),expected);
	}

}
