/*
 * Created on 23.03.2003
 *
 * To change this generated comment go to 
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package org.columba.mail.gui.action;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import org.columba.core.action.FrameAction;
import org.columba.core.gui.frame.AbstractFrameController;
import org.columba.core.gui.util.ImageLoader;
import org.columba.mail.gui.config.mailboximport.ImportWizard;

/**
 * @author frd
 *
 * To change this generated comment go to 
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class ImportAction extends FrameAction implements ActionListener {

	/**
	 * @param frameController
	 * @param name
	 * @param longDescription
	 * @param actionCommand
	 * @param small_icon
	 * @param big_icon
	 * @param mnemonic
	 * @param keyStroke
	 */
	public ImportAction(AbstractFrameController frameController) {
		super(
			frameController,
			"Import Mailbox...",
			"Import Mailbox...",
			"Import Mailbox...",
			"IMPORT",
			ImageLoader.getImageIcon("stock_convert-16.png"),
			null,
			0,
			null);

	}

	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent evt) {
		try {

			new ImportWizard();
			
		} catch (Exception ex) {
			ex.printStackTrace();
		}

	}

}
