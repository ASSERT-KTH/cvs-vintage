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
	 * 
	 */
	public CCheckBoxMenuItem() {
		super();
	}

	

	/**
	 * @param arg0
	 */
	public CCheckBoxMenuItem(Action arg0) {
		super(arg0);
		
		((CheckBoxAction) getAction()).getObservable().addObserver(this);
		
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
