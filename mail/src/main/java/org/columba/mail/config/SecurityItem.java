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
package org.columba.mail.config;

import org.columba.core.config.DefaultItem;
import org.columba.core.xml.XmlElement;

/**
 * @author waffel / Aug 5, 2005 2:29:21 PM
 * 
 * Security item to holding data related to security aspects like passphrase and
 * digest algorithm.
 */
public class SecurityItem extends DefaultItem {
	private String passphrase = "";

	private String digestAlgorithm;

	public static final String ALWAYS_ENCRYPT = "always_encrypt";

	public static final String ALWAYS_SIGN = "always_sign";

	public static final String ENABLED = "enabled";

	public static final String PATH = "path";

	public static final String ID = "id";

	/**
	 * Creates a new SecurityItem instance and sets the passphrase empty.
	 * 
	 * @param e
	 *            XML element to be used for this item.
	 */
	public SecurityItem(XmlElement e) {
		super(e);

		this.passphrase = "";
	}

	/**
	 * Returns the passphrase of the scurity item. If the passphrase was not
	 * set, an empty string is returned.
	 * 
	 * @return the passphrase of this item.
	 */
	public String getPassphrase() {
		return this.passphrase;
	}

	/**
	 * Sets the passphase of this item to an empty string.
	 */
	public void clearPassphrase() {
		this.passphrase = "";
	}

	/**
	 * Sets the passphrase to the given string. The passphrase is not scrambled
	 * or encrypted in this method. Only plain passphrase are supported.
	 * 
	 * @param s
	 *            string which should assigned to the passphrase.
	 */
	public void setPassphrase(String s) {
		this.passphrase = s;
	}

	/**
	 * Sets the digest algorithm to be used in an security operation like
	 * encrypting or decrypting. If the digest algorithm was not set, the method
	 * returns an empty string.
	 * 
	 * @return returns the digest algorithm of this security item.
	 */
	public String getDigestAlgorithm() {
		return this.digestAlgorithm;
	}

	/**
	 * Sets the digest algorithm for this security item.
	 * 
	 * @param _digestAlgorithm
	 *            algorithm to be set.
	 */
	public void setDigestAlgorithm(String _digestAlgorithm) {
		this.digestAlgorithm = _digestAlgorithm;
	}
}
