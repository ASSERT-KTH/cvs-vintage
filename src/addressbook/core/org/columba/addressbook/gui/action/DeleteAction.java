/*
 * Created on 26.03.2003
 *
 * To change this generated comment go to 
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package org.columba.addressbook.gui.action;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.KeyStroke;

import org.columba.addressbook.util.AddressbookResourceLoader;
import org.columba.core.action.FrameAction;
import org.columba.core.gui.frame.AbstractFrameController;
import org.columba.core.gui.util.ImageLoader;

/**
 * @author frd
 *
 * To change this generated comment go to 
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class DeleteAction extends FrameAction {

	public DeleteAction(AbstractFrameController frameController) {
		super(
				frameController,
				AddressbookResourceLoader.getString(
					"menu", "mainframe", "menu_edit_delete"));
		
		// tooltip text
		setTooltipText(
				AddressbookResourceLoader.getString(
					"menu", "mainframe", "menu_edit_delete"));
					
		// action command
		setActionCommand("DELETE");
		
		// icons
		setSmallIcon(ImageLoader.getSmallImageIcon("stock_paste-16.png"));
		setLargeIcon(ImageLoader.getImageIcon("stock_paste.png"));
		
		// TODO: Use & to define mnemonic
		setMnemonic('D');
		
		// shortcut key
		setAcceleratorKey(
				KeyStroke.getKeyStroke(
					KeyEvent.VK_D, ActionEvent.CTRL_MASK));
		
	}

	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent evt) {
		// TODO implement action
		super.actionPerformed(evt);
	}

}
