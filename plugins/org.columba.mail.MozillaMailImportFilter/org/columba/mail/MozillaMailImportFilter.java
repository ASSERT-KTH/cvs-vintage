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

package org.columba.mail;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.Vector;
import java.util.logging.Logger;

import org.columba.core.command.WorkerStatusController;
import org.columba.mail.folder.MessageFolder;
import org.columba.mail.folder.FolderFactory;
import org.columba.mail.folder.AbstractFolder;
import org.columba.mail.folder.mailboximport.AbstractMailboxImporter;
import org.columba.core.facade.DialogFacade;

/**
 * @author frd
 */
public class MozillaMailImportFilter extends AbstractMailboxImporter {
    
    /** JDK 1.4+ logging framework logger, used for logging. */
    private static final Logger LOG = Logger.getLogger("org.columba.mail");
    
    public MozillaMailImportFilter() {
        super();
    }
    
    /**
     * @param destination
     * @param sourceFiles
     */
    public MozillaMailImportFilter(MessageFolder destination, File[] sourceFiles) {
        super(destination, sourceFiles);
    }
    
    public String getDescription() {
        //TODO: i18n
        return "Mozilla Mail Import filter for a complete account tree\n";
    }
    
    public void importMailboxFile(File file, WorkerStatusController worker,
        MessageFolder destFolder) throws Exception {
        
        boolean sucess = false;
        
        StringBuffer strbuf = new StringBuffer();
        
        BufferedReader in = new BufferedReader(new FileReader(file));
        String str;
        
        // parse line by line
        while ((str = in.readLine()) != null) {
            // if user cancelled task exit immediately
            if (worker.cancelled()) {
                return;
            }
            
            // if line doesn't start with "From" or line length is 0
            //  -> save everything in StringBuffer
            if (!str.startsWith("From ") || str.length() == 0) {
                strbuf.append(str + "\n");
            } else {
                // line contains "-" (mozilla mbox style)
                //  -> import message in Columba
                if (str.indexOf("-") != -1) {
                    if (strbuf.length() != 0) {
                        // found new message
                        saveMessage(strbuf.toString(), worker, destFolder);
                        sucess = true;
                    }
                    strbuf.delete(0, strbuf.length());
                } else {
                    strbuf.append(str + "\n");
                }
            }
        }
        
        // save last message, because while loop aborted before being able to
        // save message
        if (sucess && (strbuf.length() > 0)) {
            saveMessage(strbuf.toString(), worker, destFolder);
        }
        
        in.close();
    }
    
    protected void generateDirectoryListing(File parent, Vector v,
        MessageFolder destination, WorkerStatusController worker) {
        // list all files
        File[] list = parent.listFiles();
        
        for (int i = 0; i < list.length; i++) {
            File file = list[i];
            LOG.fine("mailbox=" + file.getPath());
            
            // skip these config files
            if (file.getName().endsWith(".msf") || file.getName().endsWith(".dat")) {
                continue;
            }
            
            if (file.getName().endsWith(".sbd")) {
                // directory found
                
                try {
                    
                    String filename = file.getName().substring(0,
                        file.getName().indexOf(".sbd"));
                    
                    if (destination.findChildWithName(filename, false) == null) {
                        /*
                         * // folder doesn't exist -> create it
                         * destination = (Folder)destination.addFolder(filename);
                         * generateDirectoryListing( file, v, destination, worker);
                         */
                    } else {
                        generateDirectoryListing(file, v, destination, worker);
                    }
                } catch (Exception ex) {
                    DialogFacade.showExceptionDialog(ex);
                }
            }
            
            LOG.fine("found mailbox=" + file.getPath() + " - importing to folder="
                + destination.getName());
            
            // import mailbox file
            try {
                if (destination.findChildWithName(file.getName(), false) == null) {
                    // folder doesn't exist -> create it
                    AbstractFolder child = 
                        FolderFactory.getInstance().createDefaultChild(
                            destination, file.getName());
                    importMailboxFile(file, worker, (MessageFolder)child);
                } else {
                    importMailboxFile(file, worker, destination);
                }
            } catch (Exception ex) {
                DialogFacade.showExceptionDialog(ex);
            }
        }
    }
    
    public void importMailbox(WorkerStatusController worker) {
        File[] listing = getSourceFiles();
        
        // we just want to import one profile
        //  -> so, only use the first element which
        //  -> should point to the profile-directory
        File[] files = getSourceFiles();
        File accountDirectory = files[0];
        
        Vector result = new Vector();
        generateDirectoryListing(accountDirectory, result, getDestinationFolder(),
            worker);
    }
}
