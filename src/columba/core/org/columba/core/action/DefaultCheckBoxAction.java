package org.columba.core.action;

import javax.swing.ImageIcon;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.KeyStroke;

import org.columba.core.gui.FrameController;

/**
 * @author frd
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class DefaultCheckBoxAction extends DefaultAction {


	JCheckBoxMenuItem checkBoxMenuItem;
	
	/**
	 * Constructor for DefaultCheckBoxAction.
	 * @param frameController
	 * @param name
	 * @param longDescription
	 * @param actionCommand
	 * @param small_icon
	 * @param big_icon
	 * @param mnemonic
	 * @param keyStroke
	 */
	public DefaultCheckBoxAction(
		FrameController frameController,
		String name,
		String longDescription,
		String actionCommand,
		ImageIcon small_icon,
		ImageIcon big_icon,
		int mnemonic,
		KeyStroke keyStroke) {
		super(
			frameController,
			name,
			longDescription,
			actionCommand,
			small_icon,
			big_icon,
			mnemonic,
			keyStroke);
	}

	/**
	 * Constructor for DefaultCheckBoxAction.
	 * @param frameController
	 * @param name
	 * @param longDescription
	 * @param actionCommand
	 * @param small_icon
	 * @param big_icon
	 * @param mnemonic
	 * @param keyStroke
	 * @param showToolbarText
	 */
	public DefaultCheckBoxAction(
		FrameController frameController,
		String name,
		String longDescription,
		String actionCommand,
		ImageIcon small_icon,
		ImageIcon big_icon,
		int mnemonic,
		KeyStroke keyStroke,
		boolean showToolbarText) {
		super(
			frameController,
			name,
			longDescription,
			actionCommand,
			small_icon,
			big_icon,
			mnemonic,
			keyStroke,
			showToolbarText);
	}

	/**
	 * Constructor for DefaultCheckBoxAction.
	 * @param frameController
	 * @param name
	 * @param tname
	 * @param longDescription
	 * @param actionCommand
	 * @param small_icon
	 * @param big_icon
	 * @param mnemonic
	 * @param keyStroke
	 */
	public DefaultCheckBoxAction(
		FrameController frameController,
		String name,
		String tname,
		String longDescription,
		String actionCommand,
		ImageIcon small_icon,
		ImageIcon big_icon,
		int mnemonic,
		KeyStroke keyStroke) {
		super(
			frameController,
			name,
			tname,
			longDescription,
			actionCommand,
			small_icon,
			big_icon,
			mnemonic,
			keyStroke);
	}

	/**
	 * Constructor for DefaultCheckBoxAction.
	 * @param frameController
	 * @param name
	 * @param tname
	 * @param longDescription
	 * @param actionCommand
	 * @param small_icon
	 * @param big_icon
	 * @param mnemonic
	 * @param keyStroke
	 * @param showToolbarText
	 */
	public DefaultCheckBoxAction(
		FrameController frameController,
		String name,
		String tname,
		String longDescription,
		String actionCommand,
		ImageIcon small_icon,
		ImageIcon big_icon,
		int mnemonic,
		KeyStroke keyStroke,
		boolean showToolbarText) {
		super(
			frameController,
			name,
			tname,
			longDescription,
			actionCommand,
			small_icon,
			big_icon,
			mnemonic,
			keyStroke,
			showToolbarText);
	}
	
	
	/**
		 * Returns the checkBoxMenuItem.
		 * @return JCheckBoxMenuItem
		 */
		public JCheckBoxMenuItem getCheckBoxMenuItem() {
			return checkBoxMenuItem;
		}

		/**
		 * Sets the checkBoxMenuItem.
		 * @param checkBoxMenuItem The checkBoxMenuItem to set
		 */
		public void setCheckBoxMenuItem(JCheckBoxMenuItem checkBoxMenuItem) {
			this.checkBoxMenuItem = checkBoxMenuItem;
		}

}
