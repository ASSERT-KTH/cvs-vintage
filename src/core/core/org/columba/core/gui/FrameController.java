package org.columba.core.gui;

import java.awt.event.MouseAdapter;

import org.columba.main.MainInterface;
import org.columba.mail.gui.frame.TooltipMouseHandler;
import org.columba.core.gui.statusbar.StatusBar;

/**
 * @author Timo Stich (tstich@users.sourceforge.net)
 * 
 */
public abstract class FrameController {

	protected StatusBar statusBar;
	protected MouseAdapter mouseTooltipHandler;
	
	/**
	 * Constructor for FrameController.
	 */
	public FrameController() {		
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
	
	abstract public void close();	

}
