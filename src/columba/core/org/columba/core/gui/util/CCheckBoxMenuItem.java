package org.columba.core.gui.util;

import javax.swing.ImageIcon;
import javax.swing.JCheckBoxMenuItem;

import org.columba.core.action.BasicAction;
import org.columba.core.action.DefaultCheckBoxAction;

/**
 * @author frd
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class CCheckBoxMenuItem extends JCheckBoxMenuItem {

	/**
	 * Constructor for CCheckBoxMenuItem.
	 */
	public CCheckBoxMenuItem() {
		super();
	}

	/**
	 * Constructor for CCheckBoxMenuItem.
	 * @param a
	 */
	public CCheckBoxMenuItem(BasicAction a) {
		super(a);
	}

	/**
	 * Constructor for CCheckBoxMenuItem.
	 * @param a
	 */
	public CCheckBoxMenuItem(DefaultCheckBoxAction a) {
		super(a);
		
		a.setCheckBoxMenuItem(this);
	}

	/**
	 * Constructor for CCheckBoxMenuItem.
	 * @param s
	 */
	public CCheckBoxMenuItem(String s) {
		super(s);
	}

	/**
	 * Constructor for CCheckBoxMenuItem.
	 * @param s
	 * @param icon
	 */
	public CCheckBoxMenuItem(String s, ImageIcon icon) {
		super(s, icon);
	}

	

}
