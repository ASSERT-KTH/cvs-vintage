/*
 * Created on 29.03.2003
 *
 * To change this generated comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package org.columba.mail.gui.menu;

import org.columba.core.gui.frame.FrameMediator;
import org.columba.core.gui.menu.ContextMenu;
import org.columba.core.gui.menu.PopupMenuGenerator;


/**
 * @author frd
 *
 * To change this generated comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class MailContextMenu extends ContextMenu {
    /**
 * @param frameMediator
 * @param path
 */
    public MailContextMenu(FrameMediator frameController, String path) {
        super(frameController, path);
    }

    /* (non-Javadoc)
 * @see org.columba.core.gui.menu.ContextMenu#createPopupMenuGeneratorInstance(java.lang.String, org.columba.core.gui.frame.FrameController)
 */
    public PopupMenuGenerator createPopupMenuGeneratorInstance(String xmlRoot,
        FrameMediator frameController) {
        if (menuGenerator == null) {
            menuGenerator = new MailPopupMenuGenerator(frameController, xmlRoot);
        }

        return menuGenerator;
    }
}
