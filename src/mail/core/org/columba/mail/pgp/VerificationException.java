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
public class VerificationException extends PGPException {

	public VerificationException()
	{
		super("Verification failed");
	}
	
	public VerificationException( String message)
	{
		super(message);
		
	}
}
