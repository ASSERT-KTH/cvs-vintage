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
import org.columba.core.gui.util.MnemonicSetter;

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
		MnemonicSetter.setTextWithMnemonic(this, cbAction.getName());
		
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
