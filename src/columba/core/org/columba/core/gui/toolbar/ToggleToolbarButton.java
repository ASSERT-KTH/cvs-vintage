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

package org.columba.core.gui.toolbar;

import java.awt.Insets;
import java.util.Observable;
import java.util.Observer;

import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JToggleButton;

import org.columba.core.action.FrameAction;
import org.columba.core.action.CheckBoxAction;
import org.columba.core.action.SelectionStateObservable;
import org.columba.core.gui.util.ImageUtil;

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
	public ToggleToolbarButton(Action action) {
		super(action);
		setRequestFocusEnabled(false);
		setMargin(new Insets(1, 1, 1, 1));

		// no text!
		setText("");

                ImageIcon icon = (ImageIcon)action.getValue(FrameAction.SMALL_ICON);
                if (icon != null) {
                        setIcon(icon);
                        // apply transparent icon
                        setDisabledIcon(ImageUtil.createTransparentIcon((ImageIcon)icon));
                }
		
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
