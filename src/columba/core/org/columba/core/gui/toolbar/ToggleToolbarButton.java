/*
 * Created on 08.09.2003
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package org.columba.core.gui.toolbar;

import java.awt.Insets;
import java.util.Observable;
import java.util.Observer;

import javax.swing.AbstractAction;
import javax.swing.Icon;
import javax.swing.JToggleButton;

import org.columba.core.action.BasicAction;
import org.columba.core.action.CheckBoxAction;
import org.columba.core.action.SelectionStateObservable;

/**
 * Customized JToogleButton for a Toolbar.
 * <p>
 * Adds an Observer to get notified when selection state changes
 * from action.
 * <p>
 * Focus is disabled for toolbar buttons. ToggleButton should use
 * small icons as default.
 * <p>
 *
 * @author fdietz
 */
public class ToggleToolbarButton extends JToggleButton implements Observer {

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
		((CheckBoxAction) getAction()).getObservable().addObserver(this);
		
	}

	public boolean isFocusTraversable() {
		return isRequestFocusEnabled();
	}

	/* (non-Javadoc)
	 * @see java.util.Observer#update(java.util.Observable, java.lang.Object)
	 */
	public void update(Observable obs, Object arg1) {
		SelectionStateObservable o = (SelectionStateObservable) obs;

		boolean selectionState = o.isSelected();
		setSelected(selectionState);

	}

}
