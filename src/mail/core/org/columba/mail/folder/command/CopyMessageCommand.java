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
package org.columba.mail.folder.command;

import java.io.IOException;
import java.io.InputStream;
import java.text.MessageFormat;
import java.util.logging.Logger;

import javax.swing.JOptionPane;

import org.columba.core.command.Command;
import org.columba.core.command.DefaultCommandReference;
import org.columba.core.command.StatusObservableImpl;
import org.columba.core.command.WorkerStatusController;
import org.columba.mail.command.FolderCommand;
import org.columba.mail.command.FolderCommandAdapter;
import org.columba.mail.command.FolderCommandReference;
import org.columba.mail.folder.MessageFolder;
import org.columba.mail.gui.frame.TableUpdater;
import org.columba.mail.gui.table.model.TableModelChangedEvent;
import org.columba.mail.main.MailInterface;
import org.columba.mail.util.MailResourceLoader;
import org.columba.ristretto.message.Attributes;
import org.columba.ristretto.message.Flags;


/**
 * Copy a set of messages from a source to a destination
 * folder.
 * <p>
 * A dialog asks the user the destination folder.
 *
 * @author fdietz
 *
 */
public class CopyMessageCommand extends FolderCommand {

    /** JDK 1.4+ logging framework logger, used for logging. */
    private static final Logger LOG = Logger.getLogger("org.columba.mail.folder.command");

    protected FolderCommandAdapter adapter;
    protected MessageFolder destFolder;

    /**
     * Constructor for CopyMessageCommand.
     * @param frameMediator
     * @param references
     */
    public CopyMessageCommand(DefaultCommandReference[] references) {
        super(references);

        commandType = Command.UNDOABLE_OPERATION;
    }

    public void updateGUI() throws Exception {
        // notify table of changes
        TableModelChangedEvent ev = new TableModelChangedEvent(TableModelChangedEvent.UPDATE,
                destFolder);

        TableUpdater.tableChanged(ev);

        // notify treemodel
        MailInterface.treeModel.nodeChanged(destFolder);
    }

    protected void doExecute(WorkerStatusController worker,String statusMessage,
                             String errorRetryMessage,String errorIgnoreMessage,
                             String errorCopyMessage,String errorTitle,
                             String canceledMessage)
    	throws Exception
    {
	     // get references
	    FolderCommandReference[] references = (FolderCommandReference[]) getReferences();
	
	    // use wrapper class
	    adapter = new FolderCommandAdapter(references);
	
	    // get source references
	    FolderCommandReference[] r = adapter.getSourceFolderReferences();
	
	    // get destination foldedr
	    destFolder = adapter.getDestinationFolder();
	
	    // for each message
	    for (int i = 0; (i < r.length) && !worker.cancelled(); i++)
	    {
	      Object[] uids = r[i].getUids();
	
	      // get source folder
	      MessageFolder srcFolder = (MessageFolder) r[i].getFolder();
	
	      // register for status events
	      ((StatusObservableImpl) srcFolder.getObservable()).setWorker(worker);
	
	      // setting lastSelection for srcFolder to null
	      srcFolder.setLastSelection(null);
	
	      LOG.fine("src=" + srcFolder + " dest=" + destFolder);
	
	      // update status message
	      worker
	            .setDisplayText(MessageFormat.format(
	                           		MailResourceLoader.getString("statusbar",
	                                                           "message",
	                                                           statusMessage),
	                             	new Object[]{destFolder.getName()}));
	
	      // initialize progress bar with total number of messages
	      worker.setProgressBarMaximum(uids.length);
	
	      if (srcFolder.getRootFolder().equals(destFolder.getRootFolder()))
	      {
	        // folders have same root folder
	        // -> for example: two IMAP folders on the same server
	        // -----> this means we use server-side copying which
	        // -----> is much faster than using inputstreams here
	        //
	        // also used for local folders, which saves some parsing work
	        srcFolder.innerCopy(destFolder, uids);
	      }
	      else
	      {
	        // two different root folders
	        // -> get inputstream from source-folder and add it to
	        // -> destination-folder as inputstream
	        // -----> moving of raw message source
	        // (works also for copying from local to IMAP folders, etc.
	        for (int j = 0; (j < uids.length) && !worker.cancelled(); j++)
	        {
	          if (!srcFolder.exists(uids[j]))
	          {
	            continue;
	          }
	
	          try
	          {
	            // add source to destination folder
	            Attributes attributes = srcFolder.getAttributes(uids[j]);
	            Flags flags = srcFolder.getFlags(uids[j]);
	            InputStream messageSourceStream = srcFolder
	                                                       .getMessageSourceStream(uids[j]);
	            destFolder.addMessage(messageSourceStream, attributes, flags);
	            messageSourceStream.close();
	          }
	          catch (IOException ioe)
	          {
	            String[] options = new String[]{
	                MailResourceLoader.getString("statusbar", "message",
	                                             errorRetryMessage),
	                MailResourceLoader.getString("statusbar", "message",
	                                             errorIgnoreMessage),
	                MailResourceLoader.getString("", "global", "cancel")};
	
	            int result = 
	              JOptionPane.showOptionDialog(	null,
	                                          	MailResourceLoader.getString("statusbar",
	                                                                         "message",
	                                                                         errorCopyMessage),
	                                            MailResourceLoader.getString("statusbar",
	                                                                         "message",
	                                                                         errorTitle),
	                                            JOptionPane.YES_NO_CANCEL_OPTION,
	                                            JOptionPane.ERROR_MESSAGE,
	                                            null, 
	                                            options, 
	                                            options[0]);
	            switch (result)
	            {
	              case JOptionPane.YES_OPTION :
	
	                //retry copy
	                j--;
	
	                break;
	
	              case JOptionPane.CANCEL_OPTION :
	                worker.cancel();
	
	              default :
	
	                continue;
	            }
	          }
	
	          // update progress bar
	          worker.setProgressBarValue(j);
	        }
	      }
	
	      //reset progress bar
	      worker.setProgressBarValue(0);
	    }
	
	    if (worker.cancelled())
	    {
	      worker.setDisplayText(MailResourceLoader.getString("statusbar",
	                                                         "message",
	                                                         canceledMessage));
	    }
	    else
	    {
	      // We are done - clear the status message with a delay
	      worker.clearDisplayTextWithDelay();
	    }
        
    }
    
    /**
     * @see org.columba.core.command.Command#execute(Worker)
     */
    public void execute(WorkerStatusController worker) throws Exception {
      doExecute(worker,
                "copy_messages",
                "err_copy_messages_retry",
                "err_copy_messages_ignore",
                "err_copy_messages_msg",
                "err_copy_messages_title",
                "copy_messages_cancelled");
    }
}
