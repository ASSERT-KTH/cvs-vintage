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
 *@deprecated Use JSCF
 */
public class VerificationException extends PGPException {
    /**
 * 
 *@deprecated Use JSCF
 */
    public VerificationException() {
        super("Verification failed");
    }

    /**
 * @param message
 * @deprecated Use JSCF
 */
    public VerificationException(String message) {
        super(message);
    }
}
