//The contents of this file are subject to the Mozilla Public License Version 1.1
//(the "License"); you may not use this file except in compliance with the 
//License. You may obtain a copy of the License at http://www.mozilla.org/MPL/
//
//Software distributed under the License is distributed on an "AS IS" basis,
//WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License 
//for the specific language governing rights and
//limitations under the License.
//
//The Original Code is "The Columba Project"
//
//The Initial Developers of the Original Code are Frederik Dietz and Timo Stich.
//Portions created by Frederik Dietz and Timo Stich are Copyright (C) 2003. 
//
//All Rights Reserved.
package org.columba.core.command;

import org.columba.core.gui.frame.AbstractFrameController;


/**
 * Provides a more intelligent GUI update mechanism.
 * <p>
 * Imagine having many commands executed in parallel not finishing
 * in the same order they were started. The problem is that 
 * the GUI reflects the changes of the finished commands in the order
 * they were started.
 * <p>
 * SelectiveGuiUpdateCommand provides a better approach in making the
 * gui update if the last Command is finished. This is mostly probably
 * also the gui update the user wants to see.
 * <p>
 * For example: User switches between a couple of folders very quickly.
 * He has to wait until all Commands are finished to see gui update of
 * the last one. You think of it as ignoring other gui updates because
 * the most recent one has higher priority.
 * 
 * 
 * 
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
		DefaultCommandReference[] references) {
		super(references);
	}
	
	public SelectiveGuiUpdateCommand(
			AbstractFrameController frame, DefaultCommandReference[] references) {
			super(frame, references);
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
