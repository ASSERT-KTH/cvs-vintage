// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU Library General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.

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
