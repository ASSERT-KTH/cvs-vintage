// The contents of this file are subject to the Mozilla Public License Version
// 1.1
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
//The Initial Developers of the Original Code are Frederik Dietz and Timo
// Stich.
//Portions created by Frederik Dietz and Timo Stich are Copyright (C) 2003.
//
//All Rights Reserved.
package org.columba.mail.gui.mimetype;

import org.columba.core.mimetype.MimeRouter;

import org.columba.ristretto.message.MimeHeader;

import java.io.File;
import java.io.IOException;

import java.net.URL;

import javax.swing.JOptionPane;

public class ColumbaViewer extends AbstractViewer {

    public Process openWith(MimeHeader header, File tempFile) {
        boolean save = false;
        ChooseViewerDialog viewerDialog = new ChooseViewerDialog(header
                .getMimeType().getType(), header.getMimeType().getSubtype(),
                save);
        String viewer = viewerDialog.getViewer();

        if (viewer == null) { return null; }

        save = viewerDialog.saveViewer();

        if (save) {
            MimeRouter.getInstance().setViewer(header, viewer);
        }

        //System.out.println("tempfile: "+tempFile);
        String cmd = new String(viewer + " " + tempFile.toString());

        //System.out.println("cmd: "+cmd);
        Process child = null;

        try {
            child = Runtime.getRuntime().exec(cmd);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "Error: " + e.getMessage());
        }

        return child;
    }

    public Process open(MimeHeader header, File tempFile) {
        String viewer = MimeRouter.getInstance().getViewer(header);

        File viewerFile = new File(viewer);

        // if viewer is not yet specified
        // or if viewer app is not found
        if ((viewer == null) || (!viewerFile.exists())) {
            boolean save = false;
            ChooseViewerDialog viewerDialog = new ChooseViewerDialog(header
                    .getMimeType().getType(),
                    header.getMimeType().getSubtype(), save);
            viewer = viewerDialog.getViewer();

            if (viewer == null) { return null; }

            save = viewerDialog.saveViewer();

            if (save) {
                MimeRouter.getInstance().setViewer(header, viewer);
            }
        }

        Process child = null;

        try {
            child = Runtime.getRuntime().exec(
                    viewer + " " + tempFile.toString());
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "Error: " + e.getMessage());
        }

        return child;
    }

    public Process openURL(URL url) {
        boolean save = false;
        File viewerFile = null;
        String viewer = MimeRouter.getInstance().getViewer("text", "html");
        if (viewer != null) {
        	viewerFile = new File(viewer);
        }

        // if viewer is not yet specified
        // or if viewer app is not found
        if ((viewer == null) || (!viewerFile.exists())) {
            ChooseViewerDialog viewerDialog = new ChooseViewerDialog("text",
                    "html", save);

            viewer = viewerDialog.getViewer();

            if (viewer == null) { return null; }

            save = viewerDialog.saveViewer();

            if (save) {
                MimeRouter.getInstance().setViewer("text", "html", viewer);
            }
        }

        Process child = null;

        try {
            child = Runtime.getRuntime().exec(viewer + " " + url);
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error: " + ex.getMessage());
        }

        return child;
    }

    public Process openWithURL(URL url) {
        boolean save = false;
        String viewer;
        ChooseViewerDialog viewerDialog = new ChooseViewerDialog("text",
                "html", save);

        viewer = viewerDialog.getViewer();

        if (viewer == null) { return null; }

        save = viewerDialog.saveViewer();

        if (save) {
            MimeRouter.getInstance().setViewer("text", "html", viewer);
        }

        Process child = null;

        try {
            child = Runtime.getRuntime().exec(viewer + " " + url);
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error: " + ex.getMessage());
        }

        return child;
    }
}