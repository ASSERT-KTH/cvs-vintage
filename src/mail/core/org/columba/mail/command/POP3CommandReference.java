package org.columba.mail.command;

import org.columba.core.command.DefaultCommandReference;
import org.columba.mail.pop3.POP3Server;

/**
 * @author freddy
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class POP3CommandReference extends DefaultCommandReference {

	protected POP3Server server;
	protected Object[] uids;
	
	public POP3CommandReference(POP3Server server)
	{
		this.server = server;
	}
	
	public POP3CommandReference(POP3Server server, Object[] uids)
	{
		this.server = server;
		this.uids = uids;
	}
	
	/**
	 * Returns the server.
	 * @return POP3Server
	 */
	public POP3Server getServer() {
		return server;
	}

	public Object[] getUids()
	{
		return uids;
	}
	
}
