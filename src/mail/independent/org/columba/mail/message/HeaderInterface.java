package org.columba.mail.message;

/**
 * @author freddy
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public interface HeaderInterface {
	public abstract void set( String key, Object value);
	public abstract Object get(String key);
	public abstract Flags getFlags();
}
