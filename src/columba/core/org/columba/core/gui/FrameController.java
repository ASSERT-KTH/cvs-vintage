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

package org.columba.core.gui;

import java.awt.event.MouseAdapter;

import org.columba.core.gui.statusbar.StatusBar;
import org.columba.core.logging.ColumbaLogger;
import org.columba.core.main.MainInterface;
import org.columba.mail.gui.frame.TooltipMouseHandler;

/**
 * @author Timo Stich (tstich@users.sourceforge.net)
 * 
 */
public abstract class FrameController {

	protected StatusBar statusBar;
	protected MouseAdapter mouseTooltipHandler;
	protected String id;
	
	/**
	 * Constructor for FrameController.
	 */
	public FrameController( String id ) {
		this.id = id;		
		statusBar = new StatusBar( MainInterface.processor.getTaskManager() );
		
		mouseTooltipHandler = new TooltipMouseHandler( statusBar ); 		
	}

	public StatusBar getStatusBar() {
		return statusBar;
	}

	/**
	 * Returns the mouseTooltipHandler.
	 * @return MouseAdapter
	 */
	public MouseAdapter getMouseTooltipHandler() {
		return mouseTooltipHandler;
	}
	
	public void close()
	{
		ColumbaLogger.log.info("closing FrameController");
		
		MainInterface.frameModel.unregister(id);

		//getView().setVisible(false);	
	}

	abstract public FrameView getView();
}
