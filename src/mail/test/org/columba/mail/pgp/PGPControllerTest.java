/*
 * Created on Jul 10, 2003
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package org.columba.mail.pgp;

import junit.framework.TestCase;

import org.columba.core.xml.XmlElement;
import org.columba.mail.config.PGPItem;

/**
 * @author waffel
 */
public class PGPControllerTest extends TestCase {

	XmlElement xmle;
	/**
	 * Constructor for PGPControllerTest.
	 * @param arg0
	 */
	public PGPControllerTest(String arg0) {
		super(arg0);
		xmle = new XmlElement();
		xmle.addAttribute("path", "/usr/bin/gpg");
		xmle.addAttribute("id", "testid");
	}

	public void testVerifySig() {
		String sigData =
			"-----BEGIN PGP SIGNATURE-----\n"
				+ "Version: GnuPG v1.2.2-rc1-SuSE (GNU/Linux)\n\n"
				+ "iD8DBQA/Dsyrrax/zviVYscRAkf2AJ9Y5h30EGkNplHCMCJg9wYOJk+t9wCdFBDO\n"
				+ "UT8MCuHOLNANpmWGqGWFmkc=\n"
				+ "=P9GA\n"
				+ "-----END PGP SIGNATURE-----";
		String pgpMessage = "eine Testmessage";
		PGPController pgpContr =PGPController.getInstance();
		PGPItem item = new PGPItem(xmle);
		int retVal = pgpContr.verifySignature(pgpMessage, sigData, item);
		assertEquals(0,retVal);
	}
	/**
	* We can only test this, if verify is ok! What we test is, if the given testMessage is correct signed
	*/
	public void testSign() {
		String testData = "eine Testmessage";
		// new PGPController
		PGPController pgpContr = PGPController.getInstance();
		// new Item with null, this is while the passphrase is in other situations in the configFile, here we set it per Hand
		PGPItem item = new PGPItem(xmle);
		// test passphrase. You must have a test pgp-key with this passphrase
		item.setPassphrase("test");
		String signStr = pgpContr.sign(testData, item);
		int retVal = pgpContr.verifySignature(testData, signStr, item);
		assertEquals(0,retVal);
	}

}
