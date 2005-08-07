// The contents of this file are subject to the Mozilla Public License Version
// 1.1
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
//The Initial Developers of the Original Code are Frederik Dietz and Timo
// Stich.
//Portions created by Frederik Dietz and Timo Stich are Copyright (C) 2003.
//
//All Rights Reserved.

package org.columba.core.base;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.util.logging.Logger;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;

import org.columba.ristretto.coder.Base64;

public class Blowfish {
	private static final Logger LOG = Logger
	.getLogger("org.columba.util.blowfish");

	private static final byte[] BYTES = { -127, 88, 27, -88, -13, -56, 19, -4,
			45, 25, 38, 70, -17, 40, 36, -23 };
	
	private static final Key KEY = new SecretKeySpec(BYTES, "Blowfish");
	
	public static String encrypt(char[] source) {
			try {
				// Create the cipher
				Cipher blowCipher = Cipher.getInstance("Blowfish");

				// Initialize the cipher for encryption
				blowCipher.init(Cipher.ENCRYPT_MODE, KEY);

				// Our cleartext as bytes
				byte[] cleartext = new String(source).getBytes("UTF-8");

				// Encrypt the cleartext
				byte[] ciphertext = blowCipher.doFinal(cleartext);

				// Return a String representation of the cipher text
				return Base64.encode(ByteBuffer.wrap(ciphertext)).toString();
			} catch (InvalidKeyException e) {
			} catch (NoSuchAlgorithmException e) {
				LOG.severe(e.toString());
			} catch (NoSuchPaddingException e) {
			} catch (UnsupportedEncodingException e) {
				LOG.severe(e.toString());
			} catch (IllegalStateException e) {
			} catch (IllegalBlockSizeException e) {
			} catch (BadPaddingException e) {
			}
			
			return "";
	}

	public static char[] decrypt(String source) {
		try {
			// Create the cipher
			Cipher blowCipher = Cipher.getInstance("Blowfish");

			// Initialize the cipher for encryption
			blowCipher.init(Cipher.DECRYPT_MODE, KEY);

			// Encrypt the cleartext
			ByteBuffer ciphertext = Base64.decode(source);
			byte[] cipherArray = new byte[ciphertext.limit()];
			ciphertext.get(cipherArray);
			
			// Our cleartext as bytes
			byte[] cleartext = blowCipher.doFinal(cipherArray);


			// Return a String representation of the cipher text
			return new String(cleartext, "UTF-8").toCharArray();
		} catch (InvalidKeyException e) {
		} catch (NoSuchAlgorithmException e) {
			LOG.severe(e.toString());
		} catch (NoSuchPaddingException e) {
		} catch (UnsupportedEncodingException e) {
			LOG.severe(e.toString());
		} catch (IllegalStateException e) {
		} catch (IllegalBlockSizeException e) {
		} catch (BadPaddingException e) {
		}
		
		return new char[0];
}
}