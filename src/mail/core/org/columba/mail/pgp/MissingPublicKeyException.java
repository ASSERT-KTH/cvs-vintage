/*
 * Created on 30.09.2003
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package org.columba.mail.pgp;

/**
 * @author frd
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class MissingPublicKeyException extends VerificationException {

	/**
	 * 
	 */
	public MissingPublicKeyException() {
		super("Missing Public Key");
		
	}

	/**
	 * @param message
	 */
	public MissingPublicKeyException(String message) {
		super(message);
	}

}
