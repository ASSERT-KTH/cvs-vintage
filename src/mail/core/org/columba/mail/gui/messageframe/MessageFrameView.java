/*
 * Created on 06.04.2003
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package org.columba.mail.gui.messageframe;

import java.awt.BorderLayout;

import org.columba.core.config.ViewItem;
import org.columba.core.gui.frame.AbstractFrameView;
import org.columba.core.gui.frame.FrameMediator;
import org.columba.core.gui.menu.Menu;
import org.columba.core.gui.statusbar.StatusBar;
import org.columba.core.gui.toolbar.ToolBar;
import org.columba.mail.gui.infopanel.FolderInfoPanel;
import org.columba.mail.gui.menu.MailMenu;
import org.columba.mail.gui.message.MessageView;
import org.columba.mail.gui.view.AbstractMessageFrameView;
import org.columba.mail.main.MailInterface;


/**
 * @author frd
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class MessageFrameView extends AbstractFrameView
    implements AbstractMessageFrameView {
    private FolderInfoPanel folderInfoPanel;

    /**
     * @param frameMediator
     */
    public MessageFrameView(FrameMediator frameController) {
        super(frameController);
    }

    /* (non-Javadoc)
     * @see org.columba.core.gui.frame.AbstractFrameView#createMenu(org.columba.core.gui.frame.FrameMediator)
     */
    protected Menu createMenu(FrameMediator controller) {
        Menu menu = new MailMenu("org/columba/core/action/menu.xml",
                "org/columba/mail/action/messageframe_menu.xml", controller);

        return menu;
    }

    /* (non-Javadoc)
     * @see org.columba.core.gui.frame.AbstractFrameView#createToolbar(org.columba.core.gui.frame.FrameMediator)
     */
    protected ToolBar createToolbar(FrameMediator controller) {
        return new ToolBar(MailInterface.config.get("messageframe_toolbar")
                                               .getElement("toolbar"),
            controller);
    }

    public void init(MessageView message, StatusBar statusBar) {
        getContentPane().add(message, BorderLayout.CENTER);

//        ViewItem viewItem = getFrameController().getViewItem();
        ViewItem viewItem = getViewController().getViewItem();

        if (viewItem.getBoolean("toolbars", "show_folderinfo") == true) {
            toolbarPane.add(folderInfoPanel);
        }
    }

    /**
     * @return
     */
    public FolderInfoPanel getFolderInfoPanel() {
        return folderInfoPanel;
    }

    /**
     * @param panel
     */
    public void setFolderInfoPanel(FolderInfoPanel panel) {
        folderInfoPanel = panel;
    }
}
