package org.columba.mail.filter.action;

import org.columba.core.command.Command;
import org.columba.core.gui.FrameController;
import org.columba.mail.filter.FilterAction;
import org.columba.mail.folder.Folder;

/**
 * @author freddy
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public abstract class AbstractFilterAction {

	protected FrameController frameController;
	protected FilterAction filterAction;
	protected Folder srcFolder;
	protected Object[] uids;
	/**
	 * Constructor for AbstractFilterAction.
	 */
	public AbstractFilterAction(FrameController frameController, FilterAction filterAction, Folder srcFolder, Object[] uids) {
		this.frameController = frameController;
		this.filterAction = filterAction;
		this.srcFolder = srcFolder;
		this.uids = uids;
	}
	
	public abstract Command getCommand() throws Exception;
	
	
}
