/*
 * Created on 08.09.2003
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package org.columba.core.gui.toolbar;

import java.awt.Insets;

import javax.swing.AbstractAction;
import javax.swing.Icon;
import javax.swing.JToggleButton;

import org.columba.core.action.BasicAction;

/**
 * @author frd
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class ToggleToolbarButton extends JToggleButton {

	/**
	 * 
	 */
	public ToggleToolbarButton() {
		super();
		setRequestFocusEnabled(false);
	}

	/**
	 * @param icon
	 */
	public ToggleToolbarButton(Icon icon) {
		super(icon);
		setRequestFocusEnabled(false);
	}

	/**
	 * @param action
	 */
	public ToggleToolbarButton(AbstractAction action) {
		super(action);
		setRequestFocusEnabled(false);
		setMargin(new Insets(1, 1, 1, 1));
		
		// no text!
		setText("");
		
		setIcon(((BasicAction) action).getSmallIcon());
		
		//setToolTipText(((BasicAction) action).getTooltipText());
	}

	public boolean isFocusTraversable() {
		return isRequestFocusEnabled();
	}

}
