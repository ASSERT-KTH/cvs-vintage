package org.columba.core.gui.util;

import java.awt.Component;
import java.awt.Graphics;

import javax.swing.Icon;

/**
 * @author freddy
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class EmptyIcon implements Icon {

	/**
	 * @see javax.swing.Icon#paintIcon(Component, Graphics, int, int)
	 */
	public void paintIcon(Component arg0, Graphics arg1, int arg2, int arg3) {
	}

	/**
	 * @see javax.swing.Icon#getIconWidth()
	 */
	public int getIconWidth() {
		return 16;
	}

	/**
	 * @see javax.swing.Icon#getIconHeight()
	 */
	public int getIconHeight() {
		return 16;
	}

}
