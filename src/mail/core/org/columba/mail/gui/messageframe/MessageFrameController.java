/*
 * Created on 06.04.2003
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package org.columba.mail.gui.messageframe;

import org.columba.core.config.ViewItem;
import org.columba.core.gui.frame.AbstractFrameView;
import org.columba.mail.gui.attachment.AttachmentController;
import org.columba.mail.gui.frame.MailFrameController;
import org.columba.mail.gui.message.MessageController;
import org.columba.mail.gui.table.FilterToolbar;
import org.columba.mail.gui.tree.util.FolderInfoPanel;

/**
 * @author frd
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class MessageFrameController extends MailFrameController {

	/**
	 * @param viewItem
	 */
	public MessageFrameController(ViewItem viewItem) {
		super(viewItem);
		
	}

	/* (non-Javadoc)
	 * @see org.columba.core.gui.frame.AbstractFrameController#createView()
	 */
	public AbstractFrameView createView() {
		MessageFrameView view = new MessageFrameView(this);

		view.setFolderInfoPanel(folderInfoPanel);

		view.init(
			
			messageController.getView(),
			statusBar);

		view.pack();
		
		view.setVisible(true);

		return view;
	}

	

}
