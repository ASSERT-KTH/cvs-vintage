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
public class BadCommandException extends IMAPProtocolException {
	
	public BadCommandException( String s )
	{
		super( s );
	}
	
	public BadCommandException( IMAPResponse response )
	{
		super( response );
	}
}
