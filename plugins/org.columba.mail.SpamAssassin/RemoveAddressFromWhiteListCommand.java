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
import java.util.logging.Logger;

import org.columba.core.command.Command;
import org.columba.core.command.ICommandReference;
import org.columba.core.command.WorkerStatusController;
import org.columba.core.folder.IFolderCommandReference;
import org.columba.core.gui.frame.FrameMediator;
import org.columba.mail.folder.IMailbox;
import org.columba.ristretto.message.Header;


/**
 * @author fdietz
 *

 */
public class RemoveAddressFromWhiteListCommand extends Command {

    /** JDK 1.4+ logging framework logger, used for logging. */
    private static final Logger LOG = Logger.getAnonymousLogger();

    /**
     *
     * @param references
     */
    public RemoveAddressFromWhiteListCommand(
    		ICommandReference references) {
        super(references);
    }

    /**
     *
     * @param frame
     * @param references
     */
    public RemoveAddressFromWhiteListCommand(FrameMediator frame,
    		ICommandReference references) {
        super(frame, references);
    }

    /**
     * @see org.columba.core.command.Command#execute(org.columba.core.command.Worker)
     */
    public void execute(WorkerStatusController worker) throws Exception {
    	IFolderCommandReference r = (IFolderCommandReference) getReference();

        Object[] uids = r.getUids();
        IMailbox folder = (IMailbox) r.getSourceFolder();

        for (int i = 0; i < uids.length; i++) {
            Header header = folder.getHeaderFields(uids[i],
                    new String[] { "From" });
            String sender = (String) header.get("From");

            removeSender(sender);
        }
    }

    public void removeSender(String sender) {
        if (sender == null) {
            return;
        }

        if (sender.length() > 0) {
            IPCHelper ipcHelper = new IPCHelper();

            if (sender.length() > 0) {
                int exitVal = -1;

                try {
                    LOG.info("creating process..");

                    String cmd = "spamassassin -a --remove-addr-from-whitelist=\"" +
                        sender + "\"";
                    ipcHelper.executeCommand(cmd);

                    exitVal = ipcHelper.waitFor();

                    LOG.info("exitcode=" + exitVal);

                    ipcHelper.waitForThreads();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }
    }
}
