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
package org.columba.mail.gui.attachment;

import org.columba.core.gui.frame.FrameMediator;
import org.columba.core.main.MainInterface;

import org.columba.mail.gui.attachment.action.OpenAction;
import org.columba.mail.gui.attachment.command.SaveAttachmentTemporaryCommand;

import org.columba.ristretto.message.MimeTree;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;

import javax.swing.JComponent;
import javax.swing.JPopupMenu;

import org.frappucino.swing.DynamicFileFactory;

/**
 * This class shows the attachmentlist
 *
 *
 * @version 1.0
 * @author Timo Stich
 */
public class AttachmentController {
    private static final Logger LOG = Logger.getLogger("org.columba.mail.gui.attachment");

    //private IconPanel attachmentPanel;
    //private int actIndex;
    //private Object actUid;
    //private TempFolder subMessageFolder;
    //private boolean inline;
    private AttachmentMenu menu;

    //private AttachmentActionListener actionListener;
    private AttachmentView view;
    private AttachmentModel model;
    private FrameMediator frameController;

    public AttachmentController(FrameMediator superController) {
        super();

        this.frameController = superController;

        model = new AttachmentModel();

        view = new AttachmentView(model);
        view.setDragEnabled(true);
        view.setTransferHandler(new AttachmentTransferHandler(new FileGenerator()));

        frameController.getSelectionManager().addSelectionHandler(new AttachmentSelectionHandler(
                view));

        getView().setDoubleClickAction(new OpenAction(superController));

        MouseListener popupListener = new PopupListener();
        getView().addMouseListener(popupListener);
    }

    public FrameMediator getFrameController() {
        return frameController;
    }

    public AttachmentView getView() {
        return view;
    }

    public AttachmentModel getModel() {
        return model;
    }

    public boolean setMimePartTree(MimeTree collection) {
        return getView().setMimePartTree(collection);
    }

    public void createPopupMenu() {
        menu = new AttachmentMenu(getFrameController());
    }

    private JPopupMenu getPopupMenu() {
    	if ( menu == null) createPopupMenu();
    	
        return menu;
    }

    class PopupListener extends MouseAdapter {
        public void mousePressed(MouseEvent e) {
            maybeShowPopup(e);
        }

        public void mouseReleased(MouseEvent e) {
            maybeShowPopup(e);
        }

        private void maybeShowPopup(MouseEvent e) {
            if (e.isPopupTrigger()) {
                /*if (getView().countSelected() <= 1) {
                    getView().select(e.getPoint(), 0);
                }*/

                if (getView().countSelected() >= 1) {
                    getPopupMenu().show(e.getComponent(), e.getX(), e.getY());
                }
            }
        }
    }

    private class FileGenerator implements DynamicFileFactory {

        /** {@inheritDoc} */
        public File[] createFiles(JComponent arg0) throws IOException {
            File[] files = new File[1];
            SaveAttachmentTemporaryCommand command = new SaveAttachmentTemporaryCommand(
                    getFrameController().getSelectionManager()
                    .getHandler("mail.attachment").getSelection());
            LOG.fine("Waiting for command to complete.");
            MainInterface.processor.addOp(command);

            command.waitForCommandToComplete();

            files[0] = command.getTempAttachmentFile();
            LOG.fine("Temporary attachment created.");
            return files;
        }
    }
}
