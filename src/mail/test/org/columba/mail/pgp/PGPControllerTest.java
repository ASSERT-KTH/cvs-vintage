/*
 * Created on Jul 10, 2003
 *

 */
package org.columba.mail.pgp;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import junit.framework.TestCase;

import org.columba.core.io.StreamUtils;
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

	public void testVerifySig() throws Exception {
		String sigData =
			"-----BEGIN PGP SIGNATURE-----\n"
				+ "Version: GnuPG v1.2.2-rc1-SuSE (GNU/Linux)\n\n"
				+ "iD8DBQA/Dsyrrax/zviVYscRAkf2AJ9Y5h30EGkNplHCMCJg9wYOJk+t9wCdFBDO\n"
				+ "UT8MCuHOLNANpmWGqGWFmkc=\n"
				+ "=P9GA\n"
				+ "-----END PGP SIGNATURE-----";
		InputStream sigDataStream =
			new ByteArrayInputStream(sigData.getBytes("ISO-8859-1"));
		String pgpMessage = "eine Testmessage";
		InputStream message =
			new ByteArrayInputStream(pgpMessage.getBytes("ISO-8859-1"));
		PGPController pgpContr = PGPController.getInstance();
		PGPItem item = new PGPItem(xmle);
		// TODO: add better testcase
		//assertTrue(pgpContr.verifySignature(message, sigDataStream, item));
	}
	/**
	* We can only test this, if verify is ok! What we test is, if the given testMessage is correct signed
	*/
	public void testSign() throws Exception {
		String testData = "eine Testmessage";
		InputStream testDataStream =
			new ByteArrayInputStream(testData.getBytes("ISO-8859-1"));
		// we must remeber the first position, so later we can at the the begin of the Stream. In other cases (real running 
		// system) we should create a new Stream 
		testDataStream.mark(80000);
		// new PGPController
		PGPController pgpContr = PGPController.getInstance();
		// new Item with null, this is while the passphrase is in other situations in the configFile, here we set it per Hand
		PGPItem item = new PGPItem(xmle);
		// test passphrase. You must have a test pgp-key with this passphrase
		item.setPassphrase("test");
		InputStream signStream = pgpContr.sign(testDataStream, item);
		// go to the begin of the Stream
		testDataStream.reset();
		
//		TODO: add better testcase
		//assertTrue(pgpContr.verifySignature(testDataStream, signStream, item));
	}

	public void testDecrypt() throws Exception {
		String testData =
			"-----BEGIN PGP MESSAGE-----\n"
				+ "Version: GnuPG v1.2.2-rc1-SuSE (GNU/Linux)\n\n"
				+ "hQEOA0/l9LX8K2GEEAP/drK0opSL8CFpzdH/HAx4tMFV1GYKEVWLcqgshksahAQ/\n"
				+ "yqhr5vVcOoNqIkdx6w7txPJnbaoRvhBuz0TEw6hrMkNheEEAkucLHfdJ5Z9iFodv\n"
				+ "g/VpF5gnsOYBSS6zgizp0DTtElUx3um+7u7PwB3s1Ud6Dhs6XuRTJPiyPl0isHcD\n"
				+ "/iAPGMzC9Vs1txMPnP7y969K+fYsIDsFxIiNQcw72/Bg9esY3EyFw8hJ5lLN0mDs\n"
				+ "lN5DnIDcRoVCRvFH+czVdoVpkQeLyB/a8t4P01Y7vgvNpoBJeUHNgdzoZ9QGQhGa\n"
				+ "zNzkVobyO3Fbp11kgiKDTXaWrcdYj4nN/+QBi3QgD9zT0kwBg/RQoKBkHnPZlOia\n"
				+ "EaR4QFQ9TFJqM5FG6xZp64XJ1hHArIXl7zowWfAXAOuMm6St3O9+JcVRHIqSzw63\n"
				+ "1OLBw4uSX/6AakYfiVY1\n"
				+ "=ABon\n"
				+ "-----END PGP MESSAGE-----";
		InputStream testDataStream =
			new ByteArrayInputStream(testData.getBytes("ISO-8859-1"));
		PGPController pgpController = PGPController.getInstance();
		PGPItem item = new PGPItem(xmle);
		item.setPassphrase("test");
		InputStream ret = pgpController.decrypt(testDataStream, item);
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		StreamUtils.streamCopy(ret, out);
		assertEquals("eine Testmessage", out.toString());
	}
	
	public void testEncrypt() throws Exception {
		String testData = "eine Testmessage";
		InputStream testDataStream = new ByteArrayInputStream(testData.getBytes("ISO-8859-1"));
		PGPController pgpController = PGPController.getInstance();
		PGPItem item = new PGPItem(xmle);
		item.set("recipients","testid");
		item.setPassphrase("test");
		InputStream encryptStream = pgpController.encrypt(testDataStream, item);
		InputStream ret = pgpController.decrypt(encryptStream, item);
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		StreamUtils.streamCopy(ret, out);
		assertEquals(testData, out.toString());
		
	}

}
