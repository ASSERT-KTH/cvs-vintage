/*
 * Created on 30.03.2003
 *
 * To change this generated comment go to 
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package org.columba.mail.gui.attachment;

import org.columba.core.gui.frame.FrameController;
import org.columba.mail.gui.menu.MailContextMenu;

/**
 * @author frd
 *
 * To change this generated comment go to 
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class AttachmentMenu extends MailContextMenu {

	/**
	 * @param frameController
	 * @param path
	 */
	public AttachmentMenu(FrameController frameController) {
		super(frameController, "org/columba/mail/action/attachment_contextmenu.xml");
		
	}

}
