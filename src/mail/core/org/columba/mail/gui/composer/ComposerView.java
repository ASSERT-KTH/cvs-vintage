//The contents of this file are subject to the Mozilla Public License Version 1.1
//(the "License"); you may not use this file except in compliance with the 
//License. You may obtain a copy of the License at http://www.mozilla.org/MPL/
//
//Software distributed under the License is distributed on an "AS IS" basis,
//WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License 
//for the specific language governing rights and
//limitations under the License.
//
//The Original Code is "The Columba Project"
//
//The Initial Developers of the Original Code are Frederik Dietz and Timo Stich.
//Portions created by Frederik Dietz and Timo Stich are Copyright (C) 2003. 
//
//All Rights Reserved.
package org.columba.mail.gui.composer;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import org.columba.core.gui.frame.AbstractFrameView;
import org.columba.core.gui.frame.FrameMediator;
import org.columba.core.gui.menu.Menu;
import org.columba.core.gui.toolbar.ToolBar;
import org.columba.core.gui.util.LabelWithMnemonic;

import org.columba.mail.gui.composer.html.HtmlToolbar;
import org.columba.mail.gui.composer.menu.ComposerMenu;
import org.columba.mail.gui.composer.util.IdentityInfoPanel;
import org.columba.mail.gui.view.AbstractComposerView;
import org.columba.mail.main.MailInterface;
import org.columba.mail.util.MailResourceLoader;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;


/**
 * @author frd
 *
 * view for message composer dialog
 */
public class ComposerView extends AbstractFrameView
    implements AbstractComposerView {

    private JSplitPane rightSplitPane;

    /** Editor viewer resides in this panel */
    private JPanel editorPanel;

    public ComposerView(FrameMediator ctrl) {
        super(ctrl);
        setTitle(MailResourceLoader.getString("dialog", "composer",
                "composerview_title")); //$NON-NLS-1$

        Container contentPane;

        contentPane = getContentPane();

        ComposerController controller = (ComposerController) frameController;

        if (isAccountInfoPanelVisible()) {
            toolbarPane.add(controller.getIdentityInfoPanel());
        }

        JScrollPane attachmentScrollPane = new JScrollPane(controller.getAttachmentController().view);

        //attachmentScrollPane.setBorder(null);
        attachmentScrollPane.setBorder(BorderFactory.createEmptyBorder(1, 1, 1,
                1));

        rightSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);

        //rightSplitPane.setBorder(BorderFactory.createEmptyBorder(0,0,0,0));
        JScrollPane hs = new JScrollPane(controller.getHeaderController()
                                                   .getView());
        hs.getViewport().setBackground(Color.white);
        hs.setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1));
        hs.setPreferredSize(new Dimension(200, 100));

        rightSplitPane.add(hs, JSplitPane.LEFT);

        rightSplitPane.add(attachmentScrollPane, JSplitPane.RIGHT);
        rightSplitPane.setDividerSize(5);
        rightSplitPane.setDividerLocation(400);

        JPanel topPanel = new JPanel();
        topPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 0, 0));

        LabelWithMnemonic subjectLabel = new LabelWithMnemonic(MailResourceLoader.getString(
                    "dialog", "composer", "subject"));

        LabelWithMnemonic smtpLabel = new LabelWithMnemonic(MailResourceLoader.getString(
                    "dialog", "composer", "identity"));

        LabelWithMnemonic priorityLabel = new LabelWithMnemonic(MailResourceLoader.getString(
                    "dialog", "composer", "priority"));

        // Create a FormLayout instance. 
        FormLayout layout = new FormLayout("max(20dlu;pref), 3dlu, fill:default:grow, 2dlu",
                
            // 2 columns
            "fill:default, 3dlu,fill:default, 3dlu, fill:default, 3dlu, fill:default, 3dlu");

        // 3 row
        PanelBuilder builder = new PanelBuilder(topPanel, layout);
        CellConstraints cc = new CellConstraints();

        layout.setColumnGroups(new int[][] {
                { 1 }
            });

        layout.setRowGroups(new int[][] {
                { 1, 5, 7 }
            });

        builder.add(smtpLabel, cc.xy(1, 1));

        JPanel smtpPanel = new JPanel();
        FormLayout l = new FormLayout("default, 3dlu, right:default:grow, 3dlu, right:default",
                "fill:default:grow");
        PanelBuilder b = new PanelBuilder(smtpPanel, l);

        CellConstraints c = new CellConstraints();
        b.add(controller.getAccountController().view, c.xy(1, 1));
        b.add(priorityLabel, c.xy(3, 1));
        b.add(controller.getPriorityController().view, c.xy(5, 1));

        builder.add(smtpPanel, cc.xy(3, 1));

        builder.add(rightSplitPane, cc.xywh(1, 3, 3, 1));

        builder.add(subjectLabel, cc.xy(1, 5));

        builder.add(controller.getSubjectController().view, cc.xy(3, 5));

        // add JPanel with useful HTML related actions.
        HtmlToolbar htmlToolbar = new HtmlToolbar(controller, builder);

        editorPanel = new JPanel();
        editorPanel.setBorder(null);
        editorPanel.setLayout(new BorderLayout());

        // *20030907, karlpeder* getViewUIComponent returns view
        //            already encapsulated in a scroll pane.
        //JScrollPane scrollPane =
        //	new JScrollPane(controller.getEditorController().view);
        //editorPanel.add(scrollPane, BorderLayout.CENTER);
        editorPanel.add(controller.getEditorController().getViewUIComponent());

        JPanel centerPanel = new JPanel();
        centerPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        centerPanel.setLayout(new BorderLayout());

        centerPanel.add(topPanel, BorderLayout.NORTH);
        centerPanel.add(editorPanel, BorderLayout.CENTER);

        /*
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout());

        mainPanel.add(rightSplitPane, BorderLayout.NORTH);
        mainPanel.add(centerPanel, BorderLayout.CENTER);
        */
        contentPane.add(centerPanel, BorderLayout.CENTER);

        pack();
    }

    /**
     * Returns a reference to the panel, that holds the editor view.
     * This is used by the ComposerController when adding a listener
     * to that panel.
     */
    public JPanel getEditorPanel() {
        return editorPanel;
    }

    /**
     * Used to update the panel, that holds the editor viewer. This is
     * necessary e.g. if the ComposerModel is changed to hold another
     * message type (text / html), which the previous editor can not
     * handle. If so a new editor controller is created, and thereby
     * a new view.
     */
    public void setNewEditorView() {
        // get reference to composer controller
        ComposerController controller = (ComposerController) frameController;

        // update panel
        editorPanel.removeAll();
        editorPanel.add(controller.getEditorController().getViewUIComponent());
        editorPanel.validate();
    }

    public void setRightDividerLocation(int i) {
        rightSplitPane.setDividerLocation(i);
    }

    public int getRightDividerLocation() {
        return rightSplitPane.getDividerLocation();
    }

    /* (non-Javadoc)
     * @see org.columba.core.gui.FrameView#createMenu(org.columba.core.gui.FrameController)
     */
    protected Menu createMenu(FrameMediator controller) {
        Menu menu = new ComposerMenu("org/columba/core/action/menu.xml",
                controller);
        menu.extendMenuFromFile("org/columba/mail/action/composer_menu.xml");

        return menu;
    }

    /* (non-Javadoc)
     * @see org.columba.core.gui.FrameView#createToolbar(org.columba.core.gui.FrameController)
     */
    protected ToolBar createToolbar(FrameMediator controller) {
        return new ToolBar(MailInterface.config.get("composer_toolbar")
                                               .getElement("toolbar"),
            controller);
    }

    public IdentityInfoPanel getAccountInfoPanel() {
        ComposerController controller = (ComposerController) frameController;

        return controller.getIdentityInfoPanel();
    }

    /* (non-Javadoc)
             * @see org.columba.core.gui.frame.AbstractFrameView#showToolbar()
             */
    public void showToolbar() {
        boolean b = isToolbarVisible();

        if (toolbar == null) {
            return;
        }

        if (b) {
            toolbarPane.remove(toolbar);
            ((FrameMediator) frameController).enableToolbar(MAIN_TOOLBAR,
                false);
        } else {
            if (isAccountInfoPanelVisible()) {
                toolbarPane.removeAll();
                toolbarPane.add(toolbar);
                toolbarPane.add(getAccountInfoPanel());
            } else {
                toolbarPane.add(toolbar);
            }

            ((FrameMediator) frameController).enableToolbar(MAIN_TOOLBAR,
                true);
        }

        validate();
        repaint();
    }

    public void showAccountInfoPanel() {
        boolean b = isAccountInfoPanelVisible();

        if (b) {
            toolbarPane.remove(getAccountInfoPanel());
            ((FrameMediator) frameController).enableToolbar(ACCOUNTINFOPANEL,
                false);
        } else {
            toolbarPane.add(getAccountInfoPanel());

            ((FrameMediator) frameController).enableToolbar(ACCOUNTINFOPANEL,
                true);
        }

        validate();
        repaint();
    }

    public boolean isAccountInfoPanelVisible() {
        return ((FrameMediator) frameController).isToolbarEnabled(ACCOUNTINFOPANEL);
    }
}
