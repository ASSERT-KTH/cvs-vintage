package org.columba.core.gui.util;

import javax.swing.Icon;
import javax.swing.JToggleButton;

/**
 * @author freddy
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class ToolbarToggleButton extends JToggleButton{

	public ToolbarToggleButton()
	{
		setRequestFocusEnabled(false);
	}
	
	public ToolbarToggleButton( Icon icon )
	{
		super(icon);
		setRequestFocusEnabled(false);
	}
	
	public boolean isFocusTraversable() {
		return isRequestFocusEnabled();
	}
}
