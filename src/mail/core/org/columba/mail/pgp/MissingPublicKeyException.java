/*
 * Created on 30.09.2003
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package org.columba.mail.pgp;


/**
 * @author frd
 * @deprecated Use JSCF
 */
public class MissingPublicKeyException extends VerificationException {
    /**
     * @deprecated Use JSCF
     */
    public MissingPublicKeyException() {
        super("Missing Public Key");
    }

    /**
     * @param message
     * @deprecated Use JSCF
     */
    public MissingPublicKeyException(String message) {
        super(message);
    }
}
