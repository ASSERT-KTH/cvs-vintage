package org.columba.mail.gui.frame;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.AbstractButton;
import javax.swing.Action;

import org.columba.core.gui.statusbar.StatusBar;

/**
 * @author Timo Stich (tstich@users.sourceforge.net)
 * 
 */
public class TooltipMouseHandler extends MouseAdapter {

	private StatusBar statusBar;

	/**
	 * Constructor for MouseHandler.
	 */
	public TooltipMouseHandler(StatusBar statusBar) {
		super();

		this.statusBar = statusBar;
	}

	public void mouseEntered(MouseEvent evt) {
		if (evt.getSource() instanceof AbstractButton) {
			AbstractButton button = (AbstractButton) evt.getSource();
			Action action = button.getAction(); // getAction is new in JDK 1.3
			if (action != null) {
				String message =
					(String) action.getValue(Action.LONG_DESCRIPTION);
				statusBar.displayTooltipMessage(message);
			}
		}
	}

}
