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

import org.columba.core.gui.focus.FocusOwner;
import org.columba.core.main.MainInterface;

import org.columba.mail.util.MailResourceLoader;

import org.columba.ristretto.message.LocalMimePart;
import org.columba.ristretto.message.MimeHeader;
import org.columba.ristretto.message.StreamableMimePart;
import org.columba.ristretto.message.io.FileSource;

import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import java.io.File;
import java.io.IOException;

import java.net.FileNameMap;
import java.net.URLConnection;

import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

/**
 * Controller for the attachment view.
 *
 * @author frdietz
 * @author redsolo
 */
public class AttachmentController implements KeyListener, FocusOwner, ListSelectionListener {

    AttachmentView view;

    private ComposerController composerController;

    private AttachmentActionListener actionListener;

    private AttachmentMenu menu;

    private JFileChooser fileChooser;

    /**
     * Creates the attachment controller.
     * @param controller the main composer controller.
     */
    public AttachmentController(ComposerController controller) {
        this.composerController = controller;

        view = new AttachmentView(this);

        actionListener = new AttachmentActionListener(this);

        menu = new AttachmentMenu(this);

        view.addPopupListener(new PopupListener());

        // register Component as FocusOwner
        MainInterface.focusManager.registerComponent(this);

        fileChooser = new JFileChooser();

        installListener();

        view.addListSelectionListener(this);
    }

    /**
     * Returns the action listener for this attachment controller.
     * @return the action listener for this attachment controller.
     */
    public ActionListener getActionListener() {
        return actionListener;
    }

    /**
     * Installs this object as a listener to the view.
     */
    public void installListener() {
        view.installListener(this);
    }

    /**
     * Synchronizes model and view.
     *
     * @param b If true, model data is transferred to the view. If false, view data
     *            is saved in the model.
     */
    public void updateComponents(boolean b) {
        if (b) {
            // transfer attachments from model to view

            /*
             * clear existing attachments from the view *20031105, karlpeder* Added
             * to avoid dupplicating attachments when switching btw. html and plain
             * text.
             */
            view.clear();

            // add attachments (mimeparts) from model to the view
            for (int i = 0; i < composerController.getModel().getAttachments().size(); i++) {
                StreamableMimePart p = (StreamableMimePart) composerController.getModel().getAttachments().get(i);
                view.add(p);
            }
        } else {
            // transfer attachments from view to model
            // clear existing attachments from the model
            composerController.getModel().getAttachments().clear();

            // add attachments (mimeparts) from view to the model
            for (int i = 0; i < view.count(); i++) {
                StreamableMimePart mp = (StreamableMimePart) view.get(i);
                composerController.getModel().getAttachments().add(mp);
            }
        }
    }

    /**
     * Add the mime part as an attachment to the email.
     * @param part mime part.
     */
    private void add(StreamableMimePart part) {
        view.add(part);
        ((ComposerModel) composerController.getModel()).getAttachments().add(part);
    }

    /**
     * Removes the current selected attachments.
     */
    public void removeSelected() {
        Object[] mp = view.getSelectedValues();

        for (int i = 0; i < mp.length; i++) {
            view.remove((StreamableMimePart) mp[i]);
        }
    }

    /**
     * Opens up a file chooser and lets the user select the files to import.
     */
    public void addFileAttachment() {
        int returnValue;
        File[] files;

        fileChooser.setDialogTitle(MailResourceLoader.getString("menu", "composer", "menu_message_attachFile")); //$NON-NLS-1$
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        fileChooser.setMultiSelectionEnabled(true);
        returnValue = fileChooser.showOpenDialog(view);

        if (returnValue == JFileChooser.APPROVE_OPTION) {
            files = fileChooser.getSelectedFiles();

            for (int i = 0; i < files.length; i++) {
                addFileAttachment(files[i]);
            }
        }
    }

    /**
     * Attaches a file to the email as an attachment.
     * @param file the file to attach to the email.
     */
    public void addFileAttachment(File file) {
        FileNameMap fileNameMap = URLConnection.getFileNameMap();
        String mimetype = fileNameMap.getContentTypeFor(file.getName());

        if (mimetype == null) {
            mimetype = "application/octet-stream"; //REALLY NEEDED?
        }

        MimeHeader header = new MimeHeader(mimetype.substring(0, mimetype.indexOf('/')), mimetype.substring(mimetype.indexOf('/') + 1));

        header.putContentParameter("name", file.getName());
        header.setContentDisposition("attachment");
        header.putDispositionParameter("filename", file.getName());
        header.setContentTransferEncoding("base64");

        try {
            LocalMimePart mimePart = new LocalMimePart(header, new FileSource(file));

            view.add(mimePart);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /** ***************** KeyListener *************************** */

    /** {@inheritDoc} */
    public void keyPressed(KeyEvent k) {
        switch (k.getKeyCode()) {
        case (KeyEvent.VK_DELETE):
            delete();

            break;
        }
    }

    /** {@inheritDoc} */
    public void keyReleased(KeyEvent k) {
    }

    /** {@inheritDoc} */
    public void keyTyped(KeyEvent k) {
    }

    /** ******************** FocusOwner implementation ****************** */

    /** {@inheritDoc} */
    public void copy() {
        // attachment controller doesn't support copy-operation
    }

    /** {@inheritDoc} */
    public void cut() {
        if (view.count() > 0) {
            removeSelected();
        }
    }

    /** {@inheritDoc} */
    public void delete() {
        if (view.count() > 0) {
            removeSelected();
        }
    }

    /** {@inheritDoc} */
    public JComponent getComponent() {
        return view;
    }

    /** {@inheritDoc} */
    public boolean isCopyActionEnabled() {
        // attachment controller doesn't support copy actions
        return false;
    }

    /** {@inheritDoc} */
    public boolean isCutActionEnabled() {
        return (view.getSelectedValues().length > 0);
    }

    /** {@inheritDoc} */
    public boolean isDeleteActionEnabled() {
        return (view.getSelectedValues().length > 0);
    }

    /** {@inheritDoc} */
    public boolean isPasteActionEnabled() {
        // attachment controller doesn't support paste actions
        return false;
    }

    /** {@inheritDoc} */
    public boolean isSelectAllActionEnabled() {
        return (view.count() > 0);
    }

    /** {@inheritDoc} */
    public void paste() {
        // attachment controller doesn't support paste actions
    }

    /** {@inheritDoc} */
    public boolean isRedoActionEnabled() {
        // attachment controller doesn't support redo operation
        return false;
    }

    /** {@inheritDoc} */
    public boolean isUndoActionEnabled() {
        // attachment controller doesn't support undo operation
        return false;
    }

    /** {@inheritDoc} */
    public void redo() {
        // attachment controller doesn't support redo operation
    }

    /** {@inheritDoc} */
    public void selectAll() {
        view.setSelectionInterval(0, view.count() - 1);
    }

    /** {@inheritDoc} */
    public void undo() {
        // attachment controller doesn't support undo operation
    }

    /** ******************* ListSelectionListener interface ********************** */

    /** {@inheritDoc} */
    public void valueChanged(ListSelectionEvent arg0) {
        MainInterface.focusManager.updateActions();
    }

    /** ******************** MouseListener **************************** */
    class PopupListener extends MouseAdapter {

        /** {@inheritDoc} */
        public void mousePressed(MouseEvent e) {
            maybeShowPopup(e);
        }

        /** {@inheritDoc} */
        public void mouseReleased(MouseEvent e) {
            maybeShowPopup(e);
        }

        /**
         * Shows the popup menu.
         * @param e the mouse event used to get the selected attachment.
         */
        private void maybeShowPopup(MouseEvent e) {
            if (e.isPopupTrigger()) {
                Object[] values = view.getSelectedValues();

                if (values.length == 0) {
                    view.fixSelection(e.getX(), e.getY());
                }

                menu.show(e.getComponent(), e.getX(), e.getY());
            }
        }
    }
}
