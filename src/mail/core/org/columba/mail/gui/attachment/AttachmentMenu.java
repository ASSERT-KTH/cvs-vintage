/*
 * Created on 30.03.2003
 *
 * To change this generated comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package org.columba.mail.gui.attachment;

import org.columba.core.gui.frame.FrameMediator;
import org.columba.mail.gui.menu.MailContextMenu;


/**
 * @author frd
 *
 * To change this generated comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class AttachmentMenu extends MailContextMenu {
    /**
 * @param frameMediator
 * @param path
 */
    public AttachmentMenu(FrameMediator frameMediator) {
        super(frameMediator,
            "org/columba/mail/action/attachment_contextmenu.xml");
    }
}
