package org.columba.mail.folder;

import org.columba.core.command.WorkerStatusController;
import org.columba.mail.message.HeaderList;

/**
 * @author freddy
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public interface DataStorageInterface {
	public abstract void saveMessage( String source, Object uid ) throws Exception;
	
	
	public abstract String loadMessage( Object uid ) throws Exception ;
	public abstract void removeMessage( Object uid );
	
	public abstract int getMessageCount();
	
	public abstract boolean exists( Object uid ) throws Exception ;
	
	public abstract HeaderList recreateHeaderList( WorkerStatusController worker ) throws Exception ;
}
