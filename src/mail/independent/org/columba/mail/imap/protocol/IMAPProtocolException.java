package org.columba.mail.imap.protocol;

import org.columba.mail.imap.IMAPResponse;

/**
 * @author freddy
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class IMAPProtocolException extends IMAPException {

	public IMAPProtocolException( String s)
	{
		super( s );
	}
	
	public IMAPProtocolException( IMAPResponse response )
	{
		super(response);
	}
}
