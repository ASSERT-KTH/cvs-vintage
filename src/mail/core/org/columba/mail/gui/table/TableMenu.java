/*
 * Created on 12.03.2003
 *
 * To change this generated comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package org.columba.mail.gui.table;

import org.columba.core.gui.frame.FrameMediator;
import org.columba.core.xml.XmlElement;
import org.columba.mail.gui.menu.MailContextMenu;


/**
 * @author frd
 *
 * To change this generated comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class TableMenu extends MailContextMenu {
    //protected PopupMenuGenerator menuGenerator;
    //protected FrameController frameMediator;

    /**
 *
 */
    public TableMenu(FrameMediator frameController) {
        super(frameController, "org/columba/mail/action/table_contextmenu.xml");

        /*
this.frameMediator = frameMediator;

menuGenerator =
        new PopupMenuGenerator(
                frameMediator,
                "org/columba/mail/action/table_contextmenu.xml");
menuGenerator.createPopupMenu(this);
*/
    }

    public void extendMenuFromFile(String path) {
        menuGenerator.extendMenuFromFile(path);
        menuGenerator.createPopupMenu(this);
    }

    public void extendMenu(XmlElement menuExtension) {
        menuGenerator.extendMenu(menuExtension);
    }
}
