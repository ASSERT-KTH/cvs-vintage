/*
 * Created on 11.03.2003
 *
 * To change this generated comment go to 
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package org.columba.mail.gui.table.action;

import java.awt.event.ActionEvent;

import org.columba.core.action.FrameAction;
import org.columba.core.charset.CharsetOwnerInterface;
import org.columba.core.gui.frame.AbstractFrameController;
import org.columba.core.gui.selection.SelectionChangedEvent;
import org.columba.core.gui.selection.SelectionListener;
import org.columba.core.gui.util.ImageLoader;
import org.columba.core.logging.ColumbaLogger;
import org.columba.core.main.MainInterface;
import org.columba.mail.command.FolderCommandReference;
import org.columba.mail.folder.command.SaveMessageAsCommand;
import org.columba.mail.gui.frame.AbstractMailFrameController;
import org.columba.mail.gui.table.selection.TableSelectionChangedEvent;
import org.columba.mail.util.MailResourceLoader;

/**
 * @author frd
 *
 * To change this generated comment go to 
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class SaveMessageAsAction
	extends FrameAction
	implements SelectionListener {

	/* Parameters for the super constructor used
	 * 		@param frameController
	 * 		@param name
	 * 		@param longDescription
	 * 		@param actionCommand
	 * 		@param small_icon
	 * 		@param big_icon
	 * 		@param mnemonic
	 * 		@param keyStroke
	 */
	public SaveMessageAsAction(AbstractFrameController frameController) {
		super(
			frameController,
			MailResourceLoader.getString(
				"menu",
				"mainframe",
				"menu_message_save"),
			MailResourceLoader.getString(
				"menu",
				"mainframe",
				"menu_message_save_tooltip"),
			"SAVE",
			ImageLoader.getSmallImageIcon("stock_save_as-16.png"),
			ImageLoader.getImageIcon("stock_save.png"),
			'0',
			null);
		setEnabled(false);
		((AbstractMailFrameController) frameController).registerTableSelectionListener(
			this);
	}

	/**
	 * Called for activation of the SaveMessageAsAction
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent evt) {
		/*
		 * *20030611, karlpeder* Added content to method
		 * so command is activated
		 */
		ColumbaLogger.log.debug("Save Message As... activated");

		// get selected stuff
		FolderCommandReference[] r =
				((AbstractMailFrameController) getFrameController()).
					getTableSelection();

		// get active charset - necessary to decode msg for saving
		String charset = ((CharsetOwnerInterface) getFrameController())
							.getCharsetManager()
							.getSelectedCharset();

		// add command for execution
		SaveMessageAsCommand c = new SaveMessageAsCommand(r, charset);
		MainInterface.processor.addOp(c);

	}
	
	
	/**
	 * Ensures that the action is only enabled when at least 
	 * one message is selected in the GUI.
	 * @see org.columba.core.gui.util.SelectionListener#selectionChanged(org.columba.core.gui.util.SelectionChangedEvent)
	 */
	public void selectionChanged(SelectionChangedEvent e) {

		if (((TableSelectionChangedEvent) e).getUids().length > 0)
			setEnabled(true);
		else
			setEnabled(false);

	}
}
