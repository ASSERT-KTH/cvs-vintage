package org.columba.core.command;

import org.columba.core.gui.FrameController;

/**
 * @author Timo Stich (tstich@users.sourceforge.net)
 * 
 */
public abstract class SelectiveGuiUpdateCommand extends Command {
	
	private static int lastTimeStamp;

	/**
	 * Constructor for SelectiveGuiUpdateCommand.
	 * @param frameController
	 * @param references
	 */
	public SelectiveGuiUpdateCommand(
		FrameController frameController,
		DefaultCommandReference[] references) {
		super(frameController, references);
	}
	/**
	 * @see org.columba.core.command.Command#updateGUI()
	 */
	public void finish() throws Exception {
		if( getTimeStamp() == lastTimeStamp ) updateGUI();
	}

	/**
	 * @see org.columba.core.command.Command#setTimeStamp(int)
	 */
	public void setTimeStamp(int timeStamp) {
		super.setTimeStamp(timeStamp);
		
		if( timeStamp > lastTimeStamp ) {
			lastTimeStamp = timeStamp;
		}
	}

}
