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
package org.columba.mail.gui.mimetype;

import java.io.File;
import java.net.URL;

import org.columba.core.util.OSInfo;
import org.columba.ristretto.message.MimeHeader;


public class MimeTypeViewer {
    public MimeTypeViewer() {
    }

    public Process openWith(MimeHeader header, File tempFile, boolean blocking) {
        AbstractViewer viewer;

        if (OSInfo.isWin32Platform()) {
            viewer = new WindowsViewer();
        } else {
            viewer = new ColumbaViewer();
        }

        return viewer.openWith(header, tempFile, blocking);
    }

    public Process open(MimeHeader header, File tempFile, boolean blocking) {
        AbstractViewer viewer;

        if (OSInfo.isWin32Platform()) {
            viewer = new WindowsViewer();
        } else {
            viewer = new ColumbaViewer();
        }

        return viewer.open(header, tempFile, blocking);
    }

    public Process openURL(URL url) {
        AbstractViewer viewer;

        if (OSInfo.isWin32Platform()) {
            viewer = new WindowsViewer();
        } else {
            viewer = new ColumbaViewer();
        }

        return viewer.openURL(url);
    }

    public Process openWithURL(URL url) {
        AbstractViewer viewer;

        if (OSInfo.isWin32Platform()) {
            viewer = new WindowsViewer();
        } else {
            viewer = new ColumbaViewer();
        }

        return viewer.openWithURL(url);
    }

    
}
