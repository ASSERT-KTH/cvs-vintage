/*
 * Created on 14.09.2003
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package org.columba.core.gui.menu;

import java.util.Observable;
import java.util.Observer;

import javax.swing.Action;
import javax.swing.JCheckBoxMenuItem;

import org.columba.core.action.CheckBoxAction;
import org.columba.core.action.SelectionStateObservable;

/**
 * Adds an Observer to JCheckBoxMenuItem in order to make it possible
 * to receive selection state changes from its underlying action.
 *
 * @author fdietz
 */
public class CCheckBoxMenuItem extends JCheckBoxMenuItem implements Observer{

	/**
	 * default constructor
	 */
	public CCheckBoxMenuItem() {
		super();
	}

	

	/**
	 * Creates a checkbox menu item with a given action attached.
	 * <br>
	 * If the name of the action contains &, the next character is used as
	 * mnemonic. If not, the fall-back solution is to use default behaviour,
	 * i.e. the mnemonic defined using setMnemonic on the action.
	 *  
	 * @param action	The action to attach to the menu item
	 */
	/**
	 * @param arg0
	 */
	public CCheckBoxMenuItem(Action action) {
		super(action);
		
		CheckBoxAction cbAction = (CheckBoxAction) getAction();

		// register as observer on the action
		cbAction.getObservable().addObserver(this);
		
		// Set text, possibly with a mnemonic if defined using &
		setTextInclMnemonic(cbAction.getName());
		
	}
	

	/* (non-Javadoc)
	 * @see java.util.Observer#update(java.util.Observable, java.lang.Object)
	 */
	public void update(Observable obs, Object arg1) {
		SelectionStateObservable o = (SelectionStateObservable) obs;
		
		boolean selectionState = o.isSelected();
		setSelected(selectionState);
	}

	/**
	 * Private helper to set text of menu item incl. taking care of 
	 * setting the rigth mnemonic if specified using an & character in
	 * the menu text
	 * 
	 * @param	text	Text of menu item, possibly incl. & for mnemonic spec.
	 */
	private void setTextInclMnemonic(String text) {

		// search for mnemonic
		int index = text.indexOf("&");
		if ((index != -1) && ((index + 1) < text.length())) {
			// mnemonic found
			// ...and not at the end of the string (which doesn't make sence) 

			char mnemonic = text.charAt(index + 1);

			StringBuffer buf = new StringBuffer();

			// if mnemonic is first character of this string
			if (index == 0)
				buf.append(text.substring(1));
			else {
				buf.append(text.substring(0, index));
				buf.append(text.substring(index + 1));
			}

			// set display text
			this.setText(buf.toString());

			// set mnemonic
			this.setMnemonic(mnemonic);
			this.setDisplayedMnemonicIndex(index);

		} else {
			// no mnemonic found - just set the text on the menu item
			this.setText(text);
		}
		
	}

}
