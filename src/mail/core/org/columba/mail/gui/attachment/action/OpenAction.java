/*
 * Created on 30.03.2003
 *
 * To change this generated comment go to 
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package org.columba.mail.gui.attachment.action;

import java.awt.event.ActionEvent;

import org.columba.core.action.FrameAction;
import org.columba.core.gui.frame.FrameController;
import org.columba.core.gui.selection.SelectionChangedEvent;
import org.columba.core.gui.selection.SelectionListener;
import org.columba.core.gui.util.ImageLoader;
import org.columba.core.main.MainInterface;
import org.columba.mail.gui.attachment.AttachmentSelectionChangedEvent;
import org.columba.mail.gui.attachment.command.OpenAttachmentCommand;
import org.columba.mail.util.MailResourceLoader;

/**
 * @author frd
 *
 * To change this generated comment go to 
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class OpenAction extends FrameAction implements SelectionListener {

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
	public OpenAction(FrameController frameController) {
		super(frameController, MailResourceLoader.getString("menu", "mainframe", "attachmentopen"), //$NON-NLS-1$
		MailResourceLoader.getString("menu", "mainframe", "attachmentopen_tooltip"), //$NON-NLS-1$

		"OPEN", //$NON-NLS-1$
		ImageLoader.getSmallImageIcon("stock_open.png"), //$NON-NLS-1$
		ImageLoader.getImageIcon("stock_open.png"), //$NON-NLS-1$
		0, null);
		
		frameController.getSelectionManager().registerSelectionListener("mail.attachment", this);
	}

	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent evt) {
		MainInterface.processor.addOp(
			new OpenAttachmentCommand(
				getFrameController()
					.getSelectionManager()
					.getHandler("mail.attachment")
					.getSelection()));
	}

	/* (non-Javadoc)
	 * @see org.columba.mail.gui.attachment.AttachmentSelectionListener#attachmentSelectionChanged(java.lang.Integer[])
	 */
	public void selectionChanged( SelectionChangedEvent e) {
		if( ((AttachmentSelectionChangedEvent)e).getAddress() != null ) {
			setEnabled( true );
		} else {
			setEnabled( false );
		}
	}

}
