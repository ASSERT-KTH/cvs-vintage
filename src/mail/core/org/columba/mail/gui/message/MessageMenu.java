/*
 * Created on Jun 13, 2003
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package org.columba.mail.gui.message;

import org.columba.core.gui.frame.FrameMediator;
import org.columba.core.xml.XmlElement;
import org.columba.mail.gui.menu.MailContextMenu;


/**
 * @author frd
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class MessageMenu extends MailContextMenu {
    /**
 * @param frameMediator
 * @param path
 */
    public MessageMenu(FrameMediator frameController) {
        super(frameController, "org/columba/mail/action/message_contextmenu.xml");
    }

    public void extendMenuFromFile(String path) {
        menuGenerator.extendMenuFromFile(path);
        menuGenerator.createPopupMenu(this);
    }

    public void extendMenu(XmlElement menuExtension) {
        menuGenerator.extendMenu(menuExtension);
    }
}
