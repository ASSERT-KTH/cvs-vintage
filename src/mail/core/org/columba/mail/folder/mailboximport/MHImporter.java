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

package org.columba.mail.folder.mailboximport;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

import org.columba.core.command.WorkerStatusController;
import org.columba.mail.folder.MessageFolder;
import org.columba.mail.folder.mh.MHMessageFileFilter;

/**
 * @author frd
 *
 * To change this generated comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class MHImporter extends AbstractMailboxImporter {
    public MHImporter() {
        super();
    }

    /**
 * @param destinationFolder
 * @param sourceFile
 */
    public MHImporter(MessageFolder destinationFolder, File[] sourceFiles) {
        super(destinationFolder, sourceFiles);
    }

    public int getType() {
        return TYPE_DIRECTORY;
    }

    /* (non-Javadoc)
 * @see org.columba.mail.folder.mailboximport.AbstractMailboxImporter#importMailbox(java.io.File, org.columba.core.command.WorkerStatusController)
 */
    public void importMailboxFile(File directory,
        WorkerStatusController worker, MessageFolder destFolder)
        throws Exception {
        File[] list = directory.listFiles(MHMessageFileFilter.getInstance());

        for (int i = 0; i < list.length; i++) {
            File file = list[i];

            String name = file.getName();

            if (name.equals(".") || name.equals("..")) {
                continue;
            }

            if (name.startsWith(".")) {
                continue;
            }

            if ((file.exists()) && (file.length() > 0)) {
                importMessage(file, worker);
            }
        }
    }

    protected void importMessage(File file, WorkerStatusController worker)
        throws Exception {
        StringBuffer strbuf = new StringBuffer();

        BufferedReader in = new BufferedReader(new FileReader(file));
        String str;
        strbuf = new StringBuffer();

        while ((str = in.readLine()) != null) {
            strbuf.append(str + "\n");
        }

        in.close();

        saveMessage(strbuf.toString(), worker, getDestinationFolder());
    }
    
    public String getDescription() {
        //TODO: Add proper description here
        return "";
    }
}
